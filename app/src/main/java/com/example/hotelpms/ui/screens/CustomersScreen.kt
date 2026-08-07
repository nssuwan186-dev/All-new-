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
import com.example.hotelpms.data.local.CustomerEntity
import com.example.hotelpms.ui.theme.*
import com.example.hotelpms.ui.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(viewModel: HotelViewModel) {
    val customers by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCustomers = customers.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery) ||
                (it.email != null && it.email.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer") },
                text = { Text("เพิ่มลูกค้าใหม่") },
                containerColor = Teal600,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_customer_fab")
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
                text = "รายชื่อลูกค้า (${customers.size} ท่าน)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ค้นหาชื่อ / เบอร์โทรศัพท์ / อีเมล...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCustomers, key = { it.customerId }) { customer ->
                    CustomerCard(customer = customer)
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newCust ->
                viewModel.addCustomer(newCust)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CustomerCard(customer: CustomerEntity) {
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
                    Text(customer.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Surface(
                        color = if (customer.customerType == "VIP") Amber500.copy(alpha = 0.2f) else Slate100,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = customer.customerType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (customer.customerType == "VIP") Amber500 else Slate700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Slate700, modifier = Modifier.size(14.dp))
                    Text(customer.phone, fontSize = 13.sp, color = Slate700)
                }
                if (!customer.email.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Slate700, modifier = Modifier.size(14.dp))
                        Text(customer.email, fontSize = 12.sp, color = Slate700)
                    }
                }
                if (!customer.idCard.isNullOrBlank()) {
                    Text("เลขบัตรประชาชน: ${customer.idCard}", fontSize = 11.sp, color = Slate700)
                }
            }

            Surface(
                color = Teal600.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Teal600,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var idCard by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf("Regular") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("เพิ่มข้อมูลลูกค้าใหม่") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อ-นามสกุล *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("เบอร์โทรศัพท์ *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("อีเมล") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = idCard,
                    onValueChange = { idCard = it },
                    label = { Text("เลขบัตรประจำตัวประชาชน / พาสปอร์ต") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ประเภทลูกค้า:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = customerType == "Regular",
                            onClick = { customerType = "Regular" },
                            label = { Text("ทั่วไป (Regular)") }
                        )
                        FilterChip(
                            selected = customerType == "VIP",
                            onClick = { customerType = "VIP" },
                            label = { Text("VIP ⭐") }
                        )
                    }
                }
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ที่อยู่") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val newCust = CustomerEntity(
                            customerId = "CM${System.currentTimeMillis() % 10000}",
                            name = name,
                            phone = phone,
                            email = email.ifBlank { null },
                            idCard = idCard.ifBlank { null },
                            customerType = customerType,
                            address = address.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                        onSave(newCust)
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
