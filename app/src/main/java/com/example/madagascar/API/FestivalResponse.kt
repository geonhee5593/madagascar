package com.example.madagascar.API

import com.google.gson.annotations.SerializedName

data class FestivalResponse(
    val response: FestivalBody
)

data class FestivalBody(
    val body: FestivalItems
)

data class FestivalItems(
    val items: FestivalItemList
)

data class FestivalItemList(
    val item: List<FestivalItem>
)

data class FestivalItem(
    val title: String,
    val addr1: String,
    val addr2: String?,
    @SerializedName("eventstartdate") val eventStartDate: String,
    @SerializedName("eventenddate") val eventEndDate: String,
    @SerializedName("firstimage") val firstImage: String?,
    @SerializedName("firstimage2") val firstImage2: String?,
    @SerializedName("contentid") val contentId: String,
    @SerializedName("tel") val tel: String?,
    @SerializedName("eventplace") val eventPlace: String?
)

// 공통정보 조회 모델
data class CommonResponse(
    val response: CommonBody
)

data class CommonBody(
    val body: CommonItems
)

data class CommonItems(
    val items: CommonItemList
)

data class CommonItemList(
    val item: List<CommonItem>
)

data class CommonItem(
    val title: String,
    val firstimage: String?,
    val addr1: String?,
    val homepage: String?,
    val overview: String?,
    val tel: String?,
    val telname: String?
)

// 소개정보 조회 모델
data class IntroResponse(
    val response: IntroBody
)

data class IntroBody(
    val body: IntroItems
)

data class IntroItems(
    val items: IntroItemList
)

data class IntroItemList(
    val item: List<IntroItem>
)

data class IntroItem(
    val sponsor1: String?,
    val sponsor1tel: String?,
    val eventenddate: String,
    val playtime: String?,
    val eventplace: String?,
    val eventStartdate: String,
    val usetimefestival: String?
)

data class Category(
    val name: String,
    val icon: Int
)