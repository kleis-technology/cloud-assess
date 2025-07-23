package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.VirtualMachineListDto
import org.cloud_assess.model.ProductMatcher
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VirtualMachineService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    private val overrideTimeWindowParam = "timewindow"
    private val overriddenDataSourceName = "vm_inventory"
    private val helper = Helper(
        defaultDataSourceOperations,
        symbolTable,
    )

    @Suppress("DuplicatedCode")
    fun analyze(vms: VirtualMachineListDto): Map<String, ResourceAnalysis> {
        val period = with(helper) {
            vms.period.toDataExpression()
        }
        val cases = cases(vms)
        val vmsConnector = inMemoryConnector(vms)
        val sourceOps = defaultDataSourceOperations.overrideWith(vmsConnector)
        val evaluator = Evaluator(
            symbolTable.copy(
                data = symbolTable.data.override(
                    DataKey(overrideTimeWindowParam),
                    period,
                )
            ),
            BasicOperations,
            sourceOps,
        )
        val productMatcher: (String) -> ProductMatcher = { id ->
            ProductMatcher(
                name = "vm",
                process = "vm_fn",
                arguments = mapOf("id" to id)
            )
        }
        val analysis = cases.entries
            .map {
                val arguments = it.value.arguments // TODO: override total_vcpu/ram/storage
                val trace = evaluator.with(it.value.template)
                    .trace(it.value.template, arguments)
                val systemValue = trace.getSystemValue()
                val entryPoint = trace.getEntryPoint()
                val program = ContributionAnalysisProgram(systemValue, entryPoint)
                val rawAnalysis = program.run()
                mapOf(
                    it.key to ResourceAnalysis(
                        productMatcher(it.key),
                        rawAnalysis
                    )
                )
            }.fold(emptyMap<String, ResourceAnalysis>()) { acc, element -> acc.plus(element) }
        return analysis
    }

    private fun cases(
        vms: VirtualMachineListDto,
    ): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val period = with(helper) { vms.period.toLcaac() }
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
        val records = with(helper) {
            vms.virtualMachines
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
        }
        val content = mapOf(
            overriddenDataSourceName to InMemoryDatasource(records)
        )
        return InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(cacheEnabled = true, cacheSize = 1024),
            content = content,
        )
    }
}
