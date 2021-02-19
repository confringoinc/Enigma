package com.example.makepaper

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
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
import org.json.JSONException


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

        val forgot = findViewById<TextView>(R.id.tv_forgot)
        forgot.text = HtmlCompat.fromHtml(
                getString(R.string.forgot),
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        val register = findViewById<TextView>(R.id.tv_register)
        register.text = HtmlCompat.fromHtml(
                getString(R.string.notLoggedIn),
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        //password show hide
        btn_show_pass.setBackgroundResource(R.drawable.eye_hide)
        btn_show_pass.setOnClickListener {
            if (et_password.transformationMethod == null) {
                et_password.transformationMethod = PasswordTransformationMethod()
                btn_show_pass.setBackgroundResource(R.drawable.eye_hide)
            } else {
                et_password.transformationMethod = null
                btn_show_pass.setBackgroundResource(R.drawable.eye_show)
            }
        }

        generals.preference = MyPreference(baseContext)

        //  Initializing Authorization Instance
        auth = FirebaseAuth.getInstance()

        callbackManager = CallbackManager.Factory.create()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //  if user already logged in then direct him to main screen
        if(generals.preference.checkLogged()){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tv_register.setOnClickListener {
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

        tv_forgot.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }

        lb_facebook.setReadPermissions(listOf("email", "public_profile"))
        callbackManager = CallbackManager.Factory.create()

        lb_facebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {

                val request = GraphRequest.newMeRequest(
                        loginResult.accessToken
                ) { `object`, _ ->
                    if (`object` != null) {
                        try {
                            val name = `object`.getString("name")
                            val email = `object`.getString("email")
                            val uid = `object`.getString("id")
                            val accType = "FSI"

                            //  CSI: Custom Sign In
                            //  GSI: Google Sign In
                            //  FSI: Facebook Sign In

                            if (!generals.preference.checkDB()) {
                                generals.fireBaseReff.child(uid).setValue(name)
                            }
                            generals.preference.setPreference(name, email, uid, true, accType)

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                val parameters = Bundle()
                parameters.putString(
                        "fields",
                        "id, name, email"
                )
                request.parameters = parameters
                request.executeAsync()
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                progressBar!!.visibility = View.GONE
            }

            override fun onError(error: FacebookException) {
                progressBar!!.visibility = View.GONE
            }
        })
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

    public override fun onStop () {
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
                }
                catch (e: ApiException) {
                    Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
                }
            }
            else {
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

                    //  Set Preference
                    setUserInfo()
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                    progressBar!!.visibility = View.GONE
                } else {
                    // If sign in fails, display a message to the user
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setUserInfo(){
        val acct = GoogleSignIn.getLastSignedInAccount(baseContext)
        if (acct != null) {
            val name = acct.displayName
            val email = acct.email
            val uid = auth.uid
            val accType = "GSI"

            //  CSI: Custom Sign In
            //  GSI: Google Sign In

            if(!generals.preference.checkDB()){
                generals.fireBaseReff.child(uid!!).setValue(name!!)
            }
            generals.preference.setPreference(name!!, email!!, uid!!, true, accType)
        }
    }
    /*<------ Signing In activity using Google sign in feature ----->*/

    /*<------ Signing In activity using facebook sign in feature ----->*/
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

        GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null, HttpMethod.DELETE) { LoginManager.getInstance().logOut() }.executeAsync()

        generals.preference.logOut()
    }
    /*<------ Signing In activity using facebook sign in feature ----->*/

    /* <----- Sign in Facility using Typical Email Password ----->*/
    private fun logIn(){

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

        if(et_password.text.toString().isEmpty()){
            et_password.error = "Please enter your password"
            et_password.setBackgroundResource(R.drawable.text_field_error)
            et_password.requestFocus()
            return
        }
        else {
            et_password.setBackgroundResource(R.drawable.text_field)
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

        //  If all data is valid then Let user log in
        auth.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(baseContext, MainActivity::class.java))
                        //  Set Preference
                        val uid = auth.uid
                        generals.preference.getName()?.let {
                            generals.preference.setPreference(
                                    it,
                                    et_email.text.toString(),
                                    uid!!, true, "CSI"
                            )
                        }
                        finish()
                        progressBar!!.visibility = View.GONE
                    } else {
                        // If sign in fails, display a message to the user.
                        progressBar!!.visibility = View.GONE
                        Toast.makeText(
                                baseContext, "Email or password is incorrect",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }
    /* <----- Sign in Facility using Typical Email Password ----->*/

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

//  tbZZclvovChGUrPWBgJxYMyKNZF2
//  tbZZclvovChGUrPWBgJxYMyKNZF2