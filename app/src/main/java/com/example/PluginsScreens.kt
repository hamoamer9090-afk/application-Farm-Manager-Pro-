package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityLogEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.PersonalAccountEntity
import com.example.data.model.PersonalTransactionEntity
import com.example.data.model.InvoiceData
import com.example.ui.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(viewModel: FarmViewModel, accentColor: Color) {
    val logs by viewModel.activityLogsList.collectAsStateWithLifecycle()
    val zoom by viewModel.zoomLevel.collectAsStateWithLifecycle()
    var selectedLogForDetails by remember { mutableStateOf<ActivityLogEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ManageHistory, contentDescription = null, tint = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("سجل التغيرات", fontSize = (18f * (zoom / 16f)).sp, fontWeight = FontWeight.Bold)
                    Text("مراقبة جميع التعديلات والحركات على بيانات المزرعة", fontSize = (12f * (zoom / 16f)).sp, color = Color.Gray)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                LogItem(log, accentColor, zoom) { selectedLogForDetails = log }
            }
            if (logs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("لا يوجد سجلات حتى الآن", color = Color.Gray)
                    }
                }
            }
        }
    }
    
    if (selectedLogForDetails != null) {
        val log = selectedLogForDetails!!
        AlertDialog(
            onDismissRequest = { selectedLogForDetails = null },
            title = { Text("تفاصيل سجل التغيير ℹ️") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(color = accentColor.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("نوع الإجراء: ${log.actionType}", fontWeight = FontWeight.ExtraBold, color = accentColor)
                            Text("القسم / الجدول: ${log.entityType}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Text("التوقيت: ${log.dateString}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    Text("الوصف التفصيلي:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Text(log.description, fontSize = 15.sp, lineHeight = 20.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedLogForDetails = null }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                    Text("فهمت", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun LogItem(log: ActivityLogEntity, accentColor: Color, zoom: Float, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (log.actionType) {
                        "إضافة" -> Icons.Default.AddCircle
                        "تعديل" -> Icons.Default.Edit
                        "حذف" -> Icons.Default.Delete
                        else -> Icons.Default.Info
                    }
                    val iconColor = when (log.actionType) {
                        "إضافة" -> Color(0xFF059669)
                        "تعديل" -> Color(0xFF2563EB)
                        "حذف" -> Color(0xFFDC2626)
                        else -> accentColor
                    }
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(log.actionType, fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp, color = iconColor)
                }
                Text(log.dateString, fontSize = (11f * (zoom / 16f)).sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(log.description, fontSize = (14f * (zoom / 16f)).sp, fontWeight = FontWeight.Medium)
            Text(log.entityType, fontSize = (11f * (zoom / 16f)).sp, color = accentColor.copy(alpha = 0.7f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(viewModel: FarmViewModel, accentColor: Color) {
    val attendanceRecords by viewModel.attendanceList.collectAsStateWithLifecycle()
    val attTypes by viewModel.attendanceTypes.collectAsStateWithLifecycle()
    val zoom by viewModel.zoomLevel.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<AttendanceEntity?>(null) }
    var selectedMonth by remember { mutableStateOf(SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())) }

    val filteredRecords = remember(attendanceRecords, selectedMonth) {
        attendanceRecords.filter { it.dateString.contains(selectedMonth) }
    }

    val stats = remember(filteredRecords) {
        filteredRecords.groupingBy { it.dayType }.eachCount()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("جدول الحضور اليومي", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { 
                    val items = filteredRecords.map { "${it.dateString} (${if(it.isMorningShift) "صباح" else "مساء"})" to it.dayType }
                    viewModel.setCurrentInvoice(
                        InvoiceData(
                            title = "تقرير حضور شهر $selectedMonth",
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            items = items,
                            total = stats.entries.joinToString(" | ") { "${it.key}: ${it.value}" }
                        )
                    )
                }) {
                    Icon(Icons.Default.Print, contentDescription = "طباعة التقرير")
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        )

        // Month Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val cal = Calendar.getInstance().apply {
                    val date = SimpleDateFormat("MM/yyyy", Locale.getDefault()).parse(selectedMonth) ?: Date()
                    time = date
                    add(Calendar.MONTH, -1)
                }
                selectedMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.time)
                viewModel.loadAttendanceForMonth(selectedMonth)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("شهر التقرير", fontSize = 10.sp, color = Color.Gray)
                Text(selectedMonth, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            IconButton(onClick = {
                val cal = Calendar.getInstance().apply {
                    val date = SimpleDateFormat("MM/yyyy", Locale.getDefault()).parse(selectedMonth) ?: Date()
                    time = date
                    add(Calendar.MONTH, 1)
                }
                selectedMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.time)
                viewModel.loadAttendanceForMonth(selectedMonth)
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }

        // Summary Header
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attTypes) { type ->
                val count = stats[type.label] ?: 0
                val color = try {
                    Color(android.graphics.Color.parseColor(type.colorHex))
                } catch (e: Exception) {
                    accentColor
                }
                AttendanceStatCard(
                    label = type.label,
                    value = count.toString(),
                    color = color,
                    modifier = Modifier.width(100.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredRecords) { record ->
                AttendanceItem(
                    record = record,
                    attTypes = attTypes,
                    accentColor = accentColor,
                    zoom = zoom,
                    onEdit = { editingRecord = record },
                    onDelete = { viewModel.deleteAttendance(record) }
                )
            }
            if (filteredRecords.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("لا يوجد سجلات حضور لهذا الشهر", color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAttendanceDialog(
            attTypes = attTypes,
            onDismiss = { showAddDialog = false },
            onConfirm = { date, type, morning, note ->
                viewModel.recordAttendance(date, type, morning, note)
                showAddDialog = false
            },
            accentColor = accentColor
        )
    }

    if (editingRecord != null) {
        AddAttendanceDialog(
            record = editingRecord,
            attTypes = attTypes,
            onDismiss = { editingRecord = null },
            onConfirm = { date, type, morning, note ->
                viewModel.updateAttendance(editingRecord!!.copy(dateString = date, dayType = type, isMorningShift = morning, note = note))
                editingRecord = null
            },
            accentColor = accentColor
        )
    }
}

@Composable
fun AttendanceStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AttendanceStatStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    AttendanceStatCard(label, value, color, modifier)
}

@Composable
fun AttendanceItem(record: AttendanceEntity, attTypes: List<com.example.data.model.AttendanceType>, accentColor: Color, zoom: Float, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.dateString, fontWeight = FontWeight.Bold, fontSize = (15f * (zoom / 16f)).sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val color = attTypes.find { it.label == record.dayType }?.let { 
                        try { Color(android.graphics.Color.parseColor(it.colorHex)) } catch(e: Exception) { Color.Gray }
                    } ?: Color.Gray
                    
                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(record.dayType, fontSize = (12f * (zoom / 16f)).sp, color = color)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (record.isMorningShift) "وردية صباحية" else "وردية مسائية", fontSize = (11f * (zoom / 16f)).sp, color = Color.Gray)
                }
                if (record.note.isNotBlank()) {
                    Text(record.note, fontSize = (11f * (zoom / 16f)).sp, color = Color.Gray)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = accentColor.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAttendanceDialog(
    record: AttendanceEntity? = null,
    attTypes: List<com.example.data.model.AttendanceType>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, String) -> Unit,
    accentColor: Color
) {
    var date by remember { mutableStateOf(record?.dateString ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var dayType by remember { mutableStateOf(record?.dayType ?: (attTypes.firstOrNull()?.label ?: "حضور")) } 
    var isMorning by remember { mutableStateOf(record?.isMorningShift ?: true) }
    var note by remember { mutableStateOf(record?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (record == null) "تسجيل حضور موظف" else "تعديل سجل حضور") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("التاريخ") }, modifier = Modifier.fillMaxWidth())
                
                Text("حالة اليوم:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    attTypes.forEach { type ->
                        FilterChip(
                            selected = dayType == type.label,
                            onClick = { dayType = type.label },
                            label = { Text(type.label) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMorning, onCheckedChange = { isMorning = it })
                    Text("وردية صباحية")
                }

                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(date, dayType, isMorning, note) }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text("حفظ", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalAccountsScreen(viewModel: FarmViewModel, accentColor: Color, zoomLevel: Float, onBack: () -> Unit) {
    val accounts by viewModel.personalAccountsList.collectAsStateWithLifecycle()
    val transactions by viewModel.personalTransactionsList.collectAsStateWithLifecycle()
    
    val paTitle by viewModel.paPageTitle.collectAsStateWithLifecycle()
    val paPageColor by viewModel.paPageColorHex.collectAsStateWithLifecycle()
    val paTextColor by viewModel.paTextColorHex.collectAsStateWithLifecycle()
    val paFontType by viewModel.paFontType.collectAsStateWithLifecycle()
    
    val bgColor = try { Color(android.graphics.Color.parseColor(paPageColor)) } catch(e: Exception) { Color(0xFFF8FAFC) }
    val txtColor = try { Color(android.graphics.Color.parseColor(paTextColor)) } catch(e: Exception) { Color(0xFF1E293B) }
    val fontFamily = when(paFontType) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
    
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<PersonalAccountEntity?>(null) }
    var selectedAccount by remember { mutableStateOf<PersonalAccountEntity?>(null) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<PersonalTransactionEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        TopAppBar(
            title = { Text(paTitle, fontWeight = FontWeight.Bold, color = txtColor, fontFamily = fontFamily) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = txtColor)
                }
            },
            actions = {
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "إعدادات", tint = txtColor)
                }
                IconButton(onClick = { showAddAccountDialog = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = txtColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (selectedAccount == null) {
            // Accounts List
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { account ->
                    AccountCard(
                        account = account,
                        accentColor = accentColor,
                        zoom = zoomLevel,
                        onClick = { selectedAccount = account },
                        onEdit = { editingAccount = account }
                    )
                }
                if (accounts.isEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Text("لا يوجد حسابات حالياً", color = Color.Gray)
                        }
                    }
                }
            }
        } else {
            // Account Details
            val currentAccount = accounts.find { it.id == selectedAccount!!.id } ?: selectedAccount!!
            AccountDetailView(
                account = currentAccount,
                transactions = transactions.filter { it.accountId == currentAccount.id },
                accentColor = accentColor,
                zoom = zoomLevel,
                viewModel = viewModel,
                onBack = { selectedAccount = null },
                onAddTransaction = { showTransactionDialog = true },
                onDelete = {
                    viewModel.deletePersonalAccount(currentAccount)
                    selectedAccount = null
                },
                onEditTransaction = { editingTransaction = it },
                onDeleteTransaction = { viewModel.deletePersonalTransaction(it) }
            )
        }
    }

    if (showAddAccountDialog) {
        AddAccountDialog(onDismiss = { showAddAccountDialog = false }, onConfirm = { name, phone, initial, note ->
            viewModel.addPersonalAccount(name, phone, initial, note)
            showAddAccountDialog = false
        }, accentColor = accentColor)
    }

    if (editingAccount != null) {
        AddAccountDialog(
            account = editingAccount,
            onDismiss = { editingAccount = null },
            onConfirm = { name, phone, initial, note ->
                viewModel.updatePersonalAccount(editingAccount!!.copy(name = name, phone = phone, initialBalance = initial, note = note))
                editingAccount = null
            },
            accentColor = accentColor
        )
    }

    if (showTransactionDialog && selectedAccount != null) {
        PersonalTransactionDialog(
            account = selectedAccount!!,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { type, amount, desc, date, note ->
                // Basic balance logic: Credit (له) adds to balance, Debit (عليه) subtracts.
                viewModel.addPersonalTransaction(selectedAccount!!.id, selectedAccount!!.name, type, amount, desc, date, note)
                showTransactionDialog = false
            },
            accentColor = accentColor
        )
    }

    if (editingTransaction != null && selectedAccount != null) {
        PersonalTransactionDialog(
            account = selectedAccount!!,
            transaction = editingTransaction,
            onDismiss = { editingTransaction = null },
            onConfirm = { type, amount, desc, date, note ->
                viewModel.updatePersonalTransaction(
                    editingTransaction!!.copy(type = type, amount = amount, description = desc, dateString = date, note = note),
                    editingTransaction!!.amount,
                    editingTransaction!!.type
                )
                editingTransaction = null
            },
            accentColor = accentColor
        )
    }
    
    if (showSettingsDialog) {
        AccountSettingsDialog(
            viewModel = viewModel,
            currentTitle = paTitle,
            currentBg = paPageColor,
            currentTxt = paTextColor,
            currentFont = paFontType,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun AccountSettingsDialog(
    viewModel: FarmViewModel,
    currentTitle: String,
    currentBg: String,
    currentTxt: String,
    currentFont: String,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }
    var bg by remember { mutableStateOf(currentBg) }
    var txt by remember { mutableStateOf(currentTxt) }
    var font by remember { mutableStateOf(currentFont) }
    
    val presetColors = listOf("#F8FAFC", "#F1F5F9", "#E2E8F0", "#FFFBEB", "#FEF2F2", "#FFFFFF", "#1E293B", "#334155")
    val presetTextColors = listOf("#1E293B", "#0F172A", "#334155", "#000000", "#FFFFFF", "#64748B")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعدادات الحسابات الشخصية", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; viewModel.updatePaPageTitle(it) },
                    label = { Text("عنوان الصفحة") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("لون الخلفية", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetColors) { cHex ->
                        val color = try { Color(android.graphics.Color.parseColor(cHex)) } catch(e:Exception){ Color.Gray }
                        Box(
                            modifier = Modifier.size(36.dp).background(color, CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { bg = cHex; viewModel.updatePaPageColor(cHex) }
                        ) {
                            if (bg.equals(cHex, true)) Icon(Icons.Default.Check, contentDescription = null, tint = if(color == Color.White) Color.Black else Color.White, modifier = Modifier.align(Alignment.Center).size(20.dp))
                        }
                    }
                }
                OutlinedTextField(
                    value = bg,
                    onValueChange = { bg = it; if(it.startsWith("#") && it.length>=7) viewModel.updatePaPageColor(it) },
                    label = { Text("لون مخصص (Hex) للخلفية") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("لون الخط", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetTextColors) { cHex ->
                        val color = try { Color(android.graphics.Color.parseColor(cHex)) } catch(e:Exception){ Color.Gray }
                        Box(
                            modifier = Modifier.size(36.dp).background(color, CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { txt = cHex; viewModel.updatePaTextColor(cHex) }
                        ) {
                            if (txt.equals(cHex, true)) Icon(Icons.Default.Check, contentDescription = null, tint = if(color == Color.White) Color.Black else Color.White, modifier = Modifier.align(Alignment.Center).size(20.dp))
                        }
                    }
                }
                OutlinedTextField(
                    value = txt,
                    onValueChange = { txt = it; if(it.startsWith("#") && it.length>=7) viewModel.updatePaTextColor(it) },
                    label = { Text("لون مخصص (Hex) للخط") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("نوع الخط", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = font == "default", onClick = { font = "default"; viewModel.updatePaFontType("default") }, label = { Text("الافتراضي") })
                    FilterChip(selected = font == "serif", onClick = { font = "serif"; viewModel.updatePaFontType("serif") }, label = { Text("Serif") })
                    FilterChip(selected = font == "monospace", onClick = { font = "monospace"; viewModel.updatePaFontType("monospace") }, label = { Text("Monospace") })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

@Composable
fun AccountCard(account: PersonalAccountEntity, accentColor: Color, zoom: Float, onClick: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text(account.name, fontWeight = FontWeight.Bold, fontSize = (16f * (zoom / 16f)).sp)
                    if (account.note.isNotBlank()) {
                        Text(
                            text = " (${account.note})",
                            fontSize = (12f * (zoom / 16f)).sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(account.createdAt))
                    Text(
                        text = " [$dateFormatted]",
                        fontSize = (10f * (zoom / 16f)).sp,
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(account.phone, fontSize = (12f * (zoom / 16f)).sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    val color = if (account.balance >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                    Text("${account.balance}", fontWeight = FontWeight.Bold, fontSize = (18f * (zoom / 16f)).sp, color = color)
                    Text(if (account.balance >= 0) "دائن (له)" else "مدين (عليه)", fontSize = (10f * (zoom / 16f)).sp, color = color)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = accentColor.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailView(
    account: PersonalAccountEntity,
    transactions: List<PersonalTransactionEntity>,
    accentColor: Color,
    zoom: Float,
    viewModel: FarmViewModel,
    onBack: () -> Unit,
    onAddTransaction: () -> Unit,
    onDelete: () -> Unit,
    onEditTransaction: (PersonalTransactionEntity) -> Unit,
    onDeleteTransaction: (PersonalTransactionEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            Text(account.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                val itemsSummary = transactions.map { it.description to "${if(it.type=="credit") "+" else "-"}${it.amount}" }
                viewModel.setCurrentInvoice(
                    InvoiceData(
                        title = "كشف حساب: ${account.name}",
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        items = itemsSummary,
                        total = "الرصيد: ${account.balance}"
                    )
                )
            }) {
                Icon(Icons.Default.ReceiptLong, contentDescription = "كشف حساب")
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
        }
        
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الرصيد النهائي", fontSize = 14.sp)
                Text("${account.balance}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (account.balance >= 0) Color(0xFF059669) else Color(0xFFDC2626))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("سجل الحركات", fontWeight = FontWeight.Bold)
            Button(onClick = onAddTransaction, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text("إضافة حركة ➕", color = Color.White)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions) { tx ->
                PersonalTxItem(
                    tx = tx,
                    zoom = zoom,
                    accentColor = accentColor,
                    onEdit = { onEditTransaction(tx) },
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }
    }
}

@Composable
fun PersonalTxItem(tx: PersonalTransactionEntity, zoom: Float, accentColor: Color, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.description, fontWeight = FontWeight.Medium)
                if (tx.note.isNotBlank()) {
                    Text(tx.note, fontSize = 11.sp, color = Color.Gray)
                }
                Text(tx.dateString, fontSize = 11.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = if (tx.type == "credit") Color(0xFF059669) else Color(0xFFDC2626)
                Text("${if(tx.type=="credit") "+" else "-"}${tx.amount}", fontWeight = FontWeight.Bold, color = color)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = accentColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    account: PersonalAccountEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit,
    accentColor: Color
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var phone by remember { mutableStateOf(account?.phone ?: "") }
    var initial by remember { mutableStateOf(account?.initialBalance?.toString() ?: "0") }
    var note by remember { mutableStateOf(account?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "إضافة حساب شخصي جديد" else "تعديل بيانات الحساب") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم صاحب الحساب") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الجوال") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = initial, onValueChange = { initial = it }, label = { Text("الرصيد الافتتاحي") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, phone, initial.toDoubleOrNull() ?: 0.0, note) }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text(if (account == null) "إضافة" else "حفظ", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalTransactionDialog(
    account: PersonalAccountEntity,
    transaction: PersonalTransactionEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, String) -> Unit,
    accentColor: Color
) {
    var type by remember { mutableStateOf(transaction?.type ?: "credit") } // "credit" له , "debit" عليه
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var desc by remember { mutableStateOf(transaction?.description ?: "") }
    var note by remember { mutableStateOf(transaction?.note ?: "") }
    var date by remember { mutableStateOf(transaction?.dateString ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction == null) "إضافة حركة لحساب ${account.name}" else "تعديل حركة حساب ${account.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "credit", onClick = { type = "credit" }, label = { Text("دائن (قبض منه/له)") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = type == "debit", onClick = { type = "debit" }, label = { Text("مدين (دفع له/عليه)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("البيان / الوصف") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("التاريخ") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(type, amount.toDoubleOrNull() ?: 0.0, desc, date, note) }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text("حفظ", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    title: String,
    date: String,
    items: List<Pair<String, String>>,
    total: String,
    accentColor: Color,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معاينة الفاتورة") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "جاري تحضير الطباعة...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Print, contentDescription = null)
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "تم حفظ الفاتورة كصورة", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("مزرعة البر", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text("فاتورة رسمية", fontSize = 16.sp, color = Color.Gray)
            
            Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 2.dp, color = Color.Black)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("التاريخ: $date", fontSize = 14.sp, color = Color.Black)
                Text("العنوان: $title", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.LightGray).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("البند / البيان", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, color = Color.Black)
                Text("القيمة", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            
            // Table Rows
            items.forEach { (name, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, modifier = Modifier.weight(2f), color = Color.Black)
                    Text(value, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.Black)
                }
                Divider(color = Color.LightGray, thickness = 0.5.dp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الإجمالي الكلي:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(total, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text("شكراً لتعاملكم معنا", fontSize = 12.sp, color = Color.Gray)
            Text("تطبيق المزرعة برو 2026", fontSize = 10.sp, color = Color.LightGray)
        }
    }
}
