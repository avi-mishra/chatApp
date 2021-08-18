package DataClass

import android.example.chatapp.activity.MainActivity
import android.net.Uri

data class User(var pic:String, var userName:String, var uid:String, var userState: HashMap<String,Any>)
{
    constructor():this("","","", HashMap<String,Any>())
}
