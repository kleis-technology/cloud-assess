package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.request.ServiceLayerDto
import org.springframework.stereotype.Service

@Service
class PrepareService(
    private val parsingService: ParsingService,
) {
    fun prepare(
        svc: ServiceLayerDto,
    ): Map<String, EProcessTemplateApplication<BasicNumber>> {
        val nbClients = svc.virtualMachines.size
        return svc.virtualMachines.associate {
            val content = """
                process ${it.id} {
                    products {
                        1 hour ${it.id}
                    }
                    inputs {
                        1 hour vm from virtual_machine(
                            ram_size = ${it.ram.amount} GB * hour,
                            storage_size = ${it.storage.amount} GB * hour,
                            nb_clients = ${nbClients.toDouble()} slot,
                            internal_ram = ${svc.internalWorkload.ram.amount} GB * hour,
                            internal_storage = ${svc.internalWorkload.storage.amount} GB * hour,
                            )
                    }
                }
            """.trimIndent()
            it.id to parsingService.processTemplateApplication(content)
        }
    }
}
