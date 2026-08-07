package com.example.hotelpms.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hotelpms.data.models.BookingStatus
import com.example.hotelpms.data.models.RoomStatus

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val roomId: String,
    val roomNumber: String,
    val building: String,
    val floor: Int,
    val roomType: String,
    val pricePerNight: Double,
    val status: RoomStatus,
    val maxOccupancy: Int
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val name: String,
    val phone: String,
    val email: String?,
    val idCard: String?,
    val customerType: String,
    val address: String?,
    val notes: String?
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val bookingId: String,
    val customerId: String,
    val roomId: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalAmount: Double,
    val status: BookingStatus,
    val paymentStatus: String,
    val channel: String
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val expenseId: String,
    val category: String,
    val description: String,
    val amount: Double,
    val date: String,
    val paidBy: String
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val settingId: String,
    val category: String,
    val name: String,
    val value: String
)
