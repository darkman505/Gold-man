package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

@Composable
fun OwnerScreen(
    viewModel: GoldErpViewModel,
    onNavigateBack: () -> Unit
) {
    var activeTab by remember { mutableStateOf("dashboard") } // dashboard, archive, sellers, inventory, review, reports, settings

    val context = LocalContext.current
    val sellers by viewModel.allSellers.collectAsState()
    val itemsList by viewModel.allItems.collectAsState()
    val stockList by viewModel.allStock.collectAsState()
    val expensesList by viewModel.allExpenses.collectAsState()
    val amanasList by viewModel.allAmanas.collectAsState()
    val invoicesList by viewModel.allInvoices.collectAsState()

    ArabicRtlLayout {
        Scaffold(
            containerColor = DarkBg,
            bottomBar = {
                Surface(
                    color = DarkSurface,
                    tonalElevation = 8.dp,
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            "dashboard" to Pair("👁️", "الرئيسية"),
                            "ai" to Pair("✨", "الذكاء الاصطناعي"),
                            "inventory" to Pair("💎", "الخزنة والجرد"),
                            "reports" to Pair("🪙", "الدفاتر والتقارير"),
                            "crm" to Pair("👥", "العملاء والموردين"),
                            "audit" to Pair("🛡️", "سجل الحركات"),
                            "settings" to Pair("⚙️", "الإعدادات")
                        )
                        tabs.forEach { (tabId, pair) ->
                            val (emoji, label) = pair
                            val isSelected = activeTab == tabId
                            
                            val bgColor by androidx.compose.animation.animateColorAsState(
                                targetValue = if (isSelected) GoldPrimary else DarkSurfaceElevated,
                                label = "bg_color"
                            )
                            val textColor by androidx.compose.animation.animateColorAsState(
                                targetValue = if (isSelected) DarkBg else LightText,
                                label = "text_color"
                            )
                            val border = if (isSelected) null else BorderStroke(1.dp, BorderColor)
                            
                            val scale by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1.0f,
                                label = "scale"
                            )

                            Surface(
                                onClick = { activeTab = tabId },
                                shape = RoundedCornerShape(20.dp),
                                color = bgColor,
                                border = border,
                                modifier = Modifier
                                    .height(40.dp)
                                    .minimumInteractiveComponentSize()
                                    .scale(scale)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(emoji, fontSize = 16.sp)
                                    Text(
                                        text = label,
                                        color = textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
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
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👑 لوحة تحكم المعلم",
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )

                    GoldOutlinedButton(
                        text = "← الرئيسية",
                        onClick = onNavigateBack,
                        modifier = Modifier.width(110.dp)
                    )
                }

                // Active Admin Tab view
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        "dashboard" -> OwnerDashboardTab(viewModel, invoicesList, expensesList, amanasList) { tab ->
                            activeTab = tab
                        }
                        "ai" -> AiDashboardTab(viewModel)
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

@Composable
fun CombinedOwnerInventory(
    viewModel: GoldErpViewModel,
    itemsList: List<ItemEntity>,
    stockList: List<StockEntity>,
    amanasList: List<AmanaEntity>
) {
    var subTab by remember { mutableStateOf("stock") }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "stock" to "جرد المعرض والخزنة",
                "review" to "المطابقة وتحديد العجز"
            ).forEach { (id, title) ->
                val isSelected = subTab == id
                Surface(
                    onClick = { subTab = id },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) GoldPrimary else DarkSurfaceElevated,
                    border = if (isSelected) null else BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = title,
                            color = if (isSelected) DarkBg else LightText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            if (subTab == "stock") {
                OwnerInventoryTab(viewModel, itemsList, stockList, amanasList)
            } else {
                OwnerReviewTab(viewModel, stockList)
            }
        }
    }
}

@Composable
fun CombinedOwnerReports(
    viewModel: GoldErpViewModel,
    invoicesList: List<InvoiceEntity>,
    expensesList: List<ExpenseEntity>,
    sellersList: List<SellerEntity>
) {
    var subTab by remember { mutableStateOf("archive") }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "archive" to "📚 أرشيف الفواتير",
                "reports" to "📊 ميزانية المحل",
                "sellers" to "👨‍💼 كشف البياعين"
            ).forEach { (id, title) ->
                val isSelected = subTab == id
                Surface(
                    onClick = { subTab = id },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) GoldPrimary else DarkSurfaceElevated,
                    border = if (isSelected) null else BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.height(40.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = title,
                            color = if (isSelected) DarkBg else LightText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                "archive" -> OwnerArchiveTab(viewModel, invoicesList)
                "reports" -> OwnerReportsTab(viewModel, invoicesList, expensesList)
                "sellers" -> OwnerSellersTab(viewModel, sellersList, invoicesList, expensesList)
            }
        }
    }
}

// --- OWNER TAB 1: EXECUTIVE DASHBOARD ---
@Composable
fun OwnerDashboardTab(
    viewModel: GoldErpViewModel,
    invoicesList: List<InvoiceEntity>,
    expensesList: List<ExpenseEntity>,
    amanasList: List<AmanaEntity>,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    // Computations
    val todayInvs = invoicesList.filter { it.date.startsWith(today) }
    val todayExps = expensesList.filter { it.date.startsWith(today) }

    var salesAmount = 0.0
    var cashInDrawer = 0.0
    var electronicPayments = 0.0
    var scrapGoldWeight = 0.0
    var scrapPayouts = 0.0

    todayInvs.forEach { inv ->
        if (inv.id.startsWith("INV")) {
            salesAmount += inv.tot
            cashInDrawer += inv.cash
            electronicPayments += inv.ewallet
            if (inv.hasOldGold) scrapGoldWeight += inv.ogWeight
        } else if (inv.id.startsWith("BUY")) {
            val paid = Math.abs(inv.cash)
            cashInDrawer -= paid
            scrapPayouts += paid
        }
    }

    todayExps.forEach { exp ->
        cashInDrawer -= exp.amt
    }

    val activeAmanasCount = amanasList.count { !it.returned }

    // Gold Prices Ticker Widget
    var price21Input by remember { mutableStateOf("") }

    val allTodayActions = remember(todayInvs, todayExps) {
        val list = mutableListOf<Triple<String, String, Double>>() // Title, Time/Seller, Amt
        todayInvs.forEach { inv ->
            val time = try {
                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(inv.date) ?: Date()
                SimpleDateFormat("hh:mm a", Locale.US).format(parsed) + " | " + inv.seller
            } catch(e: Exception) { inv.seller }

            if (inv.id.startsWith("INV")) {
                list.add(Triple("مبيعات: ${if (inv.customer.isEmpty()) "عميل" else inv.customer}", time, inv.tot))
            } else {
                list.add(Triple("شراء كسر مباشر", time, -Math.abs(inv.cash)))
            }
        }
        todayExps.forEach { exp ->
            val time = try {
                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(exp.date) ?: Date()
                SimpleDateFormat("hh:mm a", Locale.US).format(parsed) + " | " + exp.seller
            } catch(e: Exception) { exp.seller }
            list.add(Triple("مصروف الدرج: ${exp.desc}", time, -exp.amt))
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard statistics KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "مبيعات اليوم",
                    value = "${Math.round(salesAmount).toLocaleString()} ج",
                    icon = "💰",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("reports") }
                )
                KpiCard(
                    title = "كاش في المحل",
                    value = "${Math.round(cashInDrawer).toLocaleString()} ج",
                    icon = "💵",
                    modifier = Modifier.weight(1f),
                    accentColor = EmeraldGreen,
                    onClick = { onNavigate("reports") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "تحويلات الكترونية",
                    value = "${Math.round(electronicPayments).toLocaleString()} ج",
                    icon = "📱",
                    modifier = Modifier.weight(1f),
                    accentColor = SapphireBlue,
                    onClick = { onNavigate("reports") }
                )
                KpiCard(
                    title = "كسر مستلم",
                    value = String.format(Locale.US, "%.2f ج", scrapGoldWeight) + "رام",
                    icon = "⚖️",
                    modifier = Modifier.weight(1f),
                    accentColor = CrimsonRed,
                    onClick = { onNavigate("reports") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "حركات اليوم",
                    value = "${todayInvs.size + todayExps.size} حركات",
                    icon = "🧾",
                    modifier = Modifier.weight(1f),
                    accentColor = AmberAccent,
                    onClick = { onNavigate("audit") }
                )
                KpiCard(
                    title = "معلق بالخارج (أمانات)",
                    value = "$activeAmanasCount قطعة",
                    icon = "🔍",
                    modifier = Modifier.weight(1f),
                    accentColor = OrangeWarning,
                    onClick = { onNavigate("inventory") }
                )
            }
        }

        // --------------------------------
        
        // Today Transaction Log list
        item {
            SectionHeader(title = "سجل حركات اليوم بالصاغة")
        }

        if (allTodayActions.isEmpty()) {
            item {
                EmptyState(
                    icon = "☕",
                    title = "لا توجد حركات تجارية",
                    description = "لم يتم تسجيل أي فواتير أو مصروفات اليوم حتى الآن. ابدأ العمل لإضافة حركات جديدة."
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
            GoldButton(
                text = "📤 تصدير ومشاركة التقرير اليومي لصاحب المحل (واتساب / درايف)",
                onClick = {
                    val textToShare = buildString {
                        appendLine("👑 تقرير الخلاصة اليومية - صاحب المحل")
                        appendLine("--------------------------------")
                        appendLine("التاريخ: $today")
                        appendLine("--------------------------------")
                        appendLine("💰 مبيعات اليوم: ${Math.round(salesAmount).toLocaleString()} ج")
                        appendLine("💵 كاش متوفر بالمحل: ${Math.round(cashInDrawer).toLocaleString()} ج")
                        appendLine("📱 تحويلات إلكترونية: ${Math.round(electronicPayments).toLocaleString()} ج")
                        appendLine("⚖️ وزن الكسر المستلم: ${String.format(Locale.US, "%.2f", scrapGoldWeight)} جرام")
                        appendLine("🧾 عدد العمليات: ${todayInvs.size + todayExps.size} حركات")
                        appendLine("🔍 الأمانات المعلقة بالخارج: $activeAmanasCount قطعة")
                        appendLine("--------------------------------")
                        appendLine("أسعار الذهب الافتتاحية المسجلة اليوم:")
                        appendLine("• عيار 24: ${Math.round(viewModel.goldPrice21.value * 24 / 21)} ج")
                        appendLine("• عيار 21: ${Math.round(viewModel.goldPrice21.value)} ج")
                        appendLine("• عيار 18: ${Math.round(viewModel.goldPrice21.value * 18 / 21)} ج")
                        appendLine("--------------------------------")
                        appendLine("تم استخراج التقرير محلياً بنجاح ومضمون 100%")
                    }
                    com.example.utils.ShareUtils.shareText(context, "التقرير اليومي لصاحب المحل - $today", textToShare)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

// --- OWNER TAB 2: INVOICE ARCHIVE ---
@Composable
fun OwnerArchiveTab(
    viewModel: GoldErpViewModel,
    invoicesList: List<InvoiceEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    var showInvoiceDetail by remember { mutableStateOf<InvoiceEntity?>(null) }

    val filteredInvoices = remember(invoicesList, searchQuery) {
        invoicesList.filter { inv ->
            inv.id.contains(searchQuery, ignoreCase = true) ||
            inv.customer.contains(searchQuery, ignoreCase = true) ||
            inv.seller.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GoldTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "🔍 بحث سريع بالفواتير (بالرقم أو المشتري أو البائع)",
                placeholder = "اكتب للبحث..."
            )
        }

        item {
            SectionHeader(title = "دفتر الفواتير والأرشيف (${filteredInvoices.size})")
        }

        if (filteredInvoices.isEmpty()) {
            item {
                EmptyState(
                    icon = "⚠️",
                    title = "لا توجد فواتير",
                    description = "لا توجد أي فواتير مطابقة للبحث"
                )
            }
        } else {
            items(filteredInvoices) { inv ->
                GoldCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInvoiceDetail = inv }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (inv.customer.isEmpty()) "عميل طيار" else inv.customer,
                                color = LightText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "رقم الفاتورة: ${inv.id}  •  البائع: ${inv.seller}",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = inv.date.substringBefore("T"),
                                color = MutedText,
                                fontSize = 10.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${Math.round(inv.tot).toLocaleString()} ج",
                                color = GoldPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (inv.id.startsWith("BUY")) {
                                Text("شراء كسر", color = CrimsonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("فاتورة بيع", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Invoice Detail modal dialog
    if (showInvoiceDetail != null) {
        val inv = showInvoiceDetail!!
        val parsedItems = remember(inv) { JsonHelper.fromJson(inv.itemsJson) }

        Dialog(onDismissRequest = { showInvoiceDetail = null }) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📄 تفاصيل الفاتورة", fontSize = 18.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
                        IconButton(onClick = { showInvoiceDetail = null }) {
                            Text("✕", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = BorderColor)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("رقم العملية:", color = MutedText, fontSize = 10.sp)
                                Text(inv.id, color = LightText, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("التاريخ والوقت:", color = MutedText, fontSize = 10.sp)
                                Text(inv.date.replace("T", " ").replace("Z", ""), color = LightText, fontSize = 12.sp)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("البائع المالك للعهد:", color = MutedText, fontSize = 10.sp)
                                Text(inv.seller, color = LightText, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("الزبون المشترك:", color = MutedText, fontSize = 10.sp)
                                Text(if (inv.customer.isEmpty()) "عميل طيار" else inv.customer, color = LightText, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = BorderColor)

                        Text("الأصناف المسجلة بالفاتورة:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        if (parsedItems.isEmpty()) {
                            Text("لا توجد أصناف بيع (عملية شراء كسر مباشرة من العميل)", color = MutedText, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            parsedItems.forEach { r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${r.t} (عيار ${r.k})", color = LightText, fontWeight = FontWeight.Bold)
                                    Text("${r.q} قطع / ${r.w} جرام", color = GoldPrimary, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Divider(color = BorderColor)

                        Text("المدفوعات والمستلمات:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (inv.cash != 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(if (inv.cash > 0) "كاش وارد للدرج 💵" else "كاش مصاريف خارج 💸", color = LightText)
                                    Text("${Math.round(inv.cash)} ج", color = if (inv.cash > 0) EmeraldGreen else CrimsonRed, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (inv.instapay > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("إنستا باي 📱", color = LightText)
                                    Text("${Math.round(inv.instapay)} ج", color = SapphireBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (inv.vodafone > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("فودافون كاش 📱", color = LightText)
                                    Text("${Math.round(inv.vodafone)} ج", color = CrimsonRed, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (inv.visa > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("فيزا وبنك 💳", color = LightText)
                                    Text("${Math.round(inv.visa)} ج", color = GoldSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (inv.hasOldGold && inv.ogWeight > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ذهب كسر مستلم (عيار ${inv.ogKarat}) ⚖️", color = LightText)
                                    Text("${inv.ogWeight} جرام / ${Math.round(inv.ogValue)} ج", color = AmberAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (inv.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ملاحظات الفاتورة:", color = MutedText, fontSize = 10.sp)
                            Text(inv.notes, color = LightText, fontSize = 13.sp, modifier = Modifier.background(DarkSurfaceElevated, RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth())
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        GoldButton(
                            text = "📤 مشاركة النص",
                            onClick = {
                                val textToShare = buildString {
                                    appendLine("📄 تفاصيل الفاتورة")
                                    appendLine("--------------------------")
                                    appendLine("رقم العملية: ${inv.id}")
                                    appendLine("التاريخ: ${inv.date.replace("T", " ").replace("Z", "")}")
                                    appendLine("البائع: ${inv.seller}")
                                    appendLine("العميل: ${if (inv.customer.isEmpty()) "عميل طيار" else inv.customer}")
                                    appendLine("--------------------------")
                                    appendLine("الذهب المباع/المسجل:")
                                    parsedItems.forEach { r ->
                                        appendLine("• ${r.t} (عيار ${r.k}): ${r.q} قطعة / ${r.w} جرام")
                                    }
                                    appendLine("--------------------------")
                                    appendLine("المقبوضات والمستلمات:")
                                    if (inv.cash != 0.0) appendLine("• كاش: ${inv.cash} ج")
                                    if (inv.instapay > 0) appendLine("• إنستا باي: ${inv.instapay} ج")
                                    if (inv.vodafone > 0) appendLine("• فودافون كاش: ${inv.vodafone} ج")
                                    if (inv.visa > 0) appendLine("• فيزا وبنك: ${inv.visa} ج")
                                    if (inv.hasOldGold && inv.ogWeight > 0.0) {
                                        appendLine("• ذهب كسر مستلم (عيار ${inv.ogKarat}): ${inv.ogWeight} جرام / ${inv.ogValue} ج")
                                    }
                                    appendLine("--------------------------")
                                    appendLine("إجمالي قيمة الفاتورة: ${Math.round(inv.tot)} ج")
                                    if (inv.notes.isNotEmpty()) {
                                        appendLine("ملاحظات: ${inv.notes}")
                                    }
                                    appendLine("--------------------------")
                                    appendLine("تم الاستخراج من نظام إدارة الصاغة")
                                }
                                com.example.utils.ShareUtils.shareText(context, "فاتورة رقم ${inv.id}", textToShare)
                            },
                            modifier = Modifier.weight(1.2f)
                        )

                        GoldOutlinedButton(
                            text = "📁 مشاركة CSV",
                            onClick = {
                                val csvContent = buildString {
                                    appendLine("رقم العملية,التاريخ,البائع,العميل,الأصناف,إجمالي القيمة,كاش,إنستا باي,فودافون كاش,فيزا,كسر مستلم")
                                    val itemsStr = parsedItems.joinToString("; ") { "${it.t} (عيار ${it.k}): ${it.q}ق/${it.w}جم" }
                                    appendLine("\"${inv.id}\",\"${inv.date}\",\"${inv.seller}\",\"${inv.customer}\",\"$itemsStr\",${inv.tot},${inv.cash},${inv.instapay},${inv.vodafone},${inv.visa},\"${if (inv.hasOldGold) "${inv.ogWeight}جم/${inv.ogValue}ج" else "لا يوجد"}\"")
                                }
                                com.example.utils.ShareUtils.shareFile(context, "invoice_${inv.id}.csv", csvContent, "text/csv")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    NeutralButton(text = "إغلاق التفاصيل", onClick = { showInvoiceDetail = null })
                }
            }
        }
    }
}

// --- OWNER TAB 3: SELLERS DRAWERS STATUS ---
@Composable
fun OwnerSellersTab(
    viewModel: GoldErpViewModel,
    sellers: List<SellerEntity>,
    invoicesList: List<InvoiceEntity>,
    expensesList: List<ExpenseEntity>
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "عهد نقدية وأرصدة البياعين الفعلي اليوم")
        }

        if (sellers.isEmpty()) {
            item {
                EmptyState(
                    icon = "👥",
                    title = "لا يوجد بائعون",
                    description = "لم يتم إضافة أي بائعين بالنظام بعد"
                )
            }
        } else {
            items(sellers) { s ->
                // Compute daily drawer totals for this specific seller
                var cashIn = 0.0
                var cashOut = 0.0
                var elec = 0.0
                var totalSales = 0.0
                var scrapWt = 0.0

                invoicesList.filter { it.date.startsWith(today) && it.seller == s.name }.forEach { inv ->
                    if (inv.id.startsWith("INV")) {
                        totalSales += inv.tot
                        cashIn += inv.cash
                        elec += inv.ewallet
                        if (inv.hasOldGold) scrapWt += inv.ogWeight
                    } else if (inv.id.startsWith("BUY")) {
                        val paid = Math.abs(inv.cash)
                        cashOut += paid
                    }
                }

                expensesList.filter { it.date.startsWith(today) && it.seller == s.name }.forEach { exp ->
                    cashOut += exp.amt
                }

                val currentExpectedCash = cashIn - cashOut

                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(s.name, color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (currentExpectedCash >= 0.0) EmeraldGreen.copy(alpha = 0.12f) else CrimsonRed.copy(alpha = 0.12f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "عهد كاش: ${Math.round(currentExpectedCash).toLocaleString()} ج",
                                    color = if (currentExpectedCash >= 0.0) EmeraldGreen else CrimsonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("إجمالي الفواتير", color = MutedText, fontSize = 10.sp)
                                Text("${Math.round(totalSales).toLocaleString()} ج", color = LightText, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("مدفوعات بنك/الكتروني", color = MutedText, fontSize = 10.sp)
                                Text("${Math.round(elec).toLocaleString()} ج", color = SapphireBlue, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("كسر مستلم", color = MutedText, fontSize = 10.sp)
                                Text("${scrapWt} ج", color = AmberAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- OWNER TAB 4: INVENTORY STOCK MANAGEMENT ---
@Composable
fun OwnerInventoryTab(
    viewModel: GoldErpViewModel,
    categories: List<ItemEntity>,
    stockList: List<StockEntity>,
    amanasList: List<AmanaEntity>
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Forms state
    var selectedItemForAdd by remember { mutableStateOf("") }
    var karatForAdd by remember { mutableStateOf("21") }
    var qtyForAdd by remember { mutableStateOf("1") }
    var locForAdd by remember { mutableStateOf("vitrine") } // vitrine, vault

    var selectedItemForTransfer by remember { mutableStateOf("") }
    var karatForTransfer by remember { mutableStateOf("21") }
    var qtyForTransfer by remember { mutableStateOf("1") }
    var transferFrom by remember { mutableStateOf("vault") } // vault, vitrine

    if (selectedItemForAdd.isEmpty() && categories.isNotEmpty()) {
        selectedItemForAdd = categories.first().name
    }
    if (selectedItemForTransfer.isEmpty() && categories.isNotEmpty()) {
        selectedItemForTransfer = categories.first().name
    }

    // Active amana aggregates by stock item key
    val amanaStockTotals = remember(amanasList) {
        val map = mutableMapOf<String, Int>()
        amanasList.filter { !it.returned }.forEach { am ->
            val k = "${am.item}-${am.karat}"
            map[k] = (map[k] ?: 0) + am.qty
        }
        map
    }

    val filteredStock = remember(stockList, searchQuery) {
        stockList.filter { s ->
            s.item.contains(searchQuery, ignoreCase = true) ||
            s.karat.contains(searchQuery)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form 1: Add new stock items
        item {
            GoldCard(borderColor = EmeraldGreen.copy(alpha = 0.3f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📥 إضافة بضاعة جديدة للمحل", color = EmeraldGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category picker
                        var showAddMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = selectedItemForAdd,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الصنف", color = GoldPrimary) },
                                modifier = Modifier.fillMaxWidth().clickable { showAddMenu = true },
                                textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                                trailingIcon = { Text("▼", color = GoldPrimary) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                    disabledTextColor = LightText
                                )
                            )
                            DropdownMenu(
                                expanded = showAddMenu,
                                onDismissRequest = { showAddMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name, color = LightText) },
                                        onClick = {
                                            selectedItemForAdd = cat.name
                                            if (cat.name == "سبائك" || cat.name == "سبيكة") karatForAdd = "24" else if (karatForAdd == "24") karatForAdd = "21"
                                            showAddMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Karat select
                        var showKaratMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.8f)) {
                            OutlinedTextField(
                                value = "عيار $karatForAdd",
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
                                val karatOptions = if (selectedItemForAdd == "سبائك" || selectedItemForAdd == "سبيكة") listOf("24") else listOf("21", "18")
                                karatOptions.forEach { k ->
                                    DropdownMenuItem(
                                        text = { Text(k, color = LightText) },
                                        onClick = {
                                            karatForAdd = k
                                            showKaratMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val currentKeyAdd = "$selectedItemForAdd-$karatForAdd"
                    val currentStockAdd = stockList.find { it.key == currentKeyAdd }
                    val currentVitrineQtyAdd = currentStockAdd?.vitrineQty ?: 0
                    val currentVaultQtyAdd = currentStockAdd?.vaultQty ?: 0
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الرصيد الفعلي الحالي:", color = MutedText, fontSize = 13.sp)
                        Text(
                            "الفاترينة: $currentVitrineQtyAdd | الخزنة: $currentVaultQtyAdd", 
                            color = EmeraldGreen, 
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoldTextField(
                            value = qtyForAdd,
                            onValueChange = { qtyForAdd = it },
                            label = "العدد المضاف",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        // Location Selector
                        var showLocMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = if (locForAdd == "vitrine") "الفاترينة المعروضة" else "الخزنة (المخزن الرئيسي)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("المكان الجديد", color = GoldPrimary) },
                                modifier = Modifier.fillMaxWidth().clickable { showLocMenu = true },
                                textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                                trailingIcon = { Text("▼", color = GoldPrimary) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                    disabledTextColor = LightText
                                )
                            )
                            DropdownMenu(
                                expanded = showLocMenu,
                                onDismissRequest = { showLocMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                dropdownMenuItem(
                                    text = { Text("الفاترينة المعروضة", color = LightText) },
                                    onClick = {
                                        locForAdd = "vitrine"
                                        showLocMenu = false
                                    }
                                )
                                dropdownMenuItem(
                                    text = { Text("الخزنة (المخزن)", color = LightText) },
                                    onClick = {
                                        locForAdd = "vault"
                                        showLocMenu = false
                                    }
                                )
                            }
                        }
                    }

                    PrimaryButton(
                        text = "تأكيد الإضافة للمخزون",
                        onClick = {
                            val q = qtyForAdd.toIntOrNull() ?: 0
                            if (q > 0) {
                                viewModel.addStockItem(selectedItemForAdd, karatForAdd, q, locForAdd)
                                Toast.makeText(context, "✅ تم إضافة البضاعة بنجاح", Toast.LENGTH_SHORT).show()
                                qtyForAdd = "1"
                            }
                        }
                    )
                }
            }
        }

        // Form 2: Transfer stock items
        item {
            GoldCard(borderColor = SapphireBlue.copy(alpha = 0.3f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🔄 نقل بضاعة بين الفاترينة والخزنة", color = SapphireBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category select
                        var showTransferMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = selectedItemForTransfer,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الصنف المنقول", color = GoldPrimary) },
                                modifier = Modifier.fillMaxWidth().clickable { showTransferMenu = true },
                                textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                                trailingIcon = { Text("▼", color = GoldPrimary) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                    disabledTextColor = LightText
                                )
                            )
                            DropdownMenu(
                                expanded = showTransferMenu,
                                onDismissRequest = { showTransferMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name, color = LightText) },
                                        onClick = {
                                            selectedItemForTransfer = cat.name
                                            if (cat.name == "سبائك" || cat.name == "سبيكة") karatForTransfer = "24" else if (karatForTransfer == "24") karatForTransfer = "21"
                                            showTransferMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Karat select
                        var showKTransferMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.8f)) {
                            OutlinedTextField(
                                value = "عيار $karatForTransfer",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("العيار", color = GoldPrimary) },
                                modifier = Modifier.fillMaxWidth().clickable { showKTransferMenu = true },
                                textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                                trailingIcon = { Text("▼", color = GoldPrimary) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                    disabledTextColor = LightText
                                )
                            )
                            DropdownMenu(
                                expanded = showKTransferMenu,
                                onDismissRequest = { showKTransferMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                val karatOptions = if (selectedItemForTransfer == "سبائك" || selectedItemForTransfer == "سبيكة") listOf("24") else listOf("21", "18")
                                karatOptions.forEach { k ->
                                    DropdownMenuItem(
                                        text = { Text(k, color = LightText) },
                                        onClick = {
                                            karatForTransfer = k
                                            showKTransferMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val currentKeyTransfer = "$selectedItemForTransfer-$karatForTransfer"
                    val currentStockTransfer = stockList.find { it.key == currentKeyTransfer }
                    val currentVitrineQtyTransfer = currentStockTransfer?.vitrineQty ?: 0
                    val currentVaultQtyTransfer = currentStockTransfer?.vaultQty ?: 0
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الرصيد الفعلي الحالي:", color = MutedText, fontSize = 13.sp)
                        Text(
                            "الفاترينة: $currentVitrineQtyTransfer | الخزنة: $currentVaultQtyTransfer", 
                            color = EmeraldGreen, 
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoldTextField(
                            value = qtyForTransfer,
                            onValueChange = { qtyForTransfer = it },
                            label = "العدد المنقول",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        // Source location selector
                        var showFromMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = if (transferFrom == "vault") "من الخزنة ➔ للفاترينة" else "من الفاترينة ➔ للخزنة",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("إتجاه النقل", color = GoldPrimary) },
                                modifier = Modifier.fillMaxWidth().clickable { showFromMenu = true },
                                textStyle = TextStyle(color = LightText, fontWeight = FontWeight.Bold),
                                trailingIcon = { Text("▼", color = GoldPrimary) },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.6f),
                                    disabledTextColor = LightText
                                )
                            )
                            DropdownMenu(
                                expanded = showFromMenu,
                                onDismissRequest = { showFromMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                dropdownMenuItem(
                                    text = { Text("من الخزنة ➔ للفاترينة", color = LightText) },
                                    onClick = {
                                        transferFrom = "vault"
                                        showFromMenu = false
                                    }
                                )
                                dropdownMenuItem(
                                    text = { Text("من الفاترينة ➔ للخزنة", color = LightText) },
                                    onClick = {
                                        transferFrom = "vitrine"
                                        showFromMenu = false
                                    }
                                )
                            }
                        }
                    }

                    PrimaryButton(
                        text = "نقل البضاعة بالسيستم",
                        onClick = {
                            val q = qtyForTransfer.toIntOrNull() ?: 0
                            val toLoc = if (transferFrom == "vault") "vitrine" else "vault"
                            if (q > 0) {
                                viewModel.transferStockItem(selectedItemForTransfer, karatForTransfer, q, transferFrom, toLoc) { success ->
                                    if (success) {
                                        Toast.makeText(context, "✅ تم نقل القطع بنجاح وتحديث الجرد", Toast.LENGTH_SHORT).show()
                                        qtyForTransfer = "1"
                                    } else {
                                        Toast.makeText(context, "❌ الكمية المتاحة بالنقل غير متوفرة بالموقع المصدر", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Search Bar & Table list
        item {
            GoldTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "🔍 بحث سريع بالمخزون المتوفر",
                placeholder = "اكتب الصنف أو العيار..."
            )
        }

        item {
            SectionHeader(title = "رصيد المحل بالتفصيل")
        }

        if (filteredStock.isEmpty()) {
            item {
                EmptyState(
                    icon = "⚠️",
                    title = "لا توجد أصناف",
                    description = "لا توجد أصناف مطابقة للبحث"
                )
            }
        } else {
            item {
                // Advanced Inventory Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الصنف", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("فاترينة", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text("خزنة", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text("أمانة", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text("إجمالي", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                    Text("حالة", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)
                }
            }
            
            items(filteredStock) { stock ->
                val key = stock.key
                val amanaQty = amanaStockTotals[key] ?: 0
                val totalQty = stock.vitrineQty + stock.vaultQty + amanaQty
                
                val statusColor = when {
                    totalQty <= 0 -> CrimsonRed
                    totalQty <= 3 -> OrangeWarning
                    else -> EmeraldGreen
                }
                val statusText = when {
                    totalQty <= 0 -> "نفد"
                    totalQty <= 3 -> "حرج"
                    else -> "جيد"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .border(1.dp, BorderColor.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(stock.item, color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("ع ${stock.karat}", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = stock.vitrineQty.toString(),
                        color = if (stock.vitrineQty > 0) AmberAccent else MutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(45.dp),
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )

                    Text(
                        text = stock.vaultQty.toString(),
                        color = LightText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(45.dp),
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )

                    Text(
                        text = amanaQty.toString(),
                        color = if (amanaQty > 0) CrimsonRed else MutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(45.dp),
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(4.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = totalQty.toString(),
                            color = LightText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            GoldButton(
                text = "📤 تصدير ومشاركة جرد المخزون الكلي",
                onClick = {
                    val reportText = buildString {
                        appendLine("📊 تقرير جرد مخزون المحل والخزنة")
                        appendLine("--------------------------")
                        appendLine("التاريخ: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}")
                        appendLine("--------------------------")
                        stockList.forEach { s ->
                            val amanaQty = amanaStockTotals[s.key] ?: 0
                            val totalQty = s.vitrineQty + s.vaultQty + amanaQty
                            appendLine("• ${s.item} (عيار ${s.karat}): فاترينة ${s.vitrineQty} | خزنة ${s.vaultQty} | أمانة $amanaQty | الإجمالي: $totalQty ق")
                        }
                        appendLine("--------------------------")
                        appendLine("تم الاستخراج والمطابقة بنجاح - نظام إدارة الصاغة")
                    }
                    com.example.utils.ShareUtils.shareText(context, "تقرير جرد المخزون", reportText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

// Custom drop down item creator helper
@Composable
fun dropdownMenuItem(text: @Composable () -> Unit, onClick: () -> Unit) {
    DropdownMenuItem(text = text, onClick = onClick)
}

// --- OWNER TAB 5: RECONCILIATION & DAILY REVIEW ---
@Composable
fun OwnerReviewTab(
    viewModel: GoldErpViewModel,
    stockList: List<StockEntity>
) {
    val context = LocalContext.current
    var searchReviewQuery by remember { mutableStateOf("") }
    var reviewLocation by remember { mutableStateOf("vitrine") } // vitrine, vault

    // Map to hold temporary physical counts
    val physicalCounts = remember { mutableStateMapOf<String, String>() }

    val filteredStock = remember(stockList, searchReviewQuery) {
        stockList.filter { s ->
            s.item.contains(searchReviewQuery, ignoreCase = true) ||
            s.karat.contains(searchReviewQuery)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Tab Row toggle for location review
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (reviewLocation == "vitrine") GoldPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { reviewLocation = "vitrine" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("جرد فاترينة العرض", color = if (reviewLocation == "vitrine") DarkBg else LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (reviewLocation == "vault") GoldPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { reviewLocation = "vault" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("جرد الخزنة والمخزن", color = if (reviewLocation == "vault") DarkBg else LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            GoldTextField(
                value = searchReviewQuery,
                onValueChange = { searchReviewQuery = it },
                label = "🔍 ابحث في الجرد اليومي",
                placeholder = "ابحث بالصنف..."
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("مطابقة الأرصدة للوصول إلى العجز", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                
                Text(
                    text = "✅ مطابقة كل المتبقي تلقائياً",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            filteredStock.forEach { stock ->
                                val sysQty = if (reviewLocation == "vitrine") stock.vitrineQty else stock.vaultQty
                                physicalCounts[stock.key] = sysQty.toString()
                            }
                        }
                        .padding(6.dp)
                )
            }
        }

        if (filteredStock.isEmpty()) {
            item {
                EmptyState(
                    icon = "⚠️",
                    title = "لا توجد بضاعة",
                    description = "لا توجد بضاعة مطابقة للجرد"
                )
            }
        } else {
            item {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الصنف/العيار", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("السيستم", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                    Text("الفعلي", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                    Text("الحالة", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                    Text("إجراء", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                }
            }

            items(filteredStock) { stock ->
                val sysQty = if (reviewLocation == "vitrine") stock.vitrineQty else stock.vaultQty
                val actVal = physicalCounts[stock.key] ?: ""
                val actQty = actVal.toIntOrNull()

                val rowColor = when {
                    actQty == null -> Color.Transparent
                    actQty == sysQty -> EmeraldGreen.copy(alpha = 0.05f)
                    actQty < sysQty -> CrimsonRed.copy(alpha = 0.05f)
                    else -> AmberAccent.copy(alpha = 0.05f)
                }

                val statusColor = when {
                    actQty == null -> MutedText
                    actQty == sysQty -> EmeraldGreen
                    actQty < sysQty -> CrimsonRed
                    else -> AmberAccent
                }
                
                val statusText = when {
                    actQty == null -> "-"
                    actQty == sysQty -> "مطابق"
                    actQty < sysQty -> "عجز ${sysQty - actQty}"
                    else -> "زيادة ${actQty - sysQty}"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowColor)
                        .border(1.dp, BorderColor.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Item Name
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stock.item, color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("ع ${stock.karat}", color = GoldPrimary, fontSize = 10.sp)
                    }

                    // System
                    Text(sysQty.toString(), color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)

                    // Actual
                    BasicTextField(
                        value = actVal,
                        onValueChange = { physicalCounts[stock.key] = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontFeatureSettings = "tnum"),
                        modifier = Modifier
                            .width(60.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(vertical = 8.dp)
                    )

                    // Status
                    Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)

                    // Action
                    Button(
                        onClick = {
                            val finalVal = physicalCounts[stock.key]?.toIntOrNull() ?: sysQty
                            viewModel.settleStockItem(stock.key, finalVal, reviewLocation)
                            Toast.makeText(context, "✅ تم التسوية", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.2f), contentColor = GoldPrimary),
                        modifier = Modifier.width(60.dp).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("تسوية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

          // --- OWNER TAB 6: REPORTS & METALS HEDGING ---
@Composable
fun OwnerReportsTab(
    viewModel: GoldErpViewModel,
    invoicesList: List<InvoiceEntity>,
    expensesList: List<ExpenseEntity>
) {
    val sellers by viewModel.allSellers.collectAsState()
    val categories by viewModel.allItems.collectAsState()
    val stockList by viewModel.allStock.collectAsState()
    val allInventoryMovements by viewModel.allInventoryMovements.collectAsState()
    val allJournalEntries by viewModel.allJournalEntries.collectAsState()
    val allAuditRecords by viewModel.allAuditRecords.collectAsState()

    var filterStartDate by remember { mutableStateOf("") }
    var filterEndDate by remember { mutableStateOf("") }
    var filterSeller by remember { mutableStateOf("الكل") }
    var filterBranch by remember { mutableStateOf("الكل") }
    var filterCategory by remember { mutableStateOf("الكل") }
    var filterKarat by remember { mutableStateOf("الكل") }

    // Selected Report Tab
    var selectedReportTab by remember { mutableStateOf("sales") } // sales, inventory, seller, expense, scrap, customer, audit, accounting

    val filteredInvoices = remember(invoicesList, filterStartDate, filterEndDate, filterSeller, filterBranch, filterCategory, filterKarat) {
        invoicesList.filter { inv ->
            // Date Range
            val dateStr = inv.date.substringBefore("T")
            val matchesStart = filterStartDate.isEmpty() || dateStr >= filterStartDate
            val matchesEnd = filterEndDate.isEmpty() || dateStr <= filterEndDate
            
            // Seller
            val matchesSeller = filterSeller == "الكل" || inv.seller == filterSeller
            
            // Branch (Simulated: All real data belongs to الفرع الرئيسي)
            val matchesBranch = filterBranch == "الكل" || filterBranch == "الفرع الرئيسي"
            
            // Items filter (Category & Karat)
            val items = try {
                JsonHelper.fromJson(inv.itemsJson)
            } catch (e: Exception) {
                emptyList<InvoiceItem>()
            }
            
            val matchesItems = if (inv.id.startsWith("INV")) {
                if (filterCategory == "الكل" && filterKarat == "الكل") {
                    true
                } else {
                    items.any { item ->
                        val catMatch = filterCategory == "الكل" || item.t == filterCategory
                        val karatMatch = filterKarat == "الكل" || item.k == filterKarat
                        catMatch && karatMatch
                    }
                }
            } else {
                // Scrap purchase direct
                if (filterCategory == "الكل") {
                    val karatMatch = filterKarat == "الكل" || inv.ogKarat.toString() == filterKarat
                    karatMatch
                } else {
                    false
                }
            }
            
            matchesStart && matchesEnd && matchesSeller && matchesBranch && matchesItems
        }
    }

    val filteredExpenses = remember(expensesList, filterStartDate, filterEndDate, filterSeller, filterBranch) {
        expensesList.filter { exp ->
            val dateStr = exp.date.substringBefore("T")
            val matchesStart = filterStartDate.isEmpty() || dateStr >= filterStartDate
            val matchesEnd = filterEndDate.isEmpty() || dateStr <= filterEndDate
            
            val matchesSeller = filterSeller == "الكل" || exp.seller == filterSeller
            val matchesBranch = filterBranch == "الكل" || filterBranch == "الفرع الرئيسي"
            
            matchesStart && matchesEnd && matchesSeller && matchesBranch
        }
    }

    val filteredStock = remember(stockList, filterCategory, filterKarat) {
        stockList.filter { stock ->
            val catMatch = filterCategory == "الكل" || stock.item == filterCategory
            val karatMatch = filterKarat == "الكل" || stock.karat == filterKarat
            catMatch && karatMatch
        }
    }

    // Calculations based on filtered lists
    var goldSold21Eq = 0.0
    var goldBought21Eq = 0.0
    var totalRevenue = 0.0
    var rawGoldCostValue = 0.0
    var totalExpenses = 0.0
    var cashInDrawer = 0.0
    var cardRevenue = 0.0
    var totalSalesQty = 0

    filteredInvoices.forEach { inv ->
        if (inv.id.startsWith("INV")) {
            val items = try { JsonHelper.fromJson(inv.itemsJson) } catch(e: Exception) { emptyList<InvoiceItem>() }
            var matchingItemRevenue = 0.0
            var matchingItemWeight = 0.0
            var matchingItemQty = 0

            items.forEach { item ->
                val catMatch = filterCategory == "الكل" || item.t == filterCategory
                val karatMatch = filterKarat == "الكل" || item.k == filterKarat
                if (catMatch && karatMatch) {
                    val k = item.k.toDoubleOrNull() ?: 21.0
                    goldSold21Eq += item.w * (k / 21.0)
                    rawGoldCostValue += item.w * (inv.p21 / 21.0) * k
                    matchingItemQty += item.q
                    matchingItemWeight += item.w
                }
            }

            if (filterCategory == "الكل" && filterKarat == "الكل") {
                totalRevenue += inv.tot
                cashInDrawer += inv.cash
                cardRevenue += inv.instapay + inv.vodafone + inv.visa
                totalSalesQty += inv.tq
            } else {
                if (inv.tw > 0) {
                    val ratio = matchingItemWeight / inv.tw
                    totalRevenue += inv.tot * ratio
                    cashInDrawer += inv.cash * ratio
                    cardRevenue += (inv.instapay + inv.vodafone + inv.visa) * ratio
                    totalSalesQty += matchingItemQty
                }
            }

            if (inv.hasOldGold) {
                val ogK = inv.ogKarat.toDouble()
                if (filterCategory == "الكل") {
                    goldBought21Eq += inv.ogWeight * (ogK / 21.0)
                }
            }
        } else if (inv.id.startsWith("BUY")) {
            val paid = Math.abs(inv.cash)
            cashInDrawer -= paid

            val ogK = inv.ogKarat.toDouble()
            goldBought21Eq += inv.ogWeight * (ogK / 21.0)
        }
    }

    filteredExpenses.forEach { exp ->
        totalExpenses += exp.amt
        cashInDrawer -= exp.amt
    }

    val netGoldBalance = goldSold21Eq - goldBought21Eq

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Advanced Filters Panel
        item {
            GoldCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🔍 لوحة التصفية المتقدمة لجميع التقارير", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    
                    // Row 1: Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoldTextField(
                            value = filterStartDate,
                            onValueChange = { filterStartDate = it },
                            label = "تاريخ البدء (YYYY-MM-DD)",
                            placeholder = "مثال: 2026-06-01",
                            modifier = Modifier.weight(1f)
                        )
                        GoldTextField(
                            value = filterEndDate,
                            onValueChange = { filterEndDate = it },
                            label = "تاريخ الانتهاء (YYYY-MM-DD)",
                            placeholder = "مثال: 2026-06-30",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Quick date helper buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        listOf(
                            "اليوم" to {
                                filterStartDate = todayStr
                                filterEndDate = todayStr
                            },
                            "هذا الشهر" to {
                                filterStartDate = todayStr.substring(0, 8) + "01"
                                filterEndDate = todayStr
                            },
                            "الكل" to {
                                filterStartDate = ""
                                filterEndDate = ""
                            }
                        ).forEach { (label, onClick) ->
                            Box(
                                modifier = Modifier
                                    .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                    .clickable { onClick() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(label, color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = BorderColor)

                    // Row 2: Seller, Branch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Seller filter
                        Column(modifier = Modifier.weight(1f)) {
                            Text("البائع", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            var showSellersMenu by remember { mutableStateOf(false) }
                            val sellersList = listOf("الكل") + sellers.map { it.name }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { showSellersMenu = !showSellersMenu }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(filterSeller, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("▼", color = GoldPrimary, fontSize = 10.sp)
                                }
                            }
                            if (showSellersMenu) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                    border = BorderStroke(1.dp, BorderColor),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column {
                                        sellersList.forEach { s ->
                                            Text(
                                                text = s,
                                                color = LightText,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        filterSeller = s
                                                        showSellersMenu = false
                                                    }
                                                    .padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Branch filter
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الفرع", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            var showBranchMenu by remember { mutableStateOf(false) }
                            val branches = listOf("الكل", "الفرع الرئيسي", "فرع التجمع", "فرع المهندسين")
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { showBranchMenu = !showBranchMenu }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(filterBranch, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("▼", color = GoldPrimary, fontSize = 10.sp)
                                }
                            }
                            if (showBranchMenu) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                    border = BorderStroke(1.dp, BorderColor),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column {
                                        branches.forEach { b ->
                                            Text(
                                                text = b,
                                                color = LightText,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        filterBranch = b
                                                        showBranchMenu = false
                                                    }
                                                    .padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Row 3: Category, Karat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category filter
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الصنف", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            var showCategoryMenu by remember { mutableStateOf(false) }
                            val catList = listOf("الكل") + categories.map { it.name }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { showCategoryMenu = !showCategoryMenu }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(filterCategory, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("▼", color = GoldPrimary, fontSize = 10.sp)
                                }
                            }
                            if (showCategoryMenu) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                    border = BorderStroke(1.dp, BorderColor),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column {
                                        catList.forEach { c ->
                                            Text(
                                                text = c,
                                                color = LightText,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        filterCategory = c
                                                        showCategoryMenu = false
                                                    }
                                                    .padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Karat filter
                        Column(modifier = Modifier.weight(1f)) {
                            Text("العيار", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            var showKaratMenu by remember { mutableStateOf(false) }
                            val karatList = listOf("الكل", "21", "18", "24")
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { showKaratMenu = !showKaratMenu }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(filterKarat, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("▼", color = GoldPrimary, fontSize = 10.sp)
                                }
                            }
                            if (showKaratMenu) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                    border = BorderStroke(1.dp, BorderColor),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column {
                                        karatList.forEach { k ->
                                            Text(
                                                text = k,
                                                color = LightText,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        filterKarat = k
                                                        showKaratMenu = false
                                                    }
                                                    .padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sub-reports tab buttons
        item {
            val reportTabs = listOf(
                "sales" to "💰 مبيعاتنا",
                "inventory" to "📦 بضاعتنا",
                "seller" to "👨‍💼 البياعين",
                "expense" to "💸 المصاريف",
                "scrap" to "⚖️ الكسر المشتري",
                "customer" to "👥 الزباين",
                "audit" to "🔍 الحركات",
                "accounting" to "🏛️ الحسابات"
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reportTabs) { tab ->
                    val isSelected = selectedReportTab == tab.first
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) GoldPrimary else DarkSurfaceElevated,
                                RoundedCornerShape(24.dp)
                            )
                            .border(1.dp, if (isSelected) GoldPrimary else BorderColor, RoundedCornerShape(24.dp))
                            .clickable { selectedReportTab = tab.first }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab.second,
                            color = if (isSelected) DarkBg else LightText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Selected report rendering
        if (selectedReportTab == "sales") {
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📊 تقرير المبيعات المفصل", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("إجمالي المبيعات المفلترة: ${Math.round(totalRevenue).toLocaleString()} ج", color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("إجمالي عدد القطع: $totalSalesQty", color = MutedText, fontSize = 12.sp)
                        Text("إجمالي وزن الذهب ع21 الصافي: ${String.format(Locale.US, "%.2f", goldSold21Eq)} جم", color = MutedText, fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        val salesInvoices = filteredInvoices.filter { it.id.startsWith("INV") }
                        if (salesInvoices.isEmpty()) {
                            Text("لا توجد مبيعات تطابق شروط التصفية الحالية", color = MutedText, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            salesInvoices.forEach { inv ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("فاتورة: ${inv.id}", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("التاريخ: ${inv.date.substringBefore("T")}", color = MutedText, fontSize = 10.sp)
                                        Text("البائع: ${inv.seller} | العميل: ${if (inv.customer.isEmpty()) "عميل عام" else inv.customer}", color = MutedText, fontSize = 10.sp)
                                    }
                                    Text("${Math.round(inv.tot).toLocaleString()} ج", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = BorderColor.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        if (selectedReportTab == "inventory") {
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📦 تقرير المخزون الحالي", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (filteredStock.isEmpty()) {
                            Text("لا توجد بضاعة تطابق شروط التصفية الحالية", color = MutedText, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("الصنف / العيار", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                Text("الفاترينة", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("الخزنة", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            Divider(color = BorderColor)
                            filteredStock.forEach { stock ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${stock.item} (عيار ${stock.karat})", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                    Text("${stock.vitrineQty} قطع", color = AmberAccent, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Text("${stock.vaultQty} قطع", color = LightText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                }
                                Divider(color = BorderColor.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Inventory Movements Log
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📋 سجل حركات المخزون والتبديل (Inventory Movements Log)", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (allInventoryMovements.isEmpty()) {
                            Text(
                                "لا توجد حركات مخزون مسجلة حالياً.",
                                color = MutedText,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            allInventoryMovements.sortedByDescending { it.date }.forEach { movement ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val typeColor = when (movement.movementType) {
                                            "Sale" -> GoldPrimary
                                            "Purchase" -> EmeraldGreen
                                            "Consignment" -> SapphireBlue
                                            "Transfer" -> AmberAccent
                                            else -> LightText
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(movement.movementType, color = typeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        val formattedDate = try {
                                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                            parser.parse(movement.date)?.let { formatter.format(it) } ?: movement.date
                                        } catch (e: Exception) {
                                            movement.date
                                        }
                                        Text(formattedDate, color = MutedText, fontSize = 9.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("الصنف: ${movement.itemType} (عيار ${movement.karat})", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(movement.notes, color = MutedText, fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("الكمية: ${movement.qty} قطع", color = LightText, fontSize = 10.sp)
                                        Text("الوزن: ${movement.weight} جرام", color = LightText, fontSize = 10.sp)
                                        Text("من: ${movement.fromLoc} ➔ إلى: ${movement.toLoc}", color = AmberAccent, fontSize = 10.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        if (selectedReportTab == "seller") {
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👤 تقرير أداء البائعين والموظفين", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        val activeSellers = if (filterSeller == "الكل") sellers.map { it.name } else listOf(filterSeller)
                        
                        if (activeSellers.isEmpty()) {
                            Text("لا يوجد بائعون مسجلون", color = MutedText, fontSize = 12.sp)
                        } else {
                            activeSellers.forEach { sName ->
                                val sInvs = filteredInvoices.filter { it.seller == sName && it.id.startsWith("INV") }
                                val sExps = filteredExpenses.filter { it.seller == sName }
                                val sTotalSalesAmt = sInvs.sumOf { it.tot }
                                val sTotalSalesWeight = sInvs.sumOf { it.tw }
                                
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text("👤 البائع: $sName", color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("عدد فواتير البيع: ${sInvs.size}", color = MutedText, fontSize = 11.sp)
                                        Text("إجمالي المبيعات: ${Math.round(sTotalSalesAmt).toLocaleString()} ج", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("إجمالي الجرامات المباعة: ${String.format(Locale.US, "%.2f", sTotalSalesWeight)} جم", color = MutedText, fontSize = 11.sp)
                                        Text("عدد المصاريف المسجلة: ${sExps.size}", color = MutedText, fontSize = 11.sp)
                                    }
                                    Divider(color = BorderColor, modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedReportTab == "expense") {
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💸 تقرير المصروفات المفرزة", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("إجمالي المصاريف: ${Math.round(totalExpenses).toLocaleString()} ج", color = CrimsonRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredExpenses.isEmpty()) {
                            Text("لا توجد مصروفات تطابق شروط التصفية", color = MutedText, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            filteredExpenses.forEach { exp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(exp.desc, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("التاريخ: ${exp.date.substringBefore("T")} | بواسطة: ${exp.seller}", color = MutedText, fontSize = 10.sp)
                                    }
                                    Text("-${Math.round(exp.amt).toLocaleString()} ج", color = CrimsonRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = BorderColor.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        if (selectedReportTab == "scrap") {
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚖️ تقرير شراء كسر والذهب القديم (المقايضات)", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("إجمالي جرامات الكسر الواردة: ${String.format(Locale.US, "%.2f", goldBought21Eq)} جم ع21", color = AmberAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        val scrapInvs = filteredInvoices.filter { it.hasOldGold || it.id.startsWith("BUY") }
                        if (scrapInvs.isEmpty()) {
                            Text("لا توجد حركات شراء كسر تطابق شروط التصفية الحالية", color = MutedText, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            scrapInvs.forEach { inv ->
                                val isDirect = inv.id.startsWith("BUY")
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(if (isDirect) "شراء كسر مباشر" else "كسر مستلم بفاتورة بيع", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("التاريخ: ${inv.date.substringBefore("T")} | البائع: ${inv.seller}", color = MutedText, fontSize = 10.sp)
                                        Text("الوزن: ${inv.ogWeight} جم | عيار: ${inv.ogKarat}", color = AmberAccent, fontSize = 11.sp)
                                    }
                                    Text("${Math.round(inv.ogValue).toLocaleString()} ج", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = BorderColor.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        if (selectedReportTab == "customer") {
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👥 تقرير العملاء وسجل المشتريات", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        val customersList = filteredInvoices
                            .filter { it.id.startsWith("INV") && it.customer.isNotEmpty() }
                            .groupBy { it.customer }

                        if (customersList.isEmpty()) {
                            Text("لا توجد بيانات عملاء مسجلة تطابق شروط التصفية", color = MutedText, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            customersList.forEach { (cName, cInvs) ->
                                val cTotalSpend = cInvs.sumOf { it.tot }
                                val cTotalWeight = cInvs.sumOf { it.tw }
                                val phone = cInvs.firstOrNull { it.phone.isNotEmpty() }?.phone ?: "غير متوفر"
                                
                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("👤 العميل: $cName", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("رقم الهاتف: $phone", color = MutedText, fontSize = 10.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${Math.round(cTotalSpend).toLocaleString()} ج", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("الوزن: ${String.format(Locale.US, "%.2f", cTotalWeight)} جم", color = MutedText, fontSize = 10.sp)
                                        }
                                    }
                                    Divider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedReportTab == "audit") {
            item {
                GoldCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🔍 سجل التدقيق والجرد العملياتي الموحد (Audit Trail)", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("التدقيق الأمني الفوري لجميع الحركات:", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val totalTransactions = allAuditRecords.size
                        Text("إجمالي العمليات المدققة تلقائياً: $totalTransactions حركة عملية", color = LightText, fontSize = 12.sp)
                        Text("مستوى التشفير والمزامنة الحالية: نظام محلي مشفر ومطابق لمعايير النزاهة المالية", color = MutedText, fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("سياسة عدم الحذف والتعديل:", color = CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("جميع الحركات أدناه مسجلة ببصمة النظام والمسؤول بصيغة غير قابلة للتعديل لضمان منعه من التلاعب ببيانات الدرج والفاترينة والذهب الكسر.", color = MutedText, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }

            if (allAuditRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد سجلات تدقيق عملي بعد. قم بإجراء معاملات بالصالة لتوليد سجلات أمنية تلقائية.", color = MutedText, fontSize = 12.sp)
                    }
                }
            } else {
                items(allAuditRecords.sortedByDescending { it.date }) { record ->
                    GoldCard(
                        modifier = Modifier.padding(vertical = 4.dp),
                        borderColor = BorderColor
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(record.action, color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("بواسطة: ${record.actor}", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                val formattedDate = try {
                                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    parser.parse(record.date)?.let { formatter.format(it) } ?: record.date
                                } catch (e: Exception) {
                                    record.date
                                }
                                Text(formattedDate, color = MutedText, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("قبل الإجراء: ${record.detailsBefore}", color = MutedText, fontSize = 11.sp, lineHeight = 15.sp)
                            Text("بعد الإجراء: ${record.detailsAfter}", color = LightText, fontSize = 11.sp, lineHeight = 15.sp)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = BorderColor.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("💻 الجهاز: ${record.device}", color = MutedText, fontSize = 9.sp)
                                Text("🌐 IP: ${record.ip}", color = MutedText, fontSize = 9.sp)
                                Text("📍 الموقع: ${record.location}", color = MutedText, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        if (selectedReportTab == "accounting") {
            // Gold balance analysis header
            item {
                val netColor = if (netGoldBalance > 0.0) CrimsonRed else EmeraldGreen
                val netTitle = if (netGoldBalance > 0.0) "نقص صافي الذهب (تم بيع ذهب أكثر مما تم شراؤه كسر)" else "زيادة ذهب مستلم كسر بالمخازن"
                
                GoldCard(borderColor = netColor) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪙 ميزانية ومطابقة الجرامات (بمعيار عيار ٢١ الصافي)", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("إجمالي المباع (جرام)", color = MutedText, fontSize = 10.sp)
                                Text(String.format(Locale.US, "%.2f", goldSold21Eq), color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("إجمالي الكسر المشترى (جرام)", color = MutedText, fontSize = 10.sp)
                                Text(String.format(Locale.US, "%.2f", goldBought21Eq), color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                        Text(netTitle, color = netColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f", Math.abs(netGoldBalance)) + " جرام عيار ٢١",
                            color = netColor,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Metal Hedging analysis calculator when gold is in deficit
            if (netGoldBalance > 0.0) {
                item {
                    val targetHedgePrice = Math.round(rawGoldCostValue / netGoldBalance)
                    val maxHedgePrice = Math.round(cashInDrawer / netGoldBalance)

                    GoldCard(borderColor = CrimsonRed) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🛡️ حاسبة حماية وتعويض العجز (Hedge Calculator)", color = CrimsonRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("لديك نقص في الجرامات — إليك الأسعار المرجعية لشراء ذهب كسر لتعويض النقص:", color = MutedText, fontSize = 11.sp, lineHeight = 16.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("🎯 السعر المستهدف (لتعويض الذهب الخام)", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("شراء الذهب بهذا السعر يحميك من الخسارة", color = MutedText, fontSize = 10.sp)
                                }
                                Text("$targetHedgePrice ج", color = EmeraldGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("💵 السعر الأقصى الممكن (حسب الكاش المتاح)", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("تجاوز هذا السعر يسبب عجز مالي نقدي", color = MutedText, fontSize = 10.sp)
                                }
                                Text("$maxHedgePrice ج", color = CrimsonRed, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }

                            if (maxHedgePrice < targetHedgePrice) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠️ تحذير: الكاش المتاح بالدرج حالياً لا يكفي لإعادة شراء الذهب الخام المباع بالكامل. العجز المالي النقدي: ${Math.round(rawGoldCostValue - cashInDrawer).toLocaleString()} ج",
                                    color = CrimsonRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Financial report breakdown summary
            item {
                GoldCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("الملخص والتقارير المالية الصافية للفترة المحددة", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي مبيعات المحل وارد:", color = MutedText, fontSize = 13.sp)
                            Text("${Math.round(totalRevenue).toLocaleString()} ج", color = LightText, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي الكاش بالدرج وارد:", color = MutedText, fontSize = 13.sp)
                            Text("${Math.round(cashInDrawer).toLocaleString()} ج", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي المحصل فيزا/انستاباي/فودافون:", color = MutedText, fontSize = 13.sp)
                            Text("${Math.round(cardRevenue).toLocaleString()} ج", color = LightText, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي المصاريف المسحوبة:", color = MutedText, fontSize = 13.sp)
                            Text("-${Math.round(totalExpenses).toLocaleString()} ج", color = CrimsonRed, fontWeight = FontWeight.Bold)
                        }

                        Divider(color = BorderColor)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("صافي ربح المصنعية المتوقع:", color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${Math.round(totalRevenue - rawGoldCostValue).toLocaleString()} ج", color = GoldPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Double-Entry Accounting Journal Ledger
            item {
                GoldCard {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("📋 دفتر اليومية وقيود الحسابات المزدوجة (Financial Ledger)", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (allJournalEntries.isEmpty()) {
                            Text(
                                "لا توجد قيود دفتر يومية مسجلة حالياً بالمنظومة.",
                                color = MutedText,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            allJournalEntries.sortedByDescending { it.date }.forEach { entry ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("رقم القيد: ${entry.id}", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        val formattedDate = try {
                                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                            parser.parse(entry.date)?.let { formatter.format(it) } ?: entry.date
                                        } catch (e: Exception) {
                                            entry.date
                                        }
                                        Text(formattedDate, color = MutedText, fontSize = 9.sp)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(entry.description, color = LightText, fontSize = 11.sp)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🟢 المدين (Debit):", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(entry.debitAccount, color = LightText, fontSize = 11.sp)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🔴 الدائن (Credit):", color = CrimsonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(entry.creditAccount, color = LightText, fontSize = 11.sp)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = BorderColor.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("البائع: ${entry.seller}", color = MutedText, fontSize = 10.sp)
                                        Text(
                                            text = "${Math.round(entry.amount).toLocaleString()} ج",
                                            color = GoldPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- OWNER TAB 7: ADMINISTRATIVE SETTINGS ---
@Composable
fun OwnerSettingsTab(
    viewModel: GoldErpViewModel,
    sellers: List<SellerEntity>,
    items: List<ItemEntity>
) {
    AdvancedSettingsScreen(viewModel, sellers, items)
}

