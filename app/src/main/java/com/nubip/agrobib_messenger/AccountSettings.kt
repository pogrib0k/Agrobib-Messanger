package com.nubip.agrobib_messenger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.nubip.agrobib_messenger.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import java.util.*


class AccountSettings : AppCompatActivity() {

    lateinit var user: User
    lateinit var new_user: User
    private lateinit var auth: FirebaseAuth
    var img_uri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        user = intent.getParcelableExtra("user")!!
        auth = Firebase.auth

        FirebaseStorage.getInstance()
            .getReferenceFromUrl(user?.profileImageUrl.toString()).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it.toString()).into(user_img)
            }

        editTextTextUserName.setText(user.username)
        change_email.setText(user.email)

        buttonApplySetting.setOnClickListener {
            save()
        }
        buttonChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    @SuppressLint("ShowToast")
    private fun save() {
        val email = change_email.text.toString()
        val username = editTextTextUserName.text.toString()
        val old_password = current_password.text.toString()
        val new_pass = new_password.text.toString()

        if (img_uri.isEmpty()) {
            img_uri = user.profileImageUrl
        } else {
            img_uri = FirebaseStorage
                .getInstance()
                .getReference(img_uri)
                .toString()
        }

        new_user = User(user.uuid, username, email, img_uri)

        var database = Firebase.database.reference


        if (old_password.isNotEmpty()) {
            val credential = EmailAuthProvider
                .getCredential(auth.currentUser?.email.toString(), old_password)

            auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.currentUser!!.updatePassword(new_pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            database.child("users").child(user.uuid).setValue(new_user)

                            Toast.makeText(this, "Successfully saved!", Toast.LENGTH_LONG).show()

                            finish()
                        } else {
                            Toast.makeText(this, "Invalid new password!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show()
                    img_uri = ""
                    Log.d("TAG", "Error auth failed")
                }
            }
        } else {
            database.child("users").child(user.uuid).setValue(new_user)

            Toast.makeText(this, "Successfully saved!", Toast.LENGTH_LONG).show()

            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what the selected image was....
            Log.d("Photo", "Photo was selected")

            var selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            user_img.setImageBitmap(bitmap)

            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("TAG", "Successfully uploaded image: ${it.metadata?.path}")
                    img_uri = "/images/$filename"
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("TAG", "File Location: $it")
                    }
                }
                .addOnFailureListener {
                    Log.d("TAG", "Failed to upload image to storage: ${it.message}")
                }


        }
    }
}