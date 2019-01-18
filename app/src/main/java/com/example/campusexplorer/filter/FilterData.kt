package com.example.campusexplorer.filter

import android.util.Log
import com.example.campusexplorer.model.*
import com.example.campusexplorer.storage.Storage
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

object FilterData {

    private const val TAG = "FilterData"
    private val timeRegex = Regex("\\d{2}:\\d{2}")

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

    private fun getWeekDay(): String {
        val date = LocalDate.now()
        val dow = date.dayOfWeek
        val dayName = dow.getDisplayName(TextStyle.SHORT, Locale.GERMAN)
        Log.d(TAG, "day: $dayName")
        return if (dayName == "So." || dayName == "Sa.") "Mo." else dayName
    }

    private fun checkWeekDay(date: String, cycle: String, dayOfWeek: String):Boolean{
        if (cycle == "Einzel"){
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
            val today = simpleDateFormat.format(Date())
            return date.contains(today)
        } else {
            return dayOfWeek == getWeekDay()
        }
    }

    fun getFilteredFloors(building: Building, floor: Floor, time: String = "14:00"): List<Room> {
        val filteredLectures = getFilteredDataForFloor(building, floor, time)
        val rooms = Storage.findAllRooms(floor._id)
        return rooms.filter { room ->
            filteredLectures.any { lecture ->
                lecture.events.any { event ->
                    event.room == room.name && checkWeekDay(event.date, event.cycle, event.dayOfWeek)
                }
            }
        }
    }

    fun getFreeRoomsForFloor(building: Building, floor: Floor, beginTime: String, endTime: String): List<Room> {
        val filteredDataForFloor = getFilteredDataForFloor(building, floor, beginTime, endTime)
        val rooms = Storage.findAllRooms(floor._id)
        return rooms.filter { room ->
            !filteredDataForFloor.any { lecture -> lecture.events.any { event -> event.room == room.name } }
        }
    }

    fun getFilteredDataForFloor(
        building: Building,
        floor: Floor = Floor("g707000", "", "", "", 0.0, 0, 0, 0, 0, ""),
        beginTime: String = "14:00",
        endTime: String = beginTime
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
                lecture.events.any { event ->
                    rooms.asSequence().map { room -> room.name }.contains(event.room) && checkTimeInBetween(
                        event.time,
                        beginTime,
                        endTime
                    )
                }
            }
            .toList()
        Log.d(TAG, gson.toJson(filteredLectures))
        return filteredLectures
    }

    fun getRoomTriple(
        room: Room,
        filteredLectures: List<Lecture>,
        time: String = "14:00"
    ): Triple<Room, List<Lecture>, Lecture?> {
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
                lecture.events.any { event -> room.name == event.room && checkWeekDay(event.date, event.cycle, event.dayOfWeek) }
            }
            // now split up the events of all lectures into individual lectures
            .fold(emptyList()) { acc: List<Lecture>, lecture: Lecture ->
                acc.union(lecture.events.map { event ->
                    Lecture(
                        lecture.id,
                        lecture.name,
                        listOf(event),
                        lecture.department,
                        lecture.type,
                        lecture.faculty,
                        lecture.link
                    )
                }).toList()
            }.asSequence()
            // finally, sort them by their starting time
            .sortedWith(compareBy { lecture ->
                timeRegex.findAll(lecture.events[0].time).toList().map { it -> it.value }.getOrElse(0) { "" }
            })
            .toList()
        val gson = Gson()

        Log.d(TAG, "room triple:")
        Log.d(TAG, gson.toJson(Triple(room, roomLectures, getCurrentLecture(roomLectures, time))))

        return Triple(room, roomLectures, getCurrentLecture(roomLectures, time))
    }

    private fun getCurrentLecture(roomLectures: List<Lecture>, time: String): Lecture? {
        return roomLectures.firstOrNull { lecture ->
            lecture.events.any { event -> checkTimeInBetween(event.time, time) }
        }
    }

    fun getFilteredDataForBuilding(building: Building, ignoreTypeAndFaculty: Boolean = false): List<Lecture> {
        Log.d(TAG, "filtering data")
        val allLectures = Storage.getBuildingLectures(building)
        var filteredLectures = allLectures!!.filter { lecture ->
            ignoreTypeAndFaculty || (eventTypes.any { eventType ->
                eventType.active && checkEventType(
                    lecture.type,
                    eventType.name
                )
            } &&
                    faculties.any { faculty -> faculty.active && lecture.faculty == faculty.name })
        }
        filteredLectures = filteredLectures.map { lecture ->
            // build a new Lecture object where the Event.room property is only the room, instead of "building - room"
            Lecture(lecture.id, lecture.name, lecture.events.map { event ->
                // here, build a new event Object, where the room value gets adjusted
                Event(
                    event.room.split(" - ").getOrElse(1) { "" },
                    event.room.split(" - ").getOrElse(0) { "" },
                    event.time,
                    event.date,
                    event.dayOfWeek,
                    event.cycle
                )
            }, lecture.department, lecture.type, lecture.faculty, lecture.link)
        }
        return filteredLectures
    }

    private fun checkTimeInBetween(timeString: String, beginTime: String, endTime: String = beginTime): Boolean {
        val times = timeRegex.findAll(timeString).toList().map { it -> it.value }
        if (times.size < 2) return false
        val dateFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)
        return dateFormat.parse(times[0]) <= dateFormat.parse(beginTime) && dateFormat.parse(endTime) <= dateFormat.parse(
            times[1]
        )
    }

    private fun checkEventType(lectureType: String, eventType: String): Boolean {
        if (lectureType == eventType) {
            return true
        } else {
            if (eventType == "Sonstiges" && !(eventTypes.asSequence().map { it -> it.name }.contains(lectureType))) {
                return true
            }
        }
        return false
    }
}