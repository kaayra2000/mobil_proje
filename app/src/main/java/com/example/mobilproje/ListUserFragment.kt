package com.example.mobilproje

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ListUserFragment : Fragment() {

    val database = FirebaseDatabase.getInstance().reference
    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var userListProgressBar: ProgressBar
    val userList = mutableListOf<FindPerson>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userListRecyclerView = view.findViewById(R.id.userListRecyclerView)
        userListProgressBar = view.findViewById(R.id.userListProgressBar)
        val layoutManager = LinearLayoutManager(context)
        userListRecyclerView.layoutManager = layoutManager
        getDataAndSetupAdapter()

    }

    private fun getDataAndSetupAdapter() {
        var myRef = database.child("persons").ref
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(FindPerson::class.java)
                        user?.let {
                            it.userName = userSnapshot.key ?: ""
                            userList.add(it)
                        }
                    }

                    val adapter = UserListAdapter(userList,this@ListUserFragment)
                    userListRecyclerView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ListUserFragment", "Verileri alma işlemi başarısız oldu.")
            }
        })
    }

    override fun onPause() {
        super.onPause()
        userList.clear()
    }

}
