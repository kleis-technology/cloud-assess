package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.grammar.CoreMapper
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.springframework.stereotype.Service

@Service
class ParsingService {
    private val coreMapper = CoreMapper(BasicOperations)

    fun processTemplateApplication(content: String): EProcessTemplateApplication<BasicNumber> {
        val lexer = LcaLangLexer(CharStreams.fromString(content))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val ctx = parser.processDefinition()
        val preludeUnits = Prelude.units<BasicNumber>()
        return EProcessTemplateApplication(
            template = coreMapper.process(ctx, preludeUnits, DataSourceRegister.empty()),
            arguments = emptyMap()
        )
    }

    fun data(content: String): DataExpression<BasicNumber> {
        val lexer = LcaLangLexer(CharStreams.fromString(content))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val ctx = parser.dataExpression()
        return coreMapper.dataExpression(ctx)
    }
}
