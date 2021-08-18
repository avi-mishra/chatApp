package android.example.chatapp.activity

import DataClass.ChatMessage
import DataClass.User
import android.example.chatapp.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chatfromrow.view.*
import kotlinx.android.synthetic.main.chattorow.view.*
import java.util.*
import kotlin.math.log

class ChatLogActivity : AppCompatActivity() {
    private val adapter=GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        rvChatLog.adapter=adapter
        val userName=intent.getStringExtra("name")
        val toID=intent.getStringExtra("uid")
        val pic=intent.getStringExtra("pic")
        Log.d("toId", "onCreate: $toID")
        if (toID != null) {
            getMessage(toID)
        }

        supportActionBar?.title="$userName"
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setIcon(R.drawable.ic_baseline_account_circle_24)
        val ref=FirebaseDatabase.getInstance().getReference("/users/$toID")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                val u=snapshot.child("userState").child("date").value.toString()

                Log.d("lastseen", "onDataChange: $u")
                Log.d("lastseen", "onDataChange: ${user?.userName}")
                val currState = user?.userState?.get("state")
                if (currState == "online") {
                    supportActionBar?.subtitle = "online"
                } else
                    supportActionBar?.subtitle =
                        "Last Seen ${user?.userState?.get("date")} ${user?.userState?.get("time")}"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        btnSend.setOnClickListener {
            Toast.makeText(this, "Btn send clicked", Toast.LENGTH_SHORT).show()
            val text=etEnterNewMessage.text.toString()
            if (toID != null && text.isNotEmpty()) {
                sendMessage(toID,text)
            }
        }
    }

    fun getMessage(toId: String) {
        val fromId= FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/userMessages/IasK0EM0vlXf6tP3wlSeLo27A6n2/$toId") //change
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmsg = snapshot.getValue(ChatMessage::class.java)
                if (chatmsg != null) {
                    Log.d("chatmsg", "onChildAdded: $chatmsg")
                    if (chatmsg.toId == FirebaseAuth.getInstance().uid)
                        adapter.add(ChatFromItem(chatmsg.text,chatmsg.time))
                    else
                        adapter.add(ChatToItem(chatmsg.text,chatmsg.time))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
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

    fun sendMessage(toId:String,text: String){
        val fromId= FirebaseAuth.getInstance().uid    //change -> return

        val ref=FirebaseDatabase.getInstance().getReference("/userMessages/IasK0EM0vlXf6tP3wlSeLo27A6n2/$toId").push()  //change
        val toRef=FirebaseDatabase.getInstance().getReference("/userMessages/$toId/IasK0EM0vlXf6tP3wlSeLo27A6n2").push()   //change
        val chatMessage=ChatMessage(ref.key!!,text,"IasK0EM0vlXf6tP3wlSeLo27A6n2",toId,System.currentTimeMillis())   //change
        ref.setValue(chatMessage).addOnSuccessListener {
            Log.d("saveMessage", "sendMessage: Saved message to database $chatMessage")
            etEnterNewMessage.text.clear()
            rvChatLog.scrollToPosition(adapter.itemCount-1)
        }
        if(toId!="IasK0EM0vlXf6tP3wlSeLo27A6n2")   //change
        toRef.setValue(chatMessage)

        val latestMessage=FirebaseDatabase.getInstance().getReference("/latestMessage/IasK0EM0vlXf6tP3wlSeLo27A6n2/$toId")   //change
        latestMessage.setValue(chatMessage)

        val latestMessageTo=FirebaseDatabase.getInstance().getReference("/latestMessage/$toId/IasK0EM0vlXf6tP3wlSeLo27A6n2")  //change
        latestMessageTo.setValue(chatMessage)
    }
}
class ChatFromItem(val text:String,val time:Long):Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tvFromRow.text=text
        viewHolder.itemView.timeFromRow.text=DateUtils.getRelativeTimeSpanString(time)
    }

    override fun getLayout(): Int {
        return R.layout.chatfromrow
    }

}
class ChatToItem(val text: String,val time:Long):Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tvToRow.text=text
        viewHolder.itemView.timeToRow.text=DateUtils.getRelativeTimeSpanString(time)
    }

    override fun getLayout(): Int {
        return R.layout.chattorow
    }
}