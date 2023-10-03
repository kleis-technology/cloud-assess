package org.cloud_assess.fixtures

import org.cloud_assess.dto.request.*
import org.cloud_assess.dto.response.*

class DtoFixture {
    companion object {
        fun serviceLayerDto(): ServiceLayerDto {
            return ServiceLayerDto(
                internalWorkloadDto(),
                listOf(
                    virtualMachineDto("c1"),
                )
            )
        }

        fun internalWorkloadDto(): InternalWorkloadDto {
            return InternalWorkloadDto(
                ram = quantityMemoryTime(10.0),
                storage = quantityMemoryTime(10.0),
            )
        }

        fun virtualMachineDto(id: String): VirtualMachineDto {
            return VirtualMachineDto(
                id,
                ram = quantityMemoryTime(10.0),
                storage = quantityMemoryTime(10.0),
                meta = mapOf(
                    "region" to "FR",
                ),
            )
        }

        fun quantityMemoryTime(amount: Double = 10.0): QuantityMemoryTimeDto {
            return QuantityMemoryTimeDto(
                amount,
                MemoryTimeUnitsDto.GB_HOUR
            )
        }

        fun assessmentDto(id: String): AssessmentDto {
            return AssessmentDto(
                request = requestDto(id),
                impacts = ImpactsDto(
                    ADPe = impactDto(),
                    ADPf = impactDto(),
                    AP = impactDto(),
                    GWP = impactDto(),
                    LU = impactDto(),
                    ODP = impactDto(),
                    PM = impactDto(),
                    POCP = impactDto(),
                    WU = impactDto(),
                    CTUe = impactDto(),
                    CTUh_c = impactDto(),
                    CTUh_nc = impactDto(),
                    Epf = impactDto(),
                    Epm = impactDto(),
                    Ept = impactDto(),
                    IR = impactDto(),
                )
            )
        }

        fun impactDto(): ImpactDto {
            return ImpactDto(
                total = oneKgCO2eqDto(),
            )
        }

        fun requestDto(id: String): RequestDto {
            return RequestDto(
                id = id,
                quantity = oneHourDto(),
                meta = emptyMap(),
            )
        }

        fun oneHourDto(): QuantityDto {
            return QuantityDto(
                1.0,
                "hour"
            )
        }

        fun oneKgCO2eqDto(): QuantityDto {
            return QuantityDto(
                1.0,
                "kgCO2eq"
            )
        }
    }
}
