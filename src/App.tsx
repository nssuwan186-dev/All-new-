import React, { useState, useMemo } from 'react';
import {
  LayoutDashboard, BedDouble, Users, CalendarDays, Receipt, Settings,
  LogOut, Plus, Search, CheckCircle, Clock, ShieldAlert,
  Bot, Send, QrCode, Building2, Phone, Mail, DollarSign, Edit3, X, Check
} from 'lucide-react';
import { Room, RoomStatus, Customer, Booking, BookingStatus, Expense, HotelSetting } from './types';
import { MOCK_ROOMS, MOCK_CUSTOMERS, MOCK_BOOKINGS, MOCK_EXPENSES, MOCK_SETTINGS } from './constants';

export default function App() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'rooms' | 'bookings' | 'customers' | 'expenses' | 'settings' | 'assistant'>('dashboard');

  const [rooms, setRooms] = useState<Room[]>(MOCK_ROOMS);
  const [customers, setCustomers] = useState<Customer[]>(MOCK_CUSTOMERS);
  const [bookings, setBookings] = useState<Booking[]>(MOCK_BOOKINGS);
  const [expenses, setExpenses] = useState<Expense[]>(MOCK_EXPENSES);
  const [settings, setSettings] = useState<HotelSetting[]>(MOCK_SETTINGS);

  // Filters
  const [selectedBuilding, setSelectedBuilding] = useState<string>('All');
  const [selectedStatus, setSelectedStatus] = useState<RoomStatus | 'All'>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Selected Room for Quick Modal
  const [selectedRoomModal, setSelectedRoomModal] = useState<Room | null>(null);

  // Modals
  const [showAddRoom, setShowAddRoom] = useState(false);
  const [showAddCustomer, setShowAddCustomer] = useState(false);
  const [showNewBooking, setShowNewBooking] = useState(false);
  const [showAddExpense, setShowAddExpense] = useState(false);
  const [qrModalBooking, setQrModalBooking] = useState<Booking | null>(null);

  // AI Assistant Chat State
  const [chatMessages, setChatMessages] = useState<{ role: 'user' | 'model'; text: string }[]>([
    { role: 'model', text: 'สวัสดีครับ 👋 ระบบ DB-Hotel-UP พร้อมใช้งานแล้ว มีอะไรให้ช่วยเหลือไหมครับ?' }
  ]);
  const [chatInput, setChatInput] = useState('');
  const [isAiLoading, setIsAiLoading] = useState(false);

  // Calculations
  const stats = useMemo(() => {
    const total = rooms.length;
    const available = rooms.filter(r => r.status === RoomStatus.Available).length;
    const occupied = rooms.filter(r => r.status === RoomStatus.Occupied).length;
    const cleaning = rooms.filter(r => r.status === RoomStatus.Cleaning).length;
    const maintenance = rooms.filter(r => r.status === RoomStatus.Maintenance).length;

    const totalRev = bookings.filter(b => b.payment_status === 'Paid' || b.status === BookingStatus.CheckedIn).reduce((sum, b) => sum + b.total_amount, 0);
    const totalExp = expenses.reduce((sum, e) => sum + e.amount, 0);

    return { total, available, occupied, cleaning, maintenance, totalRev, totalExp, netProfit: totalRev - totalExp };
  }, [rooms, bookings, expenses]);

  const filteredRooms = useMemo(() => {
    return rooms.filter(r => {
      const matchBuilding = selectedBuilding === 'All' || r.building === selectedBuilding;
      const matchStatus = selectedStatus === 'All' || r.status === selectedStatus;
      const matchSearch = searchQuery === '' || r.room_number.toLowerCase().includes(searchQuery.toLowerCase()) || r.room_type.toLowerCase().includes(searchQuery.toLowerCase());
      return matchBuilding && matchStatus && matchSearch;
    });
  }, [rooms, selectedBuilding, selectedStatus, searchQuery]);

  const promptPayId = useMemo(() => {
    return settings.find(s => s.name === 'PromptPay ID')?.value || '081-234-5678';
  }, [settings]);

  // Actions
  const handleUpdateRoomStatus = (roomId: string, newStatus: RoomStatus) => {
    setRooms(prev => prev.map(r => r.room_id === roomId ? { ...r, status: newStatus } : r));
    if (selectedRoomModal && selectedRoomModal.room_id === roomId) {
      setSelectedRoomModal(prev => prev ? { ...prev, status: newStatus } : null);
    }
  };

  const handleCheckOut = (bookingId: string, roomId: string) => {
    setBookings(prev => prev.map(b => b.booking_id === bookingId ? { ...b, status: BookingStatus.CheckedOut, payment_status: 'Paid' } : b));
    handleUpdateRoomStatus(roomId, RoomStatus.Cleaning);
    setSelectedRoomModal(null);
  };

  const handleSendAiMessage = (msg: string) => {
    if (!msg.trim()) return;
    setChatMessages(prev => [...prev, { role: 'user', text: msg }]);
    setChatInput('');
    setIsAiLoading(true);

    setTimeout(() => {
      let reply = '';
      const lower = msg.toLowerCase();
      if (lower.includes('ห้อง') && (lower.includes('ว่าง') || lower.includes('available'))) {
        reply = `ขณะนี้มีห้องว่าง ${stats.available} ห้องจากทั้งหมด ${stats.total} ห้องครับ`;
      } else if (lower.includes('รายได้') || lower.includes('ยอดขาย') || lower.includes('กำไร')) {
        reply = `📊 สรุปบัญชีโรงแรม:\n• รายได้รวม: ฿${stats.totalRev.toLocaleString()}\n• รายจ่ายรวม: ฿${stats.totalExp.toLocaleString()}\n• กำไรสุทธิ: ฿${stats.netProfit.toLocaleString()}`;
      } else if (lower.includes('สถานะ') || lower.includes('ภาพรวม')) {
        reply = `🏨 สรุปสถานะห้องพัก:\n• ทั้งหมด: ${stats.total} ห้อง\n• 🟢 ว่าง: ${stats.available}\n• 🔵 มีผู้เข้าพัก: ${stats.occupied}\n• 🟡 ทำความสะอาด: ${stats.cleaning}\n• 🔴 ซ่อมบำรุง: ${stats.maintenance}`;
      } else {
        reply = 'ผมยินดีช่วยเหลือครับ สามารถสอบถาม: เช็คห้องว่าง, ดูสรุปรายได้, ภาพรวมโรงแรม หรือ แนะนำห้องพักได้ครับ';
      }
      setChatMessages(prev => [...prev, { role: 'model', text: reply }]);
      setIsAiLoading(false);
    }, 500);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col md:flex-row">
      {/* Sidebar Navigation */}
      <aside className="w-full md:w-64 bg-slate-900 text-white flex-shrink-0 flex flex-col justify-between p-4">
        <div>
          <div className="flex items-center gap-3 px-2 py-3 border-b border-slate-800 mb-6">
            <Building2 className="w-8 h-8 text-teal-400" />
            <div>
              <h1 className="font-extrabold text-lg text-white tracking-wide">DB-Hotel-UP</h1>
              <p className="text-xs text-slate-400">Property Management System</p>
            </div>
          </div>

          <nav className="space-y-1">
            {[
              { id: 'dashboard', label: 'ผังห้องพัก (Dashboard)', icon: LayoutDashboard },
              { id: 'rooms', label: 'จัดการห้องพัก', icon: BedDouble },
              { id: 'bookings', label: 'การจอง & เข้าพัก', icon: CalendarDays },
              { id: 'customers', label: 'รายชื่อลูกค้า', icon: Users },
              { id: 'expenses', label: 'รายจ่าย & บัญชี', icon: Receipt },
              { id: 'settings', label: 'ตั้งค่าระบบ', icon: Settings },
              { id: 'assistant', label: 'ผู้ช่วย AI (Staff Assistant)', icon: Bot },
            ].map(item => {
              const IconComp = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id as any)}
                  className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive ? 'bg-teal-600 text-white shadow-md' : 'text-slate-300 hover:bg-slate-800'
                  }`}
                >
                  <IconComp className="w-5 h-5" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </div>

        <div className="border-t border-slate-800 pt-4 mt-6 text-xs text-slate-400 text-center">
          DB-Hotel-UP v1.0.0
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 p-4 md:p-8 overflow-y-auto">
        {/* TOP STATS BANNER */}
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center gap-3">
            <div className="p-2.5 bg-slate-100 rounded-lg text-slate-800"><BedDouble className="w-5 h-5" /></div>
            <div><p className="text-xs text-slate-500 font-medium">ห้องทั้งหมด</p><p className="text-xl font-bold text-slate-900">{stats.total}</p></div>
          </div>
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center gap-3">
            <div className="p-2.5 bg-emerald-100 rounded-lg text-emerald-600"><CheckCircle className="w-5 h-5" /></div>
            <div><p className="text-xs text-slate-500 font-medium">ห้องว่าง</p><p className="text-xl font-bold text-slate-900">{stats.available}</p></div>
          </div>
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center gap-3">
            <div className="p-2.5 bg-blue-100 rounded-lg text-blue-600"><Users className="w-5 h-5" /></div>
            <div><p className="text-xs text-slate-500 font-medium">เข้าพักอยู่</p><p className="text-xl font-bold text-slate-900">{stats.occupied}</p></div>
          </div>
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center gap-3">
            <div className="p-2.5 bg-amber-100 rounded-lg text-amber-600"><Clock className="w-5 h-5" /></div>
            <div><p className="text-xs text-slate-500 font-medium">ทำความสะอาด</p><p className="text-xl font-bold text-slate-900">{stats.cleaning}</p></div>
          </div>
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center gap-3 col-span-2 md:col-span-1">
            <div className="p-2.5 bg-teal-100 rounded-lg text-teal-600"><DollarSign className="w-5 h-5" /></div>
            <div><p className="text-xs text-slate-500 font-medium">รายได้วันนี้</p><p className="text-xl font-bold text-teal-600">฿{stats.totalRev.toLocaleString()}</p></div>
          </div>
        </div>

        {/* TAB 1: DASHBOARD */}
        {activeTab === 'dashboard' && (
          <div className="space-y-6">
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-4">
              <div className="flex flex-col md:flex-row gap-4 justify-between items-center">
                <div className="relative w-full md:w-80">
                  <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    placeholder="ค้นหาห้องพัก..."
                    className="w-full pl-9 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
                  />
                </div>

                <div className="flex flex-wrap gap-2 items-center w-full md:w-auto">
                  <span className="text-xs font-semibold text-slate-600">อาคาร:</span>
                  {['All', 'A', 'B', 'N'].map(b => (
                    <button
                      key={b}
                      onClick={() => setSelectedBuilding(b)}
                      className={`px-3 py-1 rounded-md text-xs font-medium ${selectedBuilding === b ? 'bg-teal-600 text-white' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'}`}
                    >
                      {b === 'All' ? 'ทั้งหมด' : `อาคาร ${b}`}
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex flex-wrap gap-2 items-center pt-2 border-t border-slate-100">
                <span className="text-xs font-semibold text-slate-600">สถานะ:</span>
                <button
                  onClick={() => setSelectedStatus('All')}
                  className={`px-2.5 py-1 rounded-md text-xs font-medium ${selectedStatus === 'All' ? 'bg-slate-800 text-white' : 'bg-slate-100 text-slate-700'}`}
                >
                  ทั้งหมด
                </button>
                {Object.values(RoomStatus).map(st => (
                  <button
                    key={st}
                    onClick={() => setSelectedStatus(st)}
                    className={`px-2.5 py-1 rounded-md text-xs font-medium ${selectedStatus === st ? 'bg-teal-600 text-white' : 'bg-slate-100 text-slate-700'}`}
                  >
                    {st}
                  </button>
                ))}
              </div>
            </div>

            {/* Room Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
              {filteredRooms.map(room => {
                const activeBooking = bookings.find(b => b.room_id === room.room_id && b.status === BookingStatus.CheckedIn);
                const guestCustomer = customers.find(c => c.customer_id === activeBooking?.customer_id);

                const statusColor = room.status === RoomStatus.Available ? 'bg-emerald-50 border-emerald-500 text-emerald-800'
                  : room.status === RoomStatus.Occupied ? 'bg-blue-600 text-white shadow-blue-100'
                  : room.status === RoomStatus.Cleaning ? 'bg-amber-400 text-white'
                  : 'bg-rose-500 text-white';

                return (
                  <div
                    key={room.room_id}
                    onClick={() => setSelectedRoomModal(room)}
                    className={`p-4 rounded-xl border-2 cursor-pointer transition-transform transform hover:-translate-y-1 shadow-sm ${statusColor}`}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <span className="text-xs font-bold opacity-80">อาคาร {room.building} ({room.floor}F)</span>
                      <span className="text-[10px] uppercase tracking-wider font-extrabold px-1.5 py-0.5 rounded bg-black/10">{room.status}</span>
                    </div>
                    <p className="text-2xl font-extrabold mb-1">{room.room_number}</p>
                    <p className="text-xs font-medium opacity-90">{room.room_type}</p>
                    <p className="text-xs font-bold mt-2">฿{room.price_per_night}/คืน</p>

                    {guestCustomer && room.status === RoomStatus.Occupied && (
                      <p className="text-xs font-semibold mt-2 pt-2 border-t border-white/20 truncate">👤 {guestCustomer.name}</p>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* TAB 2: ROOMS */}
        {activeTab === 'rooms' && (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold text-slate-900">รายการห้องพักทั้งหมด</h2>
              <button onClick={() => setShowAddRoom(true)} className="px-4 py-2 bg-teal-600 text-white text-sm font-semibold rounded-lg flex items-center gap-2">
                <Plus className="w-4 h-4" /> เพิ่มห้องพัก
              </button>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
              <table className="w-full text-sm text-left text-slate-700">
                <thead className="text-xs text-slate-500 uppercase bg-slate-50 border-b">
                  <tr>
                    <th className="px-4 py-3">หมายเลขห้อง</th>
                    <th className="px-4 py-3">อาคาร / ชั้น</th>
                    <th className="px-4 py-3">ประเภท</th>
                    <th className="px-4 py-3">ราคา/คืน</th>
                    <th className="px-4 py-3">สถานะ</th>
                    <th className="px-4 py-3 text-right">การจัดการ</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {rooms.map(room => (
                    <tr key={room.room_id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-bold text-slate-900">{room.room_number}</td>
                      <td className="px-4 py-3">อาคาร {room.building} (ชั้น {room.floor})</td>
                      <td className="px-4 py-3">{room.room_type}</td>
                      <td className="px-4 py-3 font-semibold text-teal-600">฿{room.price_per_night}</td>
                      <td className="px-4 py-3">
                        <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-800">
                          {room.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <button onClick={() => setSelectedRoomModal(room)} className="text-slate-600 hover:text-teal-600 font-medium">แก้ไข</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 3: BOOKINGS */}
        {activeTab === 'bookings' && (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold text-slate-900">การจองและเช็คอินเข้าพัก</h2>
              <button onClick={() => setShowNewBooking(true)} className="px-4 py-2 bg-teal-600 text-white text-sm font-semibold rounded-lg flex items-center gap-2">
                <Plus className="w-4 h-4" /> สร้างการจอง/เช็คอิน
              </button>
            </div>

            <div className="space-y-3">
              {bookings.map(b => {
                const cust = customers.find(c => c.customer_id === b.customer_id);
                const rm = rooms.find(r => r.room_id === b.room_id);

                return (
                  <div key={b.booking_id} className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-bold text-slate-900">#{b.booking_id}</span>
                        <span className="px-2 py-0.5 rounded text-xs font-bold bg-teal-100 text-teal-800">{b.status}</span>
                        <span className={`px-2 py-0.5 rounded text-xs font-bold ${b.payment_status === 'Paid' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'}`}>
                          {b.payment_status === 'Paid' ? 'ชำระแล้ว' : 'รอชำระ'}
                        </span>
                      </div>
                      <p className="text-sm font-semibold text-slate-800">ห้อง: {rm?.room_number || b.room_id} | ลูกค้า: {cust?.name || b.customer_id}</p>
                      <p className="text-xs text-slate-500">{b.check_in_date} ถึง {b.check_out_date} ({b.channel})</p>
                    </div>

                    <div className="flex items-center gap-3">
                      <p className="text-lg font-bold text-teal-600">฿{b.total_amount.toLocaleString()}</p>
                      <button onClick={() => setQrModalBooking(b)} className="px-3 py-1.5 border border-teal-600 text-teal-600 rounded-lg text-xs font-semibold flex items-center gap-1 hover:bg-teal-50">
                        <QrCode className="w-4 h-4" /> PromptPay QR
                      </button>
                      {b.status === BookingStatus.CheckedIn && (
                        <button onClick={() => handleCheckOut(b.booking_id, b.room_id)} className="px-3 py-1.5 bg-rose-500 text-white rounded-lg text-xs font-semibold hover:bg-rose-600">
                          เช็คเอาท์
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* TAB 4: CUSTOMERS */}
        {activeTab === 'customers' && (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold text-slate-900">รายชื่อลูกค้า ({customers.length} ท่าน)</h2>
              <button onClick={() => setShowAddCustomer(true)} className="px-4 py-2 bg-teal-600 text-white text-sm font-semibold rounded-lg flex items-center gap-2">
                <Plus className="w-4 h-4" /> เพิ่มลูกค้าใหม่
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {customers.map(c => (
                <div key={c.customer_id} className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex justify-between items-center">
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-bold text-slate-900">{c.name}</p>
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${c.customer_type === 'VIP' ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-700'}`}>
                        {c.customer_type}
                      </span>
                    </div>
                    <p className="text-xs text-slate-600 mt-1">📞 {c.phone}</p>
                    {c.id_card && <p className="text-xs text-slate-500">💳 {c.id_card}</p>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 5: EXPENSES */}
        {activeTab === 'expenses' && (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold text-slate-900">รายจ่ายและบัญชี</h2>
              <button onClick={() => setShowAddExpense(true)} className="px-4 py-2 bg-teal-600 text-white text-sm font-semibold rounded-lg flex items-center gap-2">
                <Plus className="w-4 h-4" /> บันทึกรายจ่าย
              </button>
            </div>

            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex justify-around text-center">
              <div><p className="text-xs text-slate-500">รายรับรวม</p><p className="text-lg font-bold text-emerald-600">฿{stats.totalRev.toLocaleString()}</p></div>
              <div className="border-r border-slate-200"></div>
              <div><p className="text-xs text-slate-500">รายจ่ายรวม</p><p className="text-lg font-bold text-rose-500">฿{stats.totalExp.toLocaleString()}</p></div>
              <div className="border-r border-slate-200"></div>
              <div><p className="text-xs text-slate-500">กำไรสุทธิ</p><p className="text-lg font-bold text-teal-600">฿{stats.netProfit.toLocaleString()}</p></div>
            </div>

            <div className="space-y-2">
              {expenses.map(e => (
                <div key={e.expense_id} className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-sm flex justify-between items-center">
                  <div>
                    <p className="font-bold text-slate-900 text-sm">{e.description}</p>
                    <p className="text-xs text-slate-500">หมวดหมู่: {e.category} | วันที่: {e.date}</p>
                  </div>
                  <p className="font-bold text-rose-500 text-sm">-฿{e.amount.toLocaleString()}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 6: SETTINGS */}
        {activeTab === 'settings' && (
          <div className="space-y-4">
            <h2 className="text-lg font-bold text-slate-900">ตั้งค่าระบบโรงแรม</h2>
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4 max-w-xl">
              {settings.map(s => (
                <div key={s.setting_id} className="flex justify-between items-center border-b pb-3">
                  <div>
                    <p className="font-bold text-slate-900 text-sm">{s.name}</p>
                    <p className="text-xs text-slate-500">หมวดหมู่: {s.category}</p>
                  </div>
                  <span className="font-bold text-teal-600 text-sm bg-teal-50 px-3 py-1 rounded-lg border border-teal-200">{s.value}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 7: AI ASSISTANT */}
        {activeTab === 'assistant' && (
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-4 flex flex-col h-[550px]">
            <div className="flex items-center gap-2 border-b pb-3 mb-4">
              <Bot className="w-6 h-6 text-teal-600" />
              <div>
                <h3 className="font-bold text-slate-900">ผู้ช่วยปัญญาประดิษฐ์ (AI Staff Assistant)</h3>
                <p className="text-xs text-slate-500">ระบบสอบถามข้อมูลโรงแรม สถานะห้องพัก และยอดขาย</p>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto space-y-3 p-2">
              {chatMessages.map((m, idx) => (
                <div key={idx} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-xs md:max-w-md p-3 rounded-xl text-sm ${m.role === 'user' ? 'bg-teal-600 text-white' : 'bg-slate-100 text-slate-900'}`}>
                    {m.text}
                  </div>
                </div>
              ))}
              {isAiLoading && <p className="text-xs text-slate-400 italic">AI กำลังวิเคราะห์ข้อมูล...</p>}
            </div>

            <div className="border-t pt-3 flex gap-2">
              <input
                type="text"
                value={chatInput}
                onChange={e => setChatInput(e.target.value)}
                placeholder="ถามเกี่ยวกับห้องว่าง หรือ รายได้..."
                className="flex-1 border px-3 py-2 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
                onKeyDown={e => e.key === 'Enter' && handleSendAiMessage(chatInput)}
              />
              <button onClick={() => handleSendAiMessage(chatInput)} className="px-4 py-2 bg-teal-600 text-white rounded-lg text-sm font-bold flex items-center gap-1">
                <Send className="w-4 h-4" /> ส่ง
              </button>
            </div>
          </div>
        )}
      </main>

      {/* ROOM ACTION MODAL */}
      {selectedRoomModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl p-6 max-w-sm w-full space-y-4">
            <div className="flex justify-between items-center border-b pb-2">
              <h3 className="font-bold text-lg">ห้อง {selectedRoomModal.room_number}</h3>
              <button onClick={() => setSelectedRoomModal(null)}><X className="w-5 h-5 text-slate-400" /></button>
            </div>

            <p className="text-sm text-slate-600">อาคาร {selectedRoomModal.building} ชั้น {selectedRoomModal.floor} | {selectedRoomModal.room_type}</p>
            <p className="text-sm font-bold text-teal-600">฿{selectedRoomModal.price_per_night} / คืน</p>

            <div className="space-y-2 pt-2 border-t">
              <p className="text-xs font-bold text-slate-700">เปลี่ยนสถานะห้องพัก:</p>
              {Object.values(RoomStatus).map(st => (
                <button
                  key={st}
                  onClick={() => handleUpdateRoomStatus(selectedRoomModal.room_id, st)}
                  className={`w-full py-2 px-3 rounded-lg text-xs font-bold border text-left flex justify-between items-center ${selectedRoomModal.status === st ? 'bg-teal-600 text-white border-teal-600' : 'bg-slate-50 text-slate-700 hover:bg-slate-100'}`}
                >
                  <span>{st}</span>
                  {selectedRoomModal.status === st && <Check className="w-4 h-4" />}
                </button>
              ))}
            </div>

            <button onClick={() => setSelectedRoomModal(null)} className="w-full py-2 bg-slate-200 text-slate-800 text-xs font-bold rounded-lg">
              ปิด
            </button>
          </div>
        </div>
      )}

      {/* PROMPTPAY QR MODAL */}
      {qrModalBooking && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl p-6 max-w-sm w-full text-center space-y-4">
            <h3 className="font-bold text-lg text-slate-900">ชำระเงิน PromptPay QR</h3>
            <p className="text-xl font-extrabold text-teal-600">฿{qrModalBooking.total_amount.toLocaleString()}</p>
            <div className="p-4 bg-slate-100 rounded-xl inline-block border-2 border-teal-600">
              <QrCode className="w-32 h-32 mx-auto text-slate-800" />
              <p className="text-xs font-bold text-teal-600 mt-2">PromptPay: {promptPayId}</p>
            </div>
            <button
              onClick={() => {
                setBookings(prev => prev.map(b => b.booking_id === qrModalBooking.booking_id ? { ...b, payment_status: 'Paid' } : b));
                setQrModalBooking(null);
              }}
              className="w-full py-2.5 bg-emerald-600 text-white font-bold text-sm rounded-lg hover:bg-emerald-700"
            >
              ยืนยันการชำระเงินเรียบร้อย
            </button>
            <button onClick={() => setQrModalBooking(null)} className="text-xs text-slate-500 underline">ปิด</button>
          </div>
        </div>
      )}
    </div>
  );
}
