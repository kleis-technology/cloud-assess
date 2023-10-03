package org.cloud_assess.controller

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [HealthController::class])
class HealthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun health() {
        // when/then
        val actual = mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val expected = """{"status":"UP"}"""
        assertEquals(expected, actual)
    }
}
