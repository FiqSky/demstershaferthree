package com.example.demstershaferthree

class DempsterShafer {
    fun initializeMassFunctions(penyakitList: List<String>): MutableMap<String, Double> {
        val massFunctions = mutableMapOf<String, Double>()

        penyakitList.forEach { penyakit ->
            massFunctions[penyakit] = 0.0
        }

        // Himpunan kosong
        massFunctions[""] = 0.0

        return massFunctions
    }

    fun combineMassFunctions(m1: MutableMap<String, Double>, m2: Map<String, Double>): MutableMap<String, Double> {
        val combinedMassFunctions = initializeMassFunctions(emptyList())
        val normalizationFactor = 1.0 - calculateConflict(m1, m2)

        m1.forEach { (key1, value1) ->
            m2.forEach { (key2, value2) ->
                if (key1 == key2) {
                    val newValue = (value1 * value2) / normalizationFactor
                    combinedMassFunctions[key1] = combinedMassFunctions.getOrDefault(key1, 0.0) + newValue
                }
            }
        }

        return combinedMassFunctions
    }

    private fun calculateConflict(m1: Map<String, Double>, m2: Map<String, Double>): Double {
        var conflict = 0.0
        m1.forEach { (key1, value1) ->
            m2.forEach { (key2, value2) ->
                if (key1 == "" || key2 == "") {
                    conflict += value1 * value2
                }
            }
        }
        return conflict
    }
}
