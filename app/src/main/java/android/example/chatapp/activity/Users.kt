package android.example.chatapp.activity

import DataClass.User
import android.content.Intent
import android.example.chatapp.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.userslayout.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log

class Users : AppCompatActivity() {

    val firebaseUser=FirebaseAuth.getInstance().currentUser
    val firestoreRef=FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        getUsers()
    }
    fun getUsers() {
        var ref=FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter=GroupAdapter<ViewHolder>()

                snapshot.children.forEach{
                    val user=it.getValue(User::class.java)
                    if(user!=null) {
                        if(user.pic.isNotEmpty()){
                            adapter.add(UserItem(user))
                        }
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem=item as UserItem

                    val i=Intent(this@Users,ChatLogActivity::class.java)
                    i.putExtra("name",userItem.user.userName)
                    i.putExtra("uid",userItem.user.uid)
                    i.putExtra("pic",userItem.user.pic)
                    startActivity(i)
                }

                rvUsers.layoutManager=LinearLayoutManager(this@Users)
                rvUsers.adapter=adapter
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
class UserItem(val user:User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tvUserName.text=user.userName
        Picasso.get().load(user.pic).into(viewHolder.itemView.cvProfile)

    }

    override fun getLayout(): Int {
        return R.layout.userslayout
    }
}