package com.example.amorhorneado.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import com.example.amorhorneado.ui.theme.BakeryTextGold
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    viewModel: RecipesViewModel,
    exchangeRate: Double,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val recipeDetails by viewModel.getRecipeFullDetails(recipeId).collectAsState(initial = null)
    val ingredientsWithQty by viewModel.getIngredientsWithQuantity(recipeId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de Receta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BakeryOrange)
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(recipeId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = BakeryOrange)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        recipeDetails?.let { details ->
            val recipe = details.recipe
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (recipe.imagePath != null) {
                            AsyncImage(
                                model = recipe.imagePath,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                                Text("🍰", fontSize = 64.sp)
                            }
                        }
                    }
                }

                // Title and Prices
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = recipe.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Precio de Venta", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", recipe.manualSalePrice ?: 0.0)}",
                                    color = BakeryOrange,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 28.sp
                                )
                                PriceInBs(priceInUsd = recipe.manualSalePrice ?: 0.0, exchangeRate = exchangeRate)
                            }
                            if (recipe.isPortionEnabled) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Por Porción", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        text = "$${String.format(Locale.getDefault(), "%.2f", recipe.portionSalePrice ?: 0.0)}",
                                        color = BakeryTextGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    PriceInBs(priceInUsd = recipe.portionSalePrice ?: 0.0, exchangeRate = exchangeRate)
                                }
                            }
                        }
                    }
                }

                // Stats
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            label = "Stock Mínimo",
                            value = "x${recipe.minStock}",
                            icon = Icons.Default.Inventory,
                            modifier = Modifier.weight(1f)
                        )
                        if (recipe.isPortionEnabled) {
                            StatCard(
                                label = "Porciones",
                                value = "x${recipe.portionsPerRecipe}",
                                icon = Icons.Default.Restaurant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Ingredients List
                item {
                    Text("INGREDIENTES", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(ingredientsWithQty) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.ingredient.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(
                                    "Costo: $${String.format(Locale.getDefault(), "%.2f", item.ingredient.costPrice * item.quantity)}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                "${item.quantity} ${item.ingredient.unit.split(" ").last()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Production Costs
                if (details.productionCosts.isNotEmpty()) {
                    item {
                        Text("COSTOS ADICIONALES", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    items(details.productionCosts) { cost ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cost.concept, color = Color.White)
                                Text("$${cost.amount}", color = BakeryOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
