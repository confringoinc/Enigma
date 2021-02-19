package com.example.makepaper

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot.*

class ForgotActivity : AppCompatActivity() {

    companion object {
        lateinit var auth: FirebaseAuth
    }

    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressBar!!.visibility = View.GONE

        generals.preference = MyPreference(baseContext)

        //  Initializing Authorization Instance
        auth = FirebaseAuth.getInstance()

        //  if user already logged in then direct him to main screen
        if(generals.preference.checkLogged()){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btn_send.setOnClickListener {
            send()
        }

        tv_back.setOnClickListener {
            onBackPressed()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /* <----- Sign in Facility using Typical Email Password ----->*/
    private fun send(){

        if(!isNetworkAvailable()) {
            Toast.makeText(
                    baseContext, "Internet is not available",
                    Toast.LENGTH_SHORT
            ).show()
            return
        }

        if(et_email.text.toString().isEmpty()){
            et_email.error = "Please enter your email address"
            et_email.setBackgroundResource(R.drawable.text_field_error)
            et_email.requestFocus()
            return
        }
        else {
            et_email.setBackgroundResource(R.drawable.text_field)
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString()).matches()){
            et_email.error = "Please enter valid email address"
            et_email.setBackgroundResource(R.drawable.text_field_error)
            et_email.requestFocus()
            return
        }
        else {
            et_email.setBackgroundResource(R.drawable.text_field)
        }

        progressBar!!.visibility = View.VISIBLE

        auth.sendPasswordResetEmail(et_email.text.toString())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(
                                this,
                                "Check your email inbox for re-setting password mail",
                                Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, LoginActivity::class.java))
                        progressBar!!.visibility = View.GONE
                    }
                    else {
                        progressBar!!.visibility = View.GONE
                        Toast.makeText(
                                this,
                                "Please enter valid email address",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}