package org.cloud_assess.controller

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.cloud_assess.dto.response.VirtualMachineListAssessmentDto
import org.cloud_assess.fixtures.DtoFixture.Companion.assessmentDto
import org.cloud_assess.fixtures.DtoFixture.Companion.serviceLayerDto
import org.cloud_assess.model.VirtualMachineAnalysis
import org.cloud_assess.service.AdapterService
import org.cloud_assess.service.ExecutionService
import org.cloud_assess.service.MapperService
import org.cloud_assess.service.PrepareService
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
        fun prepareService() = mockk<PrepareService>()

        @Bean
        fun adapterService() = mockk<AdapterService>()

        @Bean
        fun executionService() = mockk<ExecutionService>()

        @Bean
        fun mapperService() = mockk<MapperService>()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var prepareService: PrepareService

    @Autowired
    private lateinit var adapterService: AdapterService

    @Autowired
    private lateinit var executionService: ExecutionService

    @Autowired
    private lateinit var mapperService: MapperService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun virtualMachines_whenCorrectDto_then200() {
        // given
        val vm = mockk<EProcessTemplateApplication<BasicNumber>>()
        val rawAnalysis = mockk<ContributionAnalysis<BasicNumber, BasicMatrix>>()
        val analysis = mockk<VirtualMachineAnalysis>()
        val dto = serviceLayerDto()
        val outputDto = VirtualMachineListAssessmentDto(
            virtualMachines = listOf(assessmentDto("c1"))
        )

        every { prepareService.prepare(any()) } returns mapOf("c1" to vm)
        every { executionService.run(vm) } returns rawAnalysis
        every { adapterService.adapt("c1", rawAnalysis) } returns analysis
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
              "internal_workload": {
                "ram": {
                  "amount": 10.0,
                  "unit": "TB_hour"
                },
                "storage": {
                  "amount": 50.0,
                  "unit": "GB_hour"
                }
              },
              "virtual_machines": {}
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
