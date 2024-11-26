package org.cloud_assess.model

import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductMatcherTest {
    @Test
    fun productMatcher_whenMatch() {
        // given
        val product = ProductValue(
            name = "vm",
            fromProcessRef = FromProcessRefValue(
                name = "vm_fn",
                matchLabels = mapOf(
                    "phase" to StringValue("embodied"),
                    "geo" to StringValue("GLO"),
                ),
                arguments = mapOf(
                    "id" to StringValue("vm-01"),
                    "q" to QuantityValue(BasicNumber(1.0), UnitValue.none()),
                    "lc_step" to StringValue("manufacturing"),
                )
            ),
            referenceUnit = mockk()
        )
        val matcher = ProductMatcher(
            name = "vm",
            process = "vm_fn",
            labels = mapOf(
                "phase" to "embodied",
                "geo" to "GLO",
            ),
            arguments = mapOf(
                "id" to "vm-01",
            )
        )

        // when
        val actual = matcher.matches(product)

        // then
        assert(actual)
    }

    @Test
    fun productMatcher_whenNameDiffer() {
        // given
        val product = ProductValue(
            name = "vm",
            fromProcessRef = FromProcessRefValue(
                name = "vm_fn",
                matchLabels = mapOf(
                    "phase" to StringValue("embodied"),
                    "geo" to StringValue("GLO"),
                ),
                arguments = mapOf(
                    "id" to StringValue("vm-01"),
                    "q" to QuantityValue(BasicNumber(1.0), UnitValue.none()),
                    "lc_step" to StringValue("manufacturing"),
                )
            ),
            referenceUnit = mockk()
        )
        val matcher = ProductMatcher(
            name = "foo",
            process = "vm_fn",
            labels = mapOf(
                "phase" to "embodied",
                "geo" to "GLO",
            ),
            arguments = mapOf(
                "id" to "vm-01",
            )
        )

        // when
        val actual = matcher.matches(product)

        // then
        assert(!actual)
    }

    @Test
    fun productMatcher_whenProcessNameDiffer() {
        // given
        val product = ProductValue(
            name = "vm",
            fromProcessRef = FromProcessRefValue(
                name = "vm_fn",
                matchLabels = mapOf(
                    "phase" to StringValue("embodied"),
                    "geo" to StringValue("GLO"),
                ),
                arguments = mapOf(
                    "id" to StringValue("vm-01"),
                    "q" to QuantityValue(BasicNumber(1.0), UnitValue.none()),
                    "lc_step" to StringValue("manufacturing"),
                )
            ),
            referenceUnit = mockk()
        )
        val matcher = ProductMatcher(
            name = "vm",
            process = "bar_fn",
            labels = mapOf(
                "phase" to "embodied",
                "geo" to "GLO",
            ),
            arguments = mapOf(
                "id" to "vm-01",
            )
        )

        // when
        val actual = matcher.matches(product)

        // then
        assert(!actual)
    }

    @Test
    fun productMatcher_whenLabelsNotEqual() {
        // given
        val product = ProductValue(
            name = "vm",
            fromProcessRef = FromProcessRefValue(
                name = "vm_fn",
                matchLabels = mapOf(
                    "phase" to StringValue("embodied"),
                    "geo" to StringValue("GLO"),
                ),
                arguments = mapOf(
                    "id" to StringValue("vm-01"),
                    "q" to QuantityValue(BasicNumber(1.0), UnitValue.none()),
                    "lc_step" to StringValue("manufacturing"),
                )
            ),
            referenceUnit = mockk()
        )
        val matcher = ProductMatcher(
            name = "vm",
            process = "vm_fn",
            labels = mapOf(
                "phase" to "embodied",
                "foo" to "bar"
            ),
            arguments = mapOf(
                "id" to "vm-01",
            )
        )

        // when
        val actual = matcher.matches(product)

        // then
        assert(!actual)
    }

    @Test
    fun productMatcher_whenLabelsNotEqual_strictSubset() {
        // given
        val product = ProductValue(
            name = "vm",
            fromProcessRef = FromProcessRefValue(
                name = "vm_fn",
                matchLabels = mapOf(
                    "phase" to StringValue("embodied"),
                    "geo" to StringValue("GLO"),
                ),
                arguments = mapOf(
                    "id" to StringValue("vm-01"),
                    "q" to QuantityValue(BasicNumber(1.0), UnitValue.none()),
                    "lc_step" to StringValue("manufacturing"),
                )
            ),
            referenceUnit = mockk()
        )
        val matcher = ProductMatcher(
            name = "vm",
            process = "vm_fn",
            labels = mapOf(
                "phase" to "embodied",
            ),
            arguments = mapOf(
                "id" to "vm-01",
            )
        )

        // when
        val actual = matcher.matches(product)

        // then
        assert(!actual)
    }

    @Test
    fun productMatcher_whenArgumentsNotASubset() {
        // given
        val product = ProductValue(
            name = "vm",
            fromProcessRef = FromProcessRefValue(
                name = "vm_fn",
                matchLabels = mapOf(
                    "phase" to StringValue("embodied"),
                    "geo" to StringValue("GLO"),
                ),
                arguments = mapOf(
                    "id" to StringValue("vm-01"),
                    "q" to QuantityValue(BasicNumber(1.0), UnitValue.none()),
                    "lc_step" to StringValue("manufacturing"),
                )
            ),
            referenceUnit = mockk()
        )
        val matcher = ProductMatcher(
            name = "vm",
            process = "vm_fn",
            labels = mapOf(
                "phase" to "embodied",
            ),
            arguments = mapOf(
                "id" to "vm-01",
                "foo" to "bar"
            )
        )

        // when
        val actual = matcher.matches(product)

        // then
        assert(!actual)
    }

    @Test
    fun productMatcher_toString() {
        // given
        val matcher = ProductMatcher(
            name = "vm",
            process = "vm_fn",
            labels = mapOf(
                "phase" to "embodied",
                "geo" to "GLO",
            ),
            arguments = mapOf(
                "id" to "vm-01",
            )
        )

        // when
        val actual = matcher.toString()

        // then
        assertEquals(actual, "vm from vm_fn{id=vm-01}{phase=embodied, geo=GLO}")
    }
}
