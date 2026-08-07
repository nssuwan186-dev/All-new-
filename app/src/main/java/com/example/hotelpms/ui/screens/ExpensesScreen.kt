package com.example.hotelpms.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelpms.data.local.ExpenseEntity
import com.example.hotelpms.data.models.BookingStatus
import com.example.hotelpms.ui.theme.*
import com.example.hotelpms.ui.viewmodel.HotelViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: HotelViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val totalRevenue = bookings.filter { it.paymentStatus == "Paid" || it.status == BookingStatus.CheckedIn }.sumOf { it.totalAmount }
    val totalExpense = expenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpense

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Add Expense") },
                text = { Text("บันทึกรายจ่าย") },
                containerColor = Teal600,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_expense_fab")
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
                text = "บัญชีและรายจ่ายโรงแรม",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            // Financial Summary Cards
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("รายรับรวม", fontSize = 12.sp, color = Slate700)
                        Text("฿${String.format("%,.0f", totalRevenue)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                    }
                    Column {
                        Text("รายจ่ายรวม", fontSize = 12.sp, color = Slate700)
                        Text("฿${String.format("%,.0f", totalExpense)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Rose500)
                    }
                    Column {
                        Text("กำไรสุทธิ", fontSize = 12.sp, color = Slate700)
                        Text("฿${String.format("%,.0f", netProfit)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (netProfit >= 0) Teal600 else Rose500)
                    }
                }
            }

            Text("รายการบันทึกรายจ่าย:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(expenses, key = { it.expenseId }) { expense ->
                    ExpenseCard(expense = expense)
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onSave = { newExpense ->
                viewModel.addExpense(newExpense)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun ExpenseCard(expense: ExpenseEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(expense.description, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = expense.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("วันที่: ${expense.date} | ผู้จ่าย: ${expense.paidBy}", fontSize = 12.sp, color = Slate700)
            }

            Text(
                "-฿${String.format("%,.0f", expense.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Rose500
            )
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())

    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Utilities") }
    var amountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("ผู้จัดการ") }
    var date by remember { mutableStateOf(today) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("บันทึกรายการรายจ่าย") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("รายการรายจ่าย (เช่น ค่าน้ำ-ค่าไฟ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("หมวดหมู่:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Utilities", "Maintenance", "Supplies", "Salary").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("จำนวนเงิน (บาท)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paidBy,
                    onValueChange = { paidBy = it },
                    label = { Text("ผู้เบิก/ผู้ชำระ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (description.isNotBlank() && amt > 0) {
                        val newExp = ExpenseEntity(
                            expenseId = "EX${System.currentTimeMillis() % 10000}",
                            category = category,
                            description = description,
                            amount = amt,
                            date = date,
                            paidBy = paidBy
                        )
                        onSave(newExp)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal600)
            ) {
                Text("บันทึก")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ยกเลิก") }
        }
    )
}
