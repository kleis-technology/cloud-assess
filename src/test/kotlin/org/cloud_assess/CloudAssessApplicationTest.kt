package org.cloud_assess

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.cloud_assess.dto.PoolListDto
import org.cloud_assess.dto.QuantityTimeDto
import org.cloud_assess.dto.TimeUnitsDto
import org.cloud_assess.dto.VirtualMachineListDto
import org.cloud_assess.fixtures.DtoFixture
import org.cloud_assess.model.Indicator
import org.cloud_assess.service.PoolService
import org.cloud_assess.service.VirtualMachineService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CloudAssessApplicationTest(
    @Autowired
    private val virtualMachineService: VirtualMachineService,
    @Autowired
    private val poolService: PoolService,
) {

    @Test
    fun contextLoads() {
    }

    @Test
    fun poolService() {
        // given
        val pools = PoolListDto(
            period = QuantityTimeDto(1.0, TimeUnitsDto.hour),
            pools = listOf(
                DtoFixture.poolDto("client_vm", 1.0)
            )
        )

        // when
        val actual = poolService.analyze(pools)

        // then
        listOf("client_vm").forEach { id ->
            val pool = actual[id]!!
            assertThat(pool.period).isEqualTo(QuantityTimeDto(1.0, TimeUnitsDto.hour))
            val gwp = pool.contribution(Indicator.GWP)
            assertThat(gwp.amount.value).isCloseTo( 5.71403, Percentage.withPercentage(1e-3))
            assertThat(gwp.unit).isEqualTo(
                UnitValue<BasicNumber>(
                    UnitSymbol.of("kg CO2-Eq"),
                    1.0,
                    Dimension.of("global warming potential (GWP100)")
                )
            )
        }
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
            assertThat(gwp.amount.value).isCloseTo(0.971400, Percentage.withPercentage(1e-3))
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
