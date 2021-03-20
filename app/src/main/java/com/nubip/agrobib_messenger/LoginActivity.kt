package com.nubip.agrobib_messenger

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nubip.agrobib_messenger.oauth.GithubOauth
import com.nubip.agrobib_messenger.oauth.Oauth
import com.nubip.agrobib_messenger.oauth.TwitterOauth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // On click listeners
        login_button.setOnClickListener(this)
        google_oauth_image.setOnClickListener(this)
        github_oauth_image.setOnClickListener(this)
        twitter_oauth_image.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.login_button -> {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()
                signInWithEmailAndPassword(email, password)
            }
            R.id.google_oauth_image -> signInWithGoogle()
            R.id.github_oauth_image -> providerOauth(GithubOauth(auth))
            R.id.twitter_oauth_image -> providerOauth(TwitterOauth(auth))
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = editTextEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            editTextEmail.error = "Required."
            valid = false
        } else {
            editTextEmail.error = null
        }

        val password = editTextPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            editTextPassword.error = "Required."
            valid = false
        } else {
            editTextPassword.error = null
        }

        return valid
    }

    private fun startMessenger() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Email and password authentication
    private fun signInWithEmailAndPassword(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        startMessenger()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        showAuthFailMessage()
                    }

                }
    }

    // Oauth authentication via Google account
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        startMessenger()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        showAuthFailMessage()
                    }
                }
    }

    private fun providerOauth(oauth: Oauth) {
        oauth.startOauthActivity(this).addOnSuccessListener {
            startMessenger()
        }.addOnFailureListener {
            showAuthFailMessage()
        }
    }

    private fun showAuthFailMessage() {
        val view: View = findViewById(R.id.main_layout)
        Snackbar.make(view, "Authentication Failed!", Snackbar.LENGTH_SHORT).show()
    }


    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private const val RC_SIGN_IN = 47
    }

}