package DataClass

data class ChatMessage(val id:String,val text:String,val fromId:String,val toId:String,val time:Long) {
    constructor():this("","","","",-1)
}
