package com.example.hotelpms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotelpms.data.local.AppDatabase
import com.example.hotelpms.data.local.BookingEntity
import com.example.hotelpms.data.local.CustomerEntity
import com.example.hotelpms.data.local.ExpenseEntity
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.data.local.SettingEntity
import com.example.hotelpms.data.models.BookingStatus
import com.example.hotelpms.data.models.RoomStatus
import com.example.hotelpms.data.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HotelRepository

    val rooms: StateFlow<List<RoomEntity>>
    val customers: StateFlow<List<CustomerEntity>>
    val bookings: StateFlow<List<BookingEntity>>
    val expenses: StateFlow<List<ExpenseEntity>>
    val settings: StateFlow<List<SettingEntity>>

    // Filters
    private val _selectedBuilding = MutableStateFlow("All")
    val selectedBuilding: StateFlow<String> = _selectedBuilding.asStateFlow()

    private val _selectedStatus = MutableStateFlow<RoomStatus?>(null)
    val selectedStatus: StateFlow<RoomStatus?> = _selectedStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // AI Assistant
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage("model", "สวัสดีครับ 👋 ระบบ DB-Hotel-UP เชื่อมต่อข้อมูลเรียบร้อยแล้ว มีอะไรให้ช่วยเหลือไหมครับ?")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HotelRepository(database.hotelDao())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        rooms = repository.allRooms.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        customers = repository.allCustomers.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        bookings = repository.allBookings.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        expenses = repository.allExpenses.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        settings = repository.allSettings.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun setBuildingFilter(building: String) {
        _selectedBuilding.value = building
    }

    fun setStatusFilter(status: RoomStatus?) {
        _selectedStatus.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateRoomStatus(roomId: String, status: RoomStatus) {
        viewModelScope.launch {
            repository.updateRoomStatus(roomId, status)
        }
    }

    fun addRoom(room: RoomEntity) {
        viewModelScope.launch {
            repository.addRoom(room)
        }
    }

    fun updateRoom(room: RoomEntity) {
        viewModelScope.launch {
            repository.updateRoom(room)
        }
    }

    fun addCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.addCustomer(customer)
        }
    }

    fun createBooking(booking: BookingEntity) {
        viewModelScope.launch {
            repository.addBooking(booking)
            // Automatically set room status to Occupied or Confirmed
            if (booking.status == BookingStatus.CheckedIn) {
                repository.updateRoomStatus(booking.roomId, RoomStatus.Occupied)
            }
        }
    }

    fun checkOutBooking(bookingId: String, roomId: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, BookingStatus.CheckedOut, "Paid")
            repository.updateRoomStatus(roomId, RoomStatus.Cleaning)
        }
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.addExpense(expense)
        }
    }

    fun updateSetting(settingId: String, value: String) {
        viewModelScope.launch {
            repository.updateSetting(settingId, value)
        }
    }

    fun sendAiMessage(message: String) {
        if (message.isBlank()) return

        val userMsg = ChatMessage("user", message)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(600) // Simulate fast processing

            val currentRooms = rooms.value
            val currentBookings = bookings.value
            val currentExpenses = expenses.value

            val totalRooms = currentRooms.size
            val available = currentRooms.count { it.status == RoomStatus.Available }
            val occupied = currentRooms.count { it.status == RoomStatus.Occupied }
            val cleaning = currentRooms.count { it.status == RoomStatus.Cleaning }
            val maintenance = currentRooms.count { it.status == RoomStatus.Maintenance }

            val totalRevenue = currentBookings.filter { it.paymentStatus == "Paid" || it.status == BookingStatus.CheckedIn }
                .sumOf { it.totalAmount }
            val totalExpenseAmt = currentExpenses.sumOf { it.amount }

            val lower = message.lowercase()
            val responseText = when {
                lower.contains("ห้อง") && (lower.contains("ว่าง") || lower.contains("available")) -> {
                    val availList = currentRooms.filter { it.status == RoomStatus.Available }
                        .joinToString(", ") { "${it.roomNumber} (${it.roomType} ฿${it.pricePerNight.toInt()})" }
                    "ขณะนี้มีห้องว่าง $available ห้อง จากทั้งหมด $totalRooms ห้องครับ\n\nรายชื่อห้องว่าง: ${if (availList.isNotEmpty()) availList else "ไม่มีห้องว่าง"}"
                }
                lower.contains("รายได้") || lower.contains("ยอดขาย") || lower.contains("revenue") || lower.contains("กำไร") -> {
                    "📊 สรุปรายได้และค่าใช้จ่ายระบบ:\n• รายได้รวม: ฿${String.format("%,.2f", totalRevenue)}\n• รายจ่ายรวม: ฿${String.format("%,.2f", totalExpenseAmt)}\n• กำไรสุทธิ: ฿${String.format("%,.2f", totalRevenue - totalExpenseAmt)}"
                }
                lower.contains("สถานะ") || lower.contains("ภาพรวม") || lower.contains("overview") || lower.contains("สรุป") -> {
                    "🏨 สรุปภาพรวมโรงแรม DB-Hotel-UP:\n• ห้องทั้งหมด: $totalRooms ห้อง\n• 🟢 ว่าง: $available ห้อง\n• 🔵 มีผู้เข้าพัก: $occupied ห้อง\n• 🟡 ทำความสะอาด: $cleaning ห้อง\n• 🔴 ซ่อมบำรุง: $maintenance ห้อง"
                }
                lower.contains("แนะนำ") || lower.contains("ห้องไหน") -> {
                    val recommend = currentRooms.filter { it.status == RoomStatus.Available }.take(3)
                    if (recommend.isNotEmpty()) {
                        "ห้องว่างแนะนำสำหรับการจอง:\n" + recommend.joinToString("\n") { "• ห้อง ${it.roomNumber} (${it.building} ชั้น ${it.floor}) - ${it.roomType} ราคา ฿${it.pricePerNight.toInt()}/คืน" }
                    } else {
                        "ขออภัยครับ ขณะนี้ไม่มีห้องว่างพร้อมให้บริการ"
                    }
                }
                else -> {
                    "ผมสามารถช่วยตอบคำถามเกี่ยวกับโรงแรมได้ครับ ลองสอบถาม:\n1. 'เช็คห้องว่าง'\n2. 'สรุปรายได้วันนี้'\n3. 'ภาพรวมสถานะห้องพัก'\n4. 'แนะนำห้องพักสำหรับลูกค้า'"
                }
            }

            _chatMessages.value = _chatMessages.value + ChatMessage("model", responseText)
            _isAiLoading.value = false
        }
    }
}
