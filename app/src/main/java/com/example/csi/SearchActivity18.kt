package com.example.csi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class SearchActivity18 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var searchEditText: EditText
    private lateinit var itemList: MutableList<Item>
    private lateinit var filteredList: MutableList<Item>

    private lateinit var btnBelow1500: Button
    private lateinit var btn1500To3000: Button
    private lateinit var btnAbove5000: Button

    // Firebase Storage
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnBelow1500 = findViewById(R.id.btnBelow1500)
        btn1500To3000 = findViewById(R.id.btn1500To3000)
        btnAbove5000 = findViewById(R.id.btnAbove5000)

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().reference.child("ice2.json")

        itemList = mutableListOf()
        filteredList = mutableListOf()
        adapter = ItemAdapter(this, filteredList)
        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}
        })

        btnBelow1500.setOnClickListener { filterByPriceRange("0", "1500") }
        btn1500To3000.setOnClickListener { filterByPriceRange("1500", "4999") }
        btnAbove5000.setOnClickListener { filterByPriceRange("5000", "99999999") } // 임의의 큰 값 설정

        // Load data from Firebase Storage
        loadJSONFromFirebaseStorage()
    }

    private fun loadJSONFromFirebaseStorage() {
        val localFile = createTempFile("ice2", "json")
        storageReference.getFile(localFile).addOnSuccessListener {
            val json = localFile.readText()
            parseJSON(json)
        }.addOnFailureListener { exception ->
            // Handle any errors
            exception.printStackTrace()
        }
    }

    private fun parseJSON(json: String) {
        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val item = Item(
                jsonObject.getInt("no"),
                jsonObject.getString("name"),
                jsonObject.getString("price"),
                jsonObject.getString("photo")
            )
            itemList.add(item)
        }

        filteredList.addAll(itemList)
        adapter.notifyDataSetChanged()
    }

    private fun filter(text: String) {
        filteredList.clear()
        if (text.isEmpty()) {
            filteredList.addAll(itemList)
        } else {
            for (item in itemList) {
                if (item.name.contains(text, ignoreCase = true)) {
                    filteredList.add(item)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun filterByPriceRange(minPrice: String, maxPrice: String) {
        filteredList.clear()
        val min = minPrice.replace(",", "").toIntOrNull() ?: return
        val max = maxPrice.replace(",", "").toIntOrNull() ?: return

        for (item in itemList) {
            val itemPrice = item.price.replace(",", "").toIntOrNull()
            if (itemPrice != null && itemPrice in min..max) {
                filteredList.add(item)
            }
        }
        adapter.notifyDataSetChanged()
    }
}
