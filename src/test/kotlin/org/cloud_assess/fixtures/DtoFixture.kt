package org.cloud_assess.fixtures

import org.cloud_assess.dto.*

@Suppress("SameParameterValue")
class DtoFixture {
    companion object {
        fun traceRequestListWithSpecificGlobalsAndDatasources(size: Int = 3): TraceRequestListDto {
            return TraceRequestListDto((1..size).map { traceRequestDtoWithGlobalAndDatasource("r$it") })
        }

        fun traceRequestListWithCommonGlobalAndDatasource(size: Int = 3): TraceRequestListDto {
            return TraceRequestListDto(
                elements = (1..size).map { traceRequestDto("r$it") },
                globals = listOf(
                    ParameterDto(
                        "x",
                        PVNum(1.0, "kg"),
                    )
                ),
                datasources = listOf(
                    DatasourceDto(
                        name = "inventory",
                        records = listOf(
                            RecordDto(
                                listOf(
                                    EntryDto("x", VNum(1.0))
                                )
                            )
                        )
                    )
                )
            )
        }

        fun traceRequestList(size: Int = 3): TraceRequestListDto {
            return TraceRequestListDto(
                elements = (1..size).map { traceRequestDto("r$it") },
            )
        }

        private fun traceRequestDto(id: String = "r00"): TraceRequestDto {
            return TraceRequestDto(
                requestId = id,
                demand = DemandDto(
                    productName = "vm",
                    processName = "vm",
                    quantity = QuantityDto(1.0, "hour"),
                ),
                globals = emptyList(),
                meta = mapOf(
                    "group" to "foo"
                ),
                datasources = emptyList()
            )
        }


        private fun traceRequestDtoWithGlobalAndDatasource(id: String = "r00"): TraceRequestDto {
            return TraceRequestDto(
                requestId = id,
                demand = DemandDto(
                    productName = "vm",
                    processName = "vm",
                    quantity = QuantityDto(1.0, "hour"),
                ),
                globals = listOf(
                    ParameterDto(
                        "x",
                        PVNum(1.0, "kg"),
                    )
                ),
                meta = mapOf(
                    "group" to "foo"
                ),
                datasources = listOf(
                    DatasourceDto(
                        name = "inventory",
                        records = listOf(
                            RecordDto(
                                listOf(
                                    EntryDto("x", VNum(1.0))
                                )
                            )
                        )
                    )
                )
            )
        }


        fun poolListDto(): PoolListDto {
            return PoolListDto(
                period = quantityHour(1.0),
                pools = listOf(
                    poolDto("client_vm"),
                )
            )
        }

        fun poolDto(
            id: String = "client_vm",
        ): PoolDto {
            return PoolDto(
                id = id,
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
                quantity = quantityDimensionless(1.0),
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

        private fun quantityDimensionless(amount: Double = 1.0): QuantityDimensionlessDto {
            return QuantityDimensionlessDto(
                amount,
                DimensionlessUnitsDto.u,
            )
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
