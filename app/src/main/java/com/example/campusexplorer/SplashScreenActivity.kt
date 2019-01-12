package com.example.campusexplorer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson


const val SERVER_URL = "http://10.181.50.61:8080"

class SplashScreenActivity : AppCompatActivity() {
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
    }

}
