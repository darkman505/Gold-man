package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonHelper {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val invoiceItemListType = Types.newParameterizedType(List::class.java, InvoiceItem::class.java)
    private val invoiceItemAdapter = moshi.adapter<List<InvoiceItem>>(invoiceItemListType)

    fun toJson(items: List<InvoiceItem>): String {
        return try {
            invoiceItemAdapter.toJson(items)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun fromJson(json: String): List<InvoiceItem> {
        return try {
            invoiceItemAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
