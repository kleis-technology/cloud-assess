package org.cloud_assess.model

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber

class ResourceAnalysis(
    private val target: ProductMatcher,
    private val rawAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val targetManufacturing: ProductMatcher = target
        .addLabel("phase", "embodied")
        .addArgument("lc_step", "manufacturing"),
    private val targetTransport: ProductMatcher = target
        .addLabel("phase", "embodied")
        .addArgument("lc_step", "transport"),
    private val targetUse: ProductMatcher = target
        .addLabel("phase", "use"),
    private val targetEndOfLife: ProductMatcher = target
        .addLabel("phase", "embodied")
        .addArgument("lc_step", "end-of-life"),
) {
    private val mainPort = rawAnalysis.getObservablePorts().getElements()
        .filterIsInstance<ProductValue<BasicNumber>>()
        .firstOrNull { target.matches(it) }
        ?: throw IllegalStateException("no impacts found for '$target'")
    private val manufacturingPort = rawAnalysis.getObservablePorts().getElements()
        .filterIsInstance<ProductValue<BasicNumber>>()
        .firstOrNull { targetManufacturing.matches(it) }
        ?: throw IllegalStateException("no impacts found for '$targetManufacturing'")
    private val transportPort = rawAnalysis.getObservablePorts().getElements()
        .filterIsInstance<ProductValue<BasicNumber>>()
        .firstOrNull { targetTransport.matches(it) }
        ?: throw IllegalStateException("no impacts found for '$targetTransport'")
    private val usePort = rawAnalysis.getObservablePorts().getElements()
        .filterIsInstance<ProductValue<BasicNumber>>()
        .firstOrNull { targetUse.matches(it) }
        ?: throw IllegalStateException("no impacts found for '$targetUse'")
    private val endOfLifePort = rawAnalysis.getObservablePorts().getElements()
        .filterIsInstance<ProductValue<BasicNumber>>()
        .firstOrNull { targetEndOfLife.matches(it) }
        ?: throw IllegalStateException("no impacts found for '$targetEndOfLife'")

    fun total(target: Indicator): QuantityValue<BasicNumber> {
        return rawAnalysis.getPortContribution(mainPort, indicator(target))
    }

    fun manufacturing(target: Indicator): QuantityValue<BasicNumber> {
        return rawAnalysis.getPortContribution(manufacturingPort, indicator(target))
    }

    fun transport(target: Indicator): QuantityValue<BasicNumber> {
        return rawAnalysis.getPortContribution(transportPort, indicator(target))
    }

    fun use(target: Indicator): QuantityValue<BasicNumber> {
        return rawAnalysis.getPortContribution(usePort, indicator(target))
    }

    fun endOfLife(target: Indicator): QuantityValue<BasicNumber> {
        return rawAnalysis.getPortContribution(endOfLifePort, indicator(target))
    }

    private fun indicator(indicator: Indicator): IndicatorValue<BasicNumber> {
        return rawAnalysis.getControllablePorts().getElements()
            .filterIsInstance<IndicatorValue<BasicNumber>>()
            .firstOrNull { it.name == indicator.name }
            ?: throw IllegalStateException("no impact found for indicator=${indicator.name}")
    }
}
