package com.neuralbit.letsnote.ui.signIn

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.neuralbit.letsnote.R


class TermsAndConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        val webView = findViewById<WebView>(R.id.termsAndConditionWV)
        webView.loadUrl("https://www.app-privacy-policy.com/live.php?token=tstFlldVqoQF3FCgRzhhY1h9awm3uGgc")
        supportActionBar?.title = "Terms and Conditions"
        webView.webViewClient = WebViewClient()
    }
}