package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InvoiceEntity
import com.example.ui.theme.*

@Composable
fun WeeklySalesChart(invoices: List<InvoiceEntity>) {
    // A simple professional bar chart for weekly sales
    GoldCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "المبيعات الأسبوعية",
                color = LightText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Calculate mock or real weekly data here
            // For now, we will draw a professional visual representation
            val heights = listOf(0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.9f, 0.7f)
            val days = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                heights.forEachIndexed { index, fraction ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(fraction)
                                .background(GoldPrimary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = days[index],
                            color = MutedText,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryDistributionChart() {
    GoldCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "توزيع المخزون حسب العيار",
                color = LightText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val strokeWidth = 30f
                    // Draw 21k
                    drawArc(
                        color = GoldPrimary,
                        startAngle = -90f,
                        sweepAngle = 200f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Draw 18k
                    drawArc(
                        color = MutedText,
                        startAngle = 120f,
                        sweepAngle = 100f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Draw 24k
                    drawArc(
                        color = EmeraldGreen,
                        startAngle = 230f,
                        sweepAngle = 30f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("المخزون", color = MutedText, fontSize = 12.sp)
                    Text("100%", color = LightText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartLegendItem(color = GoldPrimary, label = "عيار 21")
                ChartLegendItem(color = MutedText, label = "عيار 18")
                ChartLegendItem(color = EmeraldGreen, label = "عيار 24")
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = LightText, fontSize = 12.sp)
    }
}
