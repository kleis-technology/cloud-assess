package org.cloud_assess.config

import org.cloud_assess.dto.EntryValueDtoDeserializer
import org.cloud_assess.dto.EntryValueDtoSerializer
import org.cloud_assess.dto.ParameterValueDtoDeserializer
import org.cloud_assess.dto.ParameterValueDtoSerializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    @Value("\${cors.enabled:false}")
    private var corsEnabled: Boolean = false

    @Value("\${cors.allowed-origin:na}")
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

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(jackson2HttpMessageConverter())
    }

    @Bean
    fun jackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        val converter = MappingJackson2HttpMessageConverter()
        val builder: Jackson2ObjectMapperBuilder = this.jacksonBuilder()
        converter.objectMapper = builder.build()

        return converter
    }

    fun jacksonBuilder(): Jackson2ObjectMapperBuilder {
        val builder = Jackson2ObjectMapperBuilder()
        // stuff here
        builder.serializers(
            EntryValueDtoSerializer(),
            ParameterValueDtoSerializer(),
        )
        builder.deserializers(
            EntryValueDtoDeserializer(),
            ParameterValueDtoDeserializer(),
        )
        return builder
    }
}
