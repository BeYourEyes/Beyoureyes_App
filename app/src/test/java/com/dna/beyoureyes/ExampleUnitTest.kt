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

    //setPersonalizedEnergy(gender, age) -> 26가지
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
    //natrium.personalize(age, disease) -> 8가지
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
    //carbs.personalize() -> 1가지
    @Test
    fun Carbs_isCorrect() {
        assertEquals(130000, nutriDailyValue.carbs.dailyValue)
    }

    //sugar.personalize(this.energy) -> 3가지(경계값 분석으로 -1, 0, Int.MAX_VALUE)
    @Test
    fun Sugar_isCorrect() {
        val energyArray = arrayOf(-1, 0, Int.MAX_VALUE)
        energyArray.forEach { energy ->
            val intakeCategory = nutriDailyValue.sugar.intakeRange!!.entries.find {
                energy in it.value
            }?.key
            // 결과를 출력하거나 assert로 테스트 가능
            when (intakeCategory) {
                IntakeRange.LESS -> println("$energy: LESS (주의)")
                IntakeRange.ENOUGH -> println("$energy: ENOUGH (적정)")
                IntakeRange.OVER -> println("$energy: OVER (경고)")
                else -> println("$energy: 범위 밖")
            }

        }

    }
    //protein.personalize(gender, age) -> 6가지
    //fat.personalize(this.energy, disease) -> 4가지
    //satFat.personalize(age, this.energy, disease) -> 6가지
    //chol.personalize(disease) -> 6가지sk
    @Test
    fun Protein_isCorrect() {

    }
}