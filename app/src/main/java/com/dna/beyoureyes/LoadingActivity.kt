package com.dna.beyoureyes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dna.beyoureyes.databinding.ActivityLoadingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.File
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.launch


class LoadingActivity : AppCompatActivity() {
    private val handler = Handler()

    private val extractedWords = mutableSetOf<String>()

    private lateinit var resultbtn: Button
//    private lateinit var textView: TextView

    private val textRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    ) // 한글 텍스트 인식 인스턴스 생성
    private val pickImage = 100 // 이미지 선택 요청 코드
    private val koreanCharactersList = mutableListOf<String>() // 한글 문자를 담을 Set, 중복 피하기 위해 Set 자료구조 활용
    private var koreanCharactersListmodi = mutableListOf<String>()
    private val gList = mutableListOf<String>() // "숫자" + "g/mg" 를 담을 List
    private val percentList = mutableListOf<String>() // "숫자" + "%"를 담을 List
    private val kcalList = mutableListOf<String>() // "kcal"와 "g 당"을 담을 List
    private val keywords = listOf("나트", "탄수", "지방", "당류", "트랜스", "포화", "콜레", "단백") // 특정 키워드 List

    private lateinit var moPercentList: List<String> // % -> g 으로 변형하여 담을 List

    private lateinit var binding: ActivityLoadingBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //textView = findViewById(R.id.textView) // test 하기 위해.. 삭제예정

        resultbtn = binding.resultbtn


        val filePath = intent.getStringExtra("bitmapPath")

        if (filePath != null) {
            Log.d("YourTag", "File path: $filePath") // 파일 경로 로깅

            val f = File(filePath)
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap != null) {
                detectTextInBitmap(bitmap) // 필터링 알고리즘
            } else {
                Log.e("bitmap", "Bitmap is null") // 비트맵이 null일 때
            }
            f.delete()
        } else {
            Log.e("bitmap", "File path is null") // 파일 경로가 null일 때
        }

        ////////// OCR 인식 후 결과에 따라 분석 화면 제공 - 버튼 자동 눌림 /////////////
        resultbtn.setOnClickListener {

            Log.d("final_mo", moPercentList.toString())
            Log.d("final_kacl", kcalList.toString())
            val isValidData = isValidData()
            val isValidAllergyData = isValidData_alergy()
            val hasValidKeywordOrder = checkKeywordOrder(koreanCharactersListmodi)
            val isValidPercentData = isValidData_per()

            when {
                hasValidKeywordOrder && isValidData &&  isValidPercentData && isValidAllergyData  -> {
                    startFoodInfoAllActivity()
                }
                hasValidKeywordOrder && isValidData && isValidPercentData -> {
                    startFoodInfoNutritionActivity()
                }
                isValidAllergyData -> {
                    startFoodInfoAllergyActivity()
                }
                else -> {
                    val intent = Intent(this, CameraOcrproblemActivity::class.java)
                    startActivity(intent)
                }
            }
        }


        handler.postDelayed({
            resultbtn.performClick() // 버튼을 자동으로 클릭
        }, 4000) // 4초

    }

    ////////// OPEN API로 식품 분석 결과 가져올 클래스 /////////////
    data class NutrientResult(

        val prot: Int,   // 단백질
        val fatce: Int,  // 지방
        val chocdf: Int, // 탄수화물
        val sugar: Int,  // 당류
        val nat: Int,    // 나트륨
        val chole: Int,  // 콜레스테롤
        val fasat: Int   // 포화지방산

    )

    ////////// OPEN API 호출 함수 - 식품품목보고번호 이용 /////////////
    private suspend fun fetchFoodData(
        pageNo: String,
        numOfRows: String,
        type: String,
        itemMnftrRptNo: String
    ): List<NutrientResult>? {
        return try {
            val response = RetrofitClient.apiService.getFood(
                pageNo = pageNo,
                numOfRows = numOfRows,
                type = type,
                itemMnftrRptNp = itemMnftrRptNo
            )
            val items = response.response.body.items

            Log.d("Items_success", items.toString())
            ////////// foodSize 기준으로 mg 계산 함수 /////////////
            calculateNutrient(items)
        } catch (e: Exception) {
            Log.e("ApiResponse", "API 호출 실패: ${e.message}")
            null
        }
    }

    ////////// OPEN API 호출 결과를 moPercentList로 저장 함수 /////////////
    private fun processFoodData(itemMnftrRptNo: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            val nutrientResults = fetchFoodData("1", "100", "json", itemMnftrRptNo)
            if (!nutrientResults.isNullOrEmpty()) {
                // nutrientResults의 각 필드 값을 String으로 변환하여 리스트에 추가
                moPercentList = nutrientResults.flatMap { result ->
                    listOf(
                        result.prot.toString(),
                        result.fatce.toString(),
                        result.chocdf.toString(),
                        result.sugar.toString(),
                        result.nat.toString(),
                        result.chole.toString(),
                        result.fasat.toString()
                    )
                }

                Log.d("moPercentList", moPercentList.toString())

                Log.d("ApiResponse", "API 데이터 처리 성공: ${moPercentList}")
                Log.d("ApiResponse_kcal", "API 데이터 처리 성공: ${kcalList}")
            } else {
                Log.e("ApiResponse", "API 호출 실패. OCR 데이터를 사용합니다.")
            }
            onComplete()
        }
    }

    ////////// OPEN API로 가져온 데이터 foodSize 기준으로 mg 계산 함수 /////////////
    private fun calculateNutrient(items: List<ApiResponse.Item>): List<NutrientResult> {
        val result = mutableListOf<NutrientResult>()
        for (item in items) {
            val rawNatValue = item.nat
            val natDouble = rawNatValue?.toDoubleOrNull()
            Log.d("Debug_nat", "Raw value: $rawNatValue, Converted value: $natDouble")

            //val foodSize = item.foodSize.toDoubleOrNull() ?: 1.0
            // foodSize 디버깅
            val rawFoodSize = item.foodSize
            // 정규식을 사용하여 숫자만 추출
            val foodSize = rawFoodSize?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 1.0
            Log.d("Debug_foodSize", "Raw foodSize: $rawFoodSize, Extracted foodSize: $foodSize")


            val prot = ((item.prot.toDoubleOrNull() ?: 0.0)  / 100.0 * foodSize).toInt()
            val fatce = ((item.fatce.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt()
            val chocdf = ((item.chocdf.toDoubleOrNull() ?: 0.0)/ 100.0 * foodSize).toInt()
            val sugar = ((item.sugar.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt()
            val nat = ((item.nat.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt()
            val chole = ((item.chole.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt()
            val fasat = ((item.fasat.toDoubleOrNull() ?: 0.0)/ 100.0 * foodSize).toInt()
            Log.d("kcal_s", item.enerc)
            val kcal = ((item.enerc.toDoubleOrNull() ?: 0.0)/ 100.0 * foodSize).toInt().toString()

            Log.d("nat_succes", nat.toString())
            Log.d("kcal_succes", kcal)

            Log.d("Debug_Result_Values", "prot: $prot, fatce: $fatce, chocdf: $chocdf, sugar: $sugar, nat: $nat, chole: $chole, fasat: $fasat")


            val nutrientResult = NutrientResult(prot, fatce, chocdf, sugar, nat, chole, fasat)
            Log.d("Debug_NutrientResult", nutrientResult.toString())
            result.add(nutrientResult)
            kcalList.add(kcal)
            Log.d("Nutri", result.toString())
        }
        return result
    }


    private fun useTestInfo() {
        // 추출 키워드 값 임의 설정
        koreanCharactersList.clear()
        koreanCharactersList.add("나트륨")
        koreanCharactersList.add("탄수화물")
        koreanCharactersList.add("당류")
        koreanCharactersList.add("지방")
        koreanCharactersList.add("트랜스지방")
        koreanCharactersList.add("포화지방")
        koreanCharactersList.add("콜레스테롤")
        koreanCharactersList.add("단백질")



        koreanCharactersListmodi = koreanCharactersList.distinct().toMutableList()
        koreanCharactersListmodi = koreanCharactersListmodi.map {
            it.replace(Regex("[^가-힣]"), "")
        }.toMutableList()


        // % 리스트 값 임의 설정
        percentList.clear()
        percentList.add("17") // 나트륨%
        percentList.add("17") // 탄수화물%
        percentList.add("9") // 당류%
        percentList.add("20") // 지방%
        percentList.add("19") // 포화지방%
        percentList.add("5") // 콜레스테롤%
        percentList.add("13") // 단백질%

        // % -> g 리스트 값 설정
        moPercentList = modiPercentList(percentList)
        if (moPercentList.size == 0) {
            Log.d("test", "moPercentList is empty")
        }


        // kcal 리스트 값 설정
        kcalList.clear()
        kcalList.add("343")


        // 알레르기 값 설정
        extractedWords.clear()
        extractedWords.add("밀")
        extractedWords.add("땅콩")
        extractedWords.add("새우")
    }

    ////////// 모든 정보 받아오는데 성공 /////////////
    private fun startFoodInfoAllActivity() {
        val intent = Intent(this, FoodInfoAllActivity::class.java)
        intent.putExtra("modifiedPercentList", ArrayList(moPercentList))
        intent.putExtra("PercentList", ArrayList(percentList))
        intent.putStringArrayListExtra("modifiedKcalListText", ArrayList(kcalList))
        intent.putStringArrayListExtra("allergyList", ArrayList(extractedWords.toList()))
        startActivity(intent)

    }

    ////////// 영양소 함류량 정보만 받아오는데 성공 /////////////
    private fun startFoodInfoNutritionActivity() {
        val intent = Intent(this, FoodInfoNutritionActivity::class.java)
        configureIntent(intent)
        startActivity(intent)
    }

    ////////// 알레르기 정보만 받아오는데 성공 /////////////
    private fun startFoodInfoAllergyActivity() {
        val intent = Intent(this, FoodInfoAllergyActivity::class.java)
        intent.putStringArrayListExtra("allergyList", ArrayList(extractedWords.toList()))
        startActivity(intent)
    }

    private fun configureIntent(intent: Intent) {
        intent.putExtra("modifiedPercentList", ArrayList(moPercentList))
        intent.putExtra("PercentList", ArrayList(percentList))
        intent.putStringArrayListExtra("modifiedKcalListText", ArrayList(kcalList))
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // 핸들러 제거
    }

    ////////// OCR API 호출 시 - 한글 키워드 순서 확인 함수 /////////////
    private fun checkKeywordOrder(keywordList: List<String>): Boolean {
        val targetKeywords = listOf("나트", "탄수화", "당류", "지방", "트랜스", "포화", "콜레스", "단백질")


        //textView.append(keywordList.toString())

        var targetIndex = 0
        for (keyword in keywordList) {
            val matchingKeyword = targetKeywords.getOrNull(targetIndex)
            if (matchingKeyword != null && keyword.contains(matchingKeyword)) {
                targetIndex++
            }

            if (targetIndex == targetKeywords.size) {
                //textView.append("true")
                return true
            }
        }

        return false
    }

    private fun isValidData(): Boolean { // 퍼센트와 칼로리 유효성 판단

        return percentList.size == 7 && kcalList.size == 1 && percentList.all { it.isNotEmpty() } && kcalList.all { it.isNotEmpty() } && koreanCharactersListmodi.all { it.isNotEmpty() } && moPercentList.size == 7
    }

    private fun isValidData_alergy(): Boolean { // 알레르기 유효성 판단

        return extractedWords.isNotEmpty()
    }

    private fun isValidData_per(): Boolean {
        // 퍼센트가 100% 이상이거나 리스트가 비어 있을 때 false 반환 -> OCR 인식 오류 방지 위해
        for (percent in percentList) {
            try {
                if (percent.toInt() >= 100) {
                    return false
                }
            } catch (e: NumberFormatException) {
                // 숫자로 변환할 수 없는 경우에 대한 예외 처리
                return false
            }
        }
        return percentList.isNotEmpty()
    }

    // 알림 다이얼로그 표시 함수
    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("알림")
            .setMessage(message)
            .setPositiveButton("확인") { _, _ -> }
            .show()
    }

    ////////// OPEN API 호출 시 - 품목보고번호 문자 추출 /////////////
    private fun extractItemMnftrRptNo(lines: List<String>): String? {
        val keyword = "품목보고번호"
        val itemMnftrRptNoPattern = "\\d+".toRegex()

        for (lineText in lines) {
            if (lineText.contains(keyword)) {
                // "품목보고번호" 바로 옆에 있는 번호 추출
                val keywordIndex = lineText.indexOf(keyword) + keyword.length
                val numberPart = lineText.substring(keywordIndex).trim()
                val match = itemMnftrRptNoPattern.find(numberPart)

                if (match != null) {
                    val itemMnftrRptNo = match.value
                    Log.d("ItemMnftrRptNo", "Found itemMnftrRptNo: $itemMnftrRptNo")
                    return itemMnftrRptNo
                } else {
                    Log.d("ItemMnftrRptNo", "No number found next to keyword '$keyword' in the line.")
                }
            }
        }
        //Log.d("ItemMnftrRptNo", "Keyword '$keyword' not found in the text.")
        return null
    }

    /*
    1. "품목보고번호" 추출 성공하면 -> OPEN API 호출
    2. "품목보고번호" 추출 실패하면 -> OCR API 호출
    */
    private fun detectTextInBitmap(bitmap: Bitmap) {
        try {
            // 초기화
            koreanCharactersList.clear()
            koreanCharactersListmodi.clear()
            gList.clear()
            percentList.clear()

            val image = InputImage.fromBitmap(bitmap, 0)
            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    val lines = result.text.split('\n')
                    val itemMnftrRptNo = extractItemMnftrRptNo(lines)

                    if (itemMnftrRptNo != null) {
                        // 품목보고번호가 있는 경우
                        Log.d("success", itemMnftrRptNo)
                        processFoodData(itemMnftrRptNo) {
                            // OPEN API 호출 성공 시 알레르기 추출
                            Log.d("open_api_result", moPercentList.toString())
                            if (moPercentList.isNotEmpty()) {
                                extractAllergyData(lines)
                            } else {
                                // OPEN API 실패 시 OCR API로 처리
                                extractOcrData(result)
                            }
                        }
                    } else {
                        // 품목보고번호가 없는 경우: OCR 데이터 처리
                        extractOcrData(result)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    showAlertDialog("네트워크를 연결해주세요 또는 API 연동 중이거나 적합하지 않은 이미지일 수 있습니다.")
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error", "Exception occurred: ${e.message}", e)
        }
    }

    ////////// 알레르기 추출 함수 /////////////
    private fun extractAllergyData(lines: List<String>) {
        val targetWords = listOf(
            "메밀", "밀", "대두", "땅콩", "호두", "잣", "계란",
            "아황산류", "복숭아", "토마토", "난류", "우유", "새우",
            "고등어", "오징어", "게", "조개류", "돼지고기", "쇠고기", "닭고기"
        )

        var foundHamuIndex: Int? = null

        for ((index, lineText) in lines.withIndex()) {
            if (lineText.contains("함유")) {
                foundHamuIndex = index

                val previousLineIndex = index - 1
                if (previousLineIndex >= 0) {
                    val previousLineText = lines[previousLineIndex]
                    for (targetWord in targetWords) {
                        if (previousLineText.contains(targetWord)) {
                            extractedWords.add(targetWord)
                        }
                    }
                }
            }

            if (foundHamuIndex != null && foundHamuIndex == index) {
                for (targetWord in targetWords) {
                    if (lineText.contains(targetWord)) {
                        extractedWords.add(targetWord)
                    }
                }
            }
        }

        Log.d("AllergyData", "추출된 알레르기 데이터: $extractedWords")
    }

    ////////// OCR API 호출 함수 /////////////
    private fun extractOcrData(result: Text) {
        for (block in result.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text

                // OCR에서 데이터를 추출 : mg, %, kca, g당
                extractNumberG(lineText)
                extractPercent(lineText)
                extractNumberKcal(lineText)
                // extractNumberGBracket(lineText)

                // 키워드 처리
                for (element in line.elements) {
                    val elementText = element.text
                    for (keyword in keywords) {
                        if (elementText.contains(keyword)) {
                            koreanCharactersList.add(elementText)
                        }
                    }
                }
            }
        }

        // 중복 제거 및 데이터 정제
        koreanCharactersListmodi = koreanCharactersList.distinct().toMutableList()
        koreanCharactersListmodi = koreanCharactersListmodi.map { it.replace(Regex("[^가-힣]"), "") }.toMutableList()

        // OCR 데이터를 기반으로 moPercentList 생성
        showResults()
//        moPercentList = modiPercentList(percentList)
//        if (moPercentList.isEmpty()) {
//            Log.e("OCRFallback", "OCR 데이터가 유효하지 않습니다.")
//        } else {
//            Log.d("OCRFallback", "OCR 데이터 처리 성공: $moPercentList")
//        }
    }

    //"숫자 g" 형태를 추출하는 함수 - g, mg, 9 앞에 숫자를 추출함
    private fun extractNumberG(lineText: String) {
        val regex = """(\d+(\.\d+)?)\s*(g|mg|9)(?!\s*당)""".toRegex() // "숫자" + "당"은 제외
        val matchResults = regex.findAll(lineText)
        for (matchResult in matchResults) {
            val (number) = matchResult.destructured

            val openingIndex = lineText.indexOf('(') // "(" + "숫자" 는 제외
            if (openingIndex == -1 || openingIndex > matchResult.range.first) {
                gList.add(number)
            }
        }
    }

    // "%" 형태를 추출하는 함수
    private fun extractPercent(lineText: String) {
        val percentRegex = """(\d+(\.\d+)?)\s*%""".toRegex()
        val percentMatchResults = percentRegex.findAll(lineText)
        for (percentMatchResult in percentMatchResults) {
            val (percentNumber) = percentMatchResult.destructured
            val intValue = (percentNumber.toIntOrNull() ?: 0).toString()
            percentList.add(intValue)
        }
    }

    // "숫자 kcal" 형태를 추출하는 함수
    private fun extractNumberKcal(lineText: String) {
        val kcalRegex = """(\d+(\.\d+)?)\s*(kcal)""".toRegex()
        val kcalMatchResults = kcalRegex.findAll(lineText)
        for (kcalMatchResult in kcalMatchResults) {
            val (kcalNumber) = kcalMatchResult.destructured
            kcalList.add(kcalNumber + "kcal")
        }

    }


    // "g 당" 및 "총 내용량 + 숫자" 형태를 추출하는 함수
    private fun extractNumberGBracket(lineText: String) {
        val gPattern = """(\d+(\.\d+)?)\s*(g)\)""".toRegex() // "숫자" + "g)"
        val gDangPattern = """(\d+(\.\d+)?)\s*(g|mg|9)\s*당""".toRegex() // g/mg/9 + "당"

        val gMatches = gPattern.findAll(lineText)
        for (matchResult in gMatches) {
            val (number) = matchResult.destructured
            kcalList.add(number)
        }

        val gDangMatches = gDangPattern.findAll(lineText)
        for (matchResult in gDangMatches) {
            val (number) = matchResult.destructured
            kcalList.add(number)
        }
    }


    // 칼로리 필터링 및 변형 함수
    private fun showResults() {

        // "kcal" 리스트에서 "000kcal" 및 "2000kcal" 제거
        kcalList.removeAll(listOf("000kcal", "2.000kcal", "2000kcal", "2,000kcal"))

        // kcalList에 항목이 하나만 있는지 확인, 계산 필요하지 확인 위해
        if (kcalList.size == 1) { // 항목이 하나면 계산이 필요하지 않음. "숫자" + "kcal"에서 "kcal"를 제거
            val kcalValue = kcalList[0].replace("[^\\d.]".toRegex(), "")
            kcalList.clear()
            kcalList.add(kcalValue)
        } else if (kcalList.size == 2) { // 항목이 두개면 총 kcal 계산 필요
            // 정규 표현식을 사용하여 "kcal" 부분을 제거
            val firstKcal = kcalList[0].replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val secondKcal = kcalList[1].replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 1.0 // 기본값은 1.0으로 설정

            if (secondKcal != 0.0) {
                val result = firstKcal / secondKcal // 1g당 칼로리 계산

                // gList의 첫번째 인덱스 값 가져옴
                val gListValue = if (gList.isNotEmpty()) gList[0].toDoubleOrNull() ?: 1.0 else 1.0

                // 총 kcal 값 계산
                val resultWithoutUnit = (result * gListValue).toInt().toString()

                kcalList.clear() // kcalList 비우고 계산된 결과 담기
                kcalList.add(resultWithoutUnit)
            }else {
                // 두 번째 값이 0이면 나눌 수 없음을 나타내는 메시지 출력
                //println("0이라 계산할 수 없습니다.")
            }
        }

        // g계산 알고리즘 함수 결과
        moPercentList = modiPercentList(percentList)

    }

}


// % 를 이용하여 mg으로 계산
private fun modiPercentList(percentList: List<String>): List<String> {
    if (percentList.size != 7) {
        // 퍼센트 리스트의 길이가 7이 아니면 빈 리스트를 반환
        return emptyList()
    }

    val modifiedList = percentList.mapIndexed { index, percent ->
        // 선행하는 0을 제거
        //val cleanedPercent = percent.trimStart('0').toDoubleOrNull() ?: 0.0

        val modifiedPercent = when (index) {
            0 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 2000).toInt()).toString()
            1 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 324 * 1000).toInt()).toString()
            2 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 100 * 1000).toInt()).toString()
            3 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 54 * 1000).toInt()).toString()
            4 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 15 * 1000).toInt()).toString()
            5 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 300).toInt()).toString()
            6 -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 55 * 1000).toInt()).toString()
            else -> (((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 2000).toInt()).toString() // 기본값
        }
        modifiedPercent
    }

    return modifiedList
}