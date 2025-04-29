package com.example.p2papp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameUser: TextView = itemView.findViewById(R.id.textSenderName)
        val messageTextView: TextView = itemView.findViewById(R.id.textMessage)
        val timeSend: TextView = itemView.findViewById(R.id.timeSend)
        val timeReceived: TextView = itemView.findViewById(R.id.timeReceived)
        val spaceLeft: View = itemView.findViewById(R.id.spaceLeft)
        val spaceRight: View = itemView.findViewById(R.id.spaceRight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.nameUser.text = message.nameUser
        holder.messageTextView.text = message.text
        holder.timeSend.text = message.timeSend
        holder.timeReceived.text = message.timeReceived

        if (message.isSentByMe) {
            holder.messageTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            holder.timeSend.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            holder.timeReceived.visibility=View.GONE
            holder.nameUser.visibility=View.GONE
            holder.spaceLeft.visibility = View.VISIBLE
            holder.spaceRight.visibility = View.GONE
        } else {
            holder.messageTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.timeReceived.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.timeReceived.visibility=View.VISIBLE
            holder.timeSend.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.spaceLeft.visibility = View.GONE
            holder.spaceRight.visibility = View.VISIBLE
            holder.nameUser.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = messages.size
}
