package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BirthEntity
import com.example.ui.viewmodel.FarmViewModel

import com.example.AddNewbornDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewbornsScreen(
    viewModel: FarmViewModel,
    themePrimary: Color,
    appCardBgColor: Color,
    onBack: () -> Unit
) {
    val births by viewModel.birthsList.collectAsStateWithLifecycle()
    var showLocalNewbornDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("قائمة المواليد بالحضيرة 🍼", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = themePrimary, modifier = Modifier.weight(1f))
            Button(
                onClick = { showLocalNewbornDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("إضافة مولود 🍼", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider()

        if (births.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد مواليد حالياً في الحظيرة", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(births) { birth ->
                    Surface(
                        color = appCardBgColor,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themePrimary.copy(alpha=0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(themePrimary.copy(alpha=0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍼", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val mStr = if (birth.motherId > 0) "أم ${birth.motherId}" else ""
                                val fStr = if (birth.fatherId > 0) "أب ${birth.fatherId}" else ""
                                val parentStr = listOf(mStr, fStr).filter { it.isNotEmpty() }.joinToString(" و ")
                                val titleStr = if (parentStr.isNotEmpty()) "مولود من $parentStr" else "مولود جديد"
                                Text(titleStr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${birth.gender} - ${birth.birthType} - تولد: ${birth.birthDate}", fontSize = 11.sp, color = Color.Gray)
                                
                                val ageStr = remember(birth.birthDate) {
                                    try {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val bDate = sdf.parse(birth.birthDate)
                                        val diffDays = ((System.currentTimeMillis() - bDate!!.time) / (1000 * 60 * 60 * 24)).toInt()
                                        if(diffDays < 30) "$diffDays يوم" else "${diffDays/30} شهر و ${diffDays%30} يوم"
                                    } catch(e: Exception) { "غير محدد" }
                                }
                                Text("العمر الحالي: $ageStr | الحالة: ${birth.status}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if(birth.status == "تم بيعه") Color.Red else themePrimary)
                            }
                            // Button for Selling could be added if needed, but per request it's just viewing the list
                        }
                    }
                }
            }
        }
    }

    if (showLocalNewbornDialog) {
        AddNewbornDialog(
            viewModel = viewModel,
            accentColor = themePrimary,
            onDismiss = { showLocalNewbornDialog = false }
        )
    }
}
