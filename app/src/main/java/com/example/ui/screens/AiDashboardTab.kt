package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GoldErpViewModel
import com.example.data.generateAIContent
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiDashboardTab(viewModel: GoldErpViewModel) {
    var query by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("أهلاً بيك يا باشا في مساعد الذكاء الاصطناعي. اسألني عن المبيعات، المخزون، أو أي حاجة في المحل وهديك الخلاصة.") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val allInvoices by viewModel.allInvoices.collectAsState()
    val allStock by viewModel.allStock.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    
    val todayInvoices = allInvoices.filter { it.date.startsWith(today) && it.id.startsWith("INV") }
    val todaySales = todayInvoices.sumOf { it.tot }
    val todayWeight = todayInvoices.sumOf { it.tw }
    
    val totalStockItems = allStock.sumOf { (it.vitrineQty + it.vaultQty).toInt() }

    val salesText = "مبيعات النهارده: ${todaySales} جنيه (وزن تقريبي مباع: ${todayWeight} جرام). عدد الفواتير: ${todayInvoices.size}."
    val itemsText = "المخزون الحالي في الفاترينة والخزنة: ${totalStockItems} حتة. إجمالي المصروفات النهارده: ${allExpenses.filter{it.date.startsWith(today)}.sumOf{it.amt}} جنيه."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = "AI", tint = GoldPrimary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "المساعد الذكي للأعمال (AI)",
                color = GoldPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("مبيعات اليوم", color = LightText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${Math.round(todaySales)} ج", color = EmeraldGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            GoldCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("إجمالي القطع", color = LightText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${totalStockItems} قطعة", color = SapphireBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Chat / Response Area
        GoldCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                if (isLoading) {
                    CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("بحلل البيانات وبحضرلك الرد...", color = MutedText, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Text(aiResponse, color = LightText, fontSize = 15.sp, lineHeight = 24.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("اسأل الذكاء الاصطناعي... (مثال: إيه الأخبار النهارده؟)", color = MutedText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        isLoading = true
                        val prompt = "Context:\\n$salesText\\n$itemsText\\n\\nUser Query: $query"
                        coroutineScope.launch {
                            val result = generateAIContent(prompt)
                            aiResponse = result
                            isLoading = false
                            query = ""
                        }
                    }
                },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = DarkBg)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            SuggestionChip("تحليل المبيعات", { query = "حلل مبيعاتي واقترح طرق لزيادتها" })
            SuggestionChip("نواقص المخزون", { query = "ما هي النواقص التي يجب أن أطلبها؟" })
            SuggestionChip("تقييم البائعين", { query = "من هو أفضل بائع هذا الشهر؟" })
        }
    }
}

@Composable
fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        onClick = onClick
    ) {
        Text(text, color = LightText, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}
