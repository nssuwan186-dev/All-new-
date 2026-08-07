package com.example.hotelpms.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelpms.data.local.BookingEntity
import com.example.hotelpms.data.local.CustomerEntity
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.data.models.BookingStatus
import com.example.hotelpms.data.models.RoomStatus
import com.example.hotelpms.ui.theme.*
import com.example.hotelpms.ui.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HotelViewModel,
    onNavigateToBookings: () -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val customers by viewModel.customers.collectAsState()

    val selectedBuilding by viewModel.selectedBuilding.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedRoomForAction by remember { mutableStateOf<RoomEntity?>(null) }

    val totalRooms = rooms.size
    val availableCount = rooms.count { it.status == RoomStatus.Available }
    val occupiedCount = rooms.count { it.status == RoomStatus.Occupied }
    val cleaningCount = rooms.count { it.status == RoomStatus.Cleaning }
    val maintenanceCount = rooms.count { it.status == RoomStatus.Maintenance }

    val todayRevenue = bookings
        .filter { it.status == BookingStatus.CheckedIn || it.paymentStatus == "Paid" }
        .sumOf { it.totalAmount }

    val filteredRooms = rooms.filter { room ->
        val matchesBuilding = selectedBuilding == "All" || room.building.equals(selectedBuilding, ignoreCase = true)
        val matchesStatus = selectedStatus == null || room.status == selectedStatus
        val matchesSearch = searchQuery.isBlank() ||
                room.roomNumber.contains(searchQuery, ignoreCase = true) ||
                room.building.contains(searchQuery, ignoreCase = true) ||
                room.roomType.contains(searchQuery, ignoreCase = true)
        matchesBuilding && matchesStatus && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "ทั้งหมด",
                value = "$totalRooms",
                color = Slate800,
                icon = Icons.Default.MeetingRoom,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "ว่าง",
                value = "$availableCount",
                color = Emerald600,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "ไม่ว่าง",
                value = "$occupiedCount",
                color = Blue600,
                icon = Icons.Default.Hotel,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "ทำความสะอาด",
                value = "$cleaningCount",
                color = Amber500,
                icon = Icons.Default.CleaningServices,
                modifier = Modifier.weight(1f)
            )
        }

        // Search & Filter Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("ค้นหาหมายเลขห้อง / อาคาร / ประเภท...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Building Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("อาคาร:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    listOf("All", "A", "B", "N").forEach { building ->
                        FilterChip(
                            selected = selectedBuilding == building,
                            onClick = { viewModel.setBuildingFilter(building) },
                            label = { Text(if (building == "All") "ทั้งหมด" else "อาคาร $building") }
                        )
                    }
                }

                // Status Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("สถานะ:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("ทั้งหมด") }
                    )
                    RoomStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = {
                                viewModel.setStatusFilter(if (selectedStatus == status) null else status)
                            },
                            label = { Text(status.label) }
                        )
                    }
                }
            }
        }

        // Room Grid Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ผังห้องพัก (${filteredRooms.size} ห้อง)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Surface(
                color = Teal600.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Teal600, modifier = Modifier.size(16.dp))
                    Text(
                        text = "รายได้วันนี้: ฿${String.format("%,.0f", todayRevenue)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Teal600
                    )
                }
            }
        }

        // Room Cards Grid
        if (filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("ไม่พบข้อมูลห้องพักที่ตรงกับเงื่อนไข", color = Slate700)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredRooms, key = { it.roomId }) { room ->
                    val activeBooking = bookings.find {
                        it.roomId == room.roomId && it.status == BookingStatus.CheckedIn
                    }
                    val guestCustomer = customers.find { it.customerId == activeBooking?.customerId }

                    RoomGridCard(
                        room = room,
                        guestName = guestCustomer?.name,
                        onClick = { selectedRoomForAction = room }
                    )
                }
            }
        }
    }

    // Room Detail / Quick Action Dialog
    selectedRoomForAction?.let { room ->
        val activeBooking = bookings.find {
            it.roomId == room.roomId && it.status == BookingStatus.CheckedIn
        }
        val guestCustomer = customers.find { it.customerId == activeBooking?.customerId }

        RoomActionDialog(
            room = room,
            activeBooking = activeBooking,
            guestCustomer = guestCustomer,
            onDismiss = { selectedRoomForAction = null },
            onStatusChange = { newStatus ->
                viewModel.updateRoomStatus(room.roomId, newStatus)
                selectedRoomForAction = null
            },
            onCheckOut = {
                activeBooking?.let { booking ->
                    viewModel.checkOutBooking(booking.bookingId, room.roomId)
                }
                selectedRoomForAction = null
            },
            onNavigateToBookings = {
                selectedRoomForAction = null
                onNavigateToBookings()
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(title, fontSize = 12.sp, color = Slate700, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        }
    }
}

@Composable
fun RoomGridCard(
    room: RoomEntity,
    guestName: String?,
    onClick: () -> Unit
) {
    val (statusBg, statusText, statusLabel) = when (room.status) {
        RoomStatus.Available -> Triple(StatusAvailableBg, StatusAvailableText, "ว่าง")
        RoomStatus.Occupied -> Triple(StatusOccupiedBg, StatusOccupiedText, "เข้าพัก")
        RoomStatus.Cleaning -> Triple(StatusCleaningBg, StatusCleaningText, "ทำความสะอาด")
        RoomStatus.Maintenance -> Triple(StatusMaintenanceBg, StatusMaintenanceText, "ซ่อมบำรุง")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("room_card_${room.roomNumber}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Header Color Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "อาคาร ${room.building} (ชั้น ${room.floor})",
                        fontSize = 11.sp,
                        color = statusText,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = statusText,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Body
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = room.roomNumber,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
                Text(
                    text = room.roomType,
                    fontSize = 12.sp,
                    color = Slate700
                )
                Text(
                    text = "฿${room.pricePerNight.toInt()} / คืน",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Teal600
                )

                if (!guestName.isNullOrBlank() && room.status == RoomStatus.Occupied) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Blue600, modifier = Modifier.size(14.dp))
                        Text(
                            text = guestName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue600,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoomActionDialog(
    room: RoomEntity,
    activeBooking: BookingEntity?,
    guestCustomer: CustomerEntity?,
    onDismiss: () -> Unit,
    onStatusChange: (RoomStatus) -> Unit,
    onCheckOut: () -> Unit,
    onNavigateToBookings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Hotel, contentDescription = null, tint = Teal600)
                Text("จัดการห้องพัก ${room.roomNumber}")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("อาคาร ${room.building} ชั้น ${room.floor} | ${room.roomType} | ฿${room.pricePerNight.toInt()}/คืน", fontSize = 13.sp, color = Slate700)

                if (room.status == RoomStatus.Occupied && activeBooking != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusOccupiedBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ข้อมูลผู้เข้าพักปัจจุบัน:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusOccupiedText)
                            Text("ผู้พัก: ${guestCustomer?.name ?: "ไม่ระบุ"}", fontSize = 13.sp, color = Slate900)
                            Text("เบอร์โทร: ${guestCustomer?.phone ?: "-"}", fontSize = 12.sp, color = Slate700)
                            Text("วันที่เข้าพัก: ${activeBooking.checkInDate} ถึง ${activeBooking.checkOutDate}", fontSize = 12.sp, color = Slate700)
                            Text("สถานะชำระเงิน: ${activeBooking.paymentStatus}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }
                    }
                }

                Text("เปลี่ยนสถานะห้องพัก:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)

                RoomStatus.entries.forEach { status ->
                    OutlinedButton(
                        onClick = { onStatusChange(status) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (room.status == status) Teal600.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(status.label, fontWeight = if (room.status == status) FontWeight.Bold else FontWeight.Normal)
                            if (room.status == status) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Teal600)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (room.status == RoomStatus.Occupied && activeBooking != null) {
                    Button(
                        onClick = onCheckOut,
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LogOut, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("เช็คเอาท์ (Check-Out)")
                    }
                } else if (room.status == RoomStatus.Available) {
                    Button(
                        onClick = onNavigateToBookings,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("เปิดจอง / เช็คอินห้องนี้")
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ปิดหน้าต่าง")
                }
            }
        }
    )
}
