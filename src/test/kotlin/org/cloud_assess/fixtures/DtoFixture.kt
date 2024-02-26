package org.cloud_assess.fixtures

import org.cloud_assess.dto.*

@Suppress("SameParameterValue")
class DtoFixture {
    companion object {
        fun poolListDto(): PoolListDto {
            return PoolListDto(
                period = quantityHour(1.0),
                pools = listOf(
                    poolDto("client_vm", 1.0),
                )
            )
        }

        fun poolDto(
            id: String = "client_vm",
            serviceLevel: Double = 1.0,
        ): PoolDto {
            return PoolDto(
                id = id,
                serviceLevel = QuantityDimensionlessDto(
                    amount = serviceLevel,
                    unit = DimensionlessUnitsDto.u,
                ),
                meta = mapOf(
                    "region" to "FR",
                ),
            )
        }

        fun virtualMachineListDto(): VirtualMachineListDto {
            return VirtualMachineListDto(
                period = quantityHour(1.0),
                virtualMachines = listOf(
                    virtualMachineDto("vm-01"),
                )
            )
        }

        fun virtualMachineDto(
            id: String = "vm-01",
            poolId: String = "client_vm",
            ram: Double = 10.0,
            storage: Double = 10.0,
            vcpu: Double = 1.0,
        ): VirtualMachineDto {
            return VirtualMachineDto(
                id = id,
                poolId = poolId,
                ram = quantityMemory(ram),
                storage = quantityMemory(storage),
                vcpu = quantityVCPU(vcpu),
                meta = mapOf(
                    "region" to "FR",
                ),
            )
        }

        private fun quantityVCPU(amount: Double = 1.0): QuantityVCPUDto {
            return QuantityVCPUDto(amount, VCPUUnitsDto.vCPU)
        }

        private fun quantityMemory(amount: Double = 10.0): QuantityMemoryDto {
            return QuantityMemoryDto(
                amount,
                MemoryUnitsDto.gB,
            )
        }

        private fun quantityHour(amount: Double = 1.0): QuantityTimeDto {
            return QuantityTimeDto(1.0, TimeUnitsDto.hour)
        }

        private fun impactsDto(): ImpactsDto = ImpactsDto(
            adPe = impactDto(),
            adPf = impactDto(),
            AP = impactDto(),
            GWP = impactDto(),
            LU = impactDto(),
            ODP = impactDto(),
            PM = impactDto(),
            POCP = impactDto(),
            WU = impactDto(),
            ctUe = impactDto(),
            ctUhC = impactDto(),
            ctUhNc = impactDto(),
            epf = impactDto(),
            epm = impactDto(),
            ept = impactDto(),
            IR = impactDto(),
        )

        fun virtualMachineAssessmentDto(id: String): VirtualMachineAssessmentDto {
            return VirtualMachineAssessmentDto(
                period = quantityHour(1.0),
                request = virtualMachineDto(id),
                impacts = impactsDto(),
            )
        }

        fun poolAssessmentDto(id: String): PoolAssessmentDto {
            return PoolAssessmentDto(
                period = quantityHour(1.0),
                request = poolDto(id),
                impacts = impactsDto(),
            )
        }

        private fun impactDto(): ImpactDto {
            return ImpactDto(
                total = oneKgCO2eqDto(),
            )
        }

        private fun oneKgCO2eqDto(): QuantityDto {
            return QuantityDto(
                1.0,
                "kgCO2eq"
            )
        }
    }
}
