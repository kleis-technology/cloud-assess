package org.cloud_assess

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.ServiceLayerDto
import org.cloud_assess.dto.VirtualMachineListAssessmentDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    private val symbolTable: SymbolTable<BasicNumber>,
    private val evaluator: Evaluator<BasicNumber>,
) {
    @PostMapping("/virtual_machines")
    fun virtualMachines(
        @RequestBody dto: ServiceLayerDto
    ): VirtualMachineListAssessmentDto {
        TODO()
    }
}
