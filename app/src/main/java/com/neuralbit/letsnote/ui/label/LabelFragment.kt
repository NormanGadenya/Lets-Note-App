package com.neuralbit.letsnote.ui.label

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neuralbit.letsnote.R

class LabelFragment : Fragment() {

    companion object {
        fun newInstance() = LabelFragment()
    }

    private lateinit var viewModel: LabelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.label_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LabelViewModel::class.java)
        // TODO: Use the ViewModel
    }

}