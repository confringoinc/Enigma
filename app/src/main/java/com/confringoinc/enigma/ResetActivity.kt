package com.confringoinc.enigma

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_reset.*

class ResetActivity : AppCompatActivity() {

    companion object {
        lateinit var auth: FirebaseAuth
    }

    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressBar!!.visibility = View.GONE

        //  Initializing Authorization Instance
        auth = FirebaseAuth.getInstance()

        btn_reset.setOnClickListener {
            resetPassword()
        }

        tv_login_link.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /* <----- Sign in Facility using Typical Email Password ----->*/
    private fun resetPassword() {

        if (!isNetworkAvailable()) {
            Toast.makeText(
                baseContext, "Internet is not available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (et_password_reset.text.toString().isEmpty()) {
            et_password_reset.error = "Please enter your password"
            et_password_reset.requestFocus()
            return
        }

        if (et_password_reset2.text.toString().isEmpty()) {
            et_password_reset2.error = "Please re-enter your password"
            et_password_reset2.requestFocus()
            return
        }

        if (et_password_reset.text.toString().length < 6) {
            et_password_reset.error = "Password must be at least 6 characters long"
            et_password_reset.requestFocus()
            return
        }

        if (et_password_reset.text.toString() != et_password_reset.text.toString()) {
            et_password_reset2.error = "Password must be same"
            et_password_reset2.requestFocus()
            return
        }

        progressBar!!.visibility = View.VISIBLE

        auth.currentUser!!.updatePassword(et_password_reset.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Password has been reset successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar!!.visibility = View.GONE
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Password reset failed", Toast.LENGTH_SHORT).show()
                    progressBar!!.visibility = View.GONE
                }
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}