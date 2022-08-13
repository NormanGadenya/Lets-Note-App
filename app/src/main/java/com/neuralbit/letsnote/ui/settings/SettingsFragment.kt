package com.neuralbit.letsnote.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.SettingsFragmentBinding

class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsPref : SharedPreferences
    private val binding get() = _binding!!
    private lateinit var editor : SharedPreferences.Editor



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        settingsPref = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)!!
        editor = settingsPref.edit()
        val emptyTrashImmediately = settingsPref.getBoolean("EmptyTrashImmediately",false)
        val darkMode = settingsPref.getBoolean("darkMode",false)
        val radioPosition = settingsPref.getInt("radioPosition", R.id.fontDef)
        val emptyTrashSwitch = binding.emptyTrashSwitch
        val darkModeSwitch = binding.darkModeSwitch
        val fontRadioGroup = binding.radioGroup
        emptyTrashSwitch.isChecked = emptyTrashImmediately
        darkModeSwitch.isChecked = darkMode
        emptyTrashSwitch.setOnCheckedChangeListener { _,_ ->
            editor.putBoolean("EmptyTrashImmediately",!emptyTrashImmediately)
            editor.apply()
        }

        darkModeSwitch.setOnCheckedChangeListener { _, _ ->
            if (!darkMode){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            editor.putBoolean("darkMode",!darkMode)
            editor.apply()
        }
        fontRadioGroup.check(radioPosition)
        fontRadioGroup.setOnCheckedChangeListener { p0, p1 ->
            run {
                val chosen = container?.findViewById<RadioButton>(p1)
                val font = chosen?.text.toString()
                editor.putInt("radioPosition",p1)
                editor.putString("font",font)
                editor.apply()

            }
        }



        return root
    }


}