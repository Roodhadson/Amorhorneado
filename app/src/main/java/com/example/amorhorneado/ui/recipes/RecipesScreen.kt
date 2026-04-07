package com.example.amorhorneado.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amorhorneado.data.Recipe
import com.example.amorhorneado.ui.dashboard.DashboardViewModel
import com.example.amorhorneado.ui.theme.BakeryOrange

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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val dashboardUiState by dashboardViewModel.dashboardUiState.collectAsState()
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Recetas",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 42.sp
                )
                Text(
                    text = "${uiState.recipeList.size} recetas guardadas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Buscar recetas...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Search,
                        contentDescription = null,
                        tint = BakeryOrange
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BakeryOrange,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                thickness = 0.5.dp,
                color = Color.DarkGray
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
