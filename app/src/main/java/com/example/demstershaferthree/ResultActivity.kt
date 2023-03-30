package com.example.demstershaferthree

<<<<<<< HEAD
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ResultActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView

=======
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
>>>>>>> 634f6df57bcbed8f45a50f0660556e065ff87d34
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

<<<<<<< HEAD
        tvResult = findViewById(R.id.tv_result)

        val hasilDiagnosis = intent.getStringExtra("hasil_diagnosis")
        tvResult.text = hasilDiagnosis
    }
}
=======
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

>>>>>>> 634f6df57bcbed8f45a50f0660556e065ff87d34
