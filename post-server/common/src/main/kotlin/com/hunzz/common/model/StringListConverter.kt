package com.hunzz.common.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return attribute?.joinToString(separator = "|||") ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        return dbData?.split("|||")?.filter { it.isNotBlank() } ?: emptyList()
    }
}