package org.cloud_assess

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CloudAssessApplication

fun main(args: Array<String>) {
    runApplication<CloudAssessApplication>(*args)
}
