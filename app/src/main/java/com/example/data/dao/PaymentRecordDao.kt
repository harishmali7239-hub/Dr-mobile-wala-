package com.example.data.dao

import androidx.room.*
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {
    @Query("SELECT * FROM payment_records ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE repairJobId = :jobId ORDER BY paymentDate DESC")
    fun getPaymentsForJob(jobId: Long): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Delete
    suspend fun deletePayment(payment: PaymentRecord)

    @Query("DELETE FROM payment_records")
    suspend fun deleteAllPayments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentRecord>)

    @Query("SELECT SUM(amount) FROM payment_records")
    fun getTotalRevenue(): Flow<Double?>
}
