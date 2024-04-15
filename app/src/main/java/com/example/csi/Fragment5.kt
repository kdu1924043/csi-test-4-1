package com.example.csi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class Fragment5 : Fragment(), View.OnClickListener {
    private lateinit var rootView: View
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var button6: Button
    private lateinit var button7: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment5, container, false)

        // 버튼을 XML에서 찾아 변수에 할당
        button1 = rootView.findViewById(R.id.button1)
        button2 = rootView.findViewById(R.id.button2)
        button3 = rootView.findViewById(R.id.button3)
        button4 = rootView.findViewById(R.id.button4)
        button5 = rootView.findViewById(R.id.button5)
        button6 = rootView.findViewById(R.id.button6)
        button7 = rootView.findViewById(R.id.button7)

        // 각 버튼에 클릭 리스너 설정
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
        button6.setOnClickListener(this)
        button7.setOnClickListener(this)

        return rootView
    }

    override fun onClick(v: View?) {
        // 클릭된 버튼에 따라 해당하는 액티비티를 시작
        when (v?.id) {
            R.id.button1 -> {
                startActivity(Intent(activity, SearchActivity::class.java))
            }
            R.id.button2 -> {
                startActivity(Intent(activity, SearchActivity1::class.java))
            }
            R.id.button3 -> {
                startActivity(Intent(activity, SearchActivity2::class.java))
            }
            R.id.button4 -> {
                startActivity(Intent(activity, SearchActivity3::class.java))
            }
            R.id.button5 -> {
                startActivity(Intent(activity, SearchActivity4::class.java))
            }
            R.id.button6 -> {
                startActivity(Intent(activity, SearchActivity5::class.java))
            }
            R.id.button7 -> {
                startActivity(Intent(activity, SearchActivity6::class.java))
            }
            // 나머지 버튼들에 대한 처리 추가
        }
    }
}
