package com.example.demstershaferthree.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.demstershaferthree.dao.GejalaDao
import com.example.demstershaferthree.dao.PenyakitDao
import com.example.demstershaferthree.model.Converters
import com.example.demstershaferthree.model.Gejala
import com.example.demstershaferthree.model.Penyakit

@Database(entities = [Gejala::class, Penyakit::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gejalaDao(): GejalaDao
    abstract fun penyakitDao(): PenyakitDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diagnosis-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

