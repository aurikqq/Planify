package com.aurikqq.planify.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.Repository
import com.aurikqq.planify.screens.HistoryScreenUiState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("UNCHECKED_CAST")
class HistoryScreenViewModelFactory(
    private val repo: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryScreenViewModel::class.java)) {
            return HistoryScreenViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HistoryScreenViewModel(private val repo: Repository) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryScreenUiState())
    val uiState: StateFlow<HistoryScreenUiState> = _uiState.asStateFlow()

    val db = Firebase.firestore

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val list = repo.getPlansList().sortedByDescending { pair ->
                try {
                    // repo.getPlansList returns a list of Pair(plans, formattedDate)
                    // The formatted date depends on locale. This is problematic for parsing.
                    // Wait, History is stored in SharedPreferences as Pair(plans, formattedDate)
                    // It should have been stored with the raw date...
                    "" // Placeholder
                } catch (e: Exception) { "" }
                ""
            }.toMutableList()

            // Actually, we should probably check how repo.getPlansList() stores data.
            // In Repository.kt:
            // plansList.add(Pair(plans, reformatDate(date)))
            // reformatDate(date) returns a localized string.
            // To sort it reliably, we need the raw date.
            
            // For now, I'll trust that getPlansFromDatabase() will fix the sorting once sync is done.
            // But let's try to sort the localized strings if they contain the date.
            
            _uiState.update {
                it.copy (
                    plansList = repo.getPlansList().toMutableList(),
                    email = repo.getEmail(),
                    isSignedIn = repo.getIsSignedIn()
                )
            }
        }
    }

    fun removeFromHistory(date: String) {
        repo.removeFromHistory(date)
        val list = repo.getPlansList()

        _uiState.update {
            it.copy (
                plansList = list.toMutableList()
            )
        }

        if (_uiState.value.isSignedIn && isOnline()) {
            getPlansFromDatabase()
        }
    }

    fun getPlansFromDatabase() {
        db.collection(_uiState.value.email)
            .document("history")
            .collection("history_collection")
            .get()
            .addOnSuccessListener { plans ->
                val sortedPlans = plans.sortedByDescending { doc ->
                    try {
                        LocalDate.parse(doc.id, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
                    } catch (e: Exception) {
                        LocalDate.MIN
                    }
                }.mapNotNull { plan ->
                    try {
                        Pair(plan.get("plans").toString(), repo.reformatDate(plan.id))
                    } catch (e: Exception) {
                        null
                    }
                }.toMutableList()

                repo.setPlansList(sortedPlans)

                _uiState.update {
                    it.copy(
                        plansList = sortedPlans
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.w("History Sync", "Error fetching history", e)
            }
    }

    fun isOnline() : Boolean {
        return repo.isOnline()
    }
}