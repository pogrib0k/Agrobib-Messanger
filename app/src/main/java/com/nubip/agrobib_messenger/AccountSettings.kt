package com.nubip.agrobib_messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nubip.agrobib_messenger.models.User

class AccountSettings : AppCompatActivity() {

    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        user = intent.getParcelableExtra<User>("user")!!

    }
}