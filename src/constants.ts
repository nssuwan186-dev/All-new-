import { Room, RoomStatus, Customer, Booking, BookingStatus, Expense, HotelSetting } from './types';

export const MOCK_ROOMS: Room[] = [
  { room_id: 'A101', room_number: 'A101', building: 'A', floor: 1, room_type: 'Standard', price_per_night: 400, status: RoomStatus.Available, max_occupancy: 2 },
  { room_id: 'A102', room_number: 'A102', building: 'A', floor: 1, room_type: 'Standard', price_per_night: 400, status: RoomStatus.Available, max_occupancy: 2 },
  { room_id: 'A103', room_number: 'A103', building: 'A', floor: 1, room_type: 'Standard', price_per_night: 400, status: RoomStatus.Occupied, max_occupancy: 2 },
  { room_id: 'A201', room_number: 'A201', building: 'A', floor: 2, room_type: 'Standard Twin', price_per_night: 500, status: RoomStatus.Available, max_occupancy: 2 },
  { room_id: 'B101', room_number: 'B101', building: 'B', floor: 1, room_type: 'Standard', price_per_night: 400, status: RoomStatus.Cleaning, max_occupancy: 2 },
  { room_id: 'B110', room_number: 'B110', building: 'B', floor: 1, room_type: 'Standard', price_per_night: 400, status: RoomStatus.Occupied, max_occupancy: 2 },
  { room_id: 'N1', room_number: 'N1', building: 'N', floor: 1, room_type: 'Standard', price_per_night: 400, status: RoomStatus.Maintenance, max_occupancy: 2 },
  { room_id: 'N2', room_number: 'N2', building: 'N', floor: 1, room_type: 'Standard', price_per_night: 400, status: RoomStatus.Available, max_occupancy: 2 },
];

export const MOCK_CUSTOMERS: Customer[] = [
  { customer_id: 'CM001', name: 'ชาตรี มงคล', phone: '081-234-5678', customer_type: 'Regular', id_card: '1-1000-12345-67-8' },
  { customer_id: 'CM002', name: 'กมลลักษ์ ใจดี', phone: '089-987-6543', customer_type: 'VIP', id_card: '2-3456-78901-23-4' },
  { customer_id: 'CM003', name: 'สมชาย รักชาติ', phone: '065-432-1111', customer_type: 'Regular' },
];

export const MOCK_BOOKINGS: Booking[] = [
  { booking_id: 'VP01764', customer_id: 'CM001', room_id: 'B110', check_in_date: '2026-08-05', check_out_date: '2026-08-08', total_amount: 1200, status: BookingStatus.CheckedIn, payment_status: 'Pending', channel: 'Walk-in' },
  { booking_id: 'VP01765', customer_id: 'CM002', room_id: 'A103', check_in_date: '2026-08-06', check_out_date: '2026-08-07', total_amount: 400, status: BookingStatus.CheckedIn, payment_status: 'Paid', channel: 'Line' },
  { booking_id: 'VP01766', customer_id: 'CM003', room_id: 'A201', check_in_date: '2026-08-10', check_out_date: '2026-08-11', total_amount: 500, status: BookingStatus.Confirmed, payment_status: 'Pending', channel: 'Phone' },
];

export const MOCK_EXPENSES: Expense[] = [
  { expense_id: 'EX001', category: 'Utilities', description: 'ค่าน้ำ-ค่าไฟประจำเดือน', amount: 4500, date: '2026-08-01', paid_by: 'ผู้จัดการ' },
  { expense_id: 'EX002', category: 'Maintenance', description: 'ซ่อมเครื่องปรับอากาศ Room N1', amount: 1200, date: '2026-08-03', paid_by: 'แอดมิน' },
  { expense_id: 'EX003', category: 'Supplies', description: 'อุปกรณ์ทำความสะอาดและเครื่องใช้', amount: 850, date: '2026-08-05', paid_by: 'พนักงาน' },
];

export const MOCK_SETTINGS: HotelSetting[] = [
  { setting_id: 'SET001', category: 'General', name: 'Hotel Name', value: 'DB-Hotel-UP' },
  { setting_id: 'SET002', category: 'Financial', name: 'VAT Rate', value: '7%' },
  { setting_id: 'SET003', category: 'Policy', name: 'Check-in Time', value: '14:00' },
  { setting_id: 'SET004', category: 'Policy', name: 'Check-out Time', value: '12:00' },
  { setting_id: 'SET005', category: 'Payment', name: 'PromptPay ID', value: '081-234-5678' },
];

export const MOCK_REVENUE_DATA = [
  { date: '1 ส.ค.', revenue: 14150 },
  { date: '2 ส.ค.', revenue: 15750 },
  { date: '3 ส.ค.', revenue: 8100 },
  { date: '4 ส.ค.', revenue: 4500 },
  { date: '5 ส.ค.', revenue: 6200 },
  { date: '6 ส.ค.', revenue: 1600 },
  { date: '7 ส.ค.', revenue: 0 },
];
