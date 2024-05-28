package com.example.csi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ItemAdapter(private val context: Context, private val itemList: List<Item>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noTextView: TextView = itemView.findViewById(R.id.noTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.noTextView.text = currentItem.no.toString()
        holder.nameTextView.text = currentItem.name
        holder.priceTextView.text = currentItem.price.toString()

        Glide.with(holder.itemView.context)
            .load(currentItem.photo)
            .into(holder.photoImageView)

        holder.photoImageView.setOnClickListener {
            val intent = Intent(context, ReviewActivity::class.java).apply {
                putExtra("item", currentItem)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = itemList.size
}
