package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import io.mockk.mockk
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.dto.TraceRequestDto
import org.cloud_assess.fixtures.DtoFixture
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension

class TraceServiceTest {

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
        val requestList = DtoFixture.traceRequestList(3)
        val service = TraceService(mockk(), mockk())

        // when
        val actual = service.analyze(requestList).keys

        // then
        val expected = requestList
            .elements.map { it.requestId }
            .toSet()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun analyze_singleRequest_simpleCase() {
        // given
        val symbolTable = prepare("""
            process p {
                product {
                    1 kg p
                }
                impacts {
                    1 kg GWP
                }
            }
        """.trimIndent())
        val service = TraceService(
            mockk(),
            symbolTable,
        )
    }
}
