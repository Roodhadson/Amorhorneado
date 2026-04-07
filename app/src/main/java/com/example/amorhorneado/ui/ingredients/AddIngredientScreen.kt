package com.example.amorhorneado.ui.ingredients

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.amorhorneado.ui.components.BakeryTextField
import com.example.amorhorneado.ui.theme.BakeryOrange
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    viewModel: IngredientsViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState = viewModel.ingredientUiState
    val scrollState = rememberScrollState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        viewModel.updateUiState(uiState.ingredientDetails.copy(imagePath = uri?.toString()))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.ingredientDetails.id == 0) "Nuevo Ingrediente" else "Editar Ingrediente", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Picker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable { photoLauncher.launch("image/*") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (selectedImageUri != null || uiState.ingredientDetails.imagePath != null) {
                    AsyncImage(
                        model = selectedImageUri ?: uiState.ingredientDetails.imagePath,
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
                        Text("Añadir Foto", color = Color.Gray)
                    }
                }
            }

            BakeryTextField(
                label = "NOMBRE DEL INGREDIENTE",
                value = uiState.ingredientDetails.name,
                onValueChange = { viewModel.updateUiState(uiState.ingredientDetails.copy(name = it)) },
                placeholder = "Ej. Harina de Trigo"
            )

            // Purchase Format (Packaging)
            var showPackagingDialog by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showPackagingDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BakeryOrange.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = BakeryOrange)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Formato de Compra (Cajas/Sacos)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Toca para calcular por unidades/kilos", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            if (showPackagingDialog) {
                PackagingCalculatorDialog(
                    onDismiss = { showPackagingDialog = false },
                    onConfirm = { totalUnits, totalPrice ->
                        val unitPrice = totalPrice / totalUnits
                        viewModel.updateUiState(uiState.ingredientDetails.copy(
                            costPrice = String.format(Locale.US, "%.4f", unitPrice),
                            stock = totalUnits.toString()
                        ))
                        showPackagingDialog = false
                    }
                )
            }

            // Unit Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("UNIDAD DE MEDIDA", color = Color.White, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val units = listOf("Kilogramos (kg)", "Unidades (ud)", "Litros (L)")
                    units.forEach { unit ->
                        FilterChip(
                            selected = uiState.ingredientDetails.unit == unit,
                            onClick = { viewModel.updateUiState(uiState.ingredientDetails.copy(unit = unit)) },
                            label = { Text(unit, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BakeryOrange,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    BakeryTextField(
                        label = "COSTO ($)",
                        value = uiState.ingredientDetails.costPrice,
                        onValueChange = { viewModel.updateUiState(uiState.ingredientDetails.copy(costPrice = it)) },
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }

            // Stock Actual with + and - buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("STOCK ACTUAL", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                val current = uiState.ingredientDetails.stock.replace(",", ".").toDoubleOrNull() ?: 0.0
                                if (current > 0) {
                                    val step = when {
                                        uiState.ingredientDetails.unit.contains("kg", true) -> 0.150
                                        uiState.ingredientDetails.unit.contains("(L)", true) -> 0.225
                                        else -> 1.0
                                    }
                                    val newVal = (current - step).coerceAtLeast(0.0)
                                    viewModel.updateUiState(uiState.ingredientDetails.copy(stock = String.format(Locale.US, "%.3f", newVal)))
                                }
                            },
                            modifier = Modifier.size(40.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = BakeryOrange)
                        }

                        OutlinedTextField(
                            value = uiState.ingredientDetails.stock,
                            onValueChange = { 
                                if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) {
                                    viewModel.updateUiState(uiState.ingredientDetails.copy(stock = it))
                                }
                            },
                            modifier = Modifier.width(120.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BakeryOrange,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        IconButton(
                            onClick = {
                                val current = uiState.ingredientDetails.stock.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val step = when {
                                    uiState.ingredientDetails.unit.contains("kg", true) -> 0.150
                                    uiState.ingredientDetails.unit.contains("(L)", true) -> 0.225
                                    else -> 1.0
                                }
                                val newVal = current + step
                                viewModel.updateUiState(uiState.ingredientDetails.copy(stock = String.format(Locale.US, "%.3f", newVal)))
                            },
                            modifier = Modifier.size(40.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BakeryOrange)
                        }
                    }
                }
            }

            // Min Stock Counter for Ingredients
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
                    Column {
                        Text("Stock Mínimo Alert", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Avisar cuando quede menos de:", color = Color.Gray, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = {
                                val current = uiState.ingredientDetails.minStock.replace(",", ".").toDoubleOrNull() ?: 0.0
                                if (current > 0) {
                                    val step = when {
                                        uiState.ingredientDetails.unit.contains("kg", true) -> 0.05
                                        uiState.ingredientDetails.unit.contains("(L)", true) -> 0.05
                                        else -> 1.0
                                    }
                                    viewModel.updateUiState(uiState.ingredientDetails.copy(minStock = String.format(Locale.US, "%.2f", current - step)))
                                }
                            },
                            modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = BakeryOrange)
                        }
                        
                        OutlinedTextField(
                            value = uiState.ingredientDetails.minStock,
                            onValueChange = { viewModel.updateUiState(uiState.ingredientDetails.copy(minStock = it)) },
                            modifier = Modifier.width(70.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BakeryOrange,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        IconButton(
                            onClick = {
                                val current = uiState.ingredientDetails.minStock.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val step = when {
                                    uiState.ingredientDetails.unit.contains("kg", true) -> 0.05
                                    uiState.ingredientDetails.unit.contains("(L)", true) -> 0.05
                                    else -> 1.0
                                }
                                viewModel.updateUiState(uiState.ingredientDetails.copy(minStock = String.format(Locale.US, "%.2f", current + step)))
                            },
                            modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BakeryOrange)
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.saveIngredient(onSaveSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.ingredientDetails.name.isNotBlank() && uiState.ingredientDetails.costPrice.isNotBlank()
            ) {
                Text("Guardar Ingrediente", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PackagingCalculatorDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    var numPackages by remember { mutableStateOf("") }
    var unitsPerPackage by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calculadora de Empaque", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = numPackages,
                    onValueChange = { numPackages = it },
                    label = { Text("Cantidad de Bultos/Cajas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unitsPerPackage,
                    onValueChange = { unitsPerPackage = it },
                    label = { Text("Unidades/Kilos por Bulto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalPrice,
                    onValueChange = { totalPrice = it },
                    label = { Text("Precio Total Pagado ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                val nP = numPackages.toDoubleOrNull() ?: 0.0
                val uP = unitsPerPackage.toDoubleOrNull() ?: 0.0
                val totalUnits = nP * uP
                val tP = totalPrice.toDoubleOrNull() ?: 0.0
                val unitPrice = if (totalUnits > 0) tP / totalUnits else 0.0

                if (totalUnits > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Total: ${String.format(Locale.US, "%.2f", totalUnits)} unidades", fontWeight = FontWeight.Bold, color = BakeryOrange)
                            Text("Costo Unitario: ${String.format(Locale.US, "%.4f", unitPrice)} $", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nP = numPackages.toDoubleOrNull() ?: 0.0
                    val uP = unitsPerPackage.toDoubleOrNull() ?: 0.0
                    val tP = totalPrice.toDoubleOrNull() ?: 0.0
                    if (nP > 0 && uP > 0 && tP > 0) {
                        onConfirm(nP * uP, tP)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange)
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
