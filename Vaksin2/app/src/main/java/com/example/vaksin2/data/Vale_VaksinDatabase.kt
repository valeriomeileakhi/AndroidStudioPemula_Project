package com.example.vaksin2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vaksin2.data.Pendaftaran.Vale_Pendaftaran
import com.example.vaksin2.data.Pendaftaran.Vale_PendaftaranDao

@Database(entities = [Vale_Pendaftaran::class], version = 1, exportSchema = false)

abstract class Vale_VaksinDatabase : RoomDatabase(){

    abstract fun getPendaftaranDao(): Vale_PendaftaranDao

    companion object {
        @Volatile
        private var instance: Vale_VaksinDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder (
            context.applicationContext,
            Vale_VaksinDatabase::class.java,
            "vaksin-db"
        ).build()
    }
}