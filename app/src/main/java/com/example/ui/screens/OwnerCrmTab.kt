package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InvoiceEntity
import com.example.ui.GoldErpViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LightText
import com.example.ui.theme.MutedText
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OwnerCrmTab(
    viewModel: GoldErpViewModel,
    invoices: List<InvoiceEntity>
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ar", "EG"))
    
    // Process unique customers
    val customerMap = invoices.groupBy { it.customer }
    val customerStats = customerMap.mapNotNull { (name, invList) ->
        if (name.isBlank() || name == "عميل نقدي" || name == "Cash Customer") return@mapNotNull null
        
        val totalSpent = invList.sumOf { it.tot }
        val phone = invList.firstOrNull { it.phone.isNotBlank() }?.phone ?: "غير متوفر"
        val firstDate = invList.minOfOrNull { it.date } ?: ""
        val lastDate = invList.maxOfOrNull { it.date } ?: ""
        
        CustomerStat(
            name = name,
            phone = phone,
            invoiceCount = invList.size,
            totalSpent = totalSpent,
            firstVisit = firstDate,
            lastVisit = lastDate
        )
    }.sortedByDescending { it.totalSpent }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "إدارة علاقات العملاء (CRM)",
            color = GoldPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (customerStats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد بيانات عملاء مسجلة حالياً.", color = MutedText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customerStats) { stat ->
                    CustomerCard(stat, currencyFormatter)
                }
            }
        }
    }
}

@Composable
fun CustomerCard(stat: CustomerStat, formatter: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "👤 ${stat.name}", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = formatter.format(stat.totalSpent),
                    color = GoldSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "📱 الهاتف: ${stat.phone}", color = MutedText, fontSize = 14.sp)
                Text(text = "🛒 عدد الفواتير: ${stat.invoiceCount}", color = MutedText, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "تاريخ أول زيارة: ${stat.firstVisit}", color = MutedText, fontSize = 12.sp)
                Text(text = "آخر زيارة: ${stat.lastVisit}", color = MutedText, fontSize = 12.sp)
            }
        }
    }
}

data class CustomerStat(
    val name: String,
    val phone: String,
    val invoiceCount: Int,
    val totalSpent: Double,
    val firstVisit: String,
    val lastVisit: String
)
