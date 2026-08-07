package com.example.hotelpms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelpms.data.local.BookingEntity
import com.example.hotelpms.data.local.CustomerEntity
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.data.models.BookingStatus
import com.example.hotelpms.data.models.PaymentMethod
import com.example.hotelpms.data.models.RoomStatus
import com.example.hotelpms.ui.theme.*
import com.example.hotelpms.ui.viewmodel.HotelViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(viewModel: HotelViewModel) {
    val bookings by viewModel.bookings.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val promptPayId = settings.find { it.name == "PromptPay ID" }?.value ?: "081-234-5678"

    var showNewBookingDialog by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf<BookingStatus?>(null) }
    var bookingForQrPayment by remember { mutableStateOf<BookingEntity?>(null) }

    val filteredBookings = bookings.filter {
        selectedStatusFilter == null || it.status == selectedStatusFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewBookingDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "New Booking") },
                text = { Text("สร้างรายการจอง/เช็คอิน") },
                containerColor = Teal600,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_booking_fab")
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
            Text(
                text = "การจองและเข้าพักทั้งหมด",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("ทั้งหมด") }
                )
                BookingStatus.entries.forEach { bStatus ->
                    FilterChip(
                        selected = selectedStatusFilter == bStatus,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == bStatus) null else bStatus
                        },
                        label = { Text(bStatus.label) }
                    )
                }
            }

            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ไม่พบรายการจองห้องพัก", color = Slate700)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredBookings, key = { it.bookingId }) { booking ->
                        val customer = customers.find { it.customerId == booking.customerId }
                        val room = rooms.find { it.roomId == booking.roomId }

                        BookingCard(
                            booking = booking,
                            customerName = customer?.name ?: "ไม่ระบุชื่อ",
                            roomNumber = room?.roomNumber ?: booking.roomId,
                            onOpenQr = { bookingForQrPayment = booking },
                            onCheckOut = {
                                viewModel.checkOutBooking(booking.bookingId, booking.roomId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewBookingDialog) {
        NewBookingDialog(
            rooms = rooms.filter { it.status == RoomStatus.Available },
            customers = customers,
            promptPayId = promptPayId,
            onDismiss = { showNewBookingDialog = false },
            onCreateCustomer = { newCust ->
                viewModel.addCustomer(newCust)
            },
            onSaveBooking = { newBooking ->
                viewModel.createBooking(newBooking)
                showNewBookingDialog = false
            }
        )
    }

    bookingForQrPayment?.let { booking ->
        val customer = customers.find { it.customerId == booking.customerId }
        val room = rooms.find { it.roomId == booking.roomId }

        PromptPayQrDialog(
            booking = booking,
            customerName = customer?.name ?: "ลูกค้าทั่วไป",
            roomNumber = room?.roomNumber ?: booking.roomId,
            promptPayId = promptPayId,
            onDismiss = { bookingForQrPayment = null },
            onPaymentConfirm = {
                viewModel.createBooking(booking.copy(paymentStatus = "Paid"))
                bookingForQrPayment = null
            }
        )
    }
}

@Composable
fun BookingCard(
    booking: BookingEntity,
    customerName: String,
    roomNumber: String,
    onOpenQr: () -> Unit,
    onCheckOut: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("#${booking.bookingId}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate700)
                    Surface(
                        color = when (booking.status) {
                            BookingStatus.CheckedIn -> StatusOccupiedBg
                            BookingStatus.Confirmed -> StatusAvailableBg
                            BookingStatus.CheckedOut -> Slate100
                            BookingStatus.Cancelled -> StatusMaintenanceBg
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = booking.status.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (booking.status) {
                                BookingStatus.CheckedIn -> StatusOccupiedText
                                BookingStatus.Confirmed -> StatusAvailableText
                                BookingStatus.CheckedOut -> Slate700
                                BookingStatus.Cancelled -> StatusMaintenanceText
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    color = if (booking.paymentStatus == "Paid") StatusAvailableBg else StatusCleaningBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (booking.paymentStatus == "Paid") "ชำระแล้ว" else "รอชำระเงิน",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.paymentStatus == "Paid") StatusAvailableText else StatusCleaningText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Divider(color = Slate100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ห้องพัก: $roomNumber", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Text("ลูกค้า: $customerName", fontSize = 13.sp, color = Slate700)
                    Text("ช่องทาง: ${booking.channel}", fontSize = 12.sp, color = Slate700)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("ยอดรวม:", fontSize = 11.sp, color = Slate700)
                    Text("฿${String.format("%,.0f", booking.totalAmount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Teal600)
                    Text("${booking.checkInDate} ~ ${booking.checkOutDate}", fontSize = 11.sp, color = Slate700)
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (booking.paymentStatus != "Paid") {
                    OutlinedButton(
                        onClick = onOpenQr,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ชำระเงิน PromptPay QR", fontSize = 12.sp)
                    }
                }

                if (booking.status == BookingStatus.CheckedIn) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onCheckOut,
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("เช็คเอาท์", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NewBookingDialog(
    rooms: List<RoomEntity>,
    customers: List<CustomerEntity>,
    promptPayId: String,
    onDismiss: () -> Unit,
    onCreateCustomer: (CustomerEntity) -> Unit,
    onSaveBooking: (BookingEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())

    var selectedRoom by remember { mutableStateOf<RoomEntity?>(rooms.firstOrNull()) }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(customers.firstOrNull()) }

    var checkInDate by remember { mutableStateOf(today) }
    var checkOutDate by remember { mutableStateOf(today) }
    var channel by remember { mutableStateOf("Walk-in") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.Cash) }
    var paymentStatus by remember { mutableStateOf("Paid") }

    var isAddingNewCustomer by remember { mutableStateOf(false) }
    var newCustName by remember { mutableStateOf("") }
    var newCustPhone by remember { mutableStateOf("") }

    val calculatedNights = 1
    val calculatedTotal = (selectedRoom?.pricePerNight ?: 400.0) * calculatedNights

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("สร้างการจอง / เช็คอินเข้าพักใหม่") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Customer Selector
                Text("ข้อมูลลูกค้า:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (isAddingNewCustomer) {
                    OutlinedTextField(
                        value = newCustName,
                        onValueChange = { newCustName = it },
                        label = { Text("ชื่อ-นามสกุล ลูกค้าใหม่") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCustPhone,
                        onValueChange = { newCustPhone = it },
                        label = { Text("เบอร์โทรศัพท์") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCustomer?.let { "${it.name} (${it.phone})" } ?: "ยังไม่ได้เลือกลูกค้า",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate900
                        )
                        TextButton(onClick = { isAddingNewCustomer = true }) {
                            Text("+ ลูกค้าใหม่")
                        }
                    }
                }

                // Room Selector
                Text("เลือกห้องพัก (เฉพาะห้องว่าง):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (rooms.isEmpty()) {
                    Text("ไม่มีห้องว่างในขณะนี้", color = Rose500, fontSize = 12.sp)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rooms.take(4).forEach { rm ->
                            FilterChip(
                                selected = selectedRoom?.roomId == rm.roomId,
                                onClick = { selectedRoom = rm },
                                label = { Text("${rm.roomNumber} (฿${rm.pricePerNight.toInt()})") }
                            )
                        }
                    }
                }

                // Dates
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = checkInDate,
                        onValueChange = { checkInDate = it },
                        label = { Text("วันที่เข้าพัก") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = checkOutDate,
                        onValueChange = { checkOutDate = it },
                        label = { Text("วันที่ออก") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Channel
                Text("ช่องทางการจอง:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Walk-in", "Line", "Phone", "Online").forEach { ch ->
                        FilterChip(
                            selected = channel == ch,
                            onClick = { channel = ch },
                            label = { Text(ch) }
                        )
                    }
                }

                // Summary Cost
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ราคารวมทั้งสิ้น:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("฿${String.format("%,.0f", calculatedTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Teal600)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCustomer = if (isAddingNewCustomer && newCustName.isNotBlank()) {
                        val newC = CustomerEntity(
                            customerId = "CM${System.currentTimeMillis() % 10000}",
                            name = newCustName,
                            phone = newCustPhone,
                            email = null,
                            idCard = null,
                            customerType = "Regular",
                            address = null,
                            notes = null
                        )
                        onCreateCustomer(newC)
                        newC
                    } else {
                        selectedCustomer
                    }

                    if (selectedRoom != null && finalCustomer != null) {
                        val booking = BookingEntity(
                            bookingId = "VP${(10000..99999).random()}",
                            customerId = finalCustomer.customerId,
                            roomId = selectedRoom!!.roomId,
                            checkInDate = checkInDate,
                            checkOutDate = checkOutDate,
                            totalAmount = calculatedTotal,
                            status = BookingStatus.CheckedIn,
                            paymentStatus = paymentStatus,
                            channel = channel
                        )
                        onSaveBooking(booking)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal600)
            ) {
                Text("บันทึกการเข้าพัก")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ยกเลิก") }
        }
    )
}

@Composable
fun PromptPayQrDialog(
    booking: BookingEntity,
    customerName: String,
    roomNumber: String,
    promptPayId: String,
    onDismiss: () -> Unit,
    onPaymentConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Teal600)
                Text("ชำระเงินผ่าน PromptPay QR")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("ยอดชำระ: ฿${String.format("%,.2f", booking.totalAmount)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Teal600)
                Text("ห้อง $roomNumber | ผู้เข้าพัก: $customerName", fontSize = 13.sp, color = Slate700)

                // Simulated PromptPay QR Visual Box
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(2.dp, Teal600, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = "PromptPay QR", modifier = Modifier.size(110.dp), tint = Slate800)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("PromptPay: $promptPayId", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Teal600)
                    }
                }

                Text("สแกนด้วยแอปธนาคารใดก็ได้เพื่อชำระเงิน", fontSize = 11.sp, color = Slate700, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(
                onClick = onPaymentConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ยืนยันได้รับเงินชำระแล้ว")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ปิด") }
        }
    )
}
