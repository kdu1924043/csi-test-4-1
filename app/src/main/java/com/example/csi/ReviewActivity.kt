package com.example.csi

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReviewActivity : AppCompatActivity() {

    private lateinit var reviewEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var itemNameTextView: TextView
    private lateinit var reviewContentTextView: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        reviewEditText = findViewById(R.id.reviewEditText)
        submitButton = findViewById(R.id.submitButton)
        itemNameTextView = findViewById(R.id.itemNameTextView)
        reviewContentTextView = findViewById(R.id.reviewContentTextView)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val item = intent.getParcelableExtra<Item>("item")
        item?.let {
            itemNameTextView.text = it.name
        }

        // 이전에 작성된 리뷰를 불러와서 텍스트뷰에 표시
        val itemId = item?.no ?: -1
        val savedReview = sharedPreferences.getString("review_$itemId", "")
        if (savedReview?.isNotEmpty() == true) {
            reviewContentTextView.text = savedReview
            reviewContentTextView.visibility = View.VISIBLE
        }

        submitButton.setOnClickListener {
            val reviewText = reviewEditText.text.toString()
            val newReview = "리뷰 내용: $reviewText\n" + savedReview // 이전 리뷰와 새로운 리뷰를 합침
            saveReview(itemId, newReview)
            reviewContentTextView.text = newReview
            reviewContentTextView.visibility = View.VISIBLE
        }
    }

    private fun saveReview(itemId: Int, review: String) {
        val editor = sharedPreferences.edit()
        editor.putString("review_$itemId", review)
        editor.apply()
    }
}
