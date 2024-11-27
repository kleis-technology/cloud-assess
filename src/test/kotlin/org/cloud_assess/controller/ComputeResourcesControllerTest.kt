package org.cloud_assess.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.cloud_assess.dto.ComputeResourceListAssessmentDto
import org.cloud_assess.dto.ComputeResourceListDto
import org.cloud_assess.fixtures.DtoFixture.Companion.computeResourceAssessmentDto
import org.cloud_assess.fixtures.DtoFixture.Companion.computeResourceListDto
import org.cloud_assess.service.ComputeResourceService
import org.cloud_assess.service.MapperService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [ComputeResourcesController::class])
class ComputeResourcesControllerTest {
    @TestConfiguration
    class ControllerTestConfig {
        @Bean
        fun mapperService(): MapperService = mockk()

        @Bean
        fun computeResourceService(): ComputeResourceService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var computeResourceService: ComputeResourceService

    @Autowired
    private lateinit var mapperService: MapperService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun computeResources_whenCorrectDto_then200() {
        // given
        val dto = computeResourceListDto()
        val outputDto = ComputeResourceListAssessmentDto(
            computeResources = listOf(computeResourceAssessmentDto())
        )

        every { computeResourceService.analyze(any()) } returns mockk()
        every { mapperService.map(any(), any<ComputeResourceListDto>()) } returns outputDto

        // when/then
        mockMvc.perform(
            post("/compute_resources/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk)
    }

    @Test
    fun computeResources_whenInvalidDto_then400() {
        // given
        val dto = """
            {
                "compute_resources": [
                    {
                        "id": 3.0
                    }
                ]
            }
        """.trimIndent()

        // when/then
        mockMvc.perform(
            post("/compute_resources/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isBadRequest)
    }
}
