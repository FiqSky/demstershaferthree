package com.example.demstershaferthree

import android.content.ContentValues.TAG
import android.util.Log
import com.example.demstershaferthree.db.AppDatabase
import com.example.demstershaferthree.helper.DiagnosisListener
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiagnosisCalculator(private val databaseRef: DatabaseReference) : CoroutineScope by MainScope() {
    lateinit var db: AppDatabase

    fun calculate(selectedGejala: List<String>, callback: DiagnosisListener) {
        Log.d(TAG, "calculateselectedGejala: $selectedGejala")
        val daftarPenyakit = mutableListOf<String>()

        databaseRef.child("PENYAKIT").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch {
                    snapshot.children.forEach { penyakitSnapshot ->
                        val kode_penyakit = penyakitSnapshot.child("kode_penyakit").getValue(String::class.java)
                        Log.d(TAG, "onDataChange: $kode_penyakit")
                        daftarPenyakit.add(kode_penyakit!!)
                    }

                    // Langkah 2: Buat mass functions awal untuk setiap penyakit
                    val dempsterShafer = DempsterShafer()
                    val gejalaMassFunctionsList = mutableListOf<Map<String, Double>>()
                    selectedGejala.forEach { gejala ->
                        val bobot = getBobotGejala(gejala)
                        val gejalaMassFunctions = getGejalaMassFunctions(gejala, bobot)
                        gejalaMassFunctionsList.add(gejalaMassFunctions)
                    }

                    // Langkah 3: Gunakan aturan kombinasi Dempster-Shafer untuk menggabungkan mass functions
                    var combinedMassFunctions = dempsterShafer.initializeMassFunctions(daftarPenyakit)
                    gejalaMassFunctionsList.forEach { gejalaMassFunctions ->
                        combinedMassFunctions = dempsterShafer.combineMassFunctions(combinedMassFunctions, gejalaMassFunctions)
                    }

                    // Memanggil callback listener dengan hasil diagnosis
                    withContext(Dispatchers.Main) {
                        callback.onDiagnosisComplete(combinedMassFunctions)
                    }
                    Log.d(TAG, "onDaftarBeliefAkhir: $combinedMassFunctions")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Memanggil callback listener dengan error
                callback.onDiagnosisError(error.message)
            }
        })
    }

    // Fungsi untuk mendapatkan mass functions gejala
    private fun getGejalaMassFunctions(gejala: String, bobot: Double): Map<String, Double> {
        val massFunctions = mutableMapOf<String, Double>()
        massFunctions[gejala] = bobot
        massFunctions[""] = 1 - bobot // Himpunan kosong
        return massFunctions
    }

    // Fungsi untuk mendapatkan bobot gejala dari Firebase Realtime Database
    private suspend fun getBobotGejala(namaGejala: String): Double = withContext(Dispatchers.IO) {
        val gejala = db.gejalaDao().getGejalaByNama(namaGejala)
        gejala?.bobot ?: 0.0
    }

    // Fungsi untuk mendapatkan kode gejala dari Firebase Realtime Database
    private suspend fun getKodeGejala(namaGejala: String): String {
        return withContext(Dispatchers.IO) {
            val gejala = db.gejalaDao().getGejalaByNama(namaGejala)
            gejala?.kodeGejala ?: ""
        }
    }

    // Fungsi untuk mendapatkan nama penyakit dari Firebase Realtime Database
    suspend fun getNamaPenyakit(kodePenyakit: String): String = withContext(Dispatchers.IO) {
        val penyakit = db.penyakitDao().getPenyakitByKode(kodePenyakit)
        penyakit?.namaPenyakit ?: ""
    }
}
