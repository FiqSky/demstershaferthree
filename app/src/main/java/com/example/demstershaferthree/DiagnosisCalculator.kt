package com.example.demstershaferthree

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.*

class DiagnosisCalculator(private val databaseRef: DatabaseReference) {

    fun calculate(selectedGejala: List<String>, callback: SymptomActivity) {
        // Inisialisasi variabel
        val daftarPenyakit = mutableListOf<String>()
        val daftarBelief = mutableMapOf<String, Double>()

        // Looping untuk mendapatkan daftar penyakit dan faktor keyakinannya
        databaseRef.child("PENYAKIT").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (penyakitSnapshot in snapshot.children) {
                    val kode_penyakit =
                        penyakitSnapshot.child("kode_penyakit").getValue(String::class.java)
                    val daftar_gejala = penyakitSnapshot.child("daftar_gejala")
                        .getValue(object : GenericTypeIndicator<List<String>>() {})
                    var belief = 1.0
                    for (gejala in selectedGejala) {
                        if (daftar_gejala?.contains(getKodeGejala(gejala)) == true) {
                            belief *= getBobotGejala(gejala)
                        } else {
                            belief *= (1 - getBobotGejala(gejala))
                        }
                    }
                    daftarPenyakit.add(kode_penyakit!!)
                    daftarBelief[kode_penyakit] = belief
                }

                // Menghitung faktor keyakinan total
                var beliefTotal = 0.0
                for (belief in daftarBelief.values) {
                    beliefTotal += belief
                }

                // Menghitung faktor keyakinan akhir
                val daftarBeliefAkhir = mutableMapOf<String, Double>()
                for (kode_penyakit in daftarPenyakit) {
                    val beliefAkhir =
                        daftarBelief[kode_penyakit]!! / (beliefTotal - daftarBelief[kode_penyakit]!!)
                    daftarBeliefAkhir[kode_penyakit] = beliefAkhir
                }

                // Memanggil callback listener dengan hasil diagnosis
                callback.onDiagnosisComplete(daftarBeliefAkhir)
                Log.d(TAG, "onDaftarBeliefAkhir: $daftarBeliefAkhir")
            }

            override fun onCancelled(error: DatabaseError) {
                // Memanggil callback listener dengan error
                callback.onDiagnosisError(error.message)
            }
        })
    }

    // Fungsi untuk mendapatkan bobot gejala dari Firebase Realtime Database
    private fun getBobotGejala(namaGejala: String): Double {
        var bobot = 0.0
        databaseRef.child("GEJALA").orderByChild("gejala").equalTo(namaGejala)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (gejalaSnapshot in snapshot.children) {
                            bobot =
                                gejalaSnapshot.child("bobot").getValue(Double::class.java) ?: 0.0
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        return bobot
    }

    // Fungsi untuk mendapatkan kode gejala dari Firebase Realtime Database
    private fun getKodeGejala(namaGejala: String): String {
        var kode = ""
        databaseRef.child("GEJALA").orderByChild("gejala").equalTo(namaGejala)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (gejalaSnapshot in snapshot.children) {
                            kode = gejalaSnapshot.child("kode_gejala").getValue(String::class.java)
                                ?: ""
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        return kode
    }

    // Fungsi untuk mendapatkan nama penyakit dari Firebase Realtime Database
    fun getNamaPenyakit(kodePenyakit: String): String {
        var nama = ""
        databaseRef.child("PENYAKIT").orderByChild("kode_penyakit").equalTo(kodePenyakit)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (penyakitSnapshot in snapshot.children) {
                            nama = penyakitSnapshot.child("nama_penyakit").getValue(String::class.java) ?: ""
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        return nama
    }

}
