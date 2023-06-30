package com.example.vaksin2.data.Pendaftaran

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.vaksin2.Aldy_PendaftaranActivity
import com.example.vaksin2.data.Aldy_VaksinDatabase
import com.example.vaksin2.databinding.ActivityAldyEditPendaftaranBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class Aldy_EditPendaftaranActivity : AppCompatActivity() {
    private var _binding: ActivityAldyEditPendaftaranBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 101
    private var dataGambar: Bitmap? = null
    private var old_foto_dir = ""
    private var new_foto_dir = ""

    private var id_pendaftar: Int = 0

    lateinit var pendaftarDB: Aldy_VaksinDatabase
    private val STORAGE_PERMISSION_CODE = 102

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

    fun saveMediaToStorage(bitmap: Bitmap): String {
        //Generate Nama File
        val filename = "${System.currentTimeMillis()}.jpg"
        //Output Stream
        var fos: OutputStream? = null
        var image_save = ""

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->
                //Content resolver will process the contentValues
                val contentValues = ContentValues().apply {
                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                // Inserting the contenValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputsteam with the Uri that we got
                fos = imageUri?.let {resolver.openOutputStream(it) }
                // Store file dir to image_save
                image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
            }
        }
        else {
            //These for devices running on android < Q
            val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE)
            }

            val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imageDir, filename)
            fos = FileOutputStream(image)

            image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
        }

        fos?.use {bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)}
        return image_save
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAM && resultCode == AppCompatActivity.RESULT_OK) {
            dataGambar = data?.extras?.get("data") as Bitmap
            val image_save_uri: String = saveMediaToStorage(dataGambar!!)
            binding.BtnImgPendaftar.setImageBitmap(dataGambar)
            new_foto_dir = image_save_uri
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    private fun editPendaftar() {
        val nik_pendaftar = binding.TxtEditNik.text.toString().toInt()
        val nama_pendaftar = binding.TxtEditNama.text.toString()
        val umur_pendaftar = binding.TxtEditUmur.text.toString().toInt()
        val jenis_kelamin_pendaftar = binding.RadioEditJenisKelamin.checkedRadioButtonId
        var penyakit_bawaan_pendaftar = binding.TxtEditPenyakitBawaan.text.toString()
        var foto_final_dir : String = old_foto_dir

        if (new_foto_dir != "") {
            foto_final_dir = new_foto_dir
            val imagesDir =
                Environment.getExternalStoragePublicDirectory("")
            // Foto dir string di konversi ke foto dir file
            val old_foto_delete = File(imagesDir, old_foto_dir)

            if(old_foto_delete.exists()) {
                // Foto lama ada
                if(old_foto_delete.delete()) {
                    // foto lama di hapus
                    Log.e("foto final", foto_final_dir)
                }
            }
        }

        lifecycleScope.launch {
            val pendaftar = Aldy_Pendaftaran(nik_pendaftar, nama_pendaftar, umur_pendaftar, jenis_kelamin_pendaftar, penyakit_bawaan_pendaftar, foto_final_dir)
            pendaftar.id = id_pendaftar
            pendaftarDB.getPendaftaranDao().updatePendaftaran(pendaftar)
        }
        val intentPendaftar = Intent(this, Aldy_PendaftaranActivity::class.java)
        startActivity(intentPendaftar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAldyEditPendaftaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pendaftarDB = Aldy_VaksinDatabase(this@Aldy_EditPendaftaranActivity)

        val intent = intent
        binding.TxtEditNik.setText(intent.getStringExtra("nik_pendaftar").toString())
        binding.TxtEditNama.setText(intent.getStringExtra("nama_pendaftar").toString())
        binding.TxtEditUmur.setText(intent.getStringExtra("umur_pendaftar").toString())
        binding.RadioEditJenisKelamin.check(intent.getStringExtra("jenis_kelamin_pendaftar").toString().toInt())
        binding.TxtEditPenyakitBawaan.setText(intent.getStringExtra("penyakit_bawaan_pendaftar").toString())

        id_pendaftar = intent.getStringExtra("id").toString().toInt()

        old_foto_dir = intent.getStringExtra("foto_pendaftar").toString()
        val imgFile = File("${Environment.getExternalStorageDirectory()}/${old_foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        binding.BtnImgPendaftar.setImageBitmap(myBitmap)

        if (!checkPermission()) {
            requestPermission()
        }

        binding.BtnImgPendaftar.setOnClickListener {
            openCamera()
        }

        binding.BtnEditPendaftar.setOnClickListener {
            editPendaftar()
        }
    }
}