package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.*
import org.cloud_assess.model.ResourceTrace
import org.cloud_assess.model.ResourceTraceAnalysis
import org.cloud_assess.service.ParsingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TraceService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
    @Value("\${COMPUTE_JOB_SIZE:100}")
    private val jobSize: Int = 100,
) {
    private val dataReducer = DataExpressionReducer(
        dataRegister = symbolTable.data,
        dataSourceRegister = DataSourceRegister.empty(),
        ops = BasicOperations,
        sourceOps = defaultDataSourceOperations,
    )

    private fun localEval(expression: DataExpression<BasicNumber>): DataValue<BasicNumber> {
        val data = dataReducer.reduce(expression)
        return with(ToValue(BasicOperations)) {
            data.toValue()
        }
    }

    fun analyze(request: TraceRequestListDto): ResourceTraceAnalysis {
        val meta = request.meta ?: emptyMap()
        val commonGlobals = request.globals ?: emptyList()
        val commonDatasources = request.datasources ?: emptyList()
        val elements = request.elements
            .chunked(jobSize)
            .parallelStream()
            .map { job ->
                job.map {
                    val merged = it.copy(
                        globals = commonGlobals.plus(it.globals ?: emptyList()),
                        datasources = commonDatasources.plus(it.datasources ?: emptyList()),
                    )
                    mapOf(it.requestId to analyze(merged))
                }.fold(emptyMap<String, ResourceTrace>()) { acc, element -> acc.plus(element) }
            }.reduce { acc, element -> acc.plus(element) }
            .orElse(emptyMap())
        return ResourceTraceAnalysis(meta, elements)
    }

    fun analyze(request: TraceRequestDto): ResourceTrace {
        val content = overriddenDatasources(request)

        val inMemoryConnector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(cacheEnabled = true, cacheSize = 1024),
            content = content,
        )
        val sourceOps = defaultDataSourceOperations.overrideWith(inMemoryConnector)

        val dataSources = inMemoryDataSources(request.datasources)
        val newDataSources = symbolTable.dataSources.override(dataSources)

        val overriddenSymbolTable = symbolTable.copy(
            data = globals(symbolTable.data, request),
            dataSources = newDataSources,
        )

        val evaluator = Evaluator(overriddenSymbolTable, BasicOperations, sourceOps)

        val processApplication = prepare(request)
        val trace = evaluator
            .with(processApplication.template)
            .trace(processApplication.template, processApplication.arguments)
        val systemValue = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val program = ContributionAnalysisProgram(systemValue, entryPoint)
        val analysis = program.run()

        return ResourceTrace(
            id = request.requestId,
            meta = request.meta ?: emptyMap(),
            rawTrace = trace,
            defaultMaxDepth = request.maxDepth ?: -1,
            contributionAnalysis = analysis,
        )
    }

    private fun getSchemaOf(name: String): Map<String, DataExpression<BasicNumber>> {
        return symbolTable.getDataSource(name)
            ?.schema
            ?: throw IllegalArgumentException("unknown datasource '$name'")
    }

    private fun overriddenDatasources(request: TraceRequestDto): Map<String, InMemoryDatasource<BasicNumber>> {
        val datasources = request.datasources ?: emptyList()
        return datasources
            .associate {
                val schema = getSchemaOf(it.name)
                it.name to InMemoryDatasource(
                    records = it.records.map { r -> record(schema, r) }
                )
            }
    }

    private fun record(schema: Map<String, DataExpression<BasicNumber>>, record: RecordDto): RecordValue<BasicNumber> {
        return record.elements
            .filter {
                schema.containsKey(it.name)
            }
            .associate { entry ->
                entry.name to localEval(entry.toDataExpression(schema))
            }.let { RecordValue(it) }
    }

    private fun EntryDto.toDataExpression(schema: Map<String, DataExpression<BasicNumber>>): DataExpression<BasicNumber> {
        return when (val defaultValue = schema[this.name]) {
            is QuantityExpression<*> -> when (val v = this.value) {
                is VNum -> EQuantityScale(BasicNumber(v.value), EUnitOf(defaultValue))
                is VStr -> throw IllegalArgumentException("invalid value for entry '${this.name}': expected 'number', found 'string'")
            }

            is EStringLiteral -> when (val v = this.value) {
                is VNum -> throw IllegalArgumentException("invalid value for entry '${this.name}': expected 'string', found 'number'")
                is VStr -> EStringLiteral(v.value)
            }

            else -> throw IllegalArgumentException("invalid datasource column '${this.name}'")
        }
    }

    private fun globals(dataRegister: DataRegister<BasicNumber>, request: TraceRequestDto): DataRegister<BasicNumber> {
        val requestGlobals = request.globals ?: emptyList()
        return requestGlobals.fold(dataRegister) { register, parameter ->
            register.override(
                DataKey(parameter.name),
                parameter(parameter)
            )
        }
    }

    private fun parameter(parameter: ParameterDto): DataExpression<BasicNumber> {
        return when (parameter.value) {
            is PVNum -> {
                val amount = parameter.value.amount
                val unit = parsingService.data(parameter.value.unit)
                EQuantityScale(BasicNumber(amount), unit)
            }

            is PVStr -> EStringLiteral(parameter.value.value)
        }
    }

    private fun prepare(request: TraceRequestDto): EProcessTemplateApplication<BasicNumber> {
        val demand = request.demand
        val quantity = demand.quantity
        val arguments = demand.params?.associate {
            it.name to parameter(it)
        } ?: emptyMap()
        val labels = MatchLabels<BasicNumber>(
            demand.labels?.associate { it.name to EStringLiteral(it.value) }
                ?: emptyMap())

        return EProcessTemplateApplication(
            template = EProcessTemplate(
                body = EProcess(
                    "__main__",
                    products = listOf(
                        ETechnoExchange(
                            quantity = EQuantityScale(BasicNumber(1.0), EDataRef("u")),
                            product = EProductSpec("__main__", referenceUnit = EDataRef("u")),
                        )
                    ),
                    inputs = listOf(
                        ETechnoBlockEntry(
                            ETechnoExchange(
                                quantity = EQuantityScale(BasicNumber(quantity.amount), EDataRef(quantity.unit)),
                                product = EProductSpec(
                                    demand.productName,
                                    fromProcess = FromProcess(
                                        demand.processName,
                                        matchLabels = labels,
                                        arguments = arguments,
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            arguments = emptyMap(),
        )
    }

    private fun inMemoryDataSources(dtos: List<DatasourceDto>?): Map<DataSourceKey, EDataSource<BasicNumber>> {
        val sources = dtos?.map { dto ->
            val key = DataSourceKey(dto.name)
            val schema = dto.records[0].elements.associate { it.name to EStringLiteral<BasicNumber>(it.value.toString()) }
            val source = EDataSource(
                DataSourceConfig(dto.name, IN_MEMORY_CONNECTOR_NAME, ""),
                schema
            )
            Pair(key, source)
        }
        return sources?.associate { it.first to it.second } ?: emptyMap()
    }
}
