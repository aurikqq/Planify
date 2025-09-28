package com.aurikqq.planify

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import java.lang.IndexOutOfBoundsException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlansRepository(private val sharedPreferences: SharedPreferences, private val context: Context) {
    fun getDaysList(): MutableList<Pair<String, String>> {
        val daysListJson = sharedPreferences.getString(KEY_DAILY_PLANS_LIST, "[]") ?: "[]"
        return Json.decodeFromString(daysListJson)
    }

    fun saveDaysList(daysList: MutableList<Pair<String, String>>) {
        val daysListJson = Json.encodeToString(daysList)
        sharedPreferences.edit { putString(KEY_DAILY_PLANS_LIST, daysListJson) }
    }

    fun getPlansForDate(date: String): String {
        return sharedPreferences.getString("${KEY_PLANS}_$date", "") ?: ""
    }

    fun havePlansForDate(date: String): Boolean {
        return sharedPreferences.getBoolean("${KEY_HAVE_PLANS}_$date", false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun savePlansForDate(date: String, plans: String) {
        val dailyPlansHistory =
            sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
        val plansList =
            if (dailyPlansHistory.isNotBlank()) Json.decodeFromString<MutableList<Pair<String, String>>>(dailyPlansHistory)
            else mutableListOf()

        try {
            if (plansList[plansList.lastIndex].second == reformatDate(date)) {
                plansList[plansList.lastIndex] = plansList.last().copy(
                    first = plans
                )
            }
            else {
                plansList.add(Pair(plans, reformatDate(date)))
            }
        } catch (_: IndexOutOfBoundsException) {
            plansList.add(Pair(plans, reformatDate(date)))
        }

        val jsonPlansList = Json.encodeToString(plansList)

        sharedPreferences.edit {
            putString("${KEY_PLANS}_$date", plans)
            putBoolean("${KEY_HAVE_PLANS}_$date", true)
            putString(KEY_DAILY_PLANS_HISTORY, jsonPlansList)
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }
    }
    fun getIsFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun addDateFromPicker(date: String) {
        val dayPlansAndDatePair = Pair("", date)
        var datesJson =
            sharedPreferences.getString(KEY_DAILY_PLANS_LIST, "") ?: ""
        val plansList =
            if (datesJson.isNotBlank()) Json.decodeFromString<MutableList<Pair<String, String>>>(datesJson)
            else mutableListOf()
        plansList.add(0, dayPlansAndDatePair)
        datesJson = Json.encodeToString(plansList)

        sharedPreferences.edit {
            putString(KEY_DAILY_PLANS_LIST, datesJson)
        }
    }

    fun sendToast(@StringRes text: Int, length: Int) {
        Toast.makeText(
            context,
            context.getString(text),
            length
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun reformatDate(date: String) : String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE", Locale.getDefault())

        val parsedDate = LocalDate.parse(date, inputFormatter)
        return parsedDate.format(outputFormatter)
    }

    fun setNotes(notes: String, isEditing: Boolean = false) {
        sharedPreferences.edit {
            putString(KEY_NOTES, notes)
            if (!isEditing) putBoolean(KEY_HAVE_NOTES, true)
        }
    }
    fun getNotes() : String {
        return sharedPreferences.getString(KEY_NOTES, "") ?: ""
    }

    fun setHaveNotes(value: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HAVE_NOTES, value) }
    }
    fun getHaveNotes() : Boolean {
        return sharedPreferences.getBoolean(KEY_HAVE_NOTES, false)
    }

    fun getPlansList() : MutableList<Pair<String, String>> {
        val json = sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
        return Json.decodeFromString<MutableList<Pair<String, String>>>(json)
    }
}