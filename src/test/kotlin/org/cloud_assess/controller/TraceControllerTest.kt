package org.cloud_assess.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.cloud_assess.config.MapperConfig
import org.cloud_assess.dto.EntryValueDtoDeserializer
import org.cloud_assess.dto.EntryValueDtoSerializer
import org.cloud_assess.dto.TraceRequestListDto
import org.cloud_assess.dto.TraceResponseListDto
import org.cloud_assess.fixtures.DtoFixture
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.debug.TraceService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(SpringExtension::class)
class TraceControllerTest {
    @TestConfiguration
    @Import(MapperConfig::class)
    class ControllerTestConfig {
        @Bean
        fun traceService() = mockk<TraceService>()

        @Bean
        fun mapperService() = mockk<MapperService>()

        @Bean
        fun mockMvcBuilder(
            traceService: TraceService,
            mapperService: MapperService,
        ): MockMvcBuilder {
            val builder = MockMvcBuilders.standaloneSetup(
                TraceController(
                    mapperService,
                    traceService,
                )
            )
            val jackson2ObjectMapperBuilder = Jackson2ObjectMapperBuilder()
                .deserializers(EntryValueDtoDeserializer())
                .serializers(EntryValueDtoSerializer())
            builder.setMessageConverters(
                MappingJackson2HttpMessageConverter(jackson2ObjectMapperBuilder.build()),
            )
            return builder
        }

        @Bean
        fun mockMvc(
            mockMvcBuilder: MockMvcBuilder,
        ): MockMvc = mockMvcBuilder
            .build()
    }

    @Autowired
    private lateinit var mapperService: MapperService

    @Autowired
    private lateinit var traceService: TraceService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun trace_whenCorrectDto_then200() {
        // given
        val dto = DtoFixture.traceRequestList(3)
        val outputDto = TraceResponseListDto(
            elements = emptyList(),
        )
        every { traceService.analyze(any()) } returns mockk()
        every { mapperService.map(any(), any<TraceRequestListDto>()) } returns outputDto


        // when
        mockMvc.perform(
            post("/debug/trace")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk)
    }
}
