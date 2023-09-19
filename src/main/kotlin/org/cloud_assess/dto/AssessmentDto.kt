package org.cloud_assess.dto

data class AssessmentDto(
    val request: RequestDto,
    val impacts: ImpactsDto,
)
