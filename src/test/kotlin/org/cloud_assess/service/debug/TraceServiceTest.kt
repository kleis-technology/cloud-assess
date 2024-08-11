package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import io.mockk.every
import io.mockk.mockk
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.dto.*
import org.cloud_assess.fixtures.DtoFixture
import org.cloud_assess.fixtures.QuantityFixture
import org.cloud_assess.model.Indicator
import org.cloud_assess.service.ParsingService
import org.junit.jupiter.api.Test
import java.lang.Double.parseDouble

class TraceServiceTest {

    private val parsingService = ParsingService()

    private fun prepare(content: String): SymbolTable<BasicNumber> {
        val ops = BasicOperations
        val lexer = LcaLangLexer(CharStreams.fromString(content))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val files = sequenceOf(parser.lcaFile())
        return Loader(ops).load(files, listOf(LoaderOption.WITH_PRELUDE))
    }

    @Test
    fun analyze_requestList_mapShouldHaveSameKeys() {
        // given
        val symbolTable = prepare("""
            // nothing
            datasource inventory {
                schema {
                }
            }
            process vm {
                products {
                    1 hour vm
                }
                impacts {
                    1 kg GWP
                }
            }
        """.trimIndent())
        val requestList = DtoFixture.traceRequestList(3)
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(parsingService, sourceOps, symbolTable)

        // when
        val actual = service.analyze(requestList).keys

        // then
        val expected = requestList
            .elements.map { it.requestId }
            .toSet()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun analyze_singleRequest_simpleCase_isNotEmpty() {
        // given
        val symbolTable = prepare("""
            process p {
                products {
                    1 kg p
                }
                impacts {
                    1 kg GWP
                }
            }
        """.trimIndent())
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
            ),
        )

        // when
        val actual = service.analyze(request)

        // then
        assertThat(actual.isEmpty()).isFalse()
        assertThat(actual.isNotEmpty()).isTrue()
    }

    @Test
    fun analyze_singleRequest_simpleCase_hasCorrectRequestId() {
        // given
        val symbolTable = prepare("""
            process p {
                products {
                    1 kg p
                }
                impacts {
                    1 kg GWP
                }
            }
        """.trimIndent())
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
            ),
        )

        // when
        val actual = service.analyze(request)

        // then
        assertThat(actual.getRequestId()).isEqualTo("r01")
    }

    @Test
    fun analyze_singleRequest_simpleCase_hasCorrectImpact() {
        // given
        val symbolTable = prepare("""
            process p {
                products {
                    1 kg p
                }
                impacts {
                    1 kg GWP
                }
            }
        """.trimIndent())
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
            ),
        )

        // when
        val actual = service.analyze(request).getElements()[1]

        // then
        assertThat(actual.impacts[Indicator.GWP]).isEqualTo(
            QuantityFixture.oneKg,
        )
    }

    @Test
    fun analyze_singleRequest_withParams_Num_isNotEmpty() {
        // given
        val symbolTable = prepare("""
            process p {
                params {
                    x = 0 kg
                }
                products {
                    1 kg p
                }
                impacts {
                    x GWP
                }
            }
        """.trimIndent())
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
                params = listOf(
                    ParameterDto("x", PVNum(1.0, "kg")),
                )
            ),
        )

        // when
        val actual = service.analyze(request)
            .getElements()[1]

        // then success
        assertThat(actual.impacts[Indicator.GWP]).isEqualTo(
            QuantityFixture.oneKg,
        )
    }

    @Test
    fun analyze_singleRequest_withParams_Str_isNotEmpty() {
        // given
        val symbolTable = prepare("""
            process p {
                params {
                    my_name = "bar"
                }
                products {
                    1 kg p
                }
                impacts {
                    1 kg GWP
                }
            }
        """.trimIndent())
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
                params = listOf(
                    ParameterDto("my_name", PVStr("foo")),
                )
            ),
        )

        // when
        val actual = service.analyze(request).getElements()[1]

        // then success
        assertThat(actual.target.getDisplayName())
            .isEqualTo("p from p{}{my_name=foo}")
    }

    @Test
    fun analyze_singleRequest_withGlobals_Num_isNotEmpty() {
        // given
        val symbolTable = prepare("""
            process p {
                products {
                    1 kg p
                }
                impacts {
                    x GWP
                }
            }
        """.trimIndent())
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
            ),
            globals = listOf(
                ParameterDto("x", PVNum(1.0, "kg")),
            )
        )

        // when
        val actual = service.analyze(request)
            .getElements()[1]

        // then success
        assertThat(actual.impacts[Indicator.GWP]).isEqualTo(
            QuantityFixture.oneKg,
        )
    }

    @Test
    fun analyze_singleRequest_withGlobals_Str_isNotEmpty() {
        // given
        val symbolTable = prepare("""
            process p {
                params {
                    my_name = x
                }
                products {
                    1 kg p
                }
                impacts {
                    1 kg GWP
                }
            }
        """.trimIndent())
        val sourceOps = mockk<DefaultDataSourceOperations<BasicNumber>>()
        every { sourceOps.overrideWith(any()) } returns sourceOps
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
            ),
            globals = listOf(
                ParameterDto("x", PVStr("foo")),
            )
        )

        // when
        val actual = service.analyze(request).getElements()[1]

        // then success
        assertThat(actual.target.getDisplayName())
            .isEqualTo("p from p{}{my_name=foo}")
    }

    @Test
    fun analyze_singleRequest_withOnlineDataSources_isNotEmpty() {
        // given
        val symbolTable = prepare("""
            datasource inventory {
                schema {
                    id = "s00"
                    WU = 0 l
                    GWP = 0 kg
                }
            }
            process p {
                products {
                    1 kg p
                }
                variables {
                    data = lookup inventory match (id = "s01")
                }
                impacts {
                    data.GWP GWP
                }
            }
        """.trimIndent())
        val sourceOps = DefaultDataSourceOperations(
            ops = BasicOperations,
            config = LcaacConfig(),
            connectors = emptyMap(),
        )
        val service = TraceService(
            parsingService,
            sourceOps,
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            demand = DemandDto(
                productName = "p",
                quantity = QuantityDto(1.0, "kg"),
                processName = "p",
            ),
            datasources = listOf(
                DatasourceDto(
                    name = "inventory",
                    records = records("""
                        id,WU,GWP,other
                        s00,4.0,2.0,pool-01
                        s01,3.0,1.0,pool-01
                    """.trimIndent())
                )
            )
        )

        // when
        val actual = service.analyze(request)

        // then success
        assertThat(actual.isEmpty()).isFalse()
    }

    private fun records(
        content: String
    ): List<RecordDto> {
        val lines = content.lines()
        val header = lines[0].trim(',').split(',')
        val rest = lines.drop(1)
        return rest.map { line ->
            val entries = line.trim(' ')
                .split(',')
                .mapIndexed { idx, it ->
                    try {
                        EntryDto(
                            name = header[idx],
                            value = VNum(parseDouble(it))
                        )
                    } catch (e: NumberFormatException) {
                        EntryDto(
                            name = header[idx],
                            value = VStr(it)
                        )
                    }
                }
            RecordDto(entries)
        }
    }
}
