package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.in_memory.*
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.QuantityTimeDto
import org.cloud_assess.dto.TimeUnitsDto
import org.cloud_assess.dto.VirtualMachineListDto
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
                mapOf(
                    "id" to InMemStr(vm.id),
                    "pool_id" to InMemStr(vm.poolId),
                    "ram_size" to InMemNum(vm.ram.amount), // TODO: handle user units
                    "storage_size" to InMemNum(vm.storage.amount),
                    "vcpu_size" to InMemNum(vm.vcpu.amount),
                )
            }
        val content = mapOf(
            overriddenDataSourceName to InMemoryDatasource(records)
        )
        val inMemoryConnector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(cacheEnabled = true, cacheSize = 1024),
            content = content,
            ops = BasicOperations,
        )
        return defaultDataSourceOperations.overrideWith(inMemoryConnector)
    }

    private fun QuantityTimeDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            TimeUnitsDto.hour -> EQuantityScale(BasicNumber(this.amount), EDataRef("hour"))
        }
    }
}
