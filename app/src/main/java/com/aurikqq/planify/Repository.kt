package com.aurikqq.planify

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.aurikqq.planify.viewmodels.Note
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList
import java.util.Locale

data class User (
    val email: String = "",
    val plans: List<Pair<String, String>> = emptyList(),
    val notes: MutableList<Note> = emptyList()
)

class Repository(private val sharedPreferences: SharedPreferences, private val context: Context) {
    val db = Firebase.firestore

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    fun setUserEmail(email: String) {
        _user.update {
            it.copy(email = email)
        }
        sharedPreferences.edit {
            putString(USER_EMAIL, email)
        }
    }

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
        val email = sharedPreferences.getString(USER_EMAIL, "") ?: ""

        sharedPreferences.edit {
            remove("${KEY_PLANS}_$date")
            remove("${KEY_HAVE_PLANS}_$date")
        }

        if (email.isNotBlank()) {
            db.collection(email)
                .document("plans")
                .collection("plans_collection")
                .document(date)
                .delete()
                .addOnSuccessListener {
                    Log.d("Plans Sync", "Plans deleted for date $date")
                }
                .addOnFailureListener { e ->
                    Log.w("Plans Sync", "Error deleting plans", e)
                }
        }
    }

    fun removeFromHistory(date: String) {
        val email = sharedPreferences.getString(USER_EMAIL, "") ?: ""
        val isSignedIn = email.isNotBlank()

        val dailyPlansHistory =
            sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
        val plansList = Json.decodeFromString<MutableList<Pair<String, String>>>(dailyPlansHistory)
        val newDate = reformatHistoryDate(date)
        println("current: $date")
        for (plan in plansList) {
            println(plan.second)
        }
        plansList.removeIf { it.second == date }

        if (isSignedIn) {
            println("deleting")
            db.collection(email)
                .document("plans")
                .collection("plans_collection")
                .document(reformatHistoryDate(date))
                .delete()
                .addOnSuccessListener { plansRef ->
                    Log.d("Plans Sync", "Plans deleted: $plansRef")
                }
                .addOnFailureListener { e ->
                    Log.w("Plans Sync", "Error deleting plans", e)
                }
        }

        val jsonPlansList = Json.encodeToString(plansList)
        sharedPreferences.edit {
            putString(KEY_DAILY_PLANS_HISTORY, jsonPlansList)
            remove("${KEY_PLANS}_$newDate")
            remove("${KEY_HAVE_PLANS}_$newDate")
        }
    }

    fun getIsFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun addDateFromPicker(date: String) {
        val email = sharedPreferences.getString(USER_EMAIL, "") ?: ""
        val isSignedIn = email.isNotBlank()

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

        if (isSignedIn) {
            val plans = hashMapOf(
                "plans" to ""
            )

            db.collection(email)
                .document("plans")
                .collection("plans_collection")
                .document(date)
                .set(plans)
                .addOnSuccessListener {
                    Log.d("Plans Sync", "Added date: $date")
                }
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
        return Json.decodeFromString<MutableList<Pair<String, String>>>(json)
    }

    fun setPlansList(list: MutableList<Pair<String, String>>) {
        sharedPreferences.edit {
            putString(KEY_DAILY_PLANS_HISTORY, Json.encodeToString(list))
        }
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

    fun syncOnSignIn() {
        db.collection(user.value.email)
            .document("plans")
            .collection("plans_collection")
            .get()
            .addOnSuccessListener { plans ->
                val result = plans.map { plan ->
                    Pair(
                        plan.id,
                        plan.get("plans") ?.toString() ?: ""
                    )
                }
                Log.d("Plans Sync", "User plans imported from DB")
                _user.update {
                    it.copy(
                        plans = result
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.w("Plans Sync", "Error fetching plans from DB", e)
            }

        db.collection(user.value.email)
            .document("notes")
            .collection("notes_collection")
            .get()
            .addOnSuccessListener { notes ->
                val result = notes.map { note ->
                    Note(
                        note.id,
                        note.get("title") as String,
                        note.get("text") as String,
                        note.get("is_expanded") as Boolean
                    )
                }.toMutableList()
                _user.update {
                    it.copy(
                        notes = result
                    )
                }
                Log.d("Notes Sync", "User notes imported from DB")
            }
            .addOnFailureListener { e ->
                Log.w("Notes Sync", "Error fetching notes from DB", e)
            }
    }

    fun getIsSignedIn() : Boolean {
        return sharedPreferences.getString(USER_EMAIL, "")?.isNotEmpty() ?: false
    }

    fun getEmail() : String {
        return sharedPreferences.getString(USER_EMAIL, "") ?: ""
    }
}