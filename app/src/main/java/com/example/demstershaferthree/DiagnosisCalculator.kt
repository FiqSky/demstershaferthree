package com.example.demstershaferthree

import android.content.ContentValues.TAG
import android.util.Log
import com.example.demstershaferthree.db.AppDatabase
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DiagnosisCalculator(private val databaseRef: DatabaseReference) : CoroutineScope by MainScope() {
    private lateinit var db: AppDatabase
    fun calculate(selectedGejala: List<String>, callback: DiagnosisListener) {
        Log.d(TAG, "calculateselectedGejala: $selectedGejala")
        // Inisialisasi variabel
        val daftarPenyakit = mutableListOf<String>()
        val daftarBelief = mutableMapOf<String, Double>()

        // Looping untuk mendapatkan daftar penyakit dan faktor keyakinannya
        databaseRef.child("PENYAKIT").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch {
                    snapshot.children.forEach { penyakitSnapshot ->
                        val kode_penyakit = penyakitSnapshot.child("kode_penyakit").getValue(String::class.java)
                        Log.d(TAG, "onDataChange: $kode_penyakit")
                        val daftar_gejala = penyakitSnapshot.child("daftar_gejala").getValue(object : GenericTypeIndicator<List<String>>() {})
                        Log.d(TAG, "onDataChange: $daftar_gejala")
                        var belief = 1.0
                        selectedGejala.forEach { gejala ->
                            val bobot = getBobotGejala(gejala)
                            if (daftar_gejala?.contains(getKodeGejala(gejala)) == true) {
                                belief *= bobot
                            } else {
                                belief *= (1 - bobot)
                            }
                        }
                        Log.d(TAG, "onDataChange: $daftarPenyakit")
                        daftarPenyakit.add(kode_penyakit!!)
                        daftarBelief[kode_penyakit] = belief

                        // Check if all beliefs have been calculated
                        if (daftarBelief.size == daftarPenyakit.size) {
                            // Menghitung faktor keyakinan total
                            var beliefTotal = 0.0
                            daftarBelief.values.forEach { belief ->
                                beliefTotal += belief
                            }

                            // Menghitung faktor keyakinan akhir
                            val daftarBeliefAkhir = mutableMapOf<String, Double>()
                            daftarPenyakit.forEach { kode_penyakit ->
                                val beliefAkhir = daftarBelief[kode_penyakit]!! / (beliefTotal - daftarBelief[kode_penyakit]!!)
                                daftarBeliefAkhir[kode_penyakit] = beliefAkhir
                            }

                            // Memanggil callback listener dengan hasil diagnosis
                            withContext(Dispatchers.Main) {
                                callback.onDiagnosisComplete(daftarBeliefAkhir)
                            }
                            Log.d(TAG, "onDaftarBeliefAkhir: $daftarBeliefAkhir")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Memanggil callback listener dengan error
                callback.onDiagnosisError(error.message)
            }
        })
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
