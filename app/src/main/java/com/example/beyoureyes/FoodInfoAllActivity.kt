package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class FoodInfoAllActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private lateinit var personalButton: Button

    private var user: UserInfo? = null

    val nutri = listOf("나트륨", "탄수화물", "ㄴ당류", "지방", "ㄴ포화지방", "콜레스테롤", "단백질")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_all)
        //toolBar
        val toolBar = findViewById<Toolbar>(R.id.toolbarDefault)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarBackButton = findViewById<ImageButton>(R.id.toolbarBackBtn)


        setSupportActionBar(toolBar)
        //Toolbar에 앱 이름 표시 제거!!
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.setText("영양 분석 결과")



        toolbarBackButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            //overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_enter)
        }
        // TextToSpeech 초기화
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.KOREAN)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported or missing data")
                } else {
                    // TTS 초기화 성공
                    Log.d("TTS", "TextToSpeech initialization successful")
                }
            } else {
                Log.e("TTS", "TextToSpeech initialization failed")
            }
        }
        fun speak(text: String) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val params = Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
            } else {
                // LOLLIPOP 이하의 버전에서는 UtteranceId를 지원하지 않음
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        // 버튼 초기화
        speakButton = findViewById(R.id.buttonVoice)

        // 알러지 정보 intent하여 표시
        val allergyChipGroup: ChipGroup = findViewById<ChipGroup>(R.id.allergyChipGroup1)
        val modifiedKcalList = intent.getStringArrayListExtra("modifiedKcalListText")
        val Percent = intent.getStringArrayListExtra("PercentList")
        val moPercentList = intent.getStringArrayListExtra("modifiedPercentList")
        val allergyList = intent.getStringArrayListExtra("allergyList")

        if (allergyList != null) {
            for (diseaseItem in allergyList) {
                val chip = Chip(this)
                chip.text = diseaseItem

                // Chip 뷰의 크기 및 여백 설정
                val params = ChipGroup.LayoutParams(
                    250, // 넓이 80
                    150  // 높이 50
                )
                params.setMargins(8, 8, 8, 8) // 여백을 8로..
                chip.layoutParams = params

                // 글씨 크기
                chip.textSize = 25f

                // 가운데 정렬
                chip.textAlignment = View.TEXT_ALIGNMENT_CENTER

                allergyChipGroup.addView(chip)
            }
        }

        // 칼로리 intent 하여 kcalTextView에 표시
        val kcalText: TextView = findViewById(R.id.textView5)

        if (modifiedKcalList != null) {
            kcalText.text = modifiedKcalList.joinToString(", ") + " kcal"
        }

        // 원형 차트 (영양성분 이름  + 해당 g) intent해서 표시
        val chart = findViewById<PieChart>(R.id.pieChartScanSuccess)
        chart.setUsePercentValues(true)
        val entries = ArrayList<PieEntry>()
        // LoadingActivity 에서 데이터 받기
        val koreanCharacterList = listOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")

        println(Percent)

        // 단백질, 탄수화물, 지방
        if (Percent != null) {

            entries.add(PieEntry(Percent[1].toFloat(), koreanCharacterList[1]))
            entries.add(PieEntry(Percent[3].toFloat(), koreanCharacterList[3]))
            entries.add(PieEntry(Percent[6].toFloat(), koreanCharacterList[6]))
        }

        // 색깔 적용
        val colors = listOf(
            Color.parseColor("#C2FF00"),
            Color.parseColor("#F1BC00"),
            Color.parseColor("#FFC2E5")
        )

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.apply {
            // Piechart 속 파이들 색상 설정
            setColors(colors)
            // 값(백분율)에 대한 색상 설정
            valueTextColor = Color.BLACK
            // 값에 대한 크기 설정
            valueTextSize = 20f
        }

        val pieData = PieData(pieDataSet)
        // 값에 사용자 정의 형식(백분율 값 + "%") 설정
        pieDataSet.valueFormatter = object : ValueFormatter() { // 값을 차트에 어떻게 표시할지
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%" // 값을 정수 형식으로 표시
            }
        }

        chart.apply {
            data = pieData
            chart.setEntryLabelTextSize(20f)
            description.isEnabled = false // 차트 설명 비활성화
            isRotationEnabled = false // 차트 회전 활성화
            legend.isEnabled = false // 하단 설명 비활성화
            isDrawHoleEnabled = true // 가운데 빈 구멍 활성화 비활성화 여부
            holeRadius = 20f // 가운데 빈 구멍 크기
            transparentCircleRadius = 40f // 투명한 부분 크기
            centerText = null // 가운데 텍스트 없앰
            setEntryLabelColor(Color.BLACK) // label 색상
            animateY(1400, Easing.EaseInOutQuad) // 1.4초 동안 애니메이션 설정
            animate()
        }
        // 버튼 눌렀을 때 TTS 실행 -> 수정예정
        speakButton.setOnClickListener {
            val calorieText = "칼로리는 $modifiedKcalList 입니다."
            val nutrientsText = buildString {
                for (i in koreanCharacterList.indices) {
                    append("${koreanCharacterList[i]}은 ${Percent?.get(i)}%")
                    if (i < koreanCharacterList.size - 1) {
                        append(", ")
                    }
                }
            }

            val allergyText = "해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다."


            val textToSpeak = "영양 정보를 분석해드리겠습니다. $allergyText $calorieText 또한 영양 성분 정보는 일일 권장량 당 $nutrientsText 입니다."
            speak(textToSpeak)
        }

        // Percent 리스트의 크기
        val percentSize = Percent?.size ?: 0

        // 각각의 line_percent TextView에 Percent 리스트의 값 적용
        for (i in 0 until percentSize) {
            val percentTextView = findViewById<TextView>(resources.getIdentifier("line${i + 1}_percent", "id", packageName))
            val percentValue = Percent?.get(i) ?: "N/A"
            percentTextView.text = "$percentValue%"
        }

        val nutriSize = nutri?.size ?: 0

        for (i in 0 until nutriSize) {
            val nutriTextView = findViewById<TextView>(resources.getIdentifier("line${i + 1}_label", "id", packageName))
            val nutriValue = nutri?.get(i) ?: "N/A"
            nutriTextView.text = "$nutriValue"
        }

        personalButton = findViewById(R.id.buttonPersonalized)

        // Firebase에서 사용자 정보 가져오기
        // Firebase 연결을 위한 설정값
        val userIdClass = application as userId
        val userId = userIdClass.userId
        val db = Firebase.firestore

        // 유저 정보 받아오기
        db.collection("userInfo")
            .whereEqualTo("userID", userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result

                    // 유저 정보가 이미 존재하는 경우
                    if (result != null && !result.isEmpty) {
                        for (document in result) {
                            Log.d("FIRESTORE : ", "${document.id} => ${document.data}")
                            this.user = UserInfo.parseFirebaseDoc(document)

                            if (this.user!=null) {
                                Log.d("FIRESTORE : ", "got UserInfo")
                                break
                            }
                        }
                    }

                    user?.let { u -> // 사용자 정보 있을 시

                        val intent = Intent(this, FoodInfoAllPersonalizedActivity::class.java) //OCR 실패시 OCR 가이드라인으로 이동
                        // 식품 정보 전달
                        intent.putExtra("totalKcal", modifiedKcalList?.get(0)?.toInt())
                        intent.putExtra("nutriFactsInMilliString",
                            ArrayList(moPercentList?.map {it.toInt()}))
                        intent.putExtra("allergyList", allergyList)
                        // 사용자 정보 전달
                        intent.putExtra("userAge", u.age)
                        intent.putExtra("userSex", u.gender)
                        intent.putExtra("userDisease", u.disease)
                        intent.putExtra("userAllergic", u.allergic)


                        personalButton.setOnClickListener {
                            startActivity(intent)
                            overridePendingTransition(R.anim.none, R.anim.none)
                        }

                    } ?: run {// 사용자 정보 없을 시
                        personalButton.isEnabled = false // 버튼 비활성화
                        personalButton.setBackgroundResource(R.drawable.button_grey) // 비활성화 drawable 추가함
                    }


                } else {
                    // 쿼리 중에 예외가 발생한 경우
                    Log.d("FIRESTORE : ", "Error getting documents.", task.exception)
                    personalButton.isEnabled = false // 버튼 비활성화
                    personalButton.setBackgroundResource(R.drawable.button_grey) // 비활성화 drawable 추가함

                }
            }

    }

    override fun onDestroy() {
        // TTS 해제
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()

        super.onDestroy()
    }
}