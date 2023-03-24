package com.example.demstershaferthree

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ResultActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        tvResult = findViewById(R.id.tv_result)

        val hasilDiagnosis = intent.getStringExtra("hasil_diagnosis")
        tvResult.text = hasilDiagnosis
    }
}
