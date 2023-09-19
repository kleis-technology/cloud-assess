package org.cloud_assess.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ImpactDto(
    @get:JsonProperty("total") val total: QuantityDto
)
