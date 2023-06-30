package com.example.vaksin2.data.Pendaftaran

import androidx.room.*

@Dao
interface Vale_PendaftaranDao {
    @Query("SELECT * FROM pendaftar WHERE nama_pendaftar LIKE :namaPendaftar")
    suspend fun searchPendaftaran(namaPendaftar: String) : List<Vale_Pendaftaran>

    @Insert
    suspend fun addPendaftaran(pendaftaran: Vale_Pendaftaran)

    @Update(entity = Vale_Pendaftaran::class)
    suspend fun updatePendaftaran(pendaftaran: Vale_Pendaftaran)

    @Delete
    suspend fun deletePendaftaran(pendaftaran: Vale_Pendaftaran)

    @Query("SELECT * FROM pendaftar")
    suspend fun getAllPendaftaran(): List<Vale_Pendaftaran>


}