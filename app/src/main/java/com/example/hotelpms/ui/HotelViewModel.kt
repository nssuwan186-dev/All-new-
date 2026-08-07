package com.example.hotelpms.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotelpms.data.HotelRepository
import com.example.hotelpms.data.local.AppDatabase
import com.example.hotelpms.data.local.BookingEntity
import com.example.hotelpms.data.local.CustomerEntity
import com.example.hotelpms.data.local.ExpenseEntity
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.data.local.SettingEntity
import com.example.hotelpms.data.models.BookingStatus
import com.example.hotelpms.data.models.RoomStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HotelRepository

    val rooms: StateFlow<List<RoomEntity>>
    val customers: StateFlow<List<CustomerEntity>>
    val bookings: StateFlow<List<BookingEntity>>
    val expenses: StateFlow<List<ExpenseEntity>>
    val settings: StateFlow<List<SettingEntity>>

    // Search and filter states
    val roomSearchQuery = MutableStateFlow("")
    val selectedBuilding = MutableStateFlow("All")
    val selectedRoomStatus = MutableStateFlow<RoomStatus?>(null)

    val customerSearchQuery = MutableStateFlow("")

    // AI Assistant Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ai", "สวัสดีครับ ยินดีต้อนรับสู่ DB-Hotel-UP PMS มีอะไรให้ AI ช่วยเหลือสอบถามได้เลยครับ (เช่น เช็คห้องว่าง, ดูยอดขายวันนี้, แนะนำห้องพัก)")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).hotelDao()
        repository = HotelRepository(dao)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        rooms = repository.rooms.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        customers = repository.customers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        bookings = repository.bookings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        expenses = repository.expenses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        settings = repository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun updateRoomStatus(roomId: String, status: RoomStatus) {
        viewModelScope.launch {
            repository.updateRoomStatus(roomId, status)
        }
    }

    fun addRoom(number: String, building: String, floor: Int, type: String, price: Double) {
        viewModelScope.launch {
            val id = "R" + System.currentTimeMillis().toString().takeLast(4)
            val newRoom = RoomEntity(
                roomId = id,
                roomNumber = number,
                building = building,
                floor = floor,
                roomType = type,
                pricePerNight = price,
                status = RoomStatus.Available,
                maxOccupancy = 2
            )
            repository.addRoom(newRoom)
        }
    }

    fun addCustomer(name: String, phone: String, email: String?, idCard: String?, type: String, address: String?) {
        viewModelScope.launch {
            val id = "C" + System.currentTimeMillis().toString().takeLast(4)
            val newCustomer = CustomerEntity(
                customerId = id,
                name = name,
                phone = phone,
                email = email,
                idCard = idCard,
                customerType = type,
                address = address,
                notes = null
            )
            repository.addCustomer(newCustomer)
        }
    }

    fun addBooking(
        customerId: String,
        roomId: String,
        checkIn: String,
        checkOut: String,
        amount: Double,
        channel: String,
        paymentStatus: String
    ) {
        viewModelScope.launch {
            val id = "BK" + System.currentTimeMillis().toString().takeLast(4)
            val newBooking = BookingEntity(
                bookingId = id,
                customerId = customerId,
                roomId = roomId,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                totalAmount = amount,
                status = BookingStatus.Confirmed,
                paymentStatus = paymentStatus,
                channel = channel
            )
            repository.addBooking(newBooking)
            // Mark room as occupied if today
            repository.updateRoomStatus(roomId, RoomStatus.Occupied)
        }
    }

    fun updateBookingStatus(bookingId: String, status: BookingStatus, paymentStatus: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status, paymentStatus)
            if (status == BookingStatus.CheckedOut) {
                // Find booking to get room and set cleaning
                val currentBookings = bookings.value
                val b = currentBookings.find { it.bookingId == bookingId }
                if (b != null) {
                    repository.updateRoomStatus(b.roomId, RoomStatus.Cleaning)
                }
            } else if (status == BookingStatus.CheckedIn) {
                val currentBookings = bookings.value
                val b = currentBookings.find { it.bookingId == bookingId }
                if (b != null) {
                    repository.updateRoomStatus(b.roomId, RoomStatus.Occupied)
                }
            }
        }
    }

    fun addExpense(category: String, desc: String, amount: Double, paidBy: String) {
        viewModelScope.launch {
            val id = "E" + System.currentTimeMillis().toString().takeLast(4)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val expense = ExpenseEntity(
                expenseId = id,
                category = category,
                description = desc,
                amount = amount,
                date = today,
                paidBy = paidBy
            )
            repository.addExpense(expense)
        }
    }

    fun updateSetting(settingId: String, value: String) {
        viewModelScope.launch {
            repository.updateSetting(settingId, value)
        }
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val current = _chatMessages.value.toMutableList()
        current.add(ChatMessage("user", userText))
        _chatMessages.value = current
        _isAiThinking.value = true

        viewModelScope.launch {
            delay(800) // Simulate fast AI processing
            val roomList = rooms.value
            val bookingList = bookings.value

            val totalRooms = roomList.size
            val availableCount = roomList.count { it.status == RoomStatus.Available }
            val occupiedCount = roomList.count { it.status == RoomStatus.Occupied }
            val cleaningCount = roomList.count { it.status == RoomStatus.Cleaning }
            val totalRevenue = bookingList.sumOf { it.totalAmount }

            val query = userText.lowercase()
            val replyText = when {
                query.contains("ว่าง") || query.contains("available") -> {
                    val availableRoomsStr = roomList.filter { it.status == RoomStatus.Available }
                        .joinToString(", ") { "ห้อง ${it.roomNumber} (${it.roomType} ฿${it.pricePerNight.toInt()})" }
                    "ขณะนี้มีห้องว่างทั้งหมด $availableCount ห้อง (จาก $totalRooms ห้อง)\n$availableRoomsStr"
                }
                query.contains("รายได้") || query.contains("ยอดขาย") || query.contains(" revenue") -> {
                    "ยอดรวมรายได้จากการจองทั้งหมดปัจจุบันคือ ฿${String.format("%,.2f", totalRevenue)} บาทครับ"
                }
                query.contains("ภาพรวม") || query.contains("สถานะ") -> {
                    "📊 ภาพรวมโรงแรม DB-Hotel-UP:\n• ห้องว่าง: $availableCount ห้อง\n• มีผู้เข้าพัก: $occupiedCount ห้อง\n• กำลังทำความสะอาด: $cleaningCount ห้อง\n• ยอดจองรวม: ฿${String.format("%,.2f", totalRevenue)}"
                }
                query.contains("แนะนำ") || query.contains("ห้องไหนดี") -> {
                    val recommended = roomList.firstOrNull { it.status == RoomStatus.Available }
                    if (recommended != null) {
                        "ขอแนะนำห้อง ${recommended.roomNumber} อาคาร ${recommended.building} ชั้น ${recommended.floor} (${recommended.roomType}) ราคาเพียง ฿${recommended.pricePerNight.toInt()} / คืนครับ!"
                    } else {
                        "ขออภัยครับ ขณะนี้ห้องพักเต็มทุกห้องแล้ว"
                    }
                }
                else -> {
                    "ยินดีให้บริการครับ สามารถถามฉันเกี่ยวกับ:\n- เช็คห้องว่างทั้งหมด\n- สรุปภาพรวมและสถานะห้อง\n- รายได้และยอดขาย\n- แนะนำห้องพักสำหรับลูกค้า"
                }
            }

            _chatMessages.value = _chatMessages.value + ChatMessage("ai", replyText)
            _isAiThinking.value = false
        }
    }
}
