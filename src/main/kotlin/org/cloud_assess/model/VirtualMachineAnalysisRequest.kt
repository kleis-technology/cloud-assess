package org.cloud_assess.model

import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.QuantityTimeDto

data class VirtualMachineAnalysisRequest(
    val period: QuantityTimeDto,
    val cases: Map<String, EProcessTemplateApplication<BasicNumber>>,
)
