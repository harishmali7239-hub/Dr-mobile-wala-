package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repair_jobs")
data class RepairJob(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jobTicketNumber: String, // e.g. DMW-1001
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val brand: String,
    val deviceModel: String,
    val imeiNumber: String,
    val problemDescription: String,
    val estimatedCost: Double,
    val advancePaid: Double = 0.0,
    val finalAmount: Double = estimatedCost,
    val paymentStatus: String = "PENDING", // PENDING, PARTIAL, PAID
    val jobStatus: String = "RECEIVED", // RECEIVED, DIAGNOSTICS, WAITING_FOR_PARTS, REPAIRING, READY, DELIVERED, CANCELLED
    val receivedDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long = 0L,
    val beforePhotoUris: List<String> = emptyList(),
    val afterPhotoUris: List<String> = emptyList(),
    val technicianNotes: String = "",
    val warrantyDays: Int = 30
) {
    val pendingBalance: Double
        get() = (finalAmount - advancePaid).coerceAtLeast(0.0)
}
