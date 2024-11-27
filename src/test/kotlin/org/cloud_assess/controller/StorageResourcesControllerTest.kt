package org.cloud_assess.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.cloud_assess.dto.StorageResourceListAssessmentDto
import org.cloud_assess.dto.StorageResourceListDto
import org.cloud_assess.fixtures.DtoFixture.Companion.storageResourceAssessmentDto
import org.cloud_assess.fixtures.DtoFixture.Companion.storageResourceListDto
import org.cloud_assess.service.StorageResourceService
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
@WebMvcTest(controllers = [StorageResourcesController::class])
class StorageResourcesControllerTest {
    @TestConfiguration
    class ControllerTestConfig {
        @Bean
        fun mapperService(): MapperService = mockk()

        @Bean
        fun storageResourceService(): StorageResourceService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var storageResourceService: StorageResourceService

    @Autowired
    private lateinit var mapperService: MapperService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun storageResources_whenCorrectDto_then200() {
        // given
        val dto = storageResourceListDto()
        val outputDto = StorageResourceListAssessmentDto(
            storageResources = listOf(storageResourceAssessmentDto())
        )

        every { storageResourceService.analyze(any()) } returns mockk()
        every { mapperService.map(any(), any<StorageResourceListDto>()) } returns outputDto

        // when/then
        mockMvc.perform(
            post("/storage_resources/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk)
    }

    @Test
    fun storageResources_whenInvalidDto_then400() {
        // given
        val dto = """
            {
                "storage_resources": [
                    {
                        "id": 3.0
                    }
                ]
            }
        """.trimIndent()

        // when/then
        mockMvc.perform(
            post("/storage_resources/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isBadRequest)
    }
}
