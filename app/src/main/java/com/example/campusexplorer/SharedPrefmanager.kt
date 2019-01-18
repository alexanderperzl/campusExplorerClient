package com.example.campusexplorer

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import com.example.campusexplorer.activities.PagerActivity

object SharedPrefmanager {
    private var sharedPreferences: SharedPreferences? = null

    private val ID = "campusExplorer"
    private val INTRO_BOOL = "INTRO_BOOL"


    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(ID, Activity.MODE_PRIVATE)
    }


    fun getIntroBool():Boolean?{
        return sharedPreferences?.getBoolean(INTRO_BOOL,false)
    }

    fun saveIntroBool(boolean:Boolean){
        val prefsEditor = sharedPreferences?.edit()
        prefsEditor?.putBoolean(INTRO_BOOL, boolean)
        prefsEditor?.apply()
    }
}