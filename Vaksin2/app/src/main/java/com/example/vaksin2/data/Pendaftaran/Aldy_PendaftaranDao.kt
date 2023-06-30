package com.example.vaksin2.data.Pendaftaran

import androidx.room.*

@Dao
interface Aldy_PendaftaranDao {
    @Query("SELECT * FROM pendaftar WHERE nama_pendaftar LIKE :namaPendaftar")
    suspend fun searchPendaftaran(namaPendaftar: String) : List<Aldy_Pendaftaran>

    @Insert
    suspend fun addPendaftaran(pendaftaran: Aldy_Pendaftaran)

    @Update(entity = Aldy_Pendaftaran::class)
    suspend fun updatePendaftaran(pendaftaran: Aldy_Pendaftaran)

    @Delete
    suspend fun deletePendaftaran(pendaftaran: Aldy_Pendaftaran)

    @Query("SELECT * FROM pendaftar")
    suspend fun getAllPendaftaran(): List<Aldy_Pendaftaran>


}