package com.dna.beyoureyes

import android.util.Log

/*
1. 알레르기 싱글톤 객체
- 알레르기 추출 함수 담고 있음
*/

object AllergyProcessor{
    private val allergyTargetWords = listOf(
        "메밀", "밀", "대두", "땅콩", "호두", "잣", "계란",
        "아황산류", "복숭아", "토마토", "난류", "우유", "새우",
        "고등어", "오징어", "게", "조개류", "돼지고기", "쇠고기", "닭고기"
    )

    private val extractedWords = mutableSetOf<String>()

    fun extracteAllergyData(lines: List<String>): Set<String> {
        extractedWords.clear()
        val hamuIndices = findHamuIndices(lines)
        hamuIndices.forEach{index ->
            extractWordsFirstLine(lines, index)
            extractWordsPrevioueLine(lines, index)
        }
        Log.d("Allergy", "추출된 알레르기 데이터 확인 : $extractedWords")

        return extractedWords
    }

    // "함유" 키워드 있는 라인 위치 추출
    private fun findHamuIndices(lines: List<String>): List<Int> {
        // 리스트에서 하나씩 비교하는 Kotlin 컬렉션 함수
        return lines.mapIndexedNotNull { index, line ->
            if (line.contains("함유")) index else null
        }
    }


    // 첫번째 라인에서 알레르기 추출
    private fun extractWordsFirstLine(lines: List<String>, index : Int) {
        val lineText = lines[index]
        allergyTargetWords.forEach{ targetWord ->
            if (lineText.contains(targetWord)) {
                extractedWords.add(targetWord)
            }
        }
    }

    // 이전 라인에서 알레르기 추출
    private fun extractWordsPrevioueLine(lines: List<String>, index: Int) {
        val previousLineIndex = index - 1
        if (previousLineIndex >= 0) {
            val previousLineText = lines[previousLineIndex]
            allergyTargetWords.forEach { targetWord ->
                if (previousLineText.contains(targetWord)) {
                    extractedWords.add(targetWord)
                }
            }
        }
    }
    
}