package com.example.makepaper

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null

    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        progressBar = findViewById(R.id.progress_bar)
        progressBar!!.visibility = View.GONE

        //password show hide
        btn_show_pass_reg.setBackgroundResource(R.drawable.ic_eye_hide)
        btn_show_pass_reg.setOnClickListener {
            if (et_password_reg.transformationMethod == null) {
                et_password_reg.transformationMethod = PasswordTransformationMethod()
                btn_show_pass_reg.setBackgroundResource(R.drawable.ic_eye_hide)
            } else {
                et_password_reg.transformationMethod = null
                btn_show_pass_reg.setBackgroundResource(R.drawable.ic_eye_show)
            }
        }

        //  Initializing preference
        //  Initializing Authorization Instance
        this.auth = FirebaseAuth.getInstance()

        //  Handle on register Click button
        btn_register.setOnClickListener {
            signUp()
        }

        //  Handle on click of login text
        tv_login_link.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth?.currentUser
        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun signUp(){

        if(!isNetworkAvailable()) {
            Toast.makeText(
                baseContext, "Internet is not available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if(et_name.text.toString().isEmpty()){
            et_name.error = "Please enter your name"
            et_name.requestFocus()
            return
        }

        if(et_email_reg.text.toString().isEmpty()){
            et_email_reg.error = "Please enter your email address"
            et_email_reg.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(et_email_reg.text.toString()).matches()){
            et_email_reg.error = "Please enter a valid email address"
            et_email_reg.requestFocus()
            return
        }

        if(et_password_reg.text.toString().isEmpty()){
            et_password_reg.error = "Please enter your password"
            et_password_reg.requestFocus()
            return
        }

        if(et_password_reg.text.toString().length < 6){
            et_password_reg.error = "Password must be at least 6 characters long"
            et_password_reg.requestFocus()
            return
        }

        progressBar!!.visibility = View.VISIBLE

        //  If all data is valid then add user
        auth?.createUserWithEmailAndPassword(
            et_email_reg.text.toString(),
            et_password_reg.text.toString()
        )
            ?.addOnCompleteListener(this) { task ->
                if(task.isSuccessful){

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(et_name.text.toString())
                        .build()

                    auth!!.currentUser!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { taskSuccess ->
                            if (taskSuccess.isSuccessful) {
                                Toast.makeText(baseContext, "Registration successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                                progressBar!!.visibility = View.GONE
                            }
                            else {
                                Toast.makeText(baseContext, "Registration failed", Toast.LENGTH_SHORT).show()
                                progressBar!!.visibility = View.GONE
                            }
                        }
                } else {
                    Toast.makeText(
                        baseContext,
                        "Email address is already exist",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar!!.visibility = View.GONE
                }
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}