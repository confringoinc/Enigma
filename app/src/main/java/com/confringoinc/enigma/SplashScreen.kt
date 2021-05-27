package com.confringoinc.enigma

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        splashScreenIV.alpha = 0f

        splashScreenTV.text = getString(R.string.app_name)
        splashScreenIV.animate().setDuration(1500).alpha(1f).withEndAction {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            val sharedPreferences: SharedPreferences =
                getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            if (sharedPreferences.getBoolean(OnBoardingActivity.prevStarted, false)) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, OnBoardingActivity::class.java))
                finish()
            }
        }
    }
}