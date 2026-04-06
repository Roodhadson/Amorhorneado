package com.example.amorhorneado.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.ingredients.IngredientsViewModel
import com.example.amorhorneado.ui.theme.BakeryOrange
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: IngredientsViewModel,
    exchangeRate: Double,
    onEditIngredient: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var ingredientToDelete by remember { mutableStateOf<Ingredient?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inventario", fontWeight = FontWeight.Bold) },
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
                        text = "AGREGADOS RECIENTEMENTE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                items(uiState.ingredientList) { ingredient ->
                    IngredientItemCard(
                        ingredient = ingredient,
                        exchangeRate = exchangeRate,
                        onEdit = { onEditIngredient(ingredient.id) },
                        onDelete = { ingredientToDelete = ingredient }
                    )
                }
            }
        }

        if (ingredientToDelete != null) {
            AlertDialog(
                onDismissRequest = { ingredientToDelete = null },
                title = { Text("¿Desea eliminar este ingrediente?") },
                text = { Text("Esta acción borrará el ingrediente del inventario permanentemente.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteIngredient(ingredientToDelete!!)
                        ingredientToDelete = null
                    }) {
                        Text("Sí", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ingredientToDelete = null }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
fun IngredientItemCard(
    ingredient: Ingredient,
    exchangeRate: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val unitSuffix = if (ingredient.unit.contains("kg", true)) "Kg" else "Ud"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BakeryOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (ingredient.imagePath != null) {
                    AsyncImage(
                        model = ingredient.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🍞", fontSize = 24.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = "Stock: ${ingredient.stock} $unitSuffix",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                PriceInBs(priceInUsd = ingredient.costPrice, exchangeRate = exchangeRate)
            }

            Text(
                text = "$${String.format(Locale.getDefault(), "%.1f", ingredient.costPrice)}/$unitSuffix",
                color = BakeryOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar", color = Color.White) },
                        onClick = {
                            expanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
