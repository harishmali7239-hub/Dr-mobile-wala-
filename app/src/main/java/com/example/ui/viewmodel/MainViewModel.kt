package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import com.example.data.model.RepairJob
import com.example.data.model.ShopSettings
import com.example.data.repository.RepairShopRepository
import com.example.utils.BackupRestoreUtil
import com.example.utils.ImageStorageUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object RepairJobs : Screen("repair_jobs", "Repair Jobs")
    object Customers : Screen("customers", "Customers")
    object Payments : Screen("payments", "Payments")
    object ImeiLookup : Screen("imei_lookup", "IMEI Search")
    object ShopSettings : Screen("settings", "Shop Settings")
    object JobDetail : Screen("job_detail/{jobId}", "Job Ticket") {
        fun createRoute(jobId: Long) = "job_detail/$jobId"
    }
    object AddEditJob : Screen("add_edit_job?jobId={jobId}", "Repair Job Form") {
        fun createRoute(jobId: Long? = null) = if (jobId != null) "add_edit_job?jobId=$jobId" else "add_edit_job"
    }
    object CustomerDetail : Screen("customer_detail/{customerId}", "Customer Profile") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = RepairShopRepository(
        db.customerDao(),
        db.repairJobDao(),
        db.paymentRecordDao(),
        db.shopSettingsDao()
    )

    // State
    val allCustomers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allJobs = repository.allJobs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPayments = repository.allPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val shopSettings = repository.shopSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettings())

    val totalJobCount = repository.totalJobCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val activeJobCount = repository.activeJobCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val readyForPickupCount = repository.readyForPickupCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalRevenue = repository.totalRevenue.map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL") // ALL, RECEIVED, DIAGNOSTICS, REPAIRING, READY, DELIVERED
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    val filteredJobs: StateFlow<List<RepairJob>> = combine(allJobs, _searchQuery, _statusFilter) { jobs, query, filter ->
        jobs.filter { job ->
            val matchesQuery = query.isEmpty() ||
                    job.jobTicketNumber.contains(query, ignoreCase = true) ||
                    job.customerName.contains(query, ignoreCase = true) ||
                    job.customerPhone.contains(query, ignoreCase = true) ||
                    job.deviceModel.contains(query, ignoreCase = true) ||
                    job.imeiNumber.contains(query, ignoreCase = true) ||
                    job.brand.contains(query, ignoreCase = true)

            val matchesFilter = if (filter == "ALL") true else job.jobStatus.equals(filter, ignoreCase = true)

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showToast(message: String) {
        _userMessage.value = message
    }

    // Customer Actions
    fun saveCustomer(customer: Customer, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                val id = repository.addCustomer(customer)
                _userMessage.value = "Customer added successfully"
                onSaved(id)
            } else {
                repository.updateCustomer(customer)
                _userMessage.value = "Customer updated"
                onSaved(customer.id)
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _userMessage.value = "Customer removed"
        }
    }

    // Repair Job Actions
    fun saveRepairJob(job: RepairJob, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            if (job.id == 0L) {
                val newId = repository.addRepairJob(job)
                _userMessage.value = "New repair ticket generated: ${job.jobTicketNumber}"
                onSaved(newId)
            } else {
                repository.updateRepairJob(job)
                _userMessage.value = "Repair job updated"
                onSaved(job.id)
            }
        }
    }

    fun updateJobStatus(job: RepairJob, newStatus: String) {
        viewModelScope.launch {
            val updated = job.copy(
                jobStatus = newStatus,
                deliveryDate = if (newStatus == "DELIVERED") System.currentTimeMillis() else job.deliveryDate
            )
            repository.updateRepairJob(updated)
            _userMessage.value = "Status updated to ${newStatus.replace("_", " ")}"
        }
    }

    fun deleteRepairJob(job: RepairJob) {
        viewModelScope.launch {
            repository.deleteRepairJob(job)
            _userMessage.value = "Repair job ticket deleted"
        }
    }

    // Payment Actions
    fun addPayment(job: RepairJob, amount: Double, paymentMode: String, ref: String) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.value = "Enter valid payment amount"
                return@launch
            }
            val payment = PaymentRecord(
                repairJobId = job.id,
                jobTicketNumber = job.jobTicketNumber,
                customerName = job.customerName,
                amount = amount,
                paymentMode = paymentMode,
                transactionRef = ref
            )
            repository.addPayment(payment)
            _userMessage.value = "Payment of ₹$amount recorded"
        }
    }

    // Settings & Dark Mode
    fun updateShopSettings(settings: ShopSettings) {
        viewModelScope.launch {
            repository.updateShopSettings(settings)
            _userMessage.value = "Shop settings saved"
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = shopSettings.value ?: ShopSettings()
            repository.updateShopSettings(current.copy(isDarkMode = !current.isDarkMode))
        }
    }

    // Backup & Restore
    fun exportBackup(onFileCreated: (File?) -> Unit) {
        viewModelScope.launch {
            val file = BackupRestoreUtil.createBackup(getApplication(), db)
            if (file != null) {
                _userMessage.value = "Backup saved to: ${file.name}"
            } else {
                _userMessage.value = "Failed to create backup"
            }
            onFileCreated(file)
        }
    }

    fun restoreBackup(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = BackupRestoreUtil.restoreBackupFromUri(getApplication(), db, uri)
            if (success) {
                _userMessage.value = "Data restored successfully!"
            } else {
                _userMessage.value = "Failed to restore backup file"
            }
            onComplete(success)
        }
    }

    // Attach Photos
    fun attachPhotoToJob(jobId: Long, photoUri: Uri, isBeforePhoto: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val savedPath = ImageStorageUtil.saveImageFromUri(getApplication(), photoUri)
            if (savedPath != null) {
                val job = repository.getJobById(jobId)
                if (job != null) {
                    val updated = if (isBeforePhoto) {
                        job.copy(beforePhotoUris = job.beforePhotoUris + savedPath)
                    } else {
                        job.copy(afterPhotoUris = job.afterPhotoUris + savedPath)
                    }
                    repository.updateRepairJob(updated)
                    _userMessage.value = if (isBeforePhoto) "Before-repair photo attached" else "After-repair photo attached"
                }
            } else {
                _userMessage.value = "Failed to save photo"
            }
            onDone()
        }
    }
}
