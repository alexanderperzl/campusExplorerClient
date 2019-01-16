package com.example.campusexplorer.model

data class Floor(
    val _id: String,
    val building: String,
    val mapFileName: String,
    val level: String,
    var levelDouble: Double,
    val mapWidth: Int,
    val mapHeight: Int,
    val markerOffsetX: Int?,
    val markerOffsetY: Int?,
    val address: String
) : Comparable<Floor> {

    override fun compareTo(other: Floor): Int {
        if (this.levelDouble > other.levelDouble) return 1
        return if (this.levelDouble < other.levelDouble) -1 else 0
    }


    fun setLevelDouble(): Floor {
        this.levelDouble = levelToNumber(level)
        return this
    }

    private fun levelToNumber(level: String): Double {
        val mapping: HashMap<String, Double> = hashMapOf(
            "EG" to 0.0,
            "OG 01" to 1.0,
            "OG 02" to 2.0,
            "OG 03" to 3.0,
            "OG 04" to 4.0,
            "OG 05" to 5.0,
            "OG 06" to 6.0,
            "OG 07" to 7.0,
            "OG 08" to 8.0,
            "OG 09" to 9.0,
            "OG 10" to 10.0,
            "UG 01" to -1.0,
            "UG 02" to -2.0,
            "UG 03" to -3.0,
            "UG 04" to -4.0,
            "UG 05" to -5.0,
            "UG 06" to -6.0,
            "UG 07" to -7.0,
            "UG 08" to -8.0,
            "UG 09" to -9.0,
            "UG 10" to -10.0,
            "Z" to 0.5
        )
        return mapping.keys.fold(0.0) { sum, it ->
            if (level.contains(it)) {
                sum + mapping[it]!!
            } else {
                sum
            }
        }
    }
}