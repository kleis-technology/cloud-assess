package org.cloud_assess.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.config.MapperConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class EntryDtoTest(
    @Autowired
    private val objectMapper: ObjectMapper,
) {
    @TestConfiguration
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


    @Test
    fun string() {
        // given
        val json = """
            {
                "name": "x",
                "value": "abc"
            }
        """.trimIndent()

        // when
        val actual = objectMapper.readValue(json, EntryDto::class.java)

        // then
        val expected = EntryDto(
            name = "x",
            value = VStr("abc"),
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun number() {
        // given
        val json = """
            {
                "name": "x",
                "value": 1.0
            }
        """.trimIndent()

        // when
        val actual = objectMapper.readValue(json, EntryDto::class.java)

        // then
        val expected = EntryDto(
            name = "x",
            value = VNum(1.0),
        )
        assertThat(actual).isEqualTo(expected)
    }

}
