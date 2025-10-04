package com.aurikqq.planify

import android.content.Context
import android.content.SharedPreferences
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

    fun reformatDate(date: String) : String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE", Locale.getDefault())

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
    }
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
            putBoolean(IS_UPDATE_POPUP_SHOWN, value)
        }
    }
    fun getIsUpdatePopupShown() : Boolean {
        return sharedPreferences.getBoolean(IS_UPDATE_POPUP_SHOWN, false)
    }
}