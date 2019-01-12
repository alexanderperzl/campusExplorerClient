package com.example.campusexplorer.filter

object FilterData  {

    var faculties : List<FilterObject> = emptyList()
    var eventTypes : List<FilterObject> = emptyList()

    fun init(initFaculties : List<FilterObject>, initEventTypes : List<FilterObject>){
        faculties = initFaculties
        eventTypes = initEventTypes
    }

    fun setValue(value : Boolean, name : String){
        if (faculties.any { faculty -> faculty.name == name }){
            faculties.first { faculty -> faculty.name == name}.active = value
        } else if (eventTypes.any { eventType-> eventType.name == name }){
            eventTypes.first { eventType-> eventType.name == name}.active = value
        }
    }
}