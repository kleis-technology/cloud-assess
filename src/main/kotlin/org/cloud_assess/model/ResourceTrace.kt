package org.cloud_assess.model

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber

class ResourceTrace(
    private val id: String,
    private val rawTrace: EvaluationTrace<BasicNumber>,
    private val contributionAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val entryPointRef: String = "__main__",
) {
    fun getRequestId(): String = id
    fun isEmpty(): Boolean = rawTrace.getNumberOfProcesses() == 0
    fun isNotEmpty(): Boolean = rawTrace.getNumberOfProcesses() > 0

    private val main = contributionAnalysis.getObservablePorts().getElements()
        .filterIsInstance<ProductValue<BasicNumber>>()
        .firstOrNull { it.name == entryPointRef }
        ?: throw IllegalStateException("no impacts found for id=$id")

    fun contribution(target: Indicator): QuantityValue<BasicNumber> {
        return contributionAnalysis.getPortContribution(main, indicator(target))
    }

    private fun indicator(indicator: Indicator): IndicatorValue<BasicNumber> {
        return contributionAnalysis.getControllablePorts().getElements()
            .filterIsInstance<IndicatorValue<BasicNumber>>()
            .firstOrNull { it.name == indicator.name }
            ?: throw IllegalStateException("no impact found for indicator=${indicator.name}")
    }
}
