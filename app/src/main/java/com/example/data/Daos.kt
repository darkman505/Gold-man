package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldErpDao {

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    // Amanas
    @Query("SELECT * FROM amanas ORDER BY date DESC")
    fun getAllAmanas(): Flow<List<AmanaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAmana(amana: AmanaEntity)

    @Update
    suspend fun updateAmana(amana: AmanaEntity)

    // Stock
    @Query("SELECT * FROM stock")
    fun getAllStock(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock")
    suspend fun getAllStockDirect(): List<StockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockList(stock: List<StockEntity>)

    @Query("DELETE FROM stock WHERE `key` = :key")
    suspend fun deleteStockByKey(key: String)

    // Sellers
    @Query("SELECT * FROM sellers")
    fun getAllSellers(): Flow<List<SellerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerEntity)

    @Query("DELETE FROM sellers WHERE name = :name")
    suspend fun deleteSeller(name: String)

    // Items
    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Query("DELETE FROM items WHERE name = :name")
    suspend fun deleteItem(name: String)

    // Opening Stock
    @Query("SELECT * FROM opening_stock WHERE date = :date")
    suspend fun getOpeningStock(date: String): OpeningStockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpeningStock(openingStock: OpeningStockEntity)
    
    // Clear All
    @Query("DELETE FROM invoices")
    suspend fun clearInvoices()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM amanas")
    suspend fun clearAmanas()

    @Query("DELETE FROM stock")
    suspend fun clearStock()

    @Query("DELETE FROM sellers")
    suspend fun clearSellers()

    @Query("DELETE FROM items")
    suspend fun clearItems()

    @Query("DELETE FROM opening_stock")
    suspend fun clearOpeningStock()

    // Inventory Movements
    @Query("SELECT * FROM inventory_movements ORDER BY date DESC")
    fun getAllInventoryMovements(): Flow<List<InventoryMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryMovement(movement: InventoryMovement)

    @Query("DELETE FROM inventory_movements")
    suspend fun clearInventoryMovements()

    // Journal Entries
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournalEntries()

    // Audit Records
    @Query("SELECT * FROM audit_records WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllAuditRecords(): Flow<List<AuditRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditRecord(record: AuditRecord)

    @Query("UPDATE audit_records SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteAuditRecord(id: String)

    @Query("DELETE FROM audit_records")
    suspend fun clearAuditRecords()
}
