package com.nubip.agrobib_messenger

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nubip.agrobib_messenger.models.User
import kotlinx.android.synthetic.main.activity_chats.*
import kotlinx.android.synthetic.main.activity_user_search.*

class SearchUser : AppCompatActivity() {

    private val usernameList = ArrayList<String>()
    private val userList = ArrayList<User>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_search)
        setAutocompleteAdapter()

        back_button2.setOnClickListener {
            finish()
        }
    }

    private fun setAutocompleteAdapter() {
        val query = FirebaseDatabase.getInstance().getReference("/users").orderByKey()
        adapter = ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, usernameList
        )

        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                        usernameList.add(user.username)
                    }
                }
                adapter.notifyDataSetChanged()
            }

        }
        search_text_view.setAdapter(adapter)
        search_text_view.setOnItemClickListener { parent, view, position, id ->
            val privateChatActivity = Intent(this, PrivateChatActivity::class.java)
            val user = userList.filter { user -> user.username == (view as TextView).text }.single()
            privateChatActivity.putExtra("user", user)
            startActivity(privateChatActivity)
        }
        query.addListenerForSingleValueEvent(listener)
    }
}