package com.example.mobilproje

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentHomeScreenBinding
import com.example.mobilproje.databinding.FragmentLoginBinding


class HomeScreen : Fragment() {
    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_fragmentAddUser)

        }

        binding.listButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_listUserFragment)

        }
    }



}