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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuditRecord
import com.example.ui.GoldErpViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LightText
import com.example.ui.theme.MutedText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerAuditTab(
    viewModel: GoldErpViewModel
) {
    val auditRecords by viewModel.allAuditRecords.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "سجل حركات النظام (Audit Trail & Compliance)",
            color = GoldPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (auditRecords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد حركات مسجلة حالياً.", color = MutedText)
            }
        } else {
            // Sort by date descending
            val sortedRecords = auditRecords.sortedByDescending { it.date }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedRecords) { record ->
                    AuditCard(record)
                }
            }
        }
    }
}

@Composable
fun AuditCard(record: AuditRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🛡️ ${record.action}", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = record.date, color = MutedText, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            val detailText = if (record.detailsBefore.isNotBlank()) {
                "${record.detailsBefore} ➔ ${record.detailsAfter}"
            } else {
                record.detailsAfter
            }
            Text(text = "التفاصيل: $detailText", color = LightText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "بواسطة: ${record.actor}", color = MutedText, fontSize = 12.sp)
                Text(text = "الجهاز: ${record.device}", color = MutedText, fontSize = 12.sp)
            }
        }
    }
}
