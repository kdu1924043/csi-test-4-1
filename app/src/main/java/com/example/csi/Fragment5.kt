package com.example.csi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class Fragment5 : Fragment(), View.OnClickListener {
    private lateinit var rootView: View
    private lateinit var imageButton1: ImageView
    private lateinit var imageButton2: ImageView
    private lateinit var imageButton3: ImageView
    private lateinit var imageButton4: ImageView
    private lateinit var imageButton5: ImageView
    private lateinit var imageButton6: ImageView
    private lateinit var imageButton7: ImageView
    private lateinit var imageButton8: ImageView
    private lateinit var imageButton9: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment5, container, false)

        // 이미지 뷰를 XML에서 찾아 변수에 할당
        imageButton1 = rootView.findViewById(R.id.imagebutton1)
        imageButton2 = rootView.findViewById(R.id.imagebutton2)
        imageButton3 = rootView.findViewById(R.id.imagebutton3)
        imageButton4 = rootView.findViewById(R.id.imagebutton4)
        imageButton5 = rootView.findViewById(R.id.imagebutton5)
        imageButton6 = rootView.findViewById(R.id.imagebutton6)
        imageButton7 = rootView.findViewById(R.id.imagebutton7)
        imageButton8 = rootView.findViewById(R.id.imagebutton8)
        imageButton9 = rootView.findViewById(R.id.imagebutton9)

        // 각 이미지 뷰에 클릭 리스너 설정
        imageButton1.setOnClickListener(this)
        imageButton2.setOnClickListener(this)
        imageButton3.setOnClickListener(this)
        imageButton4.setOnClickListener(this)
        imageButton5.setOnClickListener(this)
        imageButton6.setOnClickListener(this)
        imageButton7.setOnClickListener(this)
        imageButton8.setOnClickListener(this)
        imageButton9.setOnClickListener(this)

        return rootView
    }

    override fun onClick(v: View?) {
        // 클릭된 이미지 뷰에 따라 해당하는 액티비티를 시작
        when (v?.id) {
            R.id.imagebutton1 -> {
                startActivity(Intent(activity, SearchActivity::class.java))
            }
            R.id.imagebutton2 -> {
                startActivity(Intent(activity, SearchActivity1::class.java))
            }
            R.id.imagebutton3 -> {
                startActivity(Intent(activity, SearchActivity2::class.java))
            }
            R.id.imagebutton4 -> {
                startActivity(Intent(activity, SearchActivity3::class.java))
            }
            R.id.imagebutton5 -> {
                startActivity(Intent(activity, SearchActivity4::class.java))
            }
            R.id.imagebutton6 -> {
                startActivity(Intent(activity, SearchActivity5::class.java))
            }
            R.id.imagebutton7 -> {
                startActivity(Intent(activity, SearchActivity6::class.java))
            }
            R.id.imagebutton8 -> {
                startActivity(Intent(activity, SearchActivity7::class.java))
            }
            R.id.imagebutton9 -> {
                startActivity(Intent(activity, SearchActivity8::class.java))
            }
            // 나머지 이미지 뷰들에 대한 처리 추가
        }
    }
}
