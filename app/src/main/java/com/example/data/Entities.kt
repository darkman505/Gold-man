package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val date: String,
    val seller: String,
    val customer: String,
    val phone: String,
    val p21: Double,
    val itemsJson: String, // JSON array of items: [{"t": "خاتم", "k": "21", "q": 1, "w": 4.5}]
    val tw: Double,
    val tq: Int,
    val tot: Double,
    val fare: Double,
    val cash: Double,
    val instapay: Double,
    val vodafone: Double,
    val visa: Double,
    val ewallet: Double,
    val notes: String,
    val hasOldGold: Boolean,
    val ogKarat: Int,
    val ogWeight: Double,
    val ogPrice: Double,
    val ogValue: Double
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val date: String,
    val seller: String,
    val amt: Double,
    val desc: String
)

@Entity(tableName = "amanas")
data class AmanaEntity(
    @PrimaryKey val id: String,
    val date: String,
    val seller: String,
    val person: String,
    val item: String,
    val karat: String,
    val qty: Int,
    val weight: Double,
    val returned: Boolean,
    val itemKey: String
)

@Entity(tableName = "stock")
data class StockEntity(
    @PrimaryKey val key: String, // e.g. "خاتم-21"
    val item: String,
    val karat: String,
    val vitrineQty: Int,
    val vaultQty: Int
)

@Entity(tableName = "sellers")
data class SellerEntity(
    @PrimaryKey val name: String,
    val colorHex: String
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val name: String
)

@Entity(tableName = "opening_stock")
data class OpeningStockEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val vitrineStockJson: String // Serialized Map<String, Int> of vitrine stock
)

@Entity(tableName = "inventory_movements")
data class InventoryMovement(
    @PrimaryKey val id: String,
    val date: String,
    val itemKey: String,
    val itemType: String,
    val karat: String,
    val movementType: String, // "Purchase", "Sale", "Transfer", "Consignment", "Return", "Adjustment", "Audit Settlement"
    val qty: Int,
    val weight: Double,
    val fromLoc: String,
    val toLoc: String,
    val seller: String,
    val notes: String
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String,
    val date: String,
    val eventType: String, // "Sale", "Expense", "Scrap Purchase", "Inventory Adjustment", "Vault Transfer", "Consignment Loss"
    val debitAccount: String,
    val creditAccount: String,
    val amount: Double,
    val description: String,
    val seller: String
)

@Entity(tableName = "audit_records")
data class AuditRecord(
    @PrimaryKey val id: String,
    val date: String,
    val actor: String,
    val action: String,
    val detailsBefore: String,
    val detailsAfter: String,
    val device: String,
    val ip: String,
    val location: String,
    val isDeleted: Boolean = false // soft delete
)

data class InvoiceItem(
    val t: String, // Item type
    val k: String, // Karat
    val q: Int,    // Quantity
    val w: Double  // Weight
)
