package com.example.amorhorneado.ui.recipes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipesViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val recipesUiState: StateFlow<RecipesUiState> =
        combine(repository.getAllRecipesStream(), _searchQuery) { recipes, query ->
            val filteredRecipes = if (query.isEmpty()) {
                recipes
            } else {
                recipes.filter { it.title.contains(query, ignoreCase = true) }
            }
            RecipesUiState(filteredRecipes)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecipesUiState()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val availableIngredients: StateFlow<List<Ingredient>> =
        repository.getAllIngredientsStream()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val productionCosts: StateFlow<List<ProductionCost>> =
        repository.getAllProductionCostsStream()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    var recipeUiState by mutableStateOf(RecipeUiState())
        private set

    fun updateUiState(recipeDetails: RecipeDetails) {
        recipeUiState = recipeUiState.copy(recipeDetails = recipeDetails)
        calculateCosts()
    }

    fun loadRecipe(id: Int) {
        viewModelScope.launch {
            repository.getRecipeFullDetailsStream(id).firstOrNull()?.let { details ->
                val ingredientsWithQty = repository.getIngredientsWithQuantityForRecipe(id).first()
                val ingredientsMap = ingredientsWithQty.associate { item ->
                    item.ingredient.id to (item.ingredient to item.quantity)
                }
                
                recipeUiState = RecipeUiState(
                    recipeDetails = details.recipe.toRecipeDetails(),
                    selectedIngredientsMap = ingredientsMap,
                    selectedProductionCostIds = details.productionCosts.map { it.id }.toSet(),
                    profitPercentage = 30
                )
                calculateCosts()
            }
        }
    }

    fun toggleIngredientSelection(ingredient: Ingredient) {
        val currentSelected = recipeUiState.selectedIngredientsMap.toMutableMap()
        if (currentSelected.containsKey(ingredient.id)) {
            currentSelected.remove(ingredient.id)
        } else {
            currentSelected[ingredient.id] = ingredient to 1.0
        }
        recipeUiState = recipeUiState.copy(selectedIngredientsMap = currentSelected)
        calculateCosts()
    }

    fun updateIngredientQuantity(ingredientId: Int, quantity: Double) {
        val currentSelected = recipeUiState.selectedIngredientsMap.toMutableMap()
        currentSelected[ingredientId]?.let { (ing, _) ->
            currentSelected[ingredientId] = ing to quantity
        }
        recipeUiState = recipeUiState.copy(selectedIngredientsMap = currentSelected)
        calculateCosts()
    }

    fun toggleProductionCostSelection(costId: Int) {
        val currentSelected = recipeUiState.selectedProductionCostIds.toMutableSet()
        if (currentSelected.contains(costId)) {
            currentSelected.remove(costId)
        } else {
            currentSelected.add(costId)
        }
        recipeUiState = recipeUiState.copy(selectedProductionCostIds = currentSelected)
        calculateCosts()
    }

    fun updateProfitPercentage(percentage: Int) {
        recipeUiState = recipeUiState.copy(profitPercentage = percentage)
        calculateCosts()
    }

    fun toggleManualProfit(enabled: Boolean) {
        recipeUiState = recipeUiState.copy(isManualProfit = enabled)
        calculateCosts()
    }

    fun updateManualProfitValue(value: String) {
        recipeUiState = recipeUiState.copy(manualProfitValue = value)
        calculateCosts()
    }

    fun updateMinStock(stock: Int) {
        recipeUiState = recipeUiState.copy(recipeDetails = recipeUiState.recipeDetails.copy(minStock = stock))
    }

    fun clearErrorMessage() {
        recipeUiState = recipeUiState.copy(errorMessage = null)
    }

    private fun calculateCosts() {
        val ingredientsCost = recipeUiState.selectedIngredientsMap.values.sumOf { (ing, qty) -> 
            ing.costPrice * qty 
        }
        
        val selectedCostsSum = productionCosts.value
            .filter { recipeUiState.selectedProductionCostIds.contains(it.id) }
            .sumOf { it.amount }
            
        val baseCost = ingredientsCost + selectedCostsSum
        
        val profitPercent = if (recipeUiState.isManualProfit) {
            recipeUiState.manualProfitValue.replace(",", ".").toDoubleOrNull() ?: 0.0
        } else {
            recipeUiState.profitPercentage.toDouble()
        }
        
        val finalPrice = baseCost * (1 + (profitPercent / 100))
        
        // Always calculate portion price based on division
        val calculatedPortionPrice = if (recipeUiState.recipeDetails.portionsPerRecipe > 0) {
            finalPrice / recipeUiState.recipeDetails.portionsPerRecipe
        } else 0.0

        recipeUiState = recipeUiState.copy(
            calculatedCost = baseCost,
            finalPriceWithProfit = finalPrice,
            recipeDetails = recipeUiState.recipeDetails.copy(
                costPrice = baseCost.toString(),
                salePrice = finalPrice.toString(),
                baseCost = baseCost,
                portionSalePrice = calculatedPortionPrice.toString()
            )
        )
    }

    fun getRecipeFullDetails(recipeId: Int): Flow<RecipeFullDetails> {
        return repository.getRecipeFullDetailsStream(recipeId)
    }

    fun getIngredientsWithQuantity(recipeId: Int): Flow<List<IngredientWithQuantity>> {
        return repository.getIngredientsWithQuantityForRecipe(recipeId)
    }

    fun saveRecipe(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val recipe = recipeUiState.recipeDetails.toRecipe()
            val isNew = recipe.id == 0
            
            if (isNew) {
                val recipeId = repository.insertRecipe(recipe).toInt()
                saveRecipeDetails(recipeId)
            } else {
                repository.updateRecipe(recipe)
                repository.deleteIngredientsForRecipe(recipe.id)
                repository.deleteProductionCostsForRecipe(recipe.id)
                saveRecipeDetails(recipe.id)
            }
            
            recipeUiState = RecipeUiState()
            onSuccess()
        }
    }

    private suspend fun saveRecipeDetails(recipeId: Int) {
        recipeUiState.selectedIngredientsMap.forEach { (_, pair) ->
            val (ingredient, quantity) = pair
            repository.insertRecipeIngredient(RecipeIngredientCrossRef(recipeId, ingredient.id, quantity))
        }
        
        recipeUiState.selectedProductionCostIds.forEach { costId ->
            repository.insertRecipeProductionCost(RecipeProductionCostCrossRef(recipeId, costId))
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }
}

data class RecipesUiState(val recipeList: List<Recipe> = listOf())

data class RecipeUiState(
    val recipeDetails: RecipeDetails = RecipeDetails(),
    val selectedIngredientsMap: Map<Int, Pair<Ingredient, Double>> = emptyMap(),
    val selectedProductionCostIds: Set<Int> = emptySet(),
    val calculatedCost: Double = 0.0,
    val profitPercentage: Int = 30,
    val isManualProfit: Boolean = false,
    val manualProfitValue: String = "",
    val finalPriceWithProfit: Double = 0.0,
    val errorMessage: String? = null
)

data class RecipeDetails(
    val id: Int = 0,
    val title: String = "",
    val costPrice: String = "0.0",
    val salePrice: String = "",
    val imagePath: String? = null,
    val minStock: Int = 0,
    val baseCost: Double = 0.0,
    val isPortionEnabled: Boolean = false,
    val portionsPerRecipe: Int = 1,
    val portionSalePrice: String = ""
)

fun RecipeDetails.toRecipe(): Recipe = Recipe(
    id = id,
    title = title,
    manualSalePrice = salePrice.replace(",", ".").toDoubleOrNull(),
    imagePath = imagePath,
    minStock = minStock,
    baseCost = baseCost,
    isPortionEnabled = isPortionEnabled,
    portionsPerRecipe = portionsPerRecipe,
    portionSalePrice = portionSalePrice.replace(",", ".").toDoubleOrNull()
)

fun Recipe.toRecipeDetails(): RecipeDetails = RecipeDetails(
    id = id,
    title = title,
    salePrice = manualSalePrice?.toString() ?: "",
    imagePath = imagePath,
    minStock = minStock,
    baseCost = baseCost,
    isPortionEnabled = isPortionEnabled,
    portionsPerRecipe = portionsPerRecipe,
    portionSalePrice = portionSalePrice?.toString() ?: ""
)
