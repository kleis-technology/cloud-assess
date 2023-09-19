package org.cloud_assess.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class QuantityDto(
    @get:JsonProperty("amount") val amount: Double,
    @get:JsonProperty("unit") val unit: String,
)
