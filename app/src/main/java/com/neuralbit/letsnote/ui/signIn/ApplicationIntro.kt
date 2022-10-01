package com.neuralbit.letsnote.ui.signIn

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.google.firebase.auth.FirebaseAuth
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.ui.main.MainActivity

class ApplicationIntro : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val settingsPref : SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val useLocalStorage = settingsPref.getBoolean("useLocalStorage",false)
        if (firebaseUser!=null || useLocalStorage){
            val intent = Intent(this@ApplicationIntro, MainActivity::class.java)
            startActivity(intent)
        }
        addSlide(AppIntroFragment.createInstance(
            title = "Welcome",
            description = resources.getString(R.string.welcome_text),
            backgroundColorRes= R.color.Honeydew_Dark,
            imageDrawable = R.drawable.next_steps_rafiki,
        ))

        addSlide(
            AppIntroFragment.createInstance(
                imageDrawable = R.drawable.memory_storage_cuate__1_,
                title = resources.getString(R.string.never_lose_a_note),
                backgroundColorRes= R.color.Apricot_Dark,
                description = resources.getString(R.string.never_lose_a_note_desc)
        ))

        addSlide(AppIntroFragment.createInstance(
            title = resources.getString(R.string.need_privacy),
            imageDrawable = R.drawable.fingerprint_rafiki,
            backgroundColorRes= R.color.Honeydew_Dark,
            description = resources.getString(R.string.need_privacy_desc)
        ))

        addSlide(AppIntroFragment.createInstance(
            title = resources.getString(R.string.scared_forgetting) ,
            imageDrawable = R.drawable.reminders_pana,
            backgroundColorRes= R.color.Wild_orchid_Dark,
            description = resources.getString(R.string.scared_forgetting_desc)
        ))


        isColorTransitionsEnabled = true

    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        val i = Intent(applicationContext,SignInActivity::class.java)
        startActivity(i)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val i = Intent(applicationContext,SignInActivity::class.java)
        startActivity(i)
        finish()
    }
}