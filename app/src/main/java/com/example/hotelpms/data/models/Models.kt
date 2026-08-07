package com.example.hotelpms.data.models

enum class RoomStatus(val label: String) {
    Available("ว่าง"),
    Occupied("มีผู้เข้าพัก"),
    Cleaning("ทำความสะอาด"),
    Maintenance("ซ่อมบำรุง")
}

enum class BookingStatus(val label: String) {
    Confirmed("ยืนยันแล้ว"),
    CheckedIn("เข้าพักแล้ว"),
    CheckedOut("เช็คเอาท์แล้ว"),
    Cancelled("ยกเลิก")
}

enum class PaymentMethod(val label: String) {
    Cash("เงินสด"),
    QRCode("PromptPay QR"),
    Transfer("โอนเงิน")
}

data class RoomModel(
    val roomId: String,
    val roomNumber: String,
    val building: String,
    val floor: Int,
    val roomType: String,
    val pricePerNight: Double,
    val status: RoomStatus,
    val maxOccupancy: Int = 2
)

data class CustomerModel(
    val customerId: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val idCard: String? = null,
    val customerType: String = "Regular", // Regular or VIP
    val address: String? = null,
    val notes: String? = null
)

data class BookingModel(
    val bookingId: String,
    val customerId: String,
    val roomId: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalAmount: Double,
    val status: BookingStatus,
    val paymentStatus: String, // Pending, Paid, Partial
    val channel: String // Walk-in, Line, Phone, Online
)

data class ExpenseModel(
    val expenseId: String,
    val category: String,
    val description: String,
    val amount: Double,
    val date: String,
    val paidBy: String
)

data class HotelSettingModel(
    val settingId: String,
    val category: String,
    val name: String,
    val value: String
)
