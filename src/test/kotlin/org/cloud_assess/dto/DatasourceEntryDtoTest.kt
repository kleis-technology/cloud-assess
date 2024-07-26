package org.cloud_assess.dto

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.config.MapperConfig
import org.cloud_assess.config.WebConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [MapperConfig::class])
class DatasourceEntryDtoTest(
    @Autowired
    private val objectMapper: ObjectMapper,
) {

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
        val actual = objectMapper.readValue(json, DatasourceEntryDto::class.java)

        // then
        val expected = DatasourceEntryDto(
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
        val actual = objectMapper.readValue(json, DatasourceEntryDto::class.java)

        // then
        val expected = DatasourceEntryDto(
            name = "x",
            value = VNum(1.0),
        )
        assertThat(actual).isEqualTo(expected)
    }

}
