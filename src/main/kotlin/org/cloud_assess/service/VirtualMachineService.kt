package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.*
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.stereotype.Service

@Service
class VirtualMachineService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    private val overrideTimeWindowParam = "vm_timewindow"
    private val overriddenDataSourceName = "vm_inventory"

    @Suppress("DuplicatedCode")
    fun analyze(vms: VirtualMachineListDto): Map<String, ResourceAnalysis> {
        val period = vms.period
        val cases = cases(vms)
        val sourceOps = overriddenDataSource(vms)
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
        val analysis = cases.entries
            .parallelStream()
            .map {
                val trace = evaluator.with(it.value.template).trace(it.value.template, it.value.arguments)
                val systemValue = trace.getSystemValue()
                val entryPoint = trace.getEntryPoint()
                val program = ContributionAnalysisProgram(systemValue, entryPoint)
                val rawAnalysis = program.run()
                mapOf(it.key to ResourceAnalysis(it.key, period, rawAnalysis))
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
                        $period vm from vm(id = "${it.id}")
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
        return cases
    }

    private fun overriddenDataSource(
        vms: VirtualMachineListDto
    ): DefaultDataSourceOperations<BasicNumber> {
        val records = vms.virtualMachines
            .map { vm ->
                ERecord(
                    mapOf(
                        "id" to vm.id.toDataExpression(),
                        "pool_id" to vm.poolId.toDataExpression(),
                        "ram_size" to vm.ram.toDataExpression(),
                        "storage_size" to vm.storage.toDataExpression(),
                        "vcpu_size" to vm.vcpu.toDataExpression(),
                    )
                )
            }
        val content = mapOf(
            overriddenDataSourceName to InMemoryDatasource(records)
        )
        val inMemoryConnector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(cacheEnabled = true, cacheSize = 1024),
            content = content,
        )
        return defaultDataSourceOperations.overrideWith(inMemoryConnector)
    }

    private fun String.toDataExpression(): DataExpression<BasicNumber> = EStringLiteral(this)

    private fun QuantityTimeDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            TimeUnitsDto.hour -> EQuantityScale(BasicNumber(this.amount), EDataRef("hour"))
        }
    }

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
}
