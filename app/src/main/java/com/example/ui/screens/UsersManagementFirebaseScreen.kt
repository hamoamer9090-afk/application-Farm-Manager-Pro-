package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.firebase.PermissionManager
import com.example.firebase.UserRole
import com.example.firebase.FirebaseRepository
import com.example.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch

@Composable
fun UsersManagementFirebaseScreen(
    viewModel: FarmViewModel,
    accentColor: Color,
    zoom: Float
) {
    val users by viewModel.firebaseUsers.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val repo = remember { FirebaseRepository() }

    var selectedUser by remember { mutableStateOf<Pair<String, UserRole>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "المستخدمين والصلاحيات 👥",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users.entries.toList()) { (uid, role) ->
                val isRoot = role.email.equals(PermissionManager.ROOT_OWNER_EMAIL, ignoreCase = true)

                Card(
                    onClick = { selectedUser = uid to role },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(if (isRoot) accentColor.copy(alpha = 0.2f) else Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRoot) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isRoot) accentColor else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = role.email, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (isRoot) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = accentColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, accentColor)
                                    ) {
                                        Text(
                                            text = "ROOT OWNER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "الدور: ${role.role}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    selectedUser?.let { (uid, userRole) ->
        val isRoot = userRole.email.equals(PermissionManager.ROOT_OWNER_EMAIL, ignoreCase = true)
        var editedRole by remember { mutableStateOf(userRole.role) }
        var editedPermissions by remember { mutableStateOf(userRole.permissions.toMutableMap()) }

        AlertDialog(
            onDismissRequest = { selectedUser = null },
            title = { Text(if (isRoot) "Root Owner (Protected)" else "تعديل صلاحيات: ${userRole.email}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Predefined Permission Matrix
                    val matrix = listOf(
                        "view_all_farms" to "عرض جميع المزارع (Global Admin)",
                        "view_farm" to "عرض المزرعة",
                        "edit_farm" to "تعديل بيانات المزرعة",
                        "delete_farm" to "حذف المزرعة",
                        "manage_inventory" to "إدارة المخزون",
                        "view_financials" to "عرض السجلات المالية"
                    )

                    matrix.forEach { (key, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 14.sp)
                            Switch(
                                checked = isRoot || (editedPermissions[key] == true),
                                onCheckedChange = { editedPermissions[key] = it },
                                enabled = !isRoot
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isRoot) {
                            scope.launch {
                                val updatedUser = userRole.copy(role = editedRole, permissions = editedPermissions)
                                repo.updateUserProfile(uid, updatedUser)
                                selectedUser = null
                            }
                        } else {
                            selectedUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(if (isRoot) "إغلاق" else "حفظ")
                }
            },
            dismissButton = {
                if (!isRoot) {
                    TextButton(onClick = { selectedUser = null }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            }
        )
    }
}
