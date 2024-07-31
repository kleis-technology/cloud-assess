package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
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
        """.trimIndent())
        val requestList = DtoFixture.traceRequestList(3)
        val service = TraceService(parsingService, mockk(), symbolTable)

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
        val service = TraceService(
            parsingService,
            mockk(),
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            product = ProductDemandDto(QuantityDto(1.0, "kg"), "p"),
            fromProcess = FromProcessDto("p"),
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
        val service = TraceService(
            parsingService,
            mockk(),
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            product = ProductDemandDto(QuantityDto(1.0, "kg"), "p"),
            fromProcess = FromProcessDto("p"),
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
        val service = TraceService(
            parsingService,
            mockk(),
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            product = ProductDemandDto(QuantityDto(1.0, "kg"), "p"),
            fromProcess = FromProcessDto("p"),
        )

        // when
        val actual = service.analyze(request)

        // then
        assertThat(actual.contribution(Indicator.GWP)).isEqualTo(
            QuantityFixture.oneKg,
        )
    }

    @Test
    fun analyze_singleRequest_withGlobals_isNotEmpty() {
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
        val service = TraceService(
            parsingService,
            mockk(),
            symbolTable,
        )
        val request = TraceRequestDto(
            requestId = "r01",
            product = ProductDemandDto(QuantityDto(1.0, "kg"), "p"),
            fromProcess = FromProcessDto("p"),
            globals = listOf(
                ParameterDto("x", QuantityDto(1.0, "kg"))
            )
        )

        // when
        val actual = service.analyze(request)

        // then success
        assertThat(actual.isEmpty()).isFalse()
    }
}
