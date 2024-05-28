package com.example.csi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.csi.R

class TradeFragment : Fragment(R.layout.fragment7) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<ImageView>(R.id.imagebutton1)
        val imageView2 = view.findViewById<ImageView>(R.id.imagebutton2)
// ImageView 클릭 리스너 설정
        imageView.setOnClickListener {
            // Intent 생성 및 BuyActivity로 이동
            val intent = Intent(context, BuyActivity::class.java)
            startActivity(intent)
        }
        imageView2.setOnClickListener {
            // Intent 생성 및 BuyActivity로 이동
            val intent = Intent(context, Buy2Activity::class.java)
            startActivity(intent)
        }
    }
}
