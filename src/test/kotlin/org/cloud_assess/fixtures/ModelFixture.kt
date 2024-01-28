package org.cloud_assess.fixtures

import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.math.basic.BasicNumber

class ModelFixture {
    companion object {
        fun oneHour(): EQuantityScale<BasicNumber> = EQuantityScale(BasicNumber(1.0), EDataRef("hour"))
        fun oneUnit(): EQuantityScale<BasicNumber> = EQuantityScale(BasicNumber(1.0), EDataRef("u"))
    }
}
