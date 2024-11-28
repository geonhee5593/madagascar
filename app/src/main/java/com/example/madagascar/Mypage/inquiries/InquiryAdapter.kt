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
    private val onRespond: (userId: String, inquiryId: String, newResponse: String) -> Unit
) : ListAdapter<InquiryItem, InquiryAdapter.InquiryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InquiryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inquiry, parent, false)
        return InquiryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InquiryViewHolder, position: Int) {
        val inquiry = getItem(position)
        holder.bind(inquiry, onRespond)
    }

    class InquiryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(inquiry: InquiryItem, onRespond: (userId: String, inquiryId: String, newResponse: String) -> Unit) {
            val questionTextView = itemView.findViewById<TextView>(R.id.textQuestion)
            val timestampTextView = itemView.findViewById<TextView>(R.id.textTimestamp)
            val responseStatusTextView = itemView.findViewById<TextView>(R.id.textResponseStatus)
            val responseEditText = itemView.findViewById<EditText>(R.id.editTextResponse)
            val respondButton = itemView.findViewById<Button>(R.id.buttonRespond)

            questionTextView.text = inquiry.question
            timestampTextView.text = inquiry.timestamp
            responseStatusTextView.text = inquiry.responseStatus

            // Set text color based on response status
            val context = itemView.context
            val textColor = if (inquiry.responseStatus == "답변 미확인") {
                ContextCompat.getColor(context, R.color.red) // Define red in colors.xml
            } else {
                ContextCompat.getColor(context, R.color.blue) // Define blue in colors.xml
            }
            responseStatusTextView.setTextColor(textColor)

            // Handle response state
            val isAnswered = inquiry.responseStatus == "답변 완료"
            responseEditText.setText(inquiry.adminResponse ?: "")
            responseEditText.isEnabled = !isAnswered
            respondButton.isEnabled = !isAnswered
            respondButton.text = if (isAnswered) "답변 완료됨" else "답변 보내기"

            // Handle respond button click
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
