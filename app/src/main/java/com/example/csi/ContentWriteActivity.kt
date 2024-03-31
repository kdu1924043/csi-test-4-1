package com.example.csi

import ContentModel
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.csi.databinding.ActivityContentWriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ContentWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContentWriteBinding
    private val database = FirebaseDatabase.getInstance().reference.child("content")
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.writeBtn.setOnClickListener {
            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val time = getTime()
            val key = database.push().key ?: ""
            if (key.isNotEmpty()) {
                val userId = currentUser?.uid ?: "" // 현재 로그인한 사용자의 아이디 가져오기
                val contentModel = ContentModel(title, content, time, key, userId) // ContentModel에 사용자 아이디 추가
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

    // 시간을 구하는 함수
    private fun getTime(): String {
        val currentDateTime = Calendar.getInstance().time
        return SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(currentDateTime)
    }
}