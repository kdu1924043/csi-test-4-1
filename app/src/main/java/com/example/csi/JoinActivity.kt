package com.example.csi
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.csi.JoinActivity.ValidationResult.*
import com.example.csi.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class JoinActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        // Initialize Firebase Auth
        auth = Firebase.auth

        val joinBtn = findViewById<Button>(R.id.joinBtn)
        joinBtn.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.email).text.toString()
            val pwd = findViewById<TextInputEditText>(R.id.pwd).text.toString()
            val confirmPwd = findViewById<TextInputEditText>(R.id.confirmPwd).text.toString()

            // 비밀번호 검증
            val validationResult = validatePassword(pwd, confirmPwd)
            if (validationResult == VALID) {
                auth.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                            Log.d(TAG, "createUserWithEmail:success")
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        }
                    }
            } else {
                // 비밀번호가 조건을 충족하지 않음
                // 사용자에게 알림을 줌
                when (validationResult) {
                    PASSWORDS_DO_NOT_MATCH -> {
                        Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                    INVALID_CHARACTER -> {
                        Toast.makeText(this, "비밀번호에 영대소문자 숫자 특수문자를 모두 포함해주세요.", Toast.LENGTH_SHORT).show()
                    }

                    VALID -> TODO()
                }
            }
        }
    }

    // 비밀번호가 조건을 충족하는지 확인하는 함수
    private fun validatePassword(pwd: String, confirmPwd: String): ValidationResult {
        // 비밀번호가 확인 비밀번호와 일치하는지 확인
        if (pwd != confirmPwd) {
            return PASSWORDS_DO_NOT_MATCH
        }

        // 비밀번호가 비어 있는지 확인
        if (pwd.isEmpty()) {
            return INVALID_CHARACTER
        }

        // 비밀번호가 8자 이상이고, 영어 대소문자, 숫자, 특수문자가 모두 포함되는지 확인
        val regex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$")
        if (!regex.matches(pwd)) {
            return INVALID_CHARACTER
        }

        return VALID
    }

    // 비밀번호 검증 결과를 열거형으로 정의
    private enum class ValidationResult {
        VALID,
        PASSWORDS_DO_NOT_MATCH,
        INVALID_CHARACTER
    }
}
