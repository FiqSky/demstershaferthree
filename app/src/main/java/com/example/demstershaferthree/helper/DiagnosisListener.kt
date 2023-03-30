package com.example.demstershaferthree.helper

interface DiagnosisListener {
    fun onDiagnosisComplete(namaPenyakit: String, beliefValue: Double)
    fun onDiagnosisError(errorMessage: String?)
}
