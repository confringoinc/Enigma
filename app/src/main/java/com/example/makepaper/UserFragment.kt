package com.example.makepaper

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class UserFragment : Fragment() {
    companion object {
        lateinit var auth: FirebaseAuth
        fun newInstance(): UserFragment = UserFragment()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            val name = user.displayName
            val email = user.email
            view?.findViewById<TextView?>(R.id.tv_user_name)?.text = name
            view?.findViewById<TextView?>(R.id.tv_user_email)?.text = email
        }

        val logout: TextView? = view?.findViewById(R.id.tv_user_settings_logout)
        logout?.setOnClickListener { logout() }

        val info: TextView? = view?.findViewById(R.id.tv_user_settings_info)
        info?.setOnClickListener {
            startActivity(Intent(context, AboutActivity::class.java))
        }

        val delete: TextView? = view?.findViewById(R.id.tv_user_settings_delete)
        delete?.setOnClickListener { deleteAccount() }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun deleteAccount() {

        Dialog(requireContext())
                .apply {
                    setCancelable(true)
                    setContentView(R.layout.dialog)

                    val btnDelete = findViewById<TextView>(R.id.btn_delete)
                    val btnCancel = findViewById<TextView>(R.id.btn_cancel)
                    val deleteText = findViewById<TextView>(R.id.delete_text)
                    deleteText.text = getString(R.string.delete_account_msg)
                    deleteText.gravity = Gravity.CENTER

                    btnDelete.setOnClickListener {

                        auth = FirebaseAuth.getInstance()
                        val user = auth.currentUser

                        user!!.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                                context, "Account deleted successfully",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                        logout()
                                    }
                                }
                    }

                    btnCancel.setOnClickListener {
                        dismiss()
                    }
                    window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.transparent)))
                    show()
                }
    }

    private fun logout() {
        LoginActivity.auth.signOut()
        LoginActivity.googleSignInClient.signOut()

        startActivity(Intent(activity, LoginActivity::class.java))
    }
}