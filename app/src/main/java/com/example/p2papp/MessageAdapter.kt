package com.example.p2papp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageContentLeft: LinearLayout = view.findViewById(R.id.messageContentLeft)
        val messageContentRight: LinearLayout = view.findViewById(R.id.messageContentRight)

        val textRecieveName: TextView = view.findViewById(R.id.textRecieveName)
        val textRecieveMessage: TextView = view.findViewById(R.id.textRecieveMessage)
        val timeRecieveSend: TextView = view.findViewById(R.id.timeRecieveSend)
        val timeReceived: TextView = view.findViewById(R.id.timeReceived)

        val textMessage: TextView = view.findViewById(R.id.textMessage)
        val timeSend: TextView = view.findViewById(R.id.timeSend)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Ocultar ambos layouts al inicio para evitar reciclado incorrecto
        holder.messageContentLeft.visibility = View.GONE
        holder.messageContentRight.visibility = View.GONE

        if (message.isSentByMe) {
            // Mostrar y llenar layout de mensaje enviado (derecha)
            holder.messageContentRight.visibility = View.VISIBLE
            holder.textMessage.text = message.text
            holder.textMessage.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            holder.timeSend.text = message.timeSend
            holder.timeSend.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        } else {
            // Mostrar y llenar layout de mensaje recibido (izquierda)
            holder.messageContentLeft.visibility = View.VISIBLE
            holder.textRecieveMessage.text = message.text
            holder.timeRecieveSend.text = message.timeSend
            holder.timeReceived.text = message.timeReceived
            holder.textRecieveName.text = message.nameUser
        }
    }



    override fun getItemCount(): Int = messages.size
}
