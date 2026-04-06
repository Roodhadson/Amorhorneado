package com.example.amorhorneado.ui.recipes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.amorhorneado.data.Ingredient
import com.example.amorhorneado.data.ProductionCost
import com.example.amorhorneado.ui.components.BakeryTextField
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: RecipesViewModel,
    exchangeRate: Double,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState = viewModel.recipeUiState
    val availableIngredients by viewModel.availableIngredients.collectAsState()
    val availableProductionCosts by viewModel.productionCosts.collectAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        viewModel.updateUiState(uiState.recipeDetails.copy(imagePath = uri?.toString()))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.recipeDetails.id == 0) "Nueva Receta" else "Editar Receta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BakeryOrange)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Picker
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { photoLauncher.launch("image/*") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (selectedImageUri != null || uiState.recipeDetails.imagePath != null) {
                        AsyncImage(
                            model = selectedImageUri ?: uiState.recipeDetails.imagePath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(48.dp))
                            Text("Foto de la Receta", color = Color.Gray)
                        }
                    }
                }
            }

            item {
                BakeryTextField(
                    label = "NOMBRE DE LA RECETA",
                    value = uiState.recipeDetails.title,
                    onValueChange = { viewModel.updateUiState(uiState.recipeDetails.copy(title = it)) },
                    placeholder = "Ej. Pastel de Chocolate"
                )
            }

            // Min Stock Counter
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = BakeryOrange)
                            Text("Stock Mínimo Deseado", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(
                                onClick = { if (uiState.recipeDetails.minStock > 0) viewModel.updateMinStock(uiState.recipeDetails.minStock - 1) },
                                modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = BakeryOrange)
                            }
                            Text(
                                text = uiState.recipeDetails.minStock.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            IconButton(
                                onClick = { viewModel.updateMinStock(uiState.recipeDetails.minStock + 1) },
                                modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BakeryOrange)
                            }
                        }
                    }
                }
            }

            // Portions Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = BakeryOrange)
                                Text("Venta por Porciones", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Switch(
                                checked = uiState.recipeDetails.isPortionEnabled,
                                onCheckedChange = { viewModel.updateUiState(uiState.recipeDetails.copy(isPortionEnabled = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = BakeryOrange)
                            )
                        }

                        if (uiState.recipeDetails.isPortionEnabled) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Porciones por receta:", color = Color.Gray, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    IconButton(
                                        onClick = { if (uiState.recipeDetails.portionsPerRecipe > 1) viewModel.updateUiState(uiState.recipeDetails.copy(portionsPerRecipe = uiState.recipeDetails.portionsPerRecipe - 1)) },
                                        modifier = Modifier.size(28.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(16.dp))
                                    }
                                    Text(text = uiState.recipeDetails.portionsPerRecipe.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = { viewModel.updateUiState(uiState.recipeDetails.copy(portionsPerRecipe = uiState.recipeDetails.portionsPerRecipe + 1)) },
                                        modifier = Modifier.size(28.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Precio por porción (Informativo, calculado automáticamente)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Precio por Porción (Calculado):", color = Color.LightGray, fontSize = 14.sp)
                                    val price = uiState.recipeDetails.portionSalePrice.toDoubleOrNull() ?: 0.0
                                    PriceInBs(priceInUsd = price, exchangeRate = exchangeRate)
                                }
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", uiState.recipeDetails.portionSalePrice.toDoubleOrNull() ?: 0.0)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Ingredients Section
            item {
                Text("SELECCIONAR INGREDIENTES", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            items(availableIngredients) { ingredient ->
                val selection = uiState.selectedIngredientsMap[ingredient.id]
                val isSelected = selection != null
                IngredientSelectionItem(
                    ingredient = ingredient,
                    isSelected = isSelected,
                    exchangeRate = exchangeRate,
                    initialQuantity = selection?.second?.toString() ?: "",
                    onToggle = { viewModel.toggleIngredientSelection(ingredient) },
                    onQuantityChange = { qty -> 
                        qty.toDoubleOrNull()?.let { viewModel.updateIngredientQuantity(ingredient.id, it) }
                    }
                )
            }

            // Production Costs Section
            item {
                Text("COSTOS ADICIONALES (M. de Obra, Gas, etc.)", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            items(availableProductionCosts) { cost ->
                val isSelected = uiState.selectedProductionCostIds.contains(cost.id)
                ProductionCostSelectionItem(
                    cost = cost,
                    isSelected = isSelected,
                    exchangeRate = exchangeRate,
                    onToggle = { viewModel.toggleProductionCostSelection(cost.id) }
                )
            }

            // Calculation Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Costo Base
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Costo Total (Ing + Gastos):", color = Color.White, fontWeight = FontWeight.Medium)
                                PriceInBs(priceInUsd = uiState.calculatedCost, exchangeRate = exchangeRate)
                            }
                            Text("$${String.format(Locale.getDefault(), "%.2f", uiState.calculatedCost)}", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        // Profit Section
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = uiState.isManualProfit,
                                        onCheckedChange = { viewModel.toggleManualProfit(it) },
                                        colors = CheckboxDefaults.colors(checkedColor = BakeryOrange)
                                    )
                                    Text("Ganancia Manual (%)", color = Color.LightGray, fontSize = 14.sp)
                                }
                                
                                if (uiState.isManualProfit) {
                                    OutlinedTextField(
                                        value = uiState.manualProfitValue,
                                        onValueChange = { viewModel.updateManualProfitValue(it) },
                                        modifier = Modifier.width(80.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        suffix = { Text("%") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = BakeryOrange
                                        )
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { 
                                                if (uiState.profitPercentage > 5) viewModel.updateProfitPercentage(uiState.profitPercentage - 5)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, tint = BakeryOrange)
                                        }
                                        Text(
                                            text = "${uiState.profitPercentage}%",
                                            color = BakeryOrange,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        IconButton(
                                            onClick = { 
                                                if (uiState.profitPercentage < 60) viewModel.updateProfitPercentage(uiState.profitPercentage + 5)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = BakeryOrange)
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        // Final Price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Precio de Venta Final:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                PriceInBs(priceInUsd = uiState.finalPriceWithProfit, exchangeRate = exchangeRate)
                            }
                            Text("$${String.format(Locale.getDefault(), "%.2f", uiState.finalPriceWithProfit)}", color = BakeryOrange, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.saveRecipe {
                            onSaveSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.recipeDetails.title.isNotBlank() && uiState.selectedIngredientsMap.isNotEmpty()
                ) {
                    Text("Guardar Receta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun IngredientSelectionItem(
    ingredient: Ingredient,
    isSelected: Boolean,
    exchangeRate: Double,
    initialQuantity: String,
    onToggle: () -> Unit,
    onQuantityChange: (String) -> Unit
) {
    var quantityText by remember(initialQuantity) { mutableStateOf(initialQuantity) }
    val currentQtyValue = quantityText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val hasEnoughStock = ingredient.stock >= currentQtyValue

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = BakeryOrange)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ingredient.name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "Stock: ${ingredient.stock} ${ingredient.unit.split(" ").last()}", style = MaterialTheme.typography.bodySmall, color = if (hasEnoughStock) Color.Gray else Color.Red)
                    PriceInBs(priceInUsd = ingredient.costPrice, exchangeRate = exchangeRate)
                }
                if (isSelected) {
                    val isKg = ingredient.unit.contains("kg", true)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                val current = quantityText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val step = if (isKg) 0.05 else 1.0
                                if (current >= step) {
                                    val newVal = String.format(Locale.US, "%.2f", current - step)
                                    quantityText = newVal
                                    onQuantityChange(newVal)
                                }
                            },
                            modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = BakeryOrange)
                        }
                        
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { 
                                val newVal = it.replace(",", ".")
                                val newValDouble = newVal.toDoubleOrNull() ?: 0.0
                                if (it.isEmpty() || newValDouble <= ingredient.stock) {
                                    quantityText = newVal
                                    onQuantityChange(quantityText)
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (hasEnoughStock) BakeryOrange else Color.Red,
                                unfocusedBorderColor = if (hasEnoughStock) Color.Gray else Color.Red
                            )
                        )

                        IconButton(
                            onClick = { 
                                val current = quantityText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val step = if (isKg) 0.05 else 1.0
                                if (current + step <= ingredient.stock) {
                                    val newVal = String.format(Locale.US, "%.2f", current + step)
                                    quantityText = newVal
                                    onQuantityChange(newVal)
                                }
                            },
                            enabled = (currentQtyValue + (if (isKg) 0.05 else 1.0)) <= ingredient.stock,
                            modifier = Modifier.size(32.dp).background(
                                if ((currentQtyValue + (if (isKg) 0.05 else 1.0)) <= ingredient.stock) BakeryOrange.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f), 
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = if ((currentQtyValue + (if (isKg) 0.05 else 1.0)) <= ingredient.stock) BakeryOrange else Color.Gray)
                        }
                    }
                }
            }
            
            AnimatedVisibility(visible = isSelected && !hasEnoughStock) {
                Row(
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                    Text(
                        text = "No hay suficiente '${ingredient.name}'",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProductionCostSelectionItem(
    cost: ProductionCost,
    isSelected: Boolean,
    exchangeRate: Double,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = BakeryOrange)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cost.concept, color = Color.White)
                PriceInBs(priceInUsd = cost.amount, exchangeRate = exchangeRate)
            }
            Text(text = "$${cost.amount}", color = BakeryOrange, fontWeight = FontWeight.Bold)
        }
    }
}
