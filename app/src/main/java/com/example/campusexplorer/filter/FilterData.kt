package com.example.campusexplorer.filter

import android.util.Log
import com.example.campusexplorer.model.Building
import com.example.campusexplorer.model.Lecture
import com.example.campusexplorer.storage.Storage
import com.google.gson.Gson

object FilterData {

    val TAG = "FilterData"

    var faculties: List<FilterObject> = emptyList()
    var eventTypes: List<FilterObject> = emptyList()

    fun init(initFaculties: List<FilterObject>, initEventTypes: List<FilterObject>) {
        faculties = initFaculties
        eventTypes = initEventTypes
    }

    fun setValue(value: Boolean, name: String) {
        if (faculties.any { faculty -> faculty.name == name }) {
            faculties.first { faculty -> faculty.name == name }.active = value
        } else if (eventTypes.any { eventType -> eventType.name == name }) {
            eventTypes.first { eventType -> eventType.name == name }.active = value
        }
    }

    fun getFilteredData(building : Building): List<Lecture> {
        val allLectures = Storage.getBuildingLectures(building)
        var filteredLectures = allLectures!!.filter { lecture ->
            eventTypes.any { eventType -> eventType.active && lecture.type == eventType.name } &&
                    faculties.any { faculty -> faculty.active && lecture.faculty == faculty.name }
        }
        val gson = Gson()
                Log.d(TAG, gson.toJson(filteredLectures))
        return filteredLectures
    }
}