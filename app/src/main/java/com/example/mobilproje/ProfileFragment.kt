package com.example.mobilproje

import GraduatPerson
import MyLocation
import RequestDataClass
import android.app.AlertDialog
import android.content.Context
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log

import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentProfileBinding
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

// TODO: Rename parameter arguments, choose names that match



class ProfileFragment : Fragment() {
    lateinit var userName : String
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    val database = FirebaseDatabase.getInstance().reference
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    lateinit var user: GraduatPerson
    private var backPressedTime = Long.MIN_VALUE
    lateinit var sharedPrefs : SharedPreferences
    lateinit var activity: AppCompatActivity
    lateinit var editor : SharedPreferences.Editor


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Profile"
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 300
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
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
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startLocationUpdates()
            } else {
                requestLocationPermission()
            }
            findNavController().navigate(R.id.action_profile_to_homeScreen,bundle)
        }



        binding.incomingRequestsText.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Please enter a username to go to the page")

            val input = EditText(requireContext())
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val username = input.text.toString()
                if (username.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
                } else {
                    var myRef = database.child("requests").child(username).ref
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
                                toast.showMessage("This username was not found among the users who sent you a request",false)
                                return
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            // Sorgu iptal edildi.
                        }
                    })
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
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setMessage("Are you sure you want to exit?")
            alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                editor.putString("username", "")
                editor.putString("password", "")
                editor.putBoolean("loginFlag", false)
                editor.apply()
                findNavController().navigate(R.id.action_profileSettings_to_FirstFragment)
            }
            alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialogBuilder.create().show()
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
        activity.supportActionBar?.title = "Profile"
        userName = sharedPrefs.getString("username","").toString()
        startLocationUpdates()
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

        user.let {
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
                                if(!request.isOkey)
                                binding.incomingRequestsText.text = binding.incomingRequestsText.text.toString() + "\nId: " + request.senderUserName+
                                        " Name: " + request.senderName
                                else
                                    binding.incomingRequestsText.text = "Accepted Request" + "\nId: " + request.senderUserName+
                                            " Name: " + request.senderName
                            }
                            else if(request.senderUserName.equals(userName)){
                                binding.outgoingRequestsText.text = binding.outgoingRequestsText.text.toString() + "\nId: " + request.senderUserName+
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
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            requestPermissions(
                arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if(::user.isInitialized){
                locationResult.lastLocation.let { location ->
                    val myLocation = MyLocation(latitude = location.latitude, longitude = location.latitude,
                        userName = userName, name = user.name, surName = user.surName
                    )
                    database.child("locations").child(userName).setValue(myLocation)
                }

            }

        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)


    }









}