package org.cloud_assess.service

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
import org.cloud_assess.dto.StorageResourceListDto
import org.cloud_assess.model.ProductMatcher
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class StorageResourceService(
    @Value("\${COMPUTE_JOB_SIZE:100}")
    private val jobSize: Int,
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    private val overrideTimeWindowParam = "timewindow"
    private val overriddenDataSourceName = "storage_space_inventory"
    private val helper = Helper(
        defaultDataSourceOperations,
        symbolTable,
    )
    fun analyze(storageResources: StorageResourceListDto): Map<String, ResourceAnalysis> {
        val period = with(helper) {
            storageResources.period.toDataExpression()
        }
        val cases = cases(storageResources)
        val storageResourcesConnector = inMemoryConnector(storageResources)
        val sourceOps = defaultDataSourceOperations.overrideWith(storageResourcesConnector)
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
        val jobRunner = AnalysisJobRunner(
            jobSize = jobSize,
            productMatcher = { id ->
                ProductMatcher(
                    name = "storage",
                    process = "storage_space_fn",
                    arguments = mapOf("id" to id)
                )
            },
            periodDto = storageResources.period,
            evaluator = evaluator,
        )
        val analysis = jobRunner.run(cases)
        return analysis
    }

    private fun inMemoryConnector(storageResources: StorageResourceListDto): InMemoryConnector<BasicNumber> {
        val records = with(helper) {
            storageResources.storageResources
                .map { storageResource ->
                    RecordValue(
                        mapOf(
                            "id" to localEval(storageResource.id.toDataExpression()),
                            "pool_id" to localEval(storageResource.poolId.toDataExpression()),
                            "vcpu_size" to localEval(storageResource.vcpu.toDataExpression()),
                            "volume" to localEval(storageResource.storage.toDataExpression()),
                            "quantity" to localEval(storageResource.quantity.toDataExpression()),
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

    private fun cases(storageResources: StorageResourceListDto): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val period = with(helper) { storageResources.period.toLcaac() }
        val cases = storageResources.storageResources.associate {
            val content = """
                process __main__ {
                    products {
                        1 u __main__
                    }
                    inputs {
                        $period storage from storage_space_fn(id = "${it.id}")
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
        return cases
    }
}
