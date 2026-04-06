package com.example.amorhorneado.ui.debts

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.amorhorneado.data.Customer
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Rank Gradients
val GoldGradient = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFDAA520), Color(0xFFFFD700)))
val SilverGradient = Brush.linearGradient(listOf(Color(0xFFC0C0C0), Color(0xFFA9A9A9), Color(0xFFC0C0C0)))
val BronzeGradient = Brush.linearGradient(listOf(Color(0xFFCD7F32), Color(0xFF8B4513), Color(0xFFCD7F32)))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: DebtViewModel,
    exchangeRate: Double,
    onBack: () -> Unit
) {
    val uiState by viewModel.debtsUiState.collectAsState()
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Cuentas", "Clientes", "Top")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Clientes y Cuentas", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = BakeryOrange)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = Color.White
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = BakeryOrange,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddCustomerDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 32.dp) // Sube un poco más el botón
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Añadir Cliente")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                0 -> DebtsTabsSection(uiState, exchangeRate, viewModel)
                1 -> CustomersList(uiState.customers, viewModel)
                2 -> TopCustomers(uiState, exchangeRate)
            }
        }
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            customers = uiState.customers,
            onDismiss = { showAddDebtDialog = false },
            onConfirm = { customerId, amount, concept ->
                viewModel.addDebt(customerId, amount, concept)
                showAddDebtDialog = false
            }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, phone, address ->
                viewModel.addCustomer(name, phone, address)
                showAddCustomerDialog = false
            }
        )
    }
}

@Composable
fun DebtsTabsSection(uiState: DebtsUiState, exchangeRate: Double, viewModel: DebtViewModel) {
    var subTabSelected by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Pendientes", "Abonos", "Pagados")
    val totalDebt = uiState.debts.filter { !it.debt.isPaid }.sumOf { it.debt.remainingAmount }
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Total Debt Banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Deuda Pendiente", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("$${String.format(Locale.getDefault(), "%.2f", totalDebt)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    PriceInBs(priceInUsd = totalDebt, exchangeRate = exchangeRate)
                }
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(48.dp))
            }
        }

        // Search Field for Debts
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar deudor...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BakeryOrange,
                unfocusedBorderColor = Color.Gray,
                cursorColor = BakeryOrange
            ),
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            subTabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (subTabSelected == index) BakeryOrange else Color.Transparent)
                        .clickable { subTabSelected = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (subTabSelected == index) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (subTabSelected) {
            0 -> PendingDebtsGroupedList(uiState, exchangeRate, viewModel, searchQuery)
            1 -> PartialPaymentsList(uiState, exchangeRate, viewModel)
            2 -> PaidDebtsList(uiState, exchangeRate, viewModel)
        }
    }
}

@Composable
fun PendingDebtsGroupedList(uiState: DebtsUiState, exchangeRate: Double, viewModel: DebtViewModel, searchQuery: String = "") {
    val pendingDebts = uiState.debts.filter { !it.debt.isPaid }
    val groupedDebts = pendingDebts.groupBy { it.debt.customerId }
    
    var selectedCustomerDebts by remember { mutableStateOf<List<DebtDetails>?>(null) }

    val filteredCustomerIds = groupedDebts.keys.toList().filter { customerId ->
        val customerName = groupedDebts[customerId]?.firstOrNull()?.customerName ?: ""
        customerName.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (filteredCustomerIds.isNotEmpty()) {
            items(filteredCustomerIds) { customerId ->
                val customerDebts = groupedDebts[customerId] ?: emptyList()
                val customerName = customerDebts.firstOrNull()?.customerName ?: "Desconocido"
                val totalRemaining = customerDebts.sumOf { it.debt.remainingAmount }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCustomerDebts = customerDebts },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BakeryOrange.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BakeryOrange)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(customerName, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$${String.format(Locale.getDefault(), "%.2f", totalRemaining)}",
                                color = BakeryOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                            PriceInBs(priceInUsd = totalRemaining, exchangeRate = exchangeRate)
                        }
                    }
                }
            }
        }
    }

    if (selectedCustomerDebts != null) {
        CustomerDebtsDialog(
            customerName = selectedCustomerDebts!!.first().customerName,
            debts = selectedCustomerDebts!!,
            exchangeRate = exchangeRate,
            onDismiss = { selectedCustomerDebts = null },
            onPayment = { debtId, amount -> viewModel.addPayment(debtId, amount) }
        )
    }
}

@Composable
fun CustomerDebtsDialog(
    customerName: String,
    debts: List<DebtDetails>,
    exchangeRate: Double,
    onDismiss: () -> Unit,
    onPayment: (Int, Double) -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf<DebtDetails?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Deudas de $customerName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(debts) { debtDetails ->
                        val debt = debtDetails.debt
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(debt.concept, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
                                        Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(debt.date)), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "$${String.format(Locale.getDefault(), "%.2f", debt.remainingAmount)}",
                                            fontWeight = FontWeight.Bold,
                                            color = BakeryOrange
                                        )
                                        Text(
                                            "Original: $${String.format(Locale.getDefault(), "%.2f", debt.amount)}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                LinearProgressIndicator(
                                    progress = { (debt.amount - debt.remainingAmount).toFloat() / debt.amount.toFloat() },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF00E5FF),
                                    trackColor = Color.DarkGray
                                )

                                Button(
                                    onClick = { showPaymentDialog = debtDetails },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Pagar / Abonar", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Text("Cerrar", color = BakeryOrange)
                }
            }
        }
    }

    if (showPaymentDialog != null) {
        AddPaymentDialog(
            debt = showPaymentDialog!!,
            onDismiss = { showPaymentDialog = null },
            onConfirm = { amount ->
                onPayment(showPaymentDialog!!.debt.id, amount)
                showPaymentDialog = null
            }
        )
    }
}

@Composable
fun PartialPaymentsList(uiState: DebtsUiState, exchangeRate: Double, viewModel: DebtViewModel) {
    val partialPayments = uiState.payments.sortedByDescending { it.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (partialPayments.isNotEmpty()) {
            items(partialPayments) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(payment.customerName, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(payment.date)), fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "+ $${String.format(Locale.getDefault(), "%.2f", payment.amount)}",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4CAF50)
                            )
                            PriceInBs(priceInUsd = payment.amount, exchangeRate = exchangeRate)
                        }
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text("No hay abonos registrados", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PaidDebtsList(uiState: DebtsUiState, exchangeRate: Double, viewModel: DebtViewModel) {
    val paidDebts = uiState.debts.filter { it.debt.isPaid }.sortedByDescending { it.debt.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (paidDebts.isNotEmpty()) {
            items(paidDebts) { debtDetails ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Sello "PAGADO" diagonal
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .rotate(-15f)
                                .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "PAGADO",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(debtDetails.customerName, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(debtDetails.debt.concept, color = Color.Gray, fontSize = 14.sp)
                                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(debtDetails.debt.date)), fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$${String.format(Locale.getDefault(), "%.2f", debtDetails.debt.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                PriceInBs(priceInUsd = debtDetails.debt.amount, exchangeRate = exchangeRate)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text("No hay deudas pagadas", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun CustomersList(customers: List<Customer>, viewModel: DebtViewModel) {
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }.sortedBy { it.name }

    // Logic for "Recently Added" vs "All Customers"
    val currentTime = System.currentTimeMillis()
    val sixHoursInMillis = 6 * 60 * 60 * 1000L

    val recentlyAdded = customers
        .filter { currentTime - it.createdAt < sixHoursInMillis }
        .sortedByDescending { it.createdAt }
        .take(3)
    
    val allCustomers = filteredCustomers

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar cliente...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BakeryOrange,
                unfocusedBorderColor = Color.Gray,
                cursorColor = BakeryOrange
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (recentlyAdded.isNotEmpty() && searchQuery.isEmpty()) {
                item {
                    Text(
                        "Agregados recientemente",
                        color = BakeryOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(recentlyAdded) { customer ->
                    CustomerCard(
                        customer = customer,
                        onEdit = { customerToEdit = it },
                        timeAgo = getTimeAgo(customer.createdAt)
                    )
                }
                item { 
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))
                }
            }

            item {
                Text(
                    "Lista de clientes",
                    color = BakeryOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(allCustomers) { customer ->
                CustomerCard(customer, onEdit = { customerToEdit = it })
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (customerToEdit != null) {
        EditCustomerDialog(
            customer = customerToEdit!!,
            onDismiss = { customerToEdit = null },
            onConfirm = { name, phone, address ->
                viewModel.updateCustomer(customerToEdit!!.copy(name = name, phone = phone, address = address))
                customerToEdit = null
            }
        )
    }
}

@Composable
fun CustomerCard(customer: Customer, onEdit: (Customer) -> Unit, timeAgo: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(BakeryOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(customer.name.take(1).uppercase(), color = BakeryOrange, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(customer.name, fontWeight = FontWeight.Bold, color = Color.White)
                    if (timeAgo != null) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = BakeryOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = timeAgo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = BakeryOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (customer.phone.isNotBlank()) Text(customer.phone, fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = { onEdit(customer) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
            }
        }
    }
}

fun getTimeAgo(time: Long): String {
    val diff = System.currentTimeMillis() - time
    val hours = diff / (1000 * 60 * 60)
    val minutes = diff / (1000 * 60)
    
    return when {
        hours >= 1 -> "Agregado hace $hours h"
        minutes >= 1 -> "Agregado hace $minutes min"
        else -> "Recién agregado"
    }
}

@Composable
fun TopCustomers(uiState: DebtsUiState, exchangeRate: Double) {
    val topCustomers = remember(uiState) {
        uiState.customers.map { customer ->
            val totalPaid = uiState.sales
                .filter { it.customerId == customer.id }
                .sumOf { it.totalAmount }
            customer to totalPaid
        }.sortedByDescending { it.second }.filter { it.second > 0 }.take(10)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text("Ranking de Clientes", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("Basado en el total de compras acumuladas", color = Color.Gray)
            }
        }

        if (topCustomers.isNotEmpty()) {
            items(topCustomers.size) { index ->
                val (customer, total) = topCustomers[index]
                val rank = index + 1
                
                val (gradient, emoji) = when(rank) {
                    1 -> GoldGradient to "👑"
                    2 -> SilverGradient to "🥈"
                    3 -> BronzeGradient to "🥉"
                    else -> Brush.linearGradient(listOf(Color(0xFF3E2723), Color(0xFF1A120B))) to null
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = if (rank <= 3) BorderStroke(1.5.dp, gradient) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (rank <= 3) Color.White.copy(alpha = 0.08f) else Color.DarkGray.copy(alpha = 0.2f))
                                .border(1.dp, if (rank <= 3) gradient else Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (emoji != null) {
                                Text(emoji, fontSize = 32.sp) // Emojis 3D más grandes
                            } else {
                                Text("#$rank", color = Color.Gray, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(customer.name, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                            Text("Ventas Totales", color = Color.Gray, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$${String.format(Locale.getDefault(), "%.2f", total)}",
                                fontWeight = FontWeight.Black,
                                color = if (rank == 1) Color(0xFFFFD700) else Color.White,
                                fontSize = 20.sp
                            )
                            PriceInBs(priceInUsd = total, exchangeRate = exchangeRate)
                        }
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Aún no hay datos de ventas", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun AddDebtDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var amount by remember { mutableStateOf("") }
    var concept by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nueva Deuda", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                // Customer Selector
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCustomer?.name ?: "Seleccionar Cliente")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.name) },
                                onClick = {
                                    selectedCustomer = customer
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (selectedCustomer != null && amt != null) {
                                onConfirm(selectedCustomer!!.id, amt, concept)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                        enabled = selectedCustomer != null && amount.toDoubleOrNull() != null
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nuevo Cliente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, phone, address) },
                        colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun EditCustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var address by remember { mutableStateOf(customer.address) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Editar Cliente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, phone, address) },
                        colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    debt: DebtDetails,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Registrar Pago / Abono", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Cliente: ${debt.customerName}", color = Color.Gray)
                Text("Deuda pendiente: $${String.format(Locale.getDefault(), "%.2f", debt.debt.remainingAmount)}", fontWeight = FontWeight.Bold, color = BakeryOrange)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto del Pago ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onConfirm(amt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                        enabled = amount.toDoubleOrNull() != null && amount.toDouble().let { it > 0 && it <= debt.debt.remainingAmount }
                    ) {
                        Text("Registrar")
                    }
                }
            }
        }
    }
}
