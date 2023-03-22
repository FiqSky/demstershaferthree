package com.example.demstershaferthree

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class SymptomActivity : AppCompatActivity(), GejalaAdapter.OnGejalaSelectedListener {
    private lateinit var gejalaAdapter: GejalaAdapter
    private lateinit var gejalaList: MutableList<Gejala>
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom)

        // set up RecyclerView
        val recyclerViewGejala: RecyclerView = findViewById(R.id.recyclerViewGejala)
        recyclerViewGejala.layoutManager = LinearLayoutManager(this)
        gejalaList = mutableListOf()
        gejalaAdapter = GejalaAdapter(gejalaList, this)
        recyclerViewGejala.adapter = gejalaAdapter

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

    override fun onGejalaSelected(kodeGejala: String, isChecked: Boolean) {
        for (gejala in gejalaList) {
            if (gejala.kodeGejala == kodeGejala) {
                gejala.isSelected = isChecked
                break
            }
        }
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


