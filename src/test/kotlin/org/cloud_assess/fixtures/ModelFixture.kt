package org.cloud_assess.fixtures

import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.math.basic.BasicNumber

class ModelFixture {
    companion object {
        fun oneOf(ref: String): EQuantityScale<BasicNumber> {
            return EQuantityScale(BasicNumber(1.0), EDataRef(ref))
        }

        fun onePiece(): EQuantityScale<BasicNumber> = oneOf("piece")
        fun oneHour(): EQuantityScale<BasicNumber> = oneOf("hour")
    }
}
