package com.confringoinc.enigma

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_on_boarding.*

class OnBoardingActivity : AppCompatActivity() {
    private lateinit var onBoardingAdapter: OnBoardingAdapter
    private var onBoardingList = ArrayList<OnBoardingItem>()

    companion object {
        const val prevStarted: String = "prevStarted"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        val sharedPreferences: SharedPreferences =
            getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(prevStarted, false)) {
            finish()
        }

        setOnBoardingItem()
        vp_on_boarding.adapter = onBoardingAdapter
        setOnBoardingIndicator()
        setCurrentOnBoardingIndicators(0)

        vp_on_boarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentOnBoardingIndicators(position)
            }
        })

        btn_on_boarding.setOnClickListener {
            if (vp_on_boarding.currentItem + 1 < onBoardingAdapter.itemCount) {
                vp_on_boarding.currentItem = vp_on_boarding.currentItem + 1
            } else {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putBoolean(prevStarted, true)
                editor.apply()
            }
        }
    }

    private fun setOnBoardingIndicator() {
        val indicators: Array<ImageView?> = arrayOfNulls(onBoardingAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            50, ViewGroup.LayoutParams.MATCH_PARENT
        )

        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext, R.drawable.ic_dot_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            lo_indicators.addView(indicators[i])
        }
    }

    private fun setCurrentOnBoardingIndicators(index: Int) {
        val childCount = lo_indicators.childCount
        for (i in 0 until childCount) {
            val imageView: ImageView = lo_indicators.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_dot_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_dot_inactive
                    )
                )
            }
        }
        if (index == onBoardingAdapter.itemCount - 1) {
            btn_on_boarding.text = getString(R.string.get_started)
        } else {
            btn_on_boarding.text = getString(R.string.next)
        }
    }

    private fun setOnBoardingItem() {
        onBoardingList.add(
            OnBoardingItem(
                R.drawable.ic_add_paper,
                "Generate exam level question papers at your fingertip",
                "This App consists of different Templates, so you don't need to change anything at a component level, unless you want to customize the default styling"
            )
        )
        onBoardingList.add(
            OnBoardingItem(
                R.drawable.ic_paper_sent,
                "Share exam ready question paper with just few clicks!",
                "It uses hash encryption technique to protect your question papers with password so you can share papers without worrying about privacy"
            )
        )
        onBoardingList.add(
            OnBoardingItem(
                R.drawable.ic_all_papers,
                "Store your question papers and questions on cloud storage",
                "Enigma stores all of your work on real time cloud storage that means you can access your data and question papers anytime anywhere"
            )
        )
        onBoardingAdapter = OnBoardingAdapter(applicationContext, onBoardingList)
    }
}