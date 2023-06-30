package com.example.vaksin2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vaksin2.data.Pendaftaran.Aldy_Pendaftaran
import com.example.vaksin2.data.Pendaftaran.Aldy_PendaftaranDao

@Database(entities = [Aldy_Pendaftaran::class], version = 1, exportSchema = false)

abstract class Aldy_VaksinDatabase : RoomDatabase(){

    abstract fun getPendaftaranDao(): Aldy_PendaftaranDao

    companion object {
        @Volatile
        private var instance: Aldy_VaksinDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder (
            context.applicationContext,
            Aldy_VaksinDatabase::class.java,
            "vaksin-db"
        ).build()
    }
}