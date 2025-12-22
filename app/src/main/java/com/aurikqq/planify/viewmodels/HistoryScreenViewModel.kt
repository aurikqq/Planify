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
            val list = repo.getPlansList()

            _uiState.update {
                it.copy (
                    plansList = list,
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
                plansList = list
            )
        }

        if (_uiState.value.isSignedIn) {
            getPlansFromDatabase()
        }
    }

    fun getPlansFromDatabase() {
        val now = LocalDate.now()

        db.collection(_uiState.value.email)
            .document("plans")
            .collection("plans_collection")
            .get()
            .addOnSuccessListener { plans ->
                val result = plans.mapNotNull { plan ->
                    val formattedDate = LocalDate.parse(plan.id, DateTimeFormatter.ofPattern("dd_MM_yyyy"))
                    if (now.isAfter(formattedDate) || now.isEqual(formattedDate)) {
                        Pair(plan.get("plans").toString(), repo.reformatDate(plan.id))
                    } else null
                }.toMutableList()

                repo.setPlansList(result)

                _uiState.update {
                    it.copy(
                        plansList = result
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.w("Plans Sync", "Error adding plans", e)
            }
    }
}