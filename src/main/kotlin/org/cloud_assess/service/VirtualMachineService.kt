package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.*
import org.cloud_assess.model.ProductMatcher
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VirtualMachineService(
    @Value("\${COMPUTE_JOB_SIZE:100}")
    private val jobSize: Int,
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    private val overrideTimeWindowParam = "vm_timewindow"
    private val overriddenDataSourceName = "vm_inventory"
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

    @Suppress("DuplicatedCode")
    fun analyze(vms: VirtualMachineListDto): Map<String, ResourceAnalysis> {
        val period = vms.period
        val cases = cases(vms)
        val vmsConnector = inMemoryConnector(vms)
        val analysis = cases.entries
            .chunked(jobSize)
            .parallelStream()
            .map { job ->
                job.map {
                    val sourceOps = defaultDataSourceOperations.overrideWith(vmsConnector)
                    val evaluator = Evaluator(
                        symbolTable.copy(
                            data = symbolTable.data.override(
                                DataKey(overrideTimeWindowParam),
                                period.toDataExpression(),
                            )
                        ),
                        BasicOperations,
                        sourceOps,
                    )
                    val trace = evaluator.with(it.value.template).trace(it.value.template, it.value.arguments)
                    val systemValue = trace.getSystemValue()
                    val entryPoint = trace.getEntryPoint()
                    val program = ContributionAnalysisProgram(systemValue, entryPoint)
                    val rawAnalysis = program.run()
                    mapOf(
                        it.key to ResourceAnalysis(
                            ProductMatcher(
                                name = "vm",
                                process = "vm_fn",
                                arguments = mapOf("id" to it.key)
                            ), period, rawAnalysis
                        )
                    )
                }.fold(emptyMap<String, ResourceAnalysis>()) { acc, element -> acc.plus(element) }
            }.reduce { acc, element -> acc.plus(element) }
            .orElse(emptyMap())
        return analysis
    }

    private fun cases(
        vms: VirtualMachineListDto,
    ): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val period = when (vms.period.unit) {
            TimeUnitsDto.hour -> "${vms.period.amount} hour"
        }
        val cases = vms.virtualMachines.associate {
            val content = """
                process __main__ {
                    products {
                        1 u __main__
                    }
                    inputs {
                        $period vm from vm_fn(id = "${it.id}")
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
        return cases
    }

    private fun inMemoryConnector(
        vms: VirtualMachineListDto,
    ): InMemoryConnector<BasicNumber> {
        val records = vms.virtualMachines
            .map { vm ->
                RecordValue(
                    mapOf(
                        "id" to localEval(vm.id.toDataExpression()),
                        "pool_id" to localEval(vm.poolId.toDataExpression()),
                        "ram_size" to localEval(vm.ram.toDataExpression()),
                        "storage_size" to localEval(vm.storage.toDataExpression()),
                        "vcpu_size" to localEval(vm.vcpu.toDataExpression()),
                        "quantity" to localEval(vm.quantity.toDataExpression()),
                    )
                )
            }
        val content = mapOf(
            overriddenDataSourceName to InMemoryDatasource(records)
        )
        return InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(cacheEnabled = true, cacheSize = 1024),
            content = content,
        )
    }

    private fun QuantityTimeDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            TimeUnitsDto.hour -> EQuantityScale(BasicNumber(this.amount), EDataRef("hour"))
        }
    }

    private fun String.toDataExpression(): DataExpression<BasicNumber> = EStringLiteral(this)

    private fun QuantityMemoryDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            MemoryUnitsDto.gB -> EQuantityScale(BasicNumber(this.amount), EDataRef("GB"))
            MemoryUnitsDto.tB -> EQuantityScale(BasicNumber(this.amount), EDataRef("TB"))
        }
    }

    private fun QuantityVCPUDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            VCPUUnitsDto.vCPU -> EQuantityScale(BasicNumber(this.amount), EDataRef("vCPU"))
        }
    }

    private fun QuantityDimensionlessDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            DimensionlessUnitsDto.u -> EQuantityScale(BasicNumber(this.amount), EDataRef("u"))
        }
    }
}
