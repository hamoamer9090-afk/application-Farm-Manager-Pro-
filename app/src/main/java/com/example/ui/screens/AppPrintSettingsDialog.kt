package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.FarmViewModel
import com.example.util.ImageUtils
import android.graphics.BitmapFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrintSettingsDialog(
    viewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val customAppName by viewModel.customAppName.collectAsStateWithLifecycle()
    val customAppIconBase64 by viewModel.customAppIconBase64.collectAsStateWithLifecycle()
    val reportColorHex by viewModel.reportColorHex.collectAsStateWithLifecycle()
    val reportShapeStyle by viewModel.reportShapeStyle.collectAsStateWithLifecycle()

    var editAppName by remember { mutableStateOf(customAppName) }
    var editColorHex by remember { mutableStateOf(reportColorHex) }
    var editShapeStyle by remember { mutableStateOf(reportShapeStyle) }
    var editIconBase64 by remember { mutableStateOf(customAppIconBase64) }

    val presetColors = listOf(
        "#059669", "#DC2626", "#2563EB", "#F59E0B", 
        "#B45309", "#0D9488", "#7C3AED", "#DB2777", 
        "#4B5563", "#000000"
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri)
            editIconBase64 = base64
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إعدادات الهوية والطباعة", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. App Name
                    Column {
                        Text("اسم التطبيق", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("يظهر هذا الاسم في التقرير أعلى اليمين كعنوان للنشاط", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editAppName,
                            onValueChange = { editAppName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    HorizontalDivider()

                    // 2. Report Color
                    Column {
                        Text("اللون الرئيسي للتقرير", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("يتحكم في ألوان الخطوط الرئيسية والجداول والتحديدات", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.height(100.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(presetColors) { colorHex ->
                                val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (editColorHex == colorHex) 3.dp else 0.dp,
                                            color = if (editColorHex == colorHex) Color.Black else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { editColorHex = colorHex }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // 3. Report Shape Style
                    Column {
                        Text("شكل وحواف الجداول والبطاقات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { editShapeStyle = "rounded" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editShapeStyle == "rounded") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (editShapeStyle == "rounded") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.ShapeLine, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("دائري (Rounded)")
                            }

                            Button(
                                onClick = { editShapeStyle = "sharp" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editShapeStyle == "sharp") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (editShapeStyle == "sharp") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Icon(Icons.Default.ShapeLine, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("حادة (Sharp)")
                            }
                        }
                    }

                    HorizontalDivider()

                    // 4. App Icon (Logo)
                    Column {
                        Text("صورة وشعار التطبيق (اختياري)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("لتخصيص شكل التقرير وصورته", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (editIconBase64 != null) {
                                val bitmap = try {
                                    val bytes = android.util.Base64.decode(editIconBase64, android.util.Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: Exception) { null }
                                
                                if (bitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.size(64.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { galleryLauncher.launch("image/*") }) {
                                    Text("اختيار صورة")
                                }
                                if (editIconBase64 != null) {
                                    TextButton(onClick = { editIconBase64 = null }) {
                                        Text("إزالة الصورة", color = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Live Preview Area
                    Card(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = if (editShapeStyle == "rounded") RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp)
                    ) {
                        val mainColor = try { Color(android.graphics.Color.parseColor(editColorHex)) } catch(e: Exception) { Color.Gray }
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(editAppName.ifBlank { "اسم التطبيق" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = mainColor)
                                    Text("معاينة التقرير...", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (editIconBase64 != null) {
                                    val bitmap = try {
                                        val bytes = android.util.Base64.decode(editIconBase64, android.util.Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (e: Exception) { null }
                                    if (bitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = mainColor, thickness = 2.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Mock table
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), shape = if (editShapeStyle == "rounded") RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp) else RoundedCornerShape(0.dp)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("البيان", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("القيمة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المصاريف", fontSize = 12.sp)
                                Text("١٠٠٠", fontSize = 12.sp, color = mainColor)
                            }
                        }
                    }
                }

                // Save button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = {
                            viewModel.updateCustomAppName(editAppName)
                            if (editAppName.isNotBlank()) {
                                viewModel.updateCustomAppIconBase64(editIconBase64)
                                viewModel.updateReportStyle(editColorHex, editShapeStyle)
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("حفظ التعديلات والتطبيق", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
