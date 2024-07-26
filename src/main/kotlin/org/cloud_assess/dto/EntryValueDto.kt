package org.cloud_assess.dto

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*

sealed interface EntryValueDto

class EntryValueDtoDeserializer : JsonDeserializer<EntryValueDto>() {
    override fun handledType(): Class<EntryValueDto> {
        return EntryValueDto::class.java
    }

    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): EntryValueDto {
        val node = parser?.codec?.readTree<JsonNode>(parser)
            ?: throw IllegalArgumentException("invalid datasource entry value")
        return when {
            node.isNumber -> VNum(node.asDouble())
            node.isTextual -> VStr(node.asText())
            else -> throw IllegalArgumentException("invalid datasource entry value")
        }
    }
}

class EntryValueDtoSerializer : JsonSerializer<EntryValueDto>() {
    override fun handledType(): Class<EntryValueDto> {
        return EntryValueDto::class.java
    }

    override fun serialize(dto: EntryValueDto?, generator: JsonGenerator, provider: SerializerProvider) {
        return when(dto) {
            is VNum -> generator.writeNumber(dto.value)
            is VStr -> generator.writeString(dto.value)
            null -> throw IllegalStateException("missing field value")
        }
    }
}

data class VStr(val value: String): EntryValueDto
data class VNum(val value: Double): EntryValueDto
