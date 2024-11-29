package com.example.madagascar.Mypage.inquiries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R

class InquiryAdapter(
    private val onRespond: (userId: String, inquiryId: String, newResponse: String) -> Unit,
    private val showUnanswered: Boolean, // 필터 상태
    private val isUser: Boolean // 유저 화면인지 여부
) : ListAdapter<InquiryItem, InquiryAdapter.InquiryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InquiryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inquiry, parent, false)
        return InquiryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InquiryViewHolder, position: Int) {
        val inquiry = getItem(position)
        holder.bind(inquiry, onRespond, showUnanswered, isUser)
    }

    class InquiryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            inquiry: InquiryItem,
            onRespond: (userId: String, inquiryId: String, newResponse: String) -> Unit,
            showUnanswered: Boolean,
            isUser: Boolean
        ) {
            val usernameTextView = itemView.findViewById<TextView>(R.id.textUsername) // 닉네임 TextView
            val questionTextView = itemView.findViewById<TextView>(R.id.textQuestion)
            val timestampTextView = itemView.findViewById<TextView>(R.id.textTimestamp)
            val responseStatusTextView = itemView.findViewById<TextView>(R.id.textResponseStatus)
            val responseEditText = itemView.findViewById<EditText>(R.id.editTextResponse)
            val respondButton = itemView.findViewById<Button>(R.id.buttonRespond)

            // 닉네임 설정
            usernameTextView.text = "닉네임: ${inquiry.username}"

            // 문의 내용 및 기타 정보 설정
            questionTextView.text = inquiry.question
            timestampTextView.text = inquiry.timestamp
            responseStatusTextView.text = inquiry.responseStatus

            // 상태별 텍스트 색상 설정
            val context = itemView.context
            val textColor = if (inquiry.responseStatus == "답변 미확인") {
                ContextCompat.getColor(context, R.color.red)
            } else {
                ContextCompat.getColor(context, R.color.blue)
            }
            responseStatusTextView.setTextColor(textColor)

            // 유저 화면에서는 "답변 보내기" 버튼 숨김
            respondButton.visibility = if (isUser) View.GONE else View.VISIBLE

            // 답변 상태에 따른 입력 필드 설정 (읽기 전용 또는 활성화)
            val isAnswered = inquiry.responseStatus == "답변 완료"
            responseEditText.setText(inquiry.adminResponse ?: "")
            responseEditText.isEnabled = !isAnswered && !isUser
            responseEditText.hint = if (isUser) "관리자의 답변을 기다리는 중입니다." else "답변 입력"

            respondButton.setOnClickListener {
                val response = responseEditText.text.toString()
                if (response.isNotEmpty()) {
                    onRespond(inquiry.userId, inquiry.inquiryId, response)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<InquiryItem>() {
            override fun areItemsTheSame(oldItem: InquiryItem, newItem: InquiryItem) =
                oldItem.inquiryId == newItem.inquiryId

            override fun areContentsTheSame(oldItem: InquiryItem, newItem: InquiryItem) =
                oldItem == newItem
        }
    }
}



