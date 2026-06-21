package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepo = UserPreferencesRepository(application)
    
    val isSetupCompleted: StateFlow<Boolean> = userPrefsRepo.isSetupCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val ownerName: StateFlow<String> = userPrefsRepo.ownerName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        
    val farmName: StateFlow<String> = userPrefsRepo.farmName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateOwnerName(name: String) {
        _uiState.update { it.copy(ownerName = name) }
    }

    fun updateFarmName(name: String) {
        _uiState.update { it.copy(farmName = name) }
    }

    fun completeSetup() {
        val currentState = _uiState.value
        viewModelScope.launch {
            userPrefsRepo.saveSetupConfiguration(
                owner = currentState.ownerName.trim(),
                farm = currentState.farmName.trim(),
                isCompleted = true
            )
        }
    }

    fun skipSetup() {
        viewModelScope.launch {
            userPrefsRepo.saveSetupConfiguration(
                owner = "المالك الافتراضي",
                farm = "مزرعتي",
                isCompleted = true
            )
        }
    }
}

data class OnboardingUiState(
    val ownerName: String = "",
    val farmName: String = ""
) {
    val isInputValid: Boolean
        get() = ownerName.isNotBlank() && farmName.isNotBlank()
}
