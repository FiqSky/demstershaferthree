package com.example.demstershaferthree.helper

interface DiagnosisListener {
    fun onDiagnosisComplete(daftarBeliefAkhir: Map<String, Double>)
    fun onDiagnosisError(errorMessage: String?)
}
