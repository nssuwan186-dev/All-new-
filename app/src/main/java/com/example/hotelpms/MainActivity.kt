package com.example.hotelpms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelpms.ui.screens.*
import com.example.hotelpms.ui.theme.DBHotelUPTheme
import com.example.hotelpms.ui.theme.Slate900
import com.example.hotelpms.ui.theme.Teal600
import com.example.hotelpms.ui.viewmodel.HotelViewModel

enum class NavItem(val label: String, val icon: ImageVector, val tag: String) {
    Dashboard("ผังห้อง", Icons.Default.Dashboard, "nav_dashboard"),
    Rooms("ห้องพัก", Icons.Default.MeetingRoom, "nav_rooms"),
    Bookings("การจอง", Icons.Default.CalendarMonth, "nav_bookings"),
    Customers("ลูกค้า", Icons.Default.People, "nav_customers"),
    Expenses("รายจ่าย", Icons.Default.ReceiptLong, "nav_expenses"),
    Settings("ตั้งค่า", Icons.Default.Settings, "nav_settings"),
    Assistant("ผู้ช่วย AI", Icons.Default.SmartToy, "nav_assistant")
}

class MainActivity : ComponentActivity() {

    private val viewModel: HotelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DBHotelUPTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: HotelViewModel) {
    var selectedItem by remember { mutableStateOf(NavItem.Dashboard) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "DB-Hotel-UP",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Slate900
                            )
                            Text(
                                "ระบบบริหารจัดการโรงแรม & ที่พัก",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = { selectedItem = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp, fontWeight = if (selectedItem == item) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Teal600,
                            selectedTextColor = Teal600,
                            indicatorColor = Teal600.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                NavItem.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToBookings = { selectedItem = NavItem.Bookings }
                )
                NavItem.Rooms -> RoomsScreen(viewModel = viewModel)
                NavItem.Bookings -> BookingsScreen(viewModel = viewModel)
                NavItem.Customers -> CustomersScreen(viewModel = viewModel)
                NavItem.Expenses -> ExpensesScreen(viewModel = viewModel)
                NavItem.Settings -> SettingsScreen(viewModel = viewModel)
                NavItem.Assistant -> AssistantScreen(viewModel = viewModel)
            }
        }
    }
}
