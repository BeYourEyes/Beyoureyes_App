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
        nutriDailyValue.personalizeAllProperties(0, 60, arrayOf(""))

    }
    @Test
    fun Energy_isCorrect() {
        // Gender.WOMAN.ordinal = 0, Gender.MAN.ordinal = 1
        val ageArray = arrayOf(arrayOf(-1, 0, 15, 29, 30, 49, 50, 64, 65, 74, 75, Int.MAX_VALUE), arrayOf(-1, 0, 15, 18, 19, 29, 30, 49, 50, 64, 65, 74, 75, Int.MAX_VALUE))
        val expected = arrayOf(arrayOf(2000, 2000, 2000, 2000, 1900, 1900, 1700, 1700, 1600, 1600, 1500, 1500), arrayOf(2700, 2700, 2700, 2700, 2600, 2600, 2500, 2500, 2200, 2200, 2000, 2000, 1900, 1900))

        repeat(2) { gender ->
            for (i in 0 until ageArray[gender].size) {
                nutriDailyValue.personalizeAllProperties(gender, ageArray[gender][i], arrayOf(""))
                assertEquals(expected[gender][i], nutriDailyValue.energy)
            }
        }
    }
    //natrium.personalize(age, disease)
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

    // 각 영양소에 대한 분류 기준, 순서대로 사용자 personalize 예정
    //carbs.personalize()
    //sugar.personalize(this.energy)
    //protein.personalize(gender, age)
    //fat.personalize(this.energy, disease)
    //satFat.personalize(age, this.energy, disease)
    //chol.personalize(disease)
    //setPersonalizedEnergy(gender, age)
    @Test
    fun Protein_isCorrect() {

    }
}