package com.example.mobilproje

import GraduatPerson
import android.annotation.SuppressLint

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resumeWithException


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private var user =  initNullGradPerson()
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var userName: String
    lateinit var toast: CustomToast
    val database = FirebaseDatabase.getInstance().reference
    lateinit var password: String
    var loginFlag = true
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toast = CustomToast(context)
        binding.registerButton.setOnClickListener {
            editor.putString("username", binding.userNameText.text.toString())
            editor.putString("password", binding.passwordText.text.toString())
            editor.putBoolean("loginFlag", false)
            editor.apply()
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        loginFlag = sharedPrefs.getBoolean("loginFlag", false)
        binding.userNameText.setText(sharedPrefs.getString("username", "").toString())
        binding.passwordText.setText(sharedPrefs.getString("password", "").toString())
        userName = binding.userNameText.text.toString()
        password = binding.passwordText.text.toString()

        lifecycleScope.launch {
            binding.loginButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            val snapshot = database.child("users").child(sharedPrefs.getString("username", "").toString()).get().await()
            if(snapshot.exists()){
                if(snapshot.child("password").getValue().toString().equals(sharedPrefs?.getString("password", "").toString())
                    && loginFlag){

                    val bundle = bundleOf("userName" to sharedPrefs.getString("username", "").toString())
                    findNavController().navigate(R.id.action_FirstFragment_to_profileSettings,bundle)
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.loginButton.isEnabled = true
        }




        binding.loginButton.setOnClickListener{


            userName = binding.userNameText.text.toString()
            password = binding.passwordText.text.toString()
            lifecycleScope.launch {
                getUserData()
                val currUser = control(userName, password)
                if(currUser!=null){
                    toast.showMessage("Successfully Logged In", true)
                    editor.putString("username", userName)
                    editor.putString("password", password)
                    editor.putBoolean("loginFlag",true)
                    editor.apply()
                    val bundle = bundleOf("userName" to userName)
                    findNavController().navigate(R.id.action_FirstFragment_to_profileSettings,bundle)
                    toast.show()
                }
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun control(userName: String, password: String): GraduatPerson?{
        val i = user
        if(i.userName.equals(userName)){
            if(i.password.equals(password)){
                return i
            }

            else
                toast.showMessage("Username and Password don't match",false)
                return null
        }
        toast.showMessage("Username not found",false)
        return null
    }
    private fun initNullGradPerson() :  GraduatPerson{
        return GraduatPerson(
            email = "",
            name = "",
            surName = "",
            password = "",
            phoneNumber = "",
            startDate = "",
            endDate ="",
            userName = "",
            photo = ""

        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getUserData(): GraduatPerson = suspendCancellableCoroutine { continuation ->
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val i = dataSnapshot
                    user = GraduatPerson(
                        email = i.child("email").getValue().toString(),
                        name = i.child("name").getValue().toString(),
                        surName = i.child("surName").getValue().toString(),
                        password = i.child("password").getValue().toString(),
                        phoneNumber = i.child("phoneNumber").getValue().toString(),
                        startDate = i.child("startDate").getValue().toString(),
                        endDate = i.child("endDate").getValue().toString(),
                        userName = i.child("userName").getValue().toString(),
                        photo = i.child("photo").getValue().toString()
                    )
                    continuation.resume(user) {}
                } else {
                    user = initNullGradPerson()
                    continuation.resume(user) {}
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                continuation.resumeWithException(databaseError.toException())
            }
        }

        database.child("users").child(userName).addListenerForSingleValueEvent(valueEventListener)

        continuation.invokeOnCancellation {
            database.child("users").child(userName).removeEventListener(valueEventListener)
        }
    }







}