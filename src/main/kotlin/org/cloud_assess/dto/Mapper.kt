package org.cloud_assess.dto

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.response.QuantityDto

fun QuantityValue<BasicNumber>.toQuantityDto(): QuantityDto {
    return QuantityDto(
        this.amount.value,
        this.unit.toString(),
    )
}
