package org.cloud_assess.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    @Value("\${cors.enabled}")
    private var corsEnabled: Boolean = false

    @Value("\${cors.allowed-origin}")
    private lateinit var allowedOrigin: String

    override fun addCorsMappings(registry: CorsRegistry) {
        if (corsEnabled) {
            registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
        }
    }
}
