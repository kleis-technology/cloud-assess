package org.cloud_assess.controller

import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicNumber
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.cloud_assess.dto.VirtualMachineListAssessmentDto
import org.cloud_assess.fixtures.DtoFixture.Companion.assessmentDto
import org.cloud_assess.fixtures.DtoFixture.Companion.virtualMachineListDto
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.VirtualMachineService
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
@WebMvcTest(controllers = [VirtualMachinesController::class])
class VirtualMachinesControllerTest {
    @TestConfiguration
    class ControllerTestConfig {
        @Bean
        fun virtualMachineService() = mockk<VirtualMachineService>()

        @Bean
        fun mapperService() = mockk<MapperService>()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var virtualMachineService: VirtualMachineService

    @Autowired
    private lateinit var mapperService: MapperService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun virtualMachines_whenCorrectDto_then200() {
        // given
        val vm = mockk<EProcessTemplateApplication<BasicNumber>>()
        val dto = virtualMachineListDto()
        val outputDto = VirtualMachineListAssessmentDto(
            virtualMachines = listOf(assessmentDto("c1"))
        )

        every { virtualMachineService.analyze(any()) } returns mockk()
        every { mapperService.map(any(), any()) } returns outputDto

        // when/then
        mockMvc.perform(
            post("/virtual_machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk)
    }

    @Test
    fun virtualMachines_whenInvalidDto_then400() {
        // given
        val dto = """
            {
              "virtual_machines": [
                {
                    "id": 3.0
                }
              ]
            }
        """.trimIndent()

        // when/then
        mockMvc.perform(
            post("/virtual_machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dto)
        ).andExpect(status().isBadRequest)
    }
}
