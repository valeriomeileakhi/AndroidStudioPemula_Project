package com.example.vaksin2.data.Pendaftaran

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.vaksin2.Aldy_PendaftaranActivity
import com.example.vaksin2.R
import com.example.vaksin2.data.Aldy_VaksinDatabase
import com.example.vaksin2.databinding.FragmentAldyAddPendaftaranBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class Aldy_AddPendaftaranFragment : BottomSheetDialogFragment() {
    private var _binding : FragmentAldyAddPendaftaranBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 100
    private var dataGambar : Bitmap? = null
    private var saved_image_url : String = ""

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAldyAddPendaftaranBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun addPendaftar() {
        val nik_pendaftar = binding.TxtNik.text.toString().toInt()
        val nama_pendaftar = binding.TxtNama.text.toString()
        val umur_pendaftar = binding.TxtUmur.text.toString().toInt()
        var penyakit_bawaan_pendaftar = binding.TxtPenyakitBawaan.text.toString()
        val jenis_kelamin_pendaftar = binding.RadioJenisKelamin.checkedRadioButtonId

        Log.e("nik", nik_pendaftar.toString())
        Log.e("nama", nama_pendaftar.toString())
        Log.e("umur", umur_pendaftar.toString())
        Log.e("penyakit", penyakit_bawaan_pendaftar.toString())
        Log.e("jenis klmn", jenis_kelamin_pendaftar.toString())
        Log.e("foto", saved_image_url.toString())

        lifecycleScope.launch {
            val pendaftaran = Aldy_Pendaftaran(nik_pendaftar, nama_pendaftar, umur_pendaftar, jenis_kelamin_pendaftar, penyakit_bawaan_pendaftar, saved_image_url)
            Aldy_VaksinDatabase(requireContext()).getPendaftaranDao().addPendaftaran(pendaftaran)
        }
        dismiss()
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
            activity?.contentResolver?.also { resolver ->
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
            val permission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(
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
            saved_image_url = image_save_uri
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.activity?.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        (activity as Aldy_PendaftaranActivity?)?.loadDataPendaftar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.BtnImgPendaftar.setOnClickListener {
            openCamera()
        }

        binding.BtnAddPendaftar.setOnClickListener {
            if(saved_image_url != "") {
                addPendaftar()
            }
        }
    }

}