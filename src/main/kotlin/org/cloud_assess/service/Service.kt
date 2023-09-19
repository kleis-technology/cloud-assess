package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.InternalWorkloadDto
import org.cloud_assess.dto.VirtualMachineDto
import org.cloud_assess.dto.VirtualMachineListAssessmentDto
import org.springframework.stereotype.Service

@Service
class Service(
    private val evaluator: Evaluator<BasicNumber>,
) {
    fun virtualMachines(
        vms: List<VirtualMachineDto>,
        internalWorkload: InternalWorkloadDto,
    ): VirtualMachineListAssessmentDto {
        with(BasicOperations) {
            /*
                Prepare
             */
            val gbHour = EQuantityMul<BasicNumber>(EDataRef("GB"), EDataRef("hour"))
            val oneHour = EQuantityScale(pure(1.0), EDataRef("hour"))
            val onePiece = EQuantityScale(pure(1.0), EDataRef("piece"))
            val inputs = vms.map {
                val id = EStringLiteral<BasicNumber>(it.id)
                val ramSize = EQuantityScale(pure(it.ram.amount), gbHour)
                val storageSize = EQuantityScale(pure(it.storage.amount), gbHour)
                val totalNbClients = EQuantityScale(pure(vms.size.toDouble()), EDataRef("workload_slot"))
                val internalRam = EQuantityScale(pure(internalWorkload.ram.amount), gbHour)
                val internalStorage = EQuantityScale(pure(internalWorkload.storage.amount), gbHour)
                ETechnoExchange(
                    quantity = oneHour,
                    product = EProductSpec(
                        name = "vm",
                        fromProcess = FromProcess(
                            name = "virtual_machine",
                            matchLabels = MatchLabels(emptyMap()),
                            arguments = mapOf(
                                "id" to id,
                                "ram_size" to ramSize,
                                "storage_size" to storageSize,
                                "total_nb_clients" to totalNbClients,
                                "internal_ram" to internalRam,
                                "internal_storage" to internalStorage,
                            ),
                        )
                    )
                )
            }
            val main = EProcessTemplate(
                params = emptyMap(),
                locals = emptyMap(),
                body = EProcess(
                    "main",
                    products = listOf(
                        ETechnoExchange(onePiece, EProductSpec("main", onePiece))
                    ),
                    inputs = inputs,
                )
            )
            val trace = evaluator.trace(EProcessTemplateApplication(main, emptyMap()))
            val systemValue = trace.getSystemValue()
            val entryPoint = trace.getEntryPoint()

            /*
                Analyze
             */
            val program = ContributionAnalysisProgram(systemValue, entryPoint)
            val analysis = program.run()
            return VirtualMachineListAssessmentDto(
                virtualMachines = emptyList(),
            )
        }
    }
}
