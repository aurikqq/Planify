package com.aurikqq.planify.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.KEY_DAILY_PLANS_LIST
import com.aurikqq.planify.R
import com.aurikqq.planify.Repository
import com.aurikqq.planify.screens.PlansScreenUiState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Suppress("UNCHECKED_CAST")
class PlansScreenViewModelFactory(
    private val repo: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlansScreenViewModel::class.java)) {
            return PlansScreenViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class PlansScreenViewModel(private val repo: Repository) : ViewModel() {
    private val _uiState = MutableStateFlow(PlansScreenUiState())
    val uiState = _uiState.asStateFlow()
    val db = Firebase.firestore

    init {
        loadInitialData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadInitialData() {
        viewModelScope.launch {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(
                "dd_MM_yyyy", Locale.getDefault()))
            val days = repo.getDaysList()
            val havePlans = repo.havePlansForDate(currentDate)
            val isEditing = _uiState.value.isPlanEditing

            _uiState.update {
                it.copy (
                    currentDate = currentDate,
                    selectedPickerDate = currentDate,
                    days = days.sortedBy { date ->
                        LocalDate.parse(date.second, DateTimeFormatter.ofPattern("dd_MM_yyyy")) },
                    plansForSelectedDate = repo.getPlansForDate(currentDate),
                    tempPlanInput = if (isEditing || !havePlans) repo.getTempPlans() else "",
                    havePlans = havePlans,
                    isFirstLaunch = repo.getIsFirstLaunch(),
                    isUpdatePopupShown = repo.getIsUpdatePopupShown(),
                    plansNotificationsEnabled = repo.getPlansNotificationsEnabled(),
                    resetNotificationsEnabled = repo.getResetNotificationsEnabled(),
                    isSignedIn = repo.getIsSignedIn(),
                    email = repo.getEmail(),
                    plansPrefix = repo.getPlansPrefix()
                )
            }
        }
    }

    fun updateAccount() {
        _uiState.update {
            it.copy (
                isSignedIn = repo.getIsSignedIn(),
                email = repo.getEmail()
            )
        }
    }

    fun selectDate(date: String) {
        val plans = repo.getPlansForDate(date)
        val havePlans = repo.havePlansForDate(date)

        _uiState.update {
            it.copy (
                selectedPickerDate = date,
                plansForSelectedDate = plans,
                havePlans = havePlans,
                isPlanEditing = false,
                tempPlanInput = ""
            )
        }
    }

    fun onTempPlanInputChange(new: String) {
        repo.setTempPlans(new)

        _uiState.update { it.copy(tempPlanInput = new) }
    }

    fun saveNewPlans() {
        val selectedDate = _uiState.value.selectedPickerDate
        val newPlans = _uiState.value.tempPlanInput

        if (newPlans.isBlank()) return

        repo.savePlansForDate(selectedDate, newPlans)
        repo.sendToast(R.string.toast_set_plans, Toast.LENGTH_SHORT)

        _uiState.update {
            it.copy(
                plansForSelectedDate = newPlans,
                havePlans = true,
                tempPlanInput = ""
            )
        }
    }

    fun addPlans() {
        val selectedDate = _uiState.value.selectedPickerDate
        val currentPlans = _uiState.value.plansForSelectedDate
        val additionalPlans = _uiState.value.tempPlanInput

        if (additionalPlans.isBlank()) return

        val newPlans = "$currentPlans\n$additionalPlans"
        repo.savePlansForDate(selectedDate, newPlans)
        repo.sendToast(R.string.toast_added_plans, Toast.LENGTH_SHORT)

        _uiState.update {
            it.copy(
                plansForSelectedDate = newPlans,
                tempPlanInput = ""
            )
        }
    }

    fun startEditingPlans() {
        val plans = _uiState.value.plansForSelectedDate

        _uiState.update {
            it.copy(
                isPlanEditing = true,
                tempPlanInput = plans
            )
        }
    }

    fun endEditingPlans() {
        val newPlans = _uiState.value.tempPlanInput
        val selectedDate = _uiState.value.selectedPickerDate

        repo.savePlansForDate(selectedDate, newPlans)
        repo.sendToast(R.string.toast_edited_plans, Toast.LENGTH_SHORT)

        _uiState.update {
            it.copy(
                plansForSelectedDate = newPlans,
                isPlanEditing = false,
                tempPlanInput = ""
            )
        }
    }

    fun showDatePicker() {
        _uiState.update {
            it.copy(
                isDatePickerShown = true
            )
        }
    }

    fun hideDatePicker() {
        _uiState.update {
            it.copy(
                isDatePickerShown = false
            )
        }
    }

    fun getDateFromPicker(date: String) {
        val currentDaysList = _uiState.value.days.toMutableList()
        currentDaysList.add(Pair("", date))

        repo.addDateFromPicker(date)

        _uiState.update { it.copy(days = currentDaysList.sortedBy { date ->
            LocalDate.parse(date.second, DateTimeFormatter.ofPattern("dd_MM_yyyy")) })}
    }

    fun setIsDaysListEditing(value: Boolean) {
        _uiState.update {
            it.copy(
                isDaysListEditing = value
            )
        }
    }

    fun removeDay(day: Pair<String, String>, removePlansForCurrentDate: Boolean = true) {
        val currentDaysList = _uiState.value.days.toMutableList()
        currentDaysList.remove(day)
        if (removePlansForCurrentDate) {
            val now = LocalDate.now()
            val formattedDate = LocalDate.parse(day.second, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
            if (now.isAfter(formattedDate) || now.isEqual(formattedDate)) {
                repo.savePlansToHistoryDatabase(day.second, day.first)
            }
            repo.removePlansForDate(day.second)
        }
        repo.saveDaysList(currentDaysList)

        _uiState.update { it.copy(days = currentDaysList.sortedBy { date ->
            LocalDate.parse(date.second, DateTimeFormatter.ofPattern("dd_MM_yyyy")) }) }
    }

    fun tempPlans(plans: String) {
        repo.setTempPlans(plans)
        _uiState.update {
            it.copy(
                tempPlanInput = plans
            )
        }
    }

    fun updatePopupShown(value: Boolean) {
        repo.setIsUpdatePopupShown(value)

        _uiState.update {
            it.copy(
                isUpdatePopupShown = value
            )
        }
    }

    fun sendPlansToDatabase(plans: String) {
        val selectedDate = _uiState.value.selectedPickerDate
        val now = LocalDate.now()
        val formattedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd_MM_yyyy"))

        if (now.isAfter(formattedDate) || now.isEqual(formattedDate)) {
            repo.savePlansToHistoryDatabase(selectedDate, plans)
        }

        val plan = hashMapOf(
            "plans" to plans,
        )

        db.collection(_uiState.value.email)
            .document("plans")
            .collection("plans_collection")
            .document(selectedDate)
            .set(plan)
            .addOnSuccessListener { planRef ->
                Log.d("Plans Sync", "Plans added: $planRef")
            }
            .addOnFailureListener { e ->
                Log.w("Plans Sync", "Error adding plans", e)
            }
    }

    fun getPlanFromDatabase() {
        db.collection(_uiState.value.email)
            .document("plans")
            .collection("plans_collection")
            .document(_uiState.value.selectedPickerDate)
            .get()
            .addOnSuccessListener { plan ->
                val plans = plan.get("plans")?.toString() ?: ""
                Log.d("Plans Sync", "Plans found with ID: ${plan.id}")

                _uiState.update {
                    it.copy(
                        plansForSelectedDate = plans,
                        havePlans = plans.isNotEmpty()
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.w("Plans Sync", "Error adding plans", e)
            }
    }

    fun getPlansFromDatabase() {
        db.collection(_uiState.value.email)
            .document("plans")
            .collection("plans_collection")
            .get()
            .addOnSuccessListener { plans ->
                val result = plans.map { plan ->
                    Pair(
                        plan.get("plans").toString(),
                        plan.id
                    )
                }.sortedBy { date ->
                    try {
                        LocalDate.parse(date.second, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
                    } catch (e: Exception) {
                        LocalDate.MIN
                    }
                }
                Log.d("Plans Sync", "Imported plans from DB")

                _uiState.update {
                    it.copy(
                        days = result
                    )
                }
                repo.saveDaysList(result.toMutableList())
            }
            .addOnFailureListener { e ->
                Log.w("Plans Sync", "Error adding plans", e)
            }
    }

    fun togglePlanCompletion(index: Int) {
        val selectedDate = _uiState.value.selectedPickerDate
        val currentPlans = _uiState.value.plansForSelectedDate

        val lines = currentPlans.lines().toMutableList()
        if (index in lines.indices) {
            val line = lines[index]
            if (line.endsWith('*')) {
                lines[index] = line.removeSuffix("*")
            } else {
                lines[index] = "$line*"
            }

            val newPlans = lines.joinToString("\n")
            repo.savePlansForDate(selectedDate, newPlans)

            _uiState.update {
                it.copy(plansForSelectedDate = newPlans)
            }

            if (_uiState.value.isSignedIn && isOnline()) {
                sendPlansToDatabase(newPlans)
            }
        }
    }

    fun isOnline() : Boolean {
        return repo.isOnline()
    }
}