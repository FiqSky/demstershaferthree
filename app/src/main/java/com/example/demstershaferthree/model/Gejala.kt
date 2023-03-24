package com.example.demstershaferthree.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gejala")
data class Gejala(
    @PrimaryKey
    @ColumnInfo(name = "kode_gejala") val kodeGejala: String,
    @ColumnInfo(name = "gejala") val gejala: String,
    @ColumnInfo(name = "bobot") val bobot: Double
)
