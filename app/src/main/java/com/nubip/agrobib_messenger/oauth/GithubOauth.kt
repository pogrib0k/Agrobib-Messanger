package com.nubip.agrobib_messenger.oauth

import com.google.firebase.auth.FirebaseAuth

class GithubOauth(auth: FirebaseAuth) : Oauth(auth, "github.com") {

}