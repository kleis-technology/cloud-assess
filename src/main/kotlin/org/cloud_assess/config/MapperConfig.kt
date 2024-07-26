package org.cloud_assess.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.cloud_assess.dto.EntryValueDto
import org.cloud_assess.dto.EntryValueDtoDeserializer
import org.cloud_assess.dto.EntryValueDtoSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MapperConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper()
        val sm = SimpleModule()
        sm.addDeserializer(EntryValueDto::class.java, EntryValueDtoDeserializer())
        sm.addSerializer(EntryValueDto::class.java, EntryValueDtoSerializer())
        objectMapper.registerModule(sm)
        return objectMapper
    }
}
