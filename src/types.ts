export enum RoomStatus {
  Available = 'ว่าง',
  Occupied = 'มีผู้เข้าพัก',
  Cleaning = 'ทำความสะอาด',
  Maintenance = 'ซ่อมบำรุง'
}

export enum BookingStatus {
  Confirmed = 'ยืนยันแล้ว',
  CheckedIn = 'เข้าพักแล้ว',
  CheckedOut = 'เช็คเอาท์แล้ว',
  Cancelled = 'ยกเลิก'
}

export enum PaymentMethod {
  Cash = 'Cash',
  QRCode = 'QRCode',
  Transfer = 'Transfer'
}

export interface Room {
  room_id: string;
  room_number: string;
  building: string;
  floor: number;
  room_type: string;
  price_per_night: number;
  status: RoomStatus;
  max_occupancy: number;
}

export interface Customer {
  customer_id: string;
  name: string;
  phone: string;
  email?: string;
  id_card?: string;
  customer_type: 'Regular' | 'VIP';
  address?: string;
  notes?: string;
}

export interface Booking {
  booking_id: string;
  customer_id: string;
  room_id: string;
  check_in_date: string;
  check_out_date: string;
  total_amount: number;
  status: BookingStatus;
  payment_status: 'Pending' | 'Paid' | 'Partial';
  channel: 'Walk-in' | 'Line' | 'Phone' | 'Online';
}

export interface Expense {
  expense_id: string;
  category: string;
  description: string;
  amount: number;
  date: string;
  paid_by: string;
}

export interface HotelSetting {
  setting_id: string;
  category: string;
  name: string;
  value: string;
}
