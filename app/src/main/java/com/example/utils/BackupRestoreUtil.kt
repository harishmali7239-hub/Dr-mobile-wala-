package com.example.utils

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import com.example.data.model.RepairJob
import com.example.data.model.ShopSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class BackupData(
    val customers: List<Customer> = emptyList(),
    val jobs: List<RepairJob> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val settings: ShopSettings? = null,
    val backupTimestamp: Long = System.currentTimeMillis()
)

object BackupRestoreUtil {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(BackupData::class.java)

    suspend fun createBackup(context: Context, db: AppDatabase): File? = withContext(Dispatchers.IO) {
        try {
            val customers = db.customerDao().getAllCustomers().firstOrNull() ?: emptyList()
            val jobs = db.repairJobDao().getAllJobs().firstOrNull() ?: emptyList()
            val payments = db.paymentRecordDao().getAllPayments().firstOrNull() ?: emptyList()
            val settings = db.shopSettingsDao().getSettingsDirect()

            val backup = BackupData(customers, jobs, payments, settings)
            val json = adapter.toJson(backup)

            val backupDir = context.getExternalFilesDir("Backups")
            if (backupDir?.exists() == false) backupDir.mkdirs()

            val backupFile = File(backupDir, "DrMobileWala_Backup_${System.currentTimeMillis()}.json")
            FileOutputStream(backupFile).use { it.write(json.toByteArray()) }
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreBackupFromUri(context: Context, db: AppDatabase, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().use { it.readText() }
            } ?: return@withContext false

            val backup = adapter.fromJson(json) ?: return@withContext false

            if (backup.customers.isNotEmpty()) {
                db.customerDao().insertAll(backup.customers)
            }
            if (backup.jobs.isNotEmpty()) {
                db.repairJobDao().insertAll(backup.jobs)
            }
            if (backup.payments.isNotEmpty()) {
                db.paymentRecordDao().insertAll(backup.payments)
            }
            if (backup.settings != null) {
                db.shopSettingsDao().insertOrUpdateSettings(backup.settings)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
