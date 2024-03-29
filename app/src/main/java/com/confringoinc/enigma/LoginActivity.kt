package com.confringoinc.enigma

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
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN = 120
        lateinit var auth: FirebaseAuth
        lateinit var googleSignInClient: GoogleSignInClient
    }

    private var callbackManager: CallbackManager? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        progressBar = findViewById(R.id.progress_bar)
        progressBar!!.visibility = View.GONE

        //password show hide
        btn_show_pass.setBackgroundResource(R.drawable.ic_eye_hide)
        btn_show_pass.setOnClickListener {
            if (et_password.transformationMethod == null) {
                et_password.transformationMethod = PasswordTransformationMethod()
                btn_show_pass.setBackgroundResource(R.drawable.ic_eye_hide)
            } else {
                et_password.transformationMethod = null
                btn_show_pass.setBackgroundResource(R.drawable.ic_eye_show)
            }
        }

        //  Initializing Authorization Instance
        auth = FirebaseAuth.getInstance()

        callbackManager = CallbackManager.Factory.create()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        tv_register_link.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btn_login.setOnClickListener {
            logIn()
        }

        ib_google.setOnClickListener {
            signInWithGoogle()
        }

        lb_facebook.setOnClickListener {
            progressBar!!.visibility = View.VISIBLE
        }

        tv_reset.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }

        lb_facebook.setPermissions(listOf("email", "public_profile"))
        callbackManager = CallbackManager.Factory.create()

        lb_facebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                progressBar!!.visibility = View.GONE
                Toast.makeText(baseContext, "Login failed", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                progressBar!!.visibility = View.GONE
                Toast.makeText(baseContext, "Login failed", Toast.LENGTH_SHORT).show()
            }
        })
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

    public override fun onStop() {
        facebookLogOut()
        super.onStop()
    }

    /*<------ Signing In activity using Google sign in feature ----->*/
    private fun signInWithGoogle() {
        progressBar!!.visibility = View.VISIBLE
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
                }
            } else {
                progressBar!!.visibility = View.GONE
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
        }

        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressBar!!.visibility = View.GONE
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
                }
            }
    }
    /*<------ Signing In activity using Google sign in feature ----->*/

    /*<------ Signing In activity using Facebook sign in feature ----->*/
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                    progressBar!!.visibility = View.GONE
                } else {
                    // If sign in fails, display a message to the user.
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(
                        baseContext, "Failed to sign in",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun facebookLogOut() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return  // already logged out
        }

        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null, HttpMethod.DELETE
        ) { LoginManager.getInstance().logOut() }.executeAsync()
    }
    /*<------ Signing In activity using Facebook sign in feature ----->*/

    /* <----- Sign in Facility using Typical Email Password ----->*/
    private fun logIn() {

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

        if (et_password.text.toString().isEmpty()) {
            et_password.error = "Please enter your password"
            et_password.requestFocus()
            return
        }

        progressBar!!.visibility = View.VISIBLE

        //  If all data is valid then Let user log in
        auth.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                    progressBar!!.visibility = View.GONE
                } else {
                    // If sign in fails, display a message to the user.
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(
                        baseContext, "Email or password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                    et_email.requestFocus()
                }
            }
    }
    /* <----- Sign in Facility using Typical Email Password ----->*/

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
