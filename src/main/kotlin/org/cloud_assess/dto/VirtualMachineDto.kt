package org.cloud_assess.dto

data class VirtualMachineDto(
    val id: String,
    val ram: QuantityMemoryTimeDto,
    val storage: QuantityMemoryTimeDto,
    val meta: Map<String, String>,
)
