package org.cloud_assess.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class InternalWorkloadDto(
    @JsonProperty("ram") val ram: QuantityMemoryTimeDto,
    @JsonProperty("storage") val storage: QuantityMemoryTimeDto,
)
