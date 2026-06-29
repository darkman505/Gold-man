package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GoldErpRepository(private val dao: GoldErpDao) {

    val allInvoices: Flow<List<InvoiceEntity>> = dao.getAllInvoices()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allAmanas: Flow<List<AmanaEntity>> = dao.getAllAmanas()
    val allStock: Flow<List<StockEntity>> = dao.getAllStock()
    val allSellers: Flow<List<SellerEntity>> = dao.getAllSellers()
    val allItems: Flow<List<ItemEntity>> = dao.getAllItems()
    val allInventoryMovements: Flow<List<InventoryMovement>> = dao.getAllInventoryMovements()
    val allJournalEntries: Flow<List<JournalEntry>> = dao.getAllJournalEntries()
    val allAuditRecords: Flow<List<AuditRecord>> = dao.getAllAuditRecords()

    suspend fun insertInvoice(invoice: InvoiceEntity) = dao.insertInvoice(invoice)
    suspend fun insertExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    suspend fun insertAmana(amana: AmanaEntity) = dao.insertAmana(amana)
    suspend fun updateAmana(amana: AmanaEntity) = dao.updateAmana(amana)
    
    suspend fun insertStock(stock: StockEntity) = dao.insertStock(stock)
    suspend fun insertStockList(stock: List<StockEntity>) = dao.insertStockList(stock)

    suspend fun insertSeller(seller: SellerEntity) = dao.insertSeller(seller)
    suspend fun deleteSeller(name: String) = dao.deleteSeller(name)

    suspend fun insertItem(item: ItemEntity) = dao.insertItem(item)
    suspend fun deleteItem(name: String) = dao.deleteItem(name)

    suspend fun getOpeningStock(date: String): OpeningStockEntity? = dao.getOpeningStock(date)
    suspend fun insertOpeningStock(openingStock: OpeningStockEntity) = dao.insertOpeningStock(openingStock)

    suspend fun insertInventoryMovement(movement: InventoryMovement) = dao.insertInventoryMovement(movement)
    suspend fun insertJournalEntry(entry: JournalEntry) = dao.insertJournalEntry(entry)
    suspend fun insertAuditRecord(record: AuditRecord) = dao.insertAuditRecord(record)
    suspend fun softDeleteAuditRecord(id: String) = dao.softDeleteAuditRecord(id)

    suspend fun clearAllData() {
        dao.clearInvoices()
        dao.clearExpenses()
        dao.clearAmanas()
        dao.clearStock()
        dao.clearSellers()
        dao.clearItems()
        dao.clearOpeningStock()
        dao.clearInventoryMovements()
        dao.clearJournalEntries()
        dao.clearAuditRecords()
    }

    suspend fun prepopulateIfNeeded() {
        // If empty, add default sellers and items
        val sellers = dao.getAllSellers().first()
        if (sellers.isEmpty()) {
            dao.insertSeller(SellerEntity("المعلم", "#D4AF37"))
            dao.insertSeller(SellerEntity("البياع بشير", "#3b82f6"))
            dao.insertSeller(SellerEntity("البياع ماهر", "#10b981"))
        }

        val items = dao.getAllItems().first()
        if (items.isEmpty()) {
            val defaultItems = listOf("خاتم", "سلسلة", "غويشة", "دبلة", "حلق", "سبائك", "جنيه")
            defaultItems.forEach { dao.insertItem(ItemEntity(it)) }
        }

        // Prepopulate default stock if it's completely empty
        val stock = dao.getAllStockDirect()
        if (stock.isEmpty()) {
            val defaultItems = listOf("خاتم", "سلسلة", "غويشة", "دبلة", "حلق", "سبائك", "جنيه")
            val defaultKarats = listOf("21", "18", "24")
            val initialStock = mutableListOf<StockEntity>()
            defaultItems.forEach { item ->
                defaultKarats.forEach { karat ->
                    // Exclude specific non-existent categories as requested by user
                    val isInvalid = (item == "سبائك" && (karat == "21" || karat == "18")) ||
                                    (item != "سبائك" && karat == "24") ||
                                    (item == "جنيه" && karat == "18") ||
                                    (item == "سبيكة")
                    if (!isInvalid) {
                        val key = "$item-$karat"
                        val vitrineQty = 0
                        val vaultQty = 0
                        initialStock.add(StockEntity(key, item, karat, vitrineQty, vaultQty))
                    }
                }
            }
            dao.insertStockList(initialStock)
        } else {
            // Clean up any existing invalid items from the database
            dao.deleteItem("سبيكة")
            
            // Ensure سبائك exists
            val itemsList = dao.getAllItems().first()
            if (itemsList.none { it.name == "سبائك" }) {
                dao.insertItem(ItemEntity("سبائك"))
            }
            
            val defaultItems = listOf("خاتم", "سلسلة", "غويشة", "دبلة", "حلق", "سبيكة", "جنيه", "سبائك")
            defaultItems.forEach { item ->
                if (item != "سبائك") {
                    dao.deleteStockByKey("$item-24")
                }
            }
            dao.deleteStockByKey("سبائك-21")
            dao.deleteStockByKey("سبائك-18")
            dao.deleteStockByKey("جنيه-18")
            dao.deleteStockByKey("سبيكة-21")
            dao.deleteStockByKey("سبيكة-18")
            dao.deleteStockByKey("سبيكة-24")
        }
    }
}
