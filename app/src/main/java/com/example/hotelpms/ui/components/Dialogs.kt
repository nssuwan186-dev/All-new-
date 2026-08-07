package com.example.hotelpms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hotelpms.data.local.CustomerEntity
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.data.models.RoomStatus
import com.example.hotelpms.ui.theme.Emerald500
import com.example.hotelpms.ui.theme.Navy800
import com.example.hotelpms.ui.theme.SkyBlue600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookingDialog(
    rooms: List<RoomEntity>,
    customers: List<CustomerEntity>,
    initialSelectedRoom: RoomEntity? = null,
    onConfirm: (customerId: String, roomId: String, checkIn: String, checkOut: String, totalAmount: Double, channel: String, paymentStatus: String) -> Unit,
    onDismiss: () -> Unit
) {
    val availableRooms = rooms.filter { it.status == RoomStatus.Available }

    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var selectedRoom by remember { mutableStateOf(initialSelectedRoom ?: availableRooms.firstOrNull()) }

    var checkInDate by remember { mutableStateOf("2026-08-06") }
    var checkOutDate by remember { mutableStateOf("2026-08-07") }
    var channel by remember { mutableStateOf("Walk-in") }
    var paymentMethod by remember { mutableStateOf("PromptPay QR") }
    var paymentStatus by remember { mutableStateOf("Paid") }

    val calculatedAmount = remember(selectedRoom) {
        selectedRoom?.pricePerNight ?: 1200.0
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "จองห้องพัก / เข้าพักใหม่",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Divider()

                // Customer Selection
                Text("เลือกผู้เข้าพัก", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    customers.take(4).forEach { customer ->
                        FilterChip(
                            selected = selectedCustomer?.customerId == customer.customerId,
                            onClick = { selectedCustomer = customer },
                            label = { Text("${customer.name} (${customer.phone})") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Room Selection
                Text("เลือกห้องพักที่ว่าง", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (availableRooms.isEmpty()) {
                    Text("ไม่มีห้องว่างในขณะนี้", color = Color.Red, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableRooms.forEach { room ->
                            FilterChip(
                                selected = selectedRoom?.roomId == room.roomId,
                                onClick = { selectedRoom = room },
                                label = { Text("ห้อง ${room.roomNumber} (${room.roomType} - ฿${room.pricePerNight.toInt()})") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Dates
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = checkInDate,
                        onValueChange = { checkInDate = it },
                        label = { Text("วันเข้าพัก") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = checkOutDate,
                        onValueChange = { checkOutDate = it },
                        label = { Text("วันออก") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Channel
                Text("ช่องทางการจอง", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Walk-in", "Phone", "Line", "Online").forEach { ch ->
                        FilterChip(
                            selected = channel == ch,
                            onClick = { channel = ch },
                            label = { Text(ch) }
                        )
                    }
                }

                // Payment Method & Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ยอดรวมที่ต้องชำระ", color = Color.White, fontSize = 14.sp)
                            Text(
                                "฿${calculatedAmount.toInt()}",
                                color = Emerald500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("วิธีชำระเงิน: $paymentMethod", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        val cust = selectedCustomer ?: return@Button
                        val rm = selectedRoom ?: return@Button
                        onConfirm(
                            cust.customerId,
                            rm.roomId,
                            checkInDate,
                            checkOutDate,
                            calculatedAmount,
                            channel,
                            paymentStatus
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCustomer != null && selectedRoom != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ยืนยันการจองห้องพัก", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NewCustomerDialog(
    onConfirm: (name: String, phone: String, email: String?, idCard: String?, type: String, address: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var idCard by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf("Regular") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("ลงทะเบียนลูกค้าใหม่", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อ-นามสกุล") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("เบอร์โทรศัพท์") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = idCard,
                    onValueChange = { idCard = it },
                    label = { Text("เลขบัตรประชาชน / พาสปอร์ต") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = customerType == "Regular",
                        onClick = { customerType = "Regular" },
                        label = { Text("ลูกค้าทั่วไป") }
                    )
                    FilterChip(
                        selected = customerType == "VIP",
                        onClick = { customerType = "VIP" },
                        label = { Text("ลูกค้า VIP") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("ยกเลิก") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onConfirm(name, phone, email.ifBlank { null }, idCard.ifBlank { null }, customerType, address.ifBlank { null })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue600)
                    ) {
                        Text("บันทึก")
                    }
                }
            }
        }
    }
}

@Composable
fun NewRoomDialog(
    onConfirm: (number: String, building: String, floor: Int, type: String, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("A") }
    var floorText by remember { mutableStateOf("1") }
    var roomType by remember { mutableStateOf("Standard") }
    var priceText by remember { mutableStateOf("1200") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("เพิ่มห้องพักใหม่", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("หมายเลขห้อง (เช่น 104)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text("อาคาร") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("ชั้น") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = roomType,
                    onValueChange = { roomType = it },
                    label = { Text("ประเภทห้อง (Standard, Deluxe, Suite)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("ราคา/คืน (บาท)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("ยกเลิก") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (roomNumber.isNotBlank()) {
                                onConfirm(
                                    roomNumber,
                                    building,
                                    floorText.toIntOrNull() ?: 1,
                                    roomType,
                                    priceText.toDoubleOrNull() ?: 1200.0
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue600)
                    ) {
                        Text("เพิ่มห้องพัก")
                    }
                }
            }
        }
    }
}

@Composable
fun NewExpenseDialog(
    onConfirm: (category: String, desc: String, amount: Double, paidBy: String) -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf("ค่าน้ำ-ค่าไฟ") }
    var desc by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("แอดมิน") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("บันทึกรายจ่ายใหม่", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("หมวดหมู่ (เช่น ค่าน้ำ-ไฟ, ของใช้, ค่าซ่อม)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("รายละเอียดรายจ่าย") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("จำนวนเงิน (บาท)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("ยกเลิก") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (desc.isNotBlank() && amountText.toDoubleOrNull() != null) {
                                onConfirm(category, desc, amountText.toDouble(), paidBy)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue600)
                    ) {
                        Text("บันทึก")
                    }
                }
            }
        }
    }
}
