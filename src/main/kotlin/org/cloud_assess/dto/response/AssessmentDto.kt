package org.cloud_assess.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AssessmentDto(
    @get:JsonProperty("request") val request: RequestDto,
    @get:JsonProperty("impacts") val impacts: ImpactsDto,
)
