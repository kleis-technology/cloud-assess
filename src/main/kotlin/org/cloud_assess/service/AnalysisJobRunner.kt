package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.QuantityTimeDto
import org.cloud_assess.model.ProductMatcher
import org.cloud_assess.model.ResourceAnalysis

class AnalysisJobRunner(
    private val jobSize: Int,
    private val productMatcher: (String) -> ProductMatcher,
    private val periodDto: QuantityTimeDto,
    private val evaluator: Evaluator<BasicNumber>,

    ) {
    fun run(cases: Map<String, EProcessTemplateApplication<BasicNumber>>): Map<String, ResourceAnalysis> {
        return cases.entries
            .chunked(jobSize)
            .parallelStream()
            .map { job ->
                job.map {
                    val trace = evaluator.with(it.value.template).trace(it.value.template, it.value.arguments)
                    val systemValue = trace.getSystemValue()
                    val entryPoint = trace.getEntryPoint()
                    val program = ContributionAnalysisProgram(systemValue, entryPoint)
                    val rawAnalysis = program.run()
                    mapOf(
                        it.key to ResourceAnalysis(
                            productMatcher(it.key),
                            periodDto,
                            rawAnalysis
                        )
                    )
                }.fold(emptyMap<String, ResourceAnalysis>()) { acc, element -> acc.plus(element) }
            }.reduce { acc, element -> acc.plus(element) }
            .orElse(emptyMap())
    }
}
