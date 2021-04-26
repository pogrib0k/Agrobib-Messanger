package com.nubip.agrobib_messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.nubip.agrobib_messenger.models.Message
import com.nubip.agrobib_messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_private_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class PrivateChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var user_uuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_chat)

        val linearLayoutManager = LinearLayoutManager(this);
        meseeges_list.layoutManager = linearLayoutManager
        supportActionBar?.hide()

        val user = intent.getParcelableExtra<User>("user")
        username.text = user?.username
        user_uuid = user?.uuid.toString()
        meseeges_list.adapter = adapter

        listenForMessages()
        message.setOnFocusChangeListener { v: View, hasFocus: Boolean -> scrollMessageListToEnd(hasFocus) }
        btn_send_message.setOnClickListener {
            performSendMessage()
        }
    }

    private fun scrollMessageListToEnd(hasFocus: Boolean) {
        if (hasFocus) {
            meseeges_list.scrollToPosition(adapter.itemCount - 1)
        }
    }


    private fun performSendMessage() {
        // how do we actually send a message to firebase...
        val text = message.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>("user")
        val toId = user?.uuid.toString()

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages").push()
        val toReference =
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage =
            Message(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("D", "Saved our chat message: ${reference.key}")
                message.text.clear()
                meseeges_list.scrollToPosition(adapter.itemCount)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
        adapter.notifyDataSetChanged()
        message.clearFocus()
    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(Message::class.java)

                if (chatMessage != null) {
                    Log.d("D", chatMessage.text)
                    meseeges_list.scrollToPosition(adapter.itemCount - 1)
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid && chatMessage.toId == user_uuid) {
                        adapter.add(ChatFromItem(chatMessage.text, chatMessage.fromId, chatMessage))
                    } else if (chatMessage.fromId == user_uuid && chatMessage.toId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatToItem(chatMessage.text, chatMessage.toId, chatMessage))
                    }
                }

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

    }

}

class ChatFromItem(val text: String, val userId: String, val message: Message) : Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_text_from.text = text
        val sdf = java.text.SimpleDateFormat("HH:mm")
        val date = java.util.Date(message.timestamp * 1000)
        viewHolder.itemView.message_time_sent_by_current_user.text = sdf.format(date)

        val ref = FirebaseDatabase.getInstance().getReference("/users/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)

                if (chatPartnerUser?.profileImageUrl != "") {
                    val targetImageView = viewHolder.itemView.from_image
                    FirebaseStorage.getInstance()
                        .getReferenceFromUrl(chatPartnerUser?.profileImageUrl.toString()).downloadUrl.addOnSuccessListener {
                            Picasso.get().load(it.toString()).into(targetImageView)
                        }
                }

            }
        })
    }
}

class ChatToItem(val text: String, val userId: String, val message: Message) : Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null


    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_text_to.text = text
        val sdf = java.text.SimpleDateFormat("HH:mm")
        val date = java.util.Date(message.timestamp * 1000)
        viewHolder.itemView.message_time_sent_by_another_user.text = sdf.format(date)

        val ref = FirebaseDatabase.getInstance().getReference("/users/${message.fromId}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)

                if (chatPartnerUser?.profileImageUrl != "") {
                    val targetImageView = viewHolder.itemView.to_image
                    FirebaseStorage.getInstance()
                        .getReferenceFromUrl(chatPartnerUser?.profileImageUrl.toString()).downloadUrl.addOnSuccessListener {
                            Picasso.get().load(it.toString()).into(targetImageView)
                        }
                }

            }
        })
    }

}



