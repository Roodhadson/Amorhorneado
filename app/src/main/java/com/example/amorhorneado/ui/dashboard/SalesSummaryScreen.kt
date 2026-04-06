package com.example.amorhorneado.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.amorhorneado.data.Customer
import com.example.amorhorneado.data.SaleRecord
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import com.example.amorhorneado.ui.theme.BakeryTextGold
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesSummaryScreen(
    viewModel: SalesSummaryViewModel,
    isMonthly: Boolean,
    exchangeRate: Double,
    onBack: () -> Unit,
    onViewRecipeDetails: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSale by remember { mutableStateOf<SaleRecord?>(null) }
    var showTopProductDetails by remember { mutableStateOf(false) }

    LaunchedEffect(isMonthly) {
        viewModel.setPeriod(isMonthly)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Chart Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isMonthly) "Ventas del Mes Actual" else "Ventas de los Últimos 7 Días",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        SalesLineChart(
                            data = uiState.chartData,
                            amounts = uiState.chartAmounts,
                            exchangeRates = uiState.chartRates,
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            uiState.chartLabels.forEach { label ->
                                Text(text = label, color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // Top Product of the Period
            uiState.topProduct?.let { top ->
                item {
                    Text(
                        text = "Producto más vendido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTopProductDetails = true }
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(BakeryTextGold.copy(alpha = 0.1f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray)) {
                                if (top.imagePath != null) {
                                    AsyncImage(model = top.imagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BakeryOrange, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BakeryTextGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LÍDER EN VENTAS", style = MaterialTheme.typography.labelSmall, color = BakeryTextGold, fontWeight = FontWeight.Bold)
                                }
                                Text(top.title, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                                Text("${top.totalSold} unidades en este periodo", color = Color.Gray, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", top.price)}",
                                    color = BakeryOrange,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BakeryTextGold)
                                        .clickable { 
                                            onViewRecipeDetails(top.id) 
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "DETALLES", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Listado de Ventas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(uiState.salesList) { sale ->
                val customer = uiState.customers.find { it.id == sale.customerId }
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSale = sale },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sale.recipeTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = customer?.name ?: "Venta Directa",
                                color = if (customer != null) BakeryOrange else Color.Gray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = sdf.format(Date(sale.date)),
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "x${sale.quantity}",
                                color = BakeryTextGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "$${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            PriceInBs(priceInUsd = sale.totalAmount, exchangeRate = sale.exchangeRate)
                        }
                    }
                }
            }
        }
    }

    if (showTopProductDetails && uiState.topProduct != null) {
        TopProductDetailsDialog(
            topProduct = uiState.topProduct!!,
            sales = uiState.topProductSales,
            customers = uiState.customers,
            onDismiss = { showTopProductDetails = false },
            onViewRecipeDetails = onViewRecipeDetails
        )
    }

    selectedSale?.let { sale ->
        SaleDetailDialog(
            sale = sale,
            customer = uiState.customers.find { it.id == sale.customerId },
            onDismiss = { selectedSale = null }
        )
    }
}

@Composable
fun SaleDetailDialog(
    sale: SaleRecord,
    customer: Customer?,
    onDismiss: () -> Unit
) {
    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    val timeF = SimpleDateFormat("HH:mm", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detalle de Venta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray) }
                }
                
                Spacer(Modifier.height(20.dp))

                DetailRow("Producto", sale.recipeTitle, Color.White)
                DetailRow("Cliente", customer?.name ?: "Venta Directa", if (customer != null) BakeryOrange else Color.Gray)
                DetailRow("Cantidad", "x${sale.quantity}", BakeryTextGold)
                DetailRow("Fecha", sdf.format(Date(sale.date)).replaceFirstChar { it.uppercase() }, Color.LightGray)
                DetailRow("Hora", timeF.format(Date(sale.date)), Color.LightGray)
                DetailRow("Método", sale.paymentMethod, Color.White)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL USD", color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL BS", color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${String.format(Locale.getDefault(), "%.2f", sale.totalAmountBs)} Bs", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text(
                    text = "Tasa aplicada: ${String.format(Locale.getDefault(), "%.2f", sale.exchangeRate)} Bs/$",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CERRAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
