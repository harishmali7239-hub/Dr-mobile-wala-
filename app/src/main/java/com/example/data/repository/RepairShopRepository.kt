package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.PaymentRecordDao
import com.example.data.dao.RepairJobDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import com.example.data.model.RepairJob
import com.example.data.model.ShopSettings
import kotlinx.coroutines.flow.Flow

class RepairShopRepository(
    private val customerDao: CustomerDao,
    private val repairJobDao: RepairJobDao,
    private val paymentRecordDao: PaymentRecordDao,
    private val shopSettingsDao: ShopSettingsDao
) {

    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allJobs: Flow<List<RepairJob>> = repairJobDao.getAllJobs()
    val allPayments: Flow<List<PaymentRecord>> = paymentRecordDao.getAllPayments()
    val shopSettings: Flow<ShopSettings?> = shopSettingsDao.getSettings()

    val totalJobCount: Flow<Int> = repairJobDao.getTotalJobCount()
    val activeJobCount: Flow<Int> = repairJobDao.getActiveJobCount()
    val readyForPickupCount: Flow<Int> = repairJobDao.getReadyForPickupCount()
    val totalRevenue: Flow<Double?> = paymentRecordDao.getTotalRevenue()

    fun searchJobs(query: String): Flow<List<RepairJob>> = repairJobDao.searchJobs(query)
    fun searchByImei(imei: String): Flow<List<RepairJob>> = repairJobDao.searchByImei(imei)
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    fun getJobsByStatus(status: String): Flow<List<RepairJob>> = repairJobDao.getJobsByStatus(status)
    fun observeJob(id: Long): Flow<RepairJob?> = repairJobDao.observeJobById(id)
    fun getJobsForCustomer(customerId: Long): Flow<List<RepairJob>> = repairJobDao.getJobsForCustomer(customerId)
    fun getPaymentsForJob(jobId: Long): Flow<List<PaymentRecord>> = paymentRecordDao.getPaymentsForJob(jobId)

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)
    suspend fun getJobById(id: Long): RepairJob? = repairJobDao.getJobById(id)
    suspend fun getSettingsDirect(): ShopSettings? = shopSettingsDao.getSettingsDirect()

    suspend fun addCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    suspend fun addRepairJob(job: RepairJob): Long {
        val insertedId = repairJobDao.insertJob(job)
        // If advance paid > 0, log payment
        if (job.advancePaid > 0) {
            paymentRecordDao.insertPayment(
                PaymentRecord(
                    repairJobId = insertedId,
                    jobTicketNumber = job.jobTicketNumber,
                    customerName = job.customerName,
                    amount = job.advancePaid,
                    paymentMode = "CASH",
                    notes = "Advance payment at check-in"
                )
            )
        }
        // Increment job sequence counter
        val settings = shopSettingsDao.getSettingsDirect() ?: ShopSettings()
        shopSettingsDao.insertOrUpdateSettings(
            settings.copy(jobSequenceCounter = settings.jobSequenceCounter + 1)
        )
        return insertedId
    }

    suspend fun updateRepairJob(job: RepairJob) = repairJobDao.updateJob(job)
    suspend fun deleteRepairJob(job: RepairJob) = repairJobDao.deleteJob(job)

    suspend fun addPayment(payment: PaymentRecord) {
        paymentRecordDao.insertPayment(payment)
        // Update job advance/final balance
        val job = repairJobDao.getJobById(payment.repairJobId)
        if (job != null) {
            val newAdvance = job.advancePaid + payment.amount
            val newPaymentStatus = if (newAdvance >= job.finalAmount) "PAID" else "PARTIAL"
            repairJobDao.updateJob(
                job.copy(
                    advancePaid = newAdvance,
                    paymentStatus = newPaymentStatus
                )
            )
        }
    }

    suspend fun updateShopSettings(settings: ShopSettings) {
        shopSettingsDao.insertOrUpdateSettings(settings)
    }

    suspend fun seedInitialDataIfEmpty() {
        val currentSettings = shopSettingsDao.getSettingsDirect()
        if (currentSettings == null) {
            val defaultSettings = ShopSettings()
            shopSettingsDao.insertOrUpdateSettings(defaultSettings)

            // Seed Customers
            val c1Id = customerDao.insertCustomer(
                Customer(name = "Rahul Sharma", phone = "9876543210", email = "rahul@example.com", address = "Sector 14, Main Road")
            )
            val c2Id = customerDao.insertCustomer(
                Customer(name = "Priya Verma", phone = "9812345678", email = "priya@example.com", address = "Green Park, Flat 402")
            )
            val c3Id = customerDao.insertCustomer(
                Customer(name = "Amit Patel", phone = "9765432109", email = "amit@example.com", address = "Station Road Market")
            )

            // Seed Repair Jobs
            val job1 = RepairJob(
                jobTicketNumber = "DMW-1001",
                customerId = c1Id,
                customerName = "Rahul Sharma",
                customerPhone = "9876543210",
                brand = "Apple",
                deviceModel = "iPhone 13 Pro",
                imeiNumber = "358742091234567",
                problemDescription = "Cracked OLED display & battery health low (74%). Needs screen replacement and original battery.",
                estimatedCost = 14500.0,
                advancePaid = 3000.0,
                finalAmount = 14500.0,
                paymentStatus = "PARTIAL",
                jobStatus = "REPAIRING",
                technicianNotes = "Screen replacement in progress. Display calibrated.",
                warrantyDays = 60
            )

            val job2 = RepairJob(
                jobTicketNumber = "DMW-1002",
                customerId = c2Id,
                customerName = "Priya Verma",
                customerPhone = "9812345678",
                brand = "Samsung",
                deviceModel = "Galaxy S22 Ultra",
                imeiNumber = "354129087654321",
                problemDescription = "Charging port not working, moisture detected error.",
                estimatedCost = 2800.0,
                advancePaid = 2800.0,
                finalAmount = 2800.0,
                paymentStatus = "PAID",
                jobStatus = "READY",
                technicianNotes = "Replaced sub-board flex cable. QC testing passed.",
                warrantyDays = 30
            )

            val job3 = RepairJob(
                jobTicketNumber = "DMW-1003",
                customerId = c3Id,
                customerName = "Amit Patel",
                customerPhone = "9765432109",
                brand = "OnePlus",
                deviceModel = "OnePlus 9 Pro",
                imeiNumber = "867891023456789",
                problemDescription = "No display, green vertical line issue after system update.",
                estimatedCost = 8500.0,
                advancePaid = 1000.0,
                finalAmount = 8500.0,
                paymentStatus = "PARTIAL",
                jobStatus = "RECEIVED",
                technicianNotes = "Initial diagnosis pending. Display panel ordered.",
                warrantyDays = 30
            )

            val j1Id = repairJobDao.insertJob(job1)
            val j2Id = repairJobDao.insertJob(job2)
            repairJobDao.insertJob(job3)

            paymentRecordDao.insertPayment(
                PaymentRecord(
                    repairJobId = j1Id,
                    jobTicketNumber = "DMW-1001",
                    customerName = "Rahul Sharma",
                    amount = 3000.0,
                    paymentMode = "UPI",
                    transactionRef = "UPI982347102"
                )
            )

            paymentRecordDao.insertPayment(
                PaymentRecord(
                    repairJobId = j2Id,
                    jobTicketNumber = "DMW-1002",
                    customerName = "Priya Verma",
                    amount = 2800.0,
                    paymentMode = "CASH",
                    transactionRef = "CASH-REC"
                )
            )

            shopSettingsDao.insertOrUpdateSettings(defaultSettings.copy(jobSequenceCounter = 1004))
        }
    }
}
