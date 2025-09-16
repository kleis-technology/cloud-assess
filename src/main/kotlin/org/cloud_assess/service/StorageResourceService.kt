package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EDataSource
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.StorageResourceListDto
import org.cloud_assess.model.ProductMatcher
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.stereotype.Service

@Service
class StorageResourceService(
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

    @Suppress("DuplicatedCode")
    fun analyze(storageResources: StorageResourceListDto): Map<String, ResourceAnalysis> {
        val period = with(helper) {
            storageResources.period.toDataExpression()
        }
        val cases = cases(storageResources)

        val storageResourcesConnector = inMemoryConnector(storageResources)
        val sourceOps = defaultDataSourceOperations.overrideWith(storageResourcesConnector)

        val storageResourcesDatasource = inMemoryDataSource()
        val newDataSources = symbolTable.dataSources.override(mapOf(storageResourcesDatasource))

        val evaluator = Evaluator(
            symbolTable.copy(
                data = symbolTable.data.override(
                    DataKey(overrideTimeWindowParam),
                    period,
                ),
                dataSources = newDataSources,
            ),
            BasicOperations,
            sourceOps,
        )
        val productMatcher: (String) -> ProductMatcher = { id ->
            ProductMatcher(
                name = "storage",
                process = "storage_space_fn",
                arguments = mapOf("id" to id)
            )
        }
        val analysis = cases.entries
            .map {
                val arguments = it.value.arguments
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

    private fun inMemoryDataSource(): Pair<DataSourceKey, EDataSource<BasicNumber>> {
        val key = DataSourceKey(overriddenDataSourceName)
        val source = EDataSource<BasicNumber>(
            DataSourceConfig(overriddenDataSourceName, IN_MEMORY_CONNECTOR_NAME, "", "id"),
            mapOf(
                "id" to EStringLiteral("sto-01"),
                "pool_id" to EStringLiteral("client_vm"),
                "vcpu_size" to EStringLiteral("1 vCPU"),
                "volume" to EStringLiteral("4 GB"),
                "quantity" to EStringLiteral("1 p")
            )
        )
        return Pair(key,source)
    }

    private fun cases(storageResources: StorageResourceListDto): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val period = with(helper) { storageResources.period.toLcaac() }
        val totalVcpu = with(helper) { storageResources.totalVcpu.toLcaac() }
        val totalStorage = with(helper) { storageResources.totalStorage.toLcaac() }
        val cases = storageResources.storageResources.associate {
            val content = """
                process __main__ {
                    products {
                        1 u __main__
                    }
                    inputs {
                        $period storage from storage_space_fn(
                            id = "${it.id}",
                            total_vcpu = ${totalVcpu},
                            total_storage = ${totalStorage},
                            )
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
        return cases
    }
}
