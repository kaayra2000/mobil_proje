package com.example.mobilproje

import GraduatPerson
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.databinding.BindingAdapter
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private var users: ArrayList<GraduatPerson> = ArrayList()
    private lateinit var user: GraduatPerson
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var userName: String
    val mAuth = FirebaseAuth.getInstance()
    val currentUser = mAuth.currentUser
    lateinit var toast: CustomToast
    val database = FirebaseDatabase.getInstance().reference
    lateinit var password: String
    var loginFlag = true
    private val binding get() = _binding!!

    var backPressedTime = Long.MIN_VALUE


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

        database.child("users").child(sharedPrefs.getString("username", "").toString()).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot.child("password").getValue().toString().equals(sharedPrefs?.getString("password", "").toString())
                            && loginFlag){

                            val bundle = bundleOf("userName" to sharedPrefs.getString("username", "").toString())
                            findNavController().navigate(R.id.action_FirstFragment_to_profileSettings,bundle)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }


            }
        )
        /*val getValue = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                for(i in dataSnapshot.child("users").children) {
                    var situation = situation.valueOf((i.child("situation").getValue()).toString())
                    users.add(
                        GraduatPerson(
                            email = i.child("email").getValue().toString(),
                            name = i.child("name").getValue().toString(),
                            surName = i.child("surName").getValue().toString(),
                            password = i.child("password").getValue().toString(),
                            phoneNumber = i.child("phoneNumber").getValue().toString(),
                            startDate = i.child("startDate").getValue().toString(),
                            endDate = i.child("endDate").getValue().toString(),
                            situation = situation,
                            userName = i.child("userName").getValue().toString(),
                            photo = i.child("userName").getValue().toString()

                        )
                    )

                }
            }

        }*/

        database.child("users").child(userName).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val i = snapshot
                        var situation = situation.valueOf((i.child("situation").getValue()).toString())
                        user = (
                            GraduatPerson(
                                email = i.child("email").getValue().toString(),
                                name = i.child("name").getValue().toString(),
                                surName = i.child("surName").getValue().toString(),
                                password = i.child("password").getValue().toString(),
                                phoneNumber = i.child("phoneNumber").getValue().toString(),
                                startDate = i.child("startDate").getValue().toString(),
                                endDate = i.child("endDate").getValue().toString(),
                                situation = situation,
                                userName = i.child("userName").getValue().toString(),
                                photo = i.child("userName").getValue().toString()

                            )
                        )
                    } else {
                        // kullanıcının bilgileri snapshot'ta mevcut değil
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // veritabanına erişilemedi, hata oluştu
                }
            }
        )
        binding.loginButton.setOnClickListener{


            userName = binding.userNameText.text.toString()
            password = binding.passwordText.text.toString()

            val user = control(userName, password)
            if(user!=null){
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

    @SuppressLint("SuspiciousIndentation")
    private fun control(userName: String, password: String): GraduatPerson?{
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
}