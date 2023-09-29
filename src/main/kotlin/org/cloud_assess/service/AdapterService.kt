package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.model.VirtualMachineAnalysis
import org.springframework.stereotype.Service

@Service
class AdapterService {
    fun adapt(id: String, rawAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>): VirtualMachineAnalysis {
        return VirtualMachineAnalysis(id, rawAnalysis)
    }
}
