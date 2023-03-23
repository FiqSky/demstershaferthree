package com.example.demstershaferthree

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DiagnosisCalculator(private val databaseRef: DatabaseReference) {

    @OptIn(DelicateCoroutinesApi::class)
    fun calculate(selectedGejala: List<String>, callback: DiagnosisListener) {
        // Inisialisasi variabel
        val daftarPenyakit = mutableListOf<String>()
        val daftarBelief = mutableMapOf<String, Double>()

        // Looping untuk mendapatkan daftar penyakit dan faktor keyakinannya
        databaseRef.child("PENYAKIT").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                GlobalScope.launch {
                    snapshot.children.forEach { penyakitSnapshot ->
                        val kode_penyakit = penyakitSnapshot.child("kode_penyakit").getValue(String::class.java)
                        val daftar_gejala = penyakitSnapshot.child("daftar_gejala").getValue(object : GenericTypeIndicator<List<String>>() {})
                        var belief = 1.0
                        selectedGejala.forEach { gejala ->
                            val bobot = getBobotGejala(gejala)
                            if (daftar_gejala?.contains(getKodeGejala(gejala)) == true) {
                                belief *= bobot
                            } else {
                                belief *= (1 - bobot)
                            }
                        }
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
        val task = databaseRef.child("GEJALA").orderByChild("gejala").equalTo(namaGejala).get()
        val snapshot = task.await()
        var bobot = 0.0
        if (snapshot.exists()) {
            for (gejalaSnapshot in snapshot.children) {
                bobot = gejalaSnapshot.child("bobot").getValue(Double::class.java) ?: 0.0
            }
        }
        bobot
    }

    // Fungsi untuk mendapatkan kode gejala dari Firebase Realtime Database
    private suspend fun getKodeGejala(namaGejala: String): String {
        return withContext(Dispatchers.IO) {
            val dataSnapshot = databaseRef.child("GEJALA").orderByChild("gejala").equalTo(namaGejala).get().await()
            var kode = ""
            if (dataSnapshot.exists()) {
                for (gejalaSnapshot in dataSnapshot.children) {
                    kode = gejalaSnapshot.child("kode_gejala").getValue(String::class.java) ?: ""
                }
            }
            kode
        }
    }

    // Fungsi untuk mendapatkan nama penyakit dari Firebase Realtime Database
    suspend fun getNamaPenyakit(kodePenyakit: String): String = withContext(Dispatchers.IO) {
        val deferredNamaPenyakit = CompletableDeferred<String>()
        val query = databaseRef.child("PENYAKIT").orderByChild("kode_penyakit").equalTo(kodePenyakit)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (penyakitSnapshot in snapshot.children) {
                        val nama = penyakitSnapshot.child("nama_penyakit").getValue(String::class.java) ?: ""
                        deferredNamaPenyakit.complete(nama)
                    }
                } else {
                    deferredNamaPenyakit.completeExceptionally(Exception("Penyakit tidak ditemukan"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                deferredNamaPenyakit.completeExceptionally(error.toException())
            }
        })
        deferredNamaPenyakit.await()
    }

}
