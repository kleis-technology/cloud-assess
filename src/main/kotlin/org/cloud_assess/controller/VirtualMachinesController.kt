package org.cloud_assess.controller

import org.cloud_assess.api.VirtualMachinesApi
import org.cloud_assess.dto.VirtualMachineListAssessmentDto
import org.cloud_assess.dto.VirtualMachineListDto
import org.cloud_assess.service.VirtualMachineService
import org.cloud_assess.service.MapperService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class VirtualMachinesController(
    private val virtualMachineService: VirtualMachineService,
    private val mapperService: MapperService,
) : VirtualMachinesApi {
    override fun assessVirtualMachines(virtualMachineListDto: VirtualMachineListDto): ResponseEntity<VirtualMachineListAssessmentDto> {
        val analysis = virtualMachineService.analyze(virtualMachineListDto)
        return ResponseEntity.ok(mapperService.map(analysis, virtualMachineListDto))
    }
}
