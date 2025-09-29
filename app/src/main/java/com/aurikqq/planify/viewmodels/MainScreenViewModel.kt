package com.aurikqq.planify.viewmodels

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.R
import com.aurikqq.planify.Repository
import com.aurikqq.planify.screens.PlansScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Suppress("UNCHECKED_CAST")
class MainScreenViewModelFactory(
    private val repo: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainScreenViewModel::class.java)) {
            return MainScreenViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainScreenViewModel(private val repo: Repository) : ViewModel() {
    private val _uiState = MutableStateFlow(PlansScreenUiState())
    val uiState: StateFlow<PlansScreenUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(
                "dd_MM_yyyy", Locale.getDefault()))
            val days = repo.getDaysList()
            val plans = repo.getPlansForDate(currentDate)
            val havePlans = repo.havePlansForDate(currentDate)
            val isFirstLaunch = repo.getIsFirstLaunch()

            _uiState.update {
                it.copy (
                    currentDate = currentDate,
                    selectedPickerDate = currentDate,
                    days = days,
                    plansForSelectedDate = plans,
                    havePlans = havePlans,
                    isFirstLaunch = isFirstLaunch
                )
            }
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

    fun changeDateSelectedInPicker(date: String) {
        _uiState.update {
            it.copy (
                selectedPickerDate = date,
            )
        }
    }

    fun onTempPlanInputChange(new: String) {
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

        _uiState.update { it.copy(days = currentDaysList)}
    }

    fun setIsDaysListEditing() {
        _uiState.update {
            it.copy(
                isDaysListEditing = !_uiState.value.isDaysListEditing
            )
        }
    }

    fun removeDay(day: Pair<String, String>) {
        val currentDaysList = _uiState.value.days.toMutableList()
        currentDaysList.remove(day)
        repo.saveDaysList(currentDaysList)

        _uiState.update { it.copy(days = currentDaysList) }
    }
}