package com.example.mobilproje

import GraduatPerson
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.PhoneNumberUtils
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentProfileBinding
import com.example.mobilproje.databinding.FragmentProfileSettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import situation
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProfileSettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    lateinit var user: GraduatPerson
    val database = FirebaseDatabase.getInstance().reference
    private var _binding: FragmentProfileSettingsBinding? = null
    private val binding get() = _binding!!
    lateinit var toast : CustomToast
    private var pickImage = 100
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var contextThis: Context
    lateinit var imageView: ImageView
    private var imageUri: Uri? = null
    lateinit var userNameEditText: EditText
    lateinit var passwordNameEditText: EditText
    lateinit var nameEditText: EditText
    lateinit var surNameEditText: EditText
    lateinit var eMailEditText: EditText
    lateinit var phoneNumberEditText: EditText
    lateinit var startDateEditText: EditText
    lateinit var endDateEditText: EditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileSettingsBinding.inflate(inflater, container, false)
        contextThis = requireContext()
        toast = CustomToast(context)

        binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.passwordText.transformationMethod = null
            } else {
                binding.passwordText.transformationMethod = PasswordTransformationMethod()
            }
        }

        binding.passwordText.transformationMethod = PasswordTransformationMethod()
        var userName = arguments?.getString("userName")
        imageView = binding.userPhoto
        initEditTexts()
        imageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        binding.startDateText.setOnClickListener {
            initDatePicker(binding.startDateText)
            binding.startDateText.setError(null)
        }

        binding.endDateText.setOnClickListener {
            initDatePicker(binding.endDateText)
            binding.endDateText.setError(null)
        }



        val getValue = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                userName?.let {
                    updateUser(dataSnapshot.child("users").child(it))

            }
        }

    }
        binding.updateButton.setOnClickListener{
            val email = binding.emailText.text.toString()
            val name = binding.nameText.text.toString()
            val surName = binding.surNameText.text.toString()
            val phoneNumber = binding.phoneNumberText.text.toString()
            val password = binding.passwordText.text.toString()
            val currUserName = binding.userNameText.text.toString()
            val endDate = binding.endDateText.text.toString()
            val startDate = binding.startDateText.text.toString()
            var byteArray : String? = ""
            binding.userPhoto.drawable?.let {
                byteArray= convertBitmap(binding.userPhoto.drawable)
            }


            val errorDetect = controlAllFields(email, name,surName, phoneNumber, startDate
                ,endDate, currUserName, password, situation.valueOf(binding.gradOption.selectedItem.toString()),
                byteArray)
            if (!errorDetect){
                return@setOnClickListener
            }


            val database = FirebaseDatabase.getInstance().reference
            if(!userName.equals(currUserName)){
                database.child("users").addListenerForSingleValueEvent(
                        object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {


                                var flag = true
                                var message = ""
                                for (i in snapshot.children) {
                                    val tmpEmail = i.child("email").getValue().toString()
                                    val tmpPhoneNumber = i.child("phoneNumber").getValue().toString()
                                    val tmpUserName = i.child("userName").getValue().toString()

                                    if ((tmpEmail == email || tmpPhoneNumber == phoneNumber || tmpUserName == currUserName) && tmpUserName != userName) {
                                        if (tmpEmail == email) message += "Email is exists\n"
                                        if (tmpPhoneNumber == phoneNumber) message += "Phone Number is exists\n"
                                        if (tmpUserName == currUserName) message += "Username is exists\n"
                                        toast.showMessage(message.dropLast(1),false)
                                        flag = false
                                        break
                                    }
                                }

                                if(flag){
                                    database.child("users").child(currUserName).setValue(user)
                                    val tmp = userName
                                    userName =  currUserName
                                    tmp?.let { it1 -> database.child("users").child(it1).removeValue() }
                                    toast.showMessage("Successfully Updated",true)
                                    editor.putString("username", currUserName)
                                    editor.putString("password", password)
                                    editor.apply()
                                }


                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        }
                )
            }else{
                database.child("users").child(currUserName).setValue(user)
                val toast = CustomToast(context)
                toast.setMessage("Successfully Updated")
                toast.setSuccessColor()
                toast.show()
                editor.putString("username", currUserName)
                editor.putString("password", password)
                editor.apply()
            }




        }

        database.addValueEventListener(getValue)
        database.addListenerForSingleValueEvent(getValue)

        return binding.root

}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }
    private fun updateUser(i : DataSnapshot){
        val situation = situation.valueOf((i.child("situation").getValue()).toString())
        user = GraduatPerson(
            email = i.child("email").getValue().toString(),
            name = i.child("name").getValue().toString(),
            surName = i.child("surName").getValue().toString(),
            password = i.child("password").getValue().toString(),
            phoneNumber = i.child("phoneNumber").getValue().toString(),
            startDate =  i.child("startDate").getValue().toString(),
            endDate =  i.child("endDate").getValue().toString(),
            situation = situation,
            userName = i.child("userName").getValue().toString(),
            photo = i.child("photo").getValue().toString()

        )
        initValues()
    }
    private fun initValues(){
        binding.gradOption.setSelection(user.situation.ordinal)
        binding.emailText.setText(user.email)
        binding.userNameText.setText(user.userName)
        binding.nameText.setText(user.name)
        binding.surNameText.setText(user.surName)
        binding.phoneNumberText.setText(user.phoneNumber)
        binding.startDateText.setText(user.startDate)
        binding.endDateText.setText(user.endDate)
        user.photo?.let { binding.userPhoto.setImageDrawable(decodeStringToDrawable(user.photo!!,contextThis )) }

        binding.passwordText.setText(user.password)


    }
    private fun controlAllFields(email : String?, name: String?, surName: String?,
                                 phoneNumber: String, startDate: String?, endDate: String?, userName: String,
                                 password: String, situation: situation,photo: String?): Boolean {
        var returnVal = true




        if (name?.length!! <4){
            nameEditText.setError("Incorrect Name")
            returnVal = false
        }


        if (surName?.length!! <2){
            surNameEditText.setError("Incorrect Surname")
            returnVal = false
        }

        if (email?.length!!<5 || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.toString()).matches()){
            eMailEditText.setError("Incorrect Mail")
            returnVal = false
        }

        if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
            phoneNumberEditText.setError("Incorrect Phone Number")
            returnVal = false
        }
        if(startDate?.length!!>1 && endDate?.length!!>1){
            val sdf = SimpleDateFormat("dd-MM-yyyy")
            val firstDate: Date = sdf.parse(startDate)
            val secondDate: Date = sdf.parse(endDate)
            user = GraduatPerson(name,surName,email,phoneNumber, startDate, endDate, situation,userName,password,
                photo)
            if (firstDate.after(secondDate) /*|| secondDate.after(today)*/){
                startDateEditText.setError("Incorrect Date")
                endDateEditText.setError("Incorrect Date")
                returnVal = false
            }
        }
        else{
            returnVal = false
            if(startDate?.length!!<3){
                startDateEditText.setError("Date is null")
            }
            if(startDate?.length!!<3){
                endDateEditText.setError("Date is null")
            }
        }


        if (!USER_NAME_REGEX.toRegex().matches(userName)){
            returnVal = false
            userNameEditText.setError("username must start with a letter and be at least 8 letters")
        }


        if (!PASSWORD_REGEX.toRegex().matches(password)){
            returnVal = false
            passwordNameEditText.setError("Password must contain at least one " +
                "uppercase letter, one lowercase letter and one number")
        }

        return returnVal
    }

    private fun initDatePicker(editText: EditText){
        val c = Calendar.getInstance()

        // on below line we are getting
        // our day, month and year.
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // on below line we are creating a
        // variable for date picker dialog.
        val datePickerDialog = activity?.let {
            DatePickerDialog(
                // on below line we are passing context.
                it,
                { view, year, monthOfYear, dayOfMonth ->
                    // on below line we are setting
                    // date to our edit text.
                    val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    editText.setText(dat)
                },
                // on below line we are passing year, month
                // and day for the selected date in our date picker.
                year,
                month,
                day
            )
        }
        // at last we are calling show
        // to display our date picker dialog.
        datePickerDialog?.show()
    }
    fun convertBitmap(drawable: Drawable?) : String? {
        if(drawable==null)
            return null
        val bitmap = (drawable as BitmapDrawable).bitmap   ?: return null
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)

    }

    private fun initEditTexts(){
        userNameEditText = binding.userNameText
        passwordNameEditText = binding.passwordText
        nameEditText = binding.nameText
        surNameEditText = binding.surNameText
        eMailEditText = binding.emailText
        phoneNumberEditText = binding.phoneNumberText
        startDateEditText = binding.startDateText
        endDateEditText = binding.endDateText
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