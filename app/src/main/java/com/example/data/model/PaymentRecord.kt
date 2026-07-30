package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val repairJobId: Long,
    val jobTicketNumber: String,
    val customerName: String,
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, UPI, CARD, NET_BANKING
    val transactionRef: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)
