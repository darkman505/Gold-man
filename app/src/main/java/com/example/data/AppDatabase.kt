package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        InvoiceEntity::class,
        ExpenseEntity::class,
        AmanaEntity::class,
        StockEntity::class,
        SellerEntity::class,
        ItemEntity::class,
        OpeningStockEntity::class,
        InventoryMovement::class,
        JournalEntry::class,
        AuditRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GoldErpDatabase : RoomDatabase() {
    abstract fun dao(): GoldErpDao

    companion object {
        @Volatile
        private var INSTANCE: GoldErpDatabase? = null

        fun getDatabase(context: Context): GoldErpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoldErpDatabase::class.java,
                    "gold_erp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
