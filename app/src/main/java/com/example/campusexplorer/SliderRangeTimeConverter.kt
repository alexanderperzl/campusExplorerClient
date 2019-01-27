package com.example.campusexplorer

object SliderRangeTimeConverter {

    private var mapping: HashMap<Int, String> = hashMapOf(
        0 to "08:00",
        10 to "09:00",
        20 to "10:00",
        30 to "11:00",
        40 to "12:00",
        50 to "13:00",
        60 to "14:00",
        70 to "15:00",
        80 to "16:00",
        90 to "17:00",
        100 to "18:00",
        110 to "19:00",
        120 to "20:00"
    )

    fun valueToTime(id: Int): String? {
        return mapping[id]
    }

    fun timeToValue(time: String): Int? {
        return mapping.toList().fold(0){acc, (key, value)-> if (value == time) key else acc}
    }
}