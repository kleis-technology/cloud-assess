package org.cloud_assess.model

import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class ProductMatcher(
    private val name: String,
    private val process: String,
    private val labels: Map<String, String> = emptyMap(),
    private val arguments: Map<String, String> = emptyMap(),
) {
    override fun toString(): String {
        return "$name from $process$arguments$labels"
    }

    fun addLabel(name: String, value: String): ProductMatcher = this.copy(
        labels = this.labels.plus(name to value)
    )

    fun addArgument(name: String, value: String): ProductMatcher = this.copy(
        arguments = this.arguments.plus(name to value)
    )

    fun matches(product: ProductValue<BasicNumber>): Boolean {
        val matchName = name == product.name
        val matchProcessName = process == product.fromProcessRef?.name
        val actualLabels = product.fromProcessRef
            ?.matchLabels
            ?.mapValues { it.value.s }
            ?.entries ?: emptySet()
        val sameLabels = actualLabels.containsAll(labels.entries)
            && labels.entries.containsAll(actualLabels)
        val actualArguments = product.fromProcessRef
            ?.arguments
            ?.filterValues { it is StringValue<BasicNumber> }
            ?.mapValues { (it.value as StringValue).s }
            ?.entries ?: emptySet()
        val containsArguments = actualArguments.containsAll(arguments.entries)
        return matchName && matchProcessName && sameLabels && containsArguments
    }
}
