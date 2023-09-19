package org.cloud_assess.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class ServiceLayerDto(
    @JsonProperty("internal_workload") val internalWorkload: InternalWorkloadDto,
    @JsonProperty("virtual_machines") val virtualMachines: List<VirtualMachineDto>
)
