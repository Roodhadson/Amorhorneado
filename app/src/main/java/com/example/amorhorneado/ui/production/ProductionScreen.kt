package com.example.amorhorneado.ui.production

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.amorhorneado.data.SaleRecord
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(
    viewModel: ProductionViewModel,
    onRecordClick: (Int) -> Unit
) {
    val productionRecords by viewModel.productionRecords.collectAsState(initial = emptyList())
    val salesRecords by viewModel.salesRecords.collectAsState(initial = emptyList())
    val exchangeRate by viewModel.exchangeRate.collectAsState(initial = 0.0)
    val salesFilter by viewModel.salesFilter.collectAsState()
    val totalPeriodSales by viewModel.totalPeriodSales.collectAsState(initial = 0.0)
    val customers by viewModel.customers.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var sellingRecord by remember { mutableStateOf<ProductionRecordWithRecipe?>(null) }
    var forceCartMode by remember { mutableStateOf(false) }
    
    // Cart State
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var checkoutItems by remember { mutableStateOf<List<CartItem>?>(null) }
    
    val subTotal = cartItems.sumOf { it.price * it.quantity }
    val iva = subTotal * 0.16
    val discount = 0.0
    val total = subTotal + iva

    var selectedSaleForDetails by remember { mutableStateOf<SaleRecord?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCartDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A120B))
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = "Producción",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        
        Text(
            text = "Gestiona tu inventario y ventas diarias",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2D2013))
                .padding(4.dp)
        ) {
            listOf("Stock", "Historial").forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == index) BakeryOrange else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxSize()) {
            if (selectedTab == 0) {
                Box(modifier = Modifier.weight(1f)) {
                    ProductionList(
                        records = productionRecords,
                        exchangeRate = exchangeRate,
                        onSellClick = { 
                            sellingRecord = it
                            forceCartMode = false
                        },
                        onRecordClick = onRecordClick,
                        onConvertToPortions = { record, portions -> viewModel.convertToPortions(record, portions) },
                        onAddToCart = { recordWithRecipe ->
                            val recipe = recordWithRecipe.recipe
                            if (recipe != null && !recipe.isPortionEnabled) {
                                // Agregar directo si no tiene porciones
                                val existing = cartItems.find { it.recipeId == recipe.id && !it.isPortion }
                                if (existing != null) {
                                    cartItems = cartItems.map { 
                                        if (it.recipeId == recipe.id && !it.isPortion) it.copy(quantity = it.quantity + 1) else it
                                    }
                                } else {
                                    cartItems = cartItems + CartItem(
                                        recipeId = recipe.id,
                                        title = recipe.title,
                                        price = recipe.manualSalePrice ?: 0.0,
                                        quantity = 1,
                                        isPortion = false,
                                        imagePath = recipe.imagePath
                                    )
                                }
                            } else {
                                // Mostrar diálogo si tiene porciones
                                sellingRecord = recordWithRecipe
                                forceCartMode = true
                            }
                        }
                    )
                }

                if (cartItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BakeryOrange)
                            .clickable { showCartDialog = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Color.White, contentColor = BakeryOrange) {
                                            Text(cartItems.sumOf { it.quantity }.toString())
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Ver Carrito", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", total)}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
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

        if (showCartDialog) {
            CartDialog(
                items = cartItems,
                exchangeRate = exchangeRate,
                onDismiss = { showCartDialog = false },
                onRemoveItem = { item ->
                    cartItems = cartItems.filter { it != item }
                    if (cartItems.isEmpty()) showCartDialog = false
                },
                onCheckout = { 
                    checkoutItems = cartItems
                    showCartDialog = false
                }
            )
        }

        if (sellingRecord != null) {
            SellDialog(
                record = sellingRecord!!,
                exchangeRate = exchangeRate,
                customers = customers,
                initialCartMode = forceCartMode,
                onDismiss = { 
                    sellingRecord = null
                    forceCartMode = false
                },
                onConfirm = { method, customerId, isPortion, qty ->
                    viewModel.sellFromProduction(sellingRecord!!, method, customerId, isPortion, qty)
                    sellingRecord = null
                },
                onAddToCart = { isPortion, qty ->
                    val recipe = sellingRecord?.recipe
                    if (recipe != null) {
                        val existing = cartItems.find { it.recipeId == recipe.id && it.isPortion == isPortion }
                        if (existing != null) {
                            cartItems = cartItems.map { 
                                if (it.recipeId == recipe.id && it.isPortion == isPortion) it.copy(quantity = it.quantity + qty) else it
                            }
                        } else {
                            cartItems = cartItems + CartItem(
                                recipeId = recipe.id,
                                title = recipe.title,
                                price = if (isPortion) (recipe.portionSalePrice ?: 0.0) else (recipe.manualSalePrice ?: 0.0),
                                quantity = qty,
                                isPortion = isPortion,
                                imagePath = recipe.imagePath
                            )
                        }
                    }
                    sellingRecord = null
                    forceCartMode = false
                }
            )
        }

        if (checkoutItems != null) {
            CheckoutDialog(
                items = checkoutItems!!,
                exchangeRate = exchangeRate,
                customers = customers,
                onDismiss = { checkoutItems = null },
                onConfirm = { method, customerId ->
                    viewModel.checkout(checkoutItems!!, method, customerId)
                    cartItems = emptyList()
                    checkoutItems = null
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
fun CartDialog(
    items: List<CartItem>,
    exchangeRate: Double,
    onDismiss: () -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onCheckout: () -> Unit
) {
    val subTotal = items.sumOf { it.price * it.quantity }
    val iva = subTotal * 0.16
    val total = subTotal + iva

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tu Carrito", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A120B))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BakeryOrange.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.imagePath != null) {
                                    AsyncImage(
                                        model = item.imagePath,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(if (item.isPortion) "🍰" else "🎂", fontSize = 20.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (item.isPortion) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "(Porción)",
                                            color = BakeryOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text("${item.quantity}x $${String.format(Locale.getDefault(), "%.2f", item.price)}", color = Color.Gray, fontSize = 12.sp)
                            }
                            
                            IconButton(onClick = { onRemoveItem(item) }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Remover", tint = BakeryOrange)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CartRow("Sub Total", subTotal)
                    CartRow("IVA (16%)", iva)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", total)}",
                                color = BakeryOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                            PriceInBs(priceInUsd = total, exchangeRate = exchangeRate)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PROCESAR PAGO", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF2D2013),
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellDialog(
    record: ProductionRecordWithRecipe,
    exchangeRate: Double,
    customers: List<Customer>,
    initialCartMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, Boolean, Int) -> Unit,
    onAddToCart: (Boolean, Int) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("") }
    var selectedMethods by remember { mutableStateOf(setOf<String>()) }
    var isMultipleMethods by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var expandedCustomers by remember { mutableStateOf(false) }
    var isPortion by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }
    var isAddingToCart by remember { mutableStateOf(initialCartMode) }
    
    // Al abrir el diálogo, verificar si venimos de un clic en "Carrito"
    LaunchedEffect(record) {
        // Podríamos pasar un flag, pero por ahora reseteamos
    }
    
    val methods = listOf("Pagomovil", "Efectivo Bs", "Efectivo $", "Zelle", "Binance", "Transf. Bancaria", "Crédito (Fiado)")
    val recipe = record.recipe
    val prod = record.record

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isAddingToCart) "Añadir al Carrito" else "Registrar Venta", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${if (isAddingToCart) "Agregando" else "Vendiendo"} ${recipe?.title}:", color = Color.Gray)
                
                if (recipe?.isPortionEnabled == true) {
                    Text("¿Cómo quieres vender?", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A120B)).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isPortion = false; quantity = 1 },
                            modifier = Modifier.weight(1f),
                            enabled = prod.quantity > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isPortion) BakeryOrange else Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Completa", color = if (!isPortion) Color.White else if (prod.quantity > 0) Color.Gray else Color.DarkGray, fontSize = 12.sp)
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .offset(x = (0).dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A120B))
                                .border(2.dp, BakeryOrange.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("o", color = BakeryOrange, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        
                        Button(
                            onClick = { isPortion = true; quantity = 1 },
                            modifier = Modifier.weight(1f),
                            enabled = prod.portionQuantity > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPortion) BakeryOrange else Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Porción", color = if (isPortion) Color.White else if (prod.portionQuantity > 0) Color.Gray else Color.DarkGray, fontSize = 12.sp)
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
                        Text(if (isPortion) "Cantidad de Porciones:" else "Cantidad (Completa):", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Stock disponible: x$maxAvailable", fontSize = 11.sp, color = BakeryOrange)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = maxAvailable > 0 && quantity > 1,
                            modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = if (maxAvailable > 0 && quantity > 1) 0.1f else 0.05f), RoundedCornerShape(8.dp))
                        ) { Icon(Icons.Default.Remove, contentDescription = null, tint = if (maxAvailable > 0 && quantity > 1) BakeryOrange else Color.DarkGray) }
                        
                        Text(text = quantity.toString(), color = if (maxAvailable > 0) Color.White else Color.DarkGray, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        
                        IconButton(
                            onClick = { if (quantity < maxAvailable) quantity++ },
                            enabled = maxAvailable > 0 && quantity < maxAvailable,
                            modifier = Modifier.size(32.dp).background(BakeryOrange.copy(alpha = if (maxAvailable > 0 && quantity < maxAvailable) 0.1f else 0.05f), RoundedCornerShape(8.dp))
                        ) { Icon(Icons.Default.Add, contentDescription = null, tint = if (maxAvailable > 0 && quantity < maxAvailable) BakeryOrange else Color.DarkGray) }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val totalUsd = currentPrice * quantity
                    Text(
                        "$${String.format(Locale.getDefault(), "%.2f", totalUsd)}", 
                        color = Color.White, 
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("REF:", color = Color.Gray, fontSize = 14.sp)
                        PriceInBs(priceInUsd = totalUsd, exchangeRate = exchangeRate)
                    }
                }
                
                if (!isAddingToCart) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isMultipleMethods = !isMultipleMethods },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isMultipleMethods,
                            onCheckedChange = { 
                                isMultipleMethods = it
                                if (!it) selectedMethods = emptySet()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BakeryOrange, uncheckedColor = Color.Gray)
                        )
                        Text("Múltiples métodos", color = Color.White, fontSize = 14.sp)
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        methods.forEach { method ->
                            val isSelected = if (isMultipleMethods) selectedMethods.contains(method) else selectedMethod == method
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    if (isMultipleMethods) {
                                        selectedMethods = if (selectedMethods.contains(method)) {
                                            selectedMethods - method
                                        } else {
                                            selectedMethods + method
                                        }
                                        selectedMethod = if (selectedMethods.isNotEmpty()) selectedMethods.joinToString(" + ") else ""
                                    } else {
                                        selectedMethod = method
                                    }
                                    if (method != "Crédito (Fiado)" && !isMultipleMethods) selectedCustomer = null
                                },
                                label = { Text(method) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BakeryOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1A120B),
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
                                modifier = Modifier.fillMaxWidth(0.7f).background(Color(0xFF2D2013))
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
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Este producto se añadirá a tu carrito para procesar el pago junto con otros artículos.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            if (isAddingToCart) {
                Button(
                    onClick = { onAddToCart(isPortion, quantity) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange)
                ) {
                    Text("AGREGAR AL CARRITO", fontWeight = FontWeight.Bold)
                }
            } else if (selectedMethod.isNotEmpty() && quantity > 0) {
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
            TextButton(onClick = onDismiss) { 
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF2D2013),
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
        containerColor = Color(0xFF2D2013),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ProductionList(
    records: List<ProductionRecordWithRecipe>,
    exchangeRate: Double,
    onSellClick: (ProductionRecordWithRecipe) -> Unit,
    onRecordClick: (Int) -> Unit,
    onConvertToPortions: (ProductionRecord, Int) -> Unit,
    onAddToCart: (ProductionRecordWithRecipe) -> Unit
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
                    onConvertToPortions = { onConvertToPortions(record.record, it) },
                    onAddToCart = { onAddToCart(record) }
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
    onConvertToPortions: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    val recipe = recordWithRecipe.recipe
    val record = recordWithRecipe.record

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2013)),
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
                    
                    val availableText = if (recipe?.isPortionEnabled == true) {
                        "Stock: ${record.quantity} + ${record.portionQuantity}P"
                    } else {
                        "Disponible: x${record.quantity}"
                    }
                    Text(text = availableText, color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val price = recipe?.manualSalePrice ?: 0.0
                        Text("$${String.format(Locale.getDefault(), "%.2f", price)}", color = Color.White, fontWeight = FontWeight.Bold)
                        PriceInBs(priceInUsd = price, exchangeRate = exchangeRate)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            contentDescription = "Añadir al carrito",
                            tint = BakeryOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onSell, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.PointOfSale,
                            contentDescription = "Vender",
                            tint = BakeryOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF2D2013),
                        labelColor = Color.Gray
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2013).copy(alpha = 0.5f)),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckoutDialog(
    items: List<CartItem>,
    exchangeRate: Double,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("") }
    var selectedMethods by remember { mutableStateOf(setOf<String>()) }
    var isMultipleMethods by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var expandedCustomers by remember { mutableStateOf(false) }
    
    val methods = listOf("Pagomovil", "Efectivo Bs", "Efectivo $", "Zelle", "Binance", "Transf. Bancaria", "Crédito (Fiado)")
    val subTotal = items.sumOf { it.price * it.quantity }
    val iva = subTotal * 0.16
    val total = subTotal + iva

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar Venta del Carrito", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${items.size} productos en el carrito", color = Color.Gray)
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A120B))
                        .padding(8.dp)
                ) {
                    LazyColumn {
                        items(items) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.quantity}x ${item.title}${if(item.isPortion) " (P)" else ""}", color = Color.White, fontSize = 12.sp)
                                Text("$${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total: $${String.format(Locale.getDefault(), "%.2f", total)}", color = Color.White, fontWeight = FontWeight.Bold)
                    PriceInBs(priceInUsd = total, exchangeRate = exchangeRate)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isMultipleMethods = !isMultipleMethods },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isMultipleMethods,
                        onCheckedChange = { 
                            isMultipleMethods = it
                            if (!it) selectedMethods = emptySet()
                        },
                        colors = CheckboxDefaults.colors(checkedColor = BakeryOrange, uncheckedColor = Color.Gray)
                    )
                    Text("Múltiples métodos", color = Color.White, fontSize = 14.sp)
                }
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEach { method ->
                        val isSelected = if (isMultipleMethods) selectedMethods.contains(method) else selectedMethod == method
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                if (isMultipleMethods) {
                                    selectedMethods = if (selectedMethods.contains(method)) {
                                        selectedMethods - method
                                    } else {
                                        selectedMethods + method
                                    }
                                    selectedMethod = if (selectedMethods.isNotEmpty()) selectedMethods.joinToString(" + ") else ""
                                } else {
                                    selectedMethod = method
                                }
                                if (method != "Crédito (Fiado)" && !isMultipleMethods) selectedCustomer = null
                            },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BakeryOrange,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1A120B),
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
                            modifier = Modifier.fillMaxWidth(0.7f).background(Color(0xFF2D2013))
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
            if (selectedMethod.isNotEmpty()) {
                val canConfirm = if (selectedMethod == "Crédito (Fiado)") selectedCustomer != null else true
                if (canConfirm) {
                    Button(
                        onClick = { onConfirm(selectedMethod, selectedCustomer?.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange)
                    ) {
                        Text("COBRAR TODO")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = Color(0xFF2D2013),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CartRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
