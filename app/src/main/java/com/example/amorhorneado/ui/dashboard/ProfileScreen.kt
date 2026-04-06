package com.example.amorhorneado.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: DashboardViewModel,
    exchangeRate: Double,
    onBack: () -> Unit,
    onFiadosClick: () -> Unit
) {
    val uiState by viewModel.dashboardUiState.collectAsState()
    val userConfig by viewModel.userConfig.collectAsState()
    val scrollState = rememberScrollState()
    
    var name by remember(userConfig.name) { mutableStateOf(userConfig.name) }
    var imageUri by remember(userConfig.profileImagePath) { mutableStateOf(userConfig.profileImagePath) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BakeryOrange)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = BakeryOrange)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BakeryOrange)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Negocio") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BakeryOrange
                )
            )

            // Current Exchange Rate Reference
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = BakeryOrange)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Tasa de Referencia Actual", color = Color.Gray, fontSize = 12.sp)
                        Text("$1.00 USD = ${String.format(Locale.getDefault(), "%.2f", exchangeRate)} Bs.", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // New: Fiados Access (Quick link from profile)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onFiadosClick() },
                colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BakeryOrange)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Sección de Fiados", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Cobros pendientes", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            // Real Business Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("RESUMEN DE RENDIMIENTO", fontWeight = FontWeight.Bold, color = BakeryOrange, fontSize = 14.sp)
                    
                    // Weekly
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Semanal", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inversión:", color = Color.Gray)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$${String.format(Locale.getDefault(), "%.2f", uiState.weeklyInversion)}", color = Color.White, fontWeight = FontWeight.Bold)
                                PriceInBs(priceInUsd = uiState.weeklyInversion, exchangeRate = exchangeRate)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ganancia Neta:", color = Color.Gray)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$${String.format(Locale.getDefault(), "%.2f", uiState.weeklyProfit)}", color = Color.Green, fontWeight = FontWeight.Bold)
                                PriceInBs(priceInUsd = uiState.weeklyProfit, exchangeRate = exchangeRate)
                            }
                        }
                    }

                    HorizontalDivider(color = Color.DarkGray)

                    // Monthly
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mensual", color = BakeryOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inversión:", color = Color.Gray)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$${String.format(Locale.getDefault(), "%.2f", uiState.monthlyInversion)}", color = Color.White, fontWeight = FontWeight.Bold)
                                PriceInBs(priceInUsd = uiState.monthlyInversion, exchangeRate = exchangeRate)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ganancia Neta:", color = Color.Gray)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$${String.format(Locale.getDefault(), "%.2f", uiState.monthlyProfit)}", color = Color.Green, fontWeight = FontWeight.Bold)
                                PriceInBs(priceInUsd = uiState.monthlyProfit, exchangeRate = exchangeRate)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.updateUserConfig(name, imageUri)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
