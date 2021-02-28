package com.example.makepaper

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.makepaper.R.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    var isRotate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        val homeFragment = HomeFragment.newInstance()
        loadFragment(homeFragment)

        val bottomNavigation: BottomNavigationView = findViewById(id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val fabAddQuestions:FloatingActionButton = findViewById(id.fab_add_questions)
        val fabScan:FloatingActionButton = findViewById(id.fab_scan)
        val fabAddPapers:FloatingActionButton = findViewById(id.fab_add_papers)
        fabAddQuestions.visibility = View.GONE
        fabScan.visibility = View.GONE
        fabAddPapers.visibility = View.GONE
        fab.setOnClickListener {
            if(isRotate) {
                rotateFabBackward()
                fabAddQuestions.hide()
                fabScan.hide()
                fabAddPapers.hide()
            }
            else {
                rotateFabForward()
                fabAddQuestions.show()
                fabScan.show()
                fabAddPapers.show()
            }
            isRotate = !isRotate
        }

        fabAddQuestions.setOnClickListener {
            startActivity(Intent(applicationContext, AddQuestion::class.java))
        }

        fabAddPapers.setOnClickListener {
            startActivity(Intent(applicationContext, AddPaper::class.java))
        }

        //  Initialize auth var
        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun rotateFabForward() {
        ViewCompat.animate(fab).rotation(45.0F).withLayer().setDuration(300L).start()
    }

    private fun rotateFabBackward() {
        ViewCompat.animate(fab).rotation(0F).withLayer().setDuration(300L).start()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            id.nav_home -> {
                val homeFragment = HomeFragment.newInstance()
                loadFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }

            id.nav_data -> {
                val dataFragment = DataFragment.newInstance()
                loadFragment(dataFragment)
                return@OnNavigationItemSelectedListener true
            }

            id.nav_file -> {
                val fileFragment = FileFragment.newInstance()
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

    fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(id.fragment_container_view, fragment)
        transaction.commit()
    }
}