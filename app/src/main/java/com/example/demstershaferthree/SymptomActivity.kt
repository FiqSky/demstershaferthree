package com.example.demstershaferthree

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SymptomActivity : AppCompatActivity(), DiagnosisListener, CoroutineScope by MainScope() {

    private lateinit var listView: ListView
    private lateinit var databaseRef: DatabaseReference
    private lateinit var gejalaList: MutableList<String>
    private lateinit var diagnosisCalculator: DiagnosisCalculator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom)

        // set up RecyclerView
        val recyclerViewGejala: RecyclerView = findViewById(R.id.recyclerViewGejala)
        recyclerViewGejala.layoutManager = LinearLayoutManager(this)
        gejalaList = mutableListOf()
        
        diagnosisCalculator = DiagnosisCalculator(databaseRef)

        // get Firebase database reference
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("GEJALA")

        // retrieve data from Firebase database
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // clear existing data
                gejalaList.clear()

                // add new data
                for (snapshot in dataSnapshot.children) {
                    val kodeGejala = snapshot.child("kode_gejala").value.toString()
                    val gejala = snapshot.child("gejala").value.toString()
                    val bobot = snapshot.child("bobot").value.toString().toDouble()
                    val gejalaObj = Gejala(kodeGejala, gejala, bobot, false)

                    gejalaList.add(gejalaObj)
                }

                // update RecyclerView with new data
                gejalaAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error
            }
        })

        // set up search button
        val searchButton: Button = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val selectedGejalaList = getSelectedGejalaList()

            if (selectedGejalaList.isNotEmpty()) {
                val penyakitList = mutableListOf<Penyakit>()

                // Retrieve data from Firebase database
                val databaseReference = FirebaseDatabase.getInstance().getReference("PENYAKIT")
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val kodePenyakit = snapshot.child("kode_penyakit").value.toString()
                            val namaPenyakit = snapshot.child("nama_penyakit").value.toString()
                            val daftarGejala = snapshot.child("daftar_gejala").value as List<String>

                            // Calculate plausibility
                            val plausibility = DempsterShaferCalculator.calculatePlausibility(selectedGejalaList, daftarGejala)

                            val penyakit = Penyakit(kodePenyakit, namaPenyakit, plausibility)
                            penyakitList.add(penyakit)
                        }

                        // Sort penyakitList by plausibility
                        val sortedPenyakitList = penyakitList.sortedByDescending { it.plausibility }

                        // Start ResultActivity with sortedPenyakitList as extra
                        val intent = Intent(this@SymptomActivity, ResultActivity::class.java)
                        intent.putExtra("penyakitList", sortedPenyakitList.toTypedArray())
                        startActivity(intent)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
            } else {
                Toast.makeText(this, "Pilih setidaknya satu gejala", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun onButtonClick(view: View) = launch {
        val selectedGejala = mutableListOf<String>()
        val checkedItems = listView.checkedItemPositions
        for (i in 0 until checkedItems.size()) {
            val position = checkedItems.keyAt(i)
            if (checkedItems.valueAt(i)) {
                selectedGejala.add(gejalaList[position])
                Log.d(TAG, "onButtonClick: $selectedGejala")
            }
        }

        diagnosisCalculator.calculate(selectedGejala, this@SymptomActivity)
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
            Toast.makeText(this@SymptomActivity, hasilDiagnosis, Toast.LENGTH_LONG).show()
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

    private fun getSelectedGejalaList(): List<String> {
        val selectedGejalaList = mutableListOf<String>()
        for (gejala in gejalaList) {
            if (gejala.isSelected) {
                selectedGejalaList.add(gejala.kodeGejala)
            }
        }
        return selectedGejalaList
    }
}


