package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RepairJob
import com.example.data.model.ShopSettings
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.PhotoSection
import com.example.ui.components.StatusBadge
import com.example.utils.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    job: RepairJob?,
    shopSettings: ShopSettings,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onStatusUpdate: (RepairJob, String) -> Unit,
    onAddPayment: (RepairJob, Double, String, String) -> Unit,
    onAttachPhoto: (jobId: Long, photoUri: Uri, isBeforePhoto: Boolean) -> Unit,
    onDeletePhoto: (jobId: Long, photoPath: String, isBeforePhoto: Boolean) -> Unit,
    onDeleteJob: (RepairJob) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (job == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val statuses = listOf(
        "RECEIVED" to "Received",
        "DIAGNOSTICS" to "In Diagnostics",
        "REPAIRING" to "Repairing",
        "READY" to "Ready for Pickup",
        "DELIVERED" to "Delivered",
        "CANCELLED" to "Cancelled"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Ticket ${job.jobTicketNumber}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(job.id) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Ticket")
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Banner Card & Quick Action Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Status",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusBadge(status = job.jobStatus)
                        }

                        // PDF Receipt Action Button
                        Button(
                            onClick = {
                                val file = PdfGenerator.generateReceiptPdf(context, job, shopSettings)
                                if (file != null) {
                                    PdfGenerator.sharePdf(context, file)
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Receipt")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Update Status:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Dropdown / Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.take(4).forEach { (key, label) ->
                            FilterChip(
                                selected = (job.jobStatus == key),
                                onClick = { onStatusUpdate(job, key) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.drop(4).forEach { (key, label) ->
                            FilterChip(
                                selected = (job.jobStatus == key),
                                onClick = { onStatusUpdate(job, key) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            // Customer Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customer Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Name: ${job.customerName}", fontWeight = FontWeight.SemiBold)
                    Text(text = "Phone: ${job.customerPhone}")
                    Text(
                        text = "Received On: ${dateFormat.format(Date(job.receivedDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.customerPhone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call")
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=91${job.customerPhone}&text=Hello%20${job.customerName},%20regarding%20your%20repair%20ticket%20${job.jobTicketNumber}%20at%20${shopSettings.shopName}:%20Current%20Status%20is%20${job.jobStatus}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp")
                        }
                    }
                }
            }

            // Device & Problem Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Device Specifications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Brand/Model: ${job.brand} ${job.deviceModel}", fontWeight = FontWeight.Bold)
                    Text(text = "IMEI / Serial #: ${job.imeiNumber.ifEmpty { "N/A" }}")
                    Text(text = "Warranty Period: ${job.warrantyDays} Days")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Reported Problem:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = job.problemDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (job.technicianNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Technician Notes:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = job.technicianNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Before Repair Photos Section
            PhotoSection(
                title = "Before Repair Photos",
                photoPaths = job.beforePhotoUris,
                onAddPhoto = { uri -> onAttachPhoto(job.id, uri, true) },
                onDeletePhoto = { path -> onDeletePhoto(job.id, path, true) }
            )

            // After Repair Photos Section
            PhotoSection(
                title = "After Repair Photos",
                photoPaths = job.afterPhotoUris,
                onAddPhoto = { uri -> onAttachPhoto(job.id, uri, false) },
                onDeletePhoto = { path -> onDeletePhoto(job.id, path, false) }
            )

            // Financial Summary & Record Payment
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment & Financials",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Final Agreed Amount:")
                        Text("${shopSettings.currencySymbol}${job.finalAmount}", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Advance Paid:")
                        Text("${shopSettings.currencySymbol}${job.advancePaid}")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Remaining Balance Due:", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${shopSettings.currencySymbol}${job.pendingBalance}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (job.pendingBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Payment")
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        AddPaymentDialog(
            job = job,
            currencySymbol = shopSettings.currencySymbol,
            onDismiss = { showPaymentDialog = false },
            onSave = { amount, mode, ref ->
                onAddPayment(job, amount, mode, ref)
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Ticket ${job.jobTicketNumber}?") },
            text = { Text("Are you sure you want to permanently delete this repair ticket and associated data?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteJob(job)
                        showDeleteConfirmDialog = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
