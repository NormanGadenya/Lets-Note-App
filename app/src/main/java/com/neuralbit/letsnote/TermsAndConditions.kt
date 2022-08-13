package com.neuralbit.letsnote

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermsAndConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        val tCTV = findViewById<TextView>(R.id.termsAndConditionTV)
        tCTV.setText("Terms and conditions")
    }
}