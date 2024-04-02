package com.example.csi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import ContentModel
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.csi.databinding.ActivityContentWriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ContentWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContentWriteBinding
    private val database = FirebaseDatabase.getInstance().reference.child("content")
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val storageRef = FirebaseStorage.getInstance().reference.child("images")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chooseImageButton.setOnClickListener {
            openGalleryForImage()
        }

        binding.writeBtn.setOnClickListener {

            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val time = getTime()
            val key = database.push().key ?: ""
            if (key.isNotEmpty()) {
                val userId = currentUser?.uid ?: ""
                val contentModel = ContentModel(title, content, time, key, userId)
                database.child(key).setValue(contentModel)
                    .addOnSuccessListener {
                        Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "게시글 입력 실패", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "게시글 입력 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val imageUri: Uri = it.data!!
                // 이미지 URI를 Toast로 보여줍니다.
                Toast.makeText(this, "이미지 URI: $imageUri", Toast.LENGTH_SHORT).show()

                // 이미지뷰에 선택한 이미지를 설정합니다.
                binding.imageView.setImageURI(imageUri)

                val imageName = UUID.randomUUID().toString()
                val imageRef = storageRef.child(imageName)
                imageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val title = binding.titleArea.text.toString()
                            val content = binding.contentArea.text.toString()
                            val time = getTime()
                            val key = database.push().key ?: ""
                            if (key.isNotEmpty()) {
                                val userId = currentUser?.uid ?: ""
                                val contentModel = ContentModel(title, content, time, key, userId, imageUrl)
                                database.child(key).setValue(contentModel)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "게시글 입력 실패", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "게시글 입력 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    private fun getTime(): String {
        val currentDateTime = Calendar.getInstance().time
        return SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(currentDateTime)
    }
}