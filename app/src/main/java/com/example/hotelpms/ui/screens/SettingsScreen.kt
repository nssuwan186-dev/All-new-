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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelpms.data.local.SettingEntity
import com.example.hotelpms.ui.theme.*
import com.example.hotelpms.ui.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: HotelViewModel) {
    val settings by viewModel.settings.collectAsState()
    var editingSetting by remember { mutableStateOf<SettingEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "การตั้งค่าระบบโรงแรม",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Teal600)
                    Text("ข้อมูลโรงแรม & นโยบายระบบ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                }

                Divider(color = Slate100)

                settings.forEach { setting ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(setting.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text("หมวดหมู่: ${setting.category}", fontSize = 11.sp, color = Slate700)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(setting.value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Teal600)
                            IconButton(onClick = { editingSetting = setting }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate700)
                            }
                        }
                    }
                    Divider(color = Slate100)
                }
            }
        }
    }

    editingSetting?.let { setting ->
        var valText by remember { mutableStateOf(setting.value) }

        AlertDialog(
            onDismissRequest = { editingSetting = null },
            title = { Text("แก้ไข ${setting.name}") },
            text = {
                OutlinedTextField(
                    value = valText,
                    onValueChange = { valText = it },
                    label = { Text("ค่าการตั้งค่า") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSetting(setting.settingId, valText)
                        editingSetting = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600)
                ) {
                    Text("บันทึก")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSetting = null }) { Text("ยกเลิก") }
            }
        )
    }
}
