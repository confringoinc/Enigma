package com.example.makepaper

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class UserFragment : Fragment() {
    companion object {
        lateinit var auth: FirebaseAuth
        fun newInstance(): UserFragment = UserFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            val name = generals.preference.getName()
            val email = generals.preference.getEmail()
            view?.findViewById<TextView?>(R.id.tv_user_name)?.text = name
            view?.findViewById<TextView?>(R.id.tv_user_email)?.text = email
        }

        val logout: TextView? = view?.findViewById(R.id.tv_user_settings_logout)
        logout?.setOnClickListener { logout() }
        return view
    }

    private fun logout() {
        val accType = generals.preference.getAccountType()!!
        if (accType == generals.CSI) {
            customLogOut()
        }
        else {
            googleLogOut()
        }

        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun customLogOut(){
        generals.preference.logOut()
    }

    private fun googleLogOut(){
        //  First sign out from google account
        LoginActivity.auth.signOut()
        LoginActivity.googleSignInClient.signOut()

        //  Then from SharedPreferences
        generals.preference.logOut()
    }
}