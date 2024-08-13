package org.cloud_assess.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.model.ResourceTrace
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class MapperServiceTest {
    @Test
    fun resourceTrace_sameSize() {
        // given
        val analysis = listOf("r1", "r2", "r3").associateWith {
            val mockk = mockk<ResourceTrace>()
            every { mockk.getMeta() } returns emptyMap()
            every { mockk.getElements() } returns emptyList()
            mockk
        }
        val mapper = MapperService()

        // when
        val actual = mapper.map(analysis).elements!!

        // then
        assertThat(actual).hasSize(analysis.size)
    }

    @Test
    fun resourceTrace_mapMeta() {
        // given
        val analysis = listOf("r1", "r2", "r3")
            .mapIndexed { idx, it ->
                val mockk = mockk<ResourceTrace>()
                every { mockk.getMeta() } returns mapOf(
                    "group" to "G$idx"
                )
                every { mockk.getElements() } returns emptyList()
                it to mockk
            }.toMap()
        val mapper = MapperService()

        // when
        val actual = mapper.map(analysis).elements
            ?.mapNotNull { it.meta }
            ?: fail("failed")

        // then
        assertThat(actual).isEqualTo(listOf(
            mapOf("group" to "G0"),
            mapOf("group" to "G1"),
            mapOf("group" to "G2"),
        ))
    }
}
