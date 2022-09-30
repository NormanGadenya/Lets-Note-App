package com.neuralbit.letsnote.ui.signIn

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.neuralbit.letsnote.R

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