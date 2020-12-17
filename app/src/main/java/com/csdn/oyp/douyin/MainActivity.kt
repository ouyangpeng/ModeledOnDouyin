package com.csdn.oyp.douyin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    var btnToRecyclerview: Button? = null
    var btnToViewpager: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnToRecyclerview = findViewById(R.id.btn_to_recyclerview)
        btnToViewpager = findViewById(R.id.btn_to_viewpager)

        btnToRecyclerview?.setOnClickListener { skipToRecyclerViewShowActivity() }
        btnToViewpager?.setOnClickListener { skipToViewPagerShowActivity() }
    }

    private fun skipToViewPagerShowActivity() {
        val intent = Intent(this@MainActivity, ViewPagerShowActivity::class.java)
        startActivity(intent)
    }

    private fun skipToRecyclerViewShowActivity() {
        val intent = Intent(this@MainActivity, RecyclerViewShowActivity::class.java)
        startActivity(intent)
    }
}