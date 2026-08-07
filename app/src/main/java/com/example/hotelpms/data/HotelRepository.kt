package com.example.hotelpms.data

import com.example.hotelpms.data.local.BookingEntity
import com.example.hotelpms.data.local.CustomerEntity
import com.example.hotelpms.data.local.ExpenseEntity
import com.example.hotelpms.data.local.HotelDao
import com.example.hotelpms.data.local.RoomEntity
import com.example.hotelpms.data.local.SettingEntity
import com.example.hotelpms.data.models.BookingStatus
import com.example.hotelpms.data.models.RoomStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HotelRepository(private val hotelDao: HotelDao) {

    val rooms: Flow<List<RoomEntity>> = hotelDao.getAllRooms()
    val customers: Flow<List<CustomerEntity>> = hotelDao.getAllCustomers()
    val bookings: Flow<List<BookingEntity>> = hotelDao.getAllBookings()
    val expenses: Flow<List<ExpenseEntity>> = hotelDao.getAllExpenses()
    val settings: Flow<List<SettingEntity>> = hotelDao.getAllSettings()

    suspend fun seedInitialDataIfEmpty() {
        val existingRooms = hotelDao.getAllRooms().first()
        if (existingRooms.isEmpty()) {
            val defaultRooms = listOf(
                RoomEntity("R001", "101", "A", 1, "Standard", 1200.0, RoomStatus.Available, 2),
                RoomEntity("R002", "102", "A", 1, "Deluxe", 1800.0, RoomStatus.Occupied, 2),
                RoomEntity("R003", "103", "A", 1, "Suite", 2500.0, RoomStatus.Cleaning, 4),
                RoomEntity("R004", "201", "A", 2, "Standard", 1200.0, RoomStatus.Available, 2),
                RoomEntity("R005", "202", "A", 2, "Deluxe", 1800.0, RoomStatus.Maintenance, 2),
                RoomEntity("R006", "203", "B", 2, "Standard", 1200.0, RoomStatus.Available, 2),
                RoomEntity("R007", "301", "B", 3, "Suite", 2500.0, RoomStatus.Available, 4),
                RoomEntity("R008", "302", "B", 3, "Deluxe", 1800.0, RoomStatus.Occupied, 2)
            )
            hotelDao.insertRooms(defaultRooms)

            val defaultCustomers = listOf(
                CustomerEntity("C001", "สมชาย ใจดี", "081-234-5678", "somchai@email.com", "1-1234-56789-01-2", "VIP", "กรุงเทพมหานคร", "ชอบห้องชั้นล่าง"),
                CustomerEntity("C002", "สมหญิง รักษ์ดี", "082-345-6789", "somying@email.com", "1-2345-67890-12-3", "Regular", "เชียงใหม่", null),
                CustomerEntity("C003", "วิไล สุขใจ", "083-456-7890", "wilai@email.com", "1-3456-78901-23-4", "Regular", "พะเยา", null)
            )
            hotelDao.insertCustomers(defaultCustomers)

            val defaultBookings = listOf(
                BookingEntity("BK001", "C001", "R002", "2026-08-05", "2026-08-07", 3600.0, BookingStatus.CheckedIn, "Paid", "Walk-in"),
                BookingEntity("BK002", "C003", "R008", "2026-08-06", "2026-08-08", 3600.0, BookingStatus.CheckedIn, "Paid", "Online")
            )
            hotelDao.insertBookings(defaultBookings)

            val defaultExpenses = listOf(
                ExpenseEntity("E001", "ค่าน้ำ-ค่าไฟ", "ค่าไฟฟ้าประจำเดือน", 12500.0, "2026-08-01", "แอดมิน"),
                ExpenseEntity("E002", "ของใช้ในห้อง", "ผ้าเช็ดตัวและสบู่ซักฟอก", 3400.0, "2026-08-03", "สมชาย")
            )
            hotelDao.insertExpenses(defaultExpenses)

            val defaultSettings = listOf(
                SettingEntity("S001", "General", "hotel_name", "DB-Hotel-UP Management"),
                SettingEntity("S002", "General", "promptpay_id", "0812345678"),
                SettingEntity("S003", "General", "elec_rate", "7.0"),
                SettingEntity("S004", "General", "water_rate", "18.0")
            )
            hotelDao.insertSettings(defaultSettings)
        }
    }

    suspend fun updateRoomStatus(roomId: String, newStatus: RoomStatus) {
        hotelDao.updateRoomStatus(roomId, newStatus)
    }

    suspend fun addRoom(room: RoomEntity) {
        hotelDao.insertRoom(room)
    }

    suspend fun addCustomer(customer: CustomerEntity) {
        hotelDao.insertCustomer(customer)
    }

    suspend fun addBooking(booking: BookingEntity) {
        hotelDao.insertBooking(booking)
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus, paymentStatus: String) {
        hotelDao.updateBookingStatus(bookingId, status, paymentStatus)
    }

    suspend fun addExpense(expense: ExpenseEntity) {
        hotelDao.insertExpense(expense)
    }

    suspend fun updateSetting(settingId: String, value: String) {
        hotelDao.updateSetting(settingId, value)
    }
}
