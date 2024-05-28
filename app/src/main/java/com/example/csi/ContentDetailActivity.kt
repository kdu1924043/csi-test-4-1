package com.example.csi

import ContentModel
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csi.databinding.ActivityContentDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ContentDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContentDetailBinding
    private lateinit var contentModel: ContentModel
    private lateinit var database: DatabaseReference
    private lateinit var currentUserEmail: String
    private var contentLikesRef: DatabaseReference? = null
    private var userLikedRef: DatabaseReference? = null
    private var userLiked = false
    private lateinit var commentAdapter: CommentAdapter
    private val commentsList = mutableListOf<CommentModel>()
    private var commentsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contentModel = intent.getParcelableExtra("contentModel") ?: return
        val imageUrl = contentModel.imageUrl
        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(binding.imageView)
        } else {
            binding.imageView.setImageDrawable(null)
        }

        binding.textViewTitle.text = contentModel.title
        binding.textViewContent.text = contentModel.content
        database = FirebaseDatabase.getInstance().reference.child("content")
        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val username = currentUserEmail.substringBefore('@')

        contentLikesRef = database.child(contentModel.id).child("likes")
        userLikedRef = database.child(contentModel.id).child("likedBy").child(username)

        userLikedRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userLiked = snapshot.exists()
                updateLikeButtonState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ContentDetailActivity, "좋아요 상태를 불러오는 중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        binding.imagelikeButton.setOnClickListener {
            toggleLike()
        }

        binding.imagedeleteButton.setOnClickListener {
            deleteContent(contentModel)
        }

        // 댓글 관련 초기화 코드
        commentAdapter = CommentAdapter(commentsList, this, database, contentModel.id, contentModel.userEmail)

        binding.recyclerViewComments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewComments.adapter = commentAdapter

        binding.buttonSubmitComment.setOnClickListener {
            submitComment()
        }

        loadComments()
    }

    private fun toggleLike() {
        if (userLiked) {
            contentLikesRef?.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val likes = (mutableData.getValue(Int::class.java) ?: 0) - 1
                    mutableData.value = likes
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (databaseError != null) {
                        Toast.makeText(this@ContentDetailActivity, "좋아요 업데이트 중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        userLikedRef?.removeValue()
                        userLiked = false
                        updateLikeButtonState()
                        updateLikesCount(-1)
                    }
                }
            })
        } else {
            contentLikesRef?.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val likes = (mutableData.getValue(Int::class.java) ?: 0) + 1
                    mutableData.value = likes
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (databaseError != null) {
                        Toast.makeText(this@ContentDetailActivity, "좋아요 업데이트 중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        userLikedRef?.setValue(true)
                        userLiked = true
                        updateLikeButtonState()
                        updateLikesCount(1)
                    }
                }
            })
        }
    }

    private fun updateLikesCount(countChange: Int) {
        contentModel.likes += countChange
        database.child(contentModel.id).child("likes").setValue(contentModel.likes)
    }

    private fun updateLikeButtonState() {
        if (userLiked) {
            binding.imagelikeButton.setBackgroundResource(R.drawable.baseline_favorite_24)
        } else {
            binding.imagelikeButton.setBackgroundResource(R.drawable.baseline_favorite_border_24)
        }
    }

    private fun deleteContent(contentModel: ContentModel) {
        val contentId = contentModel.id
        val authorEmail = contentModel.userEmail
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (currentUserEmail != null && currentUserEmail == authorEmail) {
            database.child(contentId).removeValue()
                .addOnSuccessListener {
                    onBackPressed()
                    Toast.makeText(this, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "게시물을 삭제할 수 있는 권한이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitComment() {
        val commentText = binding.editTextComment.text.toString().trim()
        if (commentText.isNotEmpty()) {
            val commentId = database.push().key ?: ""
            val comment = CommentModel(
                id = commentId,
                userEmail = currentUserEmail,
                commentText = commentText
            )
            binding.buttonSubmitComment.isEnabled = false
            database.child(contentModel.id).child("comments").child(commentId).setValue(comment)
                .addOnSuccessListener {
                    if (!commentsList.contains(comment)) {
                        commentsList.add(comment)
                        commentAdapter.notifyItemInserted(commentsList.size - 1)
                    }
                    binding.editTextComment.text.clear()
                    Toast.makeText(this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "댓글 등록 실패", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    binding.buttonSubmitComment.isEnabled = true
                }
        }
    }

    private fun loadComments() {
        commentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentsList.clear()
                for (dataSnapshot in snapshot.children) {
                    val comment = dataSnapshot.getValue(CommentModel::class.java)
                    comment?.let { commentsList.add(it) }
                }
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ContentDetailActivity, "댓글을 불러오는 중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        database.child(contentModel.id).child("comments").addValueEventListener(commentsListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        commentsListener?.let { database.child(contentModel.id).child("comments").removeEventListener(it) }
    }
}
