package org.cloud_assess.service

import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.cloud_assess.dto.*

class Helper(
    defaultDataSourceOperations: DefaultDataSourceOperations<BasicNumber>,
    symbolTable: SymbolTable<BasicNumber>,
) {
    private val dataReducer = DataExpressionReducer(
        dataRegister = symbolTable.data,
        dataSourceRegister = DataSourceRegister.empty(),
        ops = BasicOperations,
        sourceOps = defaultDataSourceOperations,
    )

    fun localEval(expression: DataExpression<BasicNumber>): DataValue<BasicNumber> {
        val data = dataReducer.reduce(expression)
        return with(ToValue(BasicOperations)) {
            data.toValue()
        }
    }

    fun QuantityTimeDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            TimeUnitsDto.hour -> EQuantityScale(BasicNumber(this.amount), EDataRef("hour"))
        }
    }

    fun QuantityTimeDto.toLcaac(): String {
        return when (this.unit) {
            TimeUnitsDto.hour -> "${this.amount} hour"
        }
    }

    fun String.toDataExpression(): DataExpression<BasicNumber> = EStringLiteral(this)

    fun QuantityMemoryDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            MemoryUnitsDto.gB -> EQuantityScale(BasicNumber(this.amount), EDataRef("GB"))
            MemoryUnitsDto.tB -> EQuantityScale(BasicNumber(this.amount), EDataRef("TB"))
        }
    }

    fun QuantityVCPUDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            VCPUUnitsDto.vCPU -> EQuantityScale(BasicNumber(this.amount), EDataRef("vCPU"))
        }
    }

    fun QuantityDimensionlessDto.toDataExpression(): DataExpression<BasicNumber> {
        return when (this.unit) {
            DimensionlessUnitsDto.u -> EQuantityScale(BasicNumber(this.amount), EDataRef("u"))
        }
    }
}
