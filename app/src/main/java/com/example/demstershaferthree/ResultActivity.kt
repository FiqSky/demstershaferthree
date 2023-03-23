package com.example.demstershaferthree

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ResultActivity : AppCompatActivity() {

    private lateinit var penyakitList: List<Pair<Penyakit, Double>>
    private lateinit var recyclerView: RecyclerView
    private lateinit var penyakitAdapter: PenyakitAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Mendapatkan data dari Intent
        penyakitList = intent.getSerializableExtra("penyakitList") as List<Pair<Penyakit, Double>>

        // Inisialisasi RecyclerView dan adapter
        recyclerView = findViewById(R.id.recyclerViewPenyakit)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        penyakitAdapter = PenyakitAdapter(penyakitList)
        recyclerView.adapter = penyakitAdapter

        // Set toolbar sebagai action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Hasil Diagnosa"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
