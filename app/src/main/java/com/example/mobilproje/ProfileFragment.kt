package com.example.mobilproje

import GraduatPerson
import RequestDataClass
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userName = arguments?.getString("userName").toString()
        val toast = CustomToast(context)
        getDataAndSetupAdapter()
        binding.navigateButton.setOnClickListener {
            val bundle = bundleOf("userName" to userName)
            findNavController().navigate(R.id.action_profile_to_homeScreen,bundle)
        }

        binding.incomingRequestsText.setOnClickListener {
            var myRef = database.child("requests").child("kamya123").ref
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val request = dataSnapshot.getValue(RequestDataClass::class.java)
                    if (dataSnapshot.exists() && request?.recieverUserName == userName) {
                        // Gönderilecek verileri bir Bundle nesnesi içinde tutun
                        val bundle = Bundle().apply {
                            putString("applierUserName", userName)
                            putString("senderUserName",request.senderUserName )
                        }
                        findNavController().navigate(R.id.action_profile_to_acceptRequestFragment, bundle)

                    } else {
                        toast.showMessage("Id not found",false)
                        return
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Sorgu iptal edildi.
                }
            })
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

    private fun getDataAndSetupAdapter() {
        var myRef = database.child("requests").ref
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (requestSnapshot in dataSnapshot.children) {
                        val request = requestSnapshot.getValue(RequestDataClass::class.java)
                        request?.let {
                            if(request.recieverUserName.equals(userName)){
                                binding.incomingRequestsText.text = binding.incomingRequestsText.text.toString() + "\nId: " + request.senderUserName+
                                        " Name: " + request.senderName
                            }
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ListUserFragment", "Verileri alma işlemi başarısız oldu.")
            }
        })
    }




}