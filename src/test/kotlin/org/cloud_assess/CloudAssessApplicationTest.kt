package org.cloud_assess

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.cloud_assess.dto.QuantityTimeDto
import org.cloud_assess.dto.TimeUnitsDto
import org.cloud_assess.dto.VirtualMachineListDto
import org.cloud_assess.fixtures.DtoFixture
import org.cloud_assess.model.Indicator
import org.cloud_assess.service.VirtualMachineService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CloudAssessApplicationTest(
    @Autowired
    private val virtualMachineService: VirtualMachineService
) {

    @Test
    fun contextLoads() {
    }

    @Test
    fun virtualMachineService() {
        // given
        val vms = VirtualMachineListDto(
            period = QuantityTimeDto(1.0, TimeUnitsDto.hour),
            virtualMachines = listOf(
                DtoFixture.virtualMachineDto("vm-01", "client_vm"),
                DtoFixture.virtualMachineDto("vm-02", "client_vm"),
            ),
        )

        // when
        val actual = virtualMachineService.analyze(vms)

        // then
        listOf("vm-01", "vm-02").forEach { id ->
            val vm = actual[id]!!
            assertThat(vm.period).isEqualTo(QuantityTimeDto(1.0, TimeUnitsDto.hour))
            val gwp = vm.contribution(Indicator.GWP)
            assertThat(gwp.amount.value).isCloseTo(0.65994, Percentage.withPercentage(1e-3))
            assertThat(gwp.unit).isEqualTo(
                UnitValue<BasicNumber>(
                    UnitSymbol.of("kg CO2-Eq"),
                    1.0,
                    Dimension.of("global warming potential (GWP100)")
                )
            )
        }
    }
}
