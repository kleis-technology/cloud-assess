package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.DimensionlessUnitsDto
import org.cloud_assess.dto.PoolListDto
import org.cloud_assess.dto.TimeUnitsDto
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.stereotype.Service

@Service
class PoolService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {

    fun analyze(pools: PoolListDto): Map<String, ResourceAnalysis> {
        val period = pools.period
        val cases = cases(pools)
        val evaluator = Evaluator(symbolTable, BasicOperations, defaultDataSourceOperations)
        val analysis = cases.mapValues {
            val trace = evaluator.with(it.value.template).trace(it.value.template, it.value.arguments)
            val systemValue = trace.getSystemValue()
            val entryPoint = trace.getEntryPoint()
            val program = ContributionAnalysisProgram(systemValue, entryPoint)
            val rawAnalysis = program.run()
            ResourceAnalysis(it.key, period, rawAnalysis)
        }
        return analysis
    }

    private fun cases(
        pools: PoolListDto,
    ): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val period = when (pools.period.unit) {
            TimeUnitsDto.hour -> "${pools.period.amount} hour"
        }
        val cases = pools.pools.associate {
            val serviceLevel = when (it.serviceLevel.unit) {
                DimensionlessUnitsDto.u -> "${it.serviceLevel.amount} u"
            }
            val content = """
                process __main__ {
                    products {
                        1 u __main__
                    }
                    inputs {
                        $period service from service(pool_id = "${it.id}", service_level = $serviceLevel)
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
        return cases
    }
}
