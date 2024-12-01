
package com.example.madagascar.freeborad

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R

class CommentAdapter(
    private val comments: List<Comment>,
    private val context: Context
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.commentTextView.text = comment.text
        holder.timestampTextView.text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", comment.timestamp)
        holder.usernameTextView.text = comment.username
    }

    override fun getItemCount(): Int = comments.size

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
    }
}