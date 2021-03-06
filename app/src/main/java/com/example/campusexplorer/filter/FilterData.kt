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
    private val timeRegex: Regex
        get() = Regex("\\d{2}:\\d{2}")

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

    private fun getWeekDay(): String {
        val date = LocalDate.now()
        val dow = date.dayOfWeek
        val dayName = dow.getDisplayName(TextStyle.SHORT, Locale.GERMAN)
//        Log.d(TAG, "day: $dayName")
        return if (dayName == "So." || dayName == "Sa.") "Mo." else dayName
    }

    private fun checkWeekDay(date: String, cycle: String, dayOfWeek: String): Boolean {
        return if (cycle == "Einzel") {
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
            val today = simpleDateFormat.format(Date())
            date.contains(today)
        } else {
            dayOfWeek == getWeekDay()
        }
    }

    fun getFilteredFloors(
        building: Building,
        floor: Floor,
        beginTime: String = "14:00",
        endTime: String = beginTime
    ): List<Room> {
        val filteredLectures = getFilteredDataForFloor(building, floor, beginTime, endTime)
        val rooms = Storage.findAllRooms(floor._id)
        return rooms.filter { room ->
            filteredLectures.any { lecture ->
                lecture.events.any { event ->
                    event.room == room.name && checkWeekDay(
                        event.date,
                        event.cycle,
                        event.dayOfWeek
                    ) && checkTimeInBetween(
                        event.time,
                        beginTime,
                        endTime
                    )
                }
            }
        }
    }

    fun getFreeRoomsForFloor(building: Building, floor: Floor, beginTime: String, endTime: String): List<Room> {
        val filteredDataForFloor = getFilteredDataForFloor(building, floor, beginTime, endTime,
            swapTimeArguments = true,
            ignoreTypeAndFaculty = true
        )
        val filteredLecturesForBuilding = getFilteredDataForBuilding(building, true)
        val gson = Gson()
        Log.d(TAG, "begin: $beginTime, end: $endTime")
        Log.d(
            TAG,
            "filtered for floor: ${gson.toJson(filteredDataForFloor.map { lecture ->
                lecture.events.map { event ->
                    Pair(
                        event.time,
                        event.room
                    )
                }
            })}"
        )
        Log.d(
            TAG,
            "total - time is: ${filteredLecturesForBuilding.map { lecture ->
                lecture.events.map { event ->
                    Pair(
                        event.time,
                        event.room
                    )
                }
            }.size - filteredDataForFloor.map { lecture ->
                lecture.events.map { event ->
                    Pair(
                        event.time,
                        event.room
                    )
                }
            }.size}"
        )

        val rooms = Storage.findAllRooms(floor._id)
        return rooms.filter { room ->
            !filteredDataForFloor.any { lecture -> lecture.events.any { event -> event.room == room.name } } &&
                    filteredLecturesForBuilding.any { lecture -> lecture.events.any { event -> event.room == room.name } }
        }
    }

    fun getFilteredDataForFloor(
        building: Building,
        floor: Floor = Floor("g707000", "", "", "", 0.0, 0, 0, 0, 0, ""),
        beginTime: String = "14:00",
        endTime: String = beginTime, swapTimeArguments: Boolean = false, ignoreTypeAndFaculty: Boolean = false
    ): List<Lecture> {
        var filteredLectures = getFilteredDataForBuilding(building,ignoreTypeAndFaculty)
        val rooms = Storage.findAllRooms(floor._id)

        filteredLectures = filteredLectures.asSequence()
            // remove all events which are not on this floor
            .map { lecture ->
                Lecture(lecture._id, lecture.name, lecture.events.filter { event ->
                    rooms.asSequence().map { room -> room.name }.contains(event.room) && checkWeekDay(
                        event.date,
                        event.cycle,
                        event.dayOfWeek
                    )
                }, lecture.department, lecture.type, lecture.faculty, lecture.link)
            }
            // filter out all lectures which don't have lectures on this floor
            .filter { lecture ->
                // check if this lecture has events in the given floor
                lecture.events.any { event ->
                    rooms.asSequence().map { room -> room.name }.contains(event.room) && checkWeekDay(
                        event.date,
                        event.cycle,
                        event.dayOfWeek
                    ) && checkTimeInBetween(
                        event.time,
                        beginTime,
                        endTime, swapTimeArguments
                    )
                }
            }
            .toList()
        return filteredLectures
    }

    fun getRoomTriple(
        room: Room,
        filteredLectures: List<Lecture>,
        beginTime: String = "14:00"
    ): Triple<Room, List<Lecture>, Lecture?> {
        val roomLectures = filteredLectures.asSequence()
            // remove all events which are not in this room
            .map { lecture ->
                Lecture(lecture._id, lecture.name, lecture.events.filter { event ->
                    room.name == event.room && checkWeekDay(
                        event.date,
                        event.cycle,
                        event.dayOfWeek
                    )
                }, lecture.department, lecture.type, lecture.faculty, lecture.link)
            }
            // filter out all lectures which don't have lectures in this room
            .filter { lecture ->
                // check if this lecture has events in the given room
                lecture.events.any { event ->
                    room.name == event.room && checkWeekDay(
                        event.date,
                        event.cycle,
                        event.dayOfWeek
                    )
                }
            }
            // now split up the events of all lectures into individual lectures
            .fold(emptyList()) { acc: List<Lecture>, lecture: Lecture ->
                acc.union(lecture.events.map { event ->
                    Lecture(
                        lecture._id,
                        lecture.name,
                        listOf(event),
                        lecture.department,
                        lecture.type,
                        lecture.faculty,
                        lecture.link
                    )
                }).toList()
            }.asSequence()
            // finally, sort them by their starting beginTime
            .sortedWith(compareBy { lecture ->
                timeRegex.findAll(lecture.events[0].time).toList().map { it.value }.getOrElse(0) { "" }
            })
            .toList()

        return Triple(room, roomLectures, getCurrentLecture(roomLectures, beginTime))
    }

    private fun getCurrentLecture(
        roomLectures: List<Lecture>,
        beginTime: String,
        endTime: String = beginTime
    ): Lecture? {
        return roomLectures.firstOrNull { lecture ->
            lecture.events.any { event ->
                Log.d(
                    TAG,
                    "is ${event.time} between $beginTime and $endTime? : ${checkTimeInBetween(
                        event.time,
                        beginTime,
                        endTime
                    )}"
                ); checkTimeInBetween(event.time, beginTime, endTime)
            }
        }
    }

    fun getFilteredDataForBuilding(
        building: Building,
        ignoreTypeAndFaculty: Boolean = false
    ): List<Lecture> {
        val allLectures = Storage.getBuildingLectures(building)
        var filteredLectures = allLectures!!.filter { lecture ->
            lecture.events.any { event ->
                checkWeekDay(event.date, event.cycle, event.dayOfWeek)
            } &&
                    (ignoreTypeAndFaculty || (eventTypes.any { eventType ->
                        eventType.active && checkEventType(
                            lecture.type,
                            eventType.name
                        )
                    } &&
                            faculties.any { faculty -> faculty.active && lecture.faculty == faculty.name }))
        }
        filteredLectures = filteredLectures.map { lecture ->
            // build a new Lecture object where the Event.room property is only the room, instead of "building - room"
            Lecture(lecture._id, lecture.name, lecture.events.map { event ->
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

    private fun checkTimeInBetween(
        timeString: String,
        beginTime: String,
        endTime: String = beginTime,
        swapTimeArguments: Boolean = false
    ): Boolean {
        val times = timeRegex.findAll(timeString).toList().map { it -> it.value }
        if (times.size < 2) return false
        val dateFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)

        val beginsInBetween = dateFormat.parse(beginTime) < dateFormat.parse(times[1])

        val endInBetween = dateFormat.parse(times[0]) < dateFormat.parse(endTime)



        Log.d(TAG, "-------------")
        Log.d(TAG, "$beginTime is before ${times[1]}: $beginsInBetween")
        Log.d(TAG, "${times[0]} is before $endTime: $endInBetween")

        return if (swapTimeArguments) {
            beginsInBetween && endInBetween
        } else {
            dateFormat.parse(times[0]) <= dateFormat.parse(beginTime) && dateFormat.parse(endTime) <= dateFormat.parse(
                times[1]
            )
        }

    }

    private fun checkEventType(lectureType: String, eventType: String): Boolean {
        if (lectureType == eventType) {
            return true
        } else {
            if (eventType == "Sonstiges" && !(eventTypes.asSequence().map { it.name }.contains(lectureType))) {
                return true
            }
        }
        return false
    }
}