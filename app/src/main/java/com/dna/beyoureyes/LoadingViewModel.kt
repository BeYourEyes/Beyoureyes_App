package com.dna.beyoureyes

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.play.integrity.internal.e
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

class LoadingViewModel : ViewModel() {
    val foodData = FoodData()
    private val _moPercentList = MutableLiveData<List<String>>()
    val moPercentList: LiveData<List<String>> get() = _moPercentList
    private val _isValidData = MutableLiveData<Boolean>()
    val isValidData: LiveData<Boolean> get() = _isValidData
    private val _isValidAllergyData = MutableLiveData<Boolean>()
    val isValidAllergyData: LiveData<Boolean> get() = _isValidAllergyData
    private val _hasValidKeywordOrder = MutableLiveData<Boolean>()
    val hasValidKeywordOrder: LiveData<Boolean> get() = _hasValidKeywordOrder
    private val _isValidPercentData = MutableLiveData<Boolean>()
    val isValidPercentData: LiveData<Boolean> get() = _isValidPercentData

    private val textRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    fun detectTextInBitmap(bitmap: Bitmap) {
        foodData.koreanCharactersList.clear()
        foodData.percentList.clear()
        foodData.koreanCharactersListmodi.clear()
        foodData.gList.clear()
//        foodData.kcalList.clear()
        //foodData.extractedWords.clear()

        val image = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(image)
            .addOnSuccessListener { result ->
                var foundHamuIndex: Int? = null // 함유 인덱스 파악 위해

                val lines = result.text.split('\n')

                for ((index, lineText) in lines.withIndex()) {
                    if (lineText.contains("함유")) {
                        foundHamuIndex = index

                        val previousLineIndex = index - 1
                        if (previousLineIndex >= 0) {
                            val previousLineText = lines[previousLineIndex]
                            for (targetWord in foodData.targetWords) {
                                if (previousLineText.contains(targetWord)) {
                                    foodData.extractedWords.add(targetWord)
                                }
                            }
                        }
                    }

                    if (foundHamuIndex != null && foundHamuIndex == index) {
                        for (targetWord in foodData.targetWords) {
                            if (lineText.contains(targetWord)) {
                                foodData.extractedWords.add(targetWord)
                            }
                        }
                    }

                }
                val allRecognizedWords = result.text
                val extractedText = foodData.extractedWords.joinToString(", ")

                for (block in result.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val elementText = element.text
                            //특정 키워드 확인, 한글 Set는 요소별로 추출
                            for (keyword in foodData.keywords) {
                                if (elementText.contains(keyword)) {
                                    // 키워드를 한글 문자 Set에 추가
                                    foodData.koreanCharactersList.add(elementText)
                                    // replaceKoreanCharacters(koreanCharactersSet)
                                }
                            }
                        }
                        val lineText = line.text
                        val lineInfo = "라인 텍스트: $lineText" // 전체 인식한 라인 텍스트 출력


                        // "숫자 g" 형태 확인
                        extractNumberG(lineText)

                        // "%" 형태 확인
                        extractPercent(lineText)

                        // "숫자 kcal, 숫자 g당" 형태 확인
                        extractNumberKcal(lineText)

                        // "숫자 g)" 형태 확인
                        extractNumberGBracket(lineText)
                    }

                }
                // 이미지 처리 후에 결과를 출력
                showResults()
                foodData.koreanCharactersListmodi =
                    foodData.koreanCharactersList.distinct().toMutableList()
                foodData.koreanCharactersListmodi =
                    foodData.koreanCharactersListmodi.map { it.replace(Regex("[^가-힣]"), "") }
                        .toMutableList()


            }
            .addOnFailureListener { e ->

                e.printStackTrace()
                //showAlertDialog("네트워크를 연결해주세요 또는 API 연동 중이거나 적합하지 않은 이미지일 수 있습니다.")
            }
    }

    private fun extractNumberG(lineText: String) {
        val regex = """(\d+(\.\d+)?)\s*(g|mg|9)(?!\s*당)""".toRegex()
        regex.findAll(lineText).forEach { matchResult ->
            val (number) = matchResult.destructured
            if (!lineText.contains("(")) {
                foodData.percentList.add(number)
            }
        }
    }
    private fun extractPercent(lineText: String) {
        val percentRegex = """(\d+(\.\d+)?)\s*%""".toRegex()
        percentRegex.findAll(lineText).forEach { matchResult ->
            val (percentNumber) = matchResult.destructured
            val intValue = (percentNumber.toIntOrNull() ?: 0).toString()
            foodData.percentList.add(intValue)
        }
    }
    private fun extractNumberKcal(lineText: String) {
        val kcalRegex = """(\d+(\.\d+)?)\s*(kcal)""".toRegex()
        kcalRegex.findAll(lineText).forEach { matchResult ->
            val (kcalNumber) = matchResult.destructured
            foodData.kcalList.add(kcalNumber + "kcal")
        }
    }
    private fun extractNumberGBracket(lineText: String) {
        val gPattern = """(\d+(\.\d+)?)\s*(g)\)""".toRegex()
        val gDangPattern = """(\d+(\.\d+)?)\s*(g|mg|9)\s*당""".toRegex()

        gPattern.findAll(lineText).forEach { matchResult ->
            val (number) = matchResult.destructured
            foodData.kcalList.add(number)
        }

        gDangPattern.findAll(lineText).forEach { matchResult ->
            val (number) = matchResult.destructured
            foodData.kcalList.add(number)
        }
    }
    // 칼로리 필터링 및 변형 함수, 함수 이름 수정 예정
    private fun showResults() {

        // "kcal" 리스트에서 "000kcal" 및 "2000kcal" 제거
        foodData.kcalList.removeAll(listOf("000kcal", "2.000kcal", "2000kcal", "2,000kcal"))

        // kcalList에 항목이 하나만 있는지 확인, 계산 필요하지 확인 위해
        if (foodData.kcalList.size == 1) { // 항목이 하나면 계산이 필요하지 않음. "숫자" + "kcal"에서 "kcal"를 제거
            val kcalValue = foodData.kcalList[0].replace("[^\\d.]".toRegex(), "")
            foodData.kcalList.clear()
            foodData.kcalList.add(kcalValue)
        } else if (foodData.kcalList.size == 2) { // 항목이 두개면 총 kcal 계산 필요
            // 정규 표현식을 사용하여 "kcal" 부분을 제거
            val firstKcal = foodData.kcalList[0].replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val secondKcal = foodData.kcalList[1].replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 1.0 // 기본값은 1.0으로 설정

            if (secondKcal != 0.0) {
                val result = firstKcal / secondKcal // 1g당 칼로리 계산

                // gList의 첫번째 인덱스 값 가져옴
                val gListValue = if (foodData.gList.isNotEmpty()) foodData.gList[0].toDoubleOrNull() ?: 1.0 else 1.0

                // 총 kcal 값 계산
                val resultWithoutUnit = (result * gListValue).toInt().toString()

                foodData.kcalList.clear() // kcalList 비우고 계산된 결과 담기
                foodData.kcalList.add(resultWithoutUnit)
            }else {
                // 두 번째 값이 0이면 나눌 수 없음을 나타내는 메시지 출력
                //println("0이라 계산할 수 없습니다.")
            }
        }

        // g계산 알고리즘 함수 결과
        foodData.moPercentList = modiPercentList(foodData.percentList)

    }


    fun checkKeywordOrder(keywordList: List<String>): Boolean {
        val targetKeywords = listOf("나트", "탄수화", "당류", "지방", "트랜스", "포화", "콜레스", "단백질")
        var targetIndex = 0
        for (keyword in keywordList) {
            if (targetIndex < targetKeywords.size && keyword.contains(targetKeywords[targetIndex])) {
                targetIndex++
            }
            if (targetIndex == targetKeywords.size) return true
        }
        return false
    }
    fun isValidData(): Boolean {
        return foodData.percentList.size == 7 && foodData.kcalList.size == 1 &&
                foodData.percentList.all { it.isNotEmpty() } && foodData.kcalList.all { it.isNotEmpty() } &&
                foodData.koreanCharactersList.all { it.isNotEmpty() } && _moPercentList.value?.size == 7
    }

    fun isValidDataAlergy(): Boolean {
        return foodData.extractedWords.isNotEmpty()
    }

    fun isValidDataPer(): Boolean {
        return foodData.percentList.all { it.toIntOrNull() ?: 0 < 100 } && foodData.percentList.isNotEmpty()
    }

    private fun modiPercentList(percentList: List<String>): List<String> {
        if (percentList.size != 7) return emptyList()

        return percentList.mapIndexed { index, percent ->
            when (index) {
                0 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 2000).toInt()).toString()
                1 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 324 * 1000).toInt()).toString()
                2 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 100 * 1000).toInt()).toString()
                3 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 54 * 1000).toInt()).toString()
                4 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 15 * 1000).toInt()).toString()
                5 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 15 * 1000).toInt()).toString()
                6 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 55).toInt()).toString()
                else -> percent
            }
        }
    }
}