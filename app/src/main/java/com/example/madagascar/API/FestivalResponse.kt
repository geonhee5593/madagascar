package com.example.madagascar.API

data class FestivalResponse(
    val response: ResponseBody
)

data class ResponseBody(
    val body: BodyItems
)

data class BodyItems(
    val items: ItemsList
)

data class ItemsList(
    val item: List<FestivalItem>
)

data class FestivalItem(
    val title: String,
    val addr1: String,
    val eventstartdate: String,
    val eventenddate: String,
    val firstimage: String?
)