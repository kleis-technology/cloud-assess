package org.cloud_assess.controller

import org.cloud_assess.dto.request.ServiceLayerDto
import org.cloud_assess.dto.response.VirtualMachineListAssessmentDto
import org.cloud_assess.service.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    private val service: Service
) {
    @PostMapping("/virtual_machines")
    fun virtualMachines(
        @RequestBody dto: ServiceLayerDto
    ): VirtualMachineListAssessmentDto {
        return service.virtualMachines(
            dto.virtualMachines,
            dto.internalWorkload,
        )
    }
}
