package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.DrMobileWalaTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val shopSettings by mainViewModel.shopSettings.collectAsStateWithLifecycle()

            DrMobileWalaTheme(darkTheme = shopSettings?.isDarkMode ?: false) {
                MainAppStructure(viewModel = mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val filteredJobs by viewModel.filteredJobs.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()

    val totalJobCount by viewModel.totalJobCount.collectAsStateWithLifecycle()
    val activeJobCount by viewModel.activeJobCount.collectAsStateWithLifecycle()
    val readyForPickupCount by viewModel.readyForPickupCount.collectAsStateWithLifecycle()
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.RepairJobs,
        Screen.Customers,
        Screen.Payments,
        Screen.ImeiLookup
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                when (screen) {
                                    Screen.Dashboard -> Icon(
                                        imageVector = if (selected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                        contentDescription = screen.title
                                    )
                                    Screen.RepairJobs -> Icon(
                                        imageVector = if (selected) Icons.Filled.Build else Icons.Outlined.Build,
                                        contentDescription = screen.title
                                    )
                                    Screen.Customers -> Icon(
                                        imageVector = if (selected) Icons.Filled.People else Icons.Outlined.People,
                                        contentDescription = screen.title
                                    )
                                    Screen.Payments -> Icon(
                                        imageVector = if (selected) Icons.Filled.Payments else Icons.Outlined.Payments,
                                        contentDescription = screen.title
                                    )
                                    Screen.ImeiLookup -> Icon(
                                        imageVector = if (selected) Icons.Filled.QrCode else Icons.Outlined.QrCode,
                                        contentDescription = screen.title
                                    )
                                    else -> Icon(imageVector = Icons.Default.Circle, contentDescription = null)
                                }
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    shopSettings = shopSettings ?: com.example.data.model.ShopSettings(),
                    totalJobsCount = totalJobCount,
                    activeJobsCount = activeJobCount,
                    readyJobsCount = readyForPickupCount,
                    totalRevenue = totalRevenue,
                    filteredJobs = filteredJobs,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setStatusFilter(it) },
                    onJobClick = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) },
                    onNewJobClick = { navController.navigate(Screen.AddEditJob.createRoute()) },
                    onAddCustomerClick = { navController.navigate(Screen.Customers.route) },
                    onImeiSearchClick = { navController.navigate(Screen.ImeiLookup.route) },
                    onSettingsClick = { navController.navigate(Screen.ShopSettings.route) },
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            }

            composable(Screen.RepairJobs.route) {
                RepairJobsScreen(
                    filteredJobs = filteredJobs,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    currencySymbol = shopSettings?.currencySymbol ?: "₹",
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setStatusFilter(it) },
                    onJobClick = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) },
                    onNewJobClick = { navController.navigate(Screen.AddEditJob.createRoute()) }
                )
            }

            composable(Screen.Customers.route) {
                CustomersScreen(
                    customers = allCustomers,
                    onCustomerClick = { custId -> navController.navigate(Screen.CustomerDetail.createRoute(custId)) },
                    onSaveCustomer = { newCust -> viewModel.saveCustomer(newCust) }
                )
            }

            composable(Screen.Payments.route) {
                PaymentsScreen(
                    payments = allPayments,
                    totalRevenue = totalRevenue,
                    currencySymbol = shopSettings?.currencySymbol ?: "₹"
                )
            }

            composable(Screen.ImeiLookup.route) {
                ImeiLookupScreen(
                    allJobs = allJobs,
                    currencySymbol = shopSettings?.currencySymbol ?: "₹",
                    onJobClick = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) }
                )
            }

            composable(Screen.ShopSettings.route) {
                ShopSettingsScreen(
                    settings = shopSettings ?: com.example.data.model.ShopSettings(),
                    onSaveSettings = { updated -> viewModel.updateShopSettings(updated) },
                    onExportBackup = { callback -> viewModel.exportBackup(callback) },
                    onRestoreBackup = { uri, callback -> viewModel.restoreBackup(uri, callback) }
                )
            }

            composable(
                route = Screen.JobDetail.route,
                arguments = listOf(navArgument("jobId") { type = NavType.LongType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getLong("jobId") ?: 0L
                val job = allJobs.find { it.id == jobId }

                JobDetailScreen(
                    job = job,
                    shopSettings = shopSettings ?: com.example.data.model.ShopSettings(),
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.AddEditJob.createRoute(id)) },
                    onStatusUpdate = { targetJob, status -> viewModel.updateJobStatus(targetJob, status) },
                    onAddPayment = { targetJob, amount, mode, ref -> viewModel.addPayment(targetJob, amount, mode, ref) },
                    onAttachPhoto = { id, uri, isBefore -> viewModel.attachPhotoToJob(id, uri, isBefore) },
                    onDeletePhoto = { id, path, isBefore ->
                        job?.let { currentJob ->
                            val updatedBefore = if (isBefore) currentJob.beforePhotoUris.filter { it != path } else currentJob.beforePhotoUris
                            val updatedAfter = if (!isBefore) currentJob.afterPhotoUris.filter { it != path } else currentJob.afterPhotoUris
                            viewModel.saveRepairJob(currentJob.copy(beforePhotoUris = updatedBefore, afterPhotoUris = updatedAfter))
                        }
                    },
                    onDeleteJob = { targetJob -> viewModel.deleteRepairJob(targetJob) }
                )
            }

            composable(
                route = Screen.AddEditJob.route,
                arguments = listOf(navArgument("jobId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getLong("jobId") ?: -1L
                val existingJob = if (jobId != -1L) allJobs.find { it.id == jobId } else null

                AddEditJobScreen(
                    existingJob = existingJob,
                    customers = allCustomers,
                    shopSettings = shopSettings ?: com.example.data.model.ShopSettings(),
                    onBack = { navController.popBackStack() },
                    onSaveJob = { jobToSave -> viewModel.saveRepairJob(jobToSave) },
                    onAddCustomer = { newCust, onSaved -> viewModel.saveCustomer(newCust, onSaved) }
                )
            }

            composable(
                route = Screen.CustomerDetail.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                val customer = allCustomers.find { it.id == customerId }
                val customerJobs = allJobs.filter { it.customerId == customerId }

                CustomerDetailScreen(
                    customer = customer,
                    customerJobs = customerJobs,
                    currencySymbol = shopSettings?.currencySymbol ?: "₹",
                    onBack = { navController.popBackStack() },
                    onJobClick = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) }
                )
            }
        }
    }
}
