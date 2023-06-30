package com.example.vaksin2.data.Pendaftaran

import androidx.room.*
import java.io.Serializable

@Entity(tableName = "pendaftar")

data class Aldy_Pendaftaran(
    @ColumnInfo(name = "nik_pendaftar") var nik_pendaftar: Int = 0,
    @ColumnInfo(name = "nama_pendaftar") var nama_pendaftar: String = "",
    @ColumnInfo(name = "umur_pendaftar") var umur_pendaftar: Int = 0,
    @ColumnInfo(name = "jenis_kelamin_pendaftar") var jenis_kelamin_pendaftar: Int = 0,
    @ColumnInfo(name = "penyakit_bawaan_pendaftar") var penyakit_bawaan_pendaftar: String = "",
    @ColumnInfo(name = "foto_pendaftar") var foto_pendaftar: String = "",
) : Serializable {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}
