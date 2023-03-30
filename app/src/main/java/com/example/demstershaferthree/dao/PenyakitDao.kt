package com.example.demstershaferthree.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demstershaferthree.model.Penyakit

@Dao
interface PenyakitDao {
    @Query("SELECT * FROM penyakit")
    fun getAllPenyakit(): List<Penyakit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPenyakit(penyakit: Penyakit)

    @Query("DELETE FROM penyakit")
    fun deleteAllPenyakit()

    @Query("SELECT * FROM penyakit WHERE kode_penyakit = :kodePenyakit")
    fun getPenyakitByKode(kodePenyakit: String): Penyakit?
}