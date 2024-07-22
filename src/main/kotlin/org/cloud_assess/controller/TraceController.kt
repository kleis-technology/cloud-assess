package org.cloud_assess.controller

import org.cloud_assess.api.DebugApi
import org.cloud_assess.dto.TraceRequestDto
import org.cloud_assess.dto.TraceRequestListDto
import org.cloud_assess.dto.TraceResponseDto
import org.cloud_assess.dto.TraceResponseListDto
import org.cloud_assess.service.debug.TraceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TraceController(
    private val traceService: TraceService,
) : DebugApi {
    override fun trace(traceRequestListDto: TraceRequestListDto): ResponseEntity<TraceResponseListDto> {
        TODO()
    }
}
