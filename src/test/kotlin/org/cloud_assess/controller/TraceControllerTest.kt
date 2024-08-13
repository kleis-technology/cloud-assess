package org.cloud_assess.controller

import io.mockk.every
import io.mockk.mockk
import org.cloud_assess.config.WebConfig
import org.cloud_assess.dto.TraceRequestListDto
import org.cloud_assess.dto.TraceResponseListDto
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.debug.TraceService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(SpringExtension::class)
@WebAppConfiguration
@ContextConfiguration(classes = [WebConfig::class])
class TraceControllerTest {
    private val traceService: TraceService = mockk()
    private val mapperService: MapperService = mockk()
    private val traceController = TraceController(mapperService, traceService)

    @Autowired
    private lateinit var jackson2HttpMessageConverter: MappingJackson2HttpMessageConverter

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(traceController)
            .setMessageConverters(this.jackson2HttpMessageConverter)
            .build()
    }


    @Test
    fun trace_whenCorrectDto_then200() {
        // given
        val dto = """
            {
              "elements": [
                {
                  "requestId": "r01",
                  "demand": {
                    "productName": "vm",
                    "processName": "vm",
                    "quantity": {
                      "amount": 1.0,
                      "unit": "hour"
                    }
                  }
                }
              ]
            }
        """.trimIndent()
        val outputDto = TraceResponseListDto(
            elements = emptyList(),
        )
        every { traceService.analyze(any<TraceRequestListDto>()) } returns mockk()
        every { mapperService.map(any()) } returns outputDto

        // when
        mockMvc.perform(
            post("/debug/trace")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dto)
        ).andExpect(status().isOk)
    }
}
