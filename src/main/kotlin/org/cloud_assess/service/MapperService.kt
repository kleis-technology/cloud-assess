package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.*
import org.cloud_assess.model.Indicator
import org.cloud_assess.model.ResourceAnalysis
import org.springframework.stereotype.Service

@Service
class MapperService {
    fun map(
        analysis: Map<String, ResourceAnalysis>,
        dto: PoolListDto
    ): PoolListAssessmentDto {
        return PoolListAssessmentDto(
            dto.pools.map {
                val poolAnalysis = analysis[it.id] ?: throw IllegalStateException("")
                assessmentDto(poolAnalysis, it.id, it.meta)
            }
        )
    }

    fun map(
        analysis: Map<String, ResourceAnalysis>,
        dto: VirtualMachineListDto
    ): VirtualMachineListAssessmentDto {
        return VirtualMachineListAssessmentDto(
            dto.virtualMachines.map {
                val vmAnalysis = analysis[it.id] ?: throw IllegalStateException("")
                assessmentDto(vmAnalysis, it.id, it.meta)
            }
        )
    }

    fun impactDto(analysis: ResourceAnalysis, target: Indicator): ImpactDto {
        val quantity = analysis.contribution(target)
        return ImpactDto(
            total = quantity.toQuantityDto()
        )
    }

    private fun QuantityValue<BasicNumber>.toQuantityDto(): QuantityDto {
        return QuantityDto(
            this.amount.value,
            this.unit.toString(),
        )
    }

    fun assessmentDto(analysis: ResourceAnalysis, id: String, meta: Map<String, String>): AssessmentDto {
        return AssessmentDto(
            request = RequestDto(
                id = id,
                quantity = QuantityDto(analysis.period.amount, analysis.period.unit.value),
                meta = meta,
            ),
            impacts = ImpactsDto(
                adPe = impactDto(analysis, Indicator.ADPe),
                adPf = impactDto(analysis, Indicator.ADPf),
                AP = impactDto(analysis, Indicator.AP),
                GWP = impactDto(analysis, Indicator.GWP),
                LU = impactDto(analysis, Indicator.LU),
                ODP = impactDto(analysis, Indicator.ODP),
                PM = impactDto(analysis, Indicator.PM),
                POCP = impactDto(analysis, Indicator.POCP),
                WU = impactDto(analysis, Indicator.WU),
                ctUe = impactDto(analysis, Indicator.CTUe),
                ctUhC = impactDto(analysis, Indicator.CTUh_c),
                ctUhNc = impactDto(analysis, Indicator.CTUh_nc),
                epf = impactDto(analysis, Indicator.Epf),
                epm = impactDto(analysis, Indicator.Epm),
                ept = impactDto(analysis, Indicator.Ept),
                IR = impactDto(analysis, Indicator.IR),
            )
        )
    }
}
