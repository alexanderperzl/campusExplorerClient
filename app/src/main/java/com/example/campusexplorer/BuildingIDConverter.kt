package com.example.campusexplorer

object BuildingIDConverter {
    private var mapping: HashMap<String, String> = hashMapOf(
        "bw0000" to "Geschw",
        "bw7070" to "Oetting"
    )

    fun fromClientToServer(id: String): String? {
        return mapping[id]
    }

    fun getKeys() : List<String>{
        return (mapping.keys).toList()
    }
}