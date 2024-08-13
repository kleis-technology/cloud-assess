package org.cloud_assess.model

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations.toDouble

class ResourceTrace(
    private val id: String,
    private val meta: Map<String, String>,
    private val rawTrace: EvaluationTrace<BasicNumber>,
    private val contributionAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val defaultMaxDepth: Int,
    private val entryPointRef: String = "__main__",
    private val skipFirstStage: Boolean = true,
) {
    fun getRequestId(): String = id
    fun isEmpty(): Boolean = rawTrace.getNumberOfProcesses() == 0
    fun isNotEmpty(): Boolean = rawTrace.getNumberOfProcesses() > 0
    fun getMeta(): Map<String, String> = meta
    fun getDefaultMaxDepth(): Int = defaultMaxDepth

    fun getElements(
        maxDepth: Int = defaultMaxDepth,
    ): List<ResourceTraceElement> {
        return getAllElements()
            .filter {
                maxDepth < 0 || (it.depth in 0..<maxDepth)
            }
    }

    private fun getAllElements(): List<ResourceTraceElement> {
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
                    depth = if (skipFirstStage) depth - 1 else depth,
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
