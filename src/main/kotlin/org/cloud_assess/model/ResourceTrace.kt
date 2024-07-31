package org.cloud_assess.model

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber

class ResourceTrace(
    private val id: String,
    private val rawTrace: EvaluationTrace<BasicNumber>,
    private val contributionAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
) {
    fun getRequestId(): String = id
    fun isEmpty(): Boolean = rawTrace.getNumberOfProcesses() == 0
    fun isNotEmpty(): Boolean = rawTrace.getNumberOfProcesses() > 0
}
