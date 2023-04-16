package com.example.mobilproje

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentHomeScreenBinding
import com.example.mobilproje.databinding.FragmentLoginBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class HomeScreen : Fragment() {
    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!
    lateinit var userName: String
    lateinit var activity : AppCompatActivity
    val database = FirebaseDatabase.getInstance().reference

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Home Page"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        userName = requireArguments().getString("userName").toString()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileSettingsButton.setOnClickListener {
            activity.supportActionBar?.title = "Public Profile Settings"
            findNavController().navigate(R.id.action_homeScreen_to_fragmentAddUser)

        }

        binding.listButton.setOnClickListener {
            val bundle = bundleOf("userName" to userName)
            activity.supportActionBar?.title = "User List"
            findNavController().navigate(R.id.action_homeScreen_to_listUserFragment,bundle)

        }

        binding.mapButton.setOnClickListener {
            activity.supportActionBar?.title = "Map"

            database.child("locations").child(userName).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val bundle = bundleOf("userName" to userName)
                    findNavController().navigate(R.id.action_homeScreen_to_mapFragment, bundle)
                } else {
                    val customToast = CustomToast(context)
                    customToast.showMessage("Wait for the issue to be processed", false)
                }
            }.addOnFailureListener { exception ->
            }
        }

    }

    override fun onResume() {
        super.onResume()
        activity.supportActionBar?.title = "Home Page"
    }


}