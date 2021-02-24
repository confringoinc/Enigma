package com.example.makepaper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        splashScreenIV.alpha = 0f

        splashScreenTV.text = "Question Paper Maker"
        splashScreenIV.animate().setDuration(1500).alpha(1f).withEndAction {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}