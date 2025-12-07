package com.aurikqq.planify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.screens.HistoryScreenUiState
import com.aurikqq.planify.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val list = repo.getPlansList()

            _uiState.update {
                it.copy (
                    plansList = list
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
    }
}