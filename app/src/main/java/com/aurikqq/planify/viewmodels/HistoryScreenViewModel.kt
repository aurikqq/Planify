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
        observeRepositoryChanges()
    }

    private fun observeRepositoryChanges() {
        viewModelScope.launch {
            repo.plansUpdatedFlow.collect {
                val list = repo.getPlansList().sortedByDescending { pair ->
                    try {
                        val rawDate = repo.reformatHistoryDate(pair.second)
                        LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
                    } catch (e: Exception) {
                        LocalDate.MIN
                    }
                }.toMutableList()

                _uiState.update {
                    it.copy(
                        plansPrefix = repo.getPlansPrefix(),
                        plansList = list
                    )
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val list = repo.getPlansList().sortedByDescending { pair ->
                try {
                    val rawDate = repo.reformatHistoryDate(pair.second)
                    LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
                } catch (e: Exception) {
                    LocalDate.MIN
                }
            }.toMutableList()

            _uiState.update {
                it.copy (
                    plansList = list,
                    email = repo.getEmail(),
                    isSignedIn = repo.getIsSignedIn(),
                    plansPrefix = repo.getPlansPrefix()
                )
            }
        }
    }

    fun removeFromHistory(date: String) {
        repo.removeFromHistory(date)
        val list = repo.getPlansList().sortedByDescending { pair ->
            try {
                val rawDate = repo.reformatHistoryDate(pair.second)
                LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
            } catch (e: Exception) {
                LocalDate.MIN
            }
        }.toMutableList()

        _uiState.update {
            it.copy (
                plansList = list
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

    fun toggleHistoryPlanCompletion(formattedDate: String, index: Int) {
        val historyList = repo.getPlansList().toMutableList()
        val historyIndex = historyList.indexOfFirst { it.second == formattedDate }

        if (historyIndex != -1) {
            val plans = historyList[historyIndex].first
            val prefix = _uiState.value.plansPrefix
            val lines = plans.lines().toMutableList()

            if (index in lines.indices) {
                val line = lines[index]
                if (line.contains("$prefix*")) {
                    lines[index] = line.replaceFirst("$prefix*", prefix)
                } else if (line.contains(prefix)) {
                    lines[index] = line.replaceFirst(prefix, "$prefix*")
                }

                val newPlans = lines.joinToString("\n")
                historyList[historyIndex] = historyList[historyIndex].copy(first = newPlans)
                repo.setPlansList(historyList)

                _uiState.update {
                    it.copy(plansList = historyList)
                }

                try {
                    val rawDate = repo.reformatHistoryDate(formattedDate)
                    if (_uiState.value.isSignedIn && isOnline()) {
                        repo.savePlansToHistoryDatabase(rawDate, newPlans)
                    }
                } catch (e: Exception) {
                    Log.e("History", "Error reformating date for DB sync", e)
                }
            }
        }
    }

    fun isOnline() : Boolean {
        return repo.isOnline()
    }
}