package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.RepairJob
import com.example.data.model.ShopSettings
import com.example.ui.components.AddCustomerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditJobScreen(
    existingJob: RepairJob?,
    customers: List<Customer>,
    shopSettings: ShopSettings,
    onBack: () -> Unit,
    onSaveJob: (RepairJob) -> Unit,
    onAddCustomer: (Customer, (Long) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var ticketNumber by remember {
        mutableStateOf(existingJob?.jobTicketNumber ?: "DMW-${shopSettings.jobSequenceCounter}")
    }
    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.id == existingJob?.customerId } ?: customers.firstOrNull())
    }
    var customerName by remember { mutableStateOf(existingJob?.customerName ?: selectedCustomer?.name ?: "") }
    var customerPhone by remember { mutableStateOf(existingJob?.customerPhone ?: selectedCustomer?.phone ?: "") }

    var selectedBrand by remember { mutableStateOf(existingJob?.brand ?: "Apple") }
    var deviceModel by remember { mutableStateOf(existingJob?.deviceModel ?: "") }
    var imeiNumber by remember { mutableStateOf(existingJob?.imeiNumber ?: "") }
    var problemDescription by remember { mutableStateOf(existingJob?.problemDescription ?: "") }
    var estimatedCostText by remember { mutableStateOf(existingJob?.estimatedCost?.toString() ?: "") }
    var advancePaidText by remember { mutableStateOf(existingJob?.advancePaid?.toString() ?: "0") }
    var warrantyDaysText by remember { mutableStateOf(existingJob?.warrantyDays?.toString() ?: "30") }
    var technicianNotes by remember { mutableStateOf(existingJob?.technicianNotes ?: "") }

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val brands = listOf("Apple", "Samsung", "Xiaomi / Redmi", "Vivo", "Oppo", "OnePlus", "Realme", "Motorola", "Google Pixel", "Nothing", "Other")

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (existingJob == null) "New Repair Ticket" else "Edit Ticket ${existingJob.jobTicketNumber}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Ticket Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ticket Identifier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ticketNumber,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = { showAddCustomerDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Customer")
                    }
                }
            }

            // Customer Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Customer Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Customer Picker Dropdown
                    ExposedDropdownMenuBox(
                        expanded = customerExpanded,
                        onExpandedChange = { customerExpanded = !customerExpanded }
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                customerExpanded = true
                            },
                            label = { Text("Customer Name *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true
                        )

                        if (customers.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = customerExpanded,
                                onDismissRequest = { customerExpanded = false }
                            ) {
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text("${cust.name} (${cust.phone})") },
                                        onClick = {
                                            selectedCustomer = cust
                                            customerName = cust.name
                                            customerPhone = cust.phone
                                            customerExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Mobile Number *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Device Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Device Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Brand Dropdown
                    ExposedDropdownMenuBox(
                        expanded = brandExpanded,
                        onExpandedChange = { brandExpanded = !brandExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedBrand,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Brand *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = brandExpanded,
                            onDismissRequest = { brandExpanded = false }
                        ) {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
                                    onClick = {
                                        selectedBrand = brand
                                        brandExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = deviceModel,
                        onValueChange = { deviceModel = it },
                        label = { Text("Device Model * (e.g. iPhone 13 Pro, Galaxy S22)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = imeiNumber,
                        onValueChange = { imeiNumber = it },
                        label = { Text("IMEI Number / Serial (15 digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (imeiNumber.length == 15) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Valid 15 Digits",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }

            // Problem & Financials Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Problem & Financials",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = problemDescription,
                        onValueChange = { problemDescription = it },
                        label = { Text("Reported Problem / Issue *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = estimatedCostText,
                            onValueChange = { estimatedCostText = it },
                            label = { Text("Estimated Cost (${shopSettings.currencySymbol}) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = advancePaidText,
                            onValueChange = { advancePaidText = it },
                            label = { Text("Advance Paid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = warrantyDaysText,
                            onValueChange = { warrantyDaysText = it },
                            label = { Text("Warranty (Days)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = technicianNotes,
                        onValueChange = { technicianNotes = it },
                        label = { Text("Technician Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Save Ticket Button
            Button(
                onClick = {
                    val cost = estimatedCostText.toDoubleOrNull()
                    val advance = advancePaidText.toDoubleOrNull() ?: 0.0
                    val warranty = warrantyDaysText.toIntOrNull() ?: 30

                    if (customerName.isBlank() || customerPhone.isBlank()) {
                        errorText = "Customer Name and Mobile Number are required"
                    } else if (deviceModel.isBlank() || problemDescription.isBlank()) {
                        errorText = "Device Model and Problem Description are required"
                    } else if (cost == null || cost < 0) {
                        errorText = "Please enter valid Estimated Cost"
                    } else {
                        val paymentStatus = if (advance >= cost) "PAID" else if (advance > 0) "PARTIAL" else "PENDING"
                        val jobToSave = RepairJob(
                            id = existingJob?.id ?: 0L,
                            jobTicketNumber = ticketNumber,
                            customerId = selectedCustomer?.id ?: 0L,
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            brand = selectedBrand,
                            deviceModel = deviceModel.trim(),
                            imeiNumber = imeiNumber.trim(),
                            problemDescription = problemDescription.trim(),
                            estimatedCost = cost,
                            advancePaid = advance,
                            finalAmount = cost,
                            paymentStatus = paymentStatus,
                            jobStatus = existingJob?.jobStatus ?: "RECEIVED",
                            receivedDate = existingJob?.receivedDate ?: System.currentTimeMillis(),
                            deliveryDate = existingJob?.deliveryDate ?: 0L,
                            beforePhotoUris = existingJob?.beforePhotoUris ?: emptyList(),
                            afterPhotoUris = existingJob?.afterPhotoUris ?: emptyList(),
                            technicianNotes = technicianNotes.trim(),
                            warrantyDays = warranty
                        )
                        onSaveJob(jobToSave)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (existingJob == null) "Create Repair Ticket" else "Save Ticket Updates", fontSize = 16.sp)
            }
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onSave = { newCust ->
                onAddCustomer(newCust) { newId ->
                    val custWithId = newCust.copy(id = newId)
                    selectedCustomer = custWithId
                    customerName = custWithId.name
                    customerPhone = custWithId.phone
                    showAddCustomerDialog = false
                }
            }
        )
    }
}
