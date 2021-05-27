package com.confringoinc.enigma

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.confringoinc.enigma.R.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        val homeFragment = HomeFragment.newInstance()
        loadFragment(homeFragment)

        val bottomNavigation: BottomNavigationView = findViewById(id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //  Initialize auth var
        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                id.nav_home -> {
                    val homeFragment = HomeFragment.newInstance()
                    loadFragment(homeFragment)
                    return@OnNavigationItemSelectedListener true
                }

                id.nav_data -> {
                    val dataFragment = QuestionsFragment.newInstance()
                    loadFragment(dataFragment)
                    return@OnNavigationItemSelectedListener true
                }

                id.nav_file -> {
                    val fileFragment = PapersFragment.newInstance()
                    loadFragment(fileFragment)
                    return@OnNavigationItemSelectedListener true
                }

                id.nav_user -> {
                    val userFragment = UserFragment.newInstance()
                    loadFragment(userFragment)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(id.fragment_container_view, fragment)
        transaction.commit()
    }
}