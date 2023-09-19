package org.cloud_assess.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class VirtualMachineListAssessmentDto(
    @get:JsonProperty("virtual_machines") val virtualMachines: List<AssessmentDto>
)
