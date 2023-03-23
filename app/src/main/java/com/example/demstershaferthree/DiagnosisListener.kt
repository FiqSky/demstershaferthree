package com.example.demstershaferthree

interface DiagnosisListener {
    fun onDiagnosisComplete(daftarBeliefAkhir: Map<String, Double>)
    fun onDiagnosisError(errorMessage: String?)
}
