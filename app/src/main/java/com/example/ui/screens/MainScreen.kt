package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GoldErpViewModel
import com.example.ui.theme.*
import com.example.data.*
import kotlinx.coroutines.launch
import android.net.Uri
import android.widget.Toast
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(viewModel: GoldErpViewModel) {
    val itemsList by viewModel.allItems.collectAsState()
    val stockList by viewModel.allStock.collectAsState()
    val invoicesList by viewModel.allInvoices.collectAsState()
    val expensesList by viewModel.allExpenses.collectAsState()
    val amanasList by viewModel.allAmanas.collectAsState()
    val sellers by viewModel.allSellers.collectAsState()

    var activeTab by remember { mutableStateOf("dashboard") }
    var currentSellerName = remember { mutableStateOf("") }

    if (currentSellerName.value.isEmpty() && sellers.isNotEmpty()) {
        currentSellerName.value = sellers.first().name
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    
    var showExitDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val dbFile = context.getDatabasePath("gold_erp_database")
                        if (dbFile.exists()) {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                FileInputStream(dbFile).use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح ✅", Toast.LENGTH_SHORT).show()
                                activity?.finish()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                            activity?.finish()
                        }
                    }
                }
            }
        } else {
            activity?.finish()
        }
    }

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (activeTab != "dashboard") {
            activeTab = "dashboard"
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("تقفيل اليومية؟", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("عايز تقفل حسابات اليوم وتاخد نسخة احتياطية وتخرج؟ ولا تخرج من غير حفظ؟", color = LightText) },
            containerColor = DarkSurfaceElevated,
            confirmButton = {
                TextButton(onClick = { 
                    showExitDialog = false
                    val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                    exportLauncher.launch("gold_erp_backup_$dateStr.db")
                }) {
                    Text("قفل الحسابات واخرج", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showExitDialog = false
                    activity?.finish()
                }) {
                    Text("خروج فقط", color = CrimsonRed)
                }
            }
        )
    }

    
    // To match the image, we will use a bottom navigation bar with 5 main tabs
    // and use the drawer for the rest.
    val bottomTabs = listOf(
        Triple("dashboard", Icons.Filled.Home, "الرئيسية"),
        Triple("sales", Icons.Filled.ShoppingCart, "المبيعات"),
        Triple("inventory", Icons.Filled.List, "المخزون"),
        Triple("reports", Icons.Filled.DateRange, "التقارير"),
        Triple("more", Icons.Filled.Menu, "المزيد")
    )

    val allTabs = listOf(
        Triple("dashboard", "🏠", "الرئيسية"),
        Triple("sales", "💰", "المبيعات"),
        Triple("ai", "✨", "الذكاء الاصطناعي (AI)"),
        Triple("scrap", "⚖️", "شراء كسر"),
        Triple("amana", "🔍", "دفتر الجر"),
        Triple("expenses", "💸", "المصروفات"),
        Triple("inventory", "🏢", "المخزن والجرد"),
        Triple("reports", "📊", "التقارير"),
        Triple("crm", "👥", "إحصائيات العملاء"),
        Triple("audit", "📜", "سجل العمليات"),
        Triple("settings", "⚙️", "الإعدادات")
    )

    ArabicRtlLayout {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = DarkSurfaceElevated,
                    drawerContentColor = LightText,
                    modifier = Modifier.width(300.dp)
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "أقسام النظام",
                        color = GoldPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    Divider(color = BorderColor)
                    Spacer(Modifier.height(8.dp))

                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        allTabs.forEach { (tabId, emoji, label) ->
                            val isSelected = activeTab == tabId
                            NavigationDrawerItem(
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(emoji, fontSize = 20.sp)
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 16.sp
                                        )
                                    }
                                },
                                selected = isSelected,
                                onClick = {
                                    activeTab = tabId
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = GoldPrimary.copy(alpha = 0.15f),
                                    unselectedContainerColor = Color.Transparent,
                                    selectedTextColor = GoldPrimary,
                                    unselectedTextColor = LightText
                                ),
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkBg,
                        contentColor = MutedText,
                        tonalElevation = 8.dp
                    ) {
                        bottomTabs.forEach { (tabId, icon, label) ->
                            val isSelected = activeTab == tabId || (tabId == "more" && drawerState.isOpen)
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        imageVector = icon, 
                                        contentDescription = label,
                                        tint = if (isSelected) GoldPrimary else MutedText
                                    ) 
                                },
                                label = { 
                                    Text(
                                        text = label, 
                                        color = if (isSelected) GoldPrimary else MutedText,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                selected = isSelected,
                                onClick = {
                                    if (tabId == "more") {
                                        scope.launch { drawerState.open() }
                                    } else {
                                        activeTab = tabId
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = GoldPrimary,
                                    unselectedIconColor = MutedText,
                                    selectedTextColor = GoldPrimary,
                                    unselectedTextColor = MutedText
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Top control bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBg)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MutedText,
                                modifier = Modifier.clickable { activeTab = "settings" }
                            )
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MutedText
                            )
                        }

                        // Seller Selection and Title
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                val currentTitle = allTabs.find { it.first == activeTab }?.third ?: "نظام إدارة الصاغة"
                                Text(
                                    text = currentTitle,
                                    color = LightText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (sellers.isNotEmpty()) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        Text(
                                            text = currentSellerName.value,
                                            color = MutedText,
                                            fontSize = 12.sp,
                                            modifier = Modifier.clickable { expanded = true }
                                        )
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier.background(DarkSurfaceElevated)
                                        ) {
                                            sellers.forEach { s ->
                                                DropdownMenuItem(
                                                    text = { Text(s.name, color = LightText) },
                                                    onClick = {
                                                        currentSellerName.value = s.name
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Avatar Placeholder
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(GoldPrimary.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👨‍💼", fontSize = 20.sp)
                            }
                        }
                    }

                    // Main screen body
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(DarkBg)
                    ) {
                        when (activeTab) {
                            "dashboard" -> OwnerDashboardTab(viewModel, invoicesList, expensesList, amanasList) { tab -> 
                                activeTab = tab 
                            }
                            "ai" -> AiDashboardTab(viewModel)
                            "sales" -> SalesTab(viewModel, currentSellerName, itemsList, stockList)
                            "scrap" -> ScrapTab(viewModel, currentSellerName)
                            "amana" -> AmanaTab(viewModel, currentSellerName, itemsList, amanasList, stockList)
                            "expenses" -> ExpensesTab(viewModel, currentSellerName, expensesList)
                            "inventory" -> CombinedOwnerInventory(viewModel, itemsList, stockList, amanasList)
                            "reports" -> CombinedOwnerReports(viewModel, invoicesList, expensesList, sellers)
                            "crm" -> OwnerCrmTab(viewModel, invoicesList)
                            "audit" -> OwnerAuditTab(viewModel)
                            "settings" -> OwnerSettingsTab(viewModel, sellers, itemsList)
                        }
                    }
                }
            }
        }
    }
}

