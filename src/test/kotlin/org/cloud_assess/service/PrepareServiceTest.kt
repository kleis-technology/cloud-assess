package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.fixtures.DtoFixture.Companion.serviceLayerDto
import org.cloud_assess.fixtures.ModelFixture.Companion.oneHour
import org.junit.jupiter.api.Test

class PrepareServiceTest {

    @Test
    fun prepare() {
        // given
        val dto = serviceLayerDto()
        val prepareService = PrepareService(ParsingService())

        // when
        val actual = prepareService.prepare(dto)

        // then
        val tenGBHour = EQuantityScale(
            BasicNumber(10.0),
            EQuantityMul(EDataRef("GB"), EDataRef("hour"))
        )
        assertThat(actual.keys).isEqualTo(setOf("c1"))
        assertThat(actual["c1"]?.template?.body?.products).hasSize(1)
        assertThat(actual["c1"]?.template?.body?.products?.first()?.quantity).isEqualTo(oneHour())
        assertThat(actual["c1"]?.template?.body?.products?.first()?.product?.name).isEqualTo("c1")
        assertThat(actual["c1"]?.template?.body?.inputs).isEqualTo(
            listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        oneHour(),
                        EProductSpec(
                            name = "vm",
                            fromProcess = FromProcess(
                                name = "virtual_machine",
                                matchLabels = MatchLabels(emptyMap()),
                                arguments = mapOf(
                                    "ram_size" to tenGBHour,
                                    "storage_size" to tenGBHour,
                                    "internal_ram" to tenGBHour,
                                    "internal_storage" to tenGBHour,
                                    "nb_clients" to EQuantityScale(
                                        BasicNumber(1.0),
                                        EDataRef("slot"),
                                    ),
                                )
                            )
                        )
                    )
                )
            ),
        )
    }
}
