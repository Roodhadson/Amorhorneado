package com.example.amorhorneado.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amorhorneado.data.Recipe
import com.example.amorhorneado.ui.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
    dashboardViewModel: DashboardViewModel,
    exchangeRate: Double,
    onRecipeClick: (Int) -> Unit,
    onEditRecipe: (Int) -> Unit,
    onAddToProduction: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.recipesUiState.collectAsState()
    val dashboardUiState by dashboardViewModel.dashboardUiState.collectAsState()
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recetas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "RECETAS GUARDADAS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(uiState.recipeList) { recipe ->
                    val isTop = dashboardUiState.topProduct?.id == recipe.id
                    RecipeItem(
                        recipe = recipe,
                        isTop = isTop,
                        exchangeRate = exchangeRate,
                        onClick = { onRecipeClick(recipe.id) },
                        onEdit = { onEditRecipe(recipe.id) },
                        onDelete = { recipeToDelete = recipe },
                        onAddToProduction = { onAddToProduction(recipe.id) }
                    )
                }
            }
        }

        if (recipeToDelete != null) {
            AlertDialog(
                onDismissRequest = { recipeToDelete = null },
                title = { Text("¿Desea eliminar esta receta?") },
                text = { Text("Esta acción borrará la receta permanentemente.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteRecipe(recipeToDelete!!)
                        recipeToDelete = null
                    }) {
                        Text("Sí", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recipeToDelete = null }) {
                        Text("No")
                    }
                }
            )
        }
    }
}
