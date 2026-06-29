package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.GoldErpViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun SellerScreen(
    viewModel: GoldErpViewModel,
    onNavigateBack: () -> Unit
) {
    var activeTab by remember { mutableStateOf("sales") } // sales, scrap, amana, expenses, summary
    
    val context = LocalContext.current
    val sellers by viewModel.allSellers.collectAsState()
    val itemsList by viewModel.allItems.collectAsState()
    val stockList by viewModel.allStock.collectAsState()
    val expensesList by viewModel.allExpenses.collectAsState()
    val amanasList by viewModel.allAmanas.collectAsState()
    val invoicesList by viewModel.allInvoices.collectAsState()

    val currentSellerName = remember(sellers) {
        mutableStateOf(sellers.firstOrNull()?.name ?: "")
    }

    if (currentSellerName.value.isEmpty() && sellers.isNotEmpty()) {
        currentSellerName.value = sellers.first().name
    }

    var showAiDialog by remember { mutableStateOf(false) }

    ArabicRtlLayout {
        Scaffold(
            containerColor = DarkBg,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAiDialog = true },
                    containerColor = GoldPrimary,
                    contentColor = DarkBg
                ) {
                    Text("🤖", fontSize = 24.sp)
                }
            },
            bottomBar = {
                // Bottom navigation tab row
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Triple("summary", "🏠", "الرئيسية"),
                        Triple("sales", "💰", "المبيعات"),
                        Triple("scrap", "📦", "الكسر"),
                        Triple("amana", "👥", "العملاء"),
                        Triple("expenses", "💸", "المصروفات")
                    )
                    tabs.forEach { (tabId, emoji, label) ->
                        val isSelected = activeTab == tabId
                        val scale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isSelected) 1.2f else 1.0f,
                            label = "nav_scale"
                        )
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tabId },
                            icon = { Text(text = emoji, fontSize = 24.sp, modifier = Modifier.scale(scale)) },
                            label = { Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldPrimary,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldSecondary.copy(alpha = 0.4f),
                                unselectedIconColor = MutedText,
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
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "💎 ABA-Gold",
                            color = GoldPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "— شاشة البيع",
                            color = LightText.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }

                    GoldOutlinedButton(
                        text = "← الرئيسية",
                        onClick = onNavigateBack,
                        modifier = Modifier.width(110.dp)
                    )
                }

                // Main screen body per active tab
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(DarkBg)
                ) {
                    when (activeTab) {
                        "sales" -> SalesTab(viewModel, currentSellerName, itemsList, stockList)
                        "scrap" -> ScrapTab(viewModel, currentSellerName)
                        "amana" -> AmanaTab(viewModel, currentSellerName, itemsList, amanasList, stockList)
                        "expenses" -> ExpensesTab(viewModel, currentSellerName, expensesList)
                        "summary" -> SummaryTab(viewModel, currentSellerName, invoicesList, expensesList)
                    }
                }
            }
        }
    }

    if (showAiDialog) {
        AiAssistantDialog(onDismiss = { showAiDialog = false })
    }
}

// --- TAB 1: NEW SALES INVOICE ---
data class InvoiceRowState(
    val id: Long = System.nanoTime(),
    var itemType: String,
    var karat: String,
    var qtyString: String,
    var weightString: String
) {
    val qty: Int get() = qtyString.toIntOrNull() ?: 0
    val weight: Double get() = weightString.toDoubleOrNull() ?: 0.0
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SalesTab(
    viewModel: GoldErpViewModel,
    selectedSeller: MutableState<String>,
    itemsList: List<ItemEntity>,
    stockList: List<StockEntity>
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    
    // Rows of items in current invoice
    val rows = remember { mutableStateListOf<InvoiceRowState>() }

    // Aggregate weight feature
    var aggregateActive by remember { mutableStateOf(false) }
    var aggrW21 by remember { mutableStateOf("") }
    var aggrW18 by remember { mutableStateOf("") }
    var aggrW24 by remember { mutableStateOf("") }

    // Workmanship pricing mode
    var workmanshipMode by remember { mutableStateOf("unified") } // unified, separated
    var workmanshipRate by remember { mutableStateOf("150") }
    var finalTotalPriceOverride by remember { mutableStateOf("") }

    var discount by remember { mutableStateOf("") }

    // Payments
    var cashPaid by remember { mutableStateOf("") }
    var instapayPaid by remember { mutableStateOf("") }
    var vodafonePaid by remember { mutableStateOf("") }
    var visaPaid by remember { mutableStateOf("") }

    // Old Gold payout (trade-in credit)
    var oldGoldActive by remember { mutableStateOf(false) }
    var oldGoldKarat by remember { mutableStateOf("21") }
    var oldGoldWeight by remember { mutableStateOf("") }
    var oldGoldPrice by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }
    var bypassMode by remember { mutableStateOf(false) }
    var bypassClickCount by remember { mutableStateOf(0) }

    var showInvoicePreview by remember { mutableStateOf(false) }
    var customGoldPrice21 by remember { mutableStateOf("") }
    var customGoldPrice18 by remember { mutableStateOf("") }
    var customGoldPrice24 by remember { mutableStateOf("") }

    // Synchronize aggregate weights when items list or aggregate inputs change
    fun distributeAggregateWeights() {
        if (!aggregateActive) return
        val w21 = aggrW21.toDoubleOrNull() ?: 0.0
        val w18 = aggrW18.toDoubleOrNull() ?: 0.0
        val w24 = aggrW24.toDoubleOrNull() ?: 0.0

        var q21 = 0
        var q18 = 0
        var q24 = 0

        rows.forEach { r ->
            when (r.karat) {
                "21" -> q21 += r.qty
                "18" -> q18 += r.qty
                "24" -> q24 += r.qty
            }
        }

        rows.forEachIndexed { idx, r ->
            val updatedWeight = when (r.karat) {
                "21" -> if (q21 > 0) (w21 / q21) * r.qty else 0.0
                "18" -> if (q18 > 0) (w18 / q18) * r.qty else 0.0
                "24" -> if (q24 > 0) (w24 / q24) * r.qty else 0.0
                else -> 0.0
            }
            if (updatedWeight > 0) {
                val weightStr = String.format(Locale.US, "%.2f", updatedWeight)
                rows[idx] = r.copy(weightString = weightStr)
            }
        }
    }

    // Calculations
    val baseGoldPrice21 = viewModel.goldPrice21.value
    val goldPrice21 = customGoldPrice21.toDoubleOrNull() ?: baseGoldPrice21
    val goldPrice18 = customGoldPrice18.toDoubleOrNull() ?: (goldPrice21 * (18.0 / 21.0))
    val goldPrice24 = customGoldPrice24.toDoubleOrNull() ?: (goldPrice21 * (24.0 / 21.0))

    var totalWeight = 0.0
    var totalQty = 0
    var rawGoldValue = 0.0

    rows.forEach { item ->
        totalWeight += item.weight
        totalQty += item.qty
        val kVal = item.karat.toDoubleOrNull() ?: 21.0
        val appliedPrice = when (item.karat) {
            "18" -> goldPrice18
            "24" -> goldPrice24
            else -> goldPrice21
        }
        // Since we explicitly computed the prices per karat, the formula is just weight * appliedPrice
        // (Wait, appliedPrice is per gram of that karat. So rawGoldValue = sum of weight * appliedPrice)
        rawGoldValue += item.weight * appliedPrice
    }

    // Old gold value
    val ogW = oldGoldWeight.toDoubleOrNull() ?: 0.0
    val ogP = oldGoldPrice.toDoubleOrNull() ?: goldPrice21
    val ogK = oldGoldKarat.toDoubleOrNull() ?: 21.0
    val oldGoldValue = if (oldGoldActive) ogW * (ogP / 21.0) * ogK else 0.0

    // Final calculations
    val calculatedInvoiceTotal = if (workmanshipMode == "unified") {
        val wRate = workmanshipRate.toDoubleOrNull() ?: 0.0
        Math.round(rawGoldValue + (wRate * totalWeight)).toDouble()
    } else {
        finalTotalPriceOverride.toDoubleOrNull() ?: 0.0
    }

    // Effective reverse workmanship
    val calculatedWorkmanshipRate = if (workmanshipMode == "separated" && totalWeight > 0) {
        val finalPrice = finalTotalPriceOverride.toDoubleOrNull() ?: 0.0
        (finalPrice - rawGoldValue) / totalWeight
    } else {
        workmanshipRate.toDoubleOrNull() ?: 0.0
    }

    val discountVal = discount.toDoubleOrNull() ?: 0.0
    val finalRequiredTotal = maxOf(0.0, calculatedInvoiceTotal - discountVal)
    val totalPaymentsCollected = (cashPaid.toDoubleOrNull() ?: 0.0) +
            (instapayPaid.toDoubleOrNull() ?: 0.0) +
            (vodafonePaid.toDoubleOrNull() ?: 0.0) +
            (visaPaid.toDoubleOrNull() ?: 0.0) +
            oldGoldValue

    val paymentBalance = finalRequiredTotal - totalPaymentsCollected

    // Validations
    val validationErrors = mutableListOf<String>()
    val validationWarnings = mutableListOf<String>()

    if (totalWeight > 0) {
        if (calculatedWorkmanshipRate < 0) {
            validationErrors.add("مصنعية سالبة: سعر البيع أقل من تكلفة الذهب الخام.")
        } else if (calculatedWorkmanshipRate > 5000) {
            validationErrors.add("المصنعية تتجاوز 5000 ج للجرام. تأكد من الأرقام أو فعل التخطي.")
        } else if (calculatedWorkmanshipRate > 2000 && !bypassMode) {
            validationErrors.add("مصنعية غير معتادة (>2000 ج). يلزم تفعيل وضع التخطي.")
        } else if (calculatedWorkmanshipRate > 1000) {
            validationWarnings.add("المصنعية المدخلة أعلى من المعدل الطبيعي.")
        }
    }

    val totalWorkmanshipValue = calculatedWorkmanshipRate * totalWeight
    if (discountVal > calculatedInvoiceTotal * 0.4) {
        validationErrors.add("خصم يتجاوز 40% من الفاتورة غير مسموح.")
    } else if (discountVal > calculatedInvoiceTotal * 0.2 && !bypassMode) {
        validationErrors.add("خصم يتجاوز 20%. يلزم تفعيل وضع التخطي.")
    } else if (discountVal > totalWorkmanshipValue && totalWorkmanshipValue > 0) {
        validationWarnings.add("الخصم يتجاوز قيمة المصنعية (تآكل ربحية الخام).")
    }

    var rowWeightError = false
    rows.forEach { r ->
        if (r.weight > 1000 && !bypassMode) {
            validationErrors.add("وزن صنف (${r.itemType}) > 1 كجم. يلزم التخطي.")
        } else if (r.weight > 500) {
            validationWarnings.add("وزن صنف (${r.itemType}) يتجاوز المعتاد (>500 جم).")
        }
        if (r.weight <= 0) {
            rowWeightError = true
        }
        if (r.qty <= 0) {
            validationErrors.add("عدد الصنف (${r.itemType}) غير صحيح.")
        }
    }
    if (rowWeightError) {
        validationErrors.add("يجب إدخال أوزان صحيحة للأصناف المدرجة.")
    }

    if (paymentBalance < -1.0 && !bypassMode) {
        validationErrors.add("المدفوع أكبر من قيمة الفاتورة. يلزم تفعيل التخطي من المدير للاستلام بالزيادة.")
    } else if (paymentBalance > 1.0) {
        if (customerName.isBlank()) {
            validationErrors.add("يوجد عجز نقدي / ذمة على العميل. يجب تسجيل اسم العميل أولاً.")
        }
        if (!bypassMode) {
            validationErrors.add("الدفع غير مكتمل (عجز). يلزم تفعيل خيار التخطي لفتح رصيد آجل للعميل.")
        }
    }

    if (oldGoldActive) {
        if (ogW <= 0) validationErrors.add("وزن الكسر غير صحيح.")
        if (ogP <= 0) validationErrors.add("سعر الكسر غير صحيح.")
        if (ogW > 100 && !bypassMode) validationErrors.add("شراء كسر كبير (>100 جرام). يلزم موافقة الإدارة والتخطي.")
    }

    val canSave = validationErrors.isEmpty() && rows.isNotEmpty()

    // Trigger aggregate distribution on input or row updates
    LaunchedEffect(aggregateActive, aggrW21, aggrW18, aggrW24, rows.size) {
        distributeAggregateWeights()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Combined Basic Info Card (Gold Price, Seller, Customer)
        item {
            GoldCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("عيار ٢٤", color = MutedText, fontSize = 11.sp)
                            GoldTextField(
                                value = customGoldPrice24,
                                onValueChange = { customGoldPrice24 = it },
                                label = "",
                                placeholder = Math.round(goldPrice24).toString(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("عيار ٢١ (الأساس)", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            GoldTextField(
                                value = customGoldPrice21,
                                onValueChange = { customGoldPrice21 = it },
                                label = "",
                                placeholder = Math.round(baseGoldPrice21).toString(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("عيار ١٨", color = MutedText, fontSize = 11.sp)
                            GoldTextField(
                                value = customGoldPrice18,
                                onValueChange = { customGoldPrice18 = it },
                                label = "",
                                placeholder = Math.round(goldPrice18).toString(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoldTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = "اسم الزبون (اختياري)",
                            modifier = Modifier.weight(1.2f)
                        )
                        GoldTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = "رقم الهاتف",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Invoice Items Builder
        item {
            GoldCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("أصناف الفاتورة", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = aggregateActive,
                                onCheckedChange = { aggregateActive = it },
                                colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                            )
                            Text("وزن مجمع", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick item pills row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsList.forEach { category ->
                            Box(
                                modifier = Modifier
                                    .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        rows.add(
                                            InvoiceRowState(
                                                itemType = category.name,
                                                karat = if (category.name == "سبائك" || category.name == "سبيكة") "24" else "21",
                                                qtyString = "1",
                                                weightString = ""
                                            )
                                        )
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                val em = when (category.name) {
                                    "خاتم" -> "💍"
                                    "سلسلة" -> "📿"
                                    "غويشة" -> "✨"
                                    "دبلة" -> "💫"
                                    "حلق" -> "👂"
                                    "سبيكة" -> "🧱"
                                    "سبائك" -> "🧱"
                                    "جنيه" -> "🪙"
                                    else -> "📦"
                                }
                                Text("$em ${category.name}", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Aggregate Weights Input Box
                    if (aggregateActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GoldSecondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, GoldSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وزن مجمع:", color = AmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                            GoldTextField(
                                value = aggrW21,
                                onValueChange = { aggrW21 = it },
                                label = "ع ٢١",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            GoldTextField(
                                value = aggrW18,
                                onValueChange = { aggrW18 = it },
                                label = "ع ١٨",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            GoldTextField(
                                value = aggrW24,
                                onValueChange = { aggrW24 = it },
                                label = "ع ٢٤",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Active Items Table Rows
                    if (rows.isEmpty()) {
                        EmptyState(
                            icon = "🛒",
                            title = "الفاتورة فارغة",
                            description = "لم يتم إضافة أي أصناف بعد. اضغط على التصنيفات بالأعلى للإضافة.",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            rows.forEachIndexed { index, r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val stockItem = stockList.find { it.key == "${r.itemType}-${r.karat}" }
                                    val currentStock = (stockItem?.vitrineQty ?: 0) + (stockItem?.vaultQty ?: 0)

                                    // Item category static label & Stock
                                    Column(modifier = Modifier.weight(0.9f)) {
                                        Text(
                                            text = r.itemType,
                                            color = LightText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "بالمحل: $currentStock",
                                            color = if (currentStock > 0) EmeraldGreen else CrimsonRed,
                                            fontSize = 10.sp
                                        )
                                    }

                                    // Qty
                                    GoldTextField(
                                        value = r.qtyString,
                                        onValueChange = {
                                            rows[index] = r.copy(qtyString = it)
                                        },
                                        label = "عدد",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(60.dp)
                                    )

                                    // Weight
                                    GoldTextField(
                                        value = r.weightString,
                                        onValueChange = {
                                            rows[index] = r.copy(weightString = it)
                                        },
                                        label = "وزن",
                                        readOnly = aggregateActive,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.2f)
                                    )

                                    // Karat Select
                                    var showKaratMenu by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.width(48.dp)) {
                                        Text(
                                            text = "${r.karat}ع",
                                            color = GoldPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .clickable { showKaratMenu = true }
                                                .padding(6.dp)
                                                .fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                        DropdownMenu(
                                            expanded = showKaratMenu,
                                            onDismissRequest = { showKaratMenu = false },
                                            modifier = Modifier.background(DarkSurface)
                                        ) {
                                            val karatOptions = if (r.itemType == "سبائك" || r.itemType == "سبيكة") listOf("24") else listOf("21", "18")
                                            karatOptions.forEach { k ->
                                                DropdownMenuItem(
                                                    text = { Text(k, color = LightText) },
                                                    onClick = {
                                                        rows[index] = r.copy(karat = k)
                                                        showKaratMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Delete icon
                                    IconButton(
                                        onClick = { rows.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("✕", color = CrimsonRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Workmanship, Discount & Trade-In Card (Combined)
        item {
            GoldCard {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("الأسعار والخصومات", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // Mode Toggle Tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (workmanshipMode == "unified") GoldPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { workmanshipMode = "unified" }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("مصنعية موحدة", color = if (workmanshipMode == "unified") DarkBg else LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (workmanshipMode == "separated") GoldPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { workmanshipMode = "separated" }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("تسعير إجمالي", color = if (workmanshipMode == "separated") DarkBg else LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (workmanshipMode == "unified") {
                            GoldTextField(
                                value = workmanshipRate,
                                onValueChange = { workmanshipRate = it },
                                label = "المصنعية للجرام (ج)",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            GoldTextField(
                                value = finalTotalPriceOverride,
                                onValueChange = { finalTotalPriceOverride = it },
                                label = "إجمالي مطلوب (قبل الخصم)",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        GoldTextField(
                            value = discount,
                            onValueChange = { discount = it },
                            label = "خصم إضافي (ج)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Financial Breakdown Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("قيمة الذهب الخام:", color = MutedText, fontSize = 13.sp)
                            Text("${Math.round(rawGoldValue)} ج", color = LightText, fontSize = 14.sp, fontWeight = FontWeight.Bold, style = TextStyle(fontFeatureSettings = "tnum"))
                        }
                        val wTotal = calculatedInvoiceTotal - rawGoldValue
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي المصنعية (${Math.round(calculatedWorkmanshipRate)} ج/ج):", color = MutedText, fontSize = 13.sp)
                            Text("${Math.round(wTotal)} ج", color = if (calculatedWorkmanshipRate < 0) CrimsonRed else LightText, fontSize = 14.sp, fontWeight = FontWeight.Bold, style = TextStyle(fontFeatureSettings = "tnum"))
                        }
                        if ((discount.toDoubleOrNull() ?: 0.0) > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الخصم المطبق:", color = MutedText, fontSize = 13.sp)
                                Text("-${discount} ج", color = CrimsonRed, fontSize = 14.sp, fontWeight = FontWeight.Bold, style = TextStyle(fontFeatureSettings = "tnum"))
                            }
                        }
                        Divider(color = BorderColor.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإجمالي النهائي المطلوب:", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            Text("${Math.round(finalRequiredTotal)} ج", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black, style = TextStyle(fontFeatureSettings = "tnum"))
                        }
                    }

                    if (calculatedWorkmanshipRate > 500.0) {
                        Text(
                            text = "⚠️ تنبيه: متوسط مصنعية مرتفع جداً.",
                            color = OrangeWarning,
                            fontSize = 11.sp
                        )
                    } else if (calculatedWorkmanshipRate < 0.0) {
                        Text(
                            text = "❌ خطأ: مصنعية سالبة (بيع بخسارة للخام). يلزم تخطي.",
                            color = CrimsonRed,
                            fontSize = 11.sp
                        )
                    }
                    
                    Divider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))
                    
                    // Scrap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("شراء كسر (خصم من الفاتورة)", color = if (oldGoldActive) GoldPrimary else MutedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = oldGoldActive,
                            onCheckedChange = { oldGoldActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary)
                        )
                    }

                    if (oldGoldActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Karat Select Menu
                            var showKMenu by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(0.8f)) {
                                OutlinedTextField(
                                    value = "ع $oldGoldKarat",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("العيار", color = GoldPrimary, fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth().clickable { showKMenu = true },
                                    textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                    trailingIcon = { Text("▼", color = GoldPrimary, fontSize = 10.sp) },
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                        disabledTextColor = LightText
                                    )
                                )
                                DropdownMenu(
                                    expanded = showKMenu,
                                    onDismissRequest = { showKMenu = false },
                                    modifier = Modifier.background(DarkSurface)
                                ) {
                                    listOf("21", "18", "24").forEach { k ->
                                        DropdownMenuItem(
                                            text = { Text(k, color = LightText) },
                                            onClick = {
                                                oldGoldKarat = k
                                                showKMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            GoldTextField(
                                value = oldGoldWeight,
                                onValueChange = { oldGoldWeight = it },
                                label = "الوزن",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            
                            GoldTextField(
                                value = oldGoldPrice,
                                onValueChange = { oldGoldPrice = it },
                                label = "سعر ٢١ للكسر",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                placeholder = Math.round(goldPrice21).toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Payments Card
        item {
            GoldCard(borderColor = GoldPrimary.copy(alpha = 0.3f)) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("الدفع والمتبقي", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "المطلوب: ${Math.round(finalRequiredTotal)} ج",
                            color = AmberAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoldTextField(
                            value = cashPaid,
                            onValueChange = { cashPaid = it },
                            label = "كاش 💵",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        GoldTextField(
                            value = instapayPaid,
                            onValueChange = { instapayPaid = it },
                            label = "إنستا 📱",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        GoldTextField(
                            value = vodafonePaid,
                            onValueChange = { vodafonePaid = it },
                            label = "فودافون 📱",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        GoldTextField(
                            value = visaPaid,
                            onValueChange = { visaPaid = it },
                            label = "فيزا 💳",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Balance Box
                    val balanceClassColor = when {
                        paymentBalance > 0.1 -> CrimsonRed
                        paymentBalance < -0.1 -> EmeraldGreen
                        else -> SapphireBlue
                    }
                    val balanceText = when {
                        paymentBalance > 0.1 -> "متبقي عجز على الزبون"
                        paymentBalance < -0.1 -> "مرتجع فكة للزبون"
                        else -> "الحساب خالص تماماً"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(balanceClassColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, balanceClassColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(balanceText, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${Math.abs(Math.round(paymentBalance))} ج",
                            color = balanceClassColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }



        // Notes and Secret Bypass Section
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text(
                    text = "ملاحظات الفاتورة",
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            bypassClickCount++
                            if (bypassClickCount >= 3) {
                                bypassMode = !bypassMode
                                bypassClickCount = 0
                                Toast.makeText(context, if (bypassMode) "🔓 تم تفعيل وضع التخطي الاستثنائي" else "🔒 تم إغلاق وضع التخطي", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(bottom = 6.dp)
                )

                BasicTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    textStyle = TextStyle(color = LightText, fontSize = 13.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    decorationBox = @Composable { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (notes.isEmpty()) {
                                Text("أضف أي ملاحظات إضافية هنا...", color = MutedText.copy(alpha = 0.5f), fontSize = 13.sp)
                            }
                            innerTextField()
                        }
                    }
                )

                if (bypassMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonRed.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .border(1.dp, CrimsonRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تخطي حماية خسارة الذهب الخام والمصنعية",
                            color = CrimsonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = bypassMode,
                            onCheckedChange = { bypassMode = it },
                            colors = CheckboxDefaults.colors(checkedColor = CrimsonRed),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }
        }

        if (validationErrors.isNotEmpty() || validationWarnings.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                        .border(1.dp, if (validationErrors.isNotEmpty()) CrimsonRed else OrangeWarning, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("نتائج المراجعة المالية والرقابة:", color = if (validationErrors.isNotEmpty()) CrimsonRed else OrangeWarning, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    validationErrors.forEach { err ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Text("❌ ", fontSize = 11.sp)
                            Text(err, color = CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    validationWarnings.forEach { warn ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Text("⚠️ ", fontSize = 11.sp)
                            Text(warn, color = OrangeWarning, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Actions Button
        item {
            PrimaryButton(
                text = "💾 معاينة وحفظ الفاتورة",
                onClick = {
                    // Validations
                    if (rows.isEmpty()) {
                        Toast.makeText(context, "❌ الرجاء إضافة صنف واحد على الأقل", Toast.LENGTH_LONG).show()
                        return@PrimaryButton
                    }
                    if (!canSave) {
                        Toast.makeText(context, "❌ راجع أخطاء الرقابة المالية قبل الحفظ", Toast.LENGTH_LONG).show()
                        return@PrimaryButton
                    }
                    if (finalRequiredTotal <= 0.0) {
                        Toast.makeText(context, "❌ إجمالي الفاتورة غير صحيح", Toast.LENGTH_LONG).show()
                        return@PrimaryButton
                    }

                    // Check stock
                    var hasEnoughStock = true
                    rows.forEach { r ->
                        val matchedStock = stockList.find { it.key == "${r.itemType}-${r.karat}" }
                        if (matchedStock == null || matchedStock.vitrineQty < r.qty) {
                            hasEnoughStock = false
                        }
                    }

                    if (!hasEnoughStock) {
                        Toast.makeText(context, "❌ عذراً، بعض الكميات المطلوبة غير متوفرة في الفاترينة حالياً لجردها", Toast.LENGTH_LONG).show()
                        return@PrimaryButton
                    }

                    showInvoicePreview = true
                }
            )
        }
    }

    // Invoice Preview Dialog
    if (showInvoicePreview) {
        Dialog(onDismissRequest = { showInvoicePreview = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface, contentColor = LightText),
                border = BorderStroke(1.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "🧾 معاينة الفاتورة للتأكيد",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Divider(color = BorderColor)

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("الزبون:", color = MutedText, fontSize = 11.sp)
                                Text(if (customerName.isEmpty()) "عميل طيار" else customerName, color = LightText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (customerPhone.isNotEmpty()) {
                                    Text(customerPhone, color = MutedText, fontSize = 12.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("البائع المالك للعهد:", color = MutedText, fontSize = 11.sp)
                                Text(selectedSeller.value, color = LightText, fontWeight = FontWeight.Bold)
                                Text(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()), color = MutedText, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("الأصناف والعيارات المشترات:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        rows.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${r.itemType} (عيار ${r.karat})", color = LightText, fontWeight = FontWeight.Bold)
                                Text("${r.qty} قطع  /  ${r.weight} جرام", color = GoldPrimary, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("تفاصيل الدفع المحصلة:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if ((cashPaid.toDoubleOrNull() ?: 0.0) > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("كاش في الدرج 💵", color = LightText)
                                    Text("${cashPaid} جنيه", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            if ((instapayPaid.toDoubleOrNull() ?: 0.0) > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("تحويل إنستا باي 📱", color = LightText)
                                    Text("${instapayPaid} جنيه", color = SapphireBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                            if ((vodafonePaid.toDoubleOrNull() ?: 0.0) > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("فودافون كاش 📱", color = LightText)
                                    Text("${vodafonePaid} جنيه", color = CrimsonRed, fontWeight = FontWeight.Bold)
                                }
                            }
                            if ((visaPaid.toDoubleOrNull() ?: 0.0) > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("فيزا وبنك 💳", color = LightText)
                                    Text("${visaPaid} جنيه", color = GoldSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (oldGoldActive && oldGoldValue > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("مستلم بذهب كسر ⚖️", color = LightText)
                                    Text("${Math.round(oldGoldValue)} جنيه", color = AmberAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(color = BorderColor)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الإجمالي النهائي المطلوب:", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${Math.round(finalRequiredTotal)} جنيه", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryButton(
                            text = "حفظ الفاتورة ✔️",
                            onClick = {
                                // Save invoice
                                val invoiceItems = rows.map { InvoiceItem(it.itemType, it.karat, it.qty, it.weight) }
                                viewModel.addInvoice(
                                    seller = selectedSeller.value,
                                    customer = customerName,
                                    phone = customerPhone,
                                    items = invoiceItems,
                                    tw = totalWeight,
                                    tq = totalQty,
                                    tot = finalRequiredTotal,
                                    fare = calculatedWorkmanshipRate,
                                    p21 = goldPrice21,
                                    cash = cashPaid.toDoubleOrNull() ?: 0.0,
                                    instapay = instapayPaid.toDoubleOrNull() ?: 0.0,
                                    vodafone = vodafonePaid.toDoubleOrNull() ?: 0.0,
                                    visa = visaPaid.toDoubleOrNull() ?: 0.0,
                                    ewallet = (instapayPaid.toDoubleOrNull() ?: 0.0) + (vodafonePaid.toDoubleOrNull() ?: 0.0) + (visaPaid.toDoubleOrNull() ?: 0.0),
                                    notes = notes,
                                    hasOldGold = oldGoldActive,
                                    ogKarat = oldGoldKarat.toIntOrNull() ?: 21,
                                    ogWeight = ogW,
                                    ogPrice = ogP,
                                    ogValue = oldGoldValue
                                ) { success ->
                                    if (success) {
                                        Toast.makeText(context, "🎉 تم تسجيل وحفظ الفاتورة وتحديث الفاترينة بنجاح", Toast.LENGTH_LONG).show()
                                        // Reset fields
                                        customerName = ""
                                        customerPhone = ""
                                        rows.clear()
                                        workmanshipRate = "150"
                                        finalTotalPriceOverride = ""
                                        discount = ""
                                        cashPaid = ""
                                        instapayPaid = ""
                                        vodafonePaid = ""
                                        visaPaid = ""
                                        oldGoldActive = false
                                        customGoldPrice21 = ""
                                        oldGoldWeight = ""
                                        notes = ""
                                        showInvoicePreview = false
                                    } else {
                                        Toast.makeText(context, "❌ حدث خطأ أثناء الحفظ على الخادم", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.2f)
                        )

                        NeutralButton(
                            text = "تعديل",
                            onClick = { showInvoicePreview = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 2: SCRAP PURCHASE ---
@Composable
fun ScrapTab(
    viewModel: GoldErpViewModel,
    selectedSeller: MutableState<String>
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var karat by remember { mutableStateOf("21") }
    var weightInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }

    val goldPrice21 = viewModel.goldPrice21.value

    val weight = weightInput.toDoubleOrNull() ?: 0.0
    val p21 = priceInput.toDoubleOrNull() ?: goldPrice21
    val kVal = karat.toDoubleOrNull() ?: 21.0
    val calculatedValue = weight * (p21 / 21.0) * kVal
    
    var bypassMode by remember { mutableStateOf(false) }
    var bypassClickCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrimsonRed.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .border(1.dp, CrimsonRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "⚖️ شراء كسر مباشر من عميل (سيتم صرف الفلوس من الدرج كاش)",
                color = CrimsonRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable {
                    bypassClickCount++
                    if (bypassClickCount >= 3) {
                        bypassMode = !bypassMode
                        bypassClickCount = 0
                        Toast.makeText(context, if (bypassMode) "🔓 وضع التخطي مفعل" else "🔒 تم إغلاق وضع التخطي", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        if (bypassMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrimsonRed.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .border(1.dp, CrimsonRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("وضع التخطي للقيود المالية مفعل", color = CrimsonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Checkbox(
                    checked = bypassMode,
                    onCheckedChange = { bypassMode = it },
                    colors = CheckboxDefaults.colors(checkedColor = CrimsonRed),
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        GoldCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GoldTextField(
                        value = selectedSeller.value,
                        onValueChange = {},
                        label = "البائع الصارف كاش",
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )

                    GoldTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = "صاحب الكسر",
                        placeholder = "اسم العميل",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Karat dropdown trigger
                    var showKMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = "عيار $karat",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("العيار", color = GoldPrimary) },
                            modifier = Modifier.fillMaxWidth().clickable { showKMenu = true },
                            textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                            trailingIcon = { Text("▼", color = GoldPrimary) },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                disabledTextColor = LightText
                            )
                        )
                        DropdownMenu(
                            expanded = showKMenu,
                            onDismissRequest = { showKMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            listOf("21", "18", "24").forEach { k ->
                                DropdownMenuItem(
                                    text = { Text(k, color = LightText) },
                                    onClick = {
                                        karat = k
                                        showKMenu = false
                                    }
                                )
                            }
                        }
                    }

                    GoldTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = "وزن الكسر (جرام)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                GoldTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = "سعر شراء عيار ٢١ اليوم (جنيه)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = Math.round(goldPrice21).toString()
                )
                
                // Validations Display
                if (weight > 100 && !bypassMode) {
                    Text("⚠️ تنبيه: كمية كسر ضخمة (>100 جم). يلزم תفعيل التخطي من الإدارة (بالضغط 3 مرات على عنوان شراء كسر).", color = CrimsonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (p21 <= 0.0) {
                    Text("❌ السعر غير صالح", color = CrimsonRed, fontSize = 11.sp)
                } else if (p21 > goldPrice21 + 500) {
                    Text("⚠️ تنبيه: السعر أعلى بكثير من السعر المعتمد للسوق اليوم.", color = OrangeWarning, fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrimsonRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, CrimsonRed, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("سيتم صرف وسحب من عهدة الدرج مبلغ:", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${Math.round(calculatedValue)} جنيه",
                            color = CrimsonRed,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                PrimaryButton(
                    text = "✔️ تأكيد الشراء وصرف الكاش",
                    onClick = {
                        if (weight <= 0.0) {
                            Toast.makeText(context, "❌ يرجى إدخال وزن صحيح لقطع الكسر", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        if (p21 <= 0.0) {
                            Toast.makeText(context, "❌ يرجى إدخال سعر صحيح", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        if (weight > 100 && !bypassMode) {
                            Toast.makeText(context, "❌ كمية كبيرة جداً. يلزم تفعيل التخطي بواسطة الإدارة.", Toast.LENGTH_LONG).show()
                            return@PrimaryButton
                        }

                        viewModel.addScrapPurchase(
                            seller = selectedSeller.value,
                            customer = if (customerName.isEmpty()) "شراء كسر مباشر" else customerName,
                            karat = karat.toIntOrNull() ?: 21,
                            weight = weight,
                            price = p21,
                            totalCost = calculatedValue
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "✅ تم تسجيل الشراء وصرف المبلغ من عهدة الدرج بنجاح", Toast.LENGTH_LONG).show()
                                customerName = ""
                                weightInput = ""
                                priceInput = ""
                            } else {
                                Toast.makeText(context, "❌ حدث خطأ أثناء الاتصال بالخادم", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

// --- TAB 3: AMANA / CONSIGNMENT ---
@Composable
fun AmanaTab(
    viewModel: GoldErpViewModel,
    selectedSeller: MutableState<String>,
    itemsList: List<ItemEntity>,
    amanasList: List<AmanaEntity>,
    stockList: List<StockEntity>
) {
    val context = LocalContext.current
    var receiverName by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("") }
    var karat by remember { mutableStateOf("21") }
    var qtyInput by remember { mutableStateOf("1") }
    var weightInput by remember { mutableStateOf("") }

    if (selectedItem.isEmpty() && itemsList.isNotEmpty()) {
        selectedItem = itemsList.first().name
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GoldCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تسجيل خروج أمانة / ورشة", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoldTextField(
                            value = selectedSeller.value,
                            onValueChange = {},
                            label = "البائع المسؤول",
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        GoldTextField(
                            value = receiverName,
                            onValueChange = { receiverName = it },
                            label = "المستلم (الجهة / الشخص)",
                            placeholder = "الورشة أو الزبون المعاين",
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Select
                        var showItemMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = selectedItem,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الصنف", color = GoldPrimary) },
                                modifier = Modifier.fillMaxWidth().clickable { showItemMenu = true },
                                textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                                trailingIcon = { Text("▼", color = GoldPrimary) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                    disabledTextColor = LightText
                                )
                            )
                            DropdownMenu(
                                expanded = showItemMenu,
                                onDismissRequest = { showItemMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                itemsList.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name, color = LightText) },
                                        onClick = {
                                            selectedItem = category.name
                                            if (category.name == "سبائك" || category.name == "سبيكة") karat = "24" else if (karat == "24") karat = "21"
                                            showItemMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Karat dropdown
                        var showKaratMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.8f)) {
                            OutlinedTextField(
                                value = "عيار $karat",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("العيار", color = GoldPrimary) },
                                modifier = Modifier.fillMaxWidth().clickable { showKaratMenu = true },
                                textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                                trailingIcon = { Text("▼", color = GoldPrimary) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                    disabledTextColor = LightText
                                )
                            )
                            DropdownMenu(
                                expanded = showKaratMenu,
                                onDismissRequest = { showKaratMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                val karatOptions = if (selectedItem == "سبائك" || selectedItem == "سبيكة") listOf("24") else listOf("21", "18")
                                karatOptions.forEach { k ->
                                    DropdownMenuItem(
                                        text = { Text(k, color = LightText) },
                                        onClick = {
                                            karat = k
                                            showKaratMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoldTextField(
                            value = qtyInput,
                            onValueChange = { qtyInput = it },
                            label = "العدد",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        GoldTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = "الوزن الإجمالي (جرام)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    SecondaryButton(
                        text = "تسجيل خروج وخروج من الفاترينة",
                        onClick = {
                            val qty = qtyInput.toIntOrNull() ?: 0
                            val weight = weightInput.toDoubleOrNull() ?: 0.0
                            if (receiverName.isEmpty() || qty <= 0 || weight <= 0.0) {
                                Toast.makeText(context, "❌ الرجاء إكمال جميع البيانات بشكل صحيح", Toast.LENGTH_SHORT).show()
                                return@SecondaryButton
                            }

                            // Stock check
                            val matchedStock = stockList.find { it.key == "$selectedItem-$karat" }
                            if (matchedStock == null || matchedStock.vitrineQty < qty) {
                                Toast.makeText(context, "❌ الكمية المطلوبة غير متوفرة في الفاترينة لجردها حالياً", Toast.LENGTH_LONG).show()
                                return@SecondaryButton
                            }

                            viewModel.addAmana(
                                seller = selectedSeller.value,
                                person = receiverName,
                                item = selectedItem,
                                karat = karat,
                                qty = qty,
                                weight = weight
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "✅ تم تسجيل عملية الجر بنجاح وتحديث الفاترينة", Toast.LENGTH_SHORT).show()
                                    receiverName = ""
                                    qtyInput = "1"
                                    weightInput = ""
                                } else {
                                    Toast.makeText(context, "❌ فشل تسجيل عملية الجر", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }

        // Active amanas out table/cards list
        val activeAmanas = amanasList.filter { !it.returned }
        item {
            SectionHeader(title = "المسحوبات المعلقة (دفتر الجر) حالياً (${activeAmanas.size})")
        }

        if (activeAmanas.isEmpty()) {
            item {
                EmptyState(
                    icon = "👍",
                    title = "لا يوجد مسحوبات بالخارج",
                    description = "لا توجد أمانات أو بضاعة معلقة بعهد خارجية حالياً."
                )
            }
        } else {
            items(activeAmanas) { amana ->
                GoldCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(amana.person, color = LightText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${amana.qty} ${amana.item} (عيار ${amana.karat}) — ${amana.weight} جرام", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("المسؤول: ${amana.seller}", color = MutedText, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.returnAmana(amana.id) { success ->
                                    if (success) {
                                        Toast.makeText(context, "✅ تم إرجاع القطع للفاترينة وتسوية العهدة بنجاح", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "❌ حدث خطأ أثناء المطابقة", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.heightIn(min = 36.dp)
                        ) {
                            Text("إسترجاع", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 4: DAILY DRAWER EXPENSES ---
@Composable
fun ExpensesTab(
    viewModel: GoldErpViewModel,
    selectedSeller: MutableState<String>,
    expensesList: List<ExpenseEntity>
) {
    val context = LocalContext.current
    var amountInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    val todayExpenses = expensesList.filter { it.date.startsWith(todayDateString) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GoldCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة مصروف تشغيلي من الدرج كاش", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoldTextField(
                            value = selectedSeller.value,
                            onValueChange = {},
                            label = "البائع الصارف",
                            readOnly = true,
                            modifier = Modifier.weight(1.1f)
                        )
                        GoldTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = "المبلغ (جنيه)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GoldTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = "السبب أو البيان بالتفصيل",
                        placeholder = "مثال: بوفيه / شاي ومشروبات للصاغة / بوفيه ومصاريف كهرباء..."
                    )

                    SecondaryButton(
                        text = "تسجيل المصروف وتحديث الكاش",
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt <= 0.0 || descInput.isEmpty()) {
                                Toast.makeText(context, "❌ يرجى ملء المبلغ والبيان بالتفصيل", Toast.LENGTH_SHORT).show()
                                return@SecondaryButton
                            }

                            viewModel.addExpense(
                                seller = selectedSeller.value,
                                amt = amt,
                                desc = descInput
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "✅ تم تسجيل المصروف بنجاح", Toast.LENGTH_SHORT).show()
                                    amountInput = ""
                                    descInput = ""
                                } else {
                                    Toast.makeText(context, "❌ فشل تسجيل المصروف", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }

        item {
            SectionHeader(title = "مصروفاتك الشخصية اليوم")
        }

        val myTodayExpenses = todayExpenses.filter { it.seller == selectedSeller.value }

        if (myTodayExpenses.isEmpty()) {
            item {
                EmptyState(
                    icon = "☕",
                    title = "لا توجد مصاريف",
                    description = "لا توجد أي مصاريف مسجلة لعهدتك اليوم."
                )
            }
        } else {
            items(myTodayExpenses) { exp ->
                GoldCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(exp.desc, color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(SimpleDateFormat("hh:mm a", Locale.US).format(
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                                    timeZone = TimeZone.getTimeZone("UTC")
                                }.parse(exp.date) ?: Date()
                            ), color = MutedText, fontSize = 11.sp)
                        }

                        Text(
                            text = "-${Math.round(exp.amt)} ج",
                            color = CrimsonRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 5: SELLER CASH DRAWER SUMMARY ---
@Composable
fun SummaryTab(
    viewModel: GoldErpViewModel,
    selectedSeller: MutableState<String>,
    invoicesList: List<InvoiceEntity>,
    expensesList: List<ExpenseEntity>
) {
    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Process statistics for current seller
    var totalSalesAmount = 0.0
    var totalCashIn = 0.0
    var totalCashOut = 0.0
    var electronicPayments = 0.0
    var scrapWeightCollected = 0.0
    var scrapPurchaseAmount = 0.0

    val todayInvoices = invoicesList.filter { it.date.startsWith(todayDateString) && it.seller == selectedSeller.value }
    val todayExpenses = expensesList.filter { it.date.startsWith(todayDateString) && it.seller == selectedSeller.value }

    todayInvoices.forEach { inv ->
        if (inv.id.startsWith("INV")) {
            totalSalesAmount += inv.tot
            totalCashIn += inv.cash
            electronicPayments += inv.ewallet
            if (inv.hasOldGold) {
                scrapWeightCollected += inv.ogWeight
            }
        } else if (inv.id.startsWith("BUY")) {
            val paid = Math.abs(inv.cash)
            totalCashOut += paid
            scrapPurchaseAmount += paid
        }
    }

    todayExpenses.forEach { exp ->
        totalCashOut += exp.amt
    }

    val netCashInDrawer = totalCashIn - totalCashOut

    val allTodayActions = remember(todayInvoices, todayExpenses) {
        val list = mutableListOf<Triple<String, String, Double>>() // Type, Time/Desc, Amt
        todayInvoices.forEach { inv ->
            val time = try {
                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(inv.date) ?: Date()
                SimpleDateFormat("hh:mm a", Locale.US).format(parsed)
            } catch(e: Exception) { "" }

            if (inv.id.startsWith("INV")) {
                list.add(Triple("بيع ${if (inv.customer.isEmpty()) "عميل" else inv.customer}", time, inv.tot))
            } else {
                list.add(Triple("شراء كسر مباشر", time, -Math.abs(inv.cash)))
            }
        }
        todayExpenses.forEach { exp ->
            val time = try {
                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(exp.date) ?: Date()
                SimpleDateFormat("hh:mm a", Locale.US).format(parsed)
            } catch(e: Exception) { "" }
            list.add(Triple("مصروف: ${exp.desc}", time, -exp.amt))
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Star seller KPI Card
        item {
            KpiCard(
                title = "صافي الكاش الفعلي عهدتك بالدرج 💵",
                value = "${Math.round(netCashInDrawer).toLocaleString()} جنيه",
                icon = "💵",
                accentColor = EmeraldGreen
            )
        }

        // Secondary KPI Cards grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "إجمالي الفواتير",
                    value = "${Math.round(totalSalesAmount).toLocaleString()} ج",
                    icon = "💰",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "إلكتروني وبنك",
                    value = "${Math.round(electronicPayments).toLocaleString()} ج",
                    icon = "💳",
                    modifier = Modifier.weight(1f),
                    accentColor = SapphireBlue
                )
            }
        }

        // Cash details flow breakdown
        item {
            GoldCard(borderColor = EmeraldGreen.copy(alpha = 0.4f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تفاصيل حركة النقدية (الكاش) بالدرج", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("كاش مبيعات وارد (+)", color = MutedText, fontSize = 13.sp)
                        Text("+${Math.round(totalCashIn).toLocaleString()} ج", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("كاش شراء كسر (-)", color = MutedText, fontSize = 13.sp)
                        Text("-${Math.round(scrapPurchaseAmount).toLocaleString()} ج", color = CrimsonRed, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مصروفات وردية خارجة (-)", color = MutedText, fontSize = 13.sp)
                        Text("-${Math.round(todayExpenses.sumOf { it.amt }).toLocaleString()} ج", color = CrimsonRed, fontWeight = FontWeight.Bold)
                    }

                    Divider(color = BorderColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("صافي الكاش الفعلي بالدرج (=)", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${Math.round(netCashInDrawer).toLocaleString()} ج", color = EmeraldGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }

        // Scrap details
        item {
            GoldCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ذهب كسر مستلم بالدرج", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("مشتريات وكسر الزبائن كوزن", color = LightText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = String.format(Locale.US, "%.2f ج", scrapWeightCollected) + "رام",
                        color = AmberAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Transactions log
        item {
            SectionHeader(title = "سجل عملياتك اليومية")
        }

        if (allTodayActions.isEmpty()) {
            item {
                EmptyState(
                    icon = "📝",
                    title = "لا يوجد نشاط",
                    description = "لم تقم بأي عملية بيع أو حركة حتى الآن."
                )
            }
        } else {
            items(allTodayActions) { (actionTitle, actionTime, amt) ->
                GoldCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(actionTitle, color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(actionTime, color = MutedText, fontSize = 11.sp)
                        }

                        Text(
                            text = if (amt > 0) "+${Math.round(amt).toLocaleString()} ج" else "${Math.round(amt).toLocaleString()} ج",
                            color = if (amt > 0) EmeraldGreen else CrimsonRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            var actualCash by remember { mutableStateOf("") }
            var isClosing by remember { mutableStateOf(false) }

            GoldCard(borderColor = SapphireBlue.copy(alpha = 0.5f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تقفيل الوردية وجرد الكاش", color = SapphireBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GoldTextField(
                            value = actualCash,
                            onValueChange = { actualCash = it },
                            label = "الكاش الفعلي الموجود في الدرج الآن",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { isClosing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue),
                            modifier = Modifier.height(55.dp)
                        ) {
                            Text("مطابقة")
                        }
                    }

                    if (isClosing) {
                        val actual = actualCash.toDoubleOrNull() ?: 0.0
                        val diff = actual - netCashInDrawer
                        val absDiff = Math.abs(diff)

                        val statusColor = when {
                            absDiff == 0.0 -> EmeraldGreen
                            absDiff < 100 -> OrangeWarning
                            else -> CrimsonRed
                        }

                        val statusText = when {
                            absDiff == 0.0 -> "مطابق تماماً. لا يوجد عجز أو زيادة."
                            diff < 0 -> "⚠️ يوجد عجز نقدي بقيمة ${Math.round(absDiff)} جنيه."
                            else -> "⚠️ توجد زيادة نقدية بقيمة ${Math.round(absDiff)} جنيه."
                        }

                        val instructionText = when {
                            absDiff == 0.0 -> "يمكنك إنهاء الوردية بأمان."
                            absDiff < 100 -> "الفرق بسيط (أقل من 100). يرجى المراجعة سريعاً أو تقييد العجز."
                            absDiff < 500 -> "الفرق كبير. يلزم مراجعة الإدارة (Manager Review)."
                            else -> "الفرق حرج (> 500). يلزم تحقيق فورى ومطابقة الفواتير بدقة (Mandatory Investigation)."
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(statusText, color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(instructionText, color = LightText, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val reportText = buildString {
                        appendLine("📊 تقرير وردية البائع اليومي")
                        appendLine("--------------------------")
                        appendLine("البائع: ${selectedSeller.value}")
                        appendLine("التاريخ: $todayDateString")
                        appendLine("--------------------------")
                        appendLine("💰 إجمالي المبيعات: ${Math.round(totalSalesAmount).toLocaleString()} ج")
                        appendLine("💳 إلكتروني وبنك: ${Math.round(electronicPayments).toLocaleString()} ج")
                        appendLine("💵 كاش وارد للدرج: ${Math.round(totalCashIn).toLocaleString()} ج")
                        appendLine("💸 كاش مصروفات وتغيير كسر: -${Math.round(totalCashOut).toLocaleString()} ج")
                        appendLine("--------------------------")
                        appendLine("⚖️ ذهب كسر مستلم بالدرج: ${String.format(Locale.US, "%.2f", scrapWeightCollected)} جرام")
                        appendLine("--------------------------")
                        appendLine("💵 صافي الكاش الفعلي عهدتك بالدرج: ${Math.round(netCashInDrawer).toLocaleString()} ج")
                        appendLine("--------------------------")
                        appendLine("عدد الحركات اليومية: ${allTodayActions.size}")
                        appendLine("--------------------------")
                        appendLine("تم استخراجه وحفظه تلقائياً محلياً بدون إنترنت")
                    }
                    com.example.utils.ShareUtils.shareText(context, "تقرير الوردية - ${selectedSeller.value}", reportText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text("📤 مشاركة وحفظ تقرير الوردية مع الإدارة", color = DarkBg, fontWeight = FontWeight.Black)
            }
        }
    }
}

// Utility to format double with grouping separator
fun Long.toLocaleString(): String {
    return String.format(Locale.US, "%,d", this)
}

@Composable
fun AiAssistantDialog(onDismiss: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GoldCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🤖 المساعد الإداري الذكي",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "سيجيبك المساعد بأسلوب إداري ومنطقي وعملي.",
                    color = MutedText,
                    fontSize = 12.sp
                )

                GoldTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = "اكتب سؤالك أو المشكلة هنا...",
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    singleLine = false
                )

                if (isLoading) {
                    CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (answer.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = answer,
                            color = LightText,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "إلغاء",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "سؤال",
                        onClick = {
                            if (question.isNotBlank()) {
                                isLoading = true
                                answer = ""
                                coroutineScope.launch {
                                    answer = com.example.api.askAssistant(question)
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
