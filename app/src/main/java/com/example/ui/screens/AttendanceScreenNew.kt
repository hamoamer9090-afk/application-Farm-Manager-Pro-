package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceEntity
import com.example.data.model.AttendanceType
import com.example.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreenNew(viewModel: FarmViewModel, onBack: () -> Unit) {
    val attendanceRecords by viewModel.attendanceList.collectAsStateWithLifecycle()
    val attTypes by viewModel.attendanceTypes.collectAsStateWithLifecycle()
    val pageColorHex by viewModel.attendancePageColorHex.collectAsStateWithLifecycle()
    val headerColorHex by viewModel.attendanceHeaderColorHex.collectAsStateWithLifecycle()
    val monthSelectorColorHex by viewModel.attendanceMonthSelectorColorHex.collectAsStateWithLifecycle()
    val bottomBarColorHex by viewModel.attendanceBottomBarColorHex.collectAsStateWithLifecycle()
    val dayBoxColorHex by viewModel.attendanceDayBoxColorHex.collectAsStateWithLifecycle()
    val dayBoxTextColorHex by viewModel.attendanceDayBoxTextColorHex.collectAsStateWithLifecycle()
    val cardColorHex by viewModel.attendanceCardColorHex.collectAsStateWithLifecycle()
    val cardTextColorHex by viewModel.attendanceCardTextColorHex.collectAsStateWithLifecycle()
    val fontShape by viewModel.attendanceFontShape.collectAsStateWithLifecycle()
    val isRtl by viewModel.attendanceIsRtl.collectAsStateWithLifecycle()
    val pageTitle by viewModel.attendancePageTitle.collectAsStateWithLifecycle()
    
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.surface == Color.Black // rough heuristic if we're in dark theme

    val defaultBg = MaterialTheme.colorScheme.background
    val defaultPrimary = MaterialTheme.colorScheme.primary
    val defaultSecondary = MaterialTheme.colorScheme.secondary
    val defaultSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val defaultOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val defaultSurface = MaterialTheme.colorScheme.surface
    val defaultOnSurface = MaterialTheme.colorScheme.onSurface

    val bgColor = try { if(pageColorHex == "#F8FAFC") defaultBg else Color(android.graphics.Color.parseColor(pageColorHex)) } catch(e: Exception) { defaultBg }
    val headerColor = try { if(headerColorHex == "#3B5BDB") defaultPrimary else Color(android.graphics.Color.parseColor(headerColorHex)) } catch(e: Exception) { defaultPrimary }
    val monthSelectorColor = try { if(monthSelectorColorHex == "#4C6EF5") defaultSecondary else Color(android.graphics.Color.parseColor(monthSelectorColorHex)) } catch(e: Exception) { defaultSecondary }
    val bottomBarColor = try { if(bottomBarColorHex == "#3B5BDB") defaultPrimary else Color(android.graphics.Color.parseColor(bottomBarColorHex)) } catch(e: Exception) { defaultPrimary }
    val dayBoxColor = try { if(dayBoxColorHex == "#F1F5F9") defaultSurfaceVariant else Color(android.graphics.Color.parseColor(dayBoxColorHex)) } catch(e: Exception) { defaultSurfaceVariant }
    val dayBoxTextColor = try { if(dayBoxTextColorHex == "#1E293B") defaultOnSurfaceVariant else Color(android.graphics.Color.parseColor(dayBoxTextColorHex)) } catch(e: Exception) { defaultOnSurfaceVariant }
    val cardColor = try { if(cardColorHex == "#FFFFFF") defaultSurface else Color(android.graphics.Color.parseColor(cardColorHex)) } catch(e: Exception) { defaultSurface }
    val cardTextColor = try { if(cardTextColorHex == "#1E293B") defaultOnSurface else Color(android.graphics.Color.parseColor(cardTextColorHex)) } catch(e: Exception) { defaultOnSurface }

    val dynamicCardShape = if (fontShape == "sharp") RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
    ) {
        var selectedMonthDate by remember { mutableStateOf(Date()) }
    val sdfMonthQuery = SimpleDateFormat("MM/yyyy", Locale.getDefault())
    val sdfMonthDisplay = SimpleDateFormat("MMMM yyyy", Locale("ar"))
    val sdfDateKey = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val sdfDayName = SimpleDateFormat("EEEE", Locale("ar"))

    val selectedMonthStr = sdfMonthQuery.format(selectedMonthDate)
    val displayMonthStr = sdfMonthDisplay.format(selectedMonthDate)

    val currentFarm by viewModel.currentFarm.collectAsStateWithLifecycle()
    
    LaunchedEffect(selectedMonthStr, currentFarm) {
        if (currentFarm != null) {
            viewModel.loadAttendanceForMonth(selectedMonthStr)
        }
    }

    val currentMonthRecords = remember(attendanceRecords, selectedMonthStr) {
        attendanceRecords.filter { it.dateString.contains(selectedMonthStr) }
    }

    // Prepare days 1 to maxDays
    val calendar = Calendar.getInstance().apply { time = selectedMonthDate }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    var showOptionsDialog by remember { mutableStateOf(false) }
    var selectedRecordForEdit by remember { mutableStateOf<Pair<String, AttendanceEntity?>?>(null) }
    var showSummaryDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
            // --- Top Header ---
        Surface(
            color = headerColor,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                        }
                        IconButton(
                            onClick = { showOptionsDialog = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "إعدادات", tint = Color.White)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(pageTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text("نظام المتابعة اليومي الذكي", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Month Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(monthSelectorColor, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply { time = selectedMonthDate; add(Calendar.MONTH, -1) }
                        selectedMonthDate = cal.time
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(displayMonthStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("تم تسجيل ${currentMonthRecords.size} من $daysInMonth يوم", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }

                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply { time = selectedMonthDate; add(Calendar.MONTH, 1) }
                        selectedMonthDate = cal.time
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // --- List of Days ---
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary item
            item {
                Surface(
                    color = cardColor,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ملخص الشهر الحالي", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = cardTextColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val stats = currentMonthRecords.groupingBy { it.dayType }.eachCount()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            stats.entries.take(4).forEach { (type, count) ->
                                val cHex = attTypes.find { it.label == type }?.colorHex ?: "#1E293B"
                                val c = try { Color(android.graphics.Color.parseColor(cHex)) } catch(e:Exception){ Color.Gray }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(24.dp).background(c, CircleShape), contentAlignment = Alignment.Center) {
                                        Text(count.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(type, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = cardTextColor.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            items(daysInMonth) { index ->
                val day = index + 1
                val cal = Calendar.getInstance().apply { 
                    time = selectedMonthDate
                    set(Calendar.DAY_OF_MONTH, day)
                }
                val dateStr = sdfDateKey.format(cal.time)
                val dayName = sdfDayName.format(cal.time)
                val record = currentMonthRecords.find { it.dateString == dateStr }

                Card(
                    shape = dynamicCardShape,
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth().clickable { selectedRecordForEdit = Pair(dateStr, record) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = cardTextColor.copy(alpha = 0.4f))
                        
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(dayName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = cardTextColor)
                            if (record != null) {
                                val cHex = attTypes.find { it.label == record.dayType }?.colorHex ?: "#1E293B"
                                val c = try { Color(android.graphics.Color.parseColor(cHex)) } catch(e:Exception){ Color.Gray }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(record.dayType, fontSize = 12.sp, color = cardTextColor.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(modifier = Modifier.size(8.dp).background(c, CircleShape))
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("غير مسجل", fontSize = 12.sp, color = cardTextColor.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(modifier = Modifier.size(8.dp).background(Color.LightGray, CircleShape))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier.size(40.dp).background(dayBoxColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = dayBoxTextColor)
                        }
                    }
                }
            }
        }
    } // Closes Column

    // Bottom Action Bar area via Box overlay
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = { /* Quick action, maybe today's checkin */
                    val todayStr = sdfDateKey.format(Date())
                    val existing = attendanceRecords.find { it.dateString == todayStr }
                    selectedRecordForEdit = Pair(todayStr, existing)
                },
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF1E293B)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = "سجل اليوم")
            }

            Button(
                onClick = { showSummaryDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = bottomBarColor),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PieChart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("عرض ملخص الأيام 📊", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    } 
    } // End of outer Box

    // --- Dialogs ---
    if (showOptionsDialog) {
        ManageAttendanceColorsDialog(
            viewModel = viewModel,
            onDismiss = { showOptionsDialog = false }
        )
    }

    if (selectedRecordForEdit != null) {
        val (dateStr, record) = selectedRecordForEdit!!
        SelectAttendanceStatusDialog(
            dateStr = dateStr,
            currentRecord = record,
            types = attTypes,
            onDismiss = { selectedRecordForEdit = null },
            onSave = { selectedType ->
                if (record != null) {
                    viewModel.updateAttendance(record.copy(dayType = selectedType.label))
                } else {
                    viewModel.recordAttendance(dateStr, selectedType.label, true, "")
                }
                selectedRecordForEdit = null
            },
            onDelete = {
                if (record != null) {
                    viewModel.deleteAttendance(record)
                }
                selectedRecordForEdit = null
            }
        )
    }

    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { showSummaryDialog = false },
            title = { Text("ملخص أيام $displayMonthStr", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                val stats = currentMonthRecords.groupingBy { it.dayType }.eachCount()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (stats.isEmpty()) {
                        Text("لا يوجد سجلات لحساب الملخص.", color = Color.Gray)
                    }
                    stats.forEach { (typeLabel, count) ->
                        val cHex = attTypes.find { it.label == typeLabel }?.colorHex ?: "#1E293B"
                        val c = try { Color(android.graphics.Color.parseColor(cHex)) } catch(e:Exception){ Color.Gray }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("$count يوم", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(typeLabel)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(12.dp).background(c, CircleShape))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSummaryDialog = false }) { Text("حسناً") }
            }
        )
    }
    } // End of RTL Provider
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAttendanceStatusDialog(
    dateStr: String,
    currentRecord: AttendanceEntity?,
    types: List<AttendanceType>,
    onDismiss: () -> Unit,
    onSave: (AttendanceType) -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("تسجيل حالة يوم $dateStr", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                types.forEach { type ->
                    val isSelected = currentRecord?.dayType == type.label
                    val c = try { Color(android.graphics.Color.parseColor(type.colorHex)) } catch(e:Exception){ Color.Gray }
                    Surface(
                        color = if (isSelected) c.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSelected) c else Color.LightGray),
                        modifier = Modifier.fillMaxWidth().clickable { onSave(type) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if(isSelected) c else Color.Black)
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.size(12.dp).background(c, CircleShape))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentRecord != null) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("مسح السجل")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAttendanceColorsDialog(
    viewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    val types by viewModel.attendanceTypes.collectAsStateWithLifecycle()
    val pageColorHex by viewModel.attendancePageColorHex.collectAsStateWithLifecycle()
    val headerColorHex by viewModel.attendanceHeaderColorHex.collectAsStateWithLifecycle()
    val isRtl by viewModel.attendanceIsRtl.collectAsStateWithLifecycle()
    val pageTitle by viewModel.attendancePageTitle.collectAsStateWithLifecycle()
    val cardColorHex by viewModel.attendanceCardColorHex.collectAsStateWithLifecycle()
    val cardTextColorHex by viewModel.attendanceCardTextColorHex.collectAsStateWithLifecycle()
    val dialogColorHex by viewModel.attendanceSettingsDialogColorHex.collectAsStateWithLifecycle()
    val dialogTextColorHex by viewModel.attendanceSettingsDialogTextColorHex.collectAsStateWithLifecycle()
    val fontFamily by viewModel.attendanceFontFamily.collectAsStateWithLifecycle()
    val fontShape by viewModel.attendanceFontShape.collectAsStateWithLifecycle()

    var newLabel by remember { mutableStateOf("") }
    val presetColors = listOf(
        "#ec4899", "#a855f7", "#ef4444", "#f97316", "#f59e0b", "#3b82f6", "#10b981",
        "#1e293b", "#818cf8", "#facc15", "#22d3ee", "#2dd4bf"
    )
    var selectedColor by remember { mutableStateOf(presetColors[6]) } // Default to green
    var colorTabSelected by remember { mutableIntStateOf(0) } // 0: جاهز, 1: تخصيص حر
    var customHex by remember { mutableStateOf(selectedColor) }
    
    val context = LocalContext.current
    val accentColor = try { Color(android.graphics.Color.parseColor(headerColorHex)) } catch(e: Exception) { Color(0xFF3B5BDB) }

    val actualDialogColor = try { Color(android.graphics.Color.parseColor(dialogColorHex)) } catch(e: Exception) { Color.White }
    val actualDialogTextColor = try { Color(android.graphics.Color.parseColor(dialogTextColorHex)) } catch(e: Exception) { Color(0xFF1E293B) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(28.dp)),
        content = {
            Surface(
                color = actualDialogColor,
                contentColor = actualDialogTextColor,
                shape = if (fontShape == "sharp") RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onDismiss,
                            color = Color(0xFFF1F5F9),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("إدارة خيارات الحضور والالوان", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Divider(color = Color(0xFFF1F5F9))

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Section: Add Custom Option
                        Surface(
                            color = actualDialogColor,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, actualDialogTextColor.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("إضافة خيار حضور مخصص الألوان", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 15.sp)
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                }
                                
                                Text(
                                    "اسم الحالة (مثال: نصف دوام، مأمورية)",
                                    fontSize = 12.sp,
                                    color = actualDialogTextColor.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    textAlign = TextAlign.End
                                )
                                
                                OutlinedTextField(
                                    value = newLabel,
                                    onValueChange = { newLabel = it },
                                    placeholder = { Text("اكتب اسم الخيار هنا...", color = actualDialogTextColor.copy(alpha = 0.4f), fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = actualDialogTextColor.copy(alpha = 0.2f),
                                        focusedBorderColor = accentColor,
                                        unfocusedTextColor = actualDialogTextColor,
                                        focusedTextColor = actualDialogTextColor
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text("اختر لوناً جاهزاً أو خصص لوناً حراً:", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                
                                // Tabs for color selection
                                TabRow(
                                    selectedTabIndex = colorTabSelected,
                                    containerColor = Color.Transparent,
                                    contentColor = accentColor,
                                    indicator = { tabPositions ->
                                        TabRowDefaults.Indicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[colorTabSelected]),
                                            color = accentColor,
                                            height = 2.dp
                                        )
                                    },
                                    divider = {},
                                    modifier = Modifier.padding(vertical = 12.dp).height(40.dp).clip(RoundedCornerShape(8.dp))
                                ) {
                                    Tab(
                                        selected = colorTabSelected == 1,
                                        onClick = { colorTabSelected = 1 },
                                        text = { Text("تخصيص حر", fontSize = 13.sp, fontWeight = if(colorTabSelected == 1) FontWeight.Bold else FontWeight.Normal, color = if(colorTabSelected == 1) accentColor else actualDialogTextColor.copy(alpha = 0.6f)) }
                                    )
                                    Tab(
                                        selected = colorTabSelected == 0,
                                        onClick = { colorTabSelected = 0 },
                                        text = { Text("جاهز", fontSize = 13.sp, fontWeight = if(colorTabSelected == 0) FontWeight.Bold else FontWeight.Normal, color = if(colorTabSelected == 0) accentColor else actualDialogTextColor.copy(alpha = 0.6f)) }
                                    )
                                }
                                
                                if (colorTabSelected == 0) {
                                    // Grid of preset colors
                                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(6),
                                        modifier = Modifier.height(80.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(presetColors) { colorHex ->
                                            val c = try { Color(android.graphics.Color.parseColor(colorHex)) } catch(e: Exception) { Color.Gray }
                                            val isSelected = selectedColor == colorHex
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(c, CircleShape)
                                                    .clickable { 
                                                        selectedColor = colorHex
                                                        customHex = colorHex
                                                    }
                                                    .then(if (isSelected) Modifier.border(2.dp, accentColor, CircleShape).padding(2.dp).border(2.dp, actualDialogColor, CircleShape) else Modifier)
                                            )
                                        }
                                    }
                                } else {
                                    // Custom hex input
                                    OutlinedTextField(
                                        value = customHex,
                                        onValueChange = { customHex = it },
                                        placeholder = { Text("#RRGGBB") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = actualDialogTextColor.copy(alpha = 0.2f),
                                            focusedBorderColor = accentColor,
                                            unfocusedTextColor = actualDialogTextColor,
                                            focusedTextColor = actualDialogTextColor
                                        ),
                                        trailingIcon = {
                                            val c = try { Color(android.graphics.Color.parseColor(customHex)) } catch(e: Exception) { null }
                                            if (c != null) {
                                                Box(modifier = Modifier.size(24.dp).background(c, CircleShape).border(1.dp, Color.Gray, CircleShape))
                                            }
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        if (newLabel.isNotBlank()) {
                                            val colorToUse = if(colorTabSelected == 0) selectedColor else customHex
                                            if (colorToUse.startsWith("#") && (colorToUse.length == 7 || colorToUse.length == 9)) {
                                                val updated = types.toMutableList()
                                                updated.add(AttendanceType(newLabel.trim(), colorToUse))
                                                viewModel.updateAttendanceTypes(updated)
                                                newLabel = ""
                                                Toast.makeText(context, "تمت الإضافة بنجاح ✅", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "كود اللون غير صالح", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "يرجى كتابة اسم الحالة", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("إضافة الحالة لقائمتي", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "الخيارات الحالية المتوفرة يومياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = TextAlign.End,
                            color = actualDialogTextColor.copy(alpha = 0.8f)
                        )
                        
                        // List of current types
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            types.forEachIndexed { index, type ->
                                val color = try { Color(android.graphics.Color.parseColor(type.colorHex)) } catch(e: Exception) { Color.Gray }
                                var editLabel by remember { mutableStateOf(type.label) }
                                
                                Surface(
                                    color = actualDialogColor,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, actualDialogTextColor.copy(alpha = 0.1f)),
                                    tonalElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(onClick = {
                                            if (types.size > 1) {
                                                val updated = types.toMutableList()
                                                updated.removeAt(index)
                                                viewModel.updateAttendanceTypes(updated)
                                            } else {
                                                Toast.makeText(context, "يجب الإبقاء على خيار واحد على الأقل", Toast.LENGTH_SHORT).show()
                                            }
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray.copy(alpha = 0.6f))
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically, 
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            BasicTextField(
                                                value = editLabel,
                                                onValueChange = { newVal ->
                                                    editLabel = newVal
                                                    val updated = types.toMutableList()
                                                    updated[index] = type.copy(label = newVal)
                                                    viewModel.updateAttendanceTypes(updated)
                                                },
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    textAlign = TextAlign.End,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = actualDialogTextColor
                                                ),
                                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                            )
                                            // Clickable color dot for existing options
                                            var showColorPickerForType by remember { mutableStateOf(false) }
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(color, CircleShape)
                                                    .clickable { showColorPickerForType = true }
                                            )
                                            
                                            if (showColorPickerForType) {
                                                AlertDialog(
                                                    onDismissRequest = { showColorPickerForType = false },
                                                    title = { Text("اختر لوناً لـ ${type.label}", fontSize = 14.sp) },
                                                    text = {
                                                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(5),
                                                            modifier = Modifier.height(120.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            items(presetColors) { colorHex ->
                                                                val c = try { Color(android.graphics.Color.parseColor(colorHex)) } catch(e: Exception) { Color.Gray }
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(32.dp)
                                                                        .background(c, CircleShape)
                                                                        .clickable { 
                                                                            val updated = types.toMutableList()
                                                                            updated[index] = type.copy(colorHex = colorHex)
                                                                            viewModel.updateAttendanceTypes(updated)
                                                                            showColorPickerForType = false
                                                                        }
                                                                )
                                                            }
                                                        }
                                                    },
                                                    confirmButton = {}
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Page Title & Style
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "تخصيص عنوان الصفحة والألوان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = TextAlign.End,
                            color = actualDialogTextColor.copy(alpha = 0.8f)
                        )
                        
                        Surface(
                            color = actualDialogColor,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, actualDialogTextColor.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                var tempTitle by remember { mutableStateOf(pageTitle) }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    Text("عنوان الصفحة الحالية", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Title, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                }
                                OutlinedTextField(
                                    value = tempTitle,
                                    onValueChange = { 
                                        tempTitle = it
                                        viewModel.updateAttendancePageTitle(it)
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = actualDialogTextColor.copy(alpha = 0.2f),
                                        focusedBorderColor = accentColor,
                                        unfocusedTextColor = actualDialogTextColor,
                                        focusedTextColor = actualDialogTextColor
                                    ),
                                    trailingIcon = {
                                        if (tempTitle.isNotEmpty()) {
                                            IconButton(onClick = { 
                                                tempTitle = ""
                                                viewModel.updateAttendancePageTitle("")
                                            }) {
                                                Icon(Icons.Default.Cancel, contentDescription = "مسح والعاء التخصيص", tint = Color.Gray)
                                            }
                                        }
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text("اللون الرئيسي (الهيدر والأزرار):", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
                                    items(presetColors) { colorHex ->
                                        val c = try { Color(android.graphics.Color.parseColor(colorHex)) } catch(e: Exception) { Color.Gray }
                                        val isSelected = headerColorHex.equals(colorHex, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(c, CircleShape)
                                                .clickable { 
                                                    viewModel.updateAttendanceHeaderColor(colorHex)
                                                    viewModel.updateAttendanceBottomBarColor(colorHex)
                                                    viewModel.updateAttendanceMonthSelectorColor(colorHex)
                                                }
                                                .then(if (isSelected) Modifier.border(2.dp, actualDialogTextColor, CircleShape) else Modifier)
                                        )
                                    }
                                }

                                Text("لون صفحة مسجل الحضور الرئيسية:", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                var bgInput by remember { mutableStateOf(pageColorHex) }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    IconButton(onClick = {
                                        if (bgInput.startsWith("#") && (bgInput.length == 7 || bgInput.length == 9)) {
                                            viewModel.updateAttendancePageColor(bgInput)
                                        }
                                    }) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "تطبيق", tint = accentColor)
                                    }
                                    OutlinedTextField(
                                        value = bgInput,
                                        onValueChange = { bgInput = it },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = actualDialogTextColor.copy(alpha = 0.2f),
                                            focusedBorderColor = accentColor,
                                            unfocusedTextColor = actualDialogTextColor,
                                            focusedTextColor = actualDialogTextColor
                                        ),
                                        placeholder = { Text("#F8FAFC") },
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("لون خلفية البطاقات (سجلات الحضور):", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                var cardBgInput by remember { mutableStateOf(cardColorHex) }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    IconButton(onClick = {
                                        if (cardBgInput.startsWith("#") && (cardBgInput.length == 7 || cardBgInput.length == 9)) {
                                            viewModel.updateAttendanceCardColor(cardBgInput)
                                        }
                                    }) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "تطبيق", tint = accentColor)
                                    }
                                    OutlinedTextField(
                                        value = cardBgInput,
                                        onValueChange = { cardBgInput = it },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = actualDialogTextColor.copy(alpha = 0.2f),
                                            focusedBorderColor = accentColor,
                                            unfocusedTextColor = actualDialogTextColor,
                                            focusedTextColor = actualDialogTextColor
                                        ),
                                        placeholder = { Text("#FFFFFF") },
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("لون خلفية إعدادات الحضور (هذه النافذة):", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                var dialogBgInput by remember { mutableStateOf(dialogColorHex) }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    IconButton(onClick = {
                                        if (dialogBgInput.startsWith("#") && (dialogBgInput.length == 7 || dialogBgInput.length == 9)) {
                                            viewModel.updateAttendanceSettingsDialogColor(dialogBgInput)
                                        }
                                    }) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "تطبيق", tint = accentColor)
                                    }
                                    OutlinedTextField(
                                        value = dialogBgInput,
                                        onValueChange = { dialogBgInput = it },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = actualDialogTextColor.copy(alpha = 0.2f),
                                            focusedBorderColor = accentColor,
                                            unfocusedTextColor = actualDialogTextColor,
                                            focusedTextColor = actualDialogTextColor
                                        ),
                                        placeholder = { Text("#FFFFFF") },
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("لون نصوص إعدادات الحضور (عكس لون الخلفية):", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                var dialogTxtInput by remember { mutableStateOf(dialogTextColorHex) }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    IconButton(onClick = {
                                        if (dialogTxtInput.startsWith("#") && (dialogTxtInput.length == 7 || dialogTxtInput.length == 9)) {
                                            viewModel.updateAttendanceSettingsDialogTextColor(dialogTxtInput)
                                        }
                                    }) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "تطبيق", tint = accentColor)
                                    }
                                    OutlinedTextField(
                                        value = dialogTxtInput,
                                        onValueChange = { dialogTxtInput = it },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = actualDialogTextColor.copy(alpha = 0.2f),
                                            focusedBorderColor = accentColor,
                                            unfocusedTextColor = actualDialogTextColor,
                                            focusedTextColor = actualDialogTextColor
                                        ),
                                        placeholder = { Text("#1E293B") },
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("نوع الخط:", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(onClick = { viewModel.updateAttendanceFontFamily("default") }, colors = ButtonDefaults.buttonColors(containerColor = if (fontFamily == "default") accentColor else Color.LightGray), modifier = Modifier.weight(1f)) { Text("افتراضي") }
                                    Button(onClick = { viewModel.updateAttendanceFontFamily("cairo") }, colors = ButtonDefaults.buttonColors(containerColor = if (fontFamily == "cairo") accentColor else Color.LightGray), modifier = Modifier.weight(1f)) { Text("كايرو") }
                                    Button(onClick = { viewModel.updateAttendanceFontFamily("rubik") }, colors = ButtonDefaults.buttonColors(containerColor = if (fontFamily == "rubik") accentColor else Color.LightGray), modifier = Modifier.weight(1f)) { Text("روبيك") }
                                }
                        
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("شكل الخط وحواف الواجهة:", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(onClick = { viewModel.updateAttendanceFontShape("normal") }, colors = ButtonDefaults.buttonColors(containerColor = if (fontShape == "normal") accentColor else Color.LightGray), modifier = Modifier.weight(1f)) { Text("دائرية ومقوسة") }
                                    Button(onClick = { viewModel.updateAttendanceFontShape("sharp") }, colors = ButtonDefaults.buttonColors(containerColor = if (fontShape == "sharp") accentColor else Color.LightGray), modifier = Modifier.weight(1f)) { Text("حادة ورسمية") }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            Text("اتجاه الواجهة (RTL)", fontSize = 13.sp, color = actualDialogTextColor)
                            Switch(
                                checked = isRtl, 
                                onCheckedChange = { viewModel.updateAttendanceLayoutDirection(it) }, 
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha=0.5f))
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    )
}


@Composable
fun ColorSection(label: String, selectedHex: String, onUpdate: (String) -> Unit) {
    val presetColors = listOf("#F8FAFC", "#3B5BDB", "#4C6EF5", "#059669", "#DC2626", "#FBBF24", "#8B5CF6", "#EC4899", "#1E293B", "#64748B")
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.height(75.dp)
        ) {
            items(presetColors.size) { i ->
                val hex = presetColors[i]
                val c = try { Color(android.graphics.Color.parseColor(hex)) } catch(e:Exception){ Color.Gray }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(c, CircleShape)
                        .border(
                            width = if (selectedHex == hex) 2.dp else 1.dp,
                            color = if (selectedHex == hex) Color(0xFF3B5BDB) else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { onUpdate(hex) }
                )
            }
        }
    }
}
