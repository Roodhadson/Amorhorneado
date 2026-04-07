package com.example.amorhorneado.ui.production

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amorhorneado.data.ProductionCost
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import com.example.amorhorneado.ui.theme.CriticalRed
import java.util.Locale

@Composable
fun ProductionCostScreen(
    viewModel: ProductionCostViewModel,
    exchangeRate: Double,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isFormVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isFormVisible = !isFormVisible },
                containerColor = BakeryOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    if (isFormVisible) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
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
                    text = "Costos",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 42.sp
                )
                Text(
                    text = "${uiState.costList.size} conceptos registrados",
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
                placeholder = { Text("Buscar conceptos...", color = Color.Gray) },
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

            AnimatedVisibility(
                visible = isFormVisible || viewModel.costUiState.costDetails.id != 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column {
                    CostInputForm(
                        costDetails = viewModel.costUiState.costDetails,
                        onValueChange = viewModel::updateUiState,
                        onSaveClick = {
                            viewModel.saveCost()
                            isFormVisible = false
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                thickness = 0.5.dp,
                color = Color.DarkGray
            )

            CostList(
                costList = uiState.costList,
                exchangeRate = exchangeRate,
                onEdit = { viewModel.loadCost(it) },
                onDelete = { viewModel.deleteCost(it) }
            )
        }
    }
}

@Composable
fun CostInputForm(
    costDetails: CostDetails,
    onValueChange: (CostDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf("Gas", "Mano de obra", "Electricidad", "Agua", "Alquiler", "Transporte")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = BakeryOrange)
                    Text(
                        text = if (costDetails.id == 0) "Nuevo Gasto Operativo" else "Editar Gasto Operativo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                if (costDetails.id != 0) {
                    IconButton(onClick = { onValueChange(CostDetails()) }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.Gray)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = costDetails.concept,
                    onValueChange = { onValueChange(costDetails.copy(concept = it)) },
                    label = { Text("Concepto") },
                    placeholder = { Text("Ej. Gas, Mano de obra, Electricidad") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = BakeryOrange) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BakeryOrange
                    )
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionChip(
                            label = suggestion,
                            onClick = { onValueChange(costDetails.copy(concept = suggestion)) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = costDetails.amount,
                onValueChange = { onValueChange(costDetails.copy(amount = it)) },
                label = { Text("Monto en Dólares ($)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = BakeryOrange) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BakeryOrange
                )
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                enabled = costDetails.concept.isNotBlank() && costDetails.amount.isNotBlank()
            ) {
                Icon(if (costDetails.id == 0) Icons.Filled.Add else Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (costDetails.id == 0) "Guardar Concepto" else "Actualizar Concepto")
            }
        }
    }
}

@Composable
fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = BakeryOrange.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BakeryOrange.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = BakeryOrange,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CostList(
    costList: List<ProductionCost>, 
    exchangeRate: Double, 
    onEdit: (ProductionCost) -> Unit,
    onDelete: (ProductionCost) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(costList) { cost ->
            CostItem(
                cost = cost, 
                exchangeRate = exchangeRate,
                onEdit = { onEdit(cost) },
                onDelete = { onDelete(cost) }
            )
        }
    }
}

@Composable
fun CostItem(
    cost: ProductionCost, 
    exchangeRate: Double, 
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when {
        cost.concept.contains("gas", ignoreCase = true) -> Icons.Default.LocalGasStation
        cost.concept.contains("obra", ignoreCase = true) || cost.concept.contains("mano", ignoreCase = true) -> Icons.Default.Engineering
        cost.concept.contains("luz", ignoreCase = true) || cost.concept.contains("elec", ignoreCase = true) -> Icons.Default.Bolt
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = BakeryOrange)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = cost.concept, fontWeight = FontWeight.Bold, color = Color.White)
                        PriceInBs(priceInUsd = cost.amount, exchangeRate = exchangeRate)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", cost.amount)}",
                        color = BakeryOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = Color.DarkGray
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = CriticalRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", fontSize = 12.sp)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange.copy(alpha = 0.15f), contentColor = BakeryOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Modificar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
