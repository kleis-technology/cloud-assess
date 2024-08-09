package org.cloud_assess.dto

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*

sealed interface ParameterValueDto

class ParameterValueDtoDeserializer : JsonDeserializer<ParameterValueDto>() {
    override fun handledType(): Class<ParameterValueDto> {
        return ParameterValueDto::class.java
    }

    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): ParameterValueDto {
        val node = parser?.codec?.readTree<JsonNode>(parser)
            ?: throw IllegalArgumentException("invalid parameter value dto")
        return when {
            node.isTextual -> PVStr(node.asText())
            node.isObject -> {
                val fieldAmountName = "amount"
                val fieldUnitName = "unit"
                if (!node.has(fieldAmountName)) {
                    throw IllegalArgumentException("missing field '${fieldAmountName}'")
                }
                val fieldAmount = node.get(fieldAmountName)
                if (!fieldAmount.isNumber) {
                    throw IllegalArgumentException("field '${fieldAmountName}' invalid value")
                }
                val amount = fieldAmount.asDouble()

                if (!node.has(fieldUnitName)) {
                    throw IllegalArgumentException("missing field '${fieldUnitName}'")
                }
                val fieldUnit = node.get("unit")
                if (!fieldUnit.isTextual) {
                    throw IllegalArgumentException("field '${fieldUnitName}' invalid value")
                }
                val unit = fieldUnit.asText()
                PVNum(amount, unit)
            }
            else -> throw IllegalArgumentException("invalid parameter value dto")
        }
    }
}

class ParameterValueDtoSerializer : JsonSerializer<ParameterValueDto>() {
    override fun handledType(): Class<ParameterValueDto> {
        return ParameterValueDto::class.java
    }

    override fun serialize(dto: ParameterValueDto?, generator: JsonGenerator, provider: SerializerProvider?) {
        return when(dto) {
            is PVNum -> {
                generator.writeStartObject()
                generator.writeObjectField("amount", dto.amount)
                generator.writeObjectField("unit", dto.unit)
                generator.writeEndObject()
            }
            is PVStr -> generator.writeString(dto.value)
            null -> generator.writeNull()
        }
    }
}

data class PVStr(val value: String): ParameterValueDto
data class PVNum(val amount: Double, val unit: String): ParameterValueDto
