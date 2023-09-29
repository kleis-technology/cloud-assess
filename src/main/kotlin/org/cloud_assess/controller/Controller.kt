package org.cloud_assess.controller

import org.cloud_assess.dto.request.ServiceLayerDto
import org.cloud_assess.dto.response.VirtualMachineListAssessmentDto
import org.cloud_assess.service.AdapterService
import org.cloud_assess.service.ExecutionService
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.PrepareService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    private val prepareService: PrepareService,
    private val adapterService: AdapterService,
    private val executionService: ExecutionService,
    private val mapperService: MapperService,
) {
    @PostMapping("/virtual_machines")
    fun virtualMachines(
        @RequestBody dto: ServiceLayerDto
    ): VirtualMachineListAssessmentDto {
        val cases = prepareService.prepare(dto)
        val analysis = cases.mapValues {
            val rawAnalysis = executionService.run(it.value)
            adapterService.adapt(it.key, rawAnalysis)
        }
        return mapperService.map(analysis, dto)
    }
}
