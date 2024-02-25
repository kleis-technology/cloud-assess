package org.cloud_assess.model

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.QuantityTimeDto

class VirtualMachineAnalysis(
    id: String,
    val period: QuantityTimeDto,
    private val rawAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
) {
    private val main = rawAnalysis.getObservablePorts().getElements()
        .filterIsInstance<ProductValue<BasicNumber>>()
        .firstOrNull { it.name == "__main__" }
        ?: throw IllegalStateException("no impacts found for id=$id")

    fun contribution(target: Indicator): QuantityValue<BasicNumber> {
        return rawAnalysis.getPortContribution(main, indicator(target))
    }

    private fun indicator(indicator: Indicator): IndicatorValue<BasicNumber> {
        return rawAnalysis.getControllablePorts().getElements()
            .filterIsInstance<IndicatorValue<BasicNumber>>()
            .firstOrNull { it.name == indicator.name }
            ?: throw IllegalStateException("no impact found for indicator=${indicator.name}")
    }
}
