package org.cloud_assess.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.cloud_assess.dto.PoolListAssessmentDto
import org.cloud_assess.dto.PoolListDto
import org.cloud_assess.fixtures.DtoFixture.Companion.assessmentDto
import org.cloud_assess.fixtures.DtoFixture.Companion.poolListDto
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.PoolService
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
@WebMvcTest(controllers = [PoolsController::class])
class PoolsControllerTest {
    @TestConfiguration
    class ControllerTestConfig {
        @Bean
        fun poolService() = mockk<PoolService>()

        @Bean
        fun mapperService() = mockk<MapperService>()
    }

    @Autowired
    private lateinit var mapperService: MapperService

    @Autowired lateinit var poolService: PoolService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun pools_whenCorrectDto_then200() {
        // given
        val dto = poolListDto()
        val outputDto = PoolListAssessmentDto(
            pools = listOf(assessmentDto("client_vm"))
        )

        every { poolService.analyze(any()) } returns mockk()
        every { mapperService.map(any(), any<PoolListDto>()) } returns outputDto

        // when/then
        mockMvc.perform(
            post("/pools/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk)
    }

    @Test
    fun pools_whenInvalidDto_then400() {
        // given
        val dto = """
            {
                "pools": [
                    {
                        "foo": 1.0
                    }
                ]
            }
        """.trimIndent()

        // when/then
        mockMvc.perform(
            post("/pools/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dto)
        ).andExpect(status().isBadRequest)
    }
}
