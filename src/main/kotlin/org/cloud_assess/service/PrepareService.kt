package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.request.MemoryTimeUnitsDto
import org.cloud_assess.dto.request.QuantityMemoryTimeDto
import org.cloud_assess.dto.request.ServiceLayerDto
import org.springframework.stereotype.Service

@Service
class PrepareService {
    fun prepare(
        svc: ServiceLayerDto,
    ): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val nbClients = svc.virtualMachines.size
        return svc.virtualMachines.associate {
            val oneHour = EQuantityScale(BasicNumber(1.0), EDataRef("hour"))
            val product = EProductSpec(it.id, oneHour)
            val input = EProductSpec(
                name = "vm",
                referenceUnit = oneHour,
                fromProcess = FromProcess(
                    name = "virtual_machine",
                    matchLabels = MatchLabels(emptyMap()),
                    arguments = mapOf(
                        "ram_size" to it.ram.expression(),
                        "storage_size" to it.storage.expression(),
                        "total_nb_clients" to nbClients.expression(),
                        "internal_ram" to svc.internalWorkload.ram.expression(),
                        "internal_storage" to svc.internalWorkload.storage.expression(),
                    ),
                )
            )
            it.id to EProcessTemplateApplication(
                template = EProcessTemplate(
                    params = emptyMap(),
                    locals = emptyMap(),
                    body = EProcess(
                        name = it.id,
                        products = listOf(
                            ETechnoExchange(oneHour, product)
                        ),
                        inputs = listOf(
                            ETechnoExchange(oneHour, input)
                        )
                    )
                ),
                emptyMap(),
            )
        }
    }

    private fun QuantityMemoryTimeDto.expression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            MemoryTimeUnitsDto.GB_HOUR -> {
                val gbHour = EQuantityMul<BasicNumber>(EDataRef("GB"), EDataRef("hour"))
                EQuantityScale(BasicNumber(this.amount), gbHour)
            }
        }
    }

    private fun Int.expression(): DataExpression<BasicNumber> {
        return EQuantityScale(BasicNumber(this.toDouble()), EDataRef("workload_slot"))
    }
}
