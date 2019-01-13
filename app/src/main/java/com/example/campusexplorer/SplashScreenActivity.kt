package com.example.campusexplorer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


const val SERVER_URL = "http://10.176.91.25:8080"

class SplashScreenActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
    }

}
