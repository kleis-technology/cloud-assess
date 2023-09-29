package org.cloud_assess.fixtures

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class QuantityFixture {
    companion object {
        val oneKg = QuantityValue(
            BasicNumber(1.0),
            UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        )
    }
}
