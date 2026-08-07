package com.example.hotelpms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.data.models.RoomStatus
import com.example.hotelpms.ui.theme.*
import com.example.hotelpms.ui.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(viewModel: HotelViewModel) {
    val rooms by viewModel.rooms.collectAsState()
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomToEdit by remember { mutableStateOf<RoomEntity?>(null) }
    var filterBuilding by remember { mutableStateOf("All") }

    val filteredRooms = rooms.filter {
        filterBuilding == "All" || it.building.equals(filterBuilding, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddRoomDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Room") },
                text = { Text("เพิ่มห้องพัก") },
                containerColor = Teal600,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_room_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate50)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "รายการห้องพักทั้งหมด",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                // Building Filter
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("All", "A", "B", "N").forEach { b ->
                        FilterChip(
                            selected = filterBuilding == b,
                            onClick = { filterBuilding = b },
                            label = { Text(if (b == "All") "ทั้งหมด" else "อาคาร $b") }
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRooms, key = { it.roomId }) { room ->
                    RoomRowCard(
                        room = room,
                        onEdit = { roomToEdit = room }
                    )
                }
            }
        }
    }

    if (showAddRoomDialog) {
        AddEditRoomDialog(
            room = null,
            onDismiss = { showAddRoomDialog = false },
            onSave = { newRoom ->
                viewModel.addRoom(newRoom)
                showAddRoomDialog = false
            }
        )
    }

    roomToEdit?.let { room ->
        AddEditRoomDialog(
            room = room,
            onDismiss = { roomToEdit = null },
            onSave = { updatedRoom ->
                viewModel.updateRoom(updatedRoom)
                roomToEdit = null
            }
        )
    }
}

@Composable
fun RoomRowCard(
    room: RoomEntity,
    onEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(room.roomNumber, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "อาคาร ${room.building} ชั้น ${room.floor}",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Slate700
                        )
                    }
                }
                Text("${room.roomType} | รองรับได้ ${room.maxOccupancy} ท่าน", fontSize = 12.sp, color = Slate700)
                Text("฿${room.pricePerNight.toInt()} / คืน", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Teal600)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = when (room.status) {
                        RoomStatus.Available -> StatusAvailableBg
                        RoomStatus.Occupied -> StatusOccupiedBg
                        RoomStatus.Cleaning -> StatusCleaningBg
                        RoomStatus.Maintenance -> StatusMaintenanceBg
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = room.status.label,
                        color = when (room.status) {
                            RoomStatus.Available -> StatusAvailableText
                            RoomStatus.Occupied -> StatusOccupiedText
                            RoomStatus.Cleaning -> StatusCleaningText
                            RoomStatus.Maintenance -> StatusMaintenanceText
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Room", tint = Slate700)
                }
            }
        }
    }
}

@Composable
fun AddEditRoomDialog(
    room: RoomEntity?,
    onDismiss: () -> Unit,
    onSave: (RoomEntity) -> Unit
) {
    var roomNumber by remember { mutableStateOf(room?.roomNumber ?: "") }
    var building by remember { mutableStateOf(room?.building ?: "A") }
    var floorText by remember { mutableStateOf(room?.floor?.toString() ?: "1") }
    var roomType by remember { mutableStateOf(room?.roomType ?: "Standard") }
    var priceText by remember { mutableStateOf(room?.pricePerNight?.toInt()?.toString() ?: "400") }
    var maxOccupancyText by remember { mutableStateOf(room?.maxOccupancy?.toString() ?: "2") }
    var status by remember { mutableStateOf(room?.status ?: RoomStatus.Available) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (room == null) "เพิ่มห้องพักใหม่" else "แก้ไขข้อมูลห้อง ${room.roomNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("หมายเลขห้อง (เช่น A101)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text("อาคาร (A/B/N)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("ชั้น") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = roomType,
                    onValueChange = { roomType = it },
                    label = { Text("ประเภทห้อง (Standard/Deluxe/Suite)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("ราคาต่อคืน (บาท)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxOccupancyText,
                        onValueChange = { maxOccupancyText = it },
                        label = { Text("จำนวนผู้พักสูงสุด") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roomNumber.isNotBlank()) {
                        val floor = floorText.toIntOrNull() ?: 1
                        val price = priceText.toDoubleOrNull() ?: 400.0
                        val maxOcc = maxOccupancyText.toIntOrNull() ?: 2
                        val newRoom = RoomEntity(
                            roomId = room?.roomId ?: roomNumber,
                            roomNumber = roomNumber,
                            building = building,
                            floor = floor,
                            roomType = roomType,
                            pricePerNight = price,
                            status = status,
                            maxOccupancy = maxOcc
                        )
                        onSave(newRoom)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal600)
            ) {
                Text("บันทึก")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ยกเลิก") }
        }
    )
}
