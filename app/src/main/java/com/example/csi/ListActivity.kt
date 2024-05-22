package com.example.csi

import ContentAdapter
import ContentModel
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csi.databinding.ActivityListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var contentAdapter: ContentAdapter
    private val contentList = mutableListOf<ContentModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contentAdapter = ContentAdapter(contentList) { contentModel ->
            onContentItemClick(contentModel)
        }

        binding.recyclerview.adapter = contentAdapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        getFBContentData()
    }

    private fun getFBContentData() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contentList.clear()

                for (data in snapshot.children) {
                    val item = data.getValue(ContentModel::class.java)
                    if (item != null && item.userEmail == currentUserEmail) {
                        contentList.add(item)
                    }
                }
                contentList.reverse()
                contentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        }
        FirebaseDatabase.getInstance().getReference("content").addValueEventListener(postListener)
    }

    private fun onContentItemClick(contentModel: ContentModel) {
        val intent = Intent(this, ContentDetailActivity::class.java)
        intent.putExtra("contentModel", contentModel)
        startActivity(intent)
    }
}
