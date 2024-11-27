package org.cloud_assess.service

import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.cloud_assess.dto.*
import org.cloud_assess.model.*
import org.springframework.stereotype.Service

@Service
class MapperService {
    fun map(
        analysis: ResourceTraceAnalysis,
    ): TraceResponseListDto {
        val entries = analysis.elements.entries
        return TraceResponseListDto(
            meta = analysis.meta,
            elements = entries.toList()
                .map { traceResponseDto(it) }
        )
    }

    private fun traceResponseDto(entry: Map.Entry<String, ResourceTrace>): TraceResponseDto {
        val requestId = entry.key
        val resourceTrace = entry.value
        return TraceResponseDto(
            requestId = requestId,
            meta = resourceTrace.getMeta(),
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
                impacts = rawImpactsDto(element.rawImpacts)
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
                impacts = rawImpactsDto(element.rawImpacts)
            )

            is FullyQualifiedSubstanceValue -> TraceResponseRowDto(
                depth = element.depth,
                name = target.name,
                compartment = target.compartment,
                subCompartment = target.subcompartment,
                supply = element.supply.toQuantityDto(),
                impacts = rawImpactsDto(element.rawImpacts)
            )

            is PartiallyQualifiedSubstanceValue -> TraceResponseRowDto(
                depth = element.depth,
                name = target.name,
                supply = element.supply.toQuantityDto(),
                impacts = rawImpactsDto(element.rawImpacts)
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

    private fun rawImpactsDto(impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>?>): List<RawImpactDto> {
        return impacts.entries.map { entry ->
            RawImpactDto(
                indicator = entry.key.getShortName(),
                value = entry.value?.let {
                    it.toQuantityDto()
                } ?: QuantityDto( amount = 0.0, unit = "")
            )
        }.toList()
    }

    fun map(
        analysis: Map<String, ResourceAnalysis>,
        dto: ComputeResourceListDto,
    ): ComputeResourceListAssessmentDto {
        return ComputeResourceListAssessmentDto(
            dto.computeResources.map {
                val computeResourceAnalysis = analysis[it.id] ?: throw IllegalStateException("Unknown compute_resource '${it.id}'")
                ComputeResourceAssessmentDto(
                    period = dto.period,
                    request = it,
                    impacts = impactsDto(computeResourceAnalysis)
                )
            }
        )
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
        val total = analysis.total(target)
        val manufacturing =  analysis.manufacturing(target)
        val transport = analysis.transport(target)
        val use = analysis.use(target)
        val endOfLife = analysis.endOfLife(target)
        return ImpactDto(
            total = total.toQuantityDto(),
            perLcStep = ImpactPerLcStepDto(
                manufacturing = manufacturing.toQuantityDto(),
                transport = transport.toQuantityDto(),
                use = use.toQuantityDto(),
                endOfLife = endOfLife.toQuantityDto(),
            )
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

    fun map(analysis: Map<String, ResourceAnalysis>, dto: StorageResourceListDto): StorageResourceListAssessmentDto {
        return StorageResourceListAssessmentDto(
            dto.storageResources.map {
                val storageResourceAnalysis = analysis[it.id] ?: throw IllegalStateException("Unknown storage_resource '${it.id}'")
                StorageResourceAssessmentDto(
                    period = dto.period,
                    request = it,
                    impacts = impactsDto(storageResourceAnalysis),
                )
            }
        )
    }
}
