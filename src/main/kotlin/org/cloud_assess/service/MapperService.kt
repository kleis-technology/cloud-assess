package org.cloud_assess.service

import org.cloud_assess.dto.request.ServiceLayerDto
import org.cloud_assess.dto.request.VirtualMachineDto
import org.cloud_assess.dto.response.*
import org.cloud_assess.model.VirtualMachineAnalysis
import org.cloud_assess.dto.toQuantityDto
import org.springframework.stereotype.Service

@Service
class MapperService {
    fun map(analysis: Map<String, VirtualMachineAnalysis>, dto: ServiceLayerDto): VirtualMachineListAssessmentDto {
        return VirtualMachineListAssessmentDto(
            dto.virtualMachines.map {
                val vmAnalysis = analysis[it.id] ?: throw IllegalStateException("")
                assessmentDto(vmAnalysis, it)
            }
        )
    }

    fun impactDto(analysis: VirtualMachineAnalysis, target: Indicator): ImpactDto {
        val quantity = analysis.contribution(target)
        return ImpactDto(
            total = quantity.toQuantityDto()
        )
    }
    
    fun assessmentDto(analysis: VirtualMachineAnalysis, vm: VirtualMachineDto): AssessmentDto {
        return AssessmentDto(
            request = RequestDto(
                id = vm.id,
                quantity = QuantityDto(1.0, "hour"),
                meta = vm.meta,
            ),
            impacts = ImpactsDto(
                ADPe = impactDto(analysis, Indicator.ADPe),
                ADPf = impactDto(analysis, Indicator.ADPf),
                AP = impactDto(analysis, Indicator.AP),
                GWP = impactDto(analysis, Indicator.GWP),
                LU = impactDto(analysis, Indicator.LU),
                ODP = impactDto(analysis, Indicator.ODP),
                PM = impactDto(analysis, Indicator.PM),
                POCP = impactDto(analysis, Indicator.POCP),
                WU = impactDto(analysis, Indicator.WU),
                CTUe = impactDto(analysis, Indicator.CTUe),
                CTUh_c = impactDto(analysis, Indicator.CTUh_c),
                CTUh_nc = impactDto(analysis, Indicator.CTUh_nc),
                Epf = impactDto(analysis, Indicator.Epf),
                Epm = impactDto(analysis, Indicator.Epm),
                Ept = impactDto(analysis, Indicator.Ept),
                IR = impactDto(analysis, Indicator.IR),
            )
        )
    }
}
