package org.cloud_assess.ops

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.datasource.DataSourceOperationsBase
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

class OverrideDataSourceOperations(
    private val override: Map<String, Sequence<ERecord<BasicNumber>>>,
    private val defaultSourceOps: DataSourceOperations<BasicNumber>,
) : DataSourceOperations<BasicNumber> {
    private val overriddenLocations = override.keys
    private val sourceOps = DataSourceOperationsBase(BasicOperations) {
        override[it.location] ?: emptySequence()
    }

    override fun getFirst(source: DataSourceValue<BasicNumber>): ERecord<BasicNumber> {
        if (overriddenLocations.contains(source.location)) {
            return sourceOps.getFirst(source)
        }
        return defaultSourceOps.getFirst(source)
    }

    override fun readAll(source: DataSourceValue<BasicNumber>): Sequence<ERecord<BasicNumber>> {
        if (overriddenLocations.contains(source.location)) {
            return sourceOps.readAll(source)
        }
        return defaultSourceOps.readAll(source)
    }

    override fun sumProduct(source: DataSourceValue<BasicNumber>, columns: List<String>): DataExpression<BasicNumber> {
        if (overriddenLocations.contains(source.location)) {
            return sourceOps.sumProduct(source, columns)
        }
        return defaultSourceOps.sumProduct(source, columns)
    }
}
