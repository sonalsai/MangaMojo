package com.mangamojo.app.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Stores `List<String>` columns (alt titles, authors, tags, …) as JSON text. */
class Converters {
    private val json = Json

    @TypeConverter
    fun fromStringList(list: List<String>?): String =
        json.encodeToString(ListSerializer(String.serializer()), list ?: emptyList())

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList()
        else json.decodeFromString(ListSerializer(String.serializer()), value)
}
