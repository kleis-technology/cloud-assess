package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExecutionServiceTest {
    @Test
    fun run() {
        // given
        val oneKg = EQuantityScale(
            BasicNumber(1.0),
            EUnitLiteral(UnitSymbol.of("kg"), 1.0, Dimension.Companion.of("mass"))
        )
        val main = EProcessTemplateApplication(
            EProcessTemplate(
                emptyMap(),
                emptyMap(),
                body = EProcess(
                    "main",
                    products = listOf(
                        ETechnoExchange(oneKg, EProductSpec("main", oneKg))
                    ),
                    impacts = listOf(
                        EImpactBlockEntry(
                            EImpact(EQuantityScale(BasicNumber(1.0), oneKg), EIndicatorSpec("GWP", oneKg))
                        )
                    )
                )
            ),
            emptyMap()
        )
        val evaluator = Evaluator(SymbolTable.empty(), BasicOperations, mockk())
        val executionService = ExecutionService(evaluator)

        // when
        val analysis = executionService.run(main)
        val port = analysis.getObservablePorts().getElements().first { it.getShortName() == "main" }
        val indicator = analysis.getControllablePorts().getElements().first { it.getShortName() == "GWP" }
        val actual = analysis.getPortContribution(port, indicator)

        // then
        val expected = QuantityValue(
            BasicNumber(1.0),
            UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.Companion.of("mass"))
        )
        assertEquals(expected, actual)
    }
}
