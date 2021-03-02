package com.nubip.agrobib_messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        splashScreenImage.alpha = 0f

        splashScreenImage.animate().setDuration(1500).alpha(1f).withEndAction {
            val currentUser = Firebase.auth.currentUser
            changeActivity(currentUser)
        }
    }

    private fun changeActivity(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val chatActivity = Intent(this, ChatActivity::class.java)
            startActivity(chatActivity)
        } else {
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}