package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.springframework.stereotype.Service

@Service
class ExecutionService(
    private val evaluator: Evaluator<BasicNumber>,
) {
    fun run(expression: EProcessTemplateApplication<BasicNumber>): ContributionAnalysis<BasicNumber, BasicMatrix> {
        val trace = evaluator.with(expression.template).trace(expression.template, expression.arguments)
        val systemValue = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val program = ContributionAnalysisProgram(systemValue, entryPoint)
        return program.run()
    }
}
