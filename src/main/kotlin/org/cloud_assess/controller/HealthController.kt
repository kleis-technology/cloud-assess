package org.cloud_assess.controller

import org.cloud_assess.dto.HealthStatusDto
import org.cloud_assess.dto.Status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): HealthStatusDto {
        return HealthStatusDto(
            Status.UP
        )
    }
}
