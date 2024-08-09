package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.*
import org.cloud_assess.model.Indicator
import org.cloud_assess.model.ResourceAnalysis
import org.cloud_assess.model.ResourceTrace
import org.cloud_assess.model.ResourceTraceElement
import org.springframework.stereotype.Service

@Service
class MapperService {
    fun map(
        analysis: Map<String, ResourceTrace>,
        dto: TraceRequestListDto,
    ): TraceResponseListDto {
        return TraceResponseListDto(
            analysis.entries.toList()
                .map { traceResponseDto(it) }
        )
    }

    private fun traceResponseDto(entry: Map.Entry<String, ResourceTrace>): TraceResponseDto {
        val requestId = entry.key
        val resourceTrace = entry.value
        return TraceResponseDto(
            requestId = requestId,
            trace = resourceTrace.getElements()
                .map { traceResponseRowDto(it) }
        )
    }

    private fun traceResponseRowDto(element: ResourceTraceElement): TraceResponseRowDto {
        return when (val target = element.target) {
            is IndicatorValue -> TraceResponseRowDto(
                depth = element.depth,
                name = target.name,
                supply = element.supply.toQuantityDto(),
                impacts = impactsDto(element.impacts)
            )

            is ProductValue -> TraceResponseRowDto(
                depth = element.depth,
                name = target.name,
                processName = target.fromProcessRef?.name,
                params = target.fromProcessRef?.arguments?.mapNotNull {
                    parameterDto(it)
                },
                labels = target.fromProcessRef?.matchLabels?.map {
                    processLabelDto(it)
                },
                supply = element.supply.toQuantityDto(),
                impacts = impactsDto(element.impacts)
            )

            is FullyQualifiedSubstanceValue -> TraceResponseRowDto(
                depth = element.depth,
                name = target.name,
                compartment = target.compartment,
                subCompartment = target.subcompartment,
                supply = element.supply.toQuantityDto(),
                impacts = impactsDto(element.impacts)
            )

            is PartiallyQualifiedSubstanceValue -> TraceResponseRowDto(
                depth = element.depth,
                name = target.name,
                supply = element.supply.toQuantityDto(),
                impacts = impactsDto(element.impacts)
            )
        }
    }

    private fun processLabelDto(it: Map.Entry<String, StringValue<BasicNumber>>) =
        ProcessLabelDto(
            name = it.key,
            value = it.value.s,
        )

    private fun parameterDto(it: Map.Entry<String, DataValue<BasicNumber>>) =
        when (val value = it.value) {
            is QuantityValue -> ParameterDto(
                name = it.key,
                value = PVNum(value.amount.value, value.unit.toString()),
            )

            is StringValue -> ParameterDto(
                name = it.key,
                value = PVStr(
                    value = value.s,
                )
            )

            is RecordValue -> null
        }

    @Suppress("DuplicatedCode")
    private fun impactsDto(impacts: Map<Indicator, QuantityValue<BasicNumber>?>): ImpactsDto {
        return ImpactsDto(
            adPe = impactDto(impacts, Indicator.ADPe),
            adPf = impactDto(impacts, Indicator.ADPf),
            AP = impactDto(impacts, Indicator.AP),
            GWP = impactDto(impacts, Indicator.GWP),
            LU = impactDto(impacts, Indicator.LU),
            ODP = impactDto(impacts, Indicator.ODP),
            PM = impactDto(impacts, Indicator.PM),
            POCP = impactDto(impacts, Indicator.POCP),
            WU = impactDto(impacts, Indicator.WU),
            ctUe = impactDto(impacts, Indicator.CTUe),
            ctUhC = impactDto(impacts, Indicator.CTUh_c),
            ctUhNc = impactDto(impacts, Indicator.CTUh_nc),
            epf = impactDto(impacts, Indicator.Epf),
            epm = impactDto(impacts, Indicator.Epm),
            ept = impactDto(impacts, Indicator.Ept),
            IR = impactDto(impacts, Indicator.IR),
        )
    }
    
    private fun impactDto(impacts: Map<Indicator, QuantityValue<BasicNumber>?>, indicator: Indicator): ImpactDto {
        return impacts[indicator]?.let { ImpactDto(total = it.toQuantityDto()) }
            ?: ImpactDto(total = QuantityDto(amount = 0.0, unit = ""))
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

    @Suppress("DuplicatedCode")
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
