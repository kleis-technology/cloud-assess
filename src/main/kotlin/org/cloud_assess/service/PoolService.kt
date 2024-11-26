package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.PoolListDto
import org.cloud_assess.dto.TimeUnitsDto
import org.cloud_assess.model.ProductMatcher
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.stereotype.Service

@Service
class PoolService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {

    @Suppress("DuplicatedCode")
    fun analyze(pools: PoolListDto): Map<String, ResourceAnalysis> {
        val period = pools.period
        val cases = cases(pools)
        val evaluator = Evaluator(symbolTable, BasicOperations, defaultDataSourceOperations)
        val analysis = cases
            .entries.parallelStream()
            .map {
                val trace = evaluator.with(it.value.template).trace(it.value.template, it.value.arguments)
                val systemValue = trace.getSystemValue()
                val entryPoint = trace.getEntryPoint()
                val program = ContributionAnalysisProgram(systemValue, entryPoint)
                val rawAnalysis = program.run()
                mapOf(
                    it.key to ResourceAnalysis(
                        ProductMatcher(
                            name = "service",
                            process = "service_fn",
                            arguments = mapOf("id" to it.key)
                        ), period, rawAnalysis
                    )
                )
            }.reduce { acc, element -> acc.plus(element) }
            .orElse(emptyMap())
        return analysis
    }

    private fun cases(
        pools: PoolListDto,
    ): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val period = when (pools.period.unit) {
            TimeUnitsDto.hour -> "${pools.period.amount} hour"
        }
        val cases = pools.pools.associate {
            val content = """
                process __main__ {
                    products {
                        1 u __main__
                    }
                    inputs {
                        $period service from service_fn(id = "${it.id}")
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
        return cases
    }
}
