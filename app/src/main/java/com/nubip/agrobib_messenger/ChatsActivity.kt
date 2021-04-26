package com.nubip.agrobib_messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.nubip.agrobib_messenger.models.Message
import com.nubip.agrobib_messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chats.*
import kotlinx.android.synthetic.main.latest_message_row.view.*


// TODO: Temporary code. Remake all
class ChatsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private val usernameList = ArrayList<String>()
    private val userList = ArrayList<User>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listadapter: GroupAdapter<GroupieViewHolder>


    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)
        listadapter = GroupAdapter()
        listadapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, PrivateChatActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra("user", row.chatPartnerUser)
            startActivity(intent)
        }

        // Actionbar

        supportActionBar?.show()

        //setupDummyRows()
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerview_latest_messages.layoutManager = llm
        recyclerview_latest_messages.adapter = listadapter
        recyclerview_latest_messages.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        auth = Firebase.auth

        setAutocompleteAdapter()
//        greeting_text.text = "Welcome to Agrobib-messenger, ${auth.currentUser?.email}"
        listenForLatestMessages()
        fetchCurrentUser()
        logout_button.setOnClickListener(this)
    }


    private fun setAutocompleteAdapter() {
        val query = FirebaseDatabase.getInstance().getReference("/users").orderByKey()
        adapter = ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, usernameList
        )

        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                        usernameList.add(user.username)
                    }
                }
                adapter.notifyDataSetChanged()
            }

        }
        search_text_view.setAdapter(adapter)
        search_text_view.setOnItemClickListener { parent, view, position, id ->
            val privateChatActivity = Intent(this, PrivateChatActivity::class.java)
            val user = userList.filter { user -> user.username == (view as TextView).text }.single()
            privateChatActivity.putExtra("user", user)
            startActivity(privateChatActivity)
        }
        query.addListenerForSingleValueEvent(listener)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.logout_button -> signOut()
            R.id.private_msg_btn -> privateMessage()
        }
    }

    private fun privateMessage() {
        val intent = Intent(this, PrivateChatActivity::class.java)
        startActivity(intent)
    }

    private fun signOut() {
        auth.signOut()

        val chatActivity = Intent(this, LoginActivity::class.java)
        startActivity(chatActivity)
        finish()
    }

    val latestMessagesMap = HashMap<String, Message>()

    private fun refreshRecyclerViewMessages() {
        listadapter.clear()
        latestMessagesMap.values.forEach {
            listadapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


//    private fun setupDummyRows() {
//        listadapter.add(LatestMessageRow(Message()))
//        listadapter.add(LatestMessageRow(Message()))
//        listadapter.add(LatestMessageRow(Message()))
//    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current user ${currentUser?.username}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


}

class LatestMessageRow(val chatMessage: Message) : Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.latest_message.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                Log.d("Ahahahahahah1", p0.toString())

                chatPartnerUser = p0.getValue(User::class.java)

                viewHolder.itemView.last_nickname.text = chatPartnerUser?.username

                if (chatPartnerUser?.profileImageUrl != "") {
                    val targetImageView = viewHolder.itemView.user_logo
                    FirebaseStorage.getInstance().getReferenceFromUrl(chatPartnerUser?.profileImageUrl.toString()).downloadUrl.addOnSuccessListener {
                        Picasso.get().load(it.toString()).into(targetImageView)
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}