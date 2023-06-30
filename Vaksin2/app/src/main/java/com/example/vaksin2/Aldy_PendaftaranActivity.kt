package com.example.vaksin2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vaksin2.data.Aldy_VaksinDatabase
import com.example.vaksin2.data.Pendaftaran.Aldy_AddPendaftaranFragment
import com.example.vaksin2.data.Pendaftaran.Aldy_Pendaftaran
import com.example.vaksin2.data.Pendaftaran.Aldy_PendaftaranAdapter
import com.example.vaksin2.databinding.ActivityAldyPendaftaranBinding
import kotlinx.coroutines.launch
import java.io.File

class Aldy_PendaftaranActivity : AppCompatActivity() {
    private var _binding : ActivityAldyPendaftaranBinding? = null
    private val binding get() = _binding!!

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION_TAG"

    lateinit var pendaftaranRecyclerView: RecyclerView

    lateinit var vaksinDB: Aldy_VaksinDatabase

    lateinit var pendaftarList: ArrayList<Aldy_Pendaftaran>

    private fun checkPermission() : Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        }
        else {
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED

        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
            }
            catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            }
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
        }
    }

    fun loadDataPendaftar() {
        var layoutManager = LinearLayoutManager(this)
        pendaftaranRecyclerView = binding.pendaftaranListView
        pendaftaranRecyclerView.layoutManager = layoutManager
        pendaftaranRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch{
            pendaftarList = vaksinDB.getPendaftaranDao().getAllPendaftaran() as ArrayList<Aldy_Pendaftaran>
            Log.e("list pendaftar", pendaftarList.toString())
            pendaftaranRecyclerView.adapter = Aldy_PendaftaranAdapter(pendaftarList)
        }
    }

    fun deletePendaftar(pendaftar : Aldy_Pendaftaran) {
        val builder = AlertDialog.Builder(this@Aldy_PendaftaranActivity)
        builder.setMessage("Apakah ${pendaftar.nama_pendaftar} ingin dihapus?")
            .setCancelable(false)
            .setPositiveButton("Yes") {dialog, id ->
                lifecycleScope.launch {
                    vaksinDB.getPendaftaranDao().deletePendaftaran(pendaftar)
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory("")
                    // konversi dari dir string ke dir file
                    val foto_delete = File(imagesDir, pendaftar.foto_pendaftar)

                    if(foto_delete.exists()) {
                        // foto ada di dalam galery
                        if(foto_delete.delete()) {
                            // foto di delete
                            val toastDelete =Toast.makeText(applicationContext,
                                "file edit foto delete", Toast.LENGTH_LONG)
                            toastDelete.show()
                        }
                    }
                    loadDataPendaftar()
                }
            }
            .setNegativeButton("No") {dialog, id ->
                dialog.dismiss()
                loadDataPendaftar()
            }
        loadDataPendaftar()
        val alert = builder.create()
        alert.show()
    }

    fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                lifecycleScope.launch {
                    pendaftarList = vaksinDB.getPendaftaranDao().getAllPendaftaran() as ArrayList<Aldy_Pendaftaran>
                    Log.e("position swiped", pendaftarList[position].toString())
                    Log.e("position swiped", pendaftarList.size.toString())
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory("")
                    // konversi dar dir string ke dir file
                    val foto_delete = File(imagesDir, pendaftarList[position].foto_pendaftar)

                    if(foto_delete.exists()) {
                        // foto ada di dalam galery
                        if(foto_delete.delete()) {
                            // foto di delete
                            val toastDelete = Toast.makeText(applicationContext,
                                "file edit foto delete", Toast.LENGTH_LONG)
                            toastDelete.show()
                        }
                    }
                    deletePendaftar(pendaftarList[position])
                }
            }
        }).attachToRecyclerView(pendaftaranRecyclerView)
    }

    fun searchDataPendaftar(keyword : String) {
        var layoutManager = LinearLayoutManager(this)
        pendaftaranRecyclerView = binding.pendaftaranListView
        pendaftaranRecyclerView.layoutManager = layoutManager
        pendaftaranRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            pendaftarList = vaksinDB.getPendaftaranDao().searchPendaftaran(keyword) as ArrayList<Aldy_Pendaftaran>
            Log.e("list pendaftar", pendaftarList.toString())
            pendaftaranRecyclerView.adapter = Aldy_PendaftaranAdapter(pendaftarList)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAldyPendaftaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!checkPermission()) {
            requestPermission()
        }

        vaksinDB = Aldy_VaksinDatabase(this@Aldy_PendaftaranActivity)

        loadDataPendaftar()

        binding.btnAddPendaftar.setOnClickListener {
            Aldy_AddPendaftaranFragment().show(supportFragmentManager, "newPendaftarTag")
        }

        swipeDelete()

        binding.txtSearchPendaftar.addTextChangedListener {
            val keyword : String = "%${binding.txtSearchPendaftar.text.toString()}%"
            if (keyword.count() > 2) {
                searchDataPendaftar(keyword)
            }
            else {
                loadDataPendaftar()
            }
        }
    }
}