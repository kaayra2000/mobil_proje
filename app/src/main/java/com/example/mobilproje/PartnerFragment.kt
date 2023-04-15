package com.example.mobilproje

import GraduatPerson
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.mobilproje.databinding.FragmentPartnerBinding
import com.example.mobilproje.databinding.FragmentProfileBinding
import com.example.mobilproje.databinding.FragmentProfileSettingsBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream


class PartnerFragment : Fragment() {
    private var _binding: FragmentPartnerBinding? = null
    private val binding get() = _binding!!
    private lateinit var findPerson: FindPerson
    val database = FirebaseDatabase.getInstance().reference
    lateinit var status : Spinner
    lateinit var name : TextView
    lateinit var department : EditText
    lateinit var currClass  : EditText
    lateinit var stayDuration : EditText
    lateinit var distance : EditText
    lateinit var gradPerson: GraduatPerson
    lateinit var email : TextView
    lateinit var phoneNumber : TextView
    lateinit var image : ImageView
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var userName : String


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
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        userName = sharedPrefs.getString("currUserName","")!!
        _binding = FragmentPartnerBinding.inflate(inflater, container, false)
        scope.launch {
        gradPerson = database.child("users").child(userName).get().await()
            .getValue(GraduatPerson::class.java)!!
        findPerson = database.child("persons").child(userName).get().await()
            .getValue(FindPerson::class.java)!!
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.runOnUiThread {
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {

                initEditTexts()
                initTexts()
            }

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

        val scope = CoroutineScope(Job() + Dispatchers.Main)

        scope.launch {
            name.setText(gradPerson.name)
            email.setText(gradPerson.email)
            phoneNumber.setText(gradPerson.phoneNumber)
            image.setImageBitmap(convertStringToBitmap(gradPerson.photo.toString()))
        }

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
        findPerson?.let {
            withContext(Dispatchers.Main) {
                name.setText(gradPerson.name)
                currClass.setText(it.curClass)
                department.setText(it.department)
                var selectedIndex = LookingStatus.valueOf(it.lookingStatus.toString()).ordinal
                status.setSelection(selectedIndex)
                distance.setText(it.distance)
                stayDuration.setText(it.duration)
                email.setText(gradPerson.email)
                phoneNumber.setText(gradPerson.phoneNumber)
                var imageBitmap = gradPerson.photo.toString()
                image.setImageBitmap(gradPerson.photo?.let { it1 -> convertStringToBitmap(it1) })
                if(gradPerson.photo == null || gradPerson.photo!!.length < 5){
                    imageBitmap = ppString()
                    image.setImageBitmap(convertStringToBitmap(imageBitmap!!))
                }

            }
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
}