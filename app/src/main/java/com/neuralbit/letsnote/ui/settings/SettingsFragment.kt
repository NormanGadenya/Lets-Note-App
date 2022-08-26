package com.neuralbit.letsnote.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.SettingsFragmentBinding

class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private lateinit var settingsPref : SharedPreferences
    private val binding get() = _binding!!
    private lateinit var editor : SharedPreferences.Editor
    private val TAG = "SETTINGS"



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
        fontSeekBar.progress = fontPosition
        dummyText.textSize = 32f+((fontPosition-2)*4)

        fontSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                editor.putInt("fontMultiplier",p1)
                editor.apply()
                dummyText.textSize = 32f+((p1-2)*4)
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