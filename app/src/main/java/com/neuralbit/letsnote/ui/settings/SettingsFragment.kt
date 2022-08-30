package com.neuralbit.letsnote.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.SettingsFragmentBinding

class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private lateinit var settingsPref : SharedPreferences
    private val binding get() = _binding!!
    private lateinit var editor : SharedPreferences.Editor
    private val TAG = "SETTINGS"
    private lateinit var mAuth : FirebaseAuth
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var oldUser : FirebaseUser? = null
    private lateinit var migrateProgressBar : ProgressBar
    private lateinit var mInterstitialAd: InterstitialAd



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        settingsPref = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)!!
        editor = settingsPref.edit()
        settingsViewModel.settingsFrag.value = true

        val emptyTrashImmediately = settingsPref.getBoolean("EmptyTrashImmediately",false)
        val darkMode = settingsPref.getInt("darkModePosition",R.id.defaultMode)
        val fontPosition = settingsPref.getInt("fontMultiplier",2)
        val radioPosition = settingsPref.getInt("radioPosition", R.id.fontDef)
        val emptyTrashSwitch = binding.emptyTrashSwitch
        val lightModeGroup = binding.dayNightRadioGroup
        val migrateCard = binding.cardView4
        val migrateTV = binding.backUpTV
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = resources.getString(R.string.interstitial)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        migrateProgressBar = binding.migrateProgress
        oldUser
        val siginBtn = binding.signInWithGoogleBtn
        mAuth = FirebaseAuth.getInstance()

        oldUser = mAuth.currentUser

        if (oldUser?.isAnonymous == true){

            migrateCard.visibility = VISIBLE
            migrateTV.visibility = VISIBLE
        }else{
            migrateCard.visibility = GONE
            migrateTV.visibility = GONE
        }
        createRequest()

        siginBtn.setOnClickListener { signInGoogle() }
        val fontRadioGroup = binding.radioGroup
        val fontSeekBar = binding.seekBar
        val dummyText = binding.fontSizeDummyT
        emptyTrashSwitch.isChecked = emptyTrashImmediately
        emptyTrashSwitch.setOnCheckedChangeListener { _,_ ->
            editor.putBoolean("EmptyTrashImmediately",!emptyTrashImmediately)
            editor.apply()
        }
        fontRadioGroup.check(radioPosition)
        fontRadioGroup.setOnCheckedChangeListener { _, p1 ->
            run {
                val chosen = container?.findViewById<RadioButton>(p1)
                val font = chosen?.text.toString()
                editor.putInt("radioPosition",p1)
                editor.putString("font",font)
                editor.apply()

            }
        }
        mAuth.addAuthStateListener {
            val currentUser = it.currentUser
            if (currentUser != null && !currentUser.isAnonymous){
                migrateCard.visibility = GONE
                migrateTV.visibility = GONE
            }
        }
        fontSeekBar.progress = fontPosition
        dummyText.textSize = 18f+((fontPosition-2)*2)

        fontSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                editor.putInt("fontMultiplier",p1)
                editor.apply()
                dummyText.textSize = 18f+((p1-2)*2)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        lightModeGroup.check(darkMode)
        lightModeGroup.setOnCheckedChangeListener { _, p1 ->
            run {
                val chosen = container?.findViewById<RadioButton>(p1)
                val mode = chosen?.text.toString()
                editor.putInt("darkModePosition",p1)
                editor.putString("mode",mode)
                editor.apply()
                when (mode) {
                    "Dark mode" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    "Light mode" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }

            }
        }

        return root
    }

    private fun createRequest() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        mGoogleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }
    }

    private fun signInGoogle() {
        if (mInterstitialAd.isLoaded){
            mInterstitialAd.show()
        }
        mInterstitialAd.adListener = object : AdListener(){
            override fun onAdClosed() {
                migrateProgressBar.visibility = VISIBLE
                val signInIntent = mGoogleSignInClient!!.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }

            override fun onAdFailedToLoad(p0: LoadAdError?) {
                migrateProgressBar.visibility = VISIBLE
                val signInIntent = mGoogleSignInClient!!.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val prevUser = mAuth.currentUser?.uid
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                val currentUser = it.user

                oldUser?.let { it1 ->
                    if (currentUser != null) {
                        settingsViewModel.migrateData(it1.uid , currentUser.uid )
                        migrateProgressBar.visibility = GONE

                    }
                }

                prevUser?.let { it1 -> currentUser?.uid?.let { it2 -> settingsViewModel.migrateData(oldUser = it1, newUser = it2) } }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchViewMenuItem = menu.findItem(R.id.search)
        val layoutViewBtn = menu.findItem(R.id.layoutStyle)
        val deleteButton = menu.findItem(R.id.trash)

        deleteButton.isVisible = false
        layoutViewBtn.isVisible = false
        searchViewMenuItem.isVisible = false
    }


}