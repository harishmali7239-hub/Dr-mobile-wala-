package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_settings")
data class ShopSettings(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "Dr Mobile Wala",
    val ownerName: String = "Dr. Mobile Specialist",
    val phone: String = "+91 98765 43210",
    val alternatePhone: String = "",
    val email: String = "contact@drmobilewala.com",
    val address: String = "Shop No. 12, Main Mobile Market, City Center",
    val gstOrRegNumber: String = "GSTIN29ABCDE1234F1ZH",
    val upiId: String = "drmobilewala@upi",
    val termsAndConditions: String = "1. 30 days warranty on replaced hardware parts.\n2. No warranty for liquid damaged or physically dropped phones.\n3. Please verify device condition before leaving shop.\n4. Receipt required for pickup.",
    val currencySymbol: String = "₹",
    val isDarkMode: Boolean = false,
    val jobSequenceCounter: Int = 1001
)
