package com.nubip.agrobib_messenger

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
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
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import java.util.*
import kotlin.collections.HashMap

class ChatsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var auth: FirebaseAuth
    private lateinit var listadapter: GroupAdapter<GroupieViewHolder>
    private lateinit var navView: NavigationView
    private var header: View? = null

    companion object {
        var currentUser: User? = null
    }
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        fetchCurrentUser()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, SearchUser::class.java)
            startActivity(intent)
        }
        supportActionBar?.title = "Agrobib-messenger"
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        listadapter = GroupAdapter()
        listadapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, PrivateChatActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra("user", row.chatPartnerUser)
            startActivity(intent)
        }

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

        setNavigationViewListener()
        listenForLatestMessages()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.chats, menu)
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

//    override fun onClick(v: View) {
//        when (v.id) {
//            R.id.logout_button -> signOut()
//            R.id.private_msg_btn -> privateMessage()
//        }
//    }

//    private fun privateMessage() {
//        val intent = Intent(this, PrivateChatActivity::class.java)
//        startActivity(intent)
//    }

    private fun setNavigationViewListener() {
        val navigationView =
            findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
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

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)

                if (header == null) {
                    header = navView.inflateHeaderView(R.layout.nav_header_main)
                }

                Log.d("TAG", currentUser?.profileImageUrl.toString())
                header?.bar_nickname!!.text = currentUser!!.username
                header?.bar_email!!.text = currentUser!!.email

                FirebaseStorage.getInstance()
                    .getReferenceFromUrl(currentUser?.profileImageUrl.toString()).downloadUrl.addOnSuccessListener {
                        Picasso.get().load(it.toString()).into(header!!.main_user_image)
                    }
                Log.d("LatestMessages", "Current user ${currentUser?.username}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.account_settings -> openAccountSettings()
            R.id.logout -> signOut()
        }
        return true
    }

    private fun openAccountSettings() {
        val intent = Intent(this, AccountSettings::class.java)
        intent.putExtra("user", currentUser)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        fetchCurrentUser()
    }
}


class LatestMessageRow(val chatMessage: Message) : Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.latest_message.text = chatMessage.text

        if (chatMessage.text.contains("[/images/")) {
            viewHolder.itemView.latest_message.text = chatMessage.text.split("\\[/images/.*]".toRegex())[1]
        }

        val sdf = java.text.SimpleDateFormat("MM.dd")
        val date = Date(chatMessage.timestamp * 1000)

        viewHolder.itemView.date_of_last_message.text = sdf.format(date)

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
                    FirebaseStorage.getInstance()
                        .getReferenceFromUrl(chatPartnerUser?.profileImageUrl.toString()).downloadUrl.addOnSuccessListener {
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