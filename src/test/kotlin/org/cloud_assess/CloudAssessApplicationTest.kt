package org.cloud_assess

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.QuantityValue
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
                DtoFixture.poolDto("client_vm")
            )
        )

        // when
        val actual = poolService.analyze(pools)

        // then
        listOf("client_vm").forEach { id ->
            val pool = actual[id]!!
            assertThat(pool.period).isEqualTo(QuantityTimeDto(1.0, TimeUnitsDto.hour))

            val total = pool.total(Indicator.GWP)
            val manufacturing = pool.manufacturing(Indicator.GWP)
            val transport = pool.transport(Indicator.GWP)
            val use = pool.use(Indicator.GWP)
            val endOfLife = pool.endOfLife(Indicator.GWP)

            assertGWPKgCO2Eq(total, 7.5770786)
            assertGWPKgCO2Eq(manufacturing, 1.9078466376)
            assertGWPKgCO2Eq(transport, 1.890720419923)
            assertGWPKgCO2Eq(use, 1.8873543172718)
            assertGWPKgCO2Eq(endOfLife, 1.891157292304)
            assertThat(
                total.amount.value
            ).isCloseTo(
                manufacturing.amount.value
                + transport.amount.value
                + use.amount.value
                + endOfLife.amount.value,
                Percentage.withPercentage(1e-3)
            )
        }
    }

    private fun assertGWPKgCO2Eq(actual: QuantityValue<BasicNumber>, amount: Double) {
        assertThat(actual.amount.value).isCloseTo(amount, Percentage.withPercentage(1e-3))
        assertThat(actual.unit).isEqualTo(
            UnitValue<BasicNumber>(
                UnitSymbol.of("kg CO2-Eq"),
                1.0,
                Dimension.of("global warming potential (GWP100)")
            )
        )
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

            val total = vm.total(Indicator.GWP)
            val manufacturing = vm.manufacturing(Indicator.GWP)
            val transport = vm.transport(Indicator.GWP)
            val use = vm.use(Indicator.GWP)
            val endOfLife = vm.endOfLife(Indicator.GWP)

            assertGWPKgCO2Eq(total, 2.458826278)
            assertGWPKgCO2Eq(manufacturing, 0.63425488402)
            assertGWPKgCO2Eq(transport, 0.62911701870)
            assertGWPKgCO2Eq(use, 0.5662062951815)
            assertGWPKgCO2Eq(endOfLife, 0.62924808042)
            assertThat(
                total.amount.value
            ).isCloseTo(
                manufacturing.amount.value
                    + transport.amount.value
                    + use.amount.value
                    + endOfLife.amount.value,
                Percentage.withPercentage(1e-3)
            )
        }
    }
}
