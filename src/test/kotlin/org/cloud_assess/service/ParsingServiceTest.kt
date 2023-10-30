package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.expression.EQuantityClosure
import ch.kleis.lcaac.core.lang.expression.EUnitOf
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ParsingServiceTest {
    @Test
    fun processTemplateApplication_shouldIncludePreludeUnits() {
        // given
        val content = """
            process virtual_machine {
                products {
                    1 hour vm
                }
            }
        """.trimIndent()
        val service = ParsingService()

        // when
        val referenceUnit = service.processTemplateApplication(content)
            .template.body
            .products.first()
            .product
            .referenceUnit as EUnitOf<BasicNumber>
        val quantity = referenceUnit.expression as EQuantityClosure<BasicNumber>
        val actual = quantity.symbolTable.data.getValues().toList()

        // then
        val expected = Prelude.units<BasicNumber>().getValues().toList()
        assertThat(actual).isEqualTo(expected)
    }
}
