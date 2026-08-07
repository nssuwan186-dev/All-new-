package com.example.hotelpms.data.repository

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

class HotelRepository(private val dao: HotelDao) {

    val allRooms: Flow<List<RoomEntity>> = dao.getAllRooms()
    val allCustomers: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    val allBookings: Flow<List<BookingEntity>> = dao.getAllBookings()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allSettings: Flow<List<SettingEntity>> = dao.getAllSettings()

    suspend fun seedInitialDataIfEmpty() {
        if (dao.getAllRooms().first().isEmpty()) {
            val mockRooms = listOf(
                RoomEntity("A101", "A101", "A", 1, "Standard", 400.0, RoomStatus.Available, 2),
                RoomEntity("A102", "A102", "A", 1, "Standard", 400.0, RoomStatus.Available, 2),
                RoomEntity("A103", "A103", "A", 1, "Standard", 400.0, RoomStatus.Occupied, 2),
                RoomEntity("A201", "A201", "A", 2, "Standard Twin", 500.0, RoomStatus.Available, 2),
                RoomEntity("B101", "B101", "B", 1, "Standard", 400.0, RoomStatus.Cleaning, 2),
                RoomEntity("B110", "B110", "B", 1, "Standard", 400.0, RoomStatus.Occupied, 2),
                RoomEntity("N1", "N1", "N", 1, "Standard", 400.0, RoomStatus.Maintenance, 2),
                RoomEntity("N2", "N2", "N", 1, "Standard", 400.0, RoomStatus.Available, 2)
            )
            dao.insertRooms(mockRooms)

            val mockCustomers = listOf(
                CustomerEntity("CM001", "ชาตรี มงคล", "081-234-5678", "chatree@email.com", "1-1000-12345-67-8", "Regular", "กรุงเทพฯ", "ชอบห้องสงบ"),
                CustomerEntity("CM002", "กมลลักษ์ ใจดี", "089-987-6543", "kamonlak@email.com", "2-3456-78901-23-4", "VIP", "เชียงใหม่", "ลูกค้าประจำ VIP"),
                CustomerEntity("CM003", "สมชาย รักชาติ", "065-432-1111", null, null, "Regular", "พะเยา", null)
            )
            dao.insertCustomers(mockCustomers)

            val mockBookings = listOf(
                BookingEntity("VP01764", "CM001", "B110", "2026-08-05", "2026-08-08", 1200.0, BookingStatus.CheckedIn, "Pending", "Walk-in"),
                BookingEntity("VP01765", "CM002", "A103", "2026-08-06", "2026-08-07", 400.0, BookingStatus.CheckedIn, "Paid", "Line"),
                BookingEntity("VP01766", "CM003", "A201", "2026-08-10", "2026-08-11", 500.0, BookingStatus.Confirmed, "Pending", "Phone")
            )
            dao.insertBookings(mockBookings)

            val mockExpenses = listOf(
                ExpenseEntity("EX001", "Utilities", "ค่าน้ำ-ค่าไฟประจำเดือน", 4500.0, "2026-08-01", "ผู้จัดการ"),
                ExpenseEntity("EX002", "Maintenance", "ซ่อมเครื่องปรับอากาศ Room N1", 1200.0, "2026-08-03", "แอดมิน"),
                ExpenseEntity("EX003", "Supplies", "อุปกรณ์ทำความสะอาดและเครื่องใช้", 850.0, "2026-08-05", "พนักงาน")
            )
            dao.insertExpenses(mockExpenses)

            val mockSettings = listOf(
                SettingEntity("SET001", "General", "Hotel Name", "DB-Hotel-UP"),
                SettingEntity("SET002", "Financial", "VAT Rate", "7%"),
                SettingEntity("SET003", "Policy", "Check-in Time", "14:00"),
                SettingEntity("SET004", "Policy", "Check-out Time", "12:00"),
                SettingEntity("SET005", "Payment", "PromptPay ID", "081-234-5678")
            )
            dao.insertSettings(mockSettings)
        }
    }

    suspend fun addRoom(room: RoomEntity) = dao.insertRoom(room)
    suspend fun updateRoomStatus(roomId: String, status: RoomStatus) = dao.updateRoomStatus(roomId, status)
    suspend fun updateRoom(room: RoomEntity) = dao.updateRoom(room)

    suspend fun addCustomer(customer: CustomerEntity) = dao.insertCustomer(customer)

    suspend fun addBooking(booking: BookingEntity) = dao.insertBooking(booking)
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus, paymentStatus: String) =
        dao.updateBookingStatus(bookingId, status, paymentStatus)

    suspend fun addExpense(expense: ExpenseEntity) = dao.insertExpense(expense)

    suspend fun updateSetting(settingId: String, value: String) = dao.updateSetting(settingId, value)
}
