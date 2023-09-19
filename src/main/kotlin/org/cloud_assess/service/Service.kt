package org.cloud_assess.service

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.request.InternalWorkloadDto
import org.cloud_assess.dto.request.VirtualMachineDto
import org.cloud_assess.dto.response.VirtualMachineListAssessmentDto
import org.cloud_assess.dto.response.*
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

            /*
                Results
             */
            val results = vms.map { vm ->
                val id = vm.id
                val qty = QuantityDto(1.0, "hour")
                val meta = vm.meta

                val source = analysis.getObservablePorts().getElements()
                    .filterIsInstance<ProductValue<BasicNumber>>()
                    .firstOrNull { it.fromProcessRef?.arguments?.get("id")?.toString() == id }
                    ?: throw IllegalStateException("no impacts found for id=$id")
                val impacts = Indicator.entries.associate { indicator ->
                    val target = analysis.getControllablePorts().getElements()
                        .filterIsInstance<IndicatorValue<BasicNumber>>()
                        .firstOrNull { it.name == indicator.name }
                        ?: throw IllegalStateException("no impact found for indicator=${indicator.name}")
                    val impact = analysis.getPortContribution(source, target)
                    indicator.name to ImpactDto(
                        total = QuantityDto(
                            impact.amount.value,
                            impact.unit.toString(),
                        )
                    )
                }
                AssessmentDto(
                    request = RequestDto(
                        id = id,
                        quantity = qty,
                        meta = meta,
                    ),
                    impacts = ImpactsDto(
                        ADPe = impacts["ADPe"]!!,
                        ADPf = impacts["ADPf"]!!,
                        AP = impacts["AP"]!!,
                        GWP = impacts["GWP"]!!,
                        LU = impacts["LU"]!!,
                        ODP = impacts["ODP"]!!,
                        PM = impacts["PM"]!!,
                        POCP = impacts["POCP"]!!,
                        WU = impacts["WU"]!!,
                        CTUe = impacts["CTUe"]!!,
                        CTUh_c = impacts["CTUh_c"]!!,
                        CTUh_nc = impacts["CTUh_nc"]!!,
                        Epf = impacts["Epf"]!!,
                        Epm = impacts["Epm"]!!,
                        Ept = impacts["Ept"]!!,
                        IR = impacts["IR"]!!,
                    )
                )
            }

            return VirtualMachineListAssessmentDto(
                virtualMachines = results,
            )
        }
    }
}
