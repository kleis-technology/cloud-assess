package org.cloud_assess.controller

import org.cloud_assess.api.ComputeResourcesApi
import org.cloud_assess.dto.ComputeResourceListAssessmentDto
import org.cloud_assess.dto.ComputeResourceListDto
import org.cloud_assess.service.ComputeResourceService
import org.cloud_assess.service.MapperService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ComputeResourcesController(
    private val computeResourceService: ComputeResourceService,
    private val mapperService: MapperService,
) : ComputeResourcesApi {
    override fun assessComputeResources(computeResourceListDto: ComputeResourceListDto): ResponseEntity<ComputeResourceListAssessmentDto> {
        val analysis = computeResourceService.analyze(computeResourceListDto)
        val dto = mapperService.map(analysis, computeResourceListDto)
        return ResponseEntity.ok(dto)
    }
}
