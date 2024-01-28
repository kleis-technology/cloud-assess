package org.cloud_assess.controller

import org.cloud_assess.api.HealthApi
import org.cloud_assess.dto.HealthStatusDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController : HealthApi {
    override fun health(): ResponseEntity<HealthStatusDto> {
        return ResponseEntity.ok(
            HealthStatusDto(
                HealthStatusDto.Status.uP
            )
        )
    }
}
