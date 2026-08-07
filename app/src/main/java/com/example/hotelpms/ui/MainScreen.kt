package com.example.hotelpms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.ui.components.AiAssistantDialog
import com.example.hotelpms.ui.components.NewBookingDialog
import com.example.hotelpms.ui.components.NewCustomerDialog
import com.example.hotelpms.ui.components.NewExpenseDialog
import com.example.hotelpms.ui.components.NewRoomDialog
import com.example.hotelpms.ui.screens.BookingsScreen
import com.example.hotelpms.ui.screens.CustomersScreen
import com.example.hotelpms.ui.screens.DashboardScreen
import com.example.hotelpms.ui.screens.ExpensesScreen
import com.example.hotelpms.ui.screens.RoomsScreen
import com.example.hotelpms.ui.screens.SettingsScreen
import com.example.hotelpms.ui.theme.Navy800
import com.example.hotelpms.ui.theme.SkyBlue600

enum class NavigationTab(val label: String) {
    Dashboard("ภาพรวม"),
    Rooms("ห้องพัก"),
    Bookings("การจอง"),
    Customers("ลูกค้า"),
    Expenses("รายจ่าย"),
    Settings("ตั้งค่า")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HotelViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.Dashboard) }

    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showAiAssistantDialog by remember { mutableStateOf(false) }
    var showNewBookingDialog by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showNewRoomDialog by remember { mutableStateOf(false) }
    var showNewExpenseDialog by remember { mutableStateOf(false) }

    var preSelectedRoomForBooking by remember { mutableStateOf<RoomEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DB-Hotel-UP PMS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "ระบบบริหารจัดการหอพักและโรงแรม",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAiAssistantDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Navy800,
                contentColor = Color.White
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val icon = when (tab) {
                        NavigationTab.Dashboard -> Icons.Default.Dashboard
                        NavigationTab.Rooms -> Icons.Default.Bed
                        NavigationTab.Bookings -> Icons.Default.BookOnline
                        NavigationTab.Customers -> Icons.Default.People
                        NavigationTab.Expenses -> Icons.Default.Receipt
                        NavigationTab.Settings -> Icons.Default.Settings
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = { Icon(icon, contentDescription = tab.label) },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SkyBlue600,
                            selectedTextColor = SkyBlue600,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.White
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAiAssistantDialog = true },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                text = { Text("AI Assistant") },
                containerColor = SkyBlue600,
                contentColor = Color.White,
                shape = CircleShape
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.Dashboard -> DashboardScreen(
                    rooms = rooms,
                    bookings = bookings,
                    customers = customers,
                    onNavigateToRooms = { selectedTab = NavigationTab.Rooms },
                    onNavigateToBookings = { selectedTab = NavigationTab.Bookings },
                    onOpenNewBookingDialog = {
                        preSelectedRoomForBooking = null
                        showNewBookingDialog = true
                    }
                )

                NavigationTab.Rooms -> RoomsScreen(
                    rooms = rooms,
                    onUpdateRoomStatus = { roomId, status ->
                        viewModel.updateRoomStatus(roomId, status)
                    },
                    onOpenAddRoomDialog = { showNewRoomDialog = true },
                    onOpenQuickCheckIn = { room ->
                        preSelectedRoomForBooking = room
                        showNewBookingDialog = true
                    }
                )

                NavigationTab.Bookings -> BookingsScreen(
                    bookings = bookings,
                    customers = customers,
                    rooms = rooms,
                    onUpdateBookingStatus = { bookingId, status, paymentStatus ->
                        viewModel.updateBookingStatus(bookingId, status, paymentStatus)
                    },
                    onOpenAddBookingDialog = {
                        preSelectedRoomForBooking = null
                        showNewBookingDialog = true
                    }
                )

                NavigationTab.Customers -> CustomersScreen(
                    customers = customers,
                    onOpenAddCustomerDialog = { showNewCustomerDialog = true }
                )

                NavigationTab.Expenses -> ExpensesScreen(
                    expenses = expenses,
                    onOpenAddExpenseDialog = { showNewExpenseDialog = true }
                )

                NavigationTab.Settings -> SettingsScreen(
                    settings = settings,
                    onUpdateSetting = { id, valStr -> viewModel.updateSetting(id, valStr) }
                )
            }

            // Dialog Overlays
            if (showAiAssistantDialog) {
                AiAssistantDialog(
                    messages = chatMessages,
                    isThinking = isAiThinking,
                    onSendMessage = { text -> viewModel.sendChatMessage(text) },
                    onDismiss = { showAiAssistantDialog = false }
                )
            }

            if (showNewBookingDialog) {
                NewBookingDialog(
                    rooms = rooms,
                    customers = customers,
                    initialSelectedRoom = preSelectedRoomForBooking,
                    onConfirm = { custId, rmId, checkIn, checkOut, amount, ch, payStatus ->
                        viewModel.addBooking(custId, rmId, checkIn, checkOut, amount, ch, payStatus)
                        showNewBookingDialog = false
                    },
                    onDismiss = { showNewBookingDialog = false }
                )
            }

            if (showNewCustomerDialog) {
                NewCustomerDialog(
                    onConfirm = { name, phone, email, idCard, type, addr ->
                        viewModel.addCustomer(name, phone, email, idCard, type, addr)
                        showNewCustomerDialog = false
                    },
                    onDismiss = { showNewCustomerDialog = false }
                )
            }

            if (showNewRoomDialog) {
                NewRoomDialog(
                    onConfirm = { num, bld, flr, type, price ->
                        viewModel.addRoom(num, bld, flr, type, price)
                        showNewRoomDialog = false
                    },
                    onDismiss = { showNewRoomDialog = false }
                )
            }

            if (showNewExpenseDialog) {
                NewExpenseDialog(
                    onConfirm = { cat, desc, amount, paid ->
                        viewModel.addExpense(cat, desc, amount, paid)
                        showNewExpenseDialog = false
                    },
                    onDismiss = { showNewExpenseDialog = false }
                )
            }
        }
    }
}
