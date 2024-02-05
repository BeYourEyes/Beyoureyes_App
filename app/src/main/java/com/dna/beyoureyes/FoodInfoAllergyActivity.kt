package com.dna.beyoureyes

import TTSManager
import android.annotation.SuppressLint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.content.Context
import android.widget.Toast
import com.dna.beyoureyes.databinding.ActivityFoodInfoAllergyBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class FoodInfoAllergyActivity : AppCompatActivity() {

    private lateinit var ttsManager: TTSManager
    private lateinit var speakButton: Button
    private lateinit var personalButton:Button
    private val camera = Camera()
    private lateinit var binding: ActivityFoodInfoAllergyBinding


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodInfoAllergyBinding.inflate(layoutInflater)  // Initialize the binding
        setContentView(binding.root)

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "영양 분석 결과"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // 버튼 초기화
        speakButton = binding.buttonVoice



        // 알러지 정보 intent하여 표시
        val allergyChipGroup: ChipGroup = binding.allergyChipGroup
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
                chip.setPadding(20, 20, 20, 20) // 상, 좌, 하, 우 패딩
                allergyChipGroup.addView(chip)
            }
        }
        // TTSManager 초기화 완료되었을때
        ttsManager = TTSManager(this) {
            // 버튼 눌렀을 때 TTS 실행
            speakButton.setOnClickListener {

                val textToSpeak = "영양 정보를 분석해드리겠습니다. 해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다. 영양 성분 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시 찍기 버튼을 눌러주세요."

                if (ttsManager.isSpeaking()) {
                    ttsManager.stop()
                    speakButton.text = "설명 듣기"
                } else {
                    ttsManager.speak(textToSpeak)
                    speakButton.text = "재생 중"
                    ttsManager.showToast(this, "재생을 멈추려면 버튼을 다시 눌러주세요.")
                }

//                ttsManager.speak(textToSpeak)
            }
        }

        binding.buttonRetry.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
        }

        // 맞춤 정보 버튼
        personalButton = binding.buttonPersonalized

        // 사용자 맞춤 서비스 제공 여부 검사(맞춤 정보 있는지)
        // 기존 Firebase와의 통신 코드는 다 제거
        AppUser.info?.let { // 사용자 정보 있을 시

            val intent = Intent(this, FoodInfoAllergyPersonalizedActivity::class.java) //OCR 실패시 OCR 가이드라인으로 이동
            // 식품 정보 전달 (알레르기 only)
            intent.putExtra("allergyList", allergyList)
            // 이제 intent로 사용자 정보 전달할 필요 X

            // 맞춤 정보 버튼 활성화
            personalButton.setOnClickListener {
                if (ttsManager.isSpeaking()) {
                    ttsManager.stop()
                    speakButton.text = "설명 듣기"
                }
                startActivity(intent)
                overridePendingTransition(R.anim.none, R.anim.none)
            }
        } ?: run {// 사용자 정보 없을 시
            // 맞춤 정보 버튼 비활성화
            personalButton.isEnabled = false // 버튼 비활성화
            personalButton.setBackgroundResource(R.drawable.button_grey) // 비활성화 drawable 추가함
        }

    }

    override fun onBackPressed() {
        if (ttsManager.isSpeaking()) {
            ttsManager.stop()
            speakButton.text = "설명 듣기"
        }
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
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
        if (ttsManager.isSpeaking()) {
            ttsManager.stop()
        }
        ttsManager.shutdown()
        super.onDestroy()

    }

}