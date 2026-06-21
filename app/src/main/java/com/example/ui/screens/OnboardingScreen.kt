package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.OnboardingViewModel
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    farmViewModel: FarmViewModel,
    themePrimary: Color,
    onRestoreBackupFromFile: () -> Unit,
    onRestoreBackupFromFolder: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var restoreMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(themePrimary.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = "FarmManager PRO",
                        tint = themePrimary,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "أهلاً بك في FarmManager PRO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "للبدء، يرجى إدخال تفاصيل المزرعة والمالك، أو المتابعة باستخدام الإعدادات الافتراضية.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Input card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.ownerName,
                            onValueChange = { viewModel.updateOwnerName(it) },
                            label = { Text("اسم المالك") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.farmName,
                            onValueChange = { viewModel.updateFarmName(it) },
                            label = { Text("اسم المزرعة") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Actions
                Button(
                    onClick = { 
                        viewModel.completeSetup()
                        farmViewModel.createFarm(uiState.farmName, "")
                    },
                    enabled = uiState.isInputValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("بدء العمل", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        TextButton(onClick = { restoreMenuExpanded = true }) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة نسخة احتياطية", color = themePrimary)
                        }
                        DropdownMenu(
                            expanded = restoreMenuExpanded,
                            onDismissRequest = { restoreMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("استيراد ملف نسخة (JSON)") },
                                onClick = { 
                                    restoreMenuExpanded = false
                                    onRestoreBackupFromFile() 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تحديد مجلد حفظ النسخ") },
                                onClick = { 
                                    restoreMenuExpanded = false
                                    onRestoreBackupFromFolder() 
                                }
                            )
                        }
                    }

                    TextButton(
                        onClick = { 
                            viewModel.skipSetup()
                            farmViewModel.createFarm("مزرعتي", "")
                        }
                    ) {
                        Text("تخطي", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
