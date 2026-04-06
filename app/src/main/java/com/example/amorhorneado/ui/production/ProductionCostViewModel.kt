package com.example.amorhorneado.ui.production

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.IngredientRepository
import com.example.amorhorneado.data.ProductionCost
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductionCostViewModel(private val repository: IngredientRepository) : ViewModel() {

    val uiState: StateFlow<ProductionUiState> =
        repository.getAllProductionCostsStream().map { ProductionUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ProductionUiState()
            )

    var costUiState by mutableStateOf(CostUiState())
        private set

    fun updateUiState(costDetails: CostDetails) {
        costUiState = CostUiState(costDetails = costDetails)
    }

    fun saveCost() {
        viewModelScope.launch {
            repository.insertProductionCost(costUiState.costDetails.toProductionCost())
            costUiState = CostUiState() // Reset
        }
    }

    fun deleteCost(cost: ProductionCost) {
        viewModelScope.launch {
            repository.deleteProductionCost(cost)
        }
    }

    fun loadCost(cost: ProductionCost) {
        costUiState = CostUiState(cost.toCostDetails())
    }
}

data class ProductionUiState(val costList: List<ProductionCost> = listOf())

data class CostUiState(val costDetails: CostDetails = CostDetails())

data class CostDetails(
    val id: Int = 0,
    val concept: String = "",
    val amount: String = ""
)

fun CostDetails.toProductionCost(): ProductionCost = ProductionCost(
    id = id,
    concept = concept,
    amount = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
)

fun ProductionCost.toCostDetails(): CostDetails = CostDetails(
    id = id,
    concept = concept,
    amount = amount.toString()
)
