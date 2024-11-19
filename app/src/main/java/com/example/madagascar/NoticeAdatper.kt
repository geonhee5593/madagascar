package com.example.madagascar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Notice(val message: String, val timestamp: String)

class NoticeAdapter(private val noticeList: List<Notice>) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.noticeText)
        val timestampTextView: TextView = itemView.findViewById(R.id.noticeTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notice_item, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = noticeList[position]
        holder.messageTextView.text = notice.message
        holder.timestampTextView.text = notice.timestamp
    }

    override fun getItemCount(): Int = noticeList.size
}
