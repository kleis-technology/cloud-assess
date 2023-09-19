package org.cloud_assess.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class RequestDto(
    @get:JsonProperty("id") val id: String,
    @get:JsonProperty("quantity") val quantity: QuantityDto,
    @get:JsonProperty("meta") val meta: Map<String, String>
)
