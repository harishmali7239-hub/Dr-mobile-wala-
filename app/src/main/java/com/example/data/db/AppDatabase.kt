package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.CustomerDao
import com.example.data.dao.PaymentRecordDao
import com.example.data.dao.RepairJobDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import com.example.data.model.RepairJob
import com.example.data.model.ShopSettings

@Database(
    entities = [
        Customer::class,
        RepairJob::class,
        PaymentRecord::class,
        ShopSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun repairJobDao(): RepairJobDao
    abstract fun paymentRecordDao(): PaymentRecordDao
    abstract fun shopSettingsDao(): ShopSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dr_mobile_wala_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
