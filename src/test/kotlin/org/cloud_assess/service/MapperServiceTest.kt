package org.cloud_assess.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.model.ResourceTrace
import org.cloud_assess.model.ResourceTraceAnalysis
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class MapperServiceTest {
    @Test
    fun resourceTrace_sameSize() {
        // given
        val elements = listOf("r1", "r2", "r3").associateWith {
            val mockk = mockk<ResourceTrace>()
            every { mockk.getMeta() } returns emptyMap()
            every { mockk.getElements() } returns emptyList()
            mockk
        }
        val analysis = ResourceTraceAnalysis(meta = emptyMap(), elements = elements)
        val mapper = MapperService()

        // when
        val actual = mapper.map(analysis).elements!!

        // then
        assertThat(actual).hasSize(elements.size)
    }

    @Test
    fun resourceTrace_mapSpecificMeta() {
        // given
        val elements = listOf("r1", "r2", "r3")
            .mapIndexed { idx, it ->
                val mockk = mockk<ResourceTrace>()
                every { mockk.getMeta() } returns mapOf(
                    "group" to "G$idx"
                )
                every { mockk.getElements() } returns emptyList()
                it to mockk
            }.toMap()
        val analysis = ResourceTraceAnalysis(meta = emptyMap(), elements)
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

    @Test
    fun resourceTrace_mapCommonMeta() {
        // given
        val analysis = ResourceTraceAnalysis(meta = mapOf("group" to "foo"), emptyMap())
        val mapper = MapperService()

        // when
        val actual = mapper.map(analysis).meta

        // then
        assertThat(actual).isEqualTo(mapOf("group" to "foo"))
    }
}
