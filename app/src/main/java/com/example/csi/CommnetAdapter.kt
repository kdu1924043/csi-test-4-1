package com.example.csi

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class CommentAdapter(
    private val commentsList: MutableList<CommentModel>,
    private val context: Context,
    private val databaseReference: DatabaseReference,
    private val contentId: String,
    private val contentAuthorEmail: String
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.textViewComment)
        val userEmail: TextView = itemView.findViewById(R.id.textViewUserEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentsList[position]
        holder.commentText.text = comment.commentText
        holder.userEmail.text = comment.userEmail

        holder.itemView.setOnClickListener {
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
            if (currentUserEmail == comment.userEmail || currentUserEmail == contentAuthorEmail) {
                showDeleteConfirmationDialog(comment.id, position)
            } else {
                Toast.makeText(context, "댓글을 삭제할 수 있는 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    private fun showDeleteConfirmationDialog(commentId: String, position: Int) {
        AlertDialog.Builder(context)
            .setMessage("댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteComment(commentId, position)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteComment(commentId: String, position: Int) {
        databaseReference.child(contentId).child("comments").child(commentId).removeValue()
            .addOnSuccessListener {
                commentsList.removeAt(position) // 삭제 전에 목록에서 항목을 제거합니다.
                notifyItemRemoved(position) // 제거 후에 RecyclerView에 변경 사항을 알립니다.

                Toast.makeText(context, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
            }
    }

}
