package org.cloud_assess.model

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.math.basic.BasicNumber

class ResourceTrace(
    private val id: String,
    private val rawTrace: EvaluationTrace<BasicNumber>,
) {
}
