package org.cloud_assess.dto

data class HealthStatusDto(
    val status: Status
)

enum class Status {
    UP
}
