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
import org.cloud_assess.dto.ComputeResourceListDto
import org.cloud_assess.model.ProductMatcher
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.stereotype.Service

@Service
class ComputeResourceService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    private val overrideTimeWindowParam = "timewindow"
    private val overriddenDataSourceName = "compute_inventory"
    private val helper = Helper(
        defaultDataSourceOperations,
        symbolTable,
    )


    @Suppress("DuplicatedCode")
    fun analyze(computeResources: ComputeResourceListDto): Map<String, ResourceAnalysis> {
        val period = with(helper) {
            computeResources.period.toDataExpression()
        }
        val cases = cases(computeResources)
        val computeResourcesConnector = inMemoryConnector(computeResources)
        val sourceOps = defaultDataSourceOperations.overrideWith(computeResourcesConnector)
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
                name = "compute",
                process = "compute_fn",
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

    private fun inMemoryConnector(
        computeResources: ComputeResourceListDto,
    ): InMemoryConnector<BasicNumber> {
        val records = with(helper) {
            computeResources.computeResources
                .map { computeResource ->
                    RecordValue(
                        mapOf(
                            "id" to localEval(computeResource.id.toDataExpression()),
                            "pool_id" to localEval(computeResource.poolId.toDataExpression()),
                            "vcpu_size" to localEval(computeResource.vcpu.toDataExpression()),
                            "quantity" to localEval(computeResource.quantity.toDataExpression()),
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

    private fun cases(computeResources: ComputeResourceListDto): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val period = with(helper) { computeResources.period.toLcaac() }
        val totalVcpu = with(helper) { computeResources.totalVcpu.toLcaac() }
        val cases = computeResources.computeResources.associate {
            val content = """
                process __main__ {
                    products {
                        1 u __main__
                    }
                    inputs {
                        $period compute from compute_fn(id = "${it.id}", total_vcpu = ${totalVcpu})
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
        return cases
    }
}
