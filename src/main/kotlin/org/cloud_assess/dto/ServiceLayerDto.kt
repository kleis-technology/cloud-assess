package org.cloud_assess.dto

data class ServiceLayerDto(
    val internalWorkload: InternalWorkloadDto,
    val virtualMachines: List<VirtualMachineDto>
)
