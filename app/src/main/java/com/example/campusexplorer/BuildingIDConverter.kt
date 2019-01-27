package com.example.campusexplorer

object BuildingIDConverter {
    // TODO expand this to use more buildings
    private var mapping: HashMap<String, String> = hashMapOf(
        "bw0000" to "Geschw",
        "bw7070" to "Oetting",
        "bw0420" to  "Prof.-Huber-Pl. 2",
        "bw0200" to  "Schellingstr. 3",
        "bw0073" to "Amalienstr. 52",
        "bw0147" to "Amalienstr. 73A",
        "bw0050" to "Schellingstr. 4",
        "bw1020" to   "Amalienstr. 17",
        "bw0250" to "Schellingstr. 9",
        "bw1003" to "Theresienstr. 39"

    )

    fun fromClientToServer(id: String): String? {
        return mapping[id]
    }

    fun getKeys() : List<String>{
        return (mapping.keys).toList()
    }
}