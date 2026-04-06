package com.example.amorhorneado.ui.raffle

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.IngredientRepository
import com.example.amorhorneado.ui.theme.BakeryOrange
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RaffleParticipant(
    val id: Int,
    val name: String,
    val isSelected: Boolean = true
)

data class RaffleUiState(
    val participants: List<RaffleParticipant> = emptyList(),
    val color1: Color = Color(0xFF2D2013),
    val color2: Color = Color(0xFF1A120B),
    val arrowColor: Color = BakeryOrange,
    val winner: RaffleParticipant? = null,
    val targetWinnerIndex: Int = -1
)

class RaffleViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RaffleUiState())
    val uiState: StateFlow<RaffleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllCustomersStream(),
                repository.getAllSalesRecordsStream()
            ) { customers, sales ->
                val customersWithSales = customers.filter { customer ->
                    sales.any { it.customerId == customer.id }
                }.map { RaffleParticipant(it.id, it.name) }
                
                _uiState.update { it.copy(participants = customersWithSales) }
            }.collect()
        }
    }

    fun toggleParticipantSelection(participantId: Int) {
        _uiState.update { state ->
            val updatedParticipants = state.participants.map {
                if (it.id == participantId) it.copy(isSelected = !it.isSelected) else it
            }
            state.copy(participants = updatedParticipants)
        }
    }

    fun requestSpin() {
        val activeParticipants = _uiState.value.participants.filter { it.isSelected }
        if (activeParticipants.isNotEmpty()) {
            val randomIndex = (activeParticipants.indices).random()
            _uiState.update { it.copy(
                winner = null, 
                targetWinnerIndex = randomIndex
            ) }
        }
    }

    fun onAnimationFinished() {
        val activeParticipants = _uiState.value.participants.filter { it.isSelected }
        val index = _uiState.value.targetWinnerIndex
        if (index in activeParticipants.indices) {
            _uiState.update { it.copy(winner = activeParticipants[index]) }
        }
    }
    
    fun clearWinner() {
        _uiState.update { it.copy(winner = null, targetWinnerIndex = -1) }
    }
}
