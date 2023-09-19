package org.cloud_assess.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class VirtualMachineDto(
    @JsonProperty("id") val id: String,
    @JsonProperty("ram") val ram: QuantityMemoryTimeDto,
    @JsonProperty("storage") val storage: QuantityMemoryTimeDto,
    @JsonProperty("meta") val meta: Map<String, String>,
)
