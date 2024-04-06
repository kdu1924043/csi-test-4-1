package com.example.csi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {
    private val Fragment_1 = 1
    private val Fragment_2 = 2
    private val Fragment_6 = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn1).setOnClickListener {
            fragmentView(Fragment_1)
        }

        findViewById<View>(R.id.btn2).setOnClickListener {
            fragmentView(Fragment_2)
        }

        findViewById<View>(R.id.btn3).setOnClickListener {
            fragmentView(Fragment_6)
        }

        fragmentView(Fragment_1)
    }

    private fun fragmentView(fragment: Int) {
        val transaction = supportFragmentManager.beginTransaction()

        when (fragment) {
            1 -> {
                val fragment1 = Fragment1()
                transaction.replace(R.id.fragment_container, fragment1)
            }
            2 -> {
                val fragment2 = Fragment2()
                transaction.replace(R.id.fragment_container, fragment2)
            }
            6 -> {
                val fragment6 = Fragment6()
                transaction.replace(R.id.fragment_container, fragment6)
            }
        }
        transaction.commit()
    }

}
