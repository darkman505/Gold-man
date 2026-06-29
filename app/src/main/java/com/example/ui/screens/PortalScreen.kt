package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PortalScreen(
    onNavigateToSeller: () -> Unit,
    onNavigateToOwner: () -> Unit
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    val currentDateString = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar", "EG"))
        sdf.format(Date())
    }

    ArabicRtlLayout {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBg, Color(0xFF080808), Color(0xFF141108))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GOLD ERP ENTERPRISE",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF22C55E), CircleShape)
                        )
                        Text(
                            text = "فرع الصاغة الرئيسي",
                            color = MutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1C1C1C), Color(0xFF0F0F0F))
                            ),
                            CircleShape
                        )
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABA",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Luxury Logo with Gold Glow effect
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(DarkSurfaceElevated, CircleShape)
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💎",
                        fontSize = 50.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "نظام إدارة الصاغة والمجوهرات",
                    color = LightText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "GOLD ERP ENTERPRISE",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentDateString,
                    color = MutedText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Portal Options Cards
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isMobile = maxWidth < 560.dp
                    if (isMobile) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Seller Card
                            PortalCard(
                                title = "شاشة البيع",
                                description = "للبياعين — فواتير البيع والمصروفات والجر والمعاينة وشراء الكسر اليومي",
                                icon = "🛒",
                                buttonText = "دخول شاشة البيع",
                                onClick = onNavigateToSeller,
                                fillMaxHeight = false,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Owner Card
                            PortalCard(
                                title = "لوحة المعلم",
                                description = "للمالك — المراقبة والجرد المالي والتقارير وإعدادات المحل والمخزن",
                                icon = "👑",
                                buttonText = "دخول منطقة الإدارة",
                                onClick = { showPasswordDialog = true },
                                fillMaxHeight = false,
                                modifier = Modifier.fillMaxWidth(),
                                isPrimary = false
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Seller Card
                            PortalCard(
                                title = "شاشة البيع",
                                description = "للبياعين — فواتير البيع والمصروفات والجر والمعاينة وشراء الكسر اليومي",
                                icon = "🛒",
                                buttonText = "دخول شاشة البيع",
                                onClick = onNavigateToSeller,
                                modifier = Modifier.weight(1f)
                            )

                            // Owner Card
                            PortalCard(
                                title = "لوحة المعلم",
                                description = "للمالك — المراقبة والجرد المالي والتقارير وإعدادات المحل والمخزن",
                                icon = "👑",
                                buttonText = "دخول منطقة الإدارة",
                                onClick = { showPasswordDialog = true },
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Guide Trigger
                Text(
                    text = "📘 قراءة دليل تشغيل نظام إدارة الصاغة",
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { showGuideDialog = true }
                        .padding(8.dp)
                )
            }

            // Footer
            Text(
                text = "© نظام إدارة الصاغة — يعمل محلياً وآمن 100%",
                color = MutedText,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        // Passcode Input Dialog for Owner Portal
        if (showPasswordDialog) {
            Dialog(onDismissRequest = {
                showPasswordDialog = false
                passwordInput = ""
                passwordError = false
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface,
                        contentColor = LightText
                    ),
                    border = BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔐 منطقة المعلم",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "الرجاء إدخال الرقم السري للمالك لتأكيد الهوية والدخول:",
                            fontSize = 14.sp,
                            color = LightText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("••••••••", color = MutedText) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldPrimary.copy(alpha = 0.4f),
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurface,
                                cursorColor = GoldPrimary
                            )
                        )

                        if (passwordError) {
                            Text(
                                text = "❌ كلمة المرور غير صحيحة",
                                color = CrimsonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (passwordInput == "123") {
                                        showPasswordDialog = false
                                        passwordInput = ""
                                        passwordError = false
                                        onNavigateToOwner()
                                    } else {
                                        passwordError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("دخول", color = DarkBg, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    showPasswordDialog = false
                                    passwordInput = ""
                                    passwordError = false
                                },
                                border = BorderStroke(1.dp, MutedText),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء", color = LightText)
                            }
                        }
                    }
                }
            }
        }

        // User Guide Dialog Modal
        if (showGuideDialog) {
            Dialog(onDismissRequest = { showGuideDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface,
                        contentColor = LightText
                    ),
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
                            Text(
                                text = "💎 دليل تشغيل نظام إدارة الصاغة",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldPrimary
                            )
                            IconButton(onClick = { showGuideDialog = false }) {
                                Text("✕", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            GuideSection(
                                title = "💡 فكرة النظام ببساطة",
                                content = "السيستم ده معمول عشان يريح دماغك ويضبطلك حسابات المحل والذهب والفلوس من غير أي لغبطة. بيحفظ حق المعلم والبياع وبيطلعلك الصافي كل يوم بدوسة زرار."
                            )
                            GuideSection(
                                title = "👨‍💼 مين بيشتغل على النظام؟",
                                content = "• شاشة صاحب المحل (الكنترول): دي الإدارة كلها. بتشوف منها كل اللي بيحصل في المحل (بيع، شراء، مصاريف)، بتضيف منها بضاعة، بتعمل جرد للفاترينة والخزنة، وتعرف ميزانيتك ومكسبك كام بالضبط.\n\n• شاشة البياع (الوردية): دي شاشة البيع العادية للبياع. بيعمل منها الفواتير، بيشتري كسر، بيسجل أي فلوس طلعها للمصاريف، وبيشوف درج الكاش بتاعه فيه كام عشان يسلم ورديته مظبوط."
                            )
                            GuideSection(
                                title = "📝 شاشة البياع (يوميات المحل)",
                                content = "1. فاتورة بيع 💰: هنا البياع بيسجل البضاعة المباعة، بيكتب المصنعية، وبيقدر ياخد ذهب كسر من الزبون، وبيسجل الدفع كاش أو انستا باي أو فيزا.\n\n2. شراء كسر ⚖️: لو زبون داخل يبيع كسر بس، بنسجله هنا عشان نخصم كاش من الدرج ونزود وزن الكسر عندنا.\n\n3. دفتر الجر 🔍: دي الأمانة أو لو طلعنا حتة لفرع تاني أو ورشة أو زبون بيبص عليها.\n\n4. المصاريف 💸: بوفيه، نضافة، شاي، أي قرش يطلع من الدرج لازم يتكتب عشان يتخصم من عهدة البياع.\n\n5. درج البياع 💵: بيعرف البياع معاه كام كاش صافي في الدرج وكام كسر استلمه في ورديته."
                            )
                            GuideSection(
                                title = "👑 شاشة صاحب المحل (الجرد والإدارة)",
                                content = "1. الرئيسية 🏠: دي الخلاصة، مبيعات النهارده، المصاريف، الفلوس اللي في الدرج، وإجمالي الكسر.\n\n2. الذكاء الاصطناعي ✨: مساعدك الآلي، اسأله عن مبيعات المحل وهيفهمك كل حاجة بلغة بسيطة.\n\n3. الخزنة والجرد 💎: بتضيف منها بضاعة جديدة للمحل، وبترص بضاعتك من الخزنة للفاترينة، وكمان بتكتب الجرد الفعلي آخر اليوم عشان السيستم يطابق ويقولك لو فيه عجز أو زيادة.\n\n4. الدفاتر والتقارير 🪙: هنا بتشوف الفواتير كلها وتقدر تطبعها، وكمان بتشوف ميزانية الجرامات وتحسب مكسبك (Hedge) وتحول بيعك لعيار 21 صافي.\n\n5. العملاء 👥: بتسجل البياعين اللي شغالين في المحل وتتابعهم."
                            )
                            GuideSection(
                                title = "🔄 تمشي يومك إزاي؟",
                                content = "• الصبح: صاحب المحل أو المدير بيظبط السعر.\n• طول اليوم: البياعين بتسجل أي حركة فواتير أو كسر أو مصاريف في وقتها.\n• آخر الوردية: البياع بيعد الكاش اللي في الدرج يطابقه مع شاشته.\n• آخر اليوم: صاحب المحل بيجرد ويقفل يوميته، بياخد نسخة احتياطية من السيستم وهو خارج عشان الداتا متضيعش."
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        NeutralButton(text = "إغلاق الدليل", onClick = { showGuideDialog = false })
                    }
                }
            }
        }
    }
}

@Composable
fun PortalCard(
    title: String,
    description: String,
    icon: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    fillMaxHeight: Boolean = true
) {
    Card(
        modifier = modifier
            .then(if (fillMaxHeight) Modifier.fillMaxHeight(0.6f) else Modifier)
            .heightIn(min = 180.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface,
            contentColor = LightText
        ),
        border = BorderStroke(1.dp, if (isPrimary) GoldPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillMaxHeight) Modifier.fillMaxHeight() else Modifier.wrapContentHeight())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 44.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            if (isPrimary) {
                PrimaryButton(
                    text = buttonText,
                    onClick = onClick
                )
            } else {
                SecondaryButton(
                    text = buttonText,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun GuideSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            color = GoldPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            color = LightText.copy(alpha = 0.9f),
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = BorderColor)
    }
}
