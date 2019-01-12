package com.example.campusexplorer.service

import android.app.IntentService
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.example.campusexplorer.filter.FilterData
import com.example.campusexplorer.filter.FilterObject
import com.example.campusexplorer.model.Building
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.model.Room
import com.example.campusexplorer.storage.Storage
import com.google.gson.Gson
import java.util.logging.Logger


class ImportService : IntentService("ImportService") {

    val log = Logger.getLogger(ImportService::class.java.name)
    val gson = Gson()


    override fun onHandleIntent(intent: Intent?) {
        val rooms = loadRooms()
        val floors = loadFloors()
        val buildings = loadBuildings()
        log.info("Loaded ${buildings.count()} buildings, ${floors.count()} floors and ${rooms.count()} rooms")
        Storage.init(buildings, floors, rooms)

        val faculties = loadFaculties()
        val eventTypes = loadEventTypes()
        FilterData.init(faculties, eventTypes)

        val localIntent = Intent("STORAGE_INITIALIZED")
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }


    private fun loadRooms(): List<Room> {
        val roomJson = resources.assets.open("rooms.json").bufferedReader().use {
            it.readText()
        }
        return gson.fromJson(roomJson, Array<Room>::class.java).toList()
    }

    private fun loadEventTypes(): List<FilterObject> {
        val eventTypeJson = resources.assets.open("eventTypes.json").bufferedReader().use {
            it.readText()
        }
        val eventTypes = gson.fromJson(eventTypeJson, Array<String>::class.java).toList()
        return eventTypes.map {  eventType -> FilterObject(eventType, true)}
    }

    private fun loadFaculties(): List<FilterObject> {
        val facultiesJson = resources.assets.open("faculties.json").bufferedReader().use {
            it.readText()
        }
        val faculties = gson.fromJson(facultiesJson, Array<String>::class.java).toList()
        return faculties.map { faculty -> FilterObject(faculty, true)}
    }

    private fun loadFloors(): List<Floor> {
        val floorJson = resources.assets.open("floors.json").bufferedReader().use {
            it.readText()
        }
        return gson.fromJson(floorJson, Array<Floor>::class.java).toList()
    }

    private fun loadBuildings(): List<Building> {
        val buildingJson = resources.assets.open("buildings.json").bufferedReader().use {
            it.readText()
        }
        return gson.fromJson(buildingJson, Array<Building>::class.java).toList()
    }
}