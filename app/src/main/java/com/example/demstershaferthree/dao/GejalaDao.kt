package com.example.demstershaferthree.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demstershaferthree.model.Gejala

@Dao
interface GejalaDao {
    @Query("SELECT * FROM gejala")
    fun getAllGejala(): List<Gejala>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGejala(gejala: Gejala)

    @Query("DELETE FROM gejala")
    fun deleteAllGejala()

    @Query("SELECT * FROM gejala WHERE gejala = :gejala")
    fun getGejalaByNama(gejala: String): Gejala?
}