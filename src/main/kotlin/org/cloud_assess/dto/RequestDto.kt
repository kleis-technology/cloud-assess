package org.cloud_assess.dto

data class RequestDto(
    val id: String,
    val quantity: QuantityDto,
    val meta: Map<String, String>
)
