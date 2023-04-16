package com.example.mobilproje

import GraduatPerson
import RequestDataClass
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import kotlinx.coroutines.suspendCancellableCoroutine
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentPartnerBinding
import com.example.mobilproje.databinding.FragmentProfileBinding
import com.example.mobilproje.databinding.FragmentProfileSettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.toast_message.view.*
import kotlinx.android.synthetic.main.user_item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.net.URLEncoder


class PartnerFragment : Fragment() {
    private var _binding: FragmentPartnerBinding? = null
    private val binding get() = _binding!!
    private var findPerson: FindPerson? = null
    private var findPersonParent: FindPerson? = null
    val database = FirebaseDatabase.getInstance().reference
    lateinit var status : Spinner
    lateinit var name : TextView
    lateinit var department : TextView
    lateinit var currClass  : TextView
    lateinit var stayDuration : TextView
    lateinit var distance : TextView
    private var gradPerson: GraduatPerson? = null
    private var gradPersonParent: GraduatPerson? = null
    lateinit var email : TextView
    lateinit var phoneNumber : TextView
    lateinit var customToast: CustomToast
    lateinit var image : ImageView
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var userName : String
    lateinit var parentUserName:String


    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userName = requireArguments().getString("userName").toString()
        parentUserName = requireArguments().getString("parentUserName").toString()
        customToast = CustomToast(context)
        _binding = FragmentPartnerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEditTexts()
        runBlocking {
            initTexts()
        }


        email.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email.text.toString()))
                putExtra(Intent.EXTRA_SUBJECT, status.selectedItem.toString())
            }
            startActivity(Intent.createChooser(intent, "Select E-Mail App"))
        }



        phoneNumber.setOnClickListener {
            val number = "+90" + phoneNumber.text
            val packageName = "com.whatsapp"
            val uri = Uri.parse("https://wa.me/$number")

            // Check if WhatsApp is installed
            val packageManager = requireActivity().packageManager
            val isWhatsappInstalled = try {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

            // If WhatsApp is installed, open it with the phone number, otherwise open web browser
            val intent = if (isWhatsappInstalled) {
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(packageName)
                }
            } else {
                Intent(Intent.ACTION_VIEW, uri)
            }
            startActivity(intent)
        }





        binding.sendRequestButton.setOnClickListener {
            if (parentUserName == userName) {
                customToast.showMessage("You can't request yourself", false)
                return@setOnClickListener
            }
            val userNameRef = database.child("persons").child(parentUserName)

            userNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        customToast.showMessage("You hasn't got a profile", false)
                    }else{
                        val request = RequestDataClass(parentUserName, userName, false,
                        gradPersonParent!!.name)
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Are you sure?")
                        builder.setMessage("Do you want to send a request to $parentUserName?")
                        builder.setPositiveButton("Yes") { dialog, which ->
                            database.child("requests").child(parentUserName).setValue(request)
                            customToast.showMessage("Request sent", true)
                            binding.sendRequestButton.isEnabled = false
                        }
                        builder.setNegativeButton("No") { dialog, which ->
                            customToast.showMessage("Request cancelled", false)
                        }
                        builder.show()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        }



    }



    private fun initEditTexts(){
        name = binding.tvName
        currClass = binding.tvClass
        department = binding.tvDepartment
        status = binding.tvStatus
        distance = binding.tvDistance
        stayDuration = binding.tvStayDuration
        email = binding.tvEmailInfo
        phoneNumber = binding.tvPhoneInfo
        image = binding.profilePhoto
        status.isEnabled = false

    }


    fun convertStringToBitmap(encodedString: String): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message?.let { Log.d("Error", it) }
            null
        }
    }


    private suspend fun initTexts() {
        gradPerson = database.child("users").child(userName).get().await().getValue(GraduatPerson::class.java)
        findPerson = database.child("persons").child(userName).get().await().getValue(FindPerson::class.java)
        gradPersonParent = database.child("users").child(parentUserName).get().await().getValue(GraduatPerson::class.java)
        findPersonParent = database.child("persons").child(parentUserName).get().await().getValue(FindPerson::class.java)
        gradPerson?.let {
            if (it.photo != null) {
                image.setImageBitmap(convertStringToBitmap(it.photo!!))
            }
            phoneNumber.text = it.phoneNumber
            name.text = it.name
            email.text = it.email
        }
        findPerson?.let {
            department.text = (it.department)
            distance.text = (it.distance)
            val selectedIndex = LookingStatus.valueOf(it.lookingStatus.toString()).ordinal
            status.setSelection(selectedIndex)
            currClass.text = (it.curClass)
            stayDuration.text = (it.duration)
        }

    }






}