package com.dna.beyoureyes

data class FoodData(
    val koreanCharactersList: MutableList<String> = mutableListOf(),
    val percentList: MutableList<String> = mutableListOf(),
    var koreanCharactersListmodi : MutableList<String> =  mutableListOf(),
    val kcalList: MutableList<String> = mutableListOf(),
    val gList : MutableList<String> = mutableListOf(),
    var moPercentList: List<String> = mutableListOf(),
    val extractedWords: MutableSet<String> = mutableSetOf(),
    val keywords: List<String> = listOf("나트", "탄수", "지방", "당류", "트랜스", "포화", "콜레", "단백"),
    val targetWords: List<String> = listOf(
        "메밀", "밀", "대두", "땅콩", "호두", "잣", "계란", "아황산류", "복숭아",
        "토마토", "난류", "우유", "새우", "고등어", "오징어", "게", "조개류",
        "돼지고기", "쇠고기", "닭고기"
    )
)