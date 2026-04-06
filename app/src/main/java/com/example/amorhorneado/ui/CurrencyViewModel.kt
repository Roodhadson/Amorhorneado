package com.example.amorhorneado.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.IngredientRepository
import com.example.amorhorneado.data.UserConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CurrencyViewModel(private val repository: IngredientRepository) : ViewModel() {
    private val _exchangeRate = MutableStateFlow(0.0)
    val exchangeRate: StateFlow<Double> = _exchangeRate.asStateFlow()

    var showDialog by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val config = repository.getUserConfigStream().first()
            if (config != null && config.lastExchangeRate > 0.0) {
                _exchangeRate.value = config.lastExchangeRate
                showDialog = false
            } else {
                showDialog = true
            }
        }
    }

    fun updateExchangeRate(rate: Double) {
        _exchangeRate.value = rate
        showDialog = false
        viewModelScope.launch {
            val currentConfig = repository.getUserConfigStream().first() ?: UserConfig()
            repository.insertUserConfig(currentConfig.copy(lastExchangeRate = rate))
        }
    }

    fun openDialog() {
        showDialog = true
    }

    fun dismissDialog() {
        showDialog = false
    }
}
