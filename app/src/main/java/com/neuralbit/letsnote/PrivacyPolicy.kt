package com.neuralbit.letsnote

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        val webView = findViewById<WebView>(R.id.privacyPolicyWV)
        webView.loadUrl("https://pages.flycricket.io/let-s-note/privacy.html")
        supportActionBar?.title = "Privacy Policy"
        webView.webViewClient = WebViewClient()
    }
}