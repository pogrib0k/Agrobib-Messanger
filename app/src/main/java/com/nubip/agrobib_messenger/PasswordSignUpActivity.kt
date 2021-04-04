package com.nubip.agrobib_messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.nubip.agrobib_messenger.models.User
import com.nubip.agrobib_messenger.utils.PasswordSignUpValidator
import kotlinx.android.synthetic.main.activity_password_signup.*

class PasswordSignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_signup)

        auth = Firebase.auth

        signup_button.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signup_button -> signUp()
        }
    }

    private fun signUp() {
        val email = signup_email.text.toString()
        val username = signup_nickname.text.toString()
        val password = signup_password.text.toString()

        if (!validateForm()) {
            return
        }

       Toast.makeText(baseContext, "Success.", Toast.LENGTH_SHORT).show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val profileImageId = (1..3).random()
                    val profileImageUrl = FirebaseStorage
                            .getInstance()
                            .getReference("/images/default/$profileImageId.jpg")
                            .toString()

                    val user = User(auth.uid.toString(), username, email, profileImageUrl)

                    saveUserToFirebaseDatabase(user)
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(baseContext, "Success!",
                            Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, task.result.toString(),
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirebaseDatabase(user: User) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid}")
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG, "User successful saved to firebase")
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = signup_email.text.toString()
        val nickname = signup_nickname.text.toString()
        val password = signup_password.text.toString()
        val repeatPassword = signup_repeat_password.text.toString()

       if (isRequiredFieldsEmpty()) return false

        val nicknameValidationMessage = PasswordSignUpValidator.validateNickname(nickname)
        if (nicknameValidationMessage != PasswordSignUpValidator.SUCCESSFUL_VALIDATION) {
            signup_nickname.error = nicknameValidationMessage
            valid = false
        }

        val emailValidationMessage = PasswordSignUpValidator.validateEmail(email)
        if (emailValidationMessage != PasswordSignUpValidator.SUCCESSFUL_VALIDATION) {
            signup_email.error = emailValidationMessage
            valid = false
        }

        val passwordValidationMessage = PasswordSignUpValidator.validatePassword(password)
        if (passwordValidationMessage != PasswordSignUpValidator.SUCCESSFUL_VALIDATION) {
            signup_password.error = passwordValidationMessage
            valid = false
        }

        if (password != repeatPassword) {
            signup_repeat_password.error = "Password mismatch!"
            valid = false
        }

        if (!terms_and_conditions_check_box.isChecked) {
            terms_and_conditions_check_box.error = "Read Terms and Conditions!"
            valid = false
        }

        return valid
    }

    private fun isRequiredFieldsEmpty(): Boolean{
        val requiredFields = listOf(signup_email, signup_nickname, signup_password, signup_repeat_password)
        var empty = false

        requiredFields.forEach {
            val text = it.text.toString()

            if (TextUtils.isEmpty(text)) {
                it.error = "Required."
                empty = true
            }
        }
        return empty
    }

    companion object {
        private val TAG = PasswordSignUpActivity::class.java.simpleName
    }
}