package com.example.demstershaferthree

class DempsterShafer {
    fun initializeMassFunctions(penyakitList: List<String>): MutableMap<Set<String>, Double> {
        val massFunctions = mutableMapOf<Set<String>, Double>()
        val allSubsets = generateAllSubsets(penyakitList)

        allSubsets.forEach { subset ->
            massFunctions[subset] = 0.0
        }

        return massFunctions
    }

    fun combineMassFunctions(m1: MutableMap<Set<String>, Double>, m2: Map<Set<String>, Double>): MutableMap<Set<String>, Double> {
        val combinedMassFunctions = mutableMapOf<Set<String>, Double>()
        val normalizationFactor = 1.0 - calculateConflict(m1, m2)

        m1.forEach { (key1, value1) ->
            m2.forEach { (key2, value2) ->
                val intersection = key1.intersect(key2)
                if (intersection.isNotEmpty()) {
                    val newValue = (value1 * value2) / normalizationFactor
                    combinedMassFunctions[intersection] = combinedMassFunctions.getOrDefault(intersection, 0.0) + newValue
                }
            }
        }

        return combinedMassFunctions
    }

    private fun calculateConflict(m1: Map<Set<String>, Double>, m2: Map<Set<String>, Double>): Double {
        var conflict = 0.0
        m1.forEach { (key1, value1) ->
            m2.forEach { (key2, value2) ->
                if (key1.intersect(key2).isEmpty()) {
                    conflict += value1 * value2
                }
            }
        }
        return conflict
    }

    private fun generateAllSubsets(set: List<String>): List<Set<String>> {
        val result = mutableListOf<Set<String>>()
        val n = set.size

        for (i in 0 until (1 shl n)) {
            val subset = mutableSetOf<String>()
            for (j in 0 until n) {
                if (i and (1 shl j) > 0) {
                    subset.add(set[j])
                }
            }
            result.add(subset)
        }
        return result
    }
}
