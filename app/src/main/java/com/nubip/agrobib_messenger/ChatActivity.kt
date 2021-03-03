package com.nubip.agrobib_messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*
// TODO: remake all
class ChatActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = Firebase.auth

        logout_button.setOnClickListener(this)
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