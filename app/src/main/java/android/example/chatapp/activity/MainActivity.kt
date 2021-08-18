package android.example.chatapp.activity

import DataClass.ChatMessage
import android.content.Intent
import android.example.chatapp.LogIn
import android.example.chatapp.R
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.mainlayout.view.*
import kotlinx.android.synthetic.main.userslayout.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    val latestMessageMap= HashMap<String, ChatMessage?>()  //change

    var onlineState = HashMap<String, Any>()

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        if(firebaseUser==null) {
//            val i = Intent(this, LogIn::class.java)
//            startActivity(i)
//            finish()
//        }
        Toast.makeText(this, "Logged In anonymously.Tap on user to chat.", Toast.LENGTH_SHORT).show()
        getLatestMessage()
        rvMainAct.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            val userItem = item as ItemView
            userItem.partner
            val i = Intent(this@MainActivity, ChatLogActivity::class.java)
            i.putExtra("name", userItem.partner?.userName)
            i.putExtra("uid", userItem.partner?.uid)
            i.putExtra("pic", userItem.partner?.pic)
            startActivity(i)
        }
    }

    //change
    fun refreshLatestMesg(){
        adapter.clear()
        latestMessageMap.values.forEach{
            adapter.add(ItemView(it!!))
        }
    }

    fun getLatestMessage() {
        val toId = intent.getStringExtra("uid")
        val userName = intent.getStringExtra("name")
        val pic = intent.getStringExtra("pic")
//        val fromId = FirebaseAuth.getInstance().uid
        val fromId="IasK0EM0vlXf6tP3wlSeLo27A6n2"
        val latestRef = FirebaseDatabase.getInstance().getReference("/latestMessage/$fromId")
        latestRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestMsg = snapshot.getValue(ChatMessage::class.java)

                latestMessageMap[snapshot.key!!]=latestMsg  //change
                refreshLatestMesg()    //change



//                if (latestMsg != null) {
//                    adapter.add(ItemView(latestMsg))
//                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val latestMsg = snapshot.getValue(ChatMessage::class.java)

                latestMessageMap[snapshot.key!!]=latestMsg  //change
                refreshLatestMesg()  //change

//                if (latestMsg != null) {
//                    adapter.add(ItemView(latestMsg))
//                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun userStatus(state: String) {
        val calender = Calendar.getInstance()

        val currentDate = SimpleDateFormat("dd-MMM-yyyy")
        val saveCurrentDate = currentDate.format(calender.time)

        val currentTime = SimpleDateFormat("hh:mm a")
        val saveCurrentTime = currentTime.format(calender.time)

        onlineState = HashMap<String, Any>()
        onlineState["time"] = saveCurrentTime
        onlineState["date"] = saveCurrentDate
        onlineState["state"] = state

        val currentUser = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/${firebaseUser?.uid}").child("userState").updateChildren(onlineState)
    }

    override fun onStart() {
        super.onStart()

        if (firebaseUser != null)
            userStatus("online")
    }

    override fun onStop() {
        super.onStop()
        if (firebaseUser == null) {
            userStatus("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (firebaseUser != null) {
            userStatus("offline")
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId==R.id.navSignOut) {
//            Toast.makeText(this@MainActivity, "Signed Out", Toast.LENGTH_SHORT).show()
//            FirebaseAuth.getInstance().signOut()
//            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
//            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
//            mGoogleSignInClient.signOut();
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            val i= Intent(this@MainActivity, LogIn::class.java)
//            startActivity(i)
//            finish()
//        }
         if(item.itemId==R.id.Users) {
            val i= Intent(this@MainActivity, Users::class.java)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }
}
class ItemView(private val chatMessage: ChatMessage):Item<ViewHolder>() {
    var partner: DataClass.User? = null
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tvLatestMessage.text = chatMessage.text

        val id: String
//        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
        if (chatMessage.fromId=="IasK0EM0vlXf6tP3wlSeLo27A6n2"){
            id = chatMessage.toId
        } else
            id = chatMessage.fromId

        val ref = FirebaseDatabase.getInstance().getReference("/users/$id")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                partner = snapshot.getValue(DataClass.User::class.java)
                viewHolder.itemView.tvMainUsername.text = partner?.userName
                Picasso.get().load(partner?.pic).into(viewHolder.itemView.cvMainProfile)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun getLayout(): Int {
        return R.layout.mainlayout
    }
}