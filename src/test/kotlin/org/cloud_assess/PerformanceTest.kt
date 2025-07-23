package org.cloud_assess

import org.assertj.core.api.Assertions.assertThat
import org.cloud_assess.dto.*
import org.cloud_assess.fixtures.DtoFixture
import org.cloud_assess.service.VirtualMachineService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Tag("Performance")
class PerformanceTest(
    @Autowired
    private val virtualMachineService: VirtualMachineService,
) {
    @Test
    fun virtualMachines() {
        // given
        val n = 2000
        val vms = VirtualMachineListDto(
            period = QuantityTimeDto(1.0, TimeUnitsDto.hour),
            totalVcpu = QuantityVCPUDto(1.0, VCPUUnitsDto.vCPU),
            totalRam = QuantityMemoryDto(1.0, MemoryUnitsDto.gB),
            totalStorage = QuantityMemoryDto(1.0, MemoryUnitsDto.gB),
            virtualMachines = (1..n).map {
                DtoFixture.virtualMachineDto("vm-$it", "client_vm")
            },
        )

        // when
        val actual = virtualMachineService.analyze(vms)

        // then
        assertThat(actual).hasSize(n)
    }
}
