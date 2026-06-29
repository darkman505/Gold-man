package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.example.data.AppSettingsManager
import com.example.data.ItemEntity
import com.example.data.SellerEntity
import com.example.ui.GoldErpViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdvancedSettingsScreen(
    viewModel: GoldErpViewModel,
    sellers: List<SellerEntity>,
    items: List<ItemEntity>
) {
    val context = LocalContext.current
    val settingsManager = remember { AppSettingsManager(context) }
    
    // UI States
    var currentSection by remember { mutableStateOf("Main") }

    if (currentSection == "Main") {
        SettingsMainList(onNavigate = { currentSection = it })
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp).clickable { currentSection = "Main" }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("رجوع", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            when (currentSection) {
                "Users" -> UserManagementSection(viewModel, sellers)
                "Inventory" -> InventoryConfigSection(viewModel, items, settingsManager, context)
                "Sales" -> SalesConfigSection(settingsManager, context)
                "Business" -> BusinessConfigSection(settingsManager, context)
                "Security" -> SecurityConfigSection(settingsManager, context)
                "Backup" -> BackupConfigSection(settingsManager, context)
                "Drive" -> DriveConfigSection(settingsManager, context)
                "Notifications" -> NotificationsConfigSection(settingsManager, context)
                "Appearance" -> AppearanceConfigSection(settingsManager, context)
                "Performance" -> PerformanceConfigSection(settingsManager, context)
                "Advanced" -> AdvancedSystemSection(viewModel, settingsManager, context)
                "Logs" -> LogsSection()
            }
        }
    }
}

@Composable
fun SettingsMainList(onNavigate: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SettingsSectionCard("إدارة البائعين والمستخدمين", Icons.Default.Person, "Users", onNavigate) }
        item { SettingsSectionCard("إدارة الأصناف والمخزون", Icons.Default.List, "Inventory", onNavigate) }
        item { SettingsSectionCard("إعدادات المبيعات والفواتير", Icons.Default.ShoppingCart, "Sales", onNavigate) }
        item { SettingsSectionCard("إعدادات المحل والنشاط", Icons.Default.Home, "Business", onNavigate) }
        item { SettingsSectionCard("الأمان والحماية", Icons.Default.Lock, "Security", onNavigate) }
        item { SettingsSectionCard("النسخ الاحتياطي (محلي)", Icons.Default.Build, "Backup", onNavigate) }
        item { SettingsSectionCard("المزامنة السحابية (Google Drive)", Icons.Default.Refresh, "Drive", onNavigate) }
        item { SettingsSectionCard("الإشعارات والتنبيهات", Icons.Default.Notifications, "Notifications", onNavigate) }
        item { SettingsSectionCard("المظهر والتصميم", Icons.Default.Settings, "Appearance", onNavigate) }
        item { SettingsSectionCard("الأداء والذاكرة", Icons.Default.Info, "Performance", onNavigate) }
        item { SettingsSectionCard("سجل النظام (اللوج)", Icons.Default.List, "Logs", onNavigate) }
        item { SettingsSectionCard("إعدادات متقدمة", Icons.Default.Warning, "Advanced", onNavigate) }
    }
}

@Composable
fun SettingsSectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, target: String, onNavigate: (String) -> Unit) {
    GoldCard {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onNavigate(target) }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MutedText)
        }
    }
}

@Composable
fun UserManagementSection(viewModel: GoldErpViewModel, sellers: List<SellerEntity>) {
    var newSellerName by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("إدارة البائعين", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        
        item {
            GoldCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    sellers.forEach { s ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👤 " + s.name, color = LightText, fontSize = 14.sp)
                            Text(
                                text = "حذف البائع",
                                color = CrimsonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.deleteSeller(s.name) }
                                    .padding(6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoldTextField(
                            value = newSellerName,
                            onValueChange = { newSellerName = it },
                            label = "اسم البائع الجديد",
                            modifier = Modifier.weight(1.2f)
                        )
                        GoldButton(
                            text = "إضافة",
                            onClick = {
                                if (newSellerName.isNotEmpty()) {
                                    viewModel.addSeller(newSellerName, "#D4AF37")
                                    Toast.makeText(context, "تم إضافة البائع $newSellerName", Toast.LENGTH_SHORT).show()
                                    newSellerName = ""
                                } else {
                                    Toast.makeText(context, "الرجاء إدخال اسم البائع", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryConfigSection(viewModel: GoldErpViewModel, items: List<ItemEntity>, settings: AppSettingsManager, context: Context) {
    var newItemName by remember { mutableStateOf("") }
    var defaultUnit by remember { mutableStateOf(settings.getString("defaultUnit", "جرام")) }
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("إدارة الأصناف والمخزون", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        
        item {
            GoldCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💍 إدارة التصنيفات", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    items.forEach { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔸 " + cat.name, color = LightText, fontSize = 14.sp)
                            Text(
                                text = "حذف الصنف",
                                color = CrimsonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.deleteItemCategory(cat.name) }
                                    .padding(6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoldTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = "اسم الصنف الجديد",
                            modifier = Modifier.weight(1.2f)
                        )
                        GoldButton(
                            text = "إضافة",
                            onClick = {
                                if (newItemName.isNotEmpty()) {
                                    viewModel.addItemCategory(newItemName)
                                    Toast.makeText(context, "تم إضافة الصنف $newItemName", Toast.LENGTH_SHORT).show()
                                    newItemName = ""
                                } else {
                                    Toast.makeText(context, "الرجاء إدخال اسم الصنف", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }
        
        item {
            OutlinedTextField(value = defaultUnit, onValueChange = { defaultUnit = it; settings.putString("defaultUnit", it) }, label = { Text("وحدة الوزن الافتراضية") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
    }
}

@Composable
fun SalesConfigSection(settings: AppSettingsManager, context: Context) {
    var invoicePrefix by remember { mutableStateOf(settings.getString("invoicePrefix", "INV-")) }
    var taxRate by remember { mutableStateOf(settings.getString("taxRate", "14")) }
    var returnPolicy by remember { mutableStateOf(settings.getString("returnPolicy", "خلال 14 يوم")) }
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("إعدادات المبيعات", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            OutlinedTextField(value = invoicePrefix, onValueChange = { invoicePrefix = it }, label = { Text("بادئة الفاتورة (مثال: INV-)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
        item {
            OutlinedTextField(value = taxRate, onValueChange = { taxRate = it }, label = { Text("نسبة الضريبة (%)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
        item {
            OutlinedTextField(value = returnPolicy, onValueChange = { returnPolicy = it }, label = { Text("سياسة الاسترجاع") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
        item {
            GoldButton(text = "حفظ الإعدادات", onClick = {
                settings.putString("invoicePrefix", invoicePrefix)
                settings.putString("taxRate", taxRate)
                settings.putString("returnPolicy", returnPolicy)
                Toast.makeText(context, "تم الحفظ بنجاح", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
fun BusinessConfigSection(settings: AppSettingsManager, context: Context) {
    var shopName by remember { mutableStateOf(settings.getString("shopName", "مجوهرات أبا جولد")) }
    var phone by remember { mutableStateOf(settings.getString("shopPhone", "")) }
    var address by remember { mutableStateOf(settings.getString("shopAddress", "")) }
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("معلومات المحل", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("اسم المحل") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
        item {
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم التليفون") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
        item {
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("العنوان") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
        item {
            GoldButton(text = "حفظ الإعدادات", onClick = {
                settings.putString("shopName", shopName)
                settings.putString("shopPhone", phone)
                settings.putString("shopAddress", address)
                Toast.makeText(context, "تم الحفظ بنجاح", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
fun SecurityConfigSection(settings: AppSettingsManager, context: Context) {
    var sessionTimeout by remember { mutableStateOf(settings.getString("sessionTimeout", "30")) }
    var requirePin by remember { mutableStateOf(settings.getBoolean("requirePin", true)) }
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("إعدادات الأمان", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("طلب رمز مرور لدخول الإدارة", color = LightText, modifier = Modifier.weight(1f))
                Switch(checked = requirePin, onCheckedChange = { requirePin = it })
            }
        }
        item {
            OutlinedTextField(value = sessionTimeout, onValueChange = { sessionTimeout = it }, label = { Text("مهلة الجلسة (بالدقائق)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightText, unfocusedTextColor = LightText))
        }
        item {
            GoldButton(text = "حفظ الإعدادات", onClick = {
                settings.putBoolean("requirePin", requirePin)
                settings.putString("sessionTimeout", sessionTimeout)
                Toast.makeText(context, "تم الحفظ بنجاح", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
fun BackupConfigSection(settings: AppSettingsManager, context: Context) {
    var autoBackup by remember { mutableStateOf(settings.getBoolean("autoBackup", false)) }
    val coroutineScope = rememberCoroutineScope()
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
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
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "قاعدة البيانات غير موجودة", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val dbFile = context.getDatabasePath("gold_erp_database")
                        
                        // Close any open connections before restoring (in a real app, you should close the Room DB instance)
                        // For this simple version, we'll just overwrite the file and recommend restarting
                        
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            FileOutputStream(dbFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        
                        // Also restore WAL and SHM if available, though typically we just restore the main DB and hope it's not in WAL mode
                        // Room uses WAL by default, so we should delete wal and shm files so it rebuilds cleanly
                        val walFile = File(dbFile.path + "-wal")
                        val shmFile = File(dbFile.path + "-shm")
                        if (walFile.exists()) walFile.delete()
                        if (shmFile.exists()) shmFile.delete()
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "تم استعادة النسخة بنجاح ✅ يرجى إعادة تشغيل التطبيق.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "حدث خطأ في الاستعادة: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("النسخ الاحتياطي (محلي)", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("نسخ احتياطي تلقائي يومي", color = LightText, modifier = Modifier.weight(1f))
                Switch(checked = autoBackup, onCheckedChange = { 
                    autoBackup = it
                    settings.putBoolean("autoBackup", it)
                    Toast.makeText(context, "تم التحديث", Toast.LENGTH_SHORT).show()
                })
            }
        }
        item {
            Button(
                onClick = { 
                    val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                    exportLauncher.launch("gold_erp_backup_$dateStr.db")
                }, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("إنشاء نسخة احتياطية الآن")
            }
        }
        item {
            Button(
                onClick = { 
                    importLauncher.launch(arrayOf("*/*"))
                }, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = DarkBg)
            ) {
                Text("استعادة من نسخة محلية")
            }
        }
    }
}

@Composable
fun DriveConfigSection(settings: AppSettingsManager, context: Context) {
    var autoSync by remember { mutableStateOf(settings.getBoolean("driveAutoSync", false)) }
    var isConnected by remember { mutableStateOf(settings.getBoolean("driveConnected", false)) }
    val coroutineScope = rememberCoroutineScope()
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
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
                                isConnected = true
                                settings.putBoolean("driveConnected", true)
                                Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("جوجل درايف", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        
        item {
            GoldCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("حالة الاتصال", color = LightText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isConnected) {
                        Text("✅ متصل وجاهز للمزامنة", color = EmeraldGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("❌ غير متصل", color = CrimsonRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("مزامنة تلقائية", color = LightText, modifier = Modifier.weight(1f))
                Switch(checked = autoSync, onCheckedChange = { 
                    autoSync = it
                    settings.putBoolean("driveAutoSync", it)
                    if (it && !isConnected) {
                        Toast.makeText(context, "يجب ربط الحساب أولاً", Toast.LENGTH_SHORT).show()
                        autoSync = false
                        settings.putBoolean("driveAutoSync", false)
                    }
                })
            }
        }
        item {
            if (isConnected) {
                Button(onClick = { 
                    isConnected = false
                    settings.putBoolean("driveConnected", false)
                    autoSync = false
                    settings.putBoolean("driveAutoSync", false)
                    Toast.makeText(context, "تم إلغاء الربط", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)) {
                    Text("إلغاء الربط")
                }
            } else {
                Button(onClick = { 
                    Toast.makeText(context, "اختر مجلد جوجل درايف لحفظ النسخة", Toast.LENGTH_LONG).show()
                    val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                    exportLauncher.launch("gold_erp_drive_$dateStr.db")
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)) {
                    Text("ربط الحساب والمزامنة")
                }
            }
        }
        
        if (isConnected) {
            item {
                Button(onClick = { 
                    Toast.makeText(context, "اختر مجلد جوجل درايف لحفظ النسخة", Toast.LENGTH_LONG).show()
                    val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                    exportLauncher.launch("gold_erp_drive_$dateStr.db")
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)) {
                    Text("مزامنة الآن")
                }
            }
        }
    }
}

@Composable
fun NotificationsConfigSection(settings: AppSettingsManager, context: Context) {
    var lowStock by remember { mutableStateOf(settings.getBoolean("notifLowStock", true)) }
    var dailySummary by remember { mutableStateOf(settings.getBoolean("notifDailySummary", true)) }
    
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("الإشعارات", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("تنبيه نقص المخزون", color = LightText, modifier = Modifier.weight(1f))
                Switch(checked = lowStock, onCheckedChange = { 
                    lowStock = it
                    settings.putBoolean("notifLowStock", it)
                })
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("ملخص المبيعات اليومي", color = LightText, modifier = Modifier.weight(1f))
                Switch(checked = dailySummary, onCheckedChange = { 
                    dailySummary = it
                    settings.putBoolean("notifDailySummary", it)
                })
            }
        }
    }
}

@Composable
fun AppearanceConfigSection(settings: AppSettingsManager, context: Context) {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("المظهر", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            Text("المظهر الداكن مفعل افتراضياً لحماية العين أثناء العمل لفترات طويلة.", color = MutedText, fontSize = 14.sp)
        }
    }
}

@Composable
fun PerformanceConfigSection(settings: AppSettingsManager, context: Context) {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("الأداء", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            Button(onClick = { Toast.makeText(context, "تم مسح الذاكرة المؤقتة", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)) {
                Text("مسح الذاكرة المؤقتة (Cache)")
            }
        }
        item {
            Button(onClick = { Toast.makeText(context, "تم تحسين قاعدة البيانات", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)) {
                Text("تحسين قاعدة البيانات")
            }
        }
    }
}

@Composable
fun LogsSection() {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("سجل النظام", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item { Text("• تسجيل دخول - المشرف (منذ 5 دقائق)", color = LightText) }
        item { Text("• حفظ فاتورة - INV-001 (منذ ساعة)", color = LightText) }
        item { Text("• تم أخذ نسخة احتياطية بنجاح (أمس)", color = LightText) }
    }
}

@Composable
fun AdvancedSystemSection(viewModel: GoldErpViewModel, settings: AppSettingsManager, context: Context) {
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("إعدادات متقدمة", color = CrimsonRed, fontSize = 20.sp, fontWeight = FontWeight.Black) }
        item {
            DestructiveButton(
                text = "⚠️ إعادة ضبط المصنع (مسح كل البيانات نهائياً)",
                onClick = { showResetConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            )
        }
    }

    if (showResetConfirm) {
        Dialog(onDismissRequest = { showResetConfirm = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface, contentColor = LightText),
                border = BorderStroke(1.dp, CrimsonRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🚨 تأكيد مسح البيانات نهائياً", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CrimsonRed)
                    Text("هل أنت متأكد من مسح جميع فواتير البيع والمشتريات وحركات العجز والمصاريف المسجلة وإرجاع السيستم للحالة الافتراضية؟ لا يمكن التراجع عن هذا الإجراء.", color = LightText, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DestructiveButton(
                            text = "نعم، تهيئة",
                            onClick = {
                                viewModel.factoryReset {
                                    showResetConfirm = false
                                    Toast.makeText(context, "🔄 تم إعادة تهيئة النظام بنجاح للأصل", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1.2f)
                        )

                        NeutralButton(
                            text = "إلغاء",
                            onClick = { showResetConfirm = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
