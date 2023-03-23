package com.example.demstershaferthree

import kotlin.math.roundToInt

class DempsterShaferCalculator(private val gejalaList: List<Gejala>, private val penyakitList: List<Penyakit>) {
    fun calculatePlausibility(selectedGejalaList: List<String>): Map<String, Double> {
        val plausibilityMap = mutableMapOf<String, Double>()

        // create empty hypothesis set
        val hypothesisSet = mutableSetOf<Set<String>>()

        // generate power set of selected gejala
        val selectedGejalaSet = selectedGejalaList.toSet()
        val powerSet = selectedGejalaSet.powerSet()

        // add each subset of power set to hypothesis set
        for (subset in powerSet) {
            if (subset.isNotEmpty() && subset != selectedGejalaSet) {
                hypothesisSet.add(subset)
            }
        }

        // calculate mass for each hypothesis
        for (penyakit in penyakitList) {
            var mass = 1.0

            // calculate mass for each gejala
            for (gejala in gejalaList) {
                if (gejala.kodeGejala in selectedGejalaList && gejala.kodeGejala in penyakit.daftarGejala) {
                    mass *= gejala.bobot
                } else if (gejala.kodeGejala !in selectedGejalaList && gejala.kodeGejala in penyakit.daftarGejala) {
                    mass *= (1 - gejala.bobot)
                }
            }

            // calculate mass for each hypothesis
            for (hypothesis in hypothesisSet) {
                var intersection = hypothesis.intersect(penyakit.daftarGejala.toSet())

                if (intersection.isNotEmpty()) {
                    var massHypothesis = 1.0

                    // calculate mass for intersection of hypothesis and selected gejala
                    for (gejala in gejalaList) {
                        if (gejala.kodeGejala in hypothesis && gejala.kodeGejala in selectedGejalaList) {
                            massHypothesis *= gejala.bobot
                        } else if (gejala.kodeGejala in hypothesis && gejala.kodeGejala !in selectedGejalaList) {
                            massHypothesis *= (1 - gejala.bobot)
                        }
                    }

                    // update mass for hypothesis
                    mass *= (1 - massHypothesis)
                }
            }

            plausibilityMap[penyakit.namaPenyakit] = mass
        }

        // normalize plausibility values
        val totalPlausibility = plausibilityMap.values.sum()
        for (penyakit in penyakitList) {
            val plausibility = plausibilityMap[penyakit.namaPenyakit] ?: 0.0
            plausibilityMap[penyakit.namaPenyakit] = (plausibility / totalPlausibility).round(2)
        }

        return plausibilityMap
    }

    private fun <T> Set<T>.powerSet(): Set<Set<T>> {
        if (isEmpty()) return setOf(setOf())
        val element = first()
        val rest = drop(1).powerSet()
        return rest + rest.map { it + element }
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}
