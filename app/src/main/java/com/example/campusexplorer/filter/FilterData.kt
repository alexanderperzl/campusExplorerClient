package com.example.campusexplorer.filter

import android.util.Log
import com.example.campusexplorer.model.*
import com.example.campusexplorer.storage.Storage
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import java.time.temporal.WeekFields


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

//    fun getRoomTriples(building: Building, floor: Floor) : List<Triple<Room, List<Lecture>, Lecture>>{
//
//    }

    private fun getWeekDay():String{
        val date = LocalDate.now()
        val dow = date.dayOfWeek
        val dayName = dow.getDisplayName(TextStyle.SHORT, Locale.GERMAN);
        Log.d(TAG, "day: $dayName")
        return if (dayName == "So." || dayName == "Sa.") "Mo." else dayName
    }

    fun getFilteredFloors(building: Building, floor: Floor) : List<Room>{
        val filteredLectures = getFilteredDataForFloor(building, floor)
        val rooms = Storage.findAllRooms(floor._id)
        getWeekDay()
        return rooms.filter{room ->
            filteredLectures.any { lecture ->
                lecture.events.any { event ->
                    event.room == room.name && event.dayOfWeek.contains(getWeekDay())
                }
            }
        }
    }

    fun getFilteredDataForFloor(
        building: Building,
        floor: Floor = Floor("g707000", "", "", "", 0.0, 0, 0, 0, 0 ,"")
    ): List<Lecture> {
        var filteredLectures = getFilteredDataForBuilding(building)
        val rooms = Storage.findAllRooms(floor._id)
        val gson = Gson()
        Log.d(TAG, "rooms" + gson.toJson(rooms.map { room -> room.name }))

        filteredLectures = filteredLectures.asSequence()
            // remove all events which are not on this floor
            .map { lecture ->
                Lecture(lecture.id, lecture.name, lecture.events.filter { event ->
                    rooms.asSequence().map { room -> room.name }.contains(event.room)
                }, lecture.department, lecture.type, lecture.faculty, lecture.link)
            }
            // filter out all lectures which don't have lectures on this floor
            .filter { lecture ->
                // check if this lecture has events in the given floor
                lecture.events.any { event -> rooms.asSequence().map { room -> room.name }.contains(event.room) }
            }
            .toList()
        Log.d(TAG, gson.toJson(filteredLectures))
        return filteredLectures
    }

    fun getRoomTriple(room: Room, filteredLectures : List<Lecture>) : Triple<Room, List<Lecture>, Lecture>{
        val roomLectures = filteredLectures.asSequence()
            // remove all events which are not in this room
            .map { lecture ->
                Lecture(lecture.id, lecture.name, lecture.events.filter { event ->
                    room.name == event.room && event.dayOfWeek.contains(getWeekDay())
                }, lecture.department, lecture.type, lecture.faculty, lecture.link)
            }
            // filter out all lectures which don't have lectures in this room
            .filter { lecture ->
                // check if this lecture has events in the given room
                lecture.events.any { event -> room.name == event.room && event.dayOfWeek.contains(getWeekDay())}
            }
            .toList()
        val gson = Gson()

        Log.d(TAG, "room triple:")
        Log.d(TAG, gson.toJson(Triple(room, roomLectures , roomLectures.first())))
        return Triple(room, roomLectures , roomLectures.first())
    }

    fun getFilteredDataForBuilding(building: Building): List<Lecture> {
        Log.d(TAG, "filtering data")
        val allLectures = Storage.getBuildingLectures(building)
        var filteredLectures = allLectures!!.filter { lecture ->
            eventTypes.any { eventType -> eventType.active && lecture.type == eventType.name } &&
                    faculties.any { faculty -> faculty.active && lecture.faculty == faculty.name }
        }
        filteredLectures = filteredLectures.map {
            // build a new Lecture object where the Event.room property is only the room, instead of "building - room"
            Lecture(it.id, it.name, it.events.map { event ->
                // here, build a new event Object, where the room value gets adjusted
                Event(
                    event.room.split(" - ").getOrElse(1) { "" },
                    event.room.split(" - ").getOrElse(0) { "" },
                    event.time,
                    event.date,
                    event.dayOfWeek,
                    event.cycle
                )
            }, it.department, it.type, it.faculty, it.link)
        }
        return filteredLectures
    }


}