package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.TraceRequestDto
import org.cloud_assess.dto.TraceRequestListDto
import org.cloud_assess.model.ResourceTrace
import org.springframework.stereotype.Service

@Service
class TraceService(
    private val defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    private val symbolTable: SymbolTable<BasicNumber>,
) {
    fun analyze(request: TraceRequestListDto): Map<String, ResourceTrace> {
        return request.elements
            .associate { it.requestId to analyze(it) }
    }

    private fun analyze(request: TraceRequestDto): ResourceTrace {
        /*
            product from process
                parameters
                labels
            merge globals into symbol table
            override datasources
         */

        return ResourceTrace(
            id = request.requestId,
            rawTrace = EvaluationTrace.empty(),
        )
    }
}
