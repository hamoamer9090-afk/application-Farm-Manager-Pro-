package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedesignedSettingsScreen(
    viewModel: FarmViewModel,
    accentColor: Color,
    zoomLevel: Float,
    onUpdateLabels: () -> Unit
) {
    val context = LocalContext.current

    val hideWelcomeCard by viewModel.hideWelcomeCard.collectAsStateWithLifecycle()
    val hideNetBalance by viewModel.hideNetBalance.collectAsStateWithLifecycle()
    val hideDashboardQuickActions by viewModel.hideDashboardQuickActions.collectAsStateWithLifecycle()
    val hideDashboardShortcuts by viewModel.hideDashboardShortcuts.collectAsStateWithLifecycle()
    val hideDashboardNotes by viewModel.hideDashboardNotes.collectAsStateWithLifecycle()
    val enableSwipeNavigation by viewModel.enableSwipeNavigation.collectAsStateWithLifecycle()
    val invertSwipeDirection by viewModel.invertSwipeDirection.collectAsStateWithLifecycle()

    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
    val appLockFingerprintEnabled by viewModel.appLockFingerprintEnabled.collectAsStateWithLifecycle()
    val appLockPinEnabled by viewModel.appLockPinEnabled.collectAsStateWithLifecycle()
    val appLockPatternEnabled by viewModel.appLockPatternEnabled.collectAsStateWithLifecycle()
    val appLockPinCode by viewModel.appLockPinCode.collectAsStateWithLifecycle()
    val appLockPatternCode by viewModel.appLockPatternCode.collectAsStateWithLifecycle()

    val hideSidebarDashboard by viewModel.hideSidebarDashboard.collectAsStateWithLifecycle()
    val hideSidebarBarn by viewModel.hideSidebarBarn.collectAsStateWithLifecycle()
    val hideSidebarFeeds by viewModel.hideSidebarFeeds.collectAsStateWithLifecycle()
    val hideSidebarAccounts by viewModel.hideSidebarAccounts.collectAsStateWithLifecycle()
    val hideSidebarNotes by viewModel.hideSidebarNotes.collectAsStateWithLifecycle()
    val hideSidebarArchive by viewModel.hideSidebarArchive.collectAsStateWithLifecycle()
    val hideSidebarBackup by viewModel.hideSidebarBackup.collectAsStateWithLifecycle()
    val hideSidebarFeedCalc by viewModel.hideSidebarFeedCalc.collectAsStateWithLifecycle()
    val hideSidebarReports by viewModel.hideSidebarReports.collectAsStateWithLifecycle()
    val hideSidebarReminders by viewModel.hideSidebarReminders.collectAsStateWithLifecycle()
    
    val farmName by viewModel.farmName.collectAsStateWithLifecycle()
    val primaryColorHex by viewModel.primaryColorHex.collectAsStateWithLifecycle()
    val cardColorHex by viewModel.cardColorHex.collectAsStateWithLifecycle()
    val textColorHex by viewModel.textColorHex.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedFont by viewModel.selectedFont.collectAsStateWithLifecycle()
    val appCurrency by viewModel.appCurrency.collectAsStateWithLifecycle()
    val appLang by viewModel.appLang.collectAsStateWithLifecycle()
    val hideFinancials by viewModel.hideFinancials.collectAsStateWithLifecycle()

    val attendanceTypes by viewModel.attendanceTypes.collectAsStateWithLifecycle()
    var showAddAttTypeDialog by remember { mutableStateOf(false) }

    var showFarmNameDialog by remember { mutableStateOf(false) }
    var editFarmName by remember { mutableStateOf(farmName) }

    var showFarmPasswordDialog by remember { mutableStateOf(false) }
    var editFarmPassword by remember { mutableStateOf("") }

    val spTitles = remember { context.getSharedPreferences("farm_titles", Context.MODE_PRIVATE) }
    var customDashboard by remember { mutableStateOf("") }
    var customBarn by remember { mutableStateOf("") }
    var customFeeds by remember { mutableStateOf("") }
    var customAccounts by remember { mutableStateOf("") }
    var customNotes by remember { mutableStateOf("") }
    var customArchive by remember { mutableStateOf("") }
    var customBackup by remember { mutableStateOf("") }
    var customFeedCalc by remember { mutableStateOf("") }
    var customReports by remember { mutableStateOf("") }
    var customReminders by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        customDashboard = spTitles.getString("label_dashboard", "لوحة التحكم الرئيسية") ?: "لوحة التحكم الرئيسية"
        customBarn = spTitles.getString("label_barn", "الحظيرة") ?: "الحظيرة"
        customFeeds = spTitles.getString("label_feeds", "الأعلاف") ?: "الأعلاف"
        customAccounts = spTitles.getString("label_accounts", "الحسابات") ?: "الحسابات"
        customNotes = spTitles.getString("label_notes", "الملاحظات والوسائط") ?: "الملاحظات والوسائط"
        customArchive = spTitles.getString("label_archive", "الأرشيف والبيانات") ?: "الأرشيف والبيانات"
        customBackup = spTitles.getString("label_backup", "النسخ الاحتياطي ونقل السجلات") ?: "النسخ الاحتياطي ونقل السجلات"
        customFeedCalc = spTitles.getString("label_feed_calc", "حاسبة الأعلاف") ?: "حاسبة الأعلاف"
        customReports = spTitles.getString("label_reports", "الإحصائيات والتقارير") ?: "الإحصائيات والتقارير"
        customReminders = spTitles.getString("label_reminders", "التنبيهات الذكية") ?: "التنبيهات الذكية"
    }

    val securityQuestion by viewModel.securityQuestion.collectAsStateWithLifecycle()
    val securityAnswer by viewModel.securityAnswer.collectAsStateWithLifecycle()

    var showMandatorySecurityDialog by remember { mutableStateOf(false) }
    var inputSecurityQuestion by remember { mutableStateOf("") }
    var inputSecurityAnswer by remember { mutableStateOf("") }

    var showSecurityAuthDialog by remember { mutableStateOf(false) }
    var securityAuthAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var securityAuthError by remember { mutableStateOf("") }
    var securityAuthInputPin by remember { mutableStateOf("") }
    var securityAuthInputAnswer by remember { mutableStateOf("") }

    var clearAnimals by remember { mutableStateOf(false) }
    var clearFeeds by remember { mutableStateOf(false) }
    var clearTransactions by remember { mutableStateOf(false) }
    var clearNotes by remember { mutableStateOf(false) }
    var clearMedical by remember { mutableStateOf(false) }
    var clearActivityLogs by remember { mutableStateOf(false) }
    var clearAttendance by remember { mutableStateOf(false) }
    var clearPersonalAccounts by remember { mutableStateOf(false) }



    if (showFarmNameDialog) {
        AlertDialog(
            onDismissRequest = { showFarmNameDialog = false },
            title = { Text("تعديل اسم المزرعة") },
            text = {
                OutlinedTextField(
                    value = editFarmName,
                    onValueChange = { editFarmName = it },
                    label = { Text("اسم المزرعة") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateFarmName(editFarmName)
                    showFarmNameDialog = false
                }) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showFarmNameDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showAddAttTypeDialog) {
        var newLabel by remember { mutableStateOf("") }
        var newColorHex by remember { mutableStateOf("#059669") }
        AlertDialog(
            onDismissRequest = { showAddAttTypeDialog = false },
            title = { Text("إضافة خيار حالة حضور") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = newLabel, onValueChange = { newLabel = it }, label = { Text("اسم الحالة (مثال: تأخير)") }, modifier = Modifier.fillMaxWidth())
                    Text("اختر لوناً مميزاً:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#059669", "#DC2626", "#2563EB", "#F59E0B", "#B45309", "#0D9488", "#7C3AED", "#DB2777", "#4B5563", "#000000").forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(color)))
                                    .border(if (newColorHex == color) 3.dp else 1.dp, if (newColorHex == color) Color.Black else Color.LightGray, CircleShape)
                                    .clickable { newColorHex = color }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLabel.isNotBlank()) {
                            val newList = attendanceTypes + com.example.data.model.AttendanceType(newLabel, newColorHex)
                            viewModel.updateAttendanceTypes(newList)
                        }
                        showAddAttTypeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("إضافة", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showAddAttTypeDialog = false }) { Text("إلغاء") } }
        )
    }

    if (showFarmPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showFarmPasswordDialog = false },
            title = { Text("تغيير كلمة مرور المزرعة") },
            text = {
                Column {
                    Text("اترك الحقل فارغاً لإيقاف كلمة المرور", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editFarmPassword,
                        onValueChange = { editFarmPassword = it },
                        label = { Text("كلمة المرور الجديدة") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateFarmPassword(editFarmPassword) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    showFarmPasswordDialog = false
                }) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showFarmPasswordDialog = false }) { Text("إلغاء") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        // State for main categories
        var expandedCategory by remember { mutableStateOf<String?>(null) }
        val toggleCategory: (String) -> Unit = { category ->
            expandedCategory = if (expandedCategory == category) null else category
        }

        // Card 1: إعدادات الهيكل والتخصيص
        SettingSection(
            titleText = "إعدادات الهيكل والتخصيص",
            icon = Icons.Default.DashboardCustomize,
            isExpanded = expandedCategory == "layout",
            onToggle = { toggleCategory("layout") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Farm Name
                Column {
                    Text(
                        text = "إعدادات المزرعة الحالية ($farmName):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign = TextAlign.Right
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                editFarmName = farmName
                                showFarmNameDialog = true 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تغيير اسم المزرعة", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = { 
                                editFarmPassword = ""
                                showFarmPasswordDialog = true 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تغيير كلمة المرور", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Lock, contentDescription = "قفل", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Sidebar
                val sidebarOrder by viewModel.sidebarItemsOrder.collectAsStateWithLifecycle()
                var draggedSidebarIndex by remember { mutableStateOf<Int?>(null) }
                var dragSidebarOffset by remember { mutableStateOf(0f) }

                ExpandableSubSection("تخصيص عناصر القائمة الجانبية (اضغط مع الاستمرار للترتيب)", icon = Icons.Default.Sort, accentColor = accentColor) {
                    class SettingOption(val title: String, val isVisible: Boolean, val toggleFn: (Boolean) -> Unit)
                    val allSidebarOptions = mapOf(
                        "dashboard" to SettingOption("لوح التحكم الرئيسية", !hideSidebarDashboard) { viewModel.toggleHideSidebarDashboard(!it) },
                        "barn" to SettingOption("الحظيرة", !hideSidebarBarn) { viewModel.toggleHideSidebarBarn(!it) },
                        "feeds" to SettingOption("الأعلاف", !hideSidebarFeeds) { viewModel.toggleHideSidebarFeeds(!it) },
                        "accounts" to SettingOption("الحسابات", !hideSidebarAccounts) { viewModel.toggleHideSidebarAccounts(!it) },
                        "notes" to SettingOption("الملاحظات والوسائط", !hideSidebarNotes) { viewModel.toggleHideSidebarNotes(!it) },
                        "archive" to SettingOption("الأرشيف والبيانات", !hideSidebarArchive) { viewModel.toggleHideSidebarArchive(!it) },
                        "backup" to SettingOption("النسخ الاحتياطي", !hideSidebarBackup) { viewModel.toggleHideSidebarBackup(!it) },
                        "feed_calc" to SettingOption("حاسبة الأعلاف", !hideSidebarFeedCalc) { viewModel.toggleHideSidebarFeedCalc(!it) },
                        "reports" to SettingOption("الإحصائيات والتقارير", !hideSidebarReports) { viewModel.toggleHideSidebarReports(!it) },
                        "reminders" to SettingOption("التنبيهات الذكية", !hideSidebarReminders) { viewModel.toggleHideSidebarReminders(!it) },
                        "settings" to SettingOption("الإعدادات", true) { }
                    )

                    Column {
                        sidebarOrder.forEachIndexed { index, itemKey ->
                            val option = allSidebarOptions[itemKey]
                            if (option != null) {
                                val isDragged = index == draggedSidebarIndex
                                val yOffset = if (isDragged) dragSidebarOffset else 0f

                                Box(
                                    modifier = Modifier
                                        .zIndex(if (isDragged) 1f else 0f)
                                        .graphicsLayer { translationY = yOffset }
                                        .pointerInput(Unit) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { draggedSidebarIndex = index },
                                                onDragEnd = { draggedSidebarIndex = null; dragSidebarOffset = 0f },
                                                onDragCancel = { draggedSidebarIndex = null; dragSidebarOffset = 0f },
                                                onDrag = { change, dragAmount -> 
                                                    change.consume()
                                                    dragSidebarOffset += dragAmount.y
                                                    val approximateItemHeight = 60.dp.toPx()
                                                    val targetIndex = (index + (dragSidebarOffset / approximateItemHeight).roundToInt()).coerceIn(0, sidebarOrder.size - 1)
                                                    if (targetIndex != index) {
                                                        viewModel.reorderSidebarItems(index, targetIndex)
                                                        draggedSidebarIndex = targetIndex
                                                        dragSidebarOffset -= (targetIndex - index) * approximateItemHeight
                                                    }
                                                }
                                            )
                                        }
                                ) {
                                    SwitchSettingItem(label = option.title, isChecked = option.isVisible, onCheckedChange = option.toggleFn)
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Dashboard
                val dashboardOrder by viewModel.dashboardItemsOrder.collectAsStateWithLifecycle()
                var draggedDashboardIndex by remember { mutableStateOf<Int?>(null) }
                var dragDashboardOffset by remember { mutableStateOf(0f) }

                ExpandableSubSection("تخصيص عناصر الشاشة الرئيسية (اضغط مع الاستمرار للترتيب)", icon = Icons.Default.ViewQuilt, accentColor = accentColor) {
                    class SettingOption(val title: String, val isVisible: Boolean, val toggleFn: (Boolean) -> Unit)
                    val allDashboardOptions = mapOf(
                        "net_balance" to SettingOption("بطاقات الإحصائيات المالية", !hideNetBalance) { viewModel.toggleHideNetBalance(!it) },
                        "quick_actions" to SettingOption("لوحة الإجراءات السريعة", !hideDashboardQuickActions) { viewModel.toggleHideDashboardQuickActions(!it) },
                        "shortcuts" to SettingOption("أزرار أقسام المزرعة", !hideDashboardShortcuts) { viewModel.toggleHideDashboardShortcuts(!it) },
                        "notes" to SettingOption("الملاحظات والمذكرات", !hideDashboardNotes) { viewModel.toggleHideDashboardNotes(!it) }
                    )

                    Column {
                        dashboardOrder.forEachIndexed { index, itemKey ->
                            val option = allDashboardOptions[itemKey]
                            if (option != null) {
                                val isDragged = index == draggedDashboardIndex
                                val yOffset = if (isDragged) dragDashboardOffset else 0f

                                Box(
                                    modifier = Modifier
                                        .zIndex(if (isDragged) 1f else 0f)
                                        .graphicsLayer { translationY = yOffset }
                                        .pointerInput(Unit) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { draggedDashboardIndex = index },
                                                onDragEnd = { draggedDashboardIndex = null; dragDashboardOffset = 0f },
                                                onDragCancel = { draggedDashboardIndex = null; dragDashboardOffset = 0f },
                                                onDrag = { change, dragAmount -> 
                                                    change.consume()
                                                    dragDashboardOffset += dragAmount.y
                                                    val approximateItemHeight = 60.dp.toPx()
                                                    val targetIndex = (index + (dragDashboardOffset / approximateItemHeight).roundToInt()).coerceIn(0, dashboardOrder.size - 1)
                                                    if (targetIndex != index) {
                                                        viewModel.reorderDashboardItems(index, targetIndex)
                                                        draggedDashboardIndex = targetIndex
                                                        dragDashboardOffset -= (targetIndex - index) * approximateItemHeight
                                                    }
                                                }
                                            )
                                        }
                                ) {
                                    SwitchSettingItem(label = option.title, isChecked = option.isVisible, onCheckedChange = option.toggleFn)
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Bottom bar customizable pins
                val pinnedBottomTabs by viewModel.pinnedBottomBarTabs.collectAsStateWithLifecycle()
                var draggedIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableStateOf(0f) }

                ExpandableSubSection("تثبيت الأقسام بالشريط السفلي والتنقل الديناميكي 📥", icon = Icons.Default.PushPin, accentColor = accentColor) {
                    Text(
                        "اختر الأقسام التي تود تثبيتها في شريط التنقل لتسهيل الوصول إليها. يمكنك الضغط مطولاً على القسم النشط وسحبه لتغيير ترتيبه في الشريط:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val allTabsMap = mapOf(
                        "dashboard" to "لوحة التحكم الرئيسية",
                        "barn" to "الحظيرة",
                        "feeds" to "الأعلاف",
                        "accounts" to "الحسابات (دفتر الأستاذ)",
                        "notes" to "الملاحظات والوسائط",
                        "archive" to "الأرشيف والبيانات",
                        "backup" to "النسخ الاحتياطي",
                        "feed_calculator" to "حاسبة الأعلاف",
                        "analytics" to "الإحصائيات والتقارير",
                        "reminders" to "التنبيهات الذكية"
                    )

                    Column {
                        // 1. Render Pinned items natively draggable
                        pinnedBottomTabs.forEachIndexed { index, tabKey ->
                            val title = allTabsMap[tabKey] ?: tabKey
                            val isDragged = index == draggedIndex
                            val yOffset = if (isDragged) dragOffset else 0f

                            Box(
                                modifier = Modifier
                                    .zIndex(if (isDragged) 1f else 0f)
                                    .graphicsLayer { translationY = yOffset }
                                    .pointerInput(Unit) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { draggedIndex = index },
                                            onDragEnd = { draggedIndex = null; dragOffset = 0f },
                                            onDragCancel = { draggedIndex = null; dragOffset = 0f },
                                            onDrag = { change, dragAmount -> 
                                                change.consume()
                                                dragOffset += dragAmount.y
                                                val approximateItemHeight = 60.dp.toPx()
                                                val targetIndex = (index + (dragOffset / approximateItemHeight).roundToInt()).coerceIn(0, pinnedBottomTabs.size - 1)
                                                if (targetIndex != index) {
                                                    viewModel.reorderPinnedBottomBarTabs(index, targetIndex)
                                                    draggedIndex = targetIndex
                                                    dragOffset -= (targetIndex - index) * approximateItemHeight
                                                }
                                            }
                                        )
                                    }
                            ) {
                                SwitchSettingItem("$title (اضغط مطولاً للسحب 🔄)", true) { viewModel.togglePinBottomBarTab(tabKey, it) }
                            }
                        }

                        if (pinnedBottomTabs.isNotEmpty() && pinnedBottomTabs.size < allTabsMap.size) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        // 2. Render Unpinned items below
                        val unpinnedTabs = allTabsMap.keys.filter { it !in pinnedBottomTabs }
                        unpinnedTabs.forEach { tabKey ->
                            val title = allTabsMap[tabKey] ?: tabKey
                            SwitchSettingItem(title, false) { viewModel.togglePinBottomBarTab(tabKey, it) }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                ExpandableSubSection("إعدادات التنقل بين الصفحات", icon = Icons.AutoMirrored.Filled.DirectionsRun, accentColor = accentColor) {
                    SwitchSettingItem("تفعيل التنقل بالسحب يمين/يسار", enableSwipeNavigation) { viewModel.toggleEnableSwipeNavigation(it) }
                    SwitchSettingItem("عكس اتجاه السحب", invertSwipeDirection) { viewModel.toggleInvertSwipeDirection(it) }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                ExpandableSubSection("تعديل وتخصيص التسميات في واجهات التطبيق 🖋️", icon = Icons.Default.DriveFileRenameOutline, accentColor = accentColor) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "يمكنك هنا تغيير أسماء الأقسام الرئيسية لتناسب طبيعة عملك (مثلاً تغيير 'الحظيرة' إلى 'المستودع'):",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customDashboard,
                            onValueChange = { customDashboard = it },
                            label = { Text("تسمية اللوحة الرئيسية") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customBarn,
                            onValueChange = { customBarn = it },
                            label = { Text("تسمية قسم الحظيرة") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customFeeds,
                            onValueChange = { customFeeds = it },
                            label = { Text("تسمية قسم الأعلاف") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customAccounts,
                            onValueChange = { customAccounts = it },
                            label = { Text("تسمية قسم الحسابات") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customNotes,
                            onValueChange = { customNotes = it },
                            label = { Text("تسمية الملاحظات والوسائط") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customArchive,
                            onValueChange = { customArchive = it },
                            label = { Text("تسمية الأرشيف والبيانات") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customBackup,
                            onValueChange = { customBackup = it },
                            label = { Text("تسمية النسخ الاحتياطي") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customFeedCalc,
                            onValueChange = { customFeedCalc = it },
                            label = { Text("تسمية حاسبة الأعلاف") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customReports,
                            onValueChange = { customReports = it },
                            label = { Text("تسمية الإحصائيات والتقارير") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customReminders,
                            onValueChange = { customReminders = it },
                            label = { Text("تسمية التنبيهات الذكية") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                with(spTitles.edit()) {
                                    putString("label_dashboard", customDashboard)
                                    putString("label_barn", customBarn)
                                    putString("label_feeds", customFeeds)
                                    putString("label_accounts", customAccounts)
                                    putString("label_notes", customNotes)
                                    putString("label_archive", customArchive)
                                    putString("label_backup", customBackup)
                                    putString("label_feed_calc", customFeedCalc)
                                    putString("label_reports", customReports)
                                    putString("label_reminders", customReminders)
                                    apply()
                                }
                                onUpdateLabels()
                                Toast.makeText(context, "تم حفظ المسميات الجديدة بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("حفظ التسميات المخصصة", color = Color.White)
                        }
                    }
                }

            }
        }

        // Card 2: السمات والألوان
        SettingSection(
            titleText = "السمات والألوان",
            icon = Icons.Default.Palette,
            isExpanded = expandedCategory == "theme",
            onToggle = { toggleCategory("theme") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                SubSection("السمة اللونية برو:") {
                    val primaryOptions = listOf(
                        Triple("أخضر زمردي", "#117A65", true),
                        Triple("أزرق ملكي", "#1A5276", false),
                        Triple("ذهبي عسلي", "#B7950B", false),
                        Triple("بنفسجي فاخر", "#6C3483", false)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        primaryOptions.forEach { (label, hex, _) ->
                            ColorButton(
                                label = label,
                                colorHex = hex,
                                isSelected = primaryColorHex == hex,
                                onClick = { viewModel.updateThemePrimaryColor(hex) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                SubSection("نمط المظهر (ليل ونهار):") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionButton("تلقائي", Icons.Default.Settings, themeMode == "system", { viewModel.updateThemeMode("system") }, accentColor, Modifier.weight(1f))
                        OptionButton("مضيء (نهار)", Icons.Default.WbSunny, themeMode == "light", { viewModel.updateThemeMode("light") }, accentColor, Modifier.weight(1f))
                        OptionButton("مظلم (ليل)", Icons.Default.DarkMode, themeMode == "dark", { viewModel.updateThemeMode("dark") }, accentColor, Modifier.weight(1f))
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                SubSection("لون خلفية الكروت والبطاقات:") {
                    val cardOptions = listOf(
                        Pair("أبيض", "#FFFFFF"),
                        Pair("رمادي", "#F3F4F6"),
                        Pair("وردي", "#FDF2F8"),
                        Pair("داكن", "#1E293B"),
                        Pair("أسود", "#000000")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cardOptions.forEach { (label, hex) ->
                            ColorButton(
                                label = label,
                                colorHex = hex,
                                isSelected = cardColorHex == hex,
                                onClick = { viewModel.updateCardBackgroundColor(hex) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { Toast.makeText(context, "الطبقة المتقدمة لتكوين الألوان قيد التطوير", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("فتح تركيب وتدريج لون مخصص 🎨", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Card 3: الخطوط والتحكم بالنصوص
        SettingSection(
            titleText = "الخطوط والتحكم بالنصوص",
            icon = Icons.Default.TextFields,
            isExpanded = expandedCategory == "fonts",
            onToggle = { toggleCategory("fonts") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                SubSection("نوع وتصميم الخط العربي:") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionButton("تيجوال", null, selectedFont == "tajawal", { viewModel.updateFontFamily("tajawal") }, accentColor, Modifier.weight(1f))
                        OptionButton("أميري", null, selectedFont == "amiri", { viewModel.updateFontFamily("amiri") }, accentColor, Modifier.weight(1f))
                        OptionButton("كايرو", null, selectedFont == "cairo", { viewModel.updateFontFamily("cairo") }, accentColor, Modifier.weight(1f))
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                SubSection("لون خطوط ونصوص التطبيق:") {
                    val textOptions = listOf(
                        Pair("تلقائي", "#1E293B"),
                        Pair("داكن", "#334155"),
                        Pair("أسود", "#000000"),
                        Pair("أبيض", "#FFFFFF"),
                        Pair("أصفر", "#FDE047")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        textOptions.forEach { (label, hex) ->
                            ColorButton(
                                label = label,
                                colorHex = hex,
                                isSelected = textColorHex == hex,
                                onClick = { viewModel.updateTypographyColor(hex) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { Toast.makeText(context, "الطبقة المتقدمة لتكوين الألوان قيد التطوير", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("فتح تركيب وتخصيص لون نصوص دقيق 🖌️", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                SubSection("حجم النصوص والواجهة (Zoom & Scale):") {
                    Column {
                        Slider(
                            value = zoomLevel,
                            onValueChange = { viewModel.updateZoomLevel(it) },
                            valueRange = 12f..20f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("صغير (12)", fontSize = 12.sp, color = Color.Gray)
                            Text("المعتدل (16)", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
                            Text("ضخم (20)", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                SubSection("لغة التطبيق / App Language:") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionButton(
                            label = "العربية 🇸🇦",
                            icon = null,
                            isSelected = appLang == "ar",
                            onClick = { viewModel.updateAppLang("ar") },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                        OptionButton(
                            label = "English 🇬🇧",
                            icon = null,
                            isSelected = appLang == "en",
                            onClick = { viewModel.updateAppLang("en") },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
        val appLockType by viewModel.appLockType.collectAsStateWithLifecycle()

        // Card 4: الأمان والخصوصية
        SettingSection(
            titleText = "الأمان والخصوصية",
            icon = Icons.Default.Lock,
            isExpanded = expandedCategory == "security",
            onToggle = { toggleCategory("security") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                val isBiometricAuthEnabled by viewModel.isBiometricAuthEnabled.collectAsStateWithLifecycle()
                SubSection("إدارة الجلسة والمصادقة:", "التحقق من المستخدم أو الجلسة عند الدخول") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("المصادقة الحيوية (Biometrics)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("فرض بصمة/وجه للدخول للتطبيق", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isBiometricAuthEnabled,
                            onCheckedChange = { viewModel.toggleBiometricAuth(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                
                SubSection("قفل التطبيق (App Lock):", "التحكم في حماية المزرعة والبيانات") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل قفل التطبيق", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (securityQuestion.isBlank() || securityAnswer.isBlank()) {
                                        inputSecurityQuestion = ""
                                        inputSecurityAnswer = ""
                                        showMandatorySecurityDialog = true
                                    } else {
                                        viewModel.toggleAppLock(true)
                                    }
                                } else {
                                    viewModel.toggleAppLock(false)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                        )
                    }

                    AnimatedVisibility(visible = isAppLockEnabled) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                            Text("اختر طرق الحماية (يمكن تفعيل أكثر من واحدة):", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            
                            var showPinDialog by remember { mutableStateOf(false) }
                            var tempPin by remember { mutableStateOf("") }
                            
                            var showPatternDialog by remember { mutableStateOf(false) }
                            var tempPattern by remember { mutableStateOf("") }

                            SwitchSettingItem("قفل الهوية (بصمة/وجه/قفل الهاتف)", appLockFingerprintEnabled) { viewModel.toggleAppLockFingerprint(it) }
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("رمز (PIN) مخصص", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (appLockPinEnabled && appLockPinCode.isNotEmpty()) Text("تغيير الرمز", fontSize = 11.sp, color = accentColor, modifier = Modifier.clickable { showPinDialog = true })
                                }
                                Switch(
                                    checked = appLockPinEnabled,
                                    onCheckedChange = { 
                                        if (it) { showPinDialog = true } 
                                        else { viewModel.toggleAppLockPin(false) }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                                )
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("نقش مخصص", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (appLockPatternEnabled && appLockPatternCode.isNotEmpty()) Text("تغيير النقش", fontSize = 11.sp, color = accentColor, modifier = Modifier.clickable { showPatternDialog = true })
                                }
                                Switch(
                                    checked = appLockPatternEnabled,
                                    onCheckedChange = { 
                                        if (it) { showPatternDialog = true } 
                                        else { viewModel.toggleAppLockPattern(false) }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("سؤال الأمان للحماية وطوارئ المرور", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (securityQuestion.isNotEmpty()) {
                                        Text("السؤال الحالي: $securityQuestion", fontSize = 11.sp, color = Color.Gray)
                                    } else {
                                        Text("لم يتم التعيين - يرجى الإعداد للحماية ⚠️", fontSize = 11.sp, color = Color.Gray.copy(alpha = 0.7f))
                                    }
                                }
                                Button(
                                    onClick = {
                                        inputSecurityQuestion = securityQuestion
                                        inputSecurityAnswer = securityAnswer
                                        showMandatorySecurityDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f), contentColor = accentColor),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("تعديل السؤال", fontSize = 12.sp)
                                }
                            }
                            
                            if (showPinDialog) {
                                AlertDialog(
                                    onDismissRequest = { showPinDialog = false },
                                    title = { Text("تعيين رمز PIN مخصص", fontWeight = FontWeight.Bold) },
                                    text = {
                                        OutlinedTextField(value = tempPin, onValueChange = { tempPin = it }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword))
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            viewModel.updateAppLockPinCode(tempPin)
                                            viewModel.toggleAppLockPin(true)
                                            showPinDialog = false
                                        }) { Text("حفظ") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showPinDialog = false }) { Text("إلغاء") }
                                    }
                                )
                            }
                            
                            if (showPatternDialog) {
                                AlertDialog(
                                    onDismissRequest = { showPatternDialog = false },
                                    title = { Text("تعيين نقش مخصص", fontWeight = FontWeight.Bold) },
                                    text = {
                                        Column {
                                            Text("للتسهيل، يرجى كتابة كلمة مرور/نقش نصي (مثل: 12369) ليحاكي النقش", fontSize = 12.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(value = tempPattern, onValueChange = { tempPattern = it }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password))
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            viewModel.updateAppLockPatternCode(tempPattern)
                                            viewModel.toggleAppLockPattern(true)
                                            showPatternDialog = false
                                        }) { Text("حفظ") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showPatternDialog = false }) { Text("إلغاء") }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        var showClearDataConfirm by remember { mutableStateOf(false) }
        var showDeleteFarmConfirm by remember { mutableStateOf(false) }

        // Card: النسخ الاحتياطي والمزامنة (Firebase) - Conditional Show
        if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
            SettingSection(
                titleText = "حالة الاتصال والمزامنة (Firebase)",
                icon = Icons.Default.CloudSync,
                isExpanded = expandedCategory == "firebase",
                onToggle = { toggleCategory("firebase") }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val coroutineScope = rememberCoroutineScope()
                    var firebaseStatus by remember { mutableStateOf("لم يتم الفحص") }
                    
                    Text(text = "حالة الاتصال: $firebaseStatus", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Button(
                        onClick = {
                            firebaseStatus = "جاري الفحص..."
                            coroutineScope.launch {
                                val firebaseManager = com.example.data.remote.FirebaseManager()
                                try {
                                    val auth = firebaseManager.auth
                                    if (auth?.currentUser == null) {
                                        val success = firebaseManager.signInAnonymously()
                                        if (success) {
                                            firebaseStatus = "متصل بنجاح ✅ (حساب زائر)"
                                        } else {
                                            firebaseStatus = "فشل في تسجيل الدخول ❌"
                                        }
                                    } else {
                                        firebaseStatus = "متصل بنجاح ✅ (معرف: ${auth.currentUser?.uid?.take(8)}...)"
                                    }
                                } catch (e: Exception) {
                                    firebaseStatus = "فشل الاتصال ❌: ${e.localizedMessage}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("أداة فحص وتأكيد الاتصال بـ Firebase", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CloudDone, contentDescription = "اختبار", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        // Card 5: الإضافات (Plugins & Add-ons)
        val isAttendanceEnabled by viewModel.isAttendancePluginEnabled.collectAsStateWithLifecycle()
        val isNotesPluginEnabled by viewModel.isNotesPluginEnabled.collectAsStateWithLifecycle()

        SettingSection(
            titleText = "نظام الإضافات المجانية",
            icon = Icons.Default.Extension,
            isExpanded = expandedCategory == "plugins",
            onToggle = { toggleCategory("plugins") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SubSection("تفعيل وحدات إضافية (Add-ons module):") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("جدول التسجيل اليومي", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("تتبع حضور موظفي المزرعة والملاحظات اليومية", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isAttendanceEnabled,
                            onCheckedChange = { viewModel.toggleAttendancePlugin(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الملاحظات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("تسجيل وحفظ المهام والأفكار في شبكة منظمة", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isNotesPluginEnabled,
                            onCheckedChange = { viewModel.toggleNotesPlugin(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                        )
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        // Card 6: إدارة البيانات والنظام
        SettingSection(
            titleText = "إدارة البيانات والنظام",
            icon = Icons.Default.AdminPanelSettings,
            isExpanded = expandedCategory == "system",
            onToggle = { toggleCategory("system") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.factoryResetSettings()
                        Toast.makeText(context, "تمت إعادة استعادة ضبط المصنع للواجهة", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("استعادة ضبط المصنع للتخصيص", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Refresh, contentDescription = "استعادة", tint = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { showClearDataConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("إدارة مسح وحذف البيانات", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.DeleteSweep, contentDescription = "مسح", tint = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { showDeleteFarmConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("إدارة حذف المزرعة الحالية", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.DeleteForever, contentDescription = "حذف نهائي", tint = MaterialTheme.colorScheme.onSurface)
                }

                // Share App APK Button
                Button(
                    onClick = { com.example.util.ShareUtil.shareAppApk(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Text("مشاركة التطبيق كملف APK", color = accentColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = accentColor)
                }
            }
        }

        if (showClearDataConfirm) {
            AlertDialog(
                onDismissRequest = { showClearDataConfirm = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFF97316))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مسح بيانات مخصصة", fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("اختر الأقسام التي تود مسح بياناتها نهائياً:", fontSize = 12.sp, color = Color.Gray)
                        
                        SelectiveClearItem("الحيوانات والولادات", clearAnimals) { clearAnimals = it }
                        SelectiveClearItem("الأعلاف والمخازن", clearFeeds) { clearFeeds = it }
                        SelectiveClearItem("المعاملات المالية", clearTransactions) { clearTransactions = it }
                        SelectiveClearItem("الملاحظات والوسائط", clearNotes) { clearNotes = it }
                        SelectiveClearItem("السجل الطبي والتحصينات", clearMedical) { clearMedical = it }
                        SelectiveClearItem("سجل النشاطات والتغييرات", clearActivityLogs) { clearActivityLogs = it }
                        SelectiveClearItem("جدول الحضور والغياب", clearAttendance) { clearAttendance = it }
                        SelectiveClearItem("الحسابات الشخصية", clearPersonalAccounts) { clearPersonalAccounts = it }
                        
                        if (clearAnimals || clearFeeds || clearTransactions || clearNotes || clearMedical || clearActivityLogs || clearAttendance || clearPersonalAccounts) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("تنبيه: سيتم حذف البيانات المختارة نهائياً ولا يمكن استعادتها.", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("يرجى اختيار قسم واحد على الأقل للمسح.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = clearAnimals || clearFeeds || clearTransactions || clearNotes || clearMedical || clearActivityLogs || clearAttendance || clearPersonalAccounts,
                        onClick = {
                            showClearDataConfirm = false
                            securityAuthAction = {
                                viewModel.clearCurrentFarmDataSelective(
                                    animals = clearAnimals,
                                    feeds = clearFeeds,
                                    transactions = clearTransactions,
                                    notes = clearNotes,
                                    medical = clearMedical,
                                    activityLogs = clearActivityLogs,
                                    attendance = clearAttendance,
                                    personalAccounts = clearPersonalAccounts
                                )
                                Toast.makeText(context, "تم مسح البيانات المختارة بنجاح", Toast.LENGTH_SHORT).show()
                                // Reset states
                                clearAnimals = false; clearFeeds = false; clearTransactions = false; clearNotes = false
                                clearMedical = false; clearActivityLogs = false; clearAttendance = false; clearPersonalAccounts = false
                            }
                            securityAuthError = ""
                            securityAuthInputPin = ""
                            securityAuthInputAnswer = ""
                            showSecurityAuthDialog = true
                        }
                    ) {
                        Text("تأكيد المسح", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataConfirm = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }

        if (showDeleteFarmConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteFarmConfirm = false },
                title = { Text("حذف المزرعة", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
                text = { Text("سيؤدي هذا إلى حذف المزرعة كاملة بجميع بياناتها وسجلاتها نهائياً من الجهاز! هل ترغب بالاستمرار؟") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteFarmConfirm = false
                        securityAuthAction = {
                            viewModel.deleteCurrentFarm()
                            Toast.makeText(context, "تم حذف المزرعة نهائياً", Toast.LENGTH_SHORT).show()
                        }
                        securityAuthError = ""
                        securityAuthInputPin = ""
                        securityAuthInputAnswer = ""
                        showSecurityAuthDialog = true
                    }) {
                        Text("حذف المزرعة", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteFarmConfirm = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }

        if (showMandatorySecurityDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showMandatorySecurityDialog = false 
                },
                title = { Text("سؤال أمان الحساب والطوارئ 🔑", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "يرجى تحديد سؤال أمان مخصص وإدخال إجابته الحامية. هذا السؤال سيكون وسيلتك الوحيدة لاسترجاع كود الدخول في حال نسيانه.",
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = inputSecurityQuestion,
                            onValueChange = { inputSecurityQuestion = it },
                            label = { Text("سؤال الأمان (مثال: ما اسم أول مدرسة التحقت بها؟)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = inputSecurityAnswer,
                            onValueChange = { inputSecurityAnswer = it },
                            label = { Text("إجابة السؤال (تأكد من حفظها بدقة)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputSecurityQuestion.isNotBlank() && inputSecurityAnswer.isNotBlank()) {
                                viewModel.updateSecurityQuestion(inputSecurityQuestion, inputSecurityAnswer)
                                viewModel.toggleAppLock(true)
                                showMandatorySecurityDialog = false
                                Toast.makeText(context, "تم حفظ وسيلة الأمان وتفعيل القفل بنجاح 🛡️", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "الرجاء كتابة السؤال والجواب معاً لاستكمال التفعيل", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("حفظ وتأمين الدخول", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showMandatorySecurityDialog = false 
                    }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }

        if (showSecurityAuthDialog) {
            val appLockPinCode by viewModel.appLockPinCode.collectAsStateWithLifecycle()
            val appLockPinEnabled by viewModel.appLockPinEnabled.collectAsStateWithLifecycle()

            val checkAndAuthorize = {
                var authorized = false
                if (appLockPinEnabled && appLockPinCode.isNotEmpty()) {
                    if (securityAuthInputPin == appLockPinCode) {
                        authorized = true
                    }
                }
                if (securityQuestion.isNotEmpty() && securityAnswer.isNotEmpty()) {
                    if (securityAuthInputAnswer.trim().equals(securityAnswer.trim(), ignoreCase = true)) {
                        authorized = true
                    }
                }
                if (!appLockPinEnabled && securityQuestion.isEmpty()) {
                    authorized = true
                }

                if (authorized) {
                    securityAuthAction?.invoke()
                    showSecurityAuthDialog = false
                } else {
                    securityAuthError = "الرمز المدخل أو إجابة سؤال الأمان غير صحيحة!"
                }
            }

            AlertDialog(
                onDismissRequest = { showSecurityAuthDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Red, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التحقق الأمني من الهوية ⚠️", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "أنت بصدد تغيير أو مسح ملفات المزرعة الحساسة. يرجى تأكيد ملكيتك وإثبات هويتك لإكمال المسح.",
                            fontSize = 13.sp
                        )

                        if (securityAuthError.isNotEmpty()) {
                            Text(securityAuthError, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (appLockPinEnabled && appLockPinCode.isNotEmpty()) {
                            OutlinedTextField(
                                value = securityAuthInputPin,
                                onValueChange = { securityAuthInputPin = it; securityAuthError = "" },
                                label = { Text("أدخل رمز PIN لقفل التطبيق") },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (securityQuestion.isNotEmpty() && securityAnswer.isNotEmpty()) {
                            Text("سؤال الأمان الحالي: $securityQuestion", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            OutlinedTextField(
                                value = securityAuthInputAnswer,
                                onValueChange = { securityAuthInputAnswer = it; securityAuthError = "" },
                                label = { Text("إجابة سؤال الأمان") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Button(
                            onClick = {
                                if (context is androidx.fragment.app.FragmentActivity) {
                                    com.example.utils.BiometricUtils.authenticate(
                                        activity = context,
                                        title = "تأكيد هوية المدير للمسح الحساس",
                                        subtitle = "تأكيد الإجراء عبر النظام لمنع العبث",
                                        onSuccess = {
                                            securityAuthAction?.invoke()
                                            showSecurityAuthDialog = false
                                        },
                                        onError = { 
                                            securityAuthError = "فشل التحقق بالنظام الإفتراضي للموبايل"
                                        }
                                    )
                                } else {
                                    securityAuthError = "المحاكي أو الجهاز لا يدعم بصمة النظام حالياً."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.15f), contentColor = accentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بصمة الإصبع أو قفل الموبايل 📱", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            var pinAnswerMatched = false
                            if (appLockPinEnabled && appLockPinCode.isNotEmpty() && securityAuthInputPin == appLockPinCode) {
                                pinAnswerMatched = true
                            }
                            if (securityQuestion.isNotEmpty() && securityAnswer.isNotEmpty() && securityAuthInputAnswer.trim().equals(securityAnswer.trim(), ignoreCase = true)) {
                                pinAnswerMatched = true
                            }
                            
                            if (pinAnswerMatched) {
                                securityAuthAction?.invoke()
                                showSecurityAuthDialog = false
                            } else {
                                if (context is androidx.fragment.app.FragmentActivity) {
                                    com.example.utils.BiometricUtils.authenticate(
                                        activity = context,
                                        title = "التحقق الأمني من الهوية ⚠️",
                                        subtitle = "أنت بصدد تغيير أو مسح ملفات المزرعة الحساسة.",
                                        onSuccess = {
                                            securityAuthAction?.invoke()
                                            showSecurityAuthDialog = false
                                        },
                                        onError = { err ->
                                            securityAuthError = "فشل التحقق بالنظام: $err"
                                        }
                                    )
                                } else {
                                    securityAuthError = "الرمز أو الإجابة غير صحيحة، أو عذرًا الجهاز لا يدعم البصمة الإفتراضية."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("تأكيد ومسح 🗑️", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSecurityAuthDialog = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }

        // Card 6: حول التطبيق ومشاركة التطبيق
        var showAboutDialog by remember { mutableStateOf(false) }
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حول التطبيق", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "تحميل تطبيق مدير المزرعة")
                                putExtra(Intent.EXTRA_TEXT, "بإمكانك تحميل تطبيق مدير المزرعة المتكامل APK من هنا: https://ais-pre-fxdj6rtgxitvvgrecrcgnx-336811481995.europe-west2.run.app")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Emerald Green
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاركة APK", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Agriculture, contentDescription = null, tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حول مدير المزرعة Pro", fontWeight = FontWeight.ExtraBold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("نظام إدارة المزارع المتكامل", fontWeight = FontWeight.Bold, color = accentColor)
                        Text("الإصدار: 3.5.0 Pro", fontSize = 13.sp)
                        Text("تم التطوير لدعم المربين والمزارعين في تنظيم الموارد والإنتاج الحيواني والداجني بدقة عالية.", fontSize = 12.sp, lineHeight = 18.sp)
                        if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
                            Text("يتميز التطبيق بنظام مزامنة سحابية متقدم مع Google Drive و Firebase لضمان أمان بياناتك.", fontSize = 12.sp, lineHeight = 18.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp).alpha(0.5f))
                        Text("© 2024 جميع الحقوق محفوظة", fontSize = 10.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) { Text("إغلاق", color = accentColor) }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
}

@Composable
fun SelectiveClearItem(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF97316))
            )
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
        }
    }
}

@Composable
fun SubSection(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun ExpandableSubSection(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotate_icon")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.rotate(rotation)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Right
                        )
                    }
                }
                if (icon != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
        }
        
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingSection(
    titleText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isExpanded: Boolean = true,
    onToggle: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val parts = titleText.split("\n", limit = 2)
    val title = parts[0]
    val subtitle = if (parts.size > 1) parts[1] else null

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onToggle != null) it.clip(RoundedCornerShape(16.dp)).clickable { onToggle() } else it }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onToggle != null) {
                    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotate_icon")
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.rotate(rotation)
                    )
                } else {
                    Spacer(modifier = Modifier.width(24.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                    Text(
                        text = title, 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface, 
                        textAlign = TextAlign.Right
                    )
                    if (icon != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ColorButton(label: String, colorHex: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colorString = colorHex.replace("#", "")
    val colorInt = try { android.graphics.Color.parseColor("#$colorString") } catch (e: Exception) { android.graphics.Color.GRAY }
    val color = Color(colorInt)
    
    val contentColor = if (colorHex.uppercase() == "#FFFFFF" || colorHex.uppercase() == "#F3F4F6" || colorHex.uppercase() == "#FDE047" || colorHex.uppercase() == "#FDF2F8") Color.Black else Color.White

    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        color = color,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = contentColor, modifier = Modifier.size(20.dp))
                }
                Text(label, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun OptionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector?, isSelected: Boolean, onClick: () -> Unit, accentColor: Color, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
                label, 
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, 
                fontSize = 12.sp, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SwitchSettingItem(label: String, isChecked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (icon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

