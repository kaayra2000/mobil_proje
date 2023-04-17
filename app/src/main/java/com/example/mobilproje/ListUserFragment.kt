package com.example.mobilproje

import GraduatPerson
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*


class ListUserFragment : Fragment() {

    val database = FirebaseDatabase.getInstance().reference
    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var userListSearchView: SearchView
    private lateinit var spinner: Spinner
    val userList = mutableListOf<FindPerson>()
    private var option = 0
    var textQuery = ""
    val userListCopy = mutableListOf<FindPerson>()
    lateinit var userName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userName = requireArguments().getString("userName").toString()
        userListRecyclerView = view.findViewById(R.id.userListRecyclerView)
        userListSearchView = view.findViewById(R.id.searchView)
        spinner = view.findViewById(R.id.spinner)
        val layoutManager = LinearLayoutManager(context)
        userListRecyclerView.layoutManager = layoutManager
        getDataAndSetupAdapter()
        userListSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                lifecycleScope.launch {
                    filter(newText)
                }
                textQuery = newText
                return true
            }
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                option = position
                lifecycleScope.launch {
                    filter(textQuery)
                }
                // selectedValue değişkeni seçilen değere karşılık gelen indis ile güncellenir
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Hiçbir öğe seçilmediğinde yapılacak işlemler buraya yazılır.
            }
        }

    }

    private fun getDataAndSetupAdapter() {
        val myRef = database.child("persons").ref

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                userListCopy.clear()
                if (dataSnapshot.exists()) {

                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(FindPerson::class.java)
                        user?.let {
                            it.userName = userSnapshot.key ?: ""
                            userList.add(it)
                            userListCopy.add(it)
                        }
                    }

                    val adapter = UserListAdapter(userListCopy,this@ListUserFragment,userName)
                    userListRecyclerView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ListUserFragment", "Verileri alma işlemi başarısız oldu.")
            }
        })
    }

    private suspend fun filter(text: String) {
        userListCopy.clear()
        userList.forEach { user ->
            val graduatPerson = database.child("users").child(user.userName).get().await()
                .getValue(GraduatPerson::class.java)!!

            if (comperator(graduatPerson,text, findPerson = user)) {
                userListCopy.add(user)
            }
        }
        val adapter = UserListAdapter(userListCopy, this@ListUserFragment, userName)
        userListRecyclerView.adapter = adapter
    }
    private fun comperator(graduatPerson: GraduatPerson, text: String, findPerson: FindPerson):Boolean{
        var secText = ""
        when(option){
            0 -> {
                secText = graduatPerson.name
            }
            1 -> {
                secText = findPerson.curClass
            }
            2 -> {
                secText = findPerson.duration
            }
            else -> {
                secText = graduatPerson.name
            }
        }

        return secText.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
    }
    override fun onPause() {
        super.onPause()
        userList.clear()
    }

}
