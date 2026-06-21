package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.firebase.FirebaseRepository
import com.example.firebase.PermissionManager
import com.example.firebase.FarmRecord
import com.example.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch

@Composable
fun FirebaseFarmsListScreen(
    viewModel: FarmViewModel,
    accentColor: Color,
    onFarmSelected: (String, String) -> Unit
) {
    val farms by viewModel.firebaseFarms.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRoleFirebase.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val repo = remember { FirebaseRepository() }
    val context = LocalContext.current

    var showCreateFarmDialog by remember { mutableStateOf(false) }
    var newFarmName by remember { mutableStateOf("") }
    var newFarmPassword by remember { mutableStateOf("") }
    
    var showLoginDialog by remember { mutableStateOf<FarmRecord?>(null) }
    var loginPassword by remember { mutableStateOf("") }
    
    var showAddAdminDialog by remember { mutableStateOf<FarmRecord?>(null) }
    var newAdminEmailOrUid by remember { mutableStateOf("") }

    val currentUid = repo.currentUserId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "مزارعي الذهبية 🌾",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(farms) { farm ->
                val isOwner = farm.owner_uid == currentUid
                val isGlobalAdmin = PermissionManager.isGlobalAdmin()
                val canAddAdmin = isGlobalAdmin || isOwner

                Card(
                    onClick = {
                        if (farm.password.isNotBlank()) {
                            showLoginDialog = farm
                            loginPassword = ""
                        } else {
                            onFarmSelected(farm.name, "")
                        }
                    },
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
                                .background(accentColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = accentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = farm.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "المالك: ${farm.owner_email}", fontSize = 12.sp, color = Color.Gray)
                        }
                        if (canAddAdmin) {
                            IconButton(onClick = { showAddAdminDialog = farm }) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "إضافة مسؤول", tint = accentColor)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showCreateFarmDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("إنشاء مزرعة جديدة")
        }
    }

    if (showCreateFarmDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFarmDialog = false },
            title = { Text("إنشاء مزرعة جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFarmName,
                        onValueChange = { newFarmName = it },
                        label = { Text("اسم المزرعة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newFarmPassword,
                        onValueChange = { newFarmPassword = it },
                        label = { Text("كلمة مرور المزرعة (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFarmName.isNotBlank()) {
                            scope.launch {
                                val result = repo.createFarm(FarmRecord(name = newFarmName, password = newFarmPassword))
                                if (result.isSuccess) {
                                    showCreateFarmDialog = false
                                    newFarmName = ""
                                    newFarmPassword = ""
                                } else {
                                    Toast.makeText(context, "حدث خطأ أثناء الإنشاء", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("إنشاء")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFarmDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    showLoginDialog?.let { farm ->
        AlertDialog(
            onDismissRequest = { showLoginDialog = null },
            title = { Text("تأكيد الدخول للمزرعة") },
            text = {
                Column {
                    Text("المزرعة '${farm.name}' محمية بكلمة مرور. يرجى إدخالها للمتابعة:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (loginPassword == farm.password) {
                            val pass = loginPassword
                            showLoginDialog = null
                            onFarmSelected(farm.name, pass)
                        } else {
                            Toast.makeText(context, "كلمة المرور خاطئة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("دخول")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    showAddAdminDialog?.let { farm ->
        AlertDialog(
            onDismissRequest = { showAddAdminDialog = null },
            title = { Text("إضافة مسؤول إداري (Co-Manager)") },
            text = {
                Column {
                    Text("أدخل البريد الإلكتروني أو UID للمستخدم:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAdminEmailOrUid,
                        onValueChange = { newAdminEmailOrUid = it },
                        label = { Text("البريد الإلكتروني أو UID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAdminEmailOrUid.isNotBlank()) {
                            scope.launch {
                                val result = repo.addAdminToFarm(farm.id, newAdminEmailOrUid)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "تم إضافة المسؤول بنجاح", Toast.LENGTH_SHORT).show()
                                    showAddAdminDialog = null
                                    newAdminEmailOrUid = ""
                                } else {
                                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "حدث خطأ", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAdminDialog = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }
}
