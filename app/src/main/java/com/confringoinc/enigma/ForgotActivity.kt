package com.confringoinc.enigma

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
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

        //  Initializing Authorization Instance
        auth = FirebaseAuth.getInstance()

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
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    /* <----- Sign in Facility using Typical Email Password ----->*/
    private fun send() {

        if (!isNetworkAvailable()) {
            Toast.makeText(
                baseContext, "Internet is not available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (et_email.text.toString().isEmpty()) {
            et_email.error = "Please enter your email address"
            et_email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString()).matches()) {
            et_email.error = "Please enter valid email address"
            et_email.requestFocus()
            return
        }

        progressBar!!.visibility = View.VISIBLE

        val url = "https://makepaper.page.link/resetPassword"
        val settings = ActionCodeSettings.newBuilder()
            .setAndroidPackageName(
                packageName,
                true,  /* install if not available? */
                null /* minimum app version */
            )
            .setHandleCodeInApp(true)
            .setUrl(url)
            .build()

        auth.sendPasswordResetEmail(et_email.text.toString(), settings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Check your email inbox for re-setting password mail",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar!!.visibility = View.GONE
                } else {
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Please enter valid email address",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun handleDeepLink(intent: Intent) {
        FirebaseDynamicLinks
            .getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }
                Toast.makeText(
                    this,
                    deepLink.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, ResetActivity::class.java))
                finish()
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}