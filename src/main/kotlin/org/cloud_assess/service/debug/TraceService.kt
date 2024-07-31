package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.TraceRequestDto
import org.cloud_assess.dto.TraceRequestListDto
import org.cloud_assess.model.ResourceTrace
import org.cloud_assess.service.ParsingService
import org.springframework.stereotype.Service

@Service
class TraceService(
    private val parsingService: ParsingService,
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    fun analyze(request: TraceRequestListDto): Map<String, ResourceTrace> {
        return request.elements
            .associate { it.requestId to analyze(it) }
    }

    fun analyze(request: TraceRequestDto): ResourceTrace {
        /*
            override datasources
         */

        val processApplication = prepare(request)
        val symbolTablesWithGlobals = symbolTable.copy(
            data = globals(symbolTable.data, request),
        )
        val evaluator = Evaluator(symbolTablesWithGlobals, BasicOperations, defaultDataSourceOperations)
        val trace = evaluator.with(processApplication.template)
            .trace(processApplication.template, processApplication.arguments)
        val systemValue = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val program = ContributionAnalysisProgram(systemValue, entryPoint)
        val analysis = program.run()

        return ResourceTrace(
            id = request.requestId,
            rawTrace = trace,
            contributionAnalysis = analysis,
        )
    }

    private fun globals(dataRegister: DataRegister<BasicNumber>, request: TraceRequestDto): DataRegister<BasicNumber> {
        val requestGlobals = request.globals ?: emptyList()
        return requestGlobals.fold(dataRegister) { register, parameter ->
            register.override(
                DataKey(parameter.name),
                EQuantityScale(
                    BasicNumber(parameter.value.amount),
                    EDataRef(parameter.value.unit),
                )
            )
        }
    }

    private fun prepare(request: TraceRequestDto): EProcessTemplateApplication<BasicNumber> {
        val quantity = "${request.product.quantity.amount} ${request.product.quantity.unit}"
        val processName = request.fromProcess.name
        val processParams = request.fromProcess.params?.joinToString(", ") {
            "${it.name} = ${it.value.amount} ${it.value.unit}"
        }?.let {
            if (it.isBlank()) ""
            else "($it)"
        } ?: ""
        val processLabels = request.fromProcess.labels?.joinToString {
            "${it.name} = ${it.value}"
        }?.let {
            if (it.isBlank()) ""
            else "match ($it)"
        } ?: ""

        val content = """
            process __main__ {
                products {
                    1 u __main__
                }
                inputs {
                    $quantity from $processName${processParams} $processLabels
                }
            }
        """.trimIndent()
        return parsingService.processTemplateApplication(content)
    }
}
