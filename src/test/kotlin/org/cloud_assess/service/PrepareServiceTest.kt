package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.fixtures.DtoFixture.Companion.serviceLayerDto
import org.cloud_assess.fixtures.ModelFixture.Companion.oneHour
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrepareServiceTest {

    @Test
    fun prepare() {
        // given
        val dto = serviceLayerDto()
        val prepareService = PrepareService()

        // when
        val actual = prepareService.prepare(dto)

        // then
        val tenGBHour = EQuantityScale(
            BasicNumber(10.0),
            EQuantityMul(EDataRef("GB"), EDataRef("hour"))
        )
        val expected = mapOf(
            "c1" to EProcessTemplateApplication<BasicNumber>(
                EProcessTemplate(
                    params = emptyMap(),
                    locals = emptyMap(),
                    body = EProcess(
                        "c1",
                        products = listOf(
                            ETechnoExchange(oneHour(), EProductSpec("c1", oneHour()))
                        ),
                        inputs = listOf(
                            ETechnoExchange(
                                oneHour(),
                                EProductSpec(
                                    name = "vm",
                                    referenceUnit = oneHour(),
                                    fromProcess = FromProcess(
                                        name = "virtual_machine",
                                        matchLabels = MatchLabels(emptyMap()),
                                        arguments = mapOf(
                                            "ram_size" to tenGBHour,
                                            "storage_size" to tenGBHour,
                                            "internal_ram" to tenGBHour,
                                            "internal_storage" to tenGBHour,
                                            "total_nb_clients" to EQuantityScale(
                                                BasicNumber(1.0),
                                                EDataRef("workload_slot"),
                                            ),
                                        )
                                    )
                                )
                            )
                        ),
                    )
                ),
                emptyMap()
            )
        )
        assertEquals(expected, actual)
    }
}
