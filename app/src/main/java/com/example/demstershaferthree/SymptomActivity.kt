package com.example.demstershaferthree

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.demstershaferthree.db.AppDatabase
import com.example.demstershaferthree.helper.DiagnosisListener
import com.example.demstershaferthree.model.Gejala
import com.example.demstershaferthree.model.Penyakit
import com.google.firebase.database.*
import kotlinx.coroutines.*

class SymptomActivity : AppCompatActivity(), DiagnosisListener, CoroutineScope by MainScope() {

    private lateinit var db: AppDatabase
    private lateinit var listView: ListView
    private var isCalculating: Boolean = false
    private lateinit var databaseRef: DatabaseReference
    private lateinit var gejalaList: MutableList<String>
    private lateinit var diagnosisCalculator: DiagnosisCalculator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom)

        listView = findViewById(R.id.list_gejala)
        databaseRef = FirebaseDatabase.getInstance().reference
        gejalaList = mutableListOf()
        diagnosisCalculator = DiagnosisCalculator(applicationContext, databaseRef)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "diagnosis_db").build()

        // Menetapkan instance 'db' ke 'diagnosisCalculator'
        diagnosisCalculator.db = db

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, gejalaList)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.adapter = adapter

        fetchGejalaData()
        fetchPenyakitData()
    }
    private fun fetchGejalaData() {
        databaseRef.child("GEJALA").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (gejalaSnapshot in snapshot.children) {
                    val kodeGejala = gejalaSnapshot.child("kode_gejala").getValue(String::class.java)
                    val gejala = gejalaSnapshot.child("gejala").getValue(String::class.java)
                    val bobot = gejalaSnapshot.child("bobot").getValue(Double::class.java)

                    if (kodeGejala != null && gejala != null && bobot != null) {
                        val gejalaObj = Gejala(kodeGejala, gejala, bobot)
                        gejalaList.add(gejala)
                        launch(Dispatchers.IO) {
                            db.gejalaDao().insertGejala(gejalaObj)
                        }
                    }
                }
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun fetchPenyakitData() {
        databaseRef.child("PENYAKIT").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (gejalaSnapshot in snapshot.children) {
                    val kodePenyakit = gejalaSnapshot.child("kode_penyakit").getValue(String::class.java)
                    val namaPenyakit = gejalaSnapshot.child("nama_penyakit").getValue(String::class.java)
                    val daftarGejala = gejalaSnapshot.child("daftar_gejala").getValue(object : GenericTypeIndicator<List<String>>() {})

                    if (kodePenyakit != null && namaPenyakit != null && daftarGejala != null) {
                        val penyakitObj = Penyakit(kodePenyakit, namaPenyakit, daftarGejala)
                        launch(Dispatchers.IO) {
                            db.penyakitDao().insertPenyakit(penyakitObj)
                        }
                    }
                }
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }


    fun onButtonClick(view: View) {
        // Jika perhitungan sedang berjalan, jangan mulai perhitungan baru
        if (isCalculating) return

        val selectedGejala = mutableListOf<String>()
        val checkedItems = listView.checkedItemPositions
        for (i in 0 until checkedItems.size()) {
            val position = checkedItems.keyAt(i)
            if (checkedItems.valueAt(i)) {
                selectedGejala.add(gejalaList[position])
                Log.d(TAG, "onButtonClick: $selectedGejala")
            }
        }

        // Tandai bahwa perhitungan sedang berjalan
        isCalculating = true

        launch(Dispatchers.Main) {
            diagnosisCalculator.calculate(selectedGejala, this@SymptomActivity)
        }
    }
    override fun onDiagnosisComplete(namaPenyakit: String, beliefValue: Double) {
        // Menandai bahwa perhitungan telah selesai
        isCalculating = false

        // Menampilkan hasil diagnosis
        launch {
            val hasilDiagnosisList = mutableListOf<String>()

            hasilDiagnosisList.add("$namaPenyakit (${beliefValue})")
            Log.d(TAG, "onDiagnosisComplete1: $hasilDiagnosisList")

            val intent = Intent(this@SymptomActivity, ResultActivity::class.java)
            intent.putExtra("hasil_diagnosis", hasilDiagnosisList.joinToString(separator = "\n"))
            startActivity(intent)
            Log.d(TAG, "onDiagnosisComplete2: $hasilDiagnosisList")
        }
    }

    override fun onDiagnosisError(errorMessage: String?) {
        // Menampilkan error jika terjadi kesalahan pada perhitungan
        launch {
            Toast.makeText(this@SymptomActivity, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // cancel coroutine when activity is destroyed
    }
}