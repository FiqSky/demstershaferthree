package com.example.demstershaferthree

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_diagnose = findViewById<Button>(R.id.btn_diagnose)
        btn_diagnose.setOnClickListener {
            val intent = Intent (this, SymptomActivity::class.java)
            startActivity(intent)
        }
    }
}