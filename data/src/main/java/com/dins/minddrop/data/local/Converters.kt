package com.dins.minddrop.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTagsList(tags: List<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toTagsList(data: String): List<String> =
        if (data.isEmpty()) emptyList() else data.split(",")
}
