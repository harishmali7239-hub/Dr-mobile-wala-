package com.example.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return list?.joinToString(separator = "|||") ?: ""
    }

    @TypeConverter
    fun fromStringToList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split("|||").filter { it.isNotBlank() }
    }
}
