package com.dna.beyoureyes

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    private lateinit var nutriFact : NutritionFacts
    private lateinit var userNutri : UserInfo
    private lateinit var nutriDailyValue: NutrientDailyValues
    @Before
    fun setUp() {
        nutriFact = NutritionFacts()
        userNutri = UserInfo(10, 0)
        nutriDailyValue = NutrientDailyValues()
        nutriDailyValue.personalizeAllProperties(0, 60, arrayOf("고지혈증", "고혈압"))

    }

    @Test
    fun Natrium_isCorrect() {
        // daily Value의 경우
        val ageArray = arrayOf(1, 14, 15, 64, 65, 74, 75, Int.MAX_VALUE)
        val expected = arrayOf(1500, 1500, 1500, 1500, 1300, 1300, 1100, 1100)
        for (a in 0 until ageArray.size) {
            nutriDailyValue.personalizeAllProperties(0, ageArray[a], arrayOf(""))
            assertEquals(expected[a], nutriDailyValue.natrium.dailyValue)
        }

        // upperIntake의 경우


    }
}