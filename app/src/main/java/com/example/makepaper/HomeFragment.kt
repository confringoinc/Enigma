package com.example.makepaper

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    companion object {
        lateinit var auth: FirebaseAuth
        fun newInstance(): HomeFragment = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let {
            val name = generals.preference.getName()
            val firstName = name?.split(" ")
            view?.findViewById<TextView?>(R.id.tv_name)?.text = firstName?.get(0)
        }

        view.add_question.setOnClickListener {
            startActivity(Intent(view.context, AddQuestion::class.java))
        }

        return view
    }
}
