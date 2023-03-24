package com.example.demstershaferthree.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "penyakit")
data class Penyakit(
    @PrimaryKey
    @ColumnInfo(name = "kode_penyakit") val kodePenyakit: String,
    @ColumnInfo(name = "nama_penyakit") val namaPenyakit: String,
    @ColumnInfo(name = "daftar_gejala") val daftarGejala: List<String>
){
    // Konversi list menjadi string sebelum menyimpan ke database
    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return Gson().toJson(list)
    }

    // Konversi string menjadi list saat membaca data dari database
    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
