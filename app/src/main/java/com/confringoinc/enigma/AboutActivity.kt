package com.confringoinc.enigma

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_add_paper.btn_back

class AboutActivity : AppCompatActivity() {
    val TAG = "AboutActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        btn_back.setOnClickListener {
            onBackPressed()
        }

//        tv_about_contact.setOnClickListener {
//            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+918866470217"))
//            startActivity(intent)
//        }

        tv_about_mail.setOnClickListener {
            val to: String = "confringoinc@gmail.com"

            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            email.type = "message/rfc822"
            startActivity(Intent.createChooser(email, "Choose an Email client"))
        }

        tv_about_site.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://confringoinc.github.io/Enigma/"))
            startActivity(browserIntent)
        }
    }
}