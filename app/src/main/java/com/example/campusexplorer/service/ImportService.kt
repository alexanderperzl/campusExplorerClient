package com.example.campusexplorer.service

import android.app.IntentService
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
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

        val localIntent = Intent("STORAGE_INITIALIZED")
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }


    private fun loadRooms(): List<Room> {
        val roomJson = resources.assets.open("rooms.json").bufferedReader().use {
            it.readText()
        }
        return gson.fromJson(roomJson, Array<Room>::class.java).toList()
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