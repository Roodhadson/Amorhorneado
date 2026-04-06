package com.example.amorhorneado.ui.dashboard

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.*
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
import com.example.amorhorneado.ui.theme.CriticalRed
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    exchangeRate: Double,
    onProfileClick: () -> Unit,
    onManageInventory: () -> Unit,
    onEditIngredient: (Int) -> Unit,
    onViewRecipeDetails: (Int) -> Unit,
    onManageRecipes: (Int) -> Unit,
    onUpdateExchangeRate: () -> Unit,
    onFiadosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.dashboardUiState.collectAsState()
    val userConfig by viewModel.userConfig.collectAsState()
    val weather by viewModel.weatherState.collectAsState()
    
    var showTopProductDetails by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header (Omitido para brevedad, igual que antes)
            item {
                HeaderSection(userConfig, exchangeRate, weather, onProfileClick, onUpdateExchangeRate)
            }

            // Sales Indicators
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SalesIndicatorCard(title = "Ventas Mensuales", amount = uiState.monthlySales, goal = uiState.monthlyGoal, percentage = uiState.monthlyPercentage, modifier = Modifier.weight(1f))
                    SalesIndicatorCard(title = "Ventas Semanales", amount = uiState.weeklySales, goal = uiState.weeklyGoal, percentage = uiState.weeklyPercentage, modifier = Modifier.weight(1f))
                }
            }

            // Acceso a Fiados
            item {
                BannerFiados(onFiadosClick)
            }

            // Grafica Curveada
            item {
                SalesChartSection(uiState)
            }

            // Producto Top (Actualizado con tap)
            uiState.topProduct?.let { top ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTopProductDetails = true }
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(BakeryTextGold, Color.White, BakeryTextGold)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray)) {
                                if (top.imagePath != null) {
                                    AsyncImage(model = top.imagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BakeryOrange, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BakeryTextGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PRODUCTO TOP", style = MaterialTheme.typography.labelSmall, color = BakeryTextGold, fontWeight = FontWeight.Bold)
                                }
                                Text(top.title, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                                Text("${top.totalSold} unidades vendidas", color = Color.Gray, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(String.format(Locale.getDefault(), "$%.2f", top.price), color = BakeryOrange, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BakeryTextGold)
                            }
                        }
                    }
                }
            }

            // Alertas y Inventario (Omitido para brevedad)
            item { Text(text = "Alertas y Inventario...", color = Color.Gray) }
        }
    }

    if (showTopProductDetails && uiState.topProduct != null) {
        TopProductDetailsDialog(
            topProduct = uiState.topProduct!!,
            sales = uiState.topProductSales,
            customers = uiState.customers,
            exchangeRate = exchangeRate,
            onDismiss = { showTopProductDetails = false }
        )
    }
}

@Composable
fun TopProductDetailsDialog(
    topProduct: TopProduct,
    sales: List<SaleRecord>,
    customers: List<Customer>,
    exchangeRate: Double,
    onDismiss: () -> Unit
) {
    val totalRevenue = sales.sumOf { it.totalAmount }
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ventas: ${topProduct.title}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray) }
                }

                Spacer(Modifier.height(16.dp))

                // Banner Suma Total
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Recaudación Total del Producto", color = BakeryOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$${String.format(Locale.getDefault(), "%.2f", totalRevenue)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        PriceInBs(priceInUsd = totalRevenue, exchangeRate = exchangeRate)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Historial de Compradores", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sales) { sale ->
                        val customer = customers.find { it.id == sale.customerId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer?.name ?: "Venta Directa",
                                    color = if (customer != null) Color.White else BakeryOrange,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = sdf.format(Date(sale.date)), color = Color.Gray, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (sale.quantity > 0) "x${sale.quantity}" else "Pago",
                                    color = BakeryTextGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(text = "$${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Funciones auxiliares para no perder el resto del UI (HeaderSection, SalesChartSection, etc.)
@Composable
fun HeaderSection(userConfig: com.example.amorhorneado.data.UserConfig, exchangeRate: Double, weather: WeatherInfo, onProfileClick: () -> Unit, onUpdateExchangeRate: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(1.3f).clickable { onProfileClick() }, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BakeryOrange.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                if (userConfig.profileImagePath != null) AsyncImage(model = userConfig.profileImagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text("🎂", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = userConfig.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text("Perfil", style = MaterialTheme.typography.bodySmall, color = BakeryOrange)
            }
        }
        Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🇻🇪 Tasa", fontSize = 10.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = String.format(Locale.getDefault(), "%.2f", exchangeRate), fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF2C2C), fontSize = 32.sp)
                IconButton(onClick = onUpdateExchangeRate, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) }
            }
            Text("Bs/$", fontSize = 10.sp, color = Color.Gray)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(weather.temp, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(if(weather.condition.contains("Sun", true)) "☀️" else "☁️", fontSize = 18.sp)
            }
            Text(weather.condition, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BannerFiados(onFiadosClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onFiadosClick() }, colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BakeryOrange)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Sección de Fiados", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Gestionar cuentas por cobrar", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BakeryOrange)
        }
    }
}

@Composable
fun SalesChartSection(uiState: DashboardUiState) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ventas de los Últimos 7 Días", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))
            SalesLineChart(data = uiState.dailySalesData, amounts = uiState.dailySalesAmounts, modifier = Modifier.fillMaxWidth().height(130.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                uiState.dailySalesLabels.forEach { label -> Text(text = label, color = Color.Gray, fontSize = 10.sp) }
            }
        }
    }
}
