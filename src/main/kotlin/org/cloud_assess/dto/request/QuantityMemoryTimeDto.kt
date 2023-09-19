package org.cloud_assess.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class QuantityMemoryTimeDto(
    @JsonProperty("amount") val amount: Double,
    @JsonProperty("unit") val unit: MemoryTimeUnitsDto,
)
