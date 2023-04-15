package com.example.mobilproje

import GraduatPerson
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentAddUserBinding
import com.example.mobilproje.databinding.FragmentHomeScreenBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class FragmentAddUser : Fragment() {
    private var _binding: FragmentAddUserBinding? = null
    private val binding get() = _binding!!
    lateinit var sharedPrefs : SharedPreferences
    private var pickImage = 100
    lateinit var toast : CustomToast
    lateinit var editor : SharedPreferences.Editor
    lateinit var userName : String
    lateinit var name : TextView
    lateinit var department : EditText
    lateinit var currClass  : EditText
    lateinit var stayDuration : EditText
    lateinit var distance : EditText
    var findPerson : FindPerson? = null
    lateinit var status : Spinner
    lateinit var gradPerson: GraduatPerson
    lateinit var email : TextView
    lateinit var phoneNumber : TextView
    lateinit var image : ImageView
    var imageBitmap : String? = null
    var selectedIndex = 1
    val database = FirebaseDatabase.getInstance().reference


    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddUserBinding.inflate(inflater, container, false)
        userName = sharedPrefs.getString("username","").toString()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toast = CustomToast(context)
        initEditTexts()
        CoroutineScope(Dispatchers.Main).launch {
            setPerson()
        }




        status.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing to do
            }
        }
        binding.saveButton.setOnClickListener {
            val controlVal = controlAllFields()
            if (controlVal) {
                val scope = CoroutineScope(Job() + Dispatchers.Main)
                scope.launch {
                    savePerson()
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    database.child("persons").child(userName).removeValue()
                    clearAllFields()
                }
                .setNegativeButton("No") { dialog, id ->
                    // İptal işlemleri burada yapılacak
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }


        image.setOnClickListener {
            val options = arrayOf<CharSequence>("Galeri", "Kamera")
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Resim Seçin")
            builder.setItems(options) { dialog, item ->
                when {
                    options[item] == "Galeri" -> {
                        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                        startActivityForResult(gallery, pickImage)
                    }
                    options[item] == "Kamera" -> {
                        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(takePicture, pickImage)
                    }
                }
            }
            builder.show()
        }

    }



    private fun controlAllFields(): Boolean {
        var returnVal = true

        if (department.text.toString()?.length!! <2){
            department.setError("Incorrect Department")
            returnVal = false
        }

        if (currClass.text.toString().toIntOrNull() == null){
            currClass.setError("Class should be a number")
            returnVal = false
        }

        if (distance.text.toString().toIntOrNull() == null){
            distance.setError("Distance should be a number")
            returnVal = false
        }
        return returnVal
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

        val scope = CoroutineScope(Job() + Dispatchers.Main)

        scope.launch {
            gradPerson = database.child("users").child(userName).get().await().getValue(GraduatPerson::class.java)!!

            name.setText(gradPerson.name)
            email.setText(gradPerson.email)
            phoneNumber.setText(gradPerson.phoneNumber)
            image.setImageBitmap(convertStringToBitmap(gradPerson.photo.toString()))
        }

    }


    private fun savePerson() {
        findPerson = FindPerson(
            department = department.text.toString(), curClass = currClass.text.toString(),
            duration = stayDuration.text.toString(), distance = distance.text.toString(),
            lookingStatus = LookingStatus.values()[selectedIndex], userName = userName)

        gradPerson.photo = imageBitmap

        val alert = AlertDialog.Builder(context)
        alert.setTitle("Confirmation")
        alert.setMessage("Are you sure you want to update your information?")
        alert.setPositiveButton("Yes") { _, _ ->
            database.child("persons").child(userName).setValue(findPerson)
            database.child("users").child(userName).setValue(gradPerson)
            toast.showMessage("Successfully Updated", true)
        }
        alert.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                pickImage -> {
                    // Kamera veya galeriden resim seçildi
                    val imageUri = data?.data
                    if (imageUri != null) {
                        image.setImageURI(imageUri)
                        this.imageBitmap = convertBitmap(imageUri)!!

                    } else {
                        val extras = data?.extras
                        val imageBitmap = extras?.get("data") as Bitmap
                        this.imageBitmap = convertBitmapToString(imageBitmap)
                        image.setImageBitmap(imageBitmap)
                    }
                }
            }
        }
    }

    fun convertBitmap(imageUri: Uri) : String? {
        val inputStream = context?.contentResolver?.openInputStream(imageUri)
        val buffer = ByteArrayOutputStream()

        val bufferSize = 1024
        val bufferArray = ByteArray(bufferSize)

        var len = 0
        while (inputStream?.read(bufferArray, 0, bufferSize).also { len = it!! } != -1) {
            buffer.write(bufferArray, 0, len)
        }

        return Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT)


    }

    private suspend fun initTexts() {
        findPerson?.let {
            withContext(Dispatchers.Main) {
                name.setText(gradPerson.name)
                currClass.setText(it.curClass)
                department.setText(it.department)
                selectedIndex = LookingStatus.valueOf(it.lookingStatus.toString()).ordinal
                status.setSelection(selectedIndex)
                distance.setText(it.distance)
                stayDuration.setText(it.duration)
                email.setText(gradPerson.email)
                phoneNumber.setText(gradPerson.phoneNumber)
                imageBitmap = gradPerson.photo.toString()
                image.setImageBitmap(gradPerson.photo?.let { it1 -> convertStringToBitmap(it1) })
                if(gradPerson.photo == null || gradPerson.photo!!.length < 5){
                    imageBitmap = ppString()
                    image.setImageBitmap(convertStringToBitmap(imageBitmap!!))
                }

            }
        }
    }

    private suspend fun setPerson() {
        val database = FirebaseDatabase.getInstance().reference
        val dataSnapshot = database.child("persons").child(userName).get().await()
        findPerson = dataSnapshot.getValue(FindPerson::class.java)
        initTexts()
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

    fun ppString(): String{
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.baseline_account_box_24_black, null)
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun convertBitmapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun clearAllFields(){
        name.setText("")
        currClass.setText("")
        department.setText("")
        selectedIndex = 0
        status.setSelection(selectedIndex)
        distance.setText("")
        stayDuration.setText("")
        email.setText("")
        phoneNumber.setText("")
        image.setImageDrawable(resources.getDrawable(R.drawable.baseline_account_box_24_black))
    }

}