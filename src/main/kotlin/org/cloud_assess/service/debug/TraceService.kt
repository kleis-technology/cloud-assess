package org.cloud_assess.service.debug

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.TraceRequestDto
import org.springframework.stereotype.Service

@Service
class TraceService(

) {
    fun trace(request: TraceRequestDto): EvaluationTrace<BasicNumber> {
        TODO()
    }
}
