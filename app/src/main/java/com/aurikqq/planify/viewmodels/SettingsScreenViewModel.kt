package com.aurikqq.planify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurikqq.planify.Repository
import com.aurikqq.planify.screens.SettingsScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class SettingsScreenViewModelFactory(
    private val repo: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsScreenViewModel::class.java)) {
            return SettingsScreenViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

open class SettingsScreenViewModel(private val repo: Repository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState: StateFlow<SettingsScreenUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy (
                    plansNotificationsEnabled = repo.getPlansNotificationsEnabled(),
                    plansNotificationsCooldown = repo.getPlansNotificationsCooldown(),
                    resetNotificationsEnabled = repo.getResetNotificationsEnabled(),
                    isDarkThemeOn = repo.getIsInDarkTheme(),
                    isSignedIn = repo.getIsSignedIn(),
                    email = repo.getEmail(),
                    plansPrefix = repo.getPlansPrefix(),
                    isPrefixHintShown = repo.getIsPrefixHintShown(),
                    isNotesButtonAtEnd = repo.getNotesButtonPlacement()
                )
            }
        }
    }

    fun setPlansNotificationsEnabled(value: Boolean) {
        repo.plansNotificationsEnabled(value)
        _uiState.update {
            it.copy (
                plansNotificationsEnabled = value
            )
        }
    }
    fun setResetNotificationsEnabled(value: Boolean) {
        repo.resetNotificationsEnabled(value)
        _uiState.update {
            it.copy (
                resetNotificationsEnabled = value
            )
        }
    }
    fun setPlansNotificationCooldown(value: Float) {
        repo.notificationsCooldown(value)
        _uiState.update {
            it.copy (
                plansNotificationsCooldown = value
            )
        }
    }
    fun setIsInDarkTheme(value: Boolean) {
        repo.setIsInDarkTheme(value)
        _uiState.update {
            it.copy (
                isDarkThemeOn = value
            )
        }
    }

    fun logIn(email: String) {
        repo.setUserEmail(email)
        repo.syncOnSignIn()

        _uiState.update {
            it.copy(
                isSignedIn = true,
                email = email
            )
        }
    }

    fun logOut() {
        repo.setUserEmail("")

        _uiState.update {
            it.copy(
                isSignedIn = false,
                email = ""
            )
        }
    }

    fun setPlansPrefix(prefix: String) {
        val oldPrefix = repo.getPlansPrefix()
        viewModelScope.launch {
            repo.setPlansPrefix(prefix)
            repo.updatePrefixInAllPlans(oldPrefix, prefix)

            _uiState.update {
                it.copy(
                    plansPrefix = prefix
                )
            }
        }
    }

    fun hidePrefixHint() {
        repo.setIsPrefixHintShown(false)
        _uiState.update {
            it.copy(
                isPrefixHintShown = false
            )
        }
    }

    fun setNotesButtonPlacement(atEnd: Boolean) {
        repo.setNotesButtonPlacement(atEnd)
        _uiState.update {
            it.copy(
                isNotesButtonAtEnd = atEnd
            )
        }
    }

    fun getPlansPrefix(): String {
        return repo.getPlansPrefix()
    }
}