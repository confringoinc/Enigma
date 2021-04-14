package com.example.makepaper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_view_paper.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ViewPaper : AppCompatActivity() {

    val TAG = "ViewPaper"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_paper)

        myWebView.webViewClient = WebViewClient()
        makeChange()

        myWebView.loadUrl("file:///android_asset/index.html")
    }

    private fun makeChange(){
        val inpStream = assets.open("index.html")
        Log.i(TAG, "File: " + inpStream)
        val document = Jsoup.parse(inpStream, "UTF-8", "http://example.com/")
        Log.i(TAG, "Tile = " + document.title())

        val heading = document.getElementById("heading")
        Log.i(TAG, "Heading: ${heading.text()} " )
        val div = document.getElementById("sampleText")
        div.text("This is sample text")
        div.prependText("Hey")
        div.appendText(" Their")
    }

}