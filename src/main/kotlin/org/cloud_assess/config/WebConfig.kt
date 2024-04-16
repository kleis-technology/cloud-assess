package org.cloud_assess.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    @Value("\${CORS_ENABLED:false}")
    private var corsEnabled: Boolean = false

    @Value("\${CORS_ALLOWED_ORIGIN:n/a}")
    private lateinit var allowedOrigin: String

    companion object {
        private val log = LoggerFactory.getLogger({}.javaClass)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        if (corsEnabled) {
            log.info("CORS enabled - allowed origin: $allowedOrigin")
            registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
        } else {
            log.info("CORS disabled")
        }
    }
}
