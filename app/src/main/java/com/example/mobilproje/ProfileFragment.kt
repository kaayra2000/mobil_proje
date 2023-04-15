package com.example.mobilproje

import GraduatPerson
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentProfileBinding
import com.example.mobilproje.databinding.FragmentProfileSettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

// TODO: Rename parameter arguments, choose names that match



class ProfileFragment : Fragment() {
    lateinit var userName : String
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    val database = FirebaseDatabase.getInstance().reference
    lateinit var user: GraduatPerson
    private var backPressedTime = Long.MIN_VALUE
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor


    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
    }





    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val bundle = bundleOf("userName" to userName)
                findNavController().navigate(R.id.action_profileSettings_to_profileFragment, bundle)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userName = arguments?.getString("userName").toString()
        var toast = CustomToast(context)



        binding.navigateButton.setOnClickListener {

            findNavController().navigate(R.id.action_profile_to_homeScreen)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime < 2000 + backPressedTime) {
                    requireActivity().finish()
                } else {
                    backPressedTime = currentTime
                    toast.showMessage("Press again",true)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.exitButton.setOnClickListener {
            editor.putString("username", "")
            editor.putString("password", "")
            editor.putBoolean("loginFlag", false)
            editor.apply()
            findNavController().navigate(R.id.action_profileSettings_to_FirstFragment)
        }

        val getValue = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userName?.let {
                    updateUser(dataSnapshot.child("users").child(it))
                    initValues()
                }
            }
        }
        database.addValueEventListener(getValue)
        database.addListenerForSingleValueEvent(getValue)

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        userName = sharedPrefs.getString("username","").toString()
    }
    private fun updateUser(i : DataSnapshot){
        try{
        user = GraduatPerson(
            email = i.child("email").getValue().toString(),
            name = i.child("name").getValue().toString(),
            surName = i.child("surName").getValue().toString(),
            password = i.child("password").getValue().toString(),
            phoneNumber = i.child("phoneNumber").getValue().toString(),
            startDate =  i.child("startDate").getValue().toString(),
            endDate =  i.child("endDate").getValue().toString(),
            userName = i.child("userName").getValue().toString(),
            photo = i.child("photo").getValue().toString()

        )
        initValues()}catch(e : Exception){
            e.printStackTrace()
        }

    }
    private fun initValues(){

        user?.let {
            binding.nameText.setText(user.name)
            binding.surNameText.setText(user.surName)
            binding.startDateText.setText(user.startDate)
            binding.endDateText.setText(user.endDate)
            binding.userPhoto.setImageDrawable(context?.let { user.photo?.let { it1 ->
                decodeStringToDrawable(
                    it1, it)
            } })  }

    }

    fun decodeStringToDrawable(encodedString: String, context: Context): Drawable? {
        try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            return BitmapDrawable(context.resources, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }




}