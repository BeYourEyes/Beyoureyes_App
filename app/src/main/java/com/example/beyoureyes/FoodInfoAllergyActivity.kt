package com.example.beyoureyes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class FoodInfoAllergyActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private val camera = Camera()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_allergy)

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
        val allergyList = intent.getStringArrayListExtra("allergyList")

        if (allergyList != null) {
            for (diseaseItem in allergyList) {
                val chip = Chip(this)
                chip.text = diseaseItem

                // Chip 뷰의 크기 및 여백 설정
                val params = ChipGroup.LayoutParams(
                    200, // 넓이 80
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

        // 버튼 눌렀을 때 TTS 실행 -> 수정 예정
        speakButton.setOnClickListener {
            val textToSpeak = "안녕하세요! 영양 정보를 분석해드리겠습니다. 해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다. 다른 영양 성분 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시찍기 버튼을 눌러주세요."
            speak(textToSpeak)
        }

        val retryButton = findViewById<Button>(R.id.buttonRetry)
        retryButton.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
        }

        // 맞춤 정보 버튼
        val personalButton = findViewById<Button>(R.id.buttonPersonalized)

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
                    var user:UserInfo? = null

                    // 유저 정보가 이미 존재하는 경우
                    if (result != null && !result.isEmpty) {
                        for (document in result) {
                            Log.d("FIRESTORE : ", "${document.id} => ${document.data}")
                            user = UserInfo.parseFirebaseDoc(document)

                            if (user!=null) {
                                Log.d("FIRESTORE : ", "got UserInfo")
                                break
                            }
                        }
                    }

                    user?.let { u -> // 사용자 정보 있을 시

                        val intent = Intent(this, FoodInfoAllergyPersonalizedActivity::class.java)
                        // 식품 정보 전달
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                Camera.FLAG_REQ_CAMERA -> {
                    camera.processPhoto(this)
                }
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