package com.example.madagascar.freeborad

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(private val comments: List<Comment>, private val context: Context) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.commentTextView.text = comment.text

        // FirebaseAuth를 사용해 현재 로그인한 사용자의 ID를 가져옴
        val userId = comment.userId

        // Firestore에서 사용자 이름을 가져오기
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    holder.userIdTextView.text = "작성자: $username"
                } else {
                    holder.userIdTextView.text = "작성자: Unknown"
                }
            }
            .addOnFailureListener { e ->
                holder.userIdTextView.text = "작성자: Unknown"
                Log.e("CommentAdapter", "사용자 정보 불러오기 실패: ${e.message}")
            }

    }

    override fun getItemCount(): Int = comments.size
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
        val userIdTextView: TextView = itemView.findViewById(R.id.usernameTextView)  // 작성자 이름 표시할 TextView
    }
}
