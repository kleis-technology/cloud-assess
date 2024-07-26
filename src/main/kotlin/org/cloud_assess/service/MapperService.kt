package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.*
import org.cloud_assess.model.Indicator
import org.cloud_assess.model.ResourceAnalysis
import org.cloud_assess.model.ResourceTrace
import org.springframework.stereotype.Service

@Service
class MapperService {
    fun map(request: TraceRequestDto): EProcessTemplateApplication<BasicNumber> {
        TODO()
    }

    fun map(
        analysis: Map<String, ResourceTrace>,
        dto: TraceRequestListDto,
    ): TraceResponseListDto {
        TODO()
    }

    fun map(
        analysis: Map<String, ResourceAnalysis>,
        dto: PoolListDto
    ): PoolListAssessmentDto {
        return PoolListAssessmentDto(
            dto.pools.map {
                val poolAnalysis = analysis[it.id] ?: throw IllegalStateException("Unknown pool '${it.id}'")
                PoolAssessmentDto(
                    period = dto.period,
                    request = it,
                    impacts = impactsDto(poolAnalysis),
                )
            }
        )
    }

    fun map(
        analysis: Map<String, ResourceAnalysis>,
        dto: VirtualMachineListDto
    ): VirtualMachineListAssessmentDto {
        return VirtualMachineListAssessmentDto(
            dto.virtualMachines.map {
                val vmAnalysis = analysis[it.id] ?: throw IllegalStateException("Unknown virtual machines '${it.id}'")
                VirtualMachineAssessmentDto(
                    period = dto.period,
                    request = it,
                    impacts = impactsDto(vmAnalysis),
                )
            }
        )
    }

    private fun impactDto(analysis: ResourceAnalysis, target: Indicator): ImpactDto {
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

    private fun impactsDto(analysis: ResourceAnalysis): ImpactsDto {
        return ImpactsDto(
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
    }
}
