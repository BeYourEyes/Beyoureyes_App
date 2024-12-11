package com.dna.beyoureyes
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

/*
3. OCR API 싱글톤 객체
*/
object OCRProcessor {
    private val textRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    // 결과 리스트 초기화
    private val koreanCharactersList = mutableListOf<String>()
    private val percentList = mutableListOf<String>()
    private val kcalList = mutableListOf<String>()
    private val gList = mutableListOf<String>()

    private val keywords = listOf("나트", "탄수", "지방", "당류", "트랜스", "포화", "콜레", "단백")


    // OCR 텍스트 인식 실행
    fun processImage(bitmap: Bitmap, onSuccess: (List<String>, List<String>, List<String>, List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        // 결과 초기화
        koreanCharactersList.clear()
        percentList.clear()
        kcalList.clear()
        gList.clear()

        textRecognizer.process(image)
            .addOnSuccessListener { result ->
                processTextBlocks(result)
                onSuccess(koreanCharactersList, percentList, kcalList, gList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // OCR 텍스트 블록 처리
    private fun processTextBlocks(result: Text) {
        for (block in result.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text
                extractKoreanKeywords(lineText)
                extractPercent(lineText)
                extractKcal(lineText)
                extractGData(lineText)
            }
        }
    }


    // 한글 키워드 추출
    private fun extractKoreanKeywords(lineText: String) {
        for (keyword in keywords) {
            if (lineText.contains(keyword)) {
                koreanCharactersList.add(lineText)
            }
        }
    }

    // 형태 추출
    private fun extractPercent(lineText: String) {
        val percentRegex = """(\d+(\.\d+)?)\s*%""".toRegex()
        val matches = percentRegex.findAll(lineText)
        for (match in matches) {
            val cleanedPercent = match.groupValues[1].trimStart('0')
            percentList.add(cleanedPercent)
        }
    }


    // kcal 추출
    private fun extractKcal(lineText: String) {
        val kcalRegex = """(\d+(\.\d+)?)\s*(kcal)""".toRegex()
        val matches = kcalRegex.findAll(lineText)
        for (match in matches) {
            kcalList.add(match.groupValues[1] + "kcal")
        }
    }

    // "숫자 g" 형태 추출
    private fun extractGData(lineText: String) {
        val gRegex = """(\d+(\.\d+)?)\s*(g|mg|9)(?!\s*당)""".toRegex()
        val matches = gRegex.findAll(lineText)
        for (match in matches) {
            gList.add(match.groupValues[1])
        }
    }

    // %를 이용하여 mg으로 변환
    fun convertPercentToMg(percentList: List<String>): List<String> {
        if (percentList.size != 7) return emptyList()

        return percentList.mapIndexed { index, percent ->
            val cleanedPercent = percent.trimStart('0').toDoubleOrNull() ?: 0.0

            when (index) {
                0 -> ((cleanedPercent * 0.01 * 2000).toInt()).toString()
                1 -> ((cleanedPercent * 0.01 * 324 * 1000).toInt()).toString()
                2 -> ((cleanedPercent * 0.01 * 100 * 1000).toInt()).toString()
                3 -> ((cleanedPercent * 0.01 * 54 * 1000).toInt()).toString()
                4 -> ((cleanedPercent * 0.01 * 15 * 1000).toInt()).toString()
                5 -> ((cleanedPercent * 0.01 * 300).toInt()).toString()
                6 -> ((cleanedPercent * 0.01 * 55 * 1000).toInt()).toString()
                else -> ((cleanedPercent * 0.01 * 2000).toInt()).toString()
            }
        }
    }
}