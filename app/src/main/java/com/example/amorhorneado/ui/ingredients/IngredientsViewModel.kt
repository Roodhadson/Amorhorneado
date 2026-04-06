package com.example.amorhorneado.ui.ingredients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.Ingredient
import com.example.amorhorneado.data.IngredientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientsViewModel(private val repository: IngredientRepository) : ViewModel() {

    val uiState: StateFlow<IngredientsUiState> =
        repository.getAllIngredientsStream().map { IngredientsUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = IngredientsUiState()
            )

    var ingredientUiState by mutableStateOf(IngredientUiState())
        private set

    fun updateUiState(ingredientDetails: IngredientDetails) {
        ingredientUiState = IngredientUiState(ingredientDetails = ingredientDetails)
    }

    fun loadIngredient(id: Int) {
        viewModelScope.launch {
            repository.getIngredientStream(id).collect { ingredient ->
                ingredient?.let {
                    ingredientUiState = IngredientUiState(it.toIngredientDetails())
                }
            }
        }
    }

    fun saveIngredient(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val ingredient = ingredientUiState.ingredientDetails.toIngredient()
            if (ingredient.id == 0) {
                repository.insertIngredient(ingredient)
            } else {
                repository.updateIngredient(ingredient)
            }
            ingredientUiState = IngredientUiState() // Reset
            onSuccess()
        }
    }
    
    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredient)
        }
    }
}

data class IngredientsUiState(val ingredientList: List<Ingredient> = listOf())

data class IngredientUiState(
    val ingredientDetails: IngredientDetails = IngredientDetails()
)

data class IngredientDetails(
    val id: Int = 0,
    val name: String = "",
    val costPrice: String = "",
    val salePrice: String = "",
    val unit: String = "Kilogramos (kg)",
    val stock: String = "",
    val minStock: String = "0",
    val imagePath: String? = null
)

fun IngredientDetails.toIngredient(): Ingredient = Ingredient(
    id = id,
    name = name,
    costPrice = costPrice.toDoubleOrNull() ?: 0.0,
    salePrice = salePrice.toDoubleOrNull() ?: 0.0,
    unit = unit,
    stock = stock.toDoubleOrNull() ?: 0.0,
    minStock = minStock.toDoubleOrNull() ?: 0.0,
    imagePath = imagePath
)

fun Ingredient.toIngredientDetails(): IngredientDetails = IngredientDetails(
    id = id,
    name = name,
    costPrice = costPrice.toString(),
    salePrice = salePrice.toString(),
    unit = unit,
    stock = stock.toString(),
    minStock = minStock.toString(),
    imagePath = imagePath
)
