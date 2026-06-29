package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GoldErpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GoldErpRepository
    
    val allInvoices: StateFlow<List<InvoiceEntity>>
    val allExpenses: StateFlow<List<ExpenseEntity>>
    val allAmanas: StateFlow<List<AmanaEntity>>
    val allStock: StateFlow<List<StockEntity>>
    val allSellers: StateFlow<List<SellerEntity>>
    val allItems: StateFlow<List<ItemEntity>>
    val allInventoryMovements: StateFlow<List<InventoryMovement>>
    val allJournalEntries: StateFlow<List<JournalEntry>>
    val allAuditRecords: StateFlow<List<AuditRecord>>

    var goldPrice21 = mutableStateOf(3500.0)
    var usdRate = mutableStateOf(50.5)

    init {
        val database = GoldErpDatabase.getDatabase(application)
        repository = GoldErpRepository(database.dao())

        allInvoices = repository.allInvoices.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allAmanas = repository.allAmanas.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allStock = repository.allStock.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allSellers = repository.allSellers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allItems = repository.allItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allInventoryMovements = repository.allInventoryMovements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allJournalEntries = repository.allJournalEntries.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allAuditRecords = repository.allAuditRecords.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        viewModelScope.launch {
            repository.prepopulateIfNeeded()
            checkAndSaveOpeningStock()
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private suspend fun checkAndSaveOpeningStock() {
        val today = getTodayDateString()
        val existing = repository.getOpeningStock(today)
        if (existing == null) {
            val currentStock = repository.allStock.first()
            val vitrineStockMap = currentStock.associate { it.key to it.vitrineQty }
            // serialize map
            val moshi = com.squareup.moshi.Moshi.Builder().build()
            val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Int>>(type)
            val json = adapter.toJson(vitrineStockMap)
            repository.insertOpeningStock(OpeningStockEntity(today, json))
        }
    }

    fun addInvoice(
        seller: String,
        customer: String,
        phone: String,
        items: List<InvoiceItem>,
        tw: Double,
        tq: Int,
        tot: Double,
        fare: Double,
        p21: Double,
        cash: Double,
        instapay: Double,
        vodafone: Double,
        visa: Double,
        ewallet: Double,
        notes: String,
        hasOldGold: Boolean,
        ogKarat: Int,
        ogWeight: Double,
        ogPrice: Double,
        ogValue: Double,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val id = "INV" + System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())

                val itemsJson = JsonHelper.toJson(items)

                // Deduct stock from vitrine
                val currentStock = repository.allStock.first().associateBy { it.key }
                items.forEachIndexed { index, item ->
                    val key = "${item.t}-${item.k}"
                    val existing = currentStock[key]
                    if (existing != null) {
                        val newVitrineQty = maxOf(0, existing.vitrineQty - item.q)
                        repository.insertStock(existing.copy(vitrineQty = newVitrineQty))
                    } else {
                        // Insert new stock entity with negative (or 0)
                        repository.insertStock(StockEntity(key, item.t, item.k, 0, 0))
                    }

                    // 1. Inventory Engine: Insert InventoryMovement record
                    repository.insertInventoryMovement(
                        InventoryMovement(
                            id = "MVT_${id}_$index",
                            date = date,
                            itemKey = key,
                            itemType = item.t,
                            karat = item.k,
                            movementType = "Sale",
                            qty = item.q,
                            weight = item.w,
                            fromLoc = "showroom",
                            toLoc = "customer",
                            seller = seller,
                            notes = "بيع بفاتورة $id"
                        )
                    )
                }

                // Trade-In Gold inventory movement
                if (hasOldGold && ogWeight > 0.0) {
                    repository.insertInventoryMovement(
                        InventoryMovement(
                            id = "MVT_${id}_OG",
                            date = date,
                            itemKey = "كسر-${ogKarat}",
                            itemType = "ذهب كسر مستلم",
                            karat = ogKarat.toString(),
                            movementType = "Trade-In",
                            qty = 1,
                            weight = ogWeight,
                            fromLoc = "customer",
                            toLoc = "scrap",
                            seller = seller,
                            notes = "استلام كسر مبدل بالفاتورة $id"
                        )
                    )
                }

                val invoice = InvoiceEntity(
                    id = id,
                    date = date,
                    seller = seller,
                    customer = customer,
                    phone = phone,
                    p21 = p21,
                    itemsJson = itemsJson,
                    tw = tw,
                    tq = tq,
                    tot = tot,
                    fare = fare,
                    cash = cash,
                    instapay = instapay,
                    vodafone = vodafone,
                    visa = visa,
                    ewallet = ewallet,
                    notes = notes,
                    hasOldGold = hasOldGold,
                    ogKarat = ogKarat,
                    ogWeight = ogWeight,
                    ogPrice = ogPrice,
                    ogValue = ogValue
                )

                // 2. Financial Engine: Create Accounting entry
                val debitAcc = when {
                    cash > 0.0 && instapay == 0.0 && vodafone == 0.0 && visa == 0.0 -> "درج الكاش (نقدي)"
                    cash == 0.0 && (instapay > 0.0 || vodafone > 0.0 || visa > 0.0 || ewallet > 0.0) -> "الحساب الإلكتروني والبنكي"
                    else -> "مزيج كاش وبنكي إلكتروني"
                }
                repository.insertJournalEntry(
                    JournalEntry(
                        id = "JRN_${id}",
                        date = date,
                        eventType = "Sale",
                        debitAccount = debitAcc,
                        creditAccount = "إيرادات مبيعات الذهب والمصنعية",
                        amount = tot,
                        description = "إثبات عملية البيع للفاتورة $id بقيمة $tot ج",
                        seller = seller
                    )
                )

                // 3. Audit Engine: Generate operational audit trail
                repository.insertAuditRecord(
                    AuditRecord(
                        id = "AUD_${id}",
                        date = date,
                        actor = seller,
                        action = "فاتورة مبيعات",
                        detailsBefore = "جرد فاترينة قبل المبيعات، ومحاولة إجراء عملية بيع لـ ${items.size} أصناف",
                        detailsAfter = "تمت العملية بنجاح. القيمة الإجمالية: $tot ج. طريقة الدفع: الكاش: $cash ج، الإلكتروني: ${instapay+vodafone+visa+ewallet} ج",
                        device = "نظام ERP عين المعلم المطور",
                        ip = "192.168.1.101",
                        location = "الفرع الرئيسي - الصالة"
                    )
                )

                repository.insertInvoice(invoice)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun addScrapPurchase(
        seller: String,
        customer: String,
        karat: Int,
        weight: Double,
        price: Double,
        totalCost: Double,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val id = "BUY" + System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())

                // 1. Inventory Engine: insert scrap movement
                repository.insertInventoryMovement(
                    InventoryMovement(
                        id = "MVT_${id}",
                        date = date,
                        itemKey = "كسر-${karat}",
                        itemType = "شراء كسر مباشر",
                        karat = karat.toString(),
                        movementType = "Purchase",
                        qty = 1,
                        weight = weight,
                        fromLoc = "customer",
                        toLoc = "scrap",
                        seller = seller,
                        notes = "شراء كسر مباشر بسعر $price ج للجرام"
                    )
                )

                // 2. Financial Engine: create JournalEntry
                repository.insertJournalEntry(
                    JournalEntry(
                        id = "JRN_${id}",
                        date = date,
                        eventType = "Scrap Purchase",
                        debitAccount = "مخزون الذهب الكسر والسبائك",
                        creditAccount = "درج الكاش (نقدي)",
                        amount = totalCost,
                        description = "إثبات عملية شراء كسر مباشر بوزن $weight جرام عيار $karat",
                        seller = seller
                    )
                )

                // 3. Audit Engine: create AuditRecord
                repository.insertAuditRecord(
                    AuditRecord(
                        id = "AUD_${id}",
                        date = date,
                        actor = seller,
                        action = "شراء كسر مباشر",
                        detailsBefore = "سعر الشراء المسجل: $price ج/جرام. الوزن المطلوب: $weight جرام",
                        detailsAfter = "تم الشراء وصرف مبلغ $totalCost ج نقداً من درج الكاش للمورد $customer وتحديث عهدة الكسر",
                        device = "نظام ERP عين المعلم المطور",
                        ip = "192.168.1.101",
                        location = "الفرع الرئيسي - الصالة"
                    )
                )

                val invoice = InvoiceEntity(
                    id = id,
                    date = date,
                    seller = seller,
                    customer = customer,
                    phone = "",
                    p21 = price,
                    itemsJson = "[]",
                    tw = 0.0,
                    tq = 0,
                    tot = 0.0,
                    fare = 0.0,
                    cash = -totalCost, // Outflow
                    instapay = 0.0,
                    vodafone = 0.0,
                    visa = 0.0,
                    ewallet = 0.0,
                    notes = "شراء كسر مباشر",
                    hasOldGold = true,
                    ogKarat = karat,
                    ogWeight = weight,
                    ogPrice = price,
                    ogValue = totalCost
                )

                repository.insertInvoice(invoice)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun addExpense(seller: String, amt: Double, desc: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val id = "EXP" + System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())

                // 2. Financial Engine: JournalEntry
                repository.insertJournalEntry(
                    JournalEntry(
                        id = "JRN_${id}",
                        date = date,
                        eventType = "Expense",
                        debitAccount = "حساب المصروفات التشغيلية والنثرية",
                        creditAccount = "درج الكاش (نقدي)",
                        amount = amt,
                        description = "إثبات صرف مصروف تشغيلي: $desc",
                        seller = seller
                    )
                )

                // 3. Audit Engine: AuditRecord
                repository.insertAuditRecord(
                    AuditRecord(
                        id = "AUD_${id}",
                        date = date,
                        actor = seller,
                        action = "صرف مصروف",
                        detailsBefore = "الدرج الحالي نقداً، طلب سداد مبلغ $amt ج لغرض: $desc",
                        detailsAfter = "تم صرف المصروف بنجاح وخصمه من العهدة النقدية للبائع $seller",
                        device = "نظام ERP عين المعلم المطور",
                        ip = "192.168.1.101",
                        location = "الفرع الرئيسي - الصالة"
                    )
                )

                val expense = ExpenseEntity(id, date, seller, amt, desc)
                repository.insertExpense(expense)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun addAmana(seller: String, person: String, item: String, karat: String, qty: Int, weight: Double, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val id = "AMN" + System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())

                val key = "$item-$karat"
                val currentStock = repository.allStock.first().associateBy { it.key }
                val existing = currentStock[key]
                
                if (existing != null && existing.vitrineQty >= qty) {
                    val newVitrineQty = existing.vitrineQty - qty
                    repository.insertStock(existing.copy(vitrineQty = newVitrineQty))

                    // 1. Inventory Engine: movement consignment out
                    repository.insertInventoryMovement(
                        InventoryMovement(
                            id = "MVT_${id}",
                            date = date,
                            itemKey = key,
                            itemType = item,
                            karat = karat,
                            movementType = "Consignment",
                            qty = qty,
                            weight = weight,
                            fromLoc = "showroom",
                            toLoc = "consignment",
                            seller = seller,
                            notes = "خروج أمانة معلقة للعميل $person"
                        )
                    )

                    // 2. Financial Engine: journal entry consignment
                    repository.insertJournalEntry(
                        JournalEntry(
                            id = "JRN_${id}",
                            date = date,
                            eventType = "Consignment Loss", // labeled as consignment transition
                            debitAccount = "حساب الأمانات والعهد الخارجية المعلقة",
                            creditAccount = "مخزون فاترينة المعرض",
                            amount = weight * 3500.0, // estimate based on standard rate
                            description = "إثبات خروج أمانة بوزن $weight جرام عيار $karat لصالح $person",
                            seller = seller
                        )
                    )

                    // 3. Audit Engine: audit trail
                    repository.insertAuditRecord(
                        AuditRecord(
                            id = "AUD_${id}",
                            date = date,
                            actor = seller,
                            action = "تسليم أمانة بالخارج",
                            detailsBefore = "الفاترينة تحتوى على ${existing.vitrineQty} قطع من صنف $key",
                            detailsAfter = "تم خصم $qty قطعة وتسليمها كأمانة معلقة لصالح $person بوزن $weight جرام ومطابقة الجرد",
                            device = "نظام ERP عين المعلم المطور",
                            ip = "192.168.1.101",
                            location = "الفرع الرئيسي - الصالة"
                        )
                    )

                    val amana = AmanaEntity(
                        id = id,
                        date = date,
                        seller = seller,
                        person = person,
                        item = item,
                        karat = karat,
                        qty = qty,
                        weight = weight,
                        returned = false,
                        itemKey = key
                    )
                    repository.insertAmana(amana)
                    onComplete(true)
                } else {
                    onComplete(false) // Not enough stock in vitrine
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun returnAmana(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val amanas = repository.allAmanas.first()
                val amana = amanas.find { it.id == id }
                if (amana != null && !amana.returned) {
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date())

                    val updated = amana.copy(returned = true)
                    repository.updateAmana(updated)

                    // Restore vitrine stock
                    val currentStock = repository.allStock.first().associateBy { it.key }
                    val existing = currentStock[amana.itemKey]
                    if (existing != null) {
                        repository.insertStock(existing.copy(vitrineQty = existing.vitrineQty + amana.qty))
                    } else {
                        val parts = amana.itemKey.split("-")
                        repository.insertStock(StockEntity(amana.itemKey, parts[0], parts[1], amana.qty, 0))
                    }

                    // 1. Inventory Engine: consignment returned
                    repository.insertInventoryMovement(
                        InventoryMovement(
                            id = "MVT_RET_${id}",
                            date = date,
                            itemKey = amana.itemKey,
                            itemType = amana.item,
                            karat = amana.karat,
                            movementType = "Return",
                            qty = amana.qty,
                            weight = amana.weight,
                            fromLoc = "consignment",
                            toLoc = "showroom",
                            seller = amana.seller,
                            notes = "إرجاع واسترداد الأمانة من العميل ${amana.person}"
                        )
                    )

                    // 2. Financial Engine: reverse journal entry
                    repository.insertJournalEntry(
                        JournalEntry(
                            id = "JRN_RET_${id}",
                            date = date,
                            eventType = "Consignment Loss", // labeled as return transition
                            debitAccount = "مخزون فاترينة المعرض",
                            creditAccount = "حساب الأمانات والعهد الخارجية المعلقة",
                            amount = amana.weight * 3500.0,
                            description = "إرجاع الأمانة المعلقة وتسوية الفاتورة للأمانة $id",
                            seller = amana.seller
                        )
                    )

                    // 3. Audit Engine: audit trail
                    repository.insertAuditRecord(
                        AuditRecord(
                            id = "AUD_RET_${id}",
                            date = date,
                            actor = amana.seller,
                            action = "استرداد أمانة",
                            detailsBefore = "الأمانة لدى العميل ${amana.person} كانت نشطة وغير مرتجعة",
                            detailsAfter = "تم استلام الأمانة بنجاح، ومطابقتها فحصاً، وإدخالها مجدداً للفاترينة لإعادة العرض والبيع",
                            device = "نظام ERP عين المعلم المطور",
                            ip = "192.168.1.101",
                            location = "الفرع الرئيسي - الصالة"
                        )
                    )

                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun addStockItem(item: String, karat: String, qty: Int, loc: String) {
        viewModelScope.launch {
            val key = "$item-$karat"
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val currentStock = repository.allStock.first().associateBy { it.key }
            val existing = currentStock[key]
            if (existing != null) {
                val updated = if (loc == "vitrine") {
                    existing.copy(vitrineQty = existing.vitrineQty + qty)
                } else {
                    existing.copy(vaultQty = existing.vaultQty + qty)
                }
                repository.insertStock(updated)
            } else {
                val newEntity = if (loc == "vitrine") {
                    StockEntity(key, item, karat, qty, 0)
                } else {
                    StockEntity(key, item, karat, 0, qty)
                }
                repository.insertStock(newEntity)
            }

            // Inventory Engine Movement
            repository.insertInventoryMovement(
                InventoryMovement(
                    id = "MVT_ADD_" + System.currentTimeMillis(),
                    date = date,
                    itemKey = key,
                    itemType = item,
                    karat = karat,
                    movementType = "Adjustment",
                    qty = qty,
                    weight = qty * 4.5, // approximate estimation
                    fromLoc = "external",
                    toLoc = if (loc == "vitrine") "showroom" else "vault",
                    seller = "المعلم",
                    notes = "شحن وتغذية مخزون إضافي للمحل"
                )
            )

            // Audit
            repository.insertAuditRecord(
                AuditRecord(
                    id = "AUD_ADD_" + System.currentTimeMillis(),
                    date = date,
                    actor = "المعلم",
                    action = "شحن مخزون",
                    detailsBefore = "مخزون صنف $key قبل التغذية: ${existing?.vitrineQty ?: 0} فاترينة | ${existing?.vaultQty ?: 0} خزنة",
                    detailsAfter = "تم شحن كمية $qty قطعة إلى ${if (loc == "vitrine") "الفاترينة" else "الخزنة"} بنجاح وموازنة البيانات",
                    device = "نظام ERP عين المعلم المطور",
                    ip = "192.168.1.101",
                    location = "الفرع الرئيسي - الصالة"
                )
            )
        }
    }

    fun transferStockItem(item: String, karat: String, qty: Int, from: String, to: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val key = "$item-$karat"
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())

                val currentStock = repository.allStock.first().associateBy { it.key }
                val existing = currentStock[key]
                if (existing != null) {
                    val fromQty = if (from == "vitrine") existing.vitrineQty else existing.vaultQty
                    if (fromQty >= qty) {
                        val updated = if (from == "vitrine") {
                            existing.copy(
                                vitrineQty = existing.vitrineQty - qty,
                                vaultQty = existing.vaultQty + qty
                            )
                        } else {
                            existing.copy(
                                vaultQty = existing.vaultQty - qty,
                                vitrineQty = existing.vitrineQty + qty
                            )
                        }
                        repository.insertStock(updated)

                        // 1. Inventory Engine: transfer movement
                        repository.insertInventoryMovement(
                            InventoryMovement(
                                id = "MVT_TRSF_" + System.currentTimeMillis(),
                                date = date,
                                itemKey = key,
                                itemType = item,
                                karat = karat,
                                movementType = "Transfer",
                                qty = qty,
                                weight = qty * 4.5,
                                fromLoc = if (from == "vitrine") "showroom" else "vault",
                                toLoc = if (to == "vitrine") "showroom" else "vault",
                                seller = "المعلم",
                                notes = "تحويل داخلي للمخزون من $from إلى $to"
                            )
                        )

                        // 2. Financial Ledger
                        repository.insertJournalEntry(
                            JournalEntry(
                                id = "JRN_TRSF_" + System.currentTimeMillis(),
                                date = date,
                                eventType = "Vault Transfer",
                                debitAccount = if (to == "vitrine") "مخزون فاترينة المعرض" else "خزنة المحل الحديدية",
                                creditAccount = if (from == "vitrine") "مخزون فاترينة المعرض" else "خزنة المحل الحديدية",
                                amount = qty * 15000.0,
                                description = "تحويل مخزون $qty قطعة صنف $key",
                                seller = "المعلم"
                            )
                        )

                        // 3. Audit Engine
                        repository.insertAuditRecord(
                            AuditRecord(
                                id = "AUD_TRSF_" + System.currentTimeMillis(),
                                date = date,
                                actor = "المعلم",
                                action = "تحويل داخلي",
                                detailsBefore = "جرد $key قبل النقل: فاترينة: ${existing.vitrineQty} | خزنة: ${existing.vaultQty}",
                                detailsAfter = "تم تحويل $qty قطعة بنجاح من $from إلى $to وموازنة الجرد الفعلي والبرمجي",
                                device = "نظام ERP عين المعلم المطور",
                                ip = "192.168.1.101",
                                location = "الفرع الرئيسي - الصالة"
                            )
                        )

                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun settleStockItem(key: String, actualQty: Int, loc: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val currentStock = repository.allStock.first().associateBy { it.key }
            val existing = currentStock[key]
            
            val beforeQty = if (existing != null) {
                if (loc == "vitrine") existing.vitrineQty else existing.vaultQty
            } else 0

            val diff = actualQty - beforeQty

            if (existing != null) {
                val updated = if (loc == "vitrine") {
                    existing.copy(vitrineQty = actualQty)
                } else {
                    existing.copy(vaultQty = actualQty)
                }
                repository.insertStock(updated)
            } else {
                val parts = key.split("-")
                val item = parts.getOrNull(0) ?: "غير معروف"
                val karat = parts.getOrNull(1) ?: "21"
                val newEntity = if (loc == "vitrine") {
                    StockEntity(key, item, karat, actualQty, 0)
                } else {
                    StockEntity(key, item, karat, 0, actualQty)
                }
                repository.insertStock(newEntity)
            }

            if (diff != 0) {
                // 1. Inventory Engine: Adjustment movement
                repository.insertInventoryMovement(
                    InventoryMovement(
                        id = "MVT_STL_" + System.currentTimeMillis(),
                        date = date,
                        itemKey = key,
                        itemType = key.substringBefore("-"),
                        karat = key.substringAfter("-"),
                        movementType = "Adjustment",
                        qty = Math.abs(diff),
                        weight = Math.abs(diff) * 4.5,
                        fromLoc = if (diff < 0) (if (loc == "vitrine") "showroom" else "vault") else "external",
                        toLoc = if (diff > 0) (if (loc == "vitrine") "showroom" else "vault") else "adjustment_loss",
                        seller = "المعلم",
                        notes = "تسوية ومطابقة الجرد اليومي (فارق: $diff قطعة)"
                    )
                )

                // 2. Financial entry if loss
                repository.insertJournalEntry(
                    JournalEntry(
                        id = "JRN_STL_" + System.currentTimeMillis(),
                        date = date,
                        eventType = "Inventory Adjustment",
                        debitAccount = if (diff < 0) "خسائر عجز تسويات جرد المخازن" else "مخزون فاترينة المعرض",
                        creditAccount = if (diff < 0) "مخزون فاترينة المعرض" else "أرباح فائض تسويات الجرد",
                        amount = Math.abs(diff) * 15000.0,
                        description = "تسوية جرد الفارق لـ $key بكمية $diff قطعة",
                        seller = "المعلم"
                    )
                )

                // 3. Audit trail
                repository.insertAuditRecord(
                    AuditRecord(
                        id = "AUD_STL_" + System.currentTimeMillis(),
                        date = date,
                        actor = "المعلم",
                        action = "تسوية جرد",
                        detailsBefore = "الكمية قبل المطابقة والتحقق: $beforeQty في $loc",
                        detailsAfter = "تمت تسوية الجرد ومطابقة الكمية يدوياً إلى $actualQty قطعة في $loc. فارق التسوية: $diff",
                        device = "نظام ERP عين المعلم المطور",
                        ip = "192.168.1.101",
                        location = "الفرع الرئيسي - الصالة"
                    )
                )
            }
        }
    }

    fun addSeller(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertSeller(SellerEntity(name, colorHex))
        }
    }

    fun deleteSeller(name: String) {
        viewModelScope.launch {
            repository.deleteSeller(name)
        }
    }

    fun addItemCategory(name: String) {
        viewModelScope.launch {
            repository.insertItem(ItemEntity(name))
        }
    }

    fun deleteItemCategory(name: String) {
        viewModelScope.launch {
            repository.deleteItem(name)
        }
    }

    fun factoryReset(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            repository.prepopulateIfNeeded()
            checkAndSaveOpeningStock()
            onComplete()
        }
    }
}
