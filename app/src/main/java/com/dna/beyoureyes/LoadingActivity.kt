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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dna.beyoureyes.databinding.ActivityLoadingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.File


class LoadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoadingBinding
    private lateinit var viewModel: LoadingViewModel
    private lateinit var resultbtn: Button
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LoadingViewModel::class.java)

        val bitmap = intent.getParcelableExtra<Bitmap>("capturedImage")
        if (bitmap != null) {
            viewModel.detectTextInBitmap(bitmap)
        } else {
            Log.e("LoadingActivity", "Bitmap is null")
        }

        resultbtn = binding.resultbtn
        resultbtn.setOnClickListener {
            navigateToNextActivity()
        }

        observeViewModel()

        handler.postDelayed({
            resultbtn.performClick() // Automatically click the button
        }, 4000) // 4 seconds
    }


    private fun observeViewModel() {
        viewModel.moPercentList.observe(this, Observer { percentList ->
            // Handle any updates to percentList here
        })

        viewModel.isValidData.observe(this, Observer { isValid ->
            // Handle any updates to isValid here
        })

        viewModel.isValidAllergyData.observe(this, Observer { isValid ->
            // Handle any updates to isValidAllergyData here
        })

        viewModel.hasValidKeywordOrder.observe(this, Observer { hasOrder ->
            // Handle any updates to hasValidKeywordOrder here
        })

        viewModel.isValidPercentData.observe(this, Observer { isValid ->
            // Handle any updates to isValidPercentData here
        })
    }

    private fun navigateToNextActivity() {
        val isValidData = viewModel.isValidData()
        val isValidAllergyData = viewModel.isValidDataAlergy()
        val hasValidKeywordOrder = viewModel.checkKeywordOrder(viewModel.foodData.koreanCharactersListmodi)
        val isValidPercentData = viewModel.isValidDataPer()

        when {
            hasValidKeywordOrder && isValidData && isValidPercentData && isValidAllergyData -> {
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

    private fun startFoodInfoAllActivity() {
        val intent = Intent(this, FoodInfoAllActivity::class.java)
        intent.putExtra("modifiedPercentList", ArrayList(viewModel.moPercentList.value))
        intent.putExtra("PercentList", ArrayList(viewModel.moPercentList.value))  // You might need to use a different list here
        intent.putStringArrayListExtra("modifiedKcalListText", ArrayList(viewModel.moPercentList.value))  // You might need to use a different list here
        intent.putStringArrayListExtra("allergyList", ArrayList(viewModel.foodData.extractedWords.toList()))
        startActivity(intent)
    }

    private fun startFoodInfoNutritionActivity() {
        val intent = Intent(this, FoodInfoNutritionActivity::class.java)
        configureIntent(intent)
        startActivity(intent)
    }

    private fun startFoodInfoAllergyActivity() {
        val intent = Intent(this, FoodInfoAllergyActivity::class.java)
        intent.putStringArrayListExtra("allergyList", ArrayList(viewModel.foodData.extractedWords.toList()))
        startActivity(intent)
    }

    private fun configureIntent(intent: Intent) {
        intent.putExtra("modifiedPercentList", ArrayList(viewModel.moPercentList.value))
        intent.putExtra("PercentList", ArrayList(viewModel.moPercentList.value))  // You might need to use a different list here
        intent.putStringArrayListExtra("modifiedKcalListText", ArrayList(viewModel.moPercentList.value))  // You might need to use a different list here
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Remove handler callbacks
    }
}













