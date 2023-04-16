package com.example.mobilproje

import GraduatPerson
import RequestDataClass
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentAcceptRequestBinding
import com.example.mobilproje.databinding.FragmentLoginBinding
import com.example.mobilproje.databinding.FragmentPartnerBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AcceptRequestFragment : Fragment() {
    private var _binding: FragmentAcceptRequestBinding? = null
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
    private var req : RequestDataClass? = null
    private var gradPerson: GraduatPerson? = null
    private var gradPersonParent: GraduatPerson? = null
    lateinit var email : TextView
    lateinit var phoneNumber : TextView
    lateinit var customToast: CustomToast
    lateinit var image : ImageView
    lateinit var userName : String
    lateinit var parentUserName:String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        customToast = CustomToast(context)
        _binding = FragmentAcceptRequestBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEditTexts()
        val scope = CoroutineScope(Dispatchers.Main + Job())

        scope.launch {
            initTexts()
        }

        binding.acceptRequestButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to accept this request?")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    findPerson?.lookingStatus = LookingStatus.NOT_LOOKING
                    findPersonParent?.lookingStatus = LookingStatus.NOT_LOOKING
                    database.child("persons").child(userName).setValue(findPerson)
                    database.child("persons").child(parentUserName).setValue(findPersonParent)
                    req?.isOkey = true
                    database.child("requests").child(userName).setValue(req)
                    customToast.showMessage("Successful",true)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.declineRequestButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to reject this request?")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    database.child("requests").child(userName).removeValue()
                    customToast.showMessage("Successfuly Rejected",false)
                    findNavController().navigateUp()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

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


    }


    private fun initEditTexts(){
        name = binding.tvName
        currClass = binding.tvClass
        department = binding.tvDepartment
        status = binding.tvStatus
        distance = binding.tvDistance
        stayDuration = binding.tvStayDuration
        email = binding.tvEmailInfo
        arguments?.let {
            userName = it.getString("senderUserName").toString()
        }
        arguments?.let {
            parentUserName = it.getString("applierUserName").toString()
        }
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
        req = database.child("requests").child(userName).get().await().getValue(RequestDataClass::class.java)
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