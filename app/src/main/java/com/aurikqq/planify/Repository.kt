package com.aurikqq.planify

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.aurikqq.planify.viewmodels.Note
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
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

    private val json = Json { ignoreUnknownKeys = true }

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    private val _plansUpdatedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val plansUpdatedFlow = _plansUpdatedFlow.asSharedFlow()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PLANS_PREFIX || key == KEY_DAILY_PLANS_HISTORY || key?.startsWith(KEY_PLANS) == true) {
            _plansUpdatedFlow.tryEmit(Unit)
        }
    }

    private val storageDateFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun setUserEmail(email: String) {
        _user.update {
            it.copy(email = email)
        }
        sharedPreferences.edit {
            putString(USER_EMAIL, email)
        }
    }

    suspend fun getDaysList(): MutableList<Pair<String, String>> = withContext(Dispatchers.IO) {
        val daysListJson = sharedPreferences.getString(KEY_DAILY_PLANS_LIST, "[]") ?: "[]"
        return@withContext json.decodeFromString(daysListJson)
    }

    suspend fun saveDaysList(daysList: MutableList<Pair<String, String>>) = withContext(Dispatchers.IO) {
        val daysListJson = json.encodeToString(daysList)
        sharedPreferences.edit { putString(KEY_DAILY_PLANS_LIST, daysListJson) }
    }

    fun getPlansForDate(date: String): String {
        return sharedPreferences.getString("${KEY_PLANS}_$date", "") ?: ""
    }

    fun havePlansForDate(date: String): Boolean {
        return sharedPreferences.getBoolean("${KEY_HAVE_PLANS}_$date", false)
    }

    suspend fun savePlansForDate(date: String, plans: String) = withContext(Dispatchers.IO) {
        val now = LocalDate.now()
        val formattedDate = LocalDate.parse(date, storageDateFormatter)
        if (now.isAfter(formattedDate) || now.isEqual(formattedDate)) {
            val dailyPlansHistory =
                sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
            val plansList =
                if (dailyPlansHistory.isNotBlank()) json.decodeFromString<MutableList<Pair<String, String>>>(
                    dailyPlansHistory
                )
                else mutableListOf()

            try {
                if (plansList.isNotEmpty() && plansList[plansList.lastIndex].second == reformatDate(date)) {
                    plansList[plansList.lastIndex] = plansList.last().copy(
                        first = plans
                    )
                } else {
                    plansList.add(Pair(plans, reformatDate(date)))
                }
            } catch (_: IndexOutOfBoundsException) {
                plansList.add(Pair(plans, reformatDate(date)))
            }

            val jsonPlansList = json.encodeToString(plansList)

            sharedPreferences.edit {
                putString(KEY_DAILY_PLANS_HISTORY, jsonPlansList)
            }

            savePlansToHistoryDatabase(date, plans)
        }

        sharedPreferences.edit {
            putString("${KEY_PLANS}_$date", plans)
            putBoolean("${KEY_HAVE_PLANS}_$date", true)
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }
    }

    fun savePlansToHistoryDatabase(date: String, plans: String) {
        val email = sharedPreferences.getString(USER_EMAIL, "") ?: ""
        if (email.isNotBlank() && isOnline()) {
            val plan = hashMapOf(
                "plans" to plans,
            )

            db.collection(email)
                .document("history")
                .collection("history_collection")
                .document(date)
                .set(plan)
                .addOnSuccessListener {
                    Log.d("History Sync", "Plans added to history: $date")
                }
                .addOnFailureListener { e ->
                    Log.w("History Sync", "Error adding plans to history", e)
                }
        }
    }

    fun removePlansForDate(date: String) {
        val email = sharedPreferences.getString(USER_EMAIL, "") ?: ""

        sharedPreferences.edit {
            remove("${KEY_PLANS}_$date")
            remove("${KEY_HAVE_PLANS}_$date")
        }

        if (email.isNotBlank() && isOnline()) {
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

    suspend fun removeFromHistory(date: String) = withContext(Dispatchers.IO) {
        val email = sharedPreferences.getString(USER_EMAIL, "") ?: ""
        val isSignedIn = email.isNotBlank()

        val dailyPlansHistory =
            sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
        val plansList = json.decodeFromString<MutableList<Pair<String, String>>>(dailyPlansHistory)
        val newDate = reformatHistoryDate(date)
        println("current: $date")
        for (plan in plansList) {
            println(plan.second)
        }
        plansList.removeIf { it.second == date }

        if (isSignedIn && isOnline()) {
            println("deleting")
            db.collection(email)
                .document("history")
                .collection("history_collection")
                .document(reformatHistoryDate(date))
                .delete()
                .addOnSuccessListener { plansRef ->
                    Log.d("History Sync", "History deleted: $plansRef")
                }
                .addOnFailureListener { e ->
                    Log.w("History Sync", "Error deleting history", e)
                }
        }

        val jsonPlansList = json.encodeToString(plansList)
        sharedPreferences.edit {
            putString(KEY_DAILY_PLANS_HISTORY, jsonPlansList)
            remove("${KEY_PLANS}_$newDate")
            remove("${KEY_HAVE_PLANS}_$newDate")
        }
    }

    fun getIsFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    suspend fun addDateFromPicker(date: String) = withContext(Dispatchers.IO) {
        val email = sharedPreferences.getString(USER_EMAIL, "") ?: ""
        val isSignedIn = email.isNotBlank()

        val dayPlansAndDatePair = Pair("", date)
        var datesJson =
            sharedPreferences.getString(KEY_DAILY_PLANS_LIST, "") ?: ""
        val plansList =
            if (datesJson.isNotBlank()) json.decodeFromString<MutableList<Pair<String, String>>>(datesJson)
            else mutableListOf()
        plansList.add(0, dayPlansAndDatePair)
        datesJson = json.encodeToString(plansList)

        sharedPreferences.edit {
            putString(KEY_DAILY_PLANS_LIST, datesJson)
        }

        if (isSignedIn && isOnline()) {
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
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyEEE")
        val outputFormatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

        val parsedDate = LocalDate.parse(date, storageDateFormatter)
        return parsedDate.format(outputFormatter)
    }

    fun reformatHistoryDate(date: String) : String {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyEEE")
        val inputFormatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

        val parsedDate = LocalDate.parse(date, inputFormatter)
        return parsedDate.format(storageDateFormatter)
    }

    suspend fun saveNote(note: Note) = withContext(Dispatchers.IO) {
        var notesListJson =
            sharedPreferences.getString(KEY_NOTES_LIST, "[]") ?: "[]"
        val notesList =
            if (notesListJson.isNotBlank()) json.decodeFromString<MutableList<Note>>(notesListJson)
            else mutableListOf()

        val index = notesList.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notesList[index] = note
        }
        else {
            notesList.add(note)
        }

        notesListJson = json.encodeToString(notesList)

        sharedPreferences.edit {
            putString(KEY_NOTES_LIST, notesListJson)
        }
    }

    suspend fun removeNote(note: Note) = withContext(Dispatchers.IO) {
        var notesListJson =
            sharedPreferences.getString(KEY_NOTES_LIST, "[]") ?: "[]"
        val notesList = json.decodeFromString<MutableList<Note>>(notesListJson)
        notesList.remove(note)
        notesListJson = json.encodeToString(notesList)

        sharedPreferences.edit {
            putString(KEY_NOTES_LIST, notesListJson)
        }
    }

    suspend fun getNotesList() : MutableList<Note> = withContext(Dispatchers.IO) {
        val jsonStr = sharedPreferences.getString(KEY_NOTES_LIST, "[]") ?: "[]"
        return@withContext json.decodeFromString<MutableList<Note>>(jsonStr)
    }

    suspend fun getPlansList() : MutableList<Pair<String, String>> = withContext(Dispatchers.IO) {
        val jsonStr = sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "[]") ?: "[]"
        return@withContext json.decodeFromString<MutableList<Pair<String, String>>>(jsonStr)
    }

    suspend fun setPlansList(list: MutableList<Pair<String, String>>) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString(KEY_DAILY_PLANS_HISTORY, json.encodeToString(list))
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
        if (!sharedPreferences.contains(IS_DARK_THEME_ON)) {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return uiMode == Configuration.UI_MODE_NIGHT_YES
        }
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

    fun isOnline(): Boolean {
        val connectManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectManager.activeNetwork ?: return false
        val caps = connectManager.getNetworkCapabilities(network) ?: return false

        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                trySend(true)
            }
            override fun onLost(network: android.net.Network) {
                trySend(false)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        trySend(isOnline())
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

/*
    fun getTextWidgetText() : String {
        return sharedPreferences.getString(TEXT_WIDGET_TEXT, "") ?: ""
    }
    fun getTextWidgetTitle() : String {
        return sharedPreferences.getString(TEXT_WIDGET_TITLE, "") ?: ""
    }
    fun getTextWidgetData() : String {
        return sharedPreferences.getString(TEXT_WIDGET_DATA, "") ?: ""
    }
    fun setTextWidgetTitle(data: String) {
        sharedPreferences.edit {
            putString(TEXT_WIDGET_TITLE, data)
        }
    }
    fun setTextWidgetText(data: String) {
        sharedPreferences.edit {
            putString(TEXT_WIDGET_TEXT, data)
        }
    }
    fun setTextWidgetData(data: String) {
        sharedPreferences.edit {
            putString(TEXT_WIDGET_DATA, data)
        }
    }
*/

    fun setPlansPrefix(prefix: String) {
        sharedPreferences.edit {
            putString(PLANS_PREFIX, prefix)
        }
    }
    fun getPlansPrefix() : String {
        return sharedPreferences.getString(PLANS_PREFIX, "--") ?: "--"
    }

    fun setIsPrefixHintShown(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(IS_PREFIX_HINT_SHOWN, value)
        }
    }
    fun getIsPrefixHintShown() : Boolean {
        return sharedPreferences.getBoolean(IS_PREFIX_HINT_SHOWN, true)
    }

    suspend fun updatePrefixInAllPlans(oldPrefix: String, newPrefix: String) = withContext(Dispatchers.IO) {
        if (oldPrefix == newPrefix) return@withContext

        fun updatePlans(plans: String): String {
            return plans.lines().joinToString("\n") { line ->
                val trimmed = line.trimStart()
                if (trimmed.startsWith("$oldPrefix*")) {
                    val leadingSpaces = line.takeWhile { it.isWhitespace() }
                    leadingSpaces + newPrefix + "*" + trimmed.substring(oldPrefix.length + 1)
                } else if (trimmed.startsWith(oldPrefix)) {
                    val leadingSpaces = line.takeWhile { it.isWhitespace() }
                    leadingSpaces + newPrefix + trimmed.substring(oldPrefix.length)
                } else {
                    line
                }
            }
        }

        val email = getEmail()
        val isSignedIn = getIsSignedIn()
        val online = isOnline()

        // 1. Update KEY_DAILY_PLANS_LIST
        val daysList = getDaysList()
        val updatedDaysList = daysList.map { it.copy(first = updatePlans(it.first)) }.toMutableList()
        saveDaysList(updatedDaysList)

        // 2. Update KEY_DAILY_PLANS_HISTORY
        val historyList = getPlansList()
        val updatedHistoryList = historyList.map { it.copy(first = updatePlans(it.first)) }.toMutableList()
        setPlansList(updatedHistoryList)

        // 3. Update individual SharedPreferences and Sync to DB
        val datesFromList = daysList.map { it.second }
        val datesFromHistory = historyList.map {
            try { reformatHistoryDate(it.second) } catch (e: Exception) { "" }
        }.filter { it.isNotEmpty() }

        val allDates = (datesFromList + datesFromHistory).distinct()

        allDates.forEach { date ->
            val oldPlans = getPlansForDate(date)
            if (oldPlans.isNotBlank()) {
                val newPlans = updatePlans(oldPlans)
                sharedPreferences.edit { putString("${KEY_PLANS}_$date", newPlans) }

                if (isSignedIn && online) {
                    val planMap = hashMapOf("plans" to newPlans)
                    db.collection(email)
                        .document("plans")
                        .collection("plans_collection")
                        .document(date)
                        .set(planMap)

                    savePlansToHistoryDatabase(date, newPlans)
                }
            }
        }
    }
//    suspend fun updateTextWidgetData(type: TextWidgetDataTypes, data: String) {
//        updateTextWidget(type, data, context)
//    }
}