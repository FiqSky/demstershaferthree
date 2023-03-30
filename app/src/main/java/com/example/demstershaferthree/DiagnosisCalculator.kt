package com.example.demstershaferthree

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.demstershaferthree.db.AppDatabase
import com.example.demstershaferthree.helper.DiagnosisListener
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiagnosisCalculator(private val context: Context, private val databaseRef: DatabaseReference) : CoroutineScope by MainScope() {
    lateinit var db: AppDatabase
    fun calculate(selectedGejala: List<String>, callback: DiagnosisListener) {
        db = Room.databaseBuilder(context, AppDatabase::class.java, "diagnosis_db").build()
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
                        Log.d(TAG, "onDataChange - CheckBobot: $bobot")
                        val kodeGejala = getKodeGejala(gejala)
                        val gejalaMassFunctions = getGejalaMassFunctions(kodeGejala, bobot)
                        Log.d(TAG, "onDataChange - CheckGejalaMassFunctions: $gejalaMassFunctions")
                        gejalaMassFunctionsList.add(gejalaMassFunctions)
                        Log.d(TAG, "onDataChange - CheckGejalaMassFunctionsList: $gejalaMassFunctionsList")
                    }

                    // Langkah 3: Gunakan aturan kombinasi Dempster-Shafer untuk menggabungkan mass functions
                    var combinedMassFunctions = dempsterShafer.initializeMassFunctions(selectedGejala)
                    Log.d(TAG, "onDataChange - CheckSelectedGejala: $selectedGejala")
                    Log.d(TAG, "onDataChange - CheckDaftarPenyakit: $daftarPenyakit")
                    gejalaMassFunctionsList.forEach { gejalaMassFunctions ->
                        Log.d(TAG, "onDataChange - CheckGejalaMassFunctions: $gejalaMassFunctions")
                        combinedMassFunctions = dempsterShafer.combineMassFunctions(daftarPenyakit, combinedMassFunctions, gejalaMassFunctions)
                        Log.d(TAG, "onDataChange - CheckCombinedMassFunctionsAfterCombination: $combinedMassFunctions")
                    }

                    // Menemukan penyakit dengan nilai belief tertinggi
                    val (namaPenyakitTerbaik, beliefTerbaik) = getBestDiagnosisResult(daftarPenyakit, combinedMassFunctions)

                    // Memanggil callback listener dengan hasil diagnosis
                    withContext(Dispatchers.Main) {
                        callback.onDiagnosisComplete(namaPenyakitTerbaik, beliefTerbaik)
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

    // Fungsi untuk mendapatkan hasil diagnosis terbaik (penyakit dengan belief tertinggi)
    private suspend fun getBestDiagnosisResult(daftarPenyakit: List<String>, combinedMassFunctions: Map<String, Double>): Pair<String, Double> {
        var namaPenyakitTerbaik = ""
        var beliefTerbaik = -1.0

        for (kodePenyakit in daftarPenyakit) {
            val belief = combinedMassFunctions[kodePenyakit] ?: 0.0
            if (belief > beliefTerbaik) {
                beliefTerbaik = belief
                namaPenyakitTerbaik = getNamaPenyakit(kodePenyakit)
            }
        }

        return namaPenyakitTerbaik to beliefTerbaik
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
