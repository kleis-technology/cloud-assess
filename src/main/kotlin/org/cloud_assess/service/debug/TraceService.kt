package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.OverriddenDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.*
import org.cloud_assess.model.ResourceTrace
import org.cloud_assess.service.ParsingService
import org.springframework.stereotype.Service

@Service
class TraceService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    fun analyze(request: TraceRequestListDto): Map<String, ResourceTrace> {
        return request.elements
            .associate { it.requestId to analyze(it) }
    }

    fun analyze(request: TraceRequestDto): ResourceTrace {
        val processApplication = prepare(request)
        val symbolTablesWithGlobals = symbolTable.copy(
            data = globals(symbolTable.data, request),
        )
        val sourceOps = OverriddenDataSourceOperations(
            overriddenDatasources(request),
            BasicOperations,
            defaultDataSourceOperations,
        )
        val evaluator = Evaluator(symbolTablesWithGlobals, BasicOperations, sourceOps)
        val trace = evaluator
            .with(processApplication.template)
            .trace(processApplication.template, processApplication.arguments)
        val systemValue = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val program = ContributionAnalysisProgram(systemValue, entryPoint)
        val analysis = program.run()

        return ResourceTrace(
            id = request.requestId,
            rawTrace = trace,
            contributionAnalysis = analysis,
        )
    }

    private fun overriddenDatasources(request: TraceRequestDto): Map<String, List<ERecord<BasicNumber>>> {
        val datasources = request.datasources ?: emptyList()
        return datasources
            .associate {
                val schema = this.symbolTable.getDataSource(it.name)?.schema
                    ?: throw IllegalArgumentException("unknown datasource '${it.name}'")
                it.name to it.records.map { r -> record(schema, r) }
            }
    }

    private fun record(schema: Map<String, DataExpression<BasicNumber>>, record: RecordDto): ERecord<BasicNumber> {
        return ERecord(
            record.elements
                .filter {
                    schema.containsKey(it.name)
                }
                .associate {
                    val defaultValue = schema[it.name]
                        ?: throw IllegalArgumentException("unknown column '${it.name}'")
                    it.name to entry(defaultValue, it.value)
                }
        )
    }

    private fun entry(defaultValue: DataExpression<BasicNumber>, entry: EntryValueDto): DataExpression<BasicNumber> {
        return when (entry) {
            is VNum -> when (defaultValue) {
                is QuantityExpression<*> -> EQuantityScale(
                    BasicNumber(entry.value),
                    EUnitOf(defaultValue),
                )

                else -> throw IllegalArgumentException("expecting type 'number', found 'string'")
            }

            is VStr -> when (defaultValue) {
                is StringExpression -> EStringLiteral(entry.value)
                else -> throw IllegalArgumentException("expecting type 'string', found 'number'")
            }
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
}
