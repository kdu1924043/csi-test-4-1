package com.example.csi

import ImageListAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MygiActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var listView: ListView
    private lateinit var adapter: ImageListAdapter
    private val items = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_gi)

        listView = findViewById(R.id.listView)
        adapter = ImageListAdapter(this, items)
        listView.adapter = adapter

        // Firestore에서 기프티콘 이미지 불러오기
        loadGifticons()

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedItem = items[position]
            showDeleteConfirmationDialog(selectedItem)
        }
    }

    private fun loadGifticons() {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email
            firestore.collection("barcodes").whereEqualTo("user", userEmail)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val uri = document.getString("uri")
                        uri?.let { items.add(uri) }
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("MygiActivity", "Error getting gifticon images: ${exception.message}")
                }
        }
    }

    private fun showDeleteConfirmationDialog(item: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("기프티콘 삭제")
            .setMessage("기프티콘을 삭제하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->
                deleteGifticon(item)
            }
            .setNegativeButton("취소") { dialog, which ->
                // Do nothing
            }
            .show()
    }

    private fun deleteGifticon(item: String) {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email
            firestore.collection("barcodes").whereEqualTo("uri", item)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        firestore.collection("barcodes").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                items.remove(item)
                                adapter.notifyDataSetChanged()
                                deleteImageFromStorage(item)
                                Log.d("MygiActivity", "Gifticon deleted from Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.e("MygiActivity", "Error deleting gifticon from Firestore: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MygiActivity", "Error finding gifticon to delete: ${e.message}")
                }
        }
    }

    private fun deleteImageFromStorage(uri: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(uri)
        storageRef.delete()
            .addOnSuccessListener {
                Log.d("MygiActivity", "Image deleted from Storage")
            }
            .addOnFailureListener { e ->
                Log.e("MygiActivity", "Error deleting image from Storage: ${e.message}")
            }
    }
}
