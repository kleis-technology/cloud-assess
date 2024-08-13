package org.cloud_assess.model

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations.toDouble

class ResourceTrace(
    private val id: String,
    private val rawTrace: EvaluationTrace<BasicNumber>,
    private val contributionAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val entryPointRef: String = "__main__",
) {
    fun getRequestId(): String = id
    fun isEmpty(): Boolean = rawTrace.getNumberOfProcesses() == 0
    fun isNotEmpty(): Boolean = rawTrace.getNumberOfProcesses() > 0

    fun getElements(): List<ResourceTraceElement> {
        val observablePorts = contributionAnalysis.getObservablePorts()
            .getElements()
            .sortedWith(rawTrace.getComparator())
        val controllablePorts = contributionAnalysis.getControllablePorts().getElements()
            .sortedBy { it.getShortName() }
        val entryPoint = rawTrace.getEntryPoint()
        val products = entryPoint.products
        return products.flatMap { demandedProduct ->
            val allocation = (demandedProduct.allocation?.amount?.toDouble()
                ?: 1.0) * (demandedProduct.allocation?.unit?.scale ?: 1.0)
            observablePorts.map { row ->
                val supply = contributionAnalysis.supplyOf(row)
                val impacts = Indicator.entries.associateWith { indicator ->
                    controllablePorts.firstOrNull { it.getShortName() == indicator.name }
                        ?.let { col ->
                            contributionAnalysis.getPortContribution(row, col)
                        }
                }
                val depth = rawTrace.getDepthOf(row)
                    ?: throw IllegalStateException("resource trace: missing depth for observable port $row")
                ResourceTraceElement(
                    depth = depth,
                    allocation = allocation,
                    demand = demandedProduct,
                    target = row,
                    supply = supply,
                    impacts = impacts,
                )
            }
        }
    }
}

class ResourceTraceElement(
    val depth: Int,
    val allocation: Double,
    val demand: TechnoExchangeValue<BasicNumber>,
    val target: MatrixColumnIndex<BasicNumber>,
    val supply: QuantityValue<BasicNumber>,
    val impacts: Map<Indicator, QuantityValue<BasicNumber>?>,
)
