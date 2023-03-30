package com.example.demstershaferthree

import androidx.room.TypeConverter
import org.json.JSONArray

class ListStringConverter {

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.let {
            val array = JSONArray(it)
            List<String>(array.length()) { i ->
                array.getString(i)
            }
        }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.let {
            val array = JSONArray(list)
            array.toString()
        }
    }
}
