package org.cloud_assess.controller

import org.cloud_assess.api.StorageResourcesApi
import org.cloud_assess.dto.StorageResourceListAssessmentDto
import org.cloud_assess.dto.StorageResourceListDto
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.StorageResourceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class StorageResourcesController(
    private val storageResourceService: StorageResourceService,
    private val mapperService: MapperService,
) : StorageResourcesApi {
    override fun assessStorageResources(storageResourceListDto: StorageResourceListDto): ResponseEntity<StorageResourceListAssessmentDto> {
        val analysis = storageResourceService.analyze(storageResourceListDto)
        val dto = mapperService.map(analysis, storageResourceListDto)
        return ResponseEntity.ok(dto)
    }
}
