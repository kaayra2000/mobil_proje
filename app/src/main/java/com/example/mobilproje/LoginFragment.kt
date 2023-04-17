package com.example.mobilproje

import GraduatPerson
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*

import android.os.Bundle
import android.text.InputType
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
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
    private lateinit var auth: FirebaseAuth
    val database = FirebaseDatabase.getInstance().reference
    lateinit var password: String
    var loginFlag = true
    lateinit var activity: AppCompatActivity
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "Login"
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
        auth = FirebaseAuth.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        FirebaseApp.initializeApp(requireContext())
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

        binding.forgotPasswordText.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Forgot Password")
            builder.setMessage("Please enter your email address to reset your password:")

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            builder.setView(input)

            builder.setPositiveButton("Reset Password") { _, _ ->
                val email = input.text.toString()
                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter your email address", Toast.LENGTH_SHORT).show()
                } else {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toast.showMessage("Password reset email sent to $email", true)
                        } else {
                            toast.showMessage("Failed to send password reset email", false)
                        }
                    }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            val dialog = builder.create()
            dialog.show()

            // Klavyeyi açmak için bu satırı ekleyin
            input.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }



        lifecycleScope.launch {

            changeEnabled()
            binding.progressBar.visibility = View.VISIBLE

            try {
                val snapshot = database.child("users").child(sharedPrefs.getString("username", "").toString()).get().await()
                if(snapshot.exists()){
                    if(loginFlag){
                        var graduatPerson = initNullGradPerson()
                        snapshot.getValue(GraduatPerson::class.java)?.let {graduatPerson  = it }
                        graduatPerson.password = sharedPrefs.getString("password","").toString()
                        checkIfUserExists(graduatPerson)
                    }
                }
            } catch (e: Exception) {
                // handle exception
            } finally {
                binding.progressBar.visibility = View.GONE
                changeEnabled()
            }
        }





        binding.loginButton.setOnClickListener{


            userName = binding.userNameText.text.toString()
            password = binding.passwordText.text.toString()
            lifecycleScope.launch {
                 database.child("users").child(userName).get().await().getValue(GraduatPerson::class.java)?.let { user = it }
                    user.password = password
                    checkIfUserExists(user)
                }
            }


    }

    override fun onResume() {
        super.onResume()
        activity.supportActionBar?.title = "Login"
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


private fun checkIfUserExists(graduatPerson: GraduatPerson) {
    if (graduatPerson.email.length > 6 && graduatPerson.password.length >6){
    auth.signInWithEmailAndPassword(graduatPerson.email, graduatPerson.password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
                val user = auth.currentUser
                if(user!!.isEmailVerified){
                        FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                                return@addOnCompleteListener
                            }

                            // Get new FCM registration token
                            val token = task.result
                            database.child("tokens").child(graduatPerson.userName).setValue(token)
                        }

                    try {
                        database.child("users").child(graduatPerson.userName).setValue(graduatPerson)
                        toast.showMessage("Successfully Logged In", true)
                        editor.putString("username", graduatPerson.userName)
                        editor.putString("password", graduatPerson.password)
                        editor.putBoolean("loginFlag",true)
                        editor.apply()
                        val bundle = bundleOf("userName" to graduatPerson.userName)
                        findNavController().navigate(R.id.action_FirstFragment_to_profileSettings,bundle)
                    }catch (e: java.lang.Exception){
                        toast.showMessage("Error",false)
                    }

                }
                else{
                    user.sendEmailVerification()
                    toast.showMessage("Email not verified",false)
                }
            }
        else{
            toast.showMessage("Username and password not match",false)
        }
        }}
    else{
        toast.showMessage("Short mail or password",false)
    }
}

    private fun changeEnabled(){
        val bool = !binding.loginButton.isEnabled
        binding.loginButton.isEnabled = bool
        binding.passwordText.isEnabled = bool
        binding.forgotPasswordText.isClickable = bool
        binding.registerButton.isEnabled = bool
        binding.userNameText.isEnabled = bool
    }





}