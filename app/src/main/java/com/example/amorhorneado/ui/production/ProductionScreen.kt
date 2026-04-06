package com.example.amorhorneado.ui.production

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
import com.example.amorhorneado.data.Customer
import com.example.amorhorneado.data.ProductionRecord
import com.example.amorhorneado.data.Recipe
import com.example.amorhorneado.data.SaleRecord
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductionScreen(
    viewModel: ProductionViewModel,
    exchangeRate: Double,
    onRecordClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val productionRecords by viewModel.productionRecords.collectAsState()
    val salesRecords by viewModel.salesRecords.collectAsState()
    val salesFilter by viewModel.salesFilter.collectAsState()
    val customers by viewModel.customers.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("En Venta", "Historial")
    
    val totalPeriodSales = salesRecords.sumOf { it.totalAmount }
    
    var sellingRecord by remember { mutableStateOf<ProductionRecordWithRecipe?>(null) }
    var selectedSaleForDetails by remember { mutableStateOf<SaleRecord?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Producción", fontWeight = FontWeight.Bold) },
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
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTabIndex == index) BakeryOrange else Color.Transparent)
                            .clickable { selectedTabIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (selectedTabIndex == 0) {
                ProductionList(
                    records = productionRecords,
                    exchangeRate = exchangeRate,
                    onSellClick = { sellingRecord = it },
                    onRecordClick = onRecordClick,
                    onConvertToPortions = { record, portions -> viewModel.convertToPortions(record, portions) }
                )
            } else {
                // Banner de Sumatoria de Ventas
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Ventas (${salesFilter})", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("$${String.format(Locale.getDefault(), "%.2f", totalPeriodSales)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            // Aquí se usa la tasa actual porque es un resumen del periodo actual/filtrado
                            PriceInBs(priceInUsd = totalPeriodSales, exchangeRate = exchangeRate)
                        }
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(40.dp))
                    }
                }

                SalesHistorySection(
                    records = salesRecords,
                    onFilterChange = { viewModel.setSalesFilter(it) },
                    onSaleClick = { selectedSaleForDetails = it },
                    onCalendarClick = { showDatePicker = true },
                    currentFilter = salesFilter
                )
            }
        }

        if (sellingRecord != null) {
            SellDialog(
                record = sellingRecord!!,
                exchangeRate = exchangeRate,
                customers = customers,
                onDismiss = { sellingRecord = null },
                onConfirm = { method, customerId, isPortion, qty ->
                    viewModel.sellFromProduction(sellingRecord!!, method, customerId, isPortion, qty)
                    sellingRecord = null
                }
            )
        }

        if (selectedSaleForDetails != null) {
            SaleDetailsDialog(
                sale = selectedSaleForDetails!!,
                onDismiss = { selectedSaleForDetails = null }
            )
        }
        
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.setCustomDateFilter(it)
                        }
                        showDatePicker = false
                    }) { Text("Filtrar") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellDialog(
    record: ProductionRecordWithRecipe,
    exchangeRate: Double,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, Boolean, Int) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var expandedCustomers by remember { mutableStateOf(false) }
    var isPortion by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }
    
    val methods = listOf("Pagomovil", "Efectivo Bs", "Efectivo $", "Crédito (Fiado)")
    val recipe = record.recipe
    val prod = record.record

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Venta", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vender ${recipe?.title}:", color = Color.Gray)
                
                if (recipe?.isPortionEnabled == true) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isPortion = false; quantity = 1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isPortion) BakeryOrange else Color.Transparent),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Completa", color = if (!isPortion) Color.White else Color.Gray, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { isPortion = true; quantity = 1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPortion) BakeryOrange else Color.Transparent),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Porción", color = if (isPortion) Color.White else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                val maxAvailable = if (isPortion) prod.portionQuantity else prod.quantity
                val currentPrice = if (isPortion) (recipe?.portionSalePrice ?: 0.0) else (recipe?.manualSalePrice ?: 0.0)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cantidad:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Stock: x$maxAvailable", fontSize = 11.sp, color = BakeryOrange)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) { Icon(Icons.Default.Remove, contentDescription = null, tint = BakeryOrange) }
                        Text(text = quantity.toString(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        IconButton(
                            onClick = { if (quantity < maxAvailable) quantity++ },
                            modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) { Icon(Icons.Default.Add, contentDescription = null, tint = BakeryOrange) }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val totalUsd = currentPrice * quantity
                    Text("$${String.format(Locale.getDefault(), "%.2f", totalUsd)}", color = Color.White, fontWeight = FontWeight.Bold)
                    PriceInBs(priceInUsd = totalUsd, exchangeRate = exchangeRate)
                }
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { 
                                selectedMethod = method
                                if (method != "Crédito (Fiado)") selectedCustomer = null
                            },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BakeryOrange,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }

                if (selectedMethod == "Crédito (Fiado)") {
                    Spacer(Modifier.height(8.dp))
                    Text("Seleccionar Cliente:", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Box {
                        OutlinedButton(
                            onClick = { expandedCustomers = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BakeryOrange)
                            Spacer(Modifier.width(8.dp))
                            Text(selectedCustomer?.name ?: "Elegir Cliente")
                        }
                        DropdownMenu(
                            expanded = expandedCustomers,
                            onDismissRequest = { expandedCustomers = false },
                            modifier = Modifier.fillMaxWidth(0.7f).background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (customers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay clientes guardados", color = Color.Gray) },
                                    onClick = { expandedCustomers = false }
                                )
                            }
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text(customer.name, color = Color.White) },
                                    onClick = {
                                        selectedCustomer = customer
                                        expandedCustomers = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedMethod.isNotEmpty() && quantity > 0) {
                val canConfirm = if (selectedMethod == "Crédito (Fiado)") selectedCustomer != null else true
                if (canConfirm) {
                    Button(
                        onClick = { onConfirm(selectedMethod, selectedCustomer?.id, isPortion, quantity) },
                        colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange)
                    ) {
                        Text(if (selectedMethod == "Crédito (Fiado)") "CRÉDITO" else "COBRAR")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SaleDetailsDialog(sale: SaleRecord, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(sale.recipeTitle, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Método de Pago: ${sale.paymentMethod}", color = BakeryOrange, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monto Total: $${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}", color = Color.White)
                }
                // SE USA LA TASA GUARDADA EN LA VENTA
                PriceInBs(priceInUsd = sale.totalAmount, exchangeRate = sale.exchangeRate)
                Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(sale.date))}", color = Color.Gray)
                Text("Tasa de cambio que se cobro: ${String.format(Locale.getDefault(), "%.2f", sale.exchangeRate)}", color = Color.Gray)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ProductionList(
    records: List<ProductionRecordWithRecipe>,
    exchangeRate: Double,
    onSellClick: (ProductionRecordWithRecipe) -> Unit,
    onRecordClick: (Int) -> Unit,
    onConvertToPortions: (ProductionRecord, Int) -> Unit
) {
    if (records.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay productos en venta.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(records) { record ->
                ProductionCard(
                    recordWithRecipe = record,
                    exchangeRate = exchangeRate,
                    onSell = { onSellClick(record) },
                    onClick = { record.recipe?.id?.let { onRecordClick(it) } },
                    onConvertToPortions = { onConvertToPortions(record.record, it) }
                )
            }
        }
    }
}

@Composable
fun ProductionCard(
    recordWithRecipe: ProductionRecordWithRecipe,
    exchangeRate: Double,
    onSell: () -> Unit,
    onClick: () -> Unit,
    onConvertToPortions: (Int) -> Unit
) {
    val recipe = recordWithRecipe.recipe
    val record = recordWithRecipe.record

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BakeryOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (recipe?.imagePath != null) {
                        AsyncImage(
                            model = recipe.imagePath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🍪", fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = recipe?.title ?: "Desconocido", fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Disponible: x${record.quantity}", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (recipe?.isPortionEnabled == true) {
                            Text(text = "| Porciones: x${record.portionQuantity}", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val price = recipe?.manualSalePrice ?: 0.0
                        Text("$${String.format(Locale.getDefault(), "%.2f", price)}", color = Color.White, fontWeight = FontWeight.Bold)
                        PriceInBs(priceInUsd = price, exchangeRate = exchangeRate)
                    }
                }

                IconButton(onClick = onSell) {
                    Icon(Icons.Default.PointOfSale, contentDescription = "Vender", tint = BakeryOrange)
                }
            }
            
            if (recipe?.isPortionEnabled == true && record.quantity > 0) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onConvertToPortions(recipe.portionsPerRecipe) },
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Fraccionar en ${recipe.portionsPerRecipe} porciones", color = BakeryOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistorySection(
    records: List<SaleRecord>,
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    onSaleClick: (SaleRecord) -> Unit,
    onCalendarClick: () -> Unit
) {
    val filters = listOf("Hoy", "Semana", "Mes", "Año")
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = currentFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BakeryOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = BakeryOrange)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { sale ->
                SaleHistoryItem(sale, onClick = { onSaleClick(sale) })
            }
        }
    }
}

@Composable
fun SaleHistoryItem(sale: SaleRecord, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(sale.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BakeryOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (sale.imagePath != null) {
                    AsyncImage(
                        model = sale.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val icon = if (sale.recipeTitle.contains("Porción", true)) Icons.Default.Restaurant else Icons.Default.Cake
                    Icon(icon, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sale.recipeTitle, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                // SE USA LA TASA GUARDADA EN LA VENTA
                PriceInBs(priceInUsd = sale.totalAmount, exchangeRate = sale.exchangeRate)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "x${sale.quantity}", color = Color.Gray)
                Text(text = "$${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}", color = BakeryOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}
