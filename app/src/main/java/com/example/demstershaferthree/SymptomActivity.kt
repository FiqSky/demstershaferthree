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
import com.google.firebase.database.*
import kotlinx.coroutines.*

class SymptomActivity : AppCompatActivity(), DiagnosisListener, CoroutineScope by MainScope() {

    private lateinit var listView: ListView
    private lateinit var databaseRef: DatabaseReference
    private lateinit var gejalaList: MutableList<String>
    private lateinit var diagnosisCalculator: DiagnosisCalculator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom)

        listView = findViewById(R.id.list_gejala)
        databaseRef = FirebaseDatabase.getInstance().reference.child("GEJALA")
        gejalaList = mutableListOf()
        diagnosisCalculator = DiagnosisCalculator(databaseRef)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, gejalaList)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.adapter = adapter

        fetchGejalaData()
    }

    private fun fetchGejalaData() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (gejalaSnapshot in snapshot.children) {
                    val gejala = gejalaSnapshot.child("gejala").getValue(String::class.java)
                    gejala?.let { gejalaList.add(it) }
                }
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    fun onButtonClick(view: View) {
        val selectedGejala = mutableListOf<String>()
        val checkedItems = listView.checkedItemPositions
        for (i in 0 until checkedItems.size()) {
            val position = checkedItems.keyAt(i)
            if (checkedItems.valueAt(i)) {
                selectedGejala.add(gejalaList[position])
                Log.d(TAG, "onButtonClick: $selectedGejala")
            }
        }

        launch(Dispatchers.Main) {
            diagnosisCalculator.calculate(selectedGejala, this@SymptomActivity)
        }
    }


    override fun onDiagnosisComplete(daftarBeliefAkhir: Map<String, Double>) {
        // Menampilkan hasil diagnosis
        launch {
            var hasilDiagnosis = ""
            for (kode_penyakit in daftarBeliefAkhir.keys) {
                if (daftarBeliefAkhir[kode_penyakit]!! >= 0.5) {
                    hasilDiagnosis += "${diagnosisCalculator.getNamaPenyakit(kode_penyakit)} (${daftarBeliefAkhir[kode_penyakit]})\n"
                    Log.d(TAG, "onDiagnosisComplete1: $hasilDiagnosis")
                }
            }

            val intent = Intent(this@SymptomActivity, ResultActivity::class.java)
            intent.putExtra("hasil_diagnosis", hasilDiagnosis)
            startActivity(intent)
            Log.d(TAG, "onDiagnosisComplete2: $hasilDiagnosis")
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