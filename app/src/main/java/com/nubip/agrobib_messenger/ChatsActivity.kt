package com.nubip.agrobib_messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*


// TODO: Temporary code. Remake all
class ChatsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private val usernameList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = Firebase.auth
        setAutocompleteAdapter()

        greeting_text.text = "Welcome to Agrobib-messenger, ${auth.currentUser?.email}"

        logout_button.setOnClickListener(this)
    }



    private fun setAutocompleteAdapter() {
        val query = FirebaseDatabase.getInstance().getReference("/users").orderByKey()
        adapter = ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, usernameList
        )
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val username = it.child("username").value.toString()
                    usernameList.add(username)
                }
                adapter.notifyDataSetChanged()
            }

        }
        search_text_view.setAdapter(adapter)
        query.addListenerForSingleValueEvent(listener)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.logout_button -> signOut()
        }
    }

    private fun signOut() {
        auth.signOut()

        val chatActivity = Intent(this, LoginActivity::class.java)
        startActivity(chatActivity)
        finish()
    }
}