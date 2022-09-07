package com.thakurnitin2684.screentimerank.utils

import android.content.Context
import android.content.SharedPreferences



 const val PREFERENCE_NAME = "SCREEN_TIME_RANK_PREF"
 const val IS_FIRST_TIME = "IS_FIRST_TIME"
 const val UNIV_INT = "UNIV_INT"

class PrefRepository(val context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    private val editor = pref.edit()



    private fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }
    private fun String.put(int: Int) {
        editor.putInt(this, int)
        editor.commit()
    }

    private fun String.getInt() = pref.getInt(this, 0)

    private fun String.getBoolean() = pref.getBoolean(this, true)


    fun setUnivInt(value: Int) {
        UNIV_INT.put(value)
    }

    fun getUnivInt() = UNIV_INT.getInt()



    fun setIsFirstTime(isFirstTime: Boolean) {
        IS_FIRST_TIME.put(isFirstTime)
    }

    fun getIsFirstTime() = IS_FIRST_TIME.getBoolean()



    fun clearData() {
        editor.clear()
        editor.commit()
    }
}