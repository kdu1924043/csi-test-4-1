package com.example.csi

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReviewActivity : AppCompatActivity() {

    private lateinit var itemNameTextView: TextView
    private lateinit var itemPriceTextView: TextView
    private lateinit var itemImageView: ImageView
    private lateinit var reviewEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var reviewTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        itemNameTextView = findViewById(R.id.itemNameTextView)
        itemPriceTextView = findViewById(R.id.itemPriceTextView)
        itemImageView = findViewById(R.id.itemImageView)
        reviewEditText = findViewById(R.id.reviewEditText)
        submitButton = findViewById(R.id.submitButton)
        reviewTextView = findViewById(R.id.reviewTextView)

        val item = intent.getSerializableExtra("item") as? Item

        item?.let {
            itemNameTextView.text = it.name
            itemPriceTextView.text = it.price
            Glide.with(this).load(it.photo).into(itemImageView)
        }

        // Firebase Realtime Database의 "reviews" 노드에 대한 참조 가져오기
        database = FirebaseDatabase.getInstance().reference.child("reviews")
        auth = FirebaseAuth.getInstance() // FirebaseAuth 인스턴스 초기화

        submitButton.setOnClickListener {
            val reviewText = reviewEditText.text.toString()
            val currentUser = auth.currentUser // 현재 로그인한 사용자 가져오기
            if (item != null && reviewText.isNotEmpty() && currentUser != null) {
                val userEmail = currentUser.email // 현재 사용자의 이메일 가져오기
                val reviewData = mapOf(
                    "itemName" to item.name,
                    "reviewText" to reviewText,
                    "userEmail" to userEmail // 이메일 주소 추가
                )
                val reviewId = database.push().key // 리뷰에 대한 고유한 키 생성
                if (reviewId != null) {
                    database.child(reviewId).setValue(reviewData)
                        .addOnSuccessListener {
                            // 저장 성공 처리
                        }
                        .addOnFailureListener { exception ->
                            // 저장 실패 처리
                            exception.printStackTrace()
                        }
                }
                reviewEditText.text.clear()
            }
        }

        // Firebase Realtime Database에서 리뷰를 읽고 업데이트
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ReviewActivity", "onDataChange called")
                val reviews = mutableListOf<String>()
                for (reviewSnapshot in snapshot.children) {
                    val itemName = reviewSnapshot.child("itemName").getValue(String::class.java)
                    val reviewText = reviewSnapshot.child("reviewText").getValue(String::class.java)
                    val userEmail = reviewSnapshot.child("userEmail").getValue(String::class.java)
                    if (itemName != null && reviewText != null && itemName == item?.name) { // 현재 보고 있는 상품에 해당하는 리뷰만 추가
                        val review = "$userEmail:$reviewText"
                        reviews.add(review)
                    }
                }
                updateReviews(reviews)
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
            }
        })
    }

    private fun updateReviews(reviews: List<String>) {
        val reviewsText = reviews.joinToString("\n")
        reviewTextView.text = reviewsText
    }
}
