package com.example.amorhorneado.ui.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.amorhorneado.data.Ingredient
import com.example.amorhorneado.ui.theme.BakeryOrange
import com.example.amorhorneado.ui.theme.CriticalRed
import java.util.Locale

@Composable
fun getIngredientIcon(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    val lowerName = name.lowercase()
    return when {
        lowerName.contains("huevo") -> Icons.Default.Egg
        lowerName.contains("leche") -> Icons.Default.LocalDrink
        lowerName.contains("harina") -> Icons.Default.BakeryDining
        lowerName.contains("mantequilla") || lowerName.contains("marga") -> Icons.Default.Kitchen
        lowerName.contains("azucar") -> Icons.Default.RiceBowl
        lowerName.contains("levadura") -> Icons.Default.Science
        lowerName.contains("aceite") -> Icons.Default.Opacity
        lowerName.contains("sal") -> Icons.Default.Grain
        lowerName.contains("chocolate") || lowerName.contains("cacao") -> Icons.Default.Icecream
        lowerName.contains("queso") -> Icons.Default.BakeryDining 
        lowerName.contains("vainilla") || lowerName.contains("esencia") -> Icons.Default.Colorize
        lowerName.contains("fruta") || lowerName.contains("fresa") -> Icons.Default.ShoppingBasket
        else -> Icons.Default.Inventory2
    }
}

@Composable
fun IngredientsScreen(
    viewModel: IngredientsViewModel,
    onEditIngredient: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

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
                    text = "Insumos",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 42.sp
                )
                Text(
                    text = "${uiState.ingredientList.size} materiales en inventario",
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
                placeholder = { Text("Buscar ingredientes...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
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
                items(uiState.ingredientList) { ingredient ->
                    IngredientItem(
                        ingredient = ingredient,
                        onClick = { onEditIngredient(ingredient.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowStock = ingredient.stock <= ingredient.minStock

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLowStock) CriticalRed.copy(alpha = 0.1f) else BakeryOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!ingredient.imagePath.isNullOrEmpty()) {
                    AsyncImage(
                        model = ingredient.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (isLowStock) Icons.Default.Warning else getIngredientIcon(ingredient.name),
                        contentDescription = null,
                        tint = if (isLowStock) CriticalRed else BakeryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                val unitPart = ingredient.unit.split(" ").lastOrNull()?.replace("(", "")?.replace(")", "") ?: ""
                Text(
                    text = "Costo: $${String.format(Locale.getDefault(), "%.2f", ingredient.costPrice)} / $unitPart",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%.2f", ingredient.stock),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isLowStock) CriticalRed else BakeryOrange
                )
                val unitLabel = ingredient.unit.split(" ").lastOrNull()?.replace("(", "")?.replace(")", "")?.uppercase() ?: ""
                Text(
                    text = unitLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isLowStock) CriticalRed.copy(alpha = 0.7f) else Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
