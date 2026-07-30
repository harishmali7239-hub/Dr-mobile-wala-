package com.example.data.dao

import androidx.room.*
import com.example.data.model.RepairJob
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairJobDao {
    @Query("SELECT * FROM repair_jobs ORDER BY receivedDate DESC")
    fun getAllJobs(): Flow<List<RepairJob>>

    @Query("SELECT * FROM repair_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): RepairJob?

    @Query("SELECT * FROM repair_jobs WHERE id = :id")
    fun observeJobById(id: Long): Flow<RepairJob?>

    @Query("SELECT * FROM repair_jobs WHERE imeiNumber LIKE '%' || :imei || '%' ORDER BY receivedDate DESC")
    fun searchByImei(imei: String): Flow<List<RepairJob>>

    @Query("""
        SELECT * FROM repair_jobs 
        WHERE jobTicketNumber LIKE '%' || :query || '%' 
           OR customerName LIKE '%' || :query || '%' 
           OR customerPhone LIKE '%' || :query || '%' 
           OR deviceModel LIKE '%' || :query || '%' 
           OR imeiNumber LIKE '%' || :query || '%' 
        ORDER BY receivedDate DESC
    """)
    fun searchJobs(query: String): Flow<List<RepairJob>>

    @Query("SELECT * FROM repair_jobs WHERE customerId = :customerId ORDER BY receivedDate DESC")
    fun getJobsForCustomer(customerId: Long): Flow<List<RepairJob>>

    @Query("SELECT * FROM repair_jobs WHERE jobStatus = :status ORDER BY receivedDate DESC")
    fun getJobsByStatus(status: String): Flow<List<RepairJob>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: RepairJob): Long

    @Update
    suspend fun updateJob(job: RepairJob)

    @Delete
    suspend fun deleteJob(job: RepairJob)

    @Query("DELETE FROM repair_jobs")
    suspend fun deleteAllJobs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<RepairJob>)

    @Query("SELECT COUNT(*) FROM repair_jobs")
    fun getTotalJobCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM repair_jobs WHERE jobStatus NOT IN ('DELIVERED', 'CANCELLED')")
    fun getActiveJobCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM repair_jobs WHERE jobStatus = 'READY'")
    fun getReadyForPickupCount(): Flow<Int>
}
