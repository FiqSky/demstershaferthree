package com.example.demstershaferthree

class DempsterShafer {
    fun initializeMassFunctions(daftarPenyakit: List<String>): MutableMap<String, Double> {
        val massFunctions = mutableMapOf<String, Double>()

        val allCombinations = generateCombinations(daftarPenyakit)
        allCombinations.forEach { combination ->
            massFunctions[combination.joinToString(",")] = 0.0
        }

        // Himpunan kosong
        massFunctions[""] = 0.0

        return massFunctions
    }
    fun combineMassFunctions(daftarPenyakit: List<String>, m1: MutableMap<String, Double>, m2: Map<String, Double>): MutableMap<String, Double> {
        val combinedMassFunctions = initializeMassFunctions(daftarPenyakit)
        val normalizationFactor = 1.0 - calculateConflict(m1, m2, daftarPenyakit)

        m1.forEach { (key1, value1) ->
            m2.forEach { (key2, value2) ->
                val combinedKey = combineKeys(key1, key2, daftarPenyakit)
                if (combinedKey.isNotEmpty()) {
                    val newValue = (value1 * value2) / normalizationFactor
                    combinedMassFunctions[combinedKey] = combinedMassFunctions.getOrDefault(combinedKey, 0.0) + newValue
                } else if (key1.isEmpty() && key2.isEmpty()) {
                    combinedMassFunctions[""] = combinedMassFunctions.getOrDefault("", 0.0) + (value1 * value2)
                }
            }
        }

        return combinedMassFunctions
    }

    fun combineAllMassFunctions(daftarPenyakit: List<String>, gejalaMassFunctionsList: List<Map<String, Double>>): MutableMap<String, Double> {
        var combinedMassFunctions = initializeMassFunctions(daftarPenyakit)

        for (gejalaMassFunctions in gejalaMassFunctionsList) {
            combinedMassFunctions = combineMassFunctions(daftarPenyakit, combinedMassFunctions, gejalaMassFunctions)
        }

        return combinedMassFunctions
    }

    private fun combineKeys(key1: String, key2: String, daftarPenyakit: List<String>): String {
        val key1List = key1.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val key2List = key2.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        val resultSet = key1List.union(key2List).filter { daftarPenyakit.contains(it) }.sorted().joinToString(",")

        return resultSet
    }
    private fun calculateConflict(m1: Map<String, Double>, m2: Map<String, Double>, daftarPenyakit: List<String>): Double {
        var conflict = 0.0
        m1.forEach { (key1, value1) ->
            m2.forEach { (key2, value2) ->
                val combinedKey = combineKeys(key1, key2, daftarPenyakit)
                if (combinedKey.isEmpty()) {
                    conflict += value1 * value2
                }
            }
        }
        return conflict
    }

    fun intersect(set1: String, set2: String): String {
        val set1List = set1.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val set2List = set2.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        val resultSet = set1List.intersect(set2List).joinToString(",")

        return resultSet
    }

    private fun generateCombinations(selectedGejala: List<String>): List<List<String>> {
        val result = mutableListOf<List<String>>()

        fun generate(index: Int, current: List<String>) {
            if (index == selectedGejala.size) {
                if (current.isNotEmpty()) {
                    result.add(current)
                }
                return
            }
            // Exclude the current element
            generate(index + 1, current)

            // Include the current element
            generate(index + 1, current + selectedGejala[index])
        }
        generate(0, emptyList())
        return result
    }
}
