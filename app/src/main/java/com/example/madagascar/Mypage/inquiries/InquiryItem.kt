package com.example.madagascar.Mypage.inquiries

data class InquiryItem(
    var userId: String = "",
    var inquiryId: String = "",
    var question: String = "",
    var timestamp: String = "",
    var username: String = "",
    var responseStatus: String = "답변 미확인", // 기본값 설정
    var adminResponse: String? = null // 기본값 설정
)
