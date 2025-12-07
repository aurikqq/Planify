package com.aurikqq.planify

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.aurikqq.planify.viewmodels.Note
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class Repository(private val sharedPreferences: SharedPreferences, private val context: Context) {
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

    fun savePlansForDate(date: String, plans: String) {
        val now = LocalDate.now()
        val formattedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
        if (now.isAfter(formattedDate) || now.isEqual(formattedDate)) {
            val dailyPlansHistory =
                sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
            val plansList =
                if (dailyPlansHistory.isNotBlank()) Json.decodeFromString<MutableList<Pair<String, String>>>(
                    dailyPlansHistory
                )
                else mutableListOf()

            try {
                if (plansList[plansList.lastIndex].second == reformatDate(date)) {
                    plansList[plansList.lastIndex] = plansList.last().copy(
                        first = plans
                    )
                } else {
                    plansList.add(Pair(plans, reformatDate(date)))
                }
            } catch (_: IndexOutOfBoundsException) {
                plansList.add(Pair(plans, reformatDate(date)))
            }

            val jsonPlansList = Json.encodeToString(plansList)

            sharedPreferences.edit {
                putString(KEY_DAILY_PLANS_HISTORY, jsonPlansList)
            }
        }

        sharedPreferences.edit {
            putString("${KEY_PLANS}_$date", plans)
            putBoolean("${KEY_HAVE_PLANS}_$date", true)
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }
    }

    fun removePlansForDate(date: String) {
        sharedPreferences.edit {
            remove("${KEY_PLANS}_$date")
            remove("${KEY_HAVE_PLANS}_$date")
        }
    }

    fun removeFromHistory(date: String) {
        val dailyPlansHistory =
            sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
        val plansList = Json.decodeFromString<MutableList<Pair<String, String>>>(dailyPlansHistory)
        val newDate = reformatHistoryDate(date)
        plansList.removeIf { it.second == date }

        val jsonPlansList = Json.encodeToString(plansList)
        sharedPreferences.edit {
            putString(KEY_DAILY_PLANS_HISTORY, jsonPlansList)
        }

        sharedPreferences.edit {
            remove("${KEY_PLANS}_$newDate")
            remove("${KEY_HAVE_PLANS}_$newDate")
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

    fun reformatDate(date: String) : String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE", Locale.getDefault())

        val parsedDate = LocalDate.parse(date, inputFormatter)
        return parsedDate.format(outputFormatter)
    }

    fun reformatHistoryDate(date: String) : String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())

        val parsedDate = LocalDate.parse(date, inputFormatter)
        return parsedDate.format(outputFormatter)
    }

    fun saveNote(note: Note) {
        var notesListJson =
            sharedPreferences.getString(KEY_NOTES_LIST, "[]") ?: "[]"
        val notesList =
            if (notesListJson.isNotBlank()) Json.decodeFromString<MutableList<Note>>(notesListJson)
            else mutableListOf()

        val index = notesList.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notesList[index] = note
        }
        else {
            notesList.add(note)
        }

        notesListJson = Json.encodeToString(notesList)

        sharedPreferences.edit {
            putString(KEY_NOTES_LIST, notesListJson)
        }
    }

    fun removeNote(note: Note) {
        var notesListJson =
            sharedPreferences.getString(KEY_NOTES_LIST, "[]") ?: "[]"
        val notesList = Json.decodeFromString<MutableList<Note>>(notesListJson)
        notesList.remove(note)
        notesListJson = Json.encodeToString(notesList)

        sharedPreferences.edit {
            putString(KEY_NOTES_LIST, notesListJson)
        }
    }

    fun getNotesList() : MutableList<Note> {
        val json = sharedPreferences.getString(KEY_NOTES_LIST, "[]") ?: "[]"
        return Json.decodeFromString<MutableList<Note>>(json)
    }

    fun getPlansList() : MutableList<Pair<String, String>> {
        val json = sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
        Log.d("u", "${Json.decodeFromString<MutableList<Pair<String, String>>>(json)}")
        return Json.decodeFromString<MutableList<Pair<String, String>>>(json)
    }

    fun setTempNoteTitle(title: String) {
        sharedPreferences.edit {
            putString(KEY_TEMP_NOTE_TITLE, title)
        }
    }
    fun setTempNoteText(text: String) {
        sharedPreferences.edit {
            putString(KEY_TEMP_NOTE_TEXT, text)
        }
    } /*TODO*/
    fun setTempPlans(plans: String) {
        sharedPreferences.edit {
            putString(KEY_TEMP_PLANS, plans)
        }
    }

    fun getTempPlans() : String {
        return sharedPreferences.getString(KEY_TEMP_PLANS, "") ?: ""
    }

    fun setIsUpdatePopupShown(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_UPDATE_CHANGELOG_SHOWN, value)
        }
    }
    fun getIsUpdatePopupShown() : Boolean {
        return sharedPreferences.getBoolean(KEY_IS_UPDATE_CHANGELOG_SHOWN, false)
    }

    fun plansNotificationsEnabled(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(PLANS_NOTIFICATIONS_ENABLED, value)
        }
    }
    fun resetNotificationsEnabled(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(RESET_NOTIFICATIONS_ENABLED, value)
        }
    }
    fun notificationsCooldown(time: Float) {
        sharedPreferences.edit {
            putFloat(PLANS_NOTIFICATIONS_COOLDOWN, time)
        }
    }
//    fun daysListPlacement(side: Int) {
//        sharedPreferences.edit {
//            putInt(DAYS_LIST_PLACEMENT, side)
//        }
//    }
    fun getPlansNotificationsEnabled() : Boolean {
        return sharedPreferences.getBoolean(PLANS_NOTIFICATIONS_ENABLED, true)
    }
    fun getResetNotificationsEnabled() : Boolean {
        return sharedPreferences.getBoolean(RESET_NOTIFICATIONS_ENABLED, true)
    }
    fun getPlansNotificationsCooldown() : Float {
        return sharedPreferences.getFloat(PLANS_NOTIFICATIONS_COOLDOWN, 2f)
    }

    fun setIsInDarkTheme(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(IS_DARK_THEME_ON, value)
        }
    }

    fun getIsInDarkTheme() : Boolean {
        return sharedPreferences.getBoolean(IS_DARK_THEME_ON, false)
    }
}