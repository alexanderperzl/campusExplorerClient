package com.example.campusexplorer.storage

import com.example.campusexplorer.model.Building
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.model.Lecture
import com.example.campusexplorer.model.Room
import java.util.logging.Logger

object Storage {

    private val log = Logger.getLogger(Storage::class.java.name)
    private var roomData: MutableMap<String, Pair<Building, MutableMap<String, Pair<Floor, MutableMap<String, Room>>>>> =
        HashMap()
    private var floorById: MutableMap<String, Floor> = HashMap()
    private var roomById: MutableMap<String, Room> = HashMap()
    // TODO remove this after buildingLectures works
    private var lectures: List<Lecture> = emptyList()
    private var buildingLectures: MutableList<Pair<Building, List<Lecture>>> = mutableListOf()


    fun init(initBuildings: List<Building>, initFloors: List<Floor>, initRooms: List<Room>) {
        initBuildings(initBuildings)
        initFloors(initFloors)
        initRooms(initRooms)
        log.info("Stored ${roomData.count()} buildings, ${floorById.count()} floors and ${roomById.count()} rooms")
    }

    private fun initBuildings(initBuildings: List<Building>) {
        initBuildings.forEach { building ->
            roomData[building._id] = Pair(building, HashMap())
        }
    }

    private fun initFloors(initFloors: List<Floor>) {
        initFloors.forEach { floor ->
            roomData[floor.building]!!.second[floor._id] = Pair(floor, HashMap())
            floorById[floor._id] = floor
        }
    }

    private fun initRooms(initRooms: List<Room>) {
        initRooms.forEach { room ->
            val building = findBuildingForFloor(room.floor)
            if (building != null) {
                roomData[building._id]!!.second[room.floor]!!.second[room._id] = room
                roomById[room._id] = room
            }
        }
    }

    /**
     * Returns the List of Lectures for a particular Building
     */
    fun getBuildingLectures(building: Building): List<Lecture>? {
        return buildingLectures.first { pair -> pair.first._id == building._id }.second
    }

    fun setBuildingLectures(building: Building, lectures: List<Lecture>) {
        // if we already have the building in our buildingLectures, update it
        if (hasLecturesForBuilding(building)) {
            buildingLectures.map { pair ->
                if (pair.first._id == building._id) {
                    Pair(pair.first, lectures)
                } else {
                    pair
                }
            }
        }
        // otherwise, just add the building / lectures pair
        else {
            buildingLectures.add(Pair(building, lectures))
        }
    }

    fun hasLecturesForBuilding(building : Building) : Boolean{
        return buildingLectures.any { pair -> pair.first._id == building._id }
    }

    fun findBuildingForFloor(floorId: String): Building? {
        val buildingId = floorById[floorId]?.building ?: return null
        return roomData[buildingId]?.first
    }

    fun findBuilding(buildingId : String) : Building?{
        return roomData[buildingId]?.first
    }

    fun getAllBuildings(): MutableMap<String, Pair<Building, MutableMap<String, Pair<Floor, MutableMap<String, Room>>>>>? {
        return roomData
    }

    fun findFloors(buildingId: String): MutableMap<String, Pair<Floor, MutableMap<String, Room>>>? {
        return roomData[buildingId]?.second
    }

    fun findAllRooms(floorId: String): List<Room> {
        val buildingId = findBuildingForFloor(floorId)?._id ?: return emptyList()
        val floors = roomData[buildingId]?.second ?: return emptyList()
        val rooms = floors[floorId]?.second
        return if (rooms != null) {
            rooms.map { it.value }.toList()
        } else {
            emptyList<Room>()
        }
    }

    fun findRoom(buildingId: String, floorId: String, roomName: String): Room? {
        val floors = roomData[buildingId]?.second ?: return null
        val rooms = floors[floorId]?.second ?: return null
        val room = rooms.values.filter { it.name == roomName }
        return if (room.count() > 0) {
            room[0]
        } else {
            null
        }
    }

    fun findRoom(roomId: String): Room? {
        return roomById[roomId]
    }

    fun findBuildingById(buildingId: String): Pair<Building, MutableMap<String, Pair<Floor, MutableMap<String, Room>>>>? {
        return roomData[buildingId]
    }
}