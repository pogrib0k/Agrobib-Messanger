package com.nubip.agrobib_messenger.oauth

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

abstract class Oauth(private val auth: FirebaseAuth, private val providerName: String) {
    private val provider: OAuthProvider.Builder =  OAuthProvider.newBuilder(providerName)

    fun startOauthActivity(parentActivity: Activity): Task<AuthResult> {
        return auth.startActivityForSignInWithProvider(parentActivity, provider.build())
    }
}