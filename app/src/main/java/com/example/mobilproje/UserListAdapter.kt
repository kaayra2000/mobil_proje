package com.example.mobilproje

import GraduatPerson
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.user_item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class UserListAdapter(private val userList: List<FindPerson>, private val fragment: ListUserFragment) :

    RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {
    lateinit var sharedPrefs : SharedPreferences
    val database = FirebaseDatabase.getInstance().reference
    lateinit var editor : SharedPreferences.Editor
    lateinit var userName: String
    lateinit var customToast: CustomToast

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        sharedPrefs = parent.context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
        userName = sharedPrefs.getString("username","").toString()
        return UserListViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var gradPerson: GraduatPerson? = null

        fun bind(user: FindPerson) = GlobalScope.launch {
            val dataSnapshot = database.child("users").child(user.userName).get().await()
            if (dataSnapshot.exists()) {
                gradPerson = dataSnapshot.getValue(GraduatPerson::class.java)!!
                withContext(Dispatchers.Main) {
                    itemView.imageView.setImageBitmap(gradPerson?.photo?.let { convertStringToBitmap(it) })
                    itemView.nameText.text = gradPerson?.name
                    itemView.currClassText.text = user.curClass
                    itemView.durationText.text = user.duration
                    editor.putString("currUserName", user.userName)

                    itemView.imageView.setOnClickListener {
                        fragment.findNavController().navigate(R.id.action_listUserFragment_to_partnerFragment)
                    }
                }
            } else {
                itemView.setOnClickListener {
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            CustomToast(itemView.context).apply {
                                showMessage("User Cannot Found", isSuccess = false)
                            }
                        }
                    }
                }
            }

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
}
