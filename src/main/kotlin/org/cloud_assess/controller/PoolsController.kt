package org.cloud_assess.controller

import org.cloud_assess.api.PoolsApi
import org.cloud_assess.dto.PoolListAssessmentDto
import org.cloud_assess.dto.PoolListDto
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.PoolService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class PoolsController(
    private val poolService: PoolService,
    private val mapperService: MapperService,
) : PoolsApi {
    override fun assessPools(poolListDto: PoolListDto): ResponseEntity<PoolListAssessmentDto> {
        val analysis = poolService.analyze(poolListDto)
        return ResponseEntity.ok(mapperService.map(analysis, poolListDto))
    }
}
