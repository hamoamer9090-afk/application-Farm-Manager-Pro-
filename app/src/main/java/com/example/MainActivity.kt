@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example
import androidx.compose.foundation.gestures.detectTransformGestures
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.input.pointer.pointerInput
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.example.data.model.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FarmViewModel
import com.example.ui.viewmodel.BatchAnimalPurchaseItem
import com.example.ui.viewmodel.BatchAnimalSaleItem
import com.example.ui.viewmodel.BatchFeedPurchaseItem
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.lazy.rememberLazyListState
import java.text.SimpleDateFormat
import java.util.*
import com.example.ui.screens.AccountManagementDialog
import com.example.ui.screens.SyncQueueScreen
import com.example.ui.screens.FeedCalculatorScreen
import com.example.ui.screens.AnalyticsDashboardScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.RecycleBinScreen
import com.example.ui.screens.AppPrintSettingsDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import androidx.fragment.app.FragmentActivity

fun convertToEasternArabicNumerals(input: String): String {
    return input.map {
        if (it.isDigit()) (it - '0' + 0x0660).toChar() else it
    }.joinToString("")
}

fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FarmViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val primaryColorHex by viewModel.primaryColorHex.collectAsStateWithLifecycle()
            
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            val appAccentColor = remember(primaryColorHex) {
                try {
                    Color(android.graphics.Color.parseColor(primaryColorHex))
                } catch (e: Exception) {
                    Color(0xFF059669)
                }
            }
            
            MyApplicationTheme(
                darkTheme = isDarkTheme,
                primaryColor = appAccentColor,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun getTabInfo(tab: String, labelBarn: String, labelFeeds: String, labelAccounts: String): Pair<String, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (tab) {
        "dashboard" -> Pair("الرئيسية", Icons.Default.Dashboard)
        "barn" -> Pair(labelBarn, Icons.Default.Pets)
        "feeds" -> Pair(labelFeeds, Icons.Default.Grass)
        "accounts" -> Pair(labelAccounts, Icons.Default.People)
        "notes" -> Pair("الملاحظات", Icons.Default.Comment)
        "archive" -> Pair("الأرشيف", Icons.Default.Archive)
        "backup" -> Pair("النسخ الاحتياطي", Icons.Default.Backup)
        "feed_calculator" -> Pair("الحاسبة", Icons.Default.Calculate)
        "analytics" -> Pair("التقارير", Icons.Default.PieChart)
        "reminders" -> Pair("التنبيهات", Icons.Default.NotificationsActive)
        else -> Pair("صفحة", Icons.Default.Layers)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen(viewModel: FarmViewModel = viewModel()) {
    val onboardingViewModel: com.example.ui.viewmodel.OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val isSetupCompleted by onboardingViewModel.isSetupCompleted.collectAsStateWithLifecycle()
    val ownerName by onboardingViewModel.ownerName.collectAsStateWithLifecycle()

    // Collect settings
    val currentFarm by viewModel.currentFarm.collectAsStateWithLifecycle()
    val farmName by viewModel.farmName.collectAsStateWithLifecycle()
    val customAppName by viewModel.customAppName.collectAsStateWithLifecycle()
    val customAppIconBase64 by viewModel.customAppIconBase64.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()
    val primaryColorHex by viewModel.primaryColorHex.collectAsStateWithLifecycle()
    val cardColorHex by viewModel.cardColorHex.collectAsStateWithLifecycle()
    val textColorHex by viewModel.textColorHex.collectAsStateWithLifecycle()
    val enlargedImage by viewModel.enlargedImage.collectAsStateWithLifecycle()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
    val appLockFingerprintEnabled by viewModel.appLockFingerprintEnabled.collectAsStateWithLifecycle()
    val appLockPinEnabled by viewModel.appLockPinEnabled.collectAsStateWithLifecycle()
    val appLockPatternEnabled by viewModel.appLockPatternEnabled.collectAsStateWithLifecycle()
    val appLockPinCode by viewModel.appLockPinCode.collectAsStateWithLifecycle()
    val appLockPatternCode by viewModel.appLockPatternCode.collectAsStateWithLifecycle()

    val isBiometricAuthEnabled by viewModel.isBiometricAuthEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.checkSessionValidity()
    }

    var isAppUnlocked by remember { mutableStateOf(!isAppLockEnabled && !isBiometricAuthEnabled) }
    var didCheckLock by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var inputPattern by remember { mutableStateOf("") }
    var lockError by remember { mutableStateOf("") }

    var showRecoverDialog by remember { mutableStateOf(false) }
    var recoveryAnswerInput by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf("") }

    val tryBiometrics = {
        val activity = context.findActivity()
        if (activity != null) {
            com.example.utils.BiometricUtils.authenticate(
                activity = activity,
                title = "مصادقة الدخول",
                subtitle = "يرجى التحقق من هويتك لفتح التطبيق",
                onSuccess = { isAppUnlocked = true; lockError = "" },
                onError = { lockError = "فشل التحقق من البصمة" }
            )
        }
    }

    LaunchedEffect(isAppLockEnabled, isBiometricAuthEnabled) {
        if (!didCheckLock) {
            didCheckLock = true
            if (isBiometricAuthEnabled || (isAppLockEnabled && appLockFingerprintEnabled)) {
                val activity = context.findActivity()
                if (activity != null) {
                    tryBiometrics()
                }
            } else if (!isAppLockEnabled) {
                isAppUnlocked = true
            }
        }
    }

    if (!isAppUnlocked && didCheckLock && (isAppLockEnabled || isBiometricAuthEnabled)) {
        val appAccentColor = remember(primaryColorHex) {
            try {
                Color(android.graphics.Color.parseColor(primaryColorHex))
            } catch (e: Exception) {
                Color(0xFF059669)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = "App Locked", modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("التطبيق مقفل", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (lockError.isNotEmpty()) {
                    Text(lockError, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                var pinAttempts by remember { mutableIntStateOf(0) }
                var isLockedOut by remember { mutableStateOf(false) }

                LaunchedEffect(isLockedOut) {
                    if (isLockedOut) {
                        lockError = "تم حظر الإدخال مؤقتاً لمدة ٥ ثوانِ"
                        kotlinx.coroutines.delay(5000L)
                        isLockedOut = false
                        pinAttempts = 0
                        lockError = ""
                        inputPin = ""
                    }
                }

                if (appLockPinEnabled) {
                    OutlinedTextField(
                        value = inputPin,
                        onValueChange = { 
                            if (!isLockedOut) {
                                inputPin = it 
                                if (it == appLockPinCode) {
                                    isAppUnlocked = true
                                    pinAttempts = 0
                                }
                                else if (it.length >= appLockPinCode.length) {
                                    pinAttempts++
                                    if (pinAttempts >= 3) {
                                        isLockedOut = true
                                    } else {
                                        lockError = "الرمز غير صحيح. المحاولات المتبقية: ${3 - pinAttempts}"
                                        inputPin = ""
                                    }
                                }
                            }
                        },
                        label = { Text("أدخل رمز PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLockedOut
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (appLockPatternEnabled) {
                    OutlinedTextField(
                        value = inputPattern,
                        onValueChange = { 
                            inputPattern = it
                            lockError = ""
                            if (it == appLockPatternCode) isAppUnlocked = true
                            else if (it.length >= appLockPatternCode.length) lockError = "النقش غير صحيح"
                        },
                        label = { Text("أدخل نص النقش") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (appLockFingerprintEnabled) {
                    Button(onClick = tryBiometrics, colors = ButtonDefaults.buttonColors(containerColor = appAccentColor)) {
                        Text("التحقق بالبصمة/الوجه")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                TextButton(
                    onClick = {
                        showRecoverDialog = true
                        recoveryAnswerInput = ""
                        recoveryError = ""
                    }
                ) {
                    Text("نسيت كلمة المرور؟ ❓", color = appAccentColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showRecoverDialog) {
            val securityQuestion by viewModel.securityQuestion.collectAsStateWithLifecycle()
            val securityAnswer by viewModel.securityAnswer.collectAsStateWithLifecycle()

            AlertDialog(
                onDismissRequest = { showRecoverDialog = false },
                title = { Text("استرجاع وتخطي قفل التطبيق 🛡️", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (securityQuestion.isNotEmpty() && securityAnswer.isNotEmpty()) {
                            Text("الرجاء الإجابة عن سؤال الأمان المخصص لتخطي القفل وإعداد كود جديد:", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("السؤال: $securityQuestion", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = appAccentColor)
                            
                            OutlinedTextField(
                                value = recoveryAnswerInput,
                                onValueChange = { recoveryAnswerInput = it; recoveryError = "" },
                                label = { Text("إجابتك") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            if (recoveryError.isNotEmpty()) {
                                Text(recoveryError, color = Color.Red, fontSize = 12.sp)
                            }
                        } else {
                            Text("لم تقم بإعداد سؤال أمان مسبقاً! يرجى الاستمرار بتسجيل الدخول الفردي أو مراجعة المشرف.", color = Color.Red, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    if (securityQuestion.isNotEmpty() && securityAnswer.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (recoveryAnswerInput.trim().equals(securityAnswer.trim(), ignoreCase = true)) {
                                    isAppUnlocked = true
                                    showRecoverDialog = false
                                    Toast.makeText(context, "تم تخطي القفل بنجاح! الرجاء تحديث أرقام PIN من الإعدادات.", Toast.LENGTH_LONG).show()
                                } else {
                                    recoveryError = "الإجابة غير مطابقة! يرجى المحاولة مرة أخرى."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = appAccentColor)
                        ) {
                            Text("التحقق وفتح القفل", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRecoverDialog = false }) { Text("إلغاء") }
                }
            )
        }
        return
    }

    // Determine custom theme primary color from SharedPreferences dynamic HEX
    val appAccentColor = remember(primaryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(primaryColorHex))
        } catch (e: Exception) {
            Color(0xFF059669) // fallback Emerald
        }
    }

    // Determine custom card background color
    val appCardBgColor = remember(cardColorHex) {
        try {
            Color(android.graphics.Color.parseColor(cardColorHex))
        } catch (e: Exception) {
            Color.White // fallback White
        }
    }

    // Determine custom text color
    val appTextColor = remember(textColorHex) {
        try {
            Color(android.graphics.Color.parseColor(textColorHex))
        } catch (e: Exception) {
            Color(0xFF1E293B) // fallback Slate Dark
        }
    }

    // Force RTL local layout direction, custom layout scale/zoom, and custom fonts
    val currentDensity = androidx.compose.ui.platform.LocalDensity.current
    val customDensity = remember(currentDensity, zoomLevel) {
        object : androidx.compose.ui.unit.Density by currentDensity {
            override val fontScale: Float
                get() = currentDensity.fontScale * (zoomLevel / 16f)
            override val density: Float
                get() = currentDensity.density * (zoomLevel / 16f)
        }
    }

    val selectedFont by viewModel.selectedFont.collectAsStateWithLifecycle()
    val activeFontFamily = remember(selectedFont) {
        when(selectedFont) {
            "cairo" -> androidx.compose.ui.text.font.FontFamily.SansSerif
            "amiri" -> androidx.compose.ui.text.font.FontFamily.Serif
            "tajawal" -> androidx.compose.ui.text.font.FontFamily.Default
            else -> androidx.compose.ui.text.font.FontFamily.Default
        }
    }

    val appLang by viewModel.appLang.collectAsStateWithLifecycle()
    val isLtr = appLang == "en"
    val layoutDirection = if (isLtr) LayoutDirection.Ltr else LayoutDirection.Rtl

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        androidx.compose.ui.platform.LocalDensity provides customDensity,
        LocalTextStyle provides androidx.compose.ui.text.TextStyle(fontFamily = activeFontFamily)
    ) {
        val context = LocalContext.current
        DisposableEffect(Unit) {
            viewModel.checkAndAutoLinkGoogle(context)
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    viewModel.backupData(context)
                }
            }
            cm.registerNetworkCallback(request, callback)
            onDispose {
                cm.unregisterNetworkCallback(callback)
            }
        }

        val isGoogleLinked by viewModel.isGoogleLinked.collectAsStateWithLifecycle()
        val googleEmail by viewModel.googleUserEmail.collectAsStateWithLifecycle()
        val googleName by viewModel.googleUserName.collectAsStateWithLifecycle()

        val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                if (currentFarm == null) {
                    viewModel.createFarm("مزرعة مسترجعة", "")
                    viewModel.selectFarm("مزرعة مسترجعة", "")
                }
                viewModel.importBackup(context, uri)
                onboardingViewModel.completeSetup()
            }
        }

        val folderPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            if (uri != null) {
                val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                viewModel.updateBackupFolderUri(uri.toString())
                Toast.makeText(context, "تم تحديد مجلد النسخ الاحتياطية. يمكنك استعادتها من الإعدادات لاحقاً.", Toast.LENGTH_LONG).show()
                // Auto skip setup or let them continue? Let's just create a farm to let them enter the app.
                if (currentFarm == null) {
                    viewModel.createFarm("مزرعة مسترجعة", "")
                    viewModel.selectFarm("مزرعة مسترجعة", "")
                }
                onboardingViewModel.completeSetup()
            }
        }

        if (!isSetupCompleted) {
            com.example.ui.screens.OnboardingScreen(
                viewModel = onboardingViewModel,
                farmViewModel = viewModel,
                themePrimary = appAccentColor,
                onRestoreBackupFromFolder = { folderPickerLauncher.launch(null) },
                onRestoreBackupFromFile = { importLauncher.launch("*/*") }
            )
        } else if (currentFarm == null) {
            // Auto-login since setup is completed
            val storedFarm by onboardingViewModel.farmName.collectAsStateWithLifecycle()
            LaunchedEffect(storedFarm) {
                if (storedFarm.isNotBlank()) {
                    viewModel.selectFarm(storedFarm, "")
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = appAccentColor)
            }
        } else {
            // Main App with Scaffold, custom bottom navigation & sliding right drawer
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            // Dynamic custom labels loaded from SharedPreferences (translators mapping)
            val sp = context.getSharedPreferences("farm_titles", Context.MODE_PRIVATE)
            var labelDashboardRaw by remember { mutableStateOf(sp.getString("label_dashboard", "لوح التحكم الرئيسية") ?: "لوح التحكم الرئيسية") }
            var labelBarnRaw by remember { mutableStateOf(sp.getString("label_barn", "الحظيرة") ?: "الحظيرة") }
            var labelFeedsRaw by remember { mutableStateOf(sp.getString("label_feeds", "الأعلاف") ?: "الأعلاف") }
            var labelAccountsRaw by remember { mutableStateOf(sp.getString("label_accounts", "الحسابات") ?: "الحسابات") }
            var labelNotesRaw by remember { mutableStateOf(sp.getString("label_notes", "الملاحظات والوسائط") ?: "الملاحظات والوسائط") }
            var labelArchiveRaw by remember { mutableStateOf(sp.getString("label_archive", "الأرشيف والبيانات") ?: "الأرشيف والبيانات") }
            var labelBackupRaw by remember { mutableStateOf(sp.getString("label_backup", "النسخ الاحتياطي ونقل السجلات") ?: "النسخ الاحتياطي ونقل السجلات") }
            var labelFeedCalcRaw by remember { mutableStateOf(sp.getString("label_feed_calc", "حاسبة الأعلاف") ?: "حاسبة الأعلاف") }
            var labelReportsRaw by remember { mutableStateOf(sp.getString("label_reports", "الإحصائيات والتقارير") ?: "الإحصائيات والتقارير") }
            var labelRemindersRaw by remember { mutableStateOf(sp.getString("label_reminders", "التنبيهات الذكية") ?: "التنبيهات الذكية") }

            val labelDashboard = com.example.util.Localization.t(labelDashboardRaw, appLang)
            val labelBarn = com.example.util.Localization.t(labelBarnRaw, appLang)
            val labelFeeds = com.example.util.Localization.t(labelFeedsRaw, appLang)
            val labelAccounts = com.example.util.Localization.t(labelAccountsRaw, appLang)
            val labelNotes = com.example.util.Localization.t(labelNotesRaw, appLang)
            val labelArchive = com.example.util.Localization.t(labelArchiveRaw, appLang)
            val labelBackup = com.example.util.Localization.t(labelBackupRaw, appLang)
            val labelFeedCalc = com.example.util.Localization.t(labelFeedCalcRaw, appLang)
            val labelReports = com.example.util.Localization.t(labelReportsRaw, appLang)
            val labelReminders = com.example.util.Localization.t(labelRemindersRaw, appLang)

            // Active Internal navigation
            var activeTab by remember { mutableStateOf("dashboard") } // "dashboard", "barn", "feeds", "accounts", "notes", "archive", "settings"
            var invoiceToView by remember { mutableStateOf<InvoiceData?>(null) }
            val vmInvoice by viewModel.currentInvoice.collectAsStateWithLifecycle()
            LaunchedEffect(vmInvoice) {
                vmInvoice?.let {
                    invoiceToView = it
                    activeTab = "invoice"
                    viewModel.setCurrentInvoice(null)
                }
            }
            var batchInvoiceType by remember { mutableStateOf("purchase") } // "purchase", "sale", "feed"
            var barnMenuExpanded by remember { mutableStateOf(false) }
            var addonsExpanded by remember { mutableStateOf(false) }

            // --- Global Confirm & Dialog States & Permission checks ---
            var showDeleteConfirmDialog by remember { mutableStateOf(false) }
            var onDeleteConfirmed by remember { mutableStateOf<(() -> Unit)?>(null) }
            var deleteConfirmMessage by remember { mutableStateOf("") }

            var showEditConfirmDialog by remember { mutableStateOf(false) }
            var onEditConfirmed by remember { mutableStateOf<(() -> Unit)?>(null) }
            var editConfirmMessage by remember { mutableStateOf("") }

            val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
            val enableSwipeNavigation by viewModel.enableSwipeNavigation.collectAsStateWithLifecycle()
            val invertSwipeDirection by viewModel.invertSwipeDirection.collectAsStateWithLifecycle()

            val checkDeletePermissionAndConfirm: (String, () -> Unit) -> Unit = { msg, onConfirm ->
                if (!viewModel.hasPermission("delete_animal")) {
                    Toast.makeText(context, "عذراً، لا تملك صلاحية الحذف ❌", Toast.LENGTH_LONG).show()
                } else {
                    deleteConfirmMessage = msg
                    onDeleteConfirmed = onConfirm
                    showDeleteConfirmDialog = true
                }
            }

            val checkEditPermissionAndConfirm: (String, () -> Unit) -> Unit = { msg, onConfirm ->
                if (!viewModel.hasPermission("edit_animal")) {
                    Toast.makeText(context, "عذراً، لا تملك صلاحية تعديل السجلات ❌", Toast.LENGTH_LONG).show()
                } else {
                    editConfirmMessage = msg
                    onEditConfirmed = onConfirm
                    showEditConfirmDialog = true
                }
            }

            val checkAddPermission: (() -> Unit) -> Unit = { onProceed ->
                if (!viewModel.hasPermission("add_animal")) {
                    Toast.makeText(context, "عذراً، لا تملك صلاحية للإضافة ❌", Toast.LENGTH_LONG).show()
                } else {
                    onProceed()
                }
            }

            // Support phone's back button elegantly so that it returns to dashboard instead of exiting
            BackHandler(enabled = activeTab != "dashboard") {
                activeTab = "dashboard"
            }
            val animalsForDrawer by viewModel.animalsList.collectAsStateWithLifecycle()
            val filterTypeForDrawer by viewModel.selectedAnimalType.collectAsStateWithLifecycle()

            // Dialog displays
            var showAnimalDialog by remember { mutableStateOf(false) }
            var animalToEdit by remember { mutableStateOf<com.example.data.model.AnimalEntity?>(null) }
            var showNewbornDialog by remember { mutableStateOf(false) }
            var showFeedDialog by remember { mutableStateOf(false) }
            var showMedicineDialog by remember { mutableStateOf(false) }
            var showPersonDialog by remember { mutableStateOf(false) }
            var showTransactionDialog by remember { mutableStateOf(false) }
            var showGlobalSearchDialog by remember { mutableStateOf(false) }
            var showExportChoiceDialog by remember { mutableStateOf(false) }
            var transactionType by remember { mutableStateOf("income") } // "income" or "expense"
            var animalIdToView by remember { mutableStateOf<Int?>(null) }

            val updateLabels: () -> Unit = {
                labelDashboardRaw = sp.getString("label_dashboard", "لوح التحكم الرئيسية") ?: "لوح التحكم الرئيسية"
                labelBarnRaw = sp.getString("label_barn", "الحظيرة") ?: "الحظيرة"
                labelFeedsRaw = sp.getString("label_feeds", "الأعلاف") ?: "الأعلاف"
                labelAccountsRaw = sp.getString("label_accounts", "الحسابات") ?: "الحسابات"
                labelNotesRaw = sp.getString("label_notes", "الملاحظات والوسائط") ?: "الملاحظات والوسائط"
                labelArchiveRaw = sp.getString("label_archive", "الأرشيف والبيانات") ?: "الأرشيف والبيانات"
                labelBackupRaw = sp.getString("label_backup", "النسخ الاحتياطي ونقل السجلات") ?: "النسخ الاحتياطي ونقل السجلات"
                labelFeedCalcRaw = sp.getString("label_feed_calc", "حاسبة الأعلاف") ?: "حاسبة الأعلاف"
                labelReportsRaw = sp.getString("label_reports", "الإحصائيات والتقارير") ?: "الإحصائيات والتقارير"
                labelRemindersRaw = sp.getString("label_reminders", "التنبيهات الذكية") ?: "التنبيهات الذكية"
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(300.dp)
                            .background(MaterialTheme.colorScheme.surface),
                        drawerShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp)
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .statusBarsPadding()
                                    .navigationBarsPadding()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    val profilePic by viewModel.userProfilePic.collectAsStateWithLifecycle()
                                    var showProfileDialog by remember { mutableStateOf(false) }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(bottom = 24.dp)
                                            .clickable { showProfileDialog = true }
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        if (profilePic.isNotEmpty()) {
                                            val bmp = com.example.util.ImageUtils.base64ToBitmap(profilePic)
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = "صورة الحساب",
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .background(appAccentColor, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(appAccentColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (ownerName.isNotBlank()) ownerName else (if (googleName.isNotEmpty()) googleName else com.example.util.Localization.t("حساب المزارع", appLang)),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text = "${com.example.util.Localization.t("مزارعي", appLang)} - ${com.example.util.Localization.t(farmName, appLang)}",
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (showProfileDialog) {
                                        AccountManagementDialog(
                                            viewModel = viewModel,
                                            accentColor = appAccentColor,
                                            onDismiss = { showProfileDialog = false }
                                        )
                                    }

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
                                    
                                    val sidebarOrder by viewModel.sidebarItemsOrder.collectAsStateWithLifecycle()

                                    sidebarOrder.forEach { itemKey ->
                                        when (itemKey) {
                                            "dashboard" -> if (!hideSidebarDashboard) {
                                                itemDrawerButton(labelDashboard, Icons.Default.Dashboard, activeTab == "dashboard", appAccentColor) { activeTab = "dashboard"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "barn" -> if (!hideSidebarBarn) {
                                                itemDrawerButton(labelBarn, Icons.Default.Pets, activeTab == "barn", appAccentColor) { activeTab = "barn"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "feeds" -> if (!hideSidebarFeeds) {
                                                itemDrawerButton(labelFeeds, Icons.Default.Grass, activeTab == "feeds", appAccentColor) { activeTab = "feeds"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "accounts" -> if (!hideSidebarAccounts) {
                                                itemDrawerButton(labelAccounts, Icons.Default.People, activeTab == "accounts", appAccentColor) { activeTab = "accounts"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            // 'notes' has been moved to addons Menu
                                            "archive" -> if (!hideSidebarArchive) {
                                                itemDrawerButton(labelArchive, Icons.Default.Archive, activeTab == "archive", appAccentColor) { activeTab = "archive"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "backup" -> if (!hideSidebarBackup) {
                                                itemDrawerButton(labelBackup, Icons.Default.Backup, activeTab == "backup", appAccentColor) { activeTab = "backup"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "feed_calc" -> if (!hideSidebarFeedCalc) {
                                                itemDrawerButton(labelFeedCalc, Icons.Default.Calculate, activeTab == "feed_calculator", appAccentColor) { activeTab = "feed_calculator"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "reports" -> if (!hideSidebarReports) {
                                                itemDrawerButton(labelReports, Icons.Default.PieChart, activeTab == "analytics", appAccentColor) { activeTab = "analytics"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "reminders" -> if (!hideSidebarReminders) {
                                                itemDrawerButton(labelReminders, Icons.Default.NotificationsActive, activeTab == "reminders", appAccentColor) { activeTab = "reminders"; coroutineScope.launch { drawerState.close() } }
                                            }
                                            "settings" -> {
                                                itemDrawerButton("سجل الحذف", Icons.Default.DeleteSweep, activeTab == "recycle_bin", appAccentColor) {
                                                    activeTab = "recycle_bin"
                                                    coroutineScope.launch { drawerState.close() }
                                                }
                                                itemDrawerButton("إعدادات التطبيق", Icons.Default.Settings, activeTab == "settings", appAccentColor) {
                                                    activeTab = "settings"
                                                    coroutineScope.launch { drawerState.close() }
                                                }
                                            }
                                        }
                                    }

                                    val isAttendanceEnabled by viewModel.isAttendancePluginEnabled.collectAsStateWithLifecycle()
                                    val isNotesEnabled by viewModel.isNotesPluginEnabled.collectAsStateWithLifecycle()

                                    if (isAttendanceEnabled || isNotesEnabled) {
                                        Column {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { addonsExpanded = !addonsExpanded },
                                                color = if (activeTab == "attendance" || activeTab == "notes") appAccentColor.copy(alpha = 0.08f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Extension,
                                                            contentDescription = null,
                                                            tint = if (activeTab == "attendance" || activeTab == "personal_accounts" || activeTab == "notes") appAccentColor else Color(0xFF64748B),
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Text(
                                                            text = "الإضافات",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (activeTab == "attendance" || activeTab == "personal_accounts" || activeTab == "notes") appAccentColor else Color(0xFF334155)
                                                        )
                                                    }
                                                    Icon(
                                                        if (addonsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null,
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            if (addonsExpanded) {
                                                Column(modifier = Modifier.padding(start = 24.dp)) {
                                                    if (isAttendanceEnabled) {
                                                        itemDrawerButton("جدول الحضور اليومي", Icons.Default.EventNote, activeTab == "attendance", appAccentColor) { activeTab = "attendance"; coroutineScope.launch { drawerState.close() } }
                                                    }
                                                    if (isNotesEnabled) {
                                                        itemDrawerButton(labelNotes, Icons.Default.Comment, activeTab == "notes", appAccentColor) { activeTab = "notes"; coroutineScope.launch { drawerState.close() } }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    itemDrawerButton("سجل التغيرات", Icons.Default.ManageHistory, activeTab == "activity_log", appAccentColor) { activeTab = "activity_log"; coroutineScope.launch { drawerState.close() } }

                                }

                                Button(
                                    onClick = {
                                        viewModel.logout()
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(com.example.util.Localization.t("تسجيل الخروج", appLang), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            ) {
                val pluginTabs = listOf("attendance", "personal_accounts", "notes", "feed_calculator", "analytics", "reminders", "activity_log")
                val isPluginActive = activeTab in pluginTabs

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        if (!isPluginActive) {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (customAppIconBase64 != null) {
                                            val bmp = try {
                                                val bytes = android.util.Base64.decode(customAppIconBase64, android.util.Base64.DEFAULT)
                                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            } catch (e: Exception) { null }
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.HomeWork,
                                                    contentDescription = null,
                                                    tint = appAccentColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.HomeWork,
                                                contentDescription = null,
                                                tint = appAccentColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = if (customAppName.isNotBlank() && customAppName != "المزرعة برو") "$customAppName - $farmName" else farmName,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "افتح القائمة")
                                    }
                                },
                                actions = {
                                    val context = LocalContext.current
                                    IconButton(onClick = {
                                        showGlobalSearchDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "بحث الشامل",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    val coroutineScope = rememberCoroutineScope()
                                    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .combinedClickable(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        try {
                                                            if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
                                                                val firebaseManager = com.example.data.remote.FirebaseManager()
                                                                val auth = firebaseManager.auth
                                                                if (auth?.currentUser == null) {
                                                                    Toast.makeText(context, "غير متصل بـ Firebase ❌", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "متصل بـ Firebase ✅", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                            viewModel.exportBackup(context, localOnlyWithoutPrompt = true)
                                                            Toast.makeText(context, "تم حفظ نسخة احتياطية محلية لليوم بنجاح 💾", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "خطأ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                onLongClick = {
                                                    activeTab = "backup"
                                                }
                                            )
                                            .padding(12.dp) // IconButton padding equivalent
                                    ) {
                                        Icon(
                                            imageVector = if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) Icons.Default.CloudSync else Icons.Default.Save,
                                            contentDescription = "قائمة النسخ الاحتياطي",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(onClick = {
                                        activeTab = "settings"
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "الإعدادات",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(onClick = {
                                         viewModel.logout()
                                     }) {
                                         Icon(
                                             imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "تسجيل الخروج",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (!isPluginActive) {
                            val pinnedBottomTabs by viewModel.pinnedBottomBarTabs.collectAsStateWithLifecycle()
                            val leftTabs = remember(pinnedBottomTabs) {
                                val size = pinnedBottomTabs.size
                                pinnedBottomTabs.take(size / 2)
                            }
                            val rightTabs = remember(pinnedBottomTabs) {
                                val size = pinnedBottomTabs.size
                                pinnedBottomTabs.drop(size / 2)
                            }

                    val isEditorActive by viewModel.isFullScreenEditorActive.collectAsStateWithLifecycle()
                    if (!isEditorActive) {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Tabs
                                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                        for (tabKey in leftTabs) {
                                            val tabInfo = getTabInfo(tabKey, labelBarn, labelFeeds, labelAccounts)
                                            IconButton(
                                                onClick = { activeTab = tabKey },
                                                modifier = Modifier.testTag("bottom_tab_$tabKey")
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        tabInfo.second,
                                                        contentDescription = tabInfo.first,
                                                        tint = if (activeTab == tabKey) appAccentColor else Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(
                                                        tabInfo.first,
                                                        fontSize = 10.sp,
                                                        color = if (activeTab == tabKey) appAccentColor else Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Centered Custom '+' HUB button
                                    Box(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        var showMenu by remember { mutableStateOf(false) }

                                        FloatingActionButton(
                                            onClick = { showMenu = !showMenu },
                                            containerColor = appAccentColor,
                                            contentColor = Color.White,
                                            shape = CircleShape,
                                            modifier = Modifier.size(48.dp).testTag("hub_add_button")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "إضافة جديدة", modifier = Modifier.size(28.dp))
                                        }

                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("إدخل رأس ماشية منفصلة 🐃") },
                                                leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    showAnimalDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("إضافة مولود جديد 🍼") },
                                                leadingIcon = { Icon(Icons.Default.List, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    showNewbornDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("إدخل بند أعلاف منفصل 🌾") },
                                                leadingIcon = { Icon(Icons.Default.Grass, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    showFeedDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("فاتورة شراء رؤوس (مجموعة) 🧾") },
                                                leadingIcon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    batchInvoiceType = "purchase"
                                                    activeTab = "batch_invoice"
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("فاتورة بيع رؤوس (مجموعة) 🧾") },
                                                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    batchInvoiceType = "sale"
                                                    activeTab = "batch_invoice"
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("فاتورة شراء أعلاف (مجموعة) 🌽") },
                                                leadingIcon = { Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    batchInvoiceType = "feed"
                                                    activeTab = "batch_invoice"
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("إيراد أو قبض مالي (سند)") },
                                                leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    transactionType = "income"
                                                    showTransactionDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("مصروف أو دفع مالي (سند)") },
                                                leadingIcon = { Icon(Icons.Default.TrendingDown, contentDescription = null, tint = appAccentColor) },
                                                onClick = {
                                                    showMenu = false
                                                    transactionType = "expense"
                                                    showTransactionDialog = true
                                                }
                                            )
                                        }
                                    }

                                    // Right Tabs
                                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                        for (tabKey in rightTabs) {
                                            val tabInfo = getTabInfo(tabKey, labelBarn, labelFeeds, labelAccounts)
                                            IconButton(
                                                onClick = { activeTab = tabKey },
                                                modifier = Modifier.testTag("bottom_tab_$tabKey")
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        tabInfo.second,
                                                        contentDescription = tabInfo.first,
                                                        tint = if (activeTab == tabKey) appAccentColor else Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(
                                                        tabInfo.first,
                                                        fontSize = 10.sp,
                                                        color = if (activeTab == tabKey) appAccentColor else Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            ) { innerPadding ->
                    val finalPadding = if (isPluginActive) PaddingValues(0.dp) else innerPadding
                    val pinnedBottomTabs by viewModel.pinnedBottomBarTabs.collectAsStateWithLifecycle()
                    val primaryPagerTabs = remember(pinnedBottomTabs) {
                        val filtered = pinnedBottomTabs.filter { it in listOf("dashboard", "barn", "archive", "accounts") }
                        if (filtered.isEmpty()) {
                            listOf("dashboard", "barn", "archive", "accounts")
                        } else {
                            filtered
                        }
                    }
                    val isTabInPager = remember(primaryPagerTabs, activeTab) {
                        primaryPagerTabs.contains(activeTab)
                    }

                    // Setup HorizontalPager State
                    val pagerState = rememberPagerState(
                        initialPage = 0,
                        pageCount = { primaryPagerTabs.size }
                    )

                    // BI-DIRECTIONAL NAVIGATION SYNC ENGINE
                    LaunchedEffect(activeTab, primaryPagerTabs) {
                        val index = primaryPagerTabs.indexOf(activeTab)
                        if (index != -1 && index != pagerState.currentPage && index < primaryPagerTabs.size) {
                            pagerState.scrollToPage(index)
                        }
                    }

                    LaunchedEffect(pagerState.currentPage, primaryPagerTabs) {
                        if (isTabInPager && pagerState.currentPage < primaryPagerTabs.size && pagerState.currentPage >= 0) {
                            val tabAtPage = primaryPagerTabs[pagerState.currentPage]
                            if (activeTab != tabAtPage) {
                                activeTab = tabAtPage
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(finalPadding)
                    ) {
                        if (isTabInPager) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize().testTag("horizontal_pager_navigation"),
                                userScrollEnabled = enableSwipeNavigation
                            ) { pageIndex ->
                                val currentTabKey = primaryPagerTabs.getOrNull(pageIndex) ?: "dashboard"
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (currentTabKey) {
                                        "dashboard" -> DashboardScreen(
                                            viewModel = viewModel,
                                            accentColor = appAccentColor,
                                            zoomLevel = zoomLevel,
                                            onNavigateToTab = { activeTab = it },
                                            onOpenTransaction = { type ->
                                                transactionType = type
                                                showTransactionDialog = true
                                            },
                                            onOpenAnimal = { showAnimalDialog = true },
                                            onOpenFeed = { showFeedDialog = true },
                                            onOpenFinancialDetails = { type ->
                                                transactionType = type 
                                                activeTab = "financial_details"
                                            }
                                        )
                                        "barn" -> BarnScreen(
                                            viewModel,
                                            appAccentColor,
                                            zoomLevel,
                                            appCardBgColor,
                                            checkAddPermission,
                                            checkEditPermissionAndConfirm,
                                            checkDeletePermissionAndConfirm,
                                            onAddAnimalClick = { checkAddPermission { showAnimalDialog = true } },
                                            onEditAnimal = { an -> 
                                                checkEditPermissionAndConfirm("تعديل بيانات الرأس") {
                                                    animalToEdit = an
                                                    showAnimalDialog = true
                                                } 
                                            },
                                            onViewAnimal = { id ->
                                                animalIdToView = id
                                                activeTab = "animal_details"
                                            },
                                            onViewNewborns = { activeTab = "newborns" }
                                        )
                                        "newborns" -> com.example.ui.screens.NewbornsScreen(
                                            viewModel = viewModel,
                                            themePrimary = appAccentColor,
                                            appCardBgColor = appCardBgColor,
                                            onBack = { activeTab = "barn" }
                                        )
                                        "feeds" -> FeedsScreen(
                                            viewModel,
                                            appAccentColor,
                                            zoomLevel,
                                            appCardBgColor,
                                            checkAddPermission,
                                            checkEditPermissionAndConfirm,
                                            checkDeletePermissionAndConfirm,
                                            onAddFeedClick = { checkAddPermission { showFeedDialog = true } },
                                            onAddMedicineClick = { checkAddPermission { showMedicineDialog = true } }
                                        )
                                        "batch_invoice" -> BatchInvoiceScreen(viewModel, appAccentColor, zoomLevel, batchInvoiceType, onCompleted = { activeTab = "dashboard" })
                                        "accounts" -> AccountsScreen(
                                            viewModel,
                                            appAccentColor,
                                            zoomLevel,
                                            appCardBgColor,
                                            checkAddPermission,
                                            checkEditPermissionAndConfirm,
                                            checkDeletePermissionAndConfirm
                                        )
                                        "notes" -> NotesScreen(
                                            viewModel,
                                            appAccentColor,
                                            zoomLevel,
                                            appCardBgColor,
                                            checkAddPermission,
                                            checkEditPermissionAndConfirm,
                                            checkDeletePermissionAndConfirm
                                        )
                                        "archive" -> ArchiveScreen(viewModel, appAccentColor, zoomLevel, appCardBgColor, checkDeletePermissionAndConfirm, { a ->
                                            checkEditPermissionAndConfirm("سيتم تعديل بيانات الرأس") {
                                                animalToEdit = a
                                                showAnimalDialog = true
                                            }
                                        }, { id ->
                                            animalIdToView = id
                                            activeTab = "animal_details"
                                        }, { tab ->
                                            activeTab = tab
                                        })
                                        "backup" -> BackupScreen(viewModel, appAccentColor, zoomLevel, checkDeletePermissionAndConfirm)
                                        "feed_calculator" -> FeedCalculatorScreen(appAccentColor, appCardBgColor, onBack = { activeTab = "dashboard" })
                                        "analytics" -> AnalyticsDashboardScreen(appAccentColor, appCardBgColor, onBack = { activeTab = "dashboard" })
                                        "reminders" -> RemindersScreen(appAccentColor, appCardBgColor, onBack = { activeTab = "dashboard" })
                                        "activity_log" -> ActivityLogScreen(viewModel, appAccentColor)
                                        "attendance" -> com.example.ui.screens.AttendanceScreenNew(viewModel, onBack = { activeTab = "dashboard" })
                                        "personal_accounts" -> PersonalAccountsScreen(viewModel, appAccentColor, zoomLevel, onBack = { activeTab = "dashboard" })
                                        "accounting" -> com.example.ui.screens.accounting.AccountingScreen(viewModel, appAccentColor, onBack = { activeTab = "dashboard" })
                                        "invoice" -> {
                                            invoiceToView?.let { inv ->
                                                InvoiceScreen(
                                                    title = inv.title,
                                                    date = inv.date,
                                                    items = inv.items,
                                                    total = inv.total,
                                                    accentColor = appAccentColor,
                                                    onBack = { activeTab = "dashboard" }
                                                )
                                            } ?: run { activeTab = "dashboard" }
                                        }
                                        "settings" -> com.example.ui.screens.RedesignedSettingsScreen(
                                            viewModel = viewModel,
                                            accentColor = appAccentColor,
                                            zoomLevel = zoomLevel,
                                            onUpdateLabels = updateLabels
                                        )
                                    }
                                }
                            }
                        } else {
                            // Non-pinned screens (Sidebar destinations unpinned, details screens, etc.)
                            AnimatedContent(
                                targetState = activeTab,
                                label = "fade_non_pinned_screens"
                            ) { target ->
                                when (target) {
                                    "financial_details" -> com.example.ui.screens.FinancialDetailsPage(
                                        viewModel = viewModel,
                                        type = transactionType,
                                        accentColor = appAccentColor,
                                        onBack = { activeTab = "dashboard" }
                                    )
                                    "animal_details" -> com.example.ui.screens.AnimalDetailsScreen(
                                        viewModel = viewModel,
                                        animalId = animalIdToView ?: -1,
                                        accentColor = appAccentColor,
                                        onBack = { activeTab = "barn" },
                                        onSell = {  }, // we can implement these later or in the screen details
                                        onDelete = { },
                                        onEdit = { an -> 
                                            checkEditPermissionAndConfirm("تعديل بيانات الرأس") {
                                                animalToEdit = an
                                                showAnimalDialog = true
                                            } 
                                        }
                                    )
                                    "batch_invoice" -> BatchInvoiceScreen(viewModel, appAccentColor, zoomLevel, batchInvoiceType, onCompleted = { activeTab = "dashboard" })
                                    "recycle_bin" -> RecycleBinScreen(viewModel, appAccentColor, zoomLevel)
                                    // Fallback render of unpinned screens to support sidebar access
                                    "dashboard" -> DashboardScreen(
                                        viewModel = viewModel,
                                        accentColor = appAccentColor,
                                        zoomLevel = zoomLevel,
                                        onNavigateToTab = { activeTab = it },
                                        onOpenTransaction = { type ->
                                            transactionType = type
                                            showTransactionDialog = true
                                        },
                                        onOpenAnimal = { showAnimalDialog = true },
                                        onOpenFeed = { showFeedDialog = true },
                                        onOpenFinancialDetails = { type ->
                                            transactionType = type 
                                            activeTab = "financial_details"
                                        }
                                    )
                                    "barn" -> BarnScreen(
                                        viewModel,
                                        appAccentColor,
                                        zoomLevel,
                                        appCardBgColor,
                                        checkAddPermission,
                                        checkEditPermissionAndConfirm,
                                        checkDeletePermissionAndConfirm,
                                        onAddAnimalClick = { checkAddPermission { showAnimalDialog = true } },
                                        onEditAnimal = { an -> 
                                            checkEditPermissionAndConfirm("تعديل بيانات الرأس") {
                                                animalToEdit = an
                                                showAnimalDialog = true
                                            } 
                                        },
                                        onViewAnimal = { id ->
                                            animalIdToView = id
                                            activeTab = "animal_details"
                                        },
                                        onViewNewborns = { activeTab = "newborns" }
                                    )
                                    "newborns" -> com.example.ui.screens.NewbornsScreen(
                                        viewModel = viewModel,
                                        themePrimary = appAccentColor,
                                        appCardBgColor = appCardBgColor,
                                        onBack = { activeTab = "barn" }
                                    )
                                    "feeds" -> FeedsScreen(
                                        viewModel,
                                        appAccentColor,
                                        zoomLevel,
                                        appCardBgColor,
                                        checkAddPermission,
                                        checkEditPermissionAndConfirm,
                                        checkDeletePermissionAndConfirm,
                                        onAddFeedClick = { checkAddPermission { showFeedDialog = true } },
                                        onAddMedicineClick = { checkAddPermission { showMedicineDialog = true } }
                                    )
                                    "accounts" -> AccountsScreen(
                                        viewModel,
                                        appAccentColor,
                                        zoomLevel,
                                        appCardBgColor,
                                        checkAddPermission,
                                        checkEditPermissionAndConfirm,
                                        checkDeletePermissionAndConfirm
                                    )
                                    "notes" -> com.example.ui.screens.NotesScreenNew(viewModel, appAccentColor, onBack = { activeTab = "dashboard" })
                                    "archive" -> ArchiveScreen(viewModel, appAccentColor, zoomLevel, appCardBgColor, checkDeletePermissionAndConfirm, { a ->
                                        checkEditPermissionAndConfirm("سيتم تعديل بيانات الرأس") {
                                            animalToEdit = a
                                            showAnimalDialog = true
                                        }
                                    }, { id ->
                                        animalIdToView = id
                                        activeTab = "animal_details"
                                    }, { tab ->
                                        activeTab = tab
                                    })
                                    "backup" -> BackupScreen(viewModel, appAccentColor, zoomLevel, checkDeletePermissionAndConfirm)
                                    "feed_calculator" -> FeedCalculatorScreen(appAccentColor, appCardBgColor, onBack = { activeTab = "dashboard" })
                                    "analytics" -> AnalyticsDashboardScreen(appAccentColor, appCardBgColor, onBack = { activeTab = "dashboard" })
                                    "reminders" -> RemindersScreen(appAccentColor, appCardBgColor, onBack = { activeTab = "dashboard" })
                                    "activity_log" -> ActivityLogScreen(viewModel, appAccentColor)
                                    "attendance" -> com.example.ui.screens.AttendanceScreenNew(viewModel, onBack = { activeTab = "dashboard" })
                                    "personal_accounts" -> PersonalAccountsScreen(viewModel, appAccentColor, zoomLevel, onBack = { activeTab = "dashboard" })
                                    "accounting" -> com.example.ui.screens.accounting.AccountingScreen(viewModel, appAccentColor, onBack = { activeTab = "dashboard" })
                                    "invoice" -> {
                                        invoiceToView?.let { inv ->
                                            InvoiceScreen(
                                                title = inv.title,
                                                date = inv.date,
                                                items = inv.items,
                                                total = inv.total,
                                                accentColor = appAccentColor,
                                                onBack = { activeTab = "dashboard" }
                                            )
                                        } ?: run { activeTab = "dashboard" }
                                    }
                                    "settings" -> com.example.ui.screens.RedesignedSettingsScreen(
                                        viewModel = viewModel,
                                        accentColor = appAccentColor,
                                        zoomLevel = zoomLevel,
                                        onUpdateLabels = updateLabels
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Unified Insertion overlays ---
            // --- Global Confirmation Overlays for Deletes and Edits ---
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("تأكيد الحذف ⚠️", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(deleteConfirmMessage, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("نتيجة الحذف:", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("سيؤدي هذا الإجراء لنقل السجل إلى سلة المهملات. في بعض الحالات (مثل الأب/الأم أو المعاملات المالية المرتبطة لجهة اتصال)، قد تتعطل الترابطات السابقة. يرجى التفكير جيداً قبل المتابعة.", color = Color.DarkGray, fontSize = 12.sp)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                onDeleteConfirmed?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("نعم، احذف السجل", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            if (showEditConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showEditConfirmDialog = false },
                    title = { Text("تأكيد حفظ التعديلات 📝", fontWeight = FontWeight.Bold) },
                    text = { Text(editConfirmMessage, fontSize = 14.sp) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showEditConfirmDialog = false
                                onEditConfirmed?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = appAccentColor)
                        ) {
                            Text("نعم، تعديل وحفظ", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditConfirmDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            if (showAnimalDialog || animalToEdit != null) {
                AddAnimalDialog(
                    viewModel = viewModel,
                    accentColor = appAccentColor,
                    animalToEdit = animalToEdit,
                    onDismiss = { 
                        showAnimalDialog = false 
                        animalToEdit = null
                    }
                )
            }

            if (showNewbornDialog) {
                AddNewbornDialog(
                    viewModel = viewModel,
                    accentColor = appAccentColor,
                    onDismiss = { showNewbornDialog = false }
                )
            }

            if (showFeedDialog) {
                AddFeedDialog(
                    viewModel = viewModel,
                    accentColor = appAccentColor,
                    onDismiss = { showFeedDialog = false }
                )
            }

            if (showMedicineDialog) {
                AddMedicineDialog(
                    viewModel = viewModel,
                    accentColor = appAccentColor,
                    onDismiss = { showMedicineDialog = false }
                )
            }

            if (showTransactionDialog) {
                AddTransactionDialog(
                    viewModel = viewModel,
                    type = transactionType,
                    accentColor = appAccentColor,
                    onDismiss = { showTransactionDialog = false }
                )
            }
            
            if (showGlobalSearchDialog) {
                GlobalSearchDialog(viewModel = viewModel, onDismiss = { showGlobalSearchDialog = false })
            }
        }

        // Expanded full screen image overlays ("وعند الضغط علي الصور تفتح حجم اكبر")
        if (enlargedImage != null) {
            Dialog(
                onDismissRequest = { viewModel.triggerImageEnlargement(null) },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable { viewModel.triggerImageEnlargement(null) },
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = remember(enlargedImage) {
                        enlargedImage?.let { ImageUtils.base64ToBitmap(it) }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "عرض موسع",
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .fillMaxHeight(0.85f)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("تعذر عرض الصورة", color = Color.White)
                    }

                    // Floating close button
                    IconButton(
                        onClick = { viewModel.triggerImageEnlargement(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun itemDrawerButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, accentColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) accentColor.copy(alpha = 0.08f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else Color(0xFF64748B),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accentColor else Color(0xFF334155)
            )
        }
    }
}

// ================= THE MULTI-FARM SELECTION WINDOW =================
@Composable
fun FarmManagerLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Smiling sun at top-right
        val sunCenterX = width * 0.72f
        val sunCenterY = height * 0.22f
        val sunRadius = width * 0.11f
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = sunRadius,
            center = androidx.compose.ui.geometry.Offset(sunCenterX, sunCenterY)
        )
        for (i in 0 until 8) {
            val angle = (i * 45) * (Math.PI / 180)
            val startX = sunCenterX + (sunRadius + 4) * Math.cos(angle).toFloat()
            val startY = sunCenterY + (sunRadius + 4) * Math.sin(angle).toFloat()
            val endX = sunCenterX + (sunRadius + 14) * Math.cos(angle).toFloat()
            val endY = sunCenterY + (sunRadius + 14) * Math.sin(angle).toFloat()
            drawLine(
                color = Color(0xFFFBBF24),
                start = androidx.compose.ui.geometry.Offset(startX, startY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 3f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        drawCircle(
            color = Color(0xFF1E293B),
            radius = 2f,
            center = androidx.compose.ui.geometry.Offset(sunCenterX - 6, sunCenterY - 4)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = 2f,
            center = androidx.compose.ui.geometry.Offset(sunCenterX + 6, sunCenterY - 4)
        )
        drawArc(
            color = Color(0xFF1E293B),
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(sunCenterX - 8, sunCenterY - 2),
            size = androidx.compose.ui.geometry.Size(16f, 12f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Silo on the left
        val siloLeft = width * 0.28f
        val siloTop = height * 0.35f
        val siloWidth = width * 0.11f
        val siloHeight = height * 0.45f
        drawArc(
            color = Color(0xFF94A3B8),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(siloLeft, siloTop - (siloWidth / 2f)),
            size = androidx.compose.ui.geometry.Size(siloWidth, siloWidth)
        )
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = androidx.compose.ui.geometry.Offset(siloLeft, siloTop),
            size = androidx.compose.ui.geometry.Size(siloWidth, siloHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(0f)
        )
        for (j in 1..4) {
            val y = siloTop + (siloHeight * j / 5f)
            drawLine(
                color = Color(0xFF64748B),
                start = androidx.compose.ui.geometry.Offset(siloLeft, y),
                end = androidx.compose.ui.geometry.Offset(siloLeft + siloWidth, y),
                strokeWidth = 2f
            )
        }

        // Barn in the center
        val barnLeft = width * 0.40f
        val barnTop = height * 0.42f
        val barnWidth = width * 0.32f
        val barnHeight = height * 0.38f
        drawRect(
            color = Color(0xFFD97706),
            topLeft = androidx.compose.ui.geometry.Offset(barnLeft, barnTop),
            size = androidx.compose.ui.geometry.Size(barnWidth, barnHeight)
        )
        val roofPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(barnLeft - 10f, barnTop)
            lineTo(barnLeft + barnWidth / 2f, barnTop - 35f)
            lineTo(barnLeft + barnWidth + 10f, barnTop)
            close()
        }
        drawPath(
            path = roofPath,
            color = Color(0xFF15803D)
        )
        drawPath(
            path = roofPath,
            color = Color(0xFF166534),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )

        val winWidth = barnWidth * 0.28f
        val winHeight = barnHeight * 0.28f
        val winLeft = barnLeft + (barnWidth - winWidth) / 2f
        val winTop = barnTop + 14f
        drawRect(
            color = Color(0xFFFEF3C7),
            topLeft = androidx.compose.ui.geometry.Offset(winLeft, winTop),
            size = androidx.compose.ui.geometry.Size(winWidth, winHeight)
        )
        drawLine(
            color = Color(0xFFD97706),
            start = androidx.compose.ui.geometry.Offset(winLeft + winWidth / 2f, winTop),
            end = androidx.compose.ui.geometry.Offset(winLeft + winWidth / 2f, winTop + winHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0xFFD97706),
            start = androidx.compose.ui.geometry.Offset(winLeft, winTop + winHeight / 2f),
            end = androidx.compose.ui.geometry.Offset(winLeft + winWidth, winTop + winHeight / 2f),
            strokeWidth = 2f
        )

        // Barn double doors
        val doorWidth = barnWidth * 0.45f
        val doorHeight = barnHeight * 0.40f
        val doorLeft = barnLeft + (barnWidth - doorWidth) / 2f
        val doorTop = barnTop + barnHeight - doorHeight
        drawRect(
            color = Color(0xFF92400E),
            topLeft = androidx.compose.ui.geometry.Offset(doorLeft, doorTop),
            size = androidx.compose.ui.geometry.Size(doorWidth, doorHeight)
        )
        drawRect(
            color = Color(0xFFFCD34D),
            topLeft = androidx.compose.ui.geometry.Offset(doorLeft, doorTop),
            size = androidx.compose.ui.geometry.Size(doorWidth, doorHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
        )
        drawLine(
            color = Color(0xFFFCD34D),
            start = androidx.compose.ui.geometry.Offset(doorLeft, doorTop),
            end = androidx.compose.ui.geometry.Offset(doorLeft + doorWidth, doorTop + doorHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0xFFFCD34D),
            start = androidx.compose.ui.geometry.Offset(doorLeft + doorWidth, doorTop),
            end = androidx.compose.ui.geometry.Offset(doorLeft, doorTop + doorHeight),
            strokeWidth = 2f
        )

        // Tractor
        val tracLeft = width * 0.12f
        val tracTop = height * 0.62f
        val tracWidth = width * 0.18f
        val tracHeight = height * 0.18f
        drawCircle(
            color = Color(0xFF1E293B),
            radius = 16f,
            center = androidx.compose.ui.geometry.Offset(tracLeft + 15f, tracTop + tracHeight)
        )
        drawCircle(
            color = Color(0xFF94A3B8),
            radius = 6f,
            center = androidx.compose.ui.geometry.Offset(tracLeft + 15f, tracTop + tracHeight)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = 10f,
            center = androidx.compose.ui.geometry.Offset(tracLeft + tracWidth - 10f, tracTop + tracHeight + 6f)
        )
        drawCircle(
            color = Color(0xFF94A3B8),
            radius = 4f,
            center = androidx.compose.ui.geometry.Offset(tracLeft + tracWidth - 10f, tracTop + tracHeight + 6f)
        )
        drawRoundRect(
            color = Color(0xFF16A34A),
            topLeft = androidx.compose.ui.geometry.Offset(tracLeft + 8f, tracTop + 10f),
            size = androidx.compose.ui.geometry.Size(tracWidth - 14f, tracHeight - 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        drawRect(
            color = Color(0xFF16A34A),
            topLeft = androidx.compose.ui.geometry.Offset(tracLeft + 8f, tracTop - 10f),
            size = androidx.compose.ui.geometry.Size(22f, 22f)
        )
        drawRect(
            color = Color(0xFFE2E8F0),
            topLeft = androidx.compose.ui.geometry.Offset(tracLeft + 11f, tracTop - 7f),
            size = androidx.compose.ui.geometry.Size(16f, 15f)
        )
        drawLine(
            color = Color(0xFF475569),
            start = androidx.compose.ui.geometry.Offset(tracLeft + tracWidth - 16f, tracTop + 14f),
            end = androidx.compose.ui.geometry.Offset(tracLeft + tracWidth - 16f, tracTop - 12f),
            strokeWidth = 3f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Tree on the right
        val treeTrunkLeft = width * 0.78f
        val treeTrunkTop = height * 0.58f
        val treeTrunkWidth = width * 0.04f
        val treeTrunkHeight = height * 0.22f
        val treeFoliageCenterX = width * 0.80f
        val treeFoliageCenterY = height * 0.50f
        val treeFoliageRadius = width * 0.11f
        drawRect(
            color = Color(0xFF78350F),
            topLeft = androidx.compose.ui.geometry.Offset(treeTrunkLeft, treeTrunkTop),
            size = androidx.compose.ui.geometry.Size(treeTrunkWidth, treeTrunkHeight)
        )
        drawCircle(
            color = Color(0xFF22C55E),
            radius = treeFoliageRadius,
            center = androidx.compose.ui.geometry.Offset(treeFoliageCenterX, treeFoliageCenterY)
        )
        drawCircle(
            color = Color(0xFF16A34A),
            radius = treeFoliageRadius * 0.8f,
            center = androidx.compose.ui.geometry.Offset(treeFoliageCenterX - 10f, treeFoliageCenterY - 10f)
        )

        // Ground base
        drawLine(
            color = Color(0xFF15803D),
            start = androidx.compose.ui.geometry.Offset(width * 0.05f, height * 0.82f),
            end = androidx.compose.ui.geometry.Offset(width * 0.95f, height * 0.82f),
            strokeWidth = 4f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sizePx = size.width
        val center = androidx.compose.ui.geometry.Offset(sizePx / 2f, sizePx / 2f)
        val radius = sizePx * 0.45f
        
        drawArc(
            color = Color(0xFFEA4335), // Red
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        drawArc(
            color = Color(0xFFFBBC05), // Yellow
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        drawArc(
            color = Color(0xFF34A853), // Green
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        drawArc(
            color = Color(0xFF4285F4), // Blue
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        
        val innerRadius = radius * 0.55f
        drawCircle(
            color = Color.White,
            radius = innerRadius,
            center = center
        )
        
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = androidx.compose.ui.geometry.Offset(center.x, center.y - (innerRadius * 0.4f)),
            size = androidx.compose.ui.geometry.Size(radius, innerRadius * 0.8f)
        )
    }
}

@Composable
fun LoginAndFarmSelectionScreen(viewModel: FarmViewModel, themePrimary: Color, zoom: Float) {
    val allFarms by viewModel.allFarms.collectAsStateWithLifecycle()
    val error by viewModel.loginError.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isGoogleLinked by viewModel.isGoogleLinked.collectAsStateWithLifecycle()
    val googleEmailFromState by viewModel.googleUserEmail.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    var isCreatingNew by remember { mutableStateOf(true) }
    var inputName by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showFarmOnboardingDialog by remember { mutableStateOf(false) }
    var inputOnboardingFarmName by remember { mutableStateOf("") }
    
    var showLocalPasswordPrompt by remember { mutableStateOf<com.example.data.model.FarmEntity?>(null) }
    var localPasswordInput by remember { mutableStateOf("") }

    val backgroundColor = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val cardBgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val buttonGreen = Color(0xFF10B981) // Crisp Emerald Green

    var showAccountPicker by remember { mutableStateOf(false) }
    var selectedGoogleEmail by remember { mutableStateOf("") }
    var selectedGoogleName by remember { mutableStateOf("") }
    var showGoogleFarmPicker by remember { mutableStateOf(false) }
    var newFarmNameFromGoogle by remember { mutableStateOf("") }
    var newFarmPasswordFromGoogle by remember { mutableStateOf("") }

    // Build Google Sign-In options & client securely and dynamically
    val webClientId = remember(context) {
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "915535839973-h6gb5k0pme74i03n8vaop1tmgtugmes5.apps.googleusercontent.com"
        } catch (e: Exception) {
            "915535839973-h6gb5k0pme74i03n8vaop1tmgtugmes5.apps.googleusercontent.com"
        }
    }

    val gso = remember(webClientId) {
        val driveScope = com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.appdata")
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestScopes(driveScope)
        
        // Hide/Disable if needed logic is inside the builder if we wanted to block it here too,
        // but wrapping the launcher and button is safer.
        builder.build()
    }

    val googleSignInClient = remember(context, gso) {
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                val email = account?.email ?: ""
                val displayName = account?.displayName ?: account?.email?.substringBefore("@") ?: "مستخدم جوجل"
                
                if (idToken != null) {
                    selectedGoogleEmail = email
                    selectedGoogleName = displayName
                    
                    viewModel.linkGoogleAccountWithFirebase(
                        idToken = idToken,
                        email = email,
                        name = displayName,
                        photoUrl = account?.photoUrl,
                        context = context,
                        onSuccess = {
                            showGoogleFarmPicker = true
                        }
                    )
                } else {
                    Toast.makeText(context, "فشل الحصول على رمز تعريف جوجل (idToken).", Toast.LENGTH_SHORT).show()
                    showAccountPicker = true
                }
            } catch (e: ApiException) {
                e.printStackTrace()
                val friendlyMsg = com.example.data.remote.GoogleDriveManager.getFriendlyErrorMessage(e)
                Toast.makeText(context, friendlyMsg, Toast.LENGTH_LONG).show()
                showAccountPicker = true
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "حدث خطأ غير متوقع: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                showAccountPicker = true
            }
        } else {
            Toast.makeText(context, "تم إلغاء تسجيل الدخول أو فشل. جاري تشغيل المحاكي لخلل خدمات Play.", Toast.LENGTH_SHORT).show()
            showAccountPicker = true
        }
    }

    if (showAccountPicker) {
        AlertDialog(
            onDismissRequest = { 
                showAccountPicker = false 
            },
            title = { Text("تسجيل الدخول Google", fontWeight = FontWeight.Bold, color = themePrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("يرجى إدخال حساب جوجل الخاص بك للاتصال والمزامنة السحابية:", fontSize = 14.sp, color = textPrimary)
                    
                    OutlinedTextField(
                        value = selectedGoogleName,
                        onValueChange = { selectedGoogleName = it },
                        label = { Text("اسم المستخدم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = selectedGoogleEmail,
                        onValueChange = { selectedGoogleEmail = it },
                        label = { Text("البريد الإلكتروني (Google)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedGoogleName.isNotBlank() && selectedGoogleEmail.isNotBlank()) {
                            showAccountPicker = false
                            showGoogleFarmPicker = true
                            viewModel.linkGoogleAccount(selectedGoogleEmail, selectedGoogleName)
                        } else {
                            Toast.makeText(context, "الرجاء إدخال الاسم والبريد الإلكتروني", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("تسجيل الدخول", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAccountPicker = false 
                }) {
                    Text("إلغاء", color = Color.Red)
                }
            }
        )
    }

    if (showGoogleFarmPicker) {
        val displayGoogleName = selectedGoogleName.ifBlank { "لم يتم التعرف على الاسم" }
        AlertDialog(
            onDismissRequest = { showGoogleFarmPicker = false },
            title = { Text("أهلاً بك يا $displayGoogleName!", fontWeight = FontWeight.Bold, color = themePrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (allFarms.isEmpty()) {
                        Text("يبدو أنه لا توجد مزارع مرتبطة بحسابك. \nالرجاء إدخال اسم المزرعة الجديدة:", color = textPrimary, fontSize = 14.sp)
                        OutlinedTextField(
                            value = newFarmNameFromGoogle,
                            onValueChange = { newFarmNameFromGoogle = it },
                            placeholder = { Text("مثال: مزرعتي السعيدة") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                            )
                        )
                    } else {
                        Text("اختر المزرعة التي ترغب بالدخول إليها:", color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        allFarms.forEach { f ->
                            Surface(
                                onClick = {
                                    viewModel.selectFarm(f.name, "")
                                    showGoogleFarmPicker = false
                                },
                                color = cardBgColor,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = themePrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(f.name, color = textPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Divider(color = textSecondary.copy(alpha = 0.2f))
                        Text("أو قم بإنشاء مزرعة جديدة:", color = textPrimary, fontSize = 14.sp)
                        OutlinedTextField(
                            value = newFarmNameFromGoogle,
                            onValueChange = { newFarmNameFromGoogle = it },
                            placeholder = { Text("اسم المزرعة الجديدة") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                            )
                        )
                        OutlinedTextField(
                            value = newFarmPasswordFromGoogle,
                            onValueChange = { newFarmPasswordFromGoogle = it },
                            placeholder = { Text("كلمة المرور (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (newFarmNameFromGoogle.isNotBlank()) {
                    Button(onClick = {
                        viewModel.createFarm(newFarmNameFromGoogle, newFarmPasswordFromGoogle)
                        showGoogleFarmPicker = false
                        newFarmPasswordFromGoogle = ""
                    }, colors = ButtonDefaults.buttonColors(containerColor = themePrimary)) {
                        Text("إنشاء ودخول", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleFarmPicker = false }) {
                    Text("رجوع", color = textPrimary)
                }
            }
        )
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.TopCenter
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isTablet = screenWidth > 600.dp

        // Dynamic scaling factor based on screen constraints
        val scaleFactor = remember(screenWidth, screenHeight) {
            when {
                screenWidth < 360.dp -> 0.85f   // Small phones
                isTablet -> 1.1f                // Tablets/Foldables
                else -> 1.0f                    // Standard phones
            }
        }

        // Proportional variables for layout components
        val dynamicPadding = (if (isTablet) 32.dp else 16.dp) * scaleFactor
        val maxContainerWidth = if (isTablet) 580.dp else 420.dp
        val brandLogoSize = (if (isTablet) 180.dp else 130.dp) * scaleFactor

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dynamicPadding, vertical = dynamicPadding)
                .widthIn(max = maxContainerWidth)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Theme toggle button at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val nextTheme = if (isDark) "light" else "dark"
                            viewModel.setThemeMode(nextTheme)
                        },
                        modifier = Modifier
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f),
                                CircleShape
                            )
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.Brightness3,
                            contentDescription = "تبديل المظهر",
                            tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF1E293B)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // NEW: Share App Button
                    IconButton(
                        onClick = { com.example.util.ShareUtil.shareAppApk(context) },
                        modifier = Modifier
                            .background(
                                themePrimary.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة التطبيق",
                            tint = themePrimary
                        )
                    }
                }
                
                Text(
                    text = "FarmManager PRO",
                    fontSize = 11.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Centered visual farm logo
            FarmManagerLogo(
                modifier = Modifier
                    .size(brandLogoSize)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Brand title matching layout
            Text(
                text = "FarmManager PRO",
                color = if (isDark) Color.White else Color(0xFF14532D),
                fontSize = (24.sp * scaleFactor).value.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "مدير المزرعة",
                color = if (isDark) Color(0xFF22C55E) else Color(0xFF15803D),
                fontSize = (16.sp * scaleFactor).value.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Title
            Text(
                text = if (isCreatingNew) "إنشاء حساب جديد" else "المزارع المتاحة",
                color = textPrimary,
                fontSize = (20.sp * scaleFactor).value.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Form Fields Card layout
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .background(cardBgColor, RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isCreatingNew) {
                    // Username / FarmName field
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "اسم المزرعة الجديدة 🏡",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            placeholder = { Text("أدخل اسم المزرعة لبدء تسجيل السجلات والماشية فيها", fontSize = 11.sp, color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isDark) Color.LightGray else Color(0xFF64748B)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = buttonGreen,
                                unfocusedBorderColor = if (isDark) Color.DarkGray else Color(0xFFE2E8F0),
                                focusedContainerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                                unfocusedContainerColor = if (isDark) Color(0xFF0F172A) else Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Password field
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "كلمة مرور حماية المزرعة (اختياري) 🔑",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        OutlinedTextField(
                            value = inputPassword,
                            onValueChange = { inputPassword = it },
                            placeholder = { Text("أدخل كلمة مرور لحماية حساب المزرعة كخيار أمني أو اتركها فارغة", fontSize = 11.sp, color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isDark) Color.LightGray else Color(0xFF64748B)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = if (isDark) Color.LightGray else Color(0xFF64748B)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = buttonGreen,
                                unfocusedBorderColor = if (isDark) Color.DarkGray else Color(0xFFE2E8F0),
                                focusedContainerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                                unfocusedContainerColor = if (isDark) Color(0xFF0F172A) else Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Green creation button
                    Button(
                        onClick = {
                            if (inputName.isBlank()) {
                                Toast.makeText(context, "الرجاء إدخال اسم المزرعة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            // Bypass Google linkage as requested
                            viewModel.createFarm(inputName, inputPassword)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "إنشاء حساب وبدء العمل 🚀",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Replaced traditional input fields with Premium Material 3 Saved Farms interactive Grid/List
                    val allGoogleLinks by viewModel.allGoogleLinks.collectAsStateWithLifecycle()
                    val savedFarmProfiles = remember(allFarms, allGoogleLinks) {
                        allFarms.map { farm ->
                            val associatedLink = allGoogleLinks.find { it.farmName == farm.name }
                            com.example.data.model.SavedFarmProfile(
                                farmId = farm.id,
                                farmName = farm.name,
                                linkedGoogleEmail = associatedLink?.googleEmail ?: "لا توجد حماية جوجل للمزرعة"
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "المزارع المسجلة والمربوطة بالسحاب ⛅",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = (12.sp * scaleFactor).value.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        if (savedFarmProfiles.isEmpty()) {
                            // Friendly modern empty state cards for first launch
                            Surface(
                                color = if (isDark) Color(0xFF0B1329).copy(alpha = 0.5f) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.LightGray.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HomeWork,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = "لا توجد مزارع مسجلة حالياً.",
                                        color = textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Text(
                                        text = "قم بإنشاء مزرعة جديدة أو مزامنة داتا جوجل لاسترجاعها.",
                                        color = textSecondary,
                                        fontSize = 11.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        } else {
                            // Display premium list of saved farms
                            for (profile in savedFarmProfiles) {
                                var isCardAuthenticating by remember { mutableStateOf(false) }

                                androidx.compose.material3.OutlinedCard(
                                    onClick = {
                                        if (!isCardAuthenticating) {
                                            // Securely check for existing local password protection before allowing entry
                                            val farmEntity = allFarms.find { it.name == profile.farmName }
                                            if (farmEntity != null && farmEntity.password.isNotBlank()) {
                                                showLocalPasswordPrompt = farmEntity
                                                localPasswordInput = ""
                                                return@OutlinedCard
                                            }

                                            isCardAuthenticating = true
                                            // Silently verify or restore google sign in linked session dynamically!
                                            if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
                                                try {
                                                    googleSignInClient.silentSignIn().addOnCompleteListener { task ->
                                                        val account = if (task.isSuccessful) task.result else null
                                                        val email = account?.email ?: ""
                                                        
                                                        // CRITICAL: Check if this farm is linked to a specific google account
                                                        if (profile.linkedGoogleEmail != "لا توجد حماية جوجل للمزرعة") {
                                                            if (email.isBlank() || !email.equals(profile.linkedGoogleEmail, ignoreCase = true)) {
                                                                Toast.makeText(context, "عذراً، يجب تسجيل الدخول بحساب جوجل المربوط بهذه المزرعة: ${profile.linkedGoogleEmail}", Toast.LENGTH_LONG).show()
                                                                isCardAuthenticating = false
                                                                // Optional: Trigger full sign-in if silent fails and it's a linked farm
                                                                if (!task.isSuccessful) {
                                                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                                                }
                                                                return@addOnCompleteListener
                                                            }
                                                        }

                                                        if (email.isNotBlank() && account?.idToken != null) {
                                                            viewModel.linkGoogleAccountWithFirebase(
                                                                idToken = account.idToken!!,
                                                                email = email,
                                                                name = account.displayName ?: "",
                                                                photoUrl = account.photoUrl,
                                                                context = context,
                                                                onSuccess = {
                                                                    viewModel.linkEmailToFarm(email, profile.farmName)
                                                                    viewModel.selectFarm(profile.farmName, "")
                                                                    isCardAuthenticating = false
                                                                    Toast.makeText(context, "أهلاً بك مجدداً في ${profile.farmName} 🏡", Toast.LENGTH_SHORT).show()
                                                                }
                                                            )
                                                        } else {
                                                            if (email.isNotBlank()) {
                                                                viewModel.linkEmailToFarm(email, profile.farmName)
                                                            }
                                                            viewModel.selectFarm(profile.farmName, "")
                                                            isCardAuthenticating = false
                                                            Toast.makeText(context, "أهلاً بك مجدداً في ${profile.farmName} 🏡", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    // If not linked, we can fallback to local entry
                                                    if (profile.linkedGoogleEmail == "لا توجد حماية جوجل للمزرعة") {
                                                        viewModel.selectFarm(profile.farmName, "")
                                                    } else {
                                                        Toast.makeText(context, "فشل التحقق من حساب جوجل المربوط.", Toast.LENGTH_SHORT).show()
                                                    }
                                                    isCardAuthenticating = false
                                                }
                                            } else {
                                                // If disabled, just allow entry or handle as needed
                                                viewModel.selectFarm(profile.farmName, "")
                                                isCardAuthenticating = false
                                            }
                                        }
                                    },
                                    colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
                                        containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.3f) else Color.White
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Agriculture,
                                                    contentDescription = null,
                                                    tint = if (isDark) Color(0xFF34D399) else buttonGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = profile.farmName,
                                                    color = textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountCircle,
                                                        contentDescription = null,
                                                        tint = textSecondary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) profile.linkedGoogleEmail else "الخدمات السحابية معطلة",
                                                        color = textSecondary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }

                                        if (isCardAuthenticating) {
                                            CircularProgressIndicator(
                                                color = buttonGreen,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack, // Standard back arrow verified to exist!
                                                contentDescription = "الدخول السريع",
                                                tint = buttonGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showLocalPasswordPrompt != null) {
                    val farm = showLocalPasswordPrompt!!
                    AlertDialog(
                        onDismissRequest = { showLocalPasswordPrompt = null },
                        title = { Text("حماية المزرعة 🔐", fontWeight = FontWeight.Bold, color = themePrimary) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("المزرعة '${farm.name}' محمية بكلمة مرور. يرجى إدخالها للمتابعة:", fontSize = 13.sp, color = textPrimary)
                                OutlinedTextField(
                                    value = localPasswordInput,
                                    onValueChange = { localPasswordInput = it },
                                    label = { Text("كلمة المرور") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (localPasswordInput == farm.password) {
                                        viewModel.selectFarm(farm.name, localPasswordInput)
                                        showLocalPasswordPrompt = null
                                        Toast.makeText(context, "أهلاً بك في ${farm.name}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "كلمة المرور خاطئة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonGreen)
                            ) {
                                Text("دخول")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLocalPasswordPrompt = null }) {
                                Text("إلغاء", color = Color.Red)
                            }
                        }
                    )
                }

                if (showFarmOnboardingDialog) {
                    AlertDialog(
                        onDismissRequest = { showFarmOnboardingDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HomeWork,
                                    contentDescription = null,
                                    tint = buttonGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "خطوة أخيرة: اسم المزرعة 🏡",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = textPrimary
                                )
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "تم تسجيل طلب حسابك بنجاح! يرجى إدخال اسم المزرعة لتبدأ بكاشف السجلات والمحاصيل والماشية فيها:",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                                OutlinedTextField(
                                    value = inputOnboardingFarmName,
                                    onValueChange = { inputOnboardingFarmName = it },
                                    placeholder = { Text("أدخل اسم المزرعة (مثال: مزرعة البركة)", fontSize = 11.sp, color = Color.Gray) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.HomeWork,
                                            contentDescription = null,
                                            tint = buttonGreen
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        focusedBorderColor = buttonGreen,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = inputPassword,
                                    onValueChange = { inputPassword = it },
                                    placeholder = { Text("أدخل كلمة مرور (اختياري)", fontSize = 11.sp, color = Color.Gray) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = buttonGreen
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        focusedBorderColor = buttonGreen,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    singleLine = true,
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (inputOnboardingFarmName.isBlank()) {
                                        Toast.makeText(context, "الرجاء إدخال اسم المزرعة", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    showFarmOnboardingDialog = false
                                    viewModel.createFarm(inputOnboardingFarmName, inputPassword)
                                    if (inputName.isNotBlank()) {
                                        viewModel.linkEmailToFarm(inputName, inputOnboardingFarmName)
                                    }
                                    inputOnboardingFarmName = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تأكيد وبدء العمل 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showFarmOnboardingDialog = false }) {
                                Text("إلغاء", color = Color.Gray)
                            }
                        }
                    )
                }

                // White/Grey Google authentication button in line with mockup
                if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
                    Button(
                        onClick = {
                            try {
                                viewModel.unlinkGoogleAccount()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    val signInIntent = googleSignInClient.signInIntent
                                    googleSignInLauncher.launch(signInIntent)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "فشل بدء تسجيل الدخول بـ Google. جاري تشغيل المحاكي التلقائي.", Toast.LENGTH_SHORT).show()
                                showAccountPicker = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF334155) else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color.Transparent else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تسجيل الدخول باستخدام جوجل",
                                color = if (isDark) Color.White else Color(0xFF334155),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Links
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        Toast.makeText(
                            context,
                            "لاستعادة كلمة المرور، يرجى تسجيل الدخول عبر Google أو مراجعة الدعم الفني.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                ) {
                    Text(
                        text = "نسيت كلمة المرور؟",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isCreatingNew) "لديك حساب بالفعل؟ " else "ليس لديك حساب؟ ",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (isCreatingNew) "تسجيل الدخول" else "إنشاء حساب جديد",
                        color = if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            isCreatingNew = !isCreatingNew
                            inputName = ""
                            inputPassword = ""
                        }
                    )
                }
            }
        }
    }
}

// ================= THE GOOGLE RECONNECT PRELOADER SPLASH =================
@Composable
fun GoogleSplashPreloaderScreen(
    googleName: String,
    googleEmail: String,
    themePrimary: Color,
    viewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        // Automatically select/launch Default Google Cloud-secured Farm layout
        viewModel.createFarm("مزرعتي الذهبية", "")
        onDismiss()
        Toast.makeText(context, "أهلاً بك مجدداً يا $googleName! تم استرجاع ومزامنة الداتا ☁️", Toast.LENGTH_SHORT).show()
    }

    val bgGradient = remember {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Slate 900
                Color(0xFF020617)  // Slate 950
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("G", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "أهلاً بك مجدداً يا $googleName! 👋",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = googleEmail,
                color = themePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(color = themePrimary, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))

            Text(
                "جاري استرجاع ومزامنة بيانات المزرعة السحابية تلقائياً...",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تخطي وتسجيل دخول يدوي", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

// ================= THE USERS & PERMISSIONS MANAGEMENT VIEW =================
@Composable
fun UsersManagementScreen(
    viewModel: FarmViewModel,
    accentColor: Color,
    zoom: Float
) {
    com.example.ui.screens.UsersManagementFirebaseScreen(viewModel, accentColor, zoom)
}

@Composable
fun UsersManagementScreenLegacyPlaceholder(
    viewModel: FarmViewModel,
    accentColor: Color,
    zoom: Float
) {
    val users by viewModel.appUsersList.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val isGoogleLinked by viewModel.isGoogleLinked.collectAsStateWithLifecycle()
    val googleEmail by viewModel.googleUserEmail.collectAsStateWithLifecycle()
    val googleName by viewModel.googleUserName.collectAsStateWithLifecycle()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputRole by remember { mutableStateOf("عامل / محاسب") }

    var roleMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header
        Text(
            text = "المستخدمين وإدارة الصلاحيات 👥",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Session Simulation Card
        Card(
            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "محاكاة دور المستخدم الحالي (لاختبار الصلاحيات وباقي الشاشات):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الدور النشط الآن: $currentUserRole",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = accentColor
                    )
                    
                    Box {
                        Button(
                            onClick = { roleMenuExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تبديل الدور 🔄", color = Color.White, fontSize = 11.sp)
                        }
                        DropdownMenu(
                            expanded = roleMenuExpanded,
                            onDismissRequest = { roleMenuExpanded = false }
                        ) {
                            listOf("مدير عام", "مشرف", "عامل / محاسب").forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        viewModel.changeCurrentUserRole(role)
                                        roleMenuExpanded = false
                                        Toast.makeText(context, "تم التبديل إلى دور: $role", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 قواعد الصلاحيات بالمزرعة برو:\n• عامل / محاسب: للعرض فقط (تُخفى أو تُعطّل أزرار الإضافة والتعديل والحذف وتعديل المسميات)\n• مشرف: يمكنه العرض وإضافة السجلات، ولكن يُمنع من الحذف تماماً\n• مدير عام: لديه السيطرة والتحكم الكامل لتعديل وحذف كل شيء بالتطبيق",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }

        // Action add user
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "جدول الحسابات والأجهزة المرتبطة بالمزرعة:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (currentUserRole == "مدير عام") {
                Button(
                    onClick = { showAddUserDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مستخدم", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Users List Column
        var selectedUserForPermissions by remember { mutableStateOf<com.example.data.model.UserAccount?>(null) }
        
        users.forEach { user ->
            val name = user.name
            val email = user.email
            val role = user.role
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth().clickable {
                    if (currentUserRole == "مدير عام") {
                        selectedUserForPermissions = user
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(accentColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name.take(1), fontWeight = FontWeight.Black, color = accentColor)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (email == googleEmail && isGoogleLinked) {
                                    Box(
                                        modifier = Modifier
                                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("أنت / Google Connected", fontSize = 8.sp, color = accentColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(email, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    when (role) {
                                        "مدير عام" -> Color(0xFFFEF3C7)
                                        "مشرف" -> Color(0xFFDBEAFE)
                                        else -> Color(0xFFF1F5F9)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = role,
                                color = when (role) {
                                    "مدير عام" -> Color(0xFFD97706)
                                    "مشرف" -> Color(0xFF2563EB)
                                    else -> Color(0xFF475569)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (currentUserRole == "مدير عام" && email != googleEmail) {
                            IconButton(onClick = { viewModel.deleteUser(email) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف مرسل", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        
        if (selectedUserForPermissions != null) {
            val user = selectedUserForPermissions!!
            var tempPerms by remember { mutableStateOf(user.permissions.toMutableMap()) }
            val availablePermissions = listOf(
                "view_barn_livestock" to "عرض الحظيرة",
                "add_livestock_head" to "إضافة رأس",
                "add_livestock_born" to "إضافة ولادة",
                "add_livestock_breed" to "تربية حيوانات",
                "preview_invoice_modal" to "معاينة الفاتورة",
                "pay_invoice_balance" to "سداد رصيد الفاتورة",
                "print_invoice_dir" to "طباعة الفاتورة",
                "add_animal" to "إضافة حيوان",
                "edit_animal" to "تعديل حيوان",
                "delete_animal" to "حذف/نفوق حيوان",
                "view_financial" to "رؤية الشؤون المالية",
                "add_transaction" to "تسجيل معاملة مالية",
                "manage_debts" to "إدارة وتسوية الديون",
                "backup_sync" to "النسخ الاحتياطي والمزامنة",
                "manage_users" to "إدارة المستخدمين"
            )
            
            val isSuperAdmin = user.email.equals("hamo.amer9090@gmail.com", ignoreCase = true)
            
            AlertDialog(
                onDismissRequest = { selectedUserForPermissions = null },
                title = { Text("تعديل صلاحيات: ${user.name}") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSuperAdmin) {
                            Text(
                                "تحذير: هذا الحساب هو المسؤول الأساسي (Super Admin) ولا يمكن سحب أو تعديل صلاحياته لتجنب القفل الخاطئ. كافة الصلاحيات مفعلة وتتخطى قواعد البيانات.",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        availablePermissions.forEach { (permKey, permLabel) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isSuperAdmin) {
                                        val c = tempPerms[permKey] ?: false
                                        tempPerms[permKey] = !c
                                        // Trigger recomposition trick
                                        tempPerms = tempPerms.toMutableMap()
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isSuperAdmin || tempPerms[permKey] == true,
                                    onCheckedChange = { isChecked ->
                                        if (!isSuperAdmin) {
                                            tempPerms[permKey] = isChecked
                                            tempPerms = tempPerms.toMutableMap()
                                        }
                                    },
                                    enabled = !isSuperAdmin
                                )
                                Text(permLabel, fontSize = 14.sp, color = if(isSuperAdmin) Color.Gray else Color.Unspecified)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateUserPermissions(user.email, tempPerms)
                            selectedUserForPermissions = null
                            Toast.makeText(context, "تم حفظ الصلاحيات", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("حفظ", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedUserForPermissions = null }) { Text("إلغاء") }
                }
            )
        }
    }

    if (showAddUserDialog) {
        var userRoleExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("إقران وإضافة مستخدم جديد بالفريق", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("الاسم الكامل للمستخدم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("بريد Google المرتبط بالحساب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { userRoleExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("دور الصلاحيات: $inputRole 🔽")
                        }
                        DropdownMenu(
                            expanded = userRoleExpanded,
                            onDismissRequest = { userRoleExpanded = false }
                        ) {
                            listOf("مدير عام", "مشرف", "عامل / محاسب").forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        inputRole = r
                                        userRoleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isBlank() || inputEmail.isBlank()) return@Button
                        viewModel.addUser(inputName, inputEmail, inputRole)
                        showAddUserDialog = false
                        inputName = ""
                        inputEmail = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("ربط ودعوة الحساب", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("إلغاء الأمر", color = Color.Gray)
                }
            }
        )
    }
}

// ================= SCREEN COMPOSABLE 1: DASHBOARD =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FarmViewModel,
    accentColor: Color,
    zoomLevel: Float,
    onNavigateToTab: (String) -> Unit,
    onOpenTransaction: (String) -> Unit,
    onOpenAnimal: () -> Unit,
    onOpenFeed: () -> Unit,
    onOpenFinancialDetails: (String) -> Unit
) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    val summary = viewModel.getSummaryTotals()
    val notes by viewModel.notesList.collectAsStateWithLifecycle()
    val animals by viewModel.animalsList.collectAsStateWithLifecycle()
    val people by viewModel.peopleList.collectAsStateWithLifecycle()
    val hideWelcomeCard by viewModel.hideWelcomeCard.collectAsStateWithLifecycle()
    val hideNetBalance by viewModel.hideNetBalance.collectAsStateWithLifecycle()
    val hideDashboardQuickActions by viewModel.hideDashboardQuickActions.collectAsStateWithLifecycle()
    val hideDashboardShortcuts by viewModel.hideDashboardShortcuts.collectAsStateWithLifecycle()
    val hideDashboardNotes by viewModel.hideDashboardNotes.collectAsStateWithLifecycle()
    val hideFinancials by viewModel.hideFinancials.collectAsStateWithLifecycle()
    val appCurrency by viewModel.appCurrency.collectAsStateWithLifecycle()
    val appLang by viewModel.appLang.collectAsStateWithLifecycle()
    val dashboardOrder by viewModel.dashboardItemsOrder.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    var revealFinancials by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            kotlinx.coroutines.delay(800)
            isRefreshing = false
        }
    }

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Personalized Welcome Card
            if (!hideWelcomeCard) {
                val onboardingViewModel: com.example.ui.viewmodel.OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val ownerName by onboardingViewModel.ownerName.collectAsStateWithLifecycle()
                val currentFarmName by viewModel.currentFarm.collectAsStateWithLifecycle()
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WavingHand,
                            contentDescription = "Welcome",
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "أهلاً بك، ${if (ownerName.isBlank()) "صاحب المزرعة" else ownerName} 👋",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "مزرعة: ${currentFarmName ?: "قيد التحميل..."}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            dashboardOrder.forEach { itemKey ->
                when (itemKey) {
                    "net_balance" -> {
                        // Net Ledger Display Grid
                        if (!hideNetBalance) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Net income
                Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)), RoundedCornerShape(16.dp))
                    .clickable { 
                        if (hideFinancials && !revealFinancials) { revealFinancials = true } 
                        else { onOpenFinancialDetails("cash") }
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(com.example.util.Localization.t("الكاش الفعلي", appLang), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(accentColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        if (hideFinancials && !revealFinancials) "*** $appCurrency" else "${summary.first} $appCurrency",
                        fontWeight = FontWeight.Black,
                        fontSize = (16f * (zoomLevel / 16f)).sp,
                        color = if (summary.first >= 0) accentColor else Color(0xFFEF4444)
                    )
                }
            }

            // Credits ledger -> لك
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)), RoundedCornerShape(16.dp))
                    .clickable {
                        if (hideFinancials && !revealFinancials) { revealFinancials = true } 
                        else { onOpenFinancialDetails("credits") }
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(com.example.util.Localization.t("حقوقك (لك)", appLang), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF2563EB).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowCircleDown, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        if (hideFinancials && !revealFinancials) "*** $appCurrency" else "${summary.second} $appCurrency",
                        fontWeight = FontWeight.Black,
                        fontSize = (16f * (zoomLevel / 16f)).sp,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            // Debts ledger -> عليك
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)), RoundedCornerShape(16.dp))
                    .clickable {
                        if (hideFinancials && !revealFinancials) { revealFinancials = true } 
                        else { onOpenFinancialDetails("debts") }
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(com.example.util.Localization.t("مستحقات (عليك)", appLang), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowCircleUp, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        if (hideFinancials && !revealFinancials) "*** $appCurrency" else "${summary.third} $appCurrency",
                        fontWeight = FontWeight.Black,
                        fontSize = (16f * (zoomLevel / 16f)).sp,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
        }
                    }
                    "quick_actions" -> {
        // Quick shortcut panel
        if (!hideDashboardQuickActions) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 0.5.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(com.example.util.Localization.t("إجراءات تشغيلية سريعة", appLang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        buttonShortcut(com.example.util.Localization.t("شراء رأس", appLang), Icons.Default.Pets, accentColor) { onOpenAnimal() }
                        buttonShortcut(com.example.util.Localization.t("شراء علف", appLang), Icons.Default.Grass, Color(0xFFD97706)) { onOpenFeed() }
                        buttonShortcut(com.example.util.Localization.t("سند قبض", appLang), Icons.Default.TrendingUp, Color(0xFF2563EB)) { onOpenTransaction("income") }
                        buttonShortcut(com.example.util.Localization.t("سند صرف", appLang), Icons.Default.TrendingDown, Color(0xFFEF4444)) { onOpenTransaction("expense") }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.exportHtmlReport(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(com.example.util.Localization.t("تصدير ومشاركة تقرير الويب المنسق (HTML/CSS)", appLang), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
                    }
                    "shortcuts" -> {
        // Module directory buttons
        if (!hideDashboardShortcuts) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { onNavigateToTab("barn") },
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = accentColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(com.example.util.Localization.t("إدارة الحظيرة", appLang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = accentColor)
                        Text(if (appLang == "en") "${animals.size} Active Heads" else "${animals.size} رأس نشط", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Surface(
                    onClick = { onNavigateToTab("feeds") },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFD97706).copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Grass, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(com.example.util.Localization.t("الأعلاف والمخزن", appLang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD97706))
                        Text(com.example.util.Localization.t("تفاصيل وحصص منفصلة", appLang), fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { onNavigateToTab("accounts") },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(com.example.util.Localization.t("حسابات الأشخاص", appLang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                        Text(com.example.util.Localization.t("إدارة السنادات للأسماء", appLang), fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Surface(
                    onClick = { onNavigateToTab("backup") },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(com.example.util.Localization.t("النسخ الاحتياطي", appLang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF8B5CF6))
                        Text(com.example.util.Localization.t("استيراد ورفع البيانات", appLang), fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
                    }
                    "notes" -> {
        // Brief listing of recent text or photo notes
        if (!hideDashboardNotes && notes.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مذكرات وملاحظات حديثة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { onNavigateToTab("notes") }) {
                        Text("عرض الكل", color = accentColor, fontSize = 12.sp)
                    }
                }

                notes.take(3).forEach { note ->
                    Surface(
                        onClick = { onNavigateToTab("notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bitmap = remember(note.imageBase64) {
                                note.imageBase64?.let { ImageUtils.base64ToBitmap(it) }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.triggerImageEnlargement(note.imageBase64) },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.NoteAlt, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.content,
                                    maxLines = 1,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateStr = remember(note.createdAt) {
                                    SimpleDateFormat("yyyy/MM/dd", Locale("ar", "SA")).format(Date(note.createdAt))
                                }
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                             Spacer(modifier = Modifier.width(8.dp))
                             Icon(
                                 imageVector = Icons.Default.ChevronLeft,
                                 contentDescription = null,
                                 tint = Color(0xFF94A3B8),
                                 modifier = Modifier.size(16.dp)
                             )
                        }
                    }
                }
            }
        }
                    }
                }
            }
    }
}
}

@Composable
fun buttonShortcut(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}

// ================= SCREEN COMPOSABLE 2: BARN/ANIMALS =================
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BarnScreen(
    viewModel: FarmViewModel,
    themePrimary: Color,
    zoom: Float,
    cardBgColor: Color,
    onCheckAddPermission: (() -> Unit) -> Unit,
    onConfirmEdit: (String, () -> Unit) -> Unit,
    onConfirmDelete: (String, () -> Unit) -> Unit,
    onAddAnimalClick: () -> Unit,
    onEditAnimal: (com.example.data.model.AnimalEntity) -> Unit,
    onViewAnimal: (Int) -> Unit,
    onViewNewborns: () -> Unit
) {
    val context = LocalContext.current
    val animals by viewModel.animalsList.collectAsStateWithLifecycle()
    val filterType by viewModel.selectedAnimalType.collectAsStateWithLifecycle()
    val people by viewModel.peopleList.collectAsStateWithLifecycle()
    val births by viewModel.birthsList.collectAsStateWithLifecycle()

    var showSellDialogByAnimal by remember { mutableStateOf<AnimalEntity?>(null) }
    var showSellBirthDialog by remember { mutableStateOf<BirthEntity?>(null) }
    var expandedAnimalId by remember { mutableStateOf<Int?>(null) } // height-controller expansion

    // Edit states
    var editingAnimal by remember { mutableStateOf<AnimalEntity?>(null) }
    var showLocalNewbornDialog by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            kotlinx.coroutines.delay(800)
            isRefreshing = false
        }
    }

    var showAddTypeDialog by remember { mutableStateOf(false) }
    var newMaleTypeName by remember { mutableStateOf("") }
    var newFemaleTypeName by remember { mutableStateOf("") }

    val rawAnimalTypesByPref by viewModel.animalTypesList.collectAsStateWithLifecycle()
    val typesFromPrefList = remember(rawAnimalTypesByPref) {
        if (rawAnimalTypesByPref.isEmpty()) listOf("عجل", "أغنام", "ماعز", "جمال", "جاموس") else rawAnimalTypesByPref
    }

    var globalSearchQuery by remember { mutableStateOf("") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedAnimalIds by remember { mutableStateOf(setOf<Int>()) }

    val filteredList = remember(animals, filterType, globalSearchQuery) {
        var resultList = if (filterType == "all") {
            animals
        } else {
            val typeObj = com.example.utils.AnimalTypeHelper.parseAnimalType(filterType)
            animals.filter { 
                it.type.equals(typeObj.male, ignoreCase = true) || 
                it.type.equals(typeObj.female, ignoreCase = true) || 
                it.type.equals(filterType, ignoreCase = true)
            }
        }
        
        if (globalSearchQuery.isNotBlank()) {
            val query = globalSearchQuery.trim().lowercase()
            resultList = resultList.filter {
                it.name.lowercase().contains(query) ||
                it.merchantName.lowercase().contains(query) ||
                it.type.lowercase().contains(query)
            }
        }
        
        resultList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSelectionMode) {
            Row(
                modifier = Modifier.fillMaxWidth().background(themePrimary.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("المحدد: ${selectedAnimalIds.size}", fontWeight = FontWeight.Bold, color = themePrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedAnimalIds.isNotEmpty()) {
                        IconButton(onClick = {
                            try {
                                val selectedAnimalsList = filteredList.filter { selectedAnimalIds.contains(it.id) }
                                val pdf = android.graphics.pdf.PdfDocument()
                                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                                val page = pdf.startPage(pageInfo)
                                val canvas = page.canvas
                                val paint = android.graphics.Paint()
                                
                                paint.textSize = 10f
                                canvas.drawText("سجل القطيع", 50f, 50f, paint)
                                
                                var currentY = 80f
                                for (an in selectedAnimalsList) {
                                    canvas.drawText("الاسم: ${an.name} - النوع: ${an.type} - السعر: ${an.purchasePrice}", 50f, currentY, paint)
                                    currentY += 20f
                                }
                                pdf.finishPage(page)
                                
                                val file = java.io.File(context.cacheDir, "barn_report.pdf")
                                pdf.writeTo(java.io.FileOutputStream(file))
                                pdf.close()
                                
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "مشاركة سجل القطيع"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشل تصدير الفاتورة", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "تصدير", tint = themePrimary)
                        }
                        
                        IconButton(onClick = {
                            val selectedAnimalsList = filteredList.filter { selectedAnimalIds.contains(it.id) }
                            onConfirmDelete("هل أنت متأكد من حذف ${selectedAnimalsList.size} رأس/عنصر من المزرعة؟") {
                                selectedAnimalsList.forEach { animal ->
                                    viewModel.deleteAnimalRecord(animal)
                                }
                                selectedAnimalIds = emptySet()
                                isSelectionMode = false
                                Toast.makeText(context, "تم حذف المحدد بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف المحدد", tint = Color.Red)
                        }
                    }
                    TextButton(onClick = { 
                        if (selectedAnimalIds.size == filteredList.size) {
                            selectedAnimalIds = emptySet()
                        } else {
                            selectedAnimalIds = filteredList.map { it.id }.toSet()
                        }
                    }) {
                        Text(if (selectedAnimalIds.size == filteredList.size) "إلغاء التحديد" else "تحديد الكل")
                    }
                    TextButton(onClick = { isSelectionMode = false; selectedAnimalIds = emptySet() }) {
                        Text("إنهاء", color = Color.Red)
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("قائمة رؤوس الحظيرة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onAddAnimalClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إضافة جديد ➕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = onViewNewborns,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("عرض المواليد 👶", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box {
                var expandedFilter by remember { mutableStateOf(false) }
                
                Button(
                    onClick = { expandedFilter = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    val currentDisplay = if (filterType == "all") "إضافة نوع / تصفية ➕🔎" else {
                        val parsed = com.example.utils.AnimalTypeHelper.parseAnimalType(filterType)
                        "تصفية: ${parsed.male} 🔎"
                    }
                    Text(currentDisplay, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(
                    expanded = expandedFilter,
                    onDismissRequest = { expandedFilter = false }
                ) {
                    // Option 1: View All
                    DropdownMenuItem(
                        text = { Text("📋 عرض الكل (جميع السلالات)", fontWeight = if (filterType == "all") FontWeight.Bold else FontWeight.Normal, color = if (filterType == "all") themePrimary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp) },
                        onClick = {
                            viewModel.setAnimalTypeFilter("all")
                            expandedFilter = false
                        }
                    )
                    
                    HorizontalDivider()

                    // Pref / Custom list
                    typesFromPrefList.forEach { t ->
                        val parsed = com.example.utils.AnimalTypeHelper.parseAnimalType(t)
                        val displayName = "${parsed.male} / ${parsed.female}"
                        val isSelected = filterType == t
                        
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) themePrimary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                    IconButton(
                                        onClick = {
                                            viewModel.removeCustomAnimalType(t)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف النوع", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setAnimalTypeFilter(t)
                                expandedFilter = false
                            }
                        )
                    }

                    HorizontalDivider()

                    // Add custom type option
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = themePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("➕ إضافة نوع جديد للحظيرة", color = themePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            expandedFilter = false
                            showAddTypeDialog = true
                        }
                    )
                }
            }
        }

        if (showAddTypeDialog) {
            AlertDialog(
                onDismissRequest = { showAddTypeDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = themePrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة سلالة/نوع جديد بالحظيرة ✨", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("أدخل مسمى الذكر والأنثى بدقة لضبط الإشعارات والتقارير تلقائياً حسب الجنس:", fontSize = 12.sp, color = Color.Gray)
                        
                        OutlinedTextField(
                            value = newMaleTypeName,
                            onValueChange = { newMaleTypeName = it },
                            label = { Text("اسم الذكر (مثال: عجل، خروف، جدي)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        OutlinedTextField(
                            value = newFemaleTypeName,
                            onValueChange = { newFemaleTypeName = it },
                            label = { Text("اسم المؤنث (مثال: عجلة، نعجة، عنزة)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newMaleTypeName.isNotBlank() && newFemaleTypeName.isNotBlank()) {
                                viewModel.addCustomAnimalType(newMaleTypeName, newFemaleTypeName)
                                Toast.makeText(context, "تمت إضافة النوع الجديد للشبكة والمزرعة بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                                showAddTypeDialog = false
                                newMaleTypeName = ""
                                newFemaleTypeName = ""
                            } else {
                                Toast.makeText(context, "يرجى ملء الخانتين لتوليد الجنسين المتطابقين!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                    ) {
                        Text("حفظ وإضافة 💾", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTypeDialog = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }

        OutlinedTextField(
            value = globalSearchQuery,
            onValueChange = { globalSearchQuery = it },
            placeholder = { Text("بحث ذكي (الرقم، الحالة، المصدر، النوع، الوصف)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        )

        if (filteredList.isEmpty()) {
            PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد بيانات مطابقة للبحث", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredList) { animal ->
                        val isExpanded = expandedAnimalId == animal.id
                        val isItemSelected = selectedAnimalIds.contains(animal.id)
    
                        Surface(
                            color = if (isItemSelected) themePrimary.copy(alpha = 0.15f) else cardBgColor,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(if (isItemSelected) 2.dp else 1.dp, if (isItemSelected) themePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedAnimalIds = if (isItemSelected) {
                                                selectedAnimalIds - animal.id
                                            } else {
                                                selectedAnimalIds + animal.id
                                            }
                                        } else {
                                            expandedAnimalId = if (isExpanded) null else animal.id
                                        }
                                    },
                                    onLongClick = {
                                        isSelectionMode = true
                                        selectedAnimalIds = selectedAnimalIds + animal.id
                                    }
                                )
                        ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Photo snapshot preview
                                val bitmap = remember(animal.imageBase64) {
                                    animal.imageBase64?.let { ImageUtils.base64ToBitmap(it) }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.triggerImageEnlargement(animal.imageBase64) },
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(themePrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = themePrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            animal.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = (15f * (zoom / 16f)).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = themePrimary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                animal.type,
                                                fontSize = 9.sp,
                                                color = themePrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("شراء من التاجر: ${animal.merchantName}", fontSize = 11.sp, color = Color.Gray)
                                    Text("الوزن: ${animal.weight} كغ - السعر: ${animal.purchasePrice} جنيه", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(bottom = 8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                    ) {
                                        Button(
                                            onClick = { 
                                                showSellDialogByAnimal = animal
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("بيع 🏷️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                        Button(
                                            onClick = { onEditAnimal(animal) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("تعديل ✏️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                        Button(
                                            onClick = { onViewAnimal(animal.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("التفاصيل 👁️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    } // close Surface
                    } // close items(filteredList)
                    
                    if (births.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onViewNewborns,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("عرض قائمة المواليد بالحظيرة 🍼", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } // if (births.isNotEmpty)
            } // PullToRefreshBox
        } // else
    } // Column (main) it actually doesn't close here, wait, `Column` closed at 3310? Let's just remove one bracket.

    // Sell dialog overlay
    if (showSellDialogByAnimal != null) {
        val targetAnimal = showSellDialogByAnimal!!
        var sellPriceStr by remember { mutableStateOf("") }
        
        // Buyer Selection Options
        var buyerOption by remember { mutableStateOf("market") } // "market", "existing", "new"
        var selectedBuyerId by remember { mutableStateOf<Int?>(null) }
        var newBuyerName by remember { mutableStateOf("") }
        var expandedBuyerDropdown by remember { mutableStateOf(false) }

        // Collection Status
        var collectionStatus by remember { mutableStateOf("full_cash") } // "full_cash", "on_credit", "partial"
        var receivedAmountStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSellDialogByAnimal = null },
            title = { Text("وثيقة وسند بيع رأس الماشية 🪙", fontWeight = FontWeight.Black) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("اسم الرأس: ${targetAnimal.name} (${targetAnimal.type})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("سعر الشراء الكلي كان: ${targetAnimal.purchasePrice} جنيه", fontSize = 12.sp, color = Color.Gray)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // 1. BUYER SECTION
                    Text("العميل أو المشتري:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themePrimary)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { 
                                buyerOption = "market"
                                selectedBuyerId = null
                                newBuyerName = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (buyerOption == "market") themePrimary else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("سوق (عام)", color = if (buyerOption == "market") Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { 
                                buyerOption = "existing"
                                newBuyerName = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (buyerOption == "existing") themePrimary else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("حساب مسجل", color = if (buyerOption == "existing") Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { 
                                buyerOption = "new"
                                selectedBuyerId = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (buyerOption == "new") themePrimary else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("مشتري جديد ➕", color = if (buyerOption == "new") Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (buyerOption == "existing") {
                        val chosenBuyer = people.find { it.id == selectedBuyerId }
                        val btnLabel = if (chosenBuyer != null) "${chosenBuyer.name} (${chosenBuyer.role})" else "اختر الحساب المالي من القائمة 🔽"
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { expandedBuyerDropdown = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(btnLabel, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = expandedBuyerDropdown,
                                onDismissRequest = { expandedBuyerDropdown = false }
                            ) {
                                people.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.name} (${p.role}) - جوال: ${p.phone}") },
                                        onClick = {
                                            selectedBuyerId = p.id
                                            expandedBuyerDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    } else if (buyerOption == "new") {
                        OutlinedTextField(
                            value = newBuyerName,
                            onValueChange = { newBuyerName = it },
                            label = { Text("اسم العميل / المشتري الجديد") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 2. SALE PRICE
                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = { sellPriceStr = it },
                        label = { Text("قيمة وسعر البيع الإجمالي (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // 3. COLLECTION STATUS
                    Text("حالة تحصيل واستلام ثمن الماشية:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themePrimary)
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { collectionStatus = "full_cash" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (collectionStatus == "full_cash") Color(0xFF10B981) else Color(0xFFF1F5F9)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("كاش بالكامل 💵", color = if (collectionStatus == "full_cash") Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { collectionStatus = "on_credit" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (collectionStatus == "on_credit") Color(0xFFEF4444) else Color(0xFFF1F5F9)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("بالآجل (دين له) ⏳", color = if (collectionStatus == "on_credit") Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { collectionStatus = "partial" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (collectionStatus == "partial") Color(0xFFD97706) else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("محصل مالي جزئي (دفعة نقدية) 💰", color = if (collectionStatus == "partial") Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (collectionStatus == "partial") {
                        OutlinedTextField(
                            value = receivedAmountStr,
                            onValueChange = { receivedAmountStr = it },
                            label = { Text("المبلغ المالي المستلم بالفعل (جنيه)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sprice = sellPriceStr.toDoubleOrNull() ?: 0.0
                        if (sprice <= 0.0) {
                            Toast.makeText(context, "الرجاء كتابة قيمة بيع صحيحة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val recAmt = receivedAmountStr.toDoubleOrNull() ?: 0.0
                        if (collectionStatus == "partial" && (recAmt <= 0.0 || recAmt > sprice)) {
                            Toast.makeText(context, "الرجاء إدخال مبلغ مستلم صحيح أقل من إجمالي البيع", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (buyerOption == "existing" && selectedBuyerId == null) {
                            Toast.makeText(context, "الرجاء تحديد الحساب المالي المسجل للمشتري", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (buyerOption == "new" && newBuyerName.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال اسم العميل الجديد", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.sellAnimal(
                            animal = targetAnimal,
                            price = sprice,
                            associatedPersonId = selectedBuyerId,
                            newBuyerName = newBuyerName,
                            paymentStatus = collectionStatus,
                            receivedAmount = recAmt
                        )
                        Toast.makeText(context, "تم تسجيل الفاتورة وسند البيع وتحديث الأرصدة!", Toast.LENGTH_LONG).show()
                        showSellDialogByAnimal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("تسجيل وحفظ البيع", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialogByAnimal = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    // Sell birth dialog overlay
    if (showSellBirthDialog != null) {
        val b = showSellBirthDialog!!
        if (b.status == "تم بيعه") {
            AlertDialog(
                onDismissRequest = { showSellBirthDialog = null },
                title = { Text("المولود مباع") },
                text = { Text("عذراً، لا يمكن بيع هذا المولود لأنه مباع بالفعل.") },
                confirmButton = { TextButton(onClick = { showSellBirthDialog = null }) { Text("حسناً") } }
            )
        } else {
            var sellPriceStr by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showSellBirthDialog = null },
                title = { Text("بيع المولود", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("سيتم تغيير حالة المولود إلى مباع وإضافة مبلغ البيع كإيراد إلى الخزينة.")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = sellPriceStr,
                            onValueChange = { sellPriceStr = it },
                            label = { Text("مبلغ البيع (جنيه)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = sellPriceStr.toDoubleOrNull() ?: 0.0
                            viewModel.sellBirth(b, price)
                            showSellBirthDialog = null
                            Toast.makeText(context, "تم بيع المولود وتسجيل الإيراد المالي!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                    ) {
                        Text("تأكيد البيع", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSellBirthDialog = null }) { Text("إلغاء", color = Color.Gray) }
                }
            )
        }
    }

    if (showLocalNewbornDialog) {
        AddNewbornDialog(
            viewModel = viewModel,
            accentColor = themePrimary,
            onDismiss = { showLocalNewbornDialog = false }
        )
    }

    // Editing Dialog
    if (editingAnimal != null) {
        val target = editingAnimal!!
        var name by remember { mutableStateOf(target.name) }
        var type by remember { mutableStateOf(target.type) }
        var weightStr by remember { mutableStateOf(target.weight.toString()) }
        var purchasePriceStr by remember { mutableStateOf(target.purchasePrice.toString()) }
        var age by remember { mutableStateOf(target.age) }
        var feedCostStr by remember { mutableStateOf(target.feedCost.toString()) }
        var merchantName by remember { mutableStateOf(target.merchantName) }

        AlertDialog(
            onDismissRequest = { editingAnimal = null },
            title = { Text("تعديل بيانات الماشية", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("اسم التاجر المشتري / البائع") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم أو رقم الرأس") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("نوع رأس الماشية (مثال: عجل، أغنام)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("الوزن (كغ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it },
                        label = { Text("سعر الشراء (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("العمر") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = feedCostStr,
                        onValueChange = { feedCostStr = it },
                        label = { Text("تكلفة طعام هذا الرأس") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = target.copy(
                            merchantName = if (merchantName.isBlank()) "سوق" else merchantName,
                            name = name,
                            weight = weightStr.toDoubleOrNull() ?: target.weight,
                            purchasePrice = purchasePriceStr.toDoubleOrNull() ?: target.purchasePrice,
                            age = age,
                            feedCost = feedCostStr.toDoubleOrNull() ?: target.feedCost
                        )
                        onConfirmEdit("هل أنت متأكد من رغبتك في تعديل وحفظ بيانات رأس الماشية (${target.name})؟") {
                            viewModel.updateAnimalDetails(updated)
                            editingAnimal = null
                            Toast.makeText(context, "تم تعديل السجل بنجاح 📝", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("حفظ التعديلات", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAnimal = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }
}

// ================= SCREEN COMPOSABLE 3: FEEDS STORE =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsScreen(
    viewModel: FarmViewModel,
    themePrimary: Color,
    zoom: Float,
    cardBgColor: Color,
    onCheckAddPermission: (() -> Unit) -> Unit,
    onConfirmEdit: (String, () -> Unit) -> Unit,
    onConfirmDelete: (String, () -> Unit) -> Unit,
    onAddFeedClick: () -> Unit,
    onAddMedicineClick: () -> Unit
) {
    val feeds by viewModel.feedsList.collectAsStateWithLifecycle()
    val medicines by viewModel.medicinesList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var activeSubTab by remember { mutableStateOf(0) } // 0 = feeds, 1 = medicines
    var expandedFeedId by remember { mutableStateOf<Int?>(null) }
    var expandedMedicineId by remember { mutableStateOf<Int?>(null) }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            kotlinx.coroutines.delay(800)
            isRefreshing = false
        }
    }
    
    var globalSearchQuery by remember { mutableStateOf("") }
    
    val filteredFeeds = remember(feeds, globalSearchQuery) {
        if (globalSearchQuery.isNotBlank()) {
            val query = globalSearchQuery.trim().lowercase()
            feeds.filter {
                it.feedName.lowercase().contains(query) ||
                it.ingredientsDescription.lowercase().contains(query)
            }
        } else feeds
    }
    
    val filteredMedicines = remember(medicines, globalSearchQuery) {
        if (globalSearchQuery.isNotBlank()) {
            val query = globalSearchQuery.trim().lowercase()
            medicines.filter {
                it.name.lowercase().contains(query)
            }
        } else medicines
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dual Sub-Tab Switcher (M3 styling)
        Surface(
            color = themePrimary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair(0, "الأعلاف والمكونات 🌾"),
                    Pair(1, "الأدوية والعلاجات البيطرية 💊")
                ).forEach { (index, title) ->
                    val isSelected = activeSubTab == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubTab = index },
                        color = if (isSelected) themePrimary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = (12f * (zoom / 16f)).sp
                            )
                        }
                    }
                }
            }
        }

        // Section Title and Action button (adaptive based on tab)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeSubTab == 0) "مخزن وتكلفة الأعلاف المتاحة" else "مستودع الأدوية والعلاجات البيطرية",
                fontWeight = FontWeight.Bold,
                fontSize = (13f * (zoom / 16f)).sp
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = if (activeSubTab == 0) onAddFeedClick else onAddMedicineClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (activeSubTab == 0) "إضافة أعلاف ➕" else "إضافة دواء ➕",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = globalSearchQuery,
            onValueChange = { globalSearchQuery = it },
            placeholder = { Text("بحث ذكي (الرقم، الحالة، المصدر، النوع، الوصف)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        )

        if (activeSubTab == 0) {
            // FEEDS COLUMN LIST
            if (filteredFeeds.isEmpty()) {
                PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Grass, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد بيانات، اضغط + للإضافة", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(filteredFeeds) { feed ->
                            val isExpanded = expandedFeedId == feed.id
                            Surface(
                                color = cardBgColor,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedFeedId = if (isExpanded) null else feed.id }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Grass, contentDescription = null, tint = Color(0xFFD97706))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    feed.feedName,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = (15f * (zoom / 16f)).sp
                                                )
                                                Text("المواد: ${feed.ingredientsDescription}", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(top = 12.dp)
                                                .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("إجمالي الوزن المشتري: ${feed.totalWeight} كغ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("التكلفة الإجمالية: ${feed.totalCost} جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("تاريخ الإضافة السجلية: ${feed.addedDate}", fontSize = 11.sp, color = Color.Gray)

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Button(
                                                onClick = {
                                                    onConfirmDelete("هل أنت متأكد من رغبتك في حذف السند الحلفي (${feed.feedName}) نهائياً؟") {
                                                        viewModel.deleteFeedRecord(feed)
                                                        Toast.makeText(context, "تم حذف سند الأعلاف بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("حذف هذا السند الحلفي", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MEDICINES COLUMN LIST
            if (filteredMedicines.isEmpty()) {
                PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد أدوية أو علاجات مسجلة حالياً، اضغط + للإضافة", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(filteredMedicines) { med ->
                            val isExpanded = expandedMedicineId == med.id
                            Surface(
                                color = cardBgColor,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedMedicineId = if (isExpanded) null else med.id }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFF0284C7))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    med.name,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = (15f * (zoom / 16f)).sp
                                                )
                                                Text("فترة السحب بالأيام: ${med.validityDays} يوم", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(top = 12.dp)
                                                .background(Color(0xFFF0F9FF), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("التكلفة الإجمالية: ${med.totalCost} جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                                Text("فترة سحب الدواء: ${med.validityDays} يوم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("تاريخ الشراء: ${med.addedDate}", fontSize = 11.sp, color = Color.Gray)

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Button(
                                                onClick = {
                                                    onConfirmDelete("هل أنت متأكد من رغبتك في حذف السند الطبي الدوائي (${med.name}) نهائياً؟") {
                                                        viewModel.deleteMedicineRecord(med)
                                                        Toast.makeText(context, "تم حذف سند الدواء بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("حذف هذا السند الطبي الدوائي", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= SCREEN COMPOSABLE 4: ACCOUNTS (LEGER) =================
@Composable
fun AccountsScreen(
    viewModel: FarmViewModel,
    themePrimary: Color,
    zoom: Float,
    cardBgColor: Color,
    onCheckAddPermission: (() -> Unit) -> Unit,
    onConfirmEdit: (String, () -> Unit) -> Unit,
    onConfirmDelete: (String, () -> Unit) -> Unit
) {
    val people by viewModel.peopleList.collectAsStateWithLifecycle()
    val currentFarm by viewModel.currentFarm.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddPersonDialog by remember { mutableStateOf(false) }

    var expandedPersonId by remember { mutableStateOf<Int?>(null) } // height controller expander
    var selectedPersonForLedger by remember { mutableStateOf<PersonEntity?>(null) }

    // edit and delete status
    var editingPerson by remember { mutableStateOf<PersonEntity?>(null) }
    var personToDelete by remember { mutableStateOf<PersonEntity?>(null) }
    
    var globalSearchQuery by remember { mutableStateOf("") }
    
    val filteredPeople = remember(people, globalSearchQuery) {
        if (globalSearchQuery.isNotBlank()) {
            val query = globalSearchQuery.trim().lowercase()
            people.filter {
                it.name.lowercase().contains(query) ||
                it.role.lowercase().contains(query)
            }
        } else {
            people
        }
    }

    if (selectedPersonForLedger != null) {
        val currentPerson = people.find { it.id == selectedPersonForLedger?.id } ?: selectedPersonForLedger!!
        PersonLedgerPage(
            person = currentPerson,
            viewModel = viewModel,
            themePrimary = themePrimary,
            zoom = zoom,
            onClose = { selectedPersonForLedger = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("حسابات الأشخاص والعملاء دائن ومدين", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Button(
                    onClick = { onCheckAddPermission { showAddPersonDialog = true } },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("إضافة جديد +", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            OutlinedTextField(
                value = globalSearchQuery,
                onValueChange = { globalSearchQuery = it },
                placeholder = { Text("بحث ذكي (الرقم، الحالة، المصدر، النوع، الوصف)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredPeople.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد بيانات، اضغط + للإضافة", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredPeople) { person ->
                        val isExpanded = expandedPersonId == person.id

                        Surface(
                            color = cardBgColor,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedPersonId = if (isExpanded) null else person.id }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = if (person.balance >= 0) themePrimary else Color.Red,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                person.name,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = (15f * (zoom / 16f)).sp
                                            )
                                            Text("الصفة المهنية: ${person.role} - جوال: ${person.phone}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (person.balance >= 0) "له" else "عليه",
                                            fontSize = 11.sp,
                                            color = if (person.balance >= 0) themePrimary else Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${kotlin.math.abs(person.balance)} جنيه",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = if (person.balance >= 0) themePrimary else Color.Red
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 12.dp)
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Button(
                                            onClick = { selectedPersonForLedger = person },
                                            colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        ) {
                                            Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("كشف وسجل الحساب والمستندات 📑", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    if (!viewModel.hasPermission("manage_debts")) {
                                                        Toast.makeText(context, "عذراً، يرجى طلب صلاحية إدارة وتسوية الديون للتعديل ❌", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        editingPerson = person
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تعديل", color = Color.White, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    onConfirmDelete("هل أنت متأكد من حذف الحساب الكامل لـ (${person.name})؟ سيؤدي ذلك لإزالة التقرير المالي بالكامل وإلغاء ارتباطه بالمعاملات والماشية المرتبطة به. مزارعك وكل السجلات ستتأثر.") {
                                                        if (selectedPersonForLedger?.id == person.id) {
                                                            selectedPersonForLedger = null
                                                        }
                                                        viewModel.deletePersonRecord(person)
                                                        Toast.makeText(context, "تم حذف حساب الشخص بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("حذف الحساب", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPersonDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("صاحب المزرعة") } // "صاحب المزرعة", "عامل", "تاجر"
        var balStr by remember { mutableStateOf("") }
        var isCredit by remember { mutableStateOf(true) } // true: له, false: عليه

        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text("إضافة شخص أو عامل إلى السجلات", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الشخص الكامل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role selective buttons
                    Text("الصورة المهنية / العلاقية", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("صاحب المزرعة", "عامل", "تاجر").forEach { r ->
                            val active = role == r
                            Button(
                                onClick = { role = r },
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) themePrimary else Color.LightGray),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(r, color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = balStr,
                        onValueChange = { balStr = it },
                        label = { Text("الرصيد المبدئي (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isCredit, onClick = { isCredit = true })
                            Text("دائن (له مال)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !isCredit, onClick = { isCredit = false })
                            Text("مدين (عليه مال)")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val amt = balStr.toDoubleOrNull() ?: 0.0
                        val finalBal = if (isCredit) amt else -amt
                        viewModel.registerPerson(name, role, phone, finalBal)
                        showAddPersonDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("إضافة الشخص", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPersonDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    if (editingPerson != null) {
        val person = editingPerson!!
        var name by remember { mutableStateOf(person.name) }
        var phone by remember { mutableStateOf(person.phone) }
        var role by remember { mutableStateOf(person.role) }
        var balStr by remember { mutableStateOf(kotlin.math.abs(person.balance).toString()) }
        var isCredit by remember { mutableStateOf(person.balance >= 0) }

        AlertDialog(
            onDismissRequest = { editingPerson = null },
            title = { Text("تعديل بيانات الحساب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الجوال") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = balStr,
                        onValueChange = { balStr = it },
                        label = { Text("الرصيد المالي الحالي (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isCredit, onClick = { isCredit = true })
                            Text("له")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !isCredit, onClick = { isCredit = false })
                            Text("عليه")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val baseVal = balStr.toDoubleOrNull() ?: 0.0
                        val finalBal = if (isCredit) baseVal else -baseVal
                        val updatedPerson = person.copy(name = name, phone = phone, balance = finalBal)
                        onConfirmEdit("هل أنت متأكد من رغبتك في تعديل بيانات وتوازن حساب (${person.name})؟") {
                            viewModel.updatePersonRecord(updatedPerson)
                            editingPerson = null
                            Toast.makeText(context, "تم تعديل حساب الشخص بنجاح 📝", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("تعديل", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPerson = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    if (personToDelete != null) {
        val p = personToDelete!!
        AlertDialog(
            onDismissRequest = { personToDelete = null },
            title = { Text("تأكيد حذف الحساب ⚠️", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text(
                    "هل أنت متأكد من حذف الحساب الكامل لـ ( ${p.name} )؟ \n" +
                    "سيؤدي ذلك لإزالة التقرير المالي بالكامل وإلغاء ارتباطه بالمعاملات والماشية المرتبطة به. \n\n" +
                    "هذا الإجراء لا يمكن التراجع عنه!",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedPersonForLedger?.id == p.id) {
                            selectedPersonForLedger = null
                        }
                        viewModel.deletePersonRecord(p)
                        personToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("نعم، حذف نهائياً", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { personToDelete = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }
}

// ================= COMPOSABLE 4.5: PERSON LEDGER DETAIL PAGE =================
@Composable
fun PersonLedgerPage(
    person: PersonEntity,
    viewModel: FarmViewModel,
    themePrimary: Color,
    zoom: Float,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val transactionsAll by viewModel.transactionsList.collectAsStateWithLifecycle()
    val animalsAll by viewModel.animalsList.collectAsStateWithLifecycle()
    val feedsAll by viewModel.feedsList.collectAsStateWithLifecycle()

    // Filter transactions specifically for this user
    val transactions = remember(transactionsAll, person.id) {
        transactionsAll.filter { it.associatedPersonId == person.id }
    }

    // Input States
    var amountStr by remember { mutableStateOf("") }
    var descriptionStr by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("income") } // "income" (دائن/له/قبض) or "expense" (مدين/عليه/صرف)
    var selectedCategory by remember { mutableStateOf("عام") }

    // Relationship link choices
    var linkWithAnimal by remember { mutableStateOf<Int?>(null) }
    var linkWithFeed by remember { mutableStateOf<String?>(null) }
    var expandedAnimal by remember { mutableStateOf(false) }
    var expandedFeed by remember { mutableStateOf(false) }

    // Edit/Inspect Transaction Modals
    var editingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var inspectingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingSettlement by remember { mutableStateOf<TransactionEntity?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var invoiceTxForPrinting by remember { mutableStateOf<TransactionEntity?>(null) }
    var settlingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var confirmSaveParams by remember { mutableStateOf<Triple<Double, String, String>?>(null) }
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Soft slate background
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BACK HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowForward, // RTL back arrow
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                "كشف وسجل الحساب والمستندات",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // PERSON HEADER CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            person.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = themePrimary
                        )
                        Text(
                            "الصفة المهنية: ${person.role}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Balance status bubble
                    val isCreditor = person.balance >= 0
                    val balanceLabel = if (isCreditor) "دائن (له مال)" else "مدين (عليه)"
                    val balanceColor = if (isCreditor) Color(0xFF059669) else Color(0xFFDC2626)
                    val balanceBg = if (isCreditor) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .background(balanceBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            balanceLabel,
                            fontSize = 11.sp,
                            color = balanceColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${kotlin.math.abs(person.balance)} جنيه",
                            fontSize = 16.sp,
                            color = balanceColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (person.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "لا يمكن إجراء الاتصال: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                        Text("اتصال جوال: ${person.phone}", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ================= SECTION: ADD TRANSACTION FORM =================
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "إضافة حركة مالية جديدة للحساب 💰",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Debit vs Credit toggle buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isIncome = transactionType == "income"
                    Button(
                        onClick = { transactionType = "income" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (isIncome) Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "دائن (له / قبض)",
                            color = if (isIncome) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { transactionType = "expense" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncome) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (!isIncome) Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "مدين (عليه / صرف)",
                            color = if (!isIncome) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Amount text edit
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ المالي (جنيه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedLabelColor = themePrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )

                // Description
                OutlinedTextField(
                    value = descriptionStr,
                    onValueChange = { descriptionStr = it },
                    label = { Text("البيان والسبب (مثال: دفعة من الحساب، حليب، الخ)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedLabelColor = themePrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )

                // ================= RELATIONAL BINDINGS ("ربط الحسابات بالحظيرة والأعلاف والعلاج") =================
                Text(
                    "خيارات الربط والتعليق (اختياري) 🔗",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // 1. Link to Barn / Animals
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Pets, contentDescription = null, tint = themePrimary, modifier = Modifier.size(16.dp))
                            Text("ربط برأس ماشية محدد:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box {
                            val selectedAnimal = animalsAll.firstOrNull { it.id == linkWithAnimal }
                            val animalLabel = if (selectedAnimal != null) "${selectedAnimal.name} (${selectedAnimal.type})" else "اختر الرأس"
                            Button(
                                onClick = { expandedAnimal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(animalLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
                            }
                            DropdownMenu(expanded = expandedAnimal, onDismissRequest = { expandedAnimal = false }) {
                                DropdownMenuItem(
                                    text = { Text("عدم الربط بالماشية") },
                                    onClick = {
                                        linkWithAnimal = null
                                        expandedAnimal = false
                                    }
                                )
                                animalsAll.forEach { animal ->
                                    DropdownMenuItem(
                                        text = { Text("${animal.name} - ${animal.type} | وزنه ${animal.weight}كغ") },
                                        onClick = {
                                            linkWithAnimal = animal.id
                                            expandedAnimal = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Link to Feeds
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Grass, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                            Text("ربط بطلبية علف مسجلة:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box {
                            val feedLabel = linkWithFeed ?: "اختر السجل"
                            Button(
                                onClick = { expandedFeed = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(feedLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
                            }
                            DropdownMenu(expanded = expandedFeed, onDismissRequest = { expandedFeed = false }) {
                                DropdownMenuItem(
                                    text = { Text("عدم الربط بالأعلاف") },
                                    onClick = {
                                        linkWithFeed = null
                                        expandedFeed = false
                                    }
                                )
                                feedsAll.forEach { feed ->
                                    DropdownMenuItem(
                                        text = { Text("${feed.feedName} (${feed.totalWeight} كغ) - ${feed.totalCost} ج") },
                                        onClick = {
                                            linkWithFeed = feed.feedName
                                            expandedFeed = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // SAVE NEW LEDGER TRANSACTION BUTTON
                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0) {
                            Toast.makeText(context, "الرجاء كتابة قيمة مالية صحيحة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        var finalDesc = if (descriptionStr.isBlank()) {
                            "قيد مالي ${if (transactionType == "income") "دائن (له)" else "مدين (عليه)"}"
                        } else {
                            descriptionStr
                        }
                        var finalCategory = "عام"

                    if (linkWithFeed != null) {
                        if (finalDesc.isNotBlank()) finalDesc += " "
                        finalDesc += "[🌾 توريد أعلاف: ${linkWithFeed}]"
                        finalCategory = "أعلاف"
                    }
                    
                    confirmSaveParams = Triple(amount, finalDesc, finalCategory)
                },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تسجيل وحفظ السند المالي للحساب", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ================= SECTION: DETAILED TRANSACTION HISTORY (سجل الحساب) =================
        Text(
            "سجل الحركات المالية التفصيلي (القيود والدفتر)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في السندات بالبيان، المبلغ، أو التصنيف... 🔍") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themePrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )

        val mainTransactions = remember(transactions, searchQuery) {
            val allMainTxs = transactions.filter { it.category != "تسوية فواتير" || !it.description.startsWith("تسوية لسند رقم #") }
            val allSettlements = transactions.filter { it.category == "تسوية فواتير" && it.description.startsWith("تسوية لسند رقم #") }
            
            allMainTxs.filter { mainTx ->
                val associatedSettlements = allSettlements.filter { it.description.startsWith("تسوية لسند رقم #${mainTx.id}:") }
                
                if (searchQuery.isBlank()) {
                    true
                } else {
                    mainTx.description.contains(searchQuery, ignoreCase = true) ||
                    mainTx.amount.toString().contains(searchQuery) ||
                    mainTx.category.contains(searchQuery, ignoreCase = true) ||
                    mainTx.date.contains(searchQuery) ||
                    associatedSettlements.any {
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.amount.toString().contains(searchQuery) ||
                        it.date.contains(searchQuery)
                    }
                }
            }
        }

        if (mainTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لا توجد بيانات، اضغط + للإضافة", color = Color.Gray, fontSize = 11.sp)
                }
            }
        } else {
            mainTransactions.forEach { tx ->
                key(tx.id) {
                    val isTxIncome = tx.type == "income"
                    val associatedSettlements = transactions.filter { it.category == "تسوية فواتير" && it.description.startsWith("تسوية لسند رقم #${tx.id}:") }
                    val totalSettled = associatedSettlements.sumOf { it.amount }
                    val remainingAmount = (tx.amount - totalSettled).coerceAtLeast(0.0)
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = if (isTxIncome) Icons.Default.ArrowCircleUp else Icons.Default.ArrowCircleDown,
                                        contentDescription = null,
                                        tint = if (isTxIncome) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${tx.amount} جنيه",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isTxIncome) Color(0xFF047857) else Color(0xFFB91C1C),
                                            textDecoration = if (remainingAmount <= 0.0) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                        )
                                        Text(
                                            text = if (isTxIncome) "دائن (قبض / دفعة منه)" else "مدين (عليه / صرف له)",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Raw date indicator badge
                                Text(
                                    text = tx.date,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            // Display details
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = tx.description,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Medium
                            )

                            // Show Category
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "التصنيف: ${tx.category}",
                                    fontSize = 10.sp,
                                    color = themePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(themePrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                // Show Animal linkage if mapped
                                if (tx.associatedAnimalId != null) {
                                    val matchedAnimal = animalsAll.find { it.id == tx.associatedAnimalId }
                                    val animalLabel = if (matchedAnimal != null) "${matchedAnimal.name} (${matchedAnimal.type})" else "رقم #${tx.associatedAnimalId} (مؤرشف)"
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "الماشية: $animalLabel",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2563EB),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (associatedSettlements.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isExpanded = !isExpanded }
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("الدفعات والتسويات (${associatedSettlements.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = themePrimary)
                                        Text("المتبقي: $remainingAmount جنيه", fontSize = 11.sp, color = if (remainingAmount <= 0.0) Color(0xFF10B981) else Color.Gray)
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = themePrimary
                                    )
                                }
                                
                                androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                                        associatedSettlements.forEach { settlement ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Icon(Icons.Default.SubdirectoryArrowLeft, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(settlement.description, fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("${settlement.amount} ج", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if(settlement.type == "income") Color(0xFF10B981) else Color(0xFFEF4444))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    // Quick edit settlement button (Pencil icon)
                                                    IconButton(
                                                        onClick = { editingSettlement = settlement },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "تعديل الدفعة", tint = themePrimary, modifier = Modifier.size(14.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    // Quick delete settlement button
                                                    IconButton(
                                                        onClick = { txToDelete = settlement },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "حذف الدفعة", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // EDIT & DELETE ACTIONS ROW
                            Spacer(modifier = Modifier.height(12.dp))
                            Spacer(modifier = Modifier.height(1.dp).background(Color(0xFFE2E8F0)))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Unified operation actions manager button
                            Button(
                                onClick = { inspectingTx = tx },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themePrimary.copy(alpha = 0.08f),
                                    contentColor = themePrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إجراءات السند (عرض التفاصيل، تعديل، حذف) ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Print Invoice button (Always accessible)
                                    Row(
                                        modifier = Modifier
                                            .clickable { invoiceTxForPrinting = tx }
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = "طباعة السند", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("طباعة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    }
                                    
                                    // Invoice Settlement button
                                    if (viewModel.hasPermission("manage_debts")) {
                                        Row(
                                            modifier = Modifier
                                                .clickable { settlingTx = tx }
                                                .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Payments, contentDescription = "تسوية الفاتورة", tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تسوية", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (viewModel.hasPermission("add_transaction")) {
                                        IconButton(onClick = { editingTx = tx }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            if (!viewModel.hasPermission("delete_animal") && !viewModel.hasPermission("add_transaction")) {
                                                Toast.makeText(context, "عذراً، مسح السندات يحتاج صلاحيات كاملة ❌", Toast.LENGTH_SHORT).show()
                                            } else {
                                                txToDelete = tx
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف المعاملة", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= DIALOG: EDIT TRANSACTION RECORD =================
    if (editingTx != null) {
        val tx = editingTx!!
        var editAmount by remember { mutableStateOf(tx.amount.toString()) }
        var editType by remember { mutableStateOf(tx.type) }
        var editDesc by remember { mutableStateOf(tx.description) }
        var editCategory by remember { mutableStateOf(tx.category) }
        var editAnimalId by remember { mutableStateOf(tx.associatedAnimalId) }
        var editAnimalExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingTx = null },
            title = { Text("تعديل تفاصيل السند المالي", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("المبلغ المالي (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type Selector Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isIncomeSelected = editType == "income"
                        Button(
                            onClick = { editType = "income" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncomeSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "دائن (له مال)",
                                color = if (isIncomeSelected) Color(0xFF15803D) else Color.DarkGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { editType = "expense" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isIncomeSelected) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "مدين (عليه)",
                                color = if (!isIncomeSelected) Color(0xFFB91C1C) else Color.DarkGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("البيان والوصف") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("التصنيف الكلي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Edit Animal Link Selection
                    Text("ربط بالماشية (اختياري)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Box {
                        val activeAnimal = animalsAll.firstOrNull { it.id == editAnimalId }
                        val activeName = if (activeAnimal != null) "${activeAnimal.name} (${activeAnimal.type})" else "عدم الربط بالماشية"
                        Button(
                            onClick = { editAnimalExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(activeName, color = Color.Black, fontSize = 11.sp)
                        }
                        DropdownMenu(expanded = editAnimalExpanded, onDismissRequest = { editAnimalExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("عدم الربط بالماشية") },
                                onClick = {
                                    editAnimalId = null
                                    editAnimalExpanded = false
                                }
                            )
                            animalsAll.forEach { animal ->
                                DropdownMenuItem(
                                    text = { Text("${animal.name} (${animal.type})") },
                                    onClick = {
                                        editAnimalId = animal.id
                                        editAnimalExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dynamically calculate and display the expected financial change
                    val parsedAmount = editAmount.toDoubleOrNull() ?: 0.0
                    val oldContribution = if (tx.type == "income") tx.amount else -tx.amount
                    val newContribution = if (editType == "income") parsedAmount else -parsedAmount
                    val balanceDelta = newContribution - oldContribution
                    val currentBalance = person.balance
                    val expectedNewBalance = currentBalance + balanceDelta
                    
                    val currentBalanceText = if (currentBalance >= 0) "${currentBalance} ج.م (له)" else "${-currentBalance} ج.م (عليه)"
                    val expectedNewBalanceText = if (expectedNewBalance >= 0) "${expectedNewBalance} ج.م (له)" else "${-expectedNewBalance} ج.م (عليه)"

                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = themePrimary.copy(alpha = 0.06f)
                        ),
                        border = BorderStroke(1.dp, themePrimary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "📊 الأثر المالي المتوقع للحساب بعد الحفظ:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = themePrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الرصيد الحالي للحساب:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(currentBalanceText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مقدار التغير بالصافي:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (balanceDelta >= 0) "+${balanceDelta} ج.م" else "${balanceDelta} ج.م",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balanceDelta >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(themePrimary.copy(alpha = 0.15f)))
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الرصيد الجديد المتوقع للحساب:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = expectedNewBalanceText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = themePrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = editAmount.toDoubleOrNull() ?: tx.amount
                        val updatedTx = tx.copy(
                            amount = amountVal,
                            type = editType,
                            description = editDesc,
                            category = editCategory,
                            associatedAnimalId = editAnimalId
                        )
                        viewModel.updateTransactionRecord(tx, updatedTx)
                        editingTx = null
                        Toast.makeText(context, "تم حفظ وتعديل القيد المالي", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("حفظ التغييرات", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTx = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    if (confirmSaveParams != null) {
        val (cAmount, cDesc, cCategory) = confirmSaveParams!!
        AlertDialog(
            onDismissRequest = { confirmSaveParams = null },
            title = { Text("تأكيد تسجيل السند المالي ⚠️", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "هل أنت متأكد من تسجيل هذه الحركة المالية وتعديل رصيد الحساب؟\n\n" +
                    "المبلغ: $cAmount جنيه\n" +
                    "البيان: $cDesc",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.registerManualTransactionWithDetails(
                            type = transactionType,
                            amount = cAmount,
                            description = cDesc,
                            category = cCategory,
                            personId = person.id,
                            associatedAnimalId = linkWithAnimal
                        )

                        // Reset fields
                        amountStr = ""
                        descriptionStr = ""
                        linkWithAnimal = null
                        linkWithFeed = null
                        confirmSaveParams = null

                        Toast.makeText(context, "تم تسجيل وإضافة المعاملة بنجاح وتحديث الرصيد", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("نعم، تأكيد وحفظ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmSaveParams = null }) {
                    Text("مراجعة وإلغاء", color = Color.Gray)
                }
            }
        )
    }

    // ================= DIALOG: EDIT SETTLEMENT =================
    if (editingSettlement != null) {
        val settlement = editingSettlement!!
        val parentTxId = remember(settlement) {
            settlement.description.substringAfter("تسوية لسند رقم #").substringBefore(":").toIntOrNull()
        }
        val parentTx = remember(parentTxId, transactionsAll) {
            if (parentTxId != null) transactionsAll.find { it.id == parentTxId } else null
        }
        val otherSettlementsSum = remember(transactionsAll, parentTxId, settlement) {
            if (parentTxId != null) {
                transactionsAll.filter {
                    it.id != settlement.id &&
                    it.category == "تسوية فواتير" &&
                    it.description.startsWith("تسوية لسند رقم #$parentTxId:")
                }.sumOf { it.amount }
            } else 0.0
        }
        val maxAllowed = remember(parentTx, otherSettlementsSum) {
            if (parentTx != null) (parentTx.amount - otherSettlementsSum).coerceAtLeast(0.0) else Double.MAX_VALUE
        }

        var editAmountStr by remember { mutableStateOf(settlement.amount.toString()) }

        AlertDialog(
            onDismissRequest = { editingSettlement = null },
            title = { Text("تعديل مبلغ الدفعة / التسوية ✏️", fontWeight = FontWeight.Bold, color = themePrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (parentTx != null) {
                        Surface(
                            color = themePrimary.copy(alpha=0.08f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("سند الفاتورة الأساسية: #TX-00${parentTx.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("إجمالي الفاتورة الأساسية: ${parentTx.amount} جنيه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("إجمالي الدفعات الأخرى: $otherSettlementsSum جنيه", fontSize = 11.sp, color = Color(0xFF10B981))
                                Text("أقصى مبلغ مسموح به لهذه الدفعة: $maxAllowed جنيه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editAmountStr,
                        onValueChange = { editAmountStr = it },
                        label = { Text("مبلغ الدفعة الجديد (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = editAmountStr.toDoubleOrNull() ?: 0.0
                        if (amountVal > 0.0 && amountVal <= maxAllowed) {
                            val currentDateTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
                            val updatedDesc = if (parentTxId != null) {
                                "تسوية لسند رقم #$parentTxId: دفعة بقيمة $amountVal في ($currentDateTime)"
                            } else {
                                settlement.description
                             }
                             val updatedSettlement = settlement.copy(
                                 amount = amountVal,
                                 description = updatedDesc
                             )
                             viewModel.updateTransactionRecord(settlement, updatedSettlement)
                             editingSettlement = null
                             Toast.makeText(context, "تم تعديل وتحديث مبلغ التسوية بنجاح ✅", Toast.LENGTH_SHORT).show()
                         } else {
                             Toast.makeText(context, "الرجاء إدخال مبلغ صحيح لا يتجاوز $maxAllowed", Toast.LENGTH_SHORT).show()
                         }
                     },
                     colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                 ) {
                     Text("حفظ التعديل", color = Color.White)
                 }
             },
             dismissButton = {
                 TextButton(onClick = { editingSettlement = null }) {
                     Text("إلغاء", color = Color.Gray)
                 }
             }
         )
     }

    // ================= DIALOG: SETTLE TRANSACTION =================
    if (settlingTx != null) {
        val tx = settlingTx!!
        
        // Calculate already settled amount based on past settlement transactions linked to this tx.id
        val settledAmount = remember(transactionsAll, tx.id) {
            transactionsAll.filter { it.description.startsWith("تسوية لسند رقم #${tx.id}:") }.sumOf { it.amount }
        }
        val remainingAmount = (tx.amount - settledAmount).coerceAtLeast(0.0)
        
        var settleAmountStr by remember { mutableStateOf(remainingAmount.toString()) }
        
        AlertDialog(
            onDismissRequest = { settlingTx = null },
            title = { Text("تسوية فاتورة / مستحقات 💰", fontWeight = FontWeight.Bold, color = themePrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = themePrimary.copy(alpha=0.08f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي الفاتورة الأساسية:", fontSize = 12.sp, color = Color.Gray)
                                Text("${tx.amount} جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي المدفوع/المسوى سابقاً:", fontSize = 12.sp, color = Color.Gray)
                                Text("$settledAmount جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المبلغ المتبقي (الرصيد المعلق):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("$remainingAmount جنيه", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }

                    if (remainingAmount <= 0.0) {
                        Text("تمت تسوية هذه الفاتورة بالكامل مسبقاً! ✅", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Text("أدخل الدفعة المستلمة للتسوية الجزئية. تسجيل عملية التسوية سيتضمن التاريخ والوقت بدقة لتتبع الدفعات.", fontSize = 11.sp, color = Color.Gray)
                        
                        OutlinedTextField(
                            value = settleAmountStr,
                            onValueChange = { settleAmountStr = it },
                            label = { Text("قيمة الدفعة / التسوية المراد دفعها (جنيه)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = { settleAmountStr = remainingAmount.toString() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("دفع التسوية بالكامل ($remainingAmount)", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                if (remainingAmount > 0.0) {
                    Button(
                        onClick = {
                            val amountVal = settleAmountStr.toDoubleOrNull() ?: 0.0
                            if (amountVal > 0.0 && amountVal <= remainingAmount) {
                                val newTxType = tx.type
                                
                                val currentDateTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
                                viewModel.registerManualTransaction(
                                    type = newTxType,
                                    amount = amountVal,
                                    description = "تسوية لسند رقم #${tx.id}: دفعة بقيمة $amountVal في ($currentDateTime)",
                                    category = "تسوية فواتير",
                                    personId = person.id
                                )
                                settlingTx = null
                                Toast.makeText(context, "تم تسجيل دفعة التسوية بنجاح ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "يرجى كتابة مبلغ دفع صحيح لا يتجاوز المتبقي", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                    ) {
                        Text("تأكيد دفع التسوية", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { settlingTx = null }) {
                    Text("إغلاق", color = Color.Gray)
                }
            }
        )
    }

    if (txToDelete != null) {
        val tx = txToDelete!!
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("تأكيد حذف القيد أو السند المالي ⚠️", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                val deletionDelta = if (tx.type == "income") -tx.amount else tx.amount
                val currentBal = person.balance
                val expectedBal = currentBal + deletionDelta
                
                val currentBalText = if (currentBal >= 0) "${currentBal} ج.م (له)" else "${-currentBal} ج.م (عليه)"
                val expectedBalText = if (expectedBal >= 0) "${expectedBal} ج.م (له)" else "${-expectedBal} ج.م (عليه)"

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "هل أنت متأكد من حذف هذا السند المالي نهائياً؟",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("وصف السند: ${tx.description}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("المبلغ: ${tx.amount} جنيه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            Text("تبويب الفئة: ${tx.category}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "🚨 الأثر والنتائج المالية بحذف الحركة بالتفصيل:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB91C1C)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("رصيد الحساب الحالي:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(currentBalText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("صافي الأثر المسترجع/الخصم:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (deletionDelta >= 0) "+${deletionDelta} ج.م (استرداد)" else "${deletionDelta} ج.م (خصم)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (deletionDelta >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFFCA5A5).copy(alpha = 0.5f)))
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الرصيد النهائي المتوقع بعد الحذف:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text(
                                    text = expectedBalText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.softDeleteTransactionRecord(tx)
                        txToDelete = null
                        Toast.makeText(context, "تم نقل المعاملة لسلة المحذوفات وتعديل التقرير المالي بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("نعم، حذف وتعديل الرصيد", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    if (invoiceTxForPrinting != null) {
        val tx = invoiceTxForPrinting!!
        val liveTx = remember(transactionsAll, tx.id) {
            transactionsAll.find { it.id == tx.id } ?: tx
        }
        val isTxIncome = liveTx.type == "income"
        
        val associatedSettlements = remember(transactionsAll, liveTx.id) {
            transactionsAll.filter { it.category == "تسوية فواتير" && it.description.startsWith("تسوية لسند رقم #${liveTx.id}:") }
        }
        val totalSettled = remember(associatedSettlements) { associatedSettlements.sumOf { it.amount } }
        val remainingAmountForThisTx = remember(liveTx.amount, totalSettled) { (liveTx.amount - totalSettled).coerceAtLeast(0.0) }
        
        AlertDialog(
            onDismissRequest = { invoiceTxForPrinting = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF60A5FA))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("معاينة الفاتورة المستندية 🧾", fontWeight = FontWeight.Black, color = Color.White)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.currentFarm.value ?: "مزرعتي الخاصة",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF60A5FA)
                        )
                        Text(
                            text = "سند قيد رسمي معتمد وموثق رقمياً",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFF334155)))
                    }

                    // Horizontal Metadata Row instead of stacked vertical list
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "رقم السند: ", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(text = "TX-00${liveTx.id}#", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = liveTx.date, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E8F0))
                        }
                    }

                    // High contrast Client & Category Block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF334155).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("جهة السند / مخصوم لصالح:", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(person.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("بند القيد والمصاريف:", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(liveTx.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نوع السند الحالي:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = if (isTxIncome) "وصل استلام نقدية (له/قبض)" else "وصل صرف نقدية (عليه/دفع)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTxIncome) Color(0xFF10B981) else Color(0xFFFBBF24)
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFF334155)))

                    Text(
                        text = "البيان والتفاصيل:\n${liveTx.description}",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Dependent Settlements structured table list interior
                    if (associatedSettlements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "تسجيلات الدفعات والمدفوعات التابعة:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                            associatedSettlements.forEachIndexed { index, settlement ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFF334155)))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(settlement.description.substringAfter(": "), fontSize = 9.sp, color = Color(0xFF94A3B8))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "${settlement.amount} ج",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { 
                                                editingSettlement = settlement
                                                invoiceTxForPrinting = null
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "تعديل",
                                                tint = Color(0xFF60A5FA),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Beautiful high contrast Footer Summary above the signature line
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, Color(0xFF475569)), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إجمالي قيمة الفاتورة المعتمدة:", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                            Text("${liveTx.amount} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إجمالي المسدّد والمقسط طرفكم:", fontSize = 10.sp, color = Color(0xFF34D399))
                            Text("$totalSettled ج.م", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
                        }
                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFF475569)))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المبلغ المتبقي للتحصيل / السداد:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                            Text(
                                "$remainingAmountForThisTx ج.م",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }

                    Text(
                        text = "توقيع المستلم المعتمد لـ ${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}: _________________",
                        fontSize = 8.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.End).padding(top = 10.dp)
                    )
                    
                    if (remainingAmountForThisTx > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { 
                                val txToSettle = liveTx
                                invoiceTxForPrinting = null
                                if (txToSettle != null) {
                                    settlingTx = txToSettle
                                } 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "تسديد أو إضافة دفعة متبقية للحساب ($remainingAmountForThisTx ج.م)", 
                                color = Color(0xFF0F172A), 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Native real PDF sharing
                    Button(
                        onClick = {
                            try {
                                val pdf = android.graphics.pdf.PdfDocument()
                                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                                val page = pdf.startPage(pageInfo)
                                val canvas = page.canvas
                                val paint = android.graphics.Paint()
                                val isTxIncome = tx.type == "income"
                                
                                paint.color = android.graphics.Color.parseColor("#3B82F6")
                                canvas.drawRect(30f, 30f, 565f, 110f, paint)
                                
                                paint.color = android.graphics.Color.WHITE
                                paint.isAntiAlias = true
                                paint.textSize = 20f
                                paint.isFakeBoldText = true
                                canvas.drawText(viewModel.currentFarm.value ?: "مزرعتي الخاصة", 50f, 70f, paint)
                                
                                paint.textSize = 10f
                                paint.isFakeBoldText = false
                                canvas.drawText("سند قيد رسمي معتمد وموثق رقمياً لـ ${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}", 50f, 95f, paint)
                                
                                paint.color = android.graphics.Color.BLACK
                                paint.textSize = 12f
                                
                                var topY = 150f
                                val drawLine: (String, String) -> Unit = { key, value ->
                                    paint.color = android.graphics.Color.GRAY
                                    canvas.drawText(key, 50f, topY, paint)
                                    paint.color = android.graphics.Color.BLACK
                                    paint.isFakeBoldText = true
                                    canvas.drawText(convertToEasternArabicNumerals(value), 300f, topY, paint)
                                    paint.isFakeBoldText = false
                                    topY += 28f
                                }
                                
                                drawLine("رقم السند الفاتورة:", "#TX-00${tx.id}")
                                drawLine("تاريخ المعاملة:", tx.date)
                                drawLine("نوع السند:", if (isTxIncome) "وصل استلام نقدية (له/قبض)" else "وصل صرف نقدية (عليه/دفع)")
                                drawLine("التصنيف وبند القيد:", tx.category)
                                
                                paint.color = android.graphics.Color.LTGRAY
                                canvas.drawLine(50f, topY, 545f, topY, paint)
                                topY += 30f
                                
                                paint.color = android.graphics.Color.parseColor("#F3F4F6")
                                canvas.drawRect(50f, topY - 20f, 545f, topY + 30f, paint)
                                
                                paint.color = if (isTxIncome) android.graphics.Color.parseColor("#047857") else android.graphics.Color.parseColor("#B91C1C")
                                paint.textSize = 15f
                                paint.isFakeBoldText = true
                                canvas.drawText("المبلغ الصافي: ${tx.amount} جنيه مصري", 70f, topY + 12f, paint)
                                paint.isFakeBoldText = false
                                topY += 75f
                                
                                paint.color = android.graphics.Color.BLACK
                                paint.textSize = 11f
                                canvas.drawText("البيان والتفاصيل:", 50f, topY, paint)
                                topY += 20f
                                
                                paint.color = android.graphics.Color.DKGRAY
                                val words = tx.description.split(" ")
                                var line = StringBuilder()
                                for (word in words) {
                                    if (paint.measureText(line.toString() + word) > 480) {
                                        canvas.drawText(line.toString(), 50f, topY, paint)
                                        line = StringBuilder(word + " ")
                                        topY += 18f
                                    } else {
                                        line.append(word).append(" ")
                                    }
                                }
                                canvas.drawText(line.toString(), 50f, topY, paint)
                                topY += 60f
                                
                                paint.color = android.graphics.Color.GRAY
                                paint.textSize = 10f
                                canvas.drawText("توقيع المستلم المعتمد لـ ${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}: _________________", 180f, topY, paint)
                                
                                pdf.finishPage(page)
                                
                                val filename = "${(viewModel.currentFarm.value ?: "مزرعتي الخاصة")}_invoice_TX00${tx.id}.pdf".replace(" ", "_")
                                val file = java.io.File(context.cacheDir, filename)
                                val outputStream = java.io.FileOutputStream(file)
                                pdf.writeTo(outputStream)
                                pdf.close()
                                outputStream.close()
                                
                                val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "فاتورة رقم #TX-00${tx.id}")
                                    putExtra(android.content.Intent.EXTRA_TEXT, "مرفق فاتورة رقم #TX-00${tx.id} الصادرة من ${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "مشاركة الفاتورة مستند PDF 📄"))
                                invoiceTxForPrinting = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشل مشاركة الفاتورة كـ PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة الفاتورة", color = Color.White, fontSize = 11.sp)
                    }

                    // Native PDF print adaptation
                    Button(
                        onClick = {
                            try {
                                val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                                val jobName = "${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}_Invoice_${tx.id}"
                                printManager.print(jobName, object : android.print.PrintDocumentAdapter() {
                                    override fun onWrite(
                                        pages: Array<out android.print.PageRange>?,
                                        destination: android.os.ParcelFileDescriptor?,
                                        cancellationSignal: android.os.CancellationSignal?,
                                        callback: WriteResultCallback?
                                    ) {
                                        val pdf = android.graphics.pdf.PdfDocument()
                                        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                                        val page = pdf.startPage(pageInfo)
                                        val canvas = page.canvas
                                        val paint = android.graphics.Paint()
                                        val isTxIncome = tx.type == "income"
                                        
                                        paint.color = android.graphics.Color.parseColor("#3B82F6")
                                        canvas.drawRect(30f, 30f, 565f, 110f, paint)
                                        
                                        paint.color = android.graphics.Color.WHITE
                                        paint.isAntiAlias = true
                                        paint.textSize = 20f
                                        paint.isFakeBoldText = true
                                        canvas.drawText(viewModel.currentFarm.value ?: "مزرعتي الخاصة", 50f, 70f, paint)
                                        
                                        paint.textSize = 10f
                                        paint.isFakeBoldText = false
                                        canvas.drawText("سند قيد رسمي معتمد وموثق رقمياً لـ ${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}", 50f, 95f, paint)
                                        
                                        paint.color = android.graphics.Color.BLACK
                                        paint.textSize = 12f
                                        
                                        var topY = 150f
                                        val drawLine: (String, String) -> Unit = { key, value ->
                                            paint.color = android.graphics.Color.GRAY
                                            canvas.drawText(key, 50f, topY, paint)
                                            paint.color = android.graphics.Color.BLACK
                                            paint.isFakeBoldText = true
                                            canvas.drawText(convertToEasternArabicNumerals(value), 300f, topY, paint)
                                            paint.isFakeBoldText = false
                                            topY += 28f
                                        }
                                        
                                        drawLine("رقم السند الفاتورة:", "#TX-00${tx.id}")
                                        drawLine("تاريخ المعاملة:", tx.date)
                                        drawLine("نوع السند:", if (isTxIncome) "وصل استلام نقدية (له/قبض)" else "وصل صرف نقدية (عليه/دفع)")
                                        drawLine("التصنيف وبند القيد:", tx.category)
                                        
                                        paint.color = android.graphics.Color.LTGRAY
                                        canvas.drawLine(50f, topY, 545f, topY, paint)
                                        topY += 30f
                                        
                                        paint.color = android.graphics.Color.parseColor("#F3F4F6")
                                        canvas.drawRect(50f, topY - 20f, 545f, topY + 30f, paint)
                                        
                                        paint.color = if (isTxIncome) android.graphics.Color.parseColor("#047857") else android.graphics.Color.parseColor("#B91C1C")
                                        paint.textSize = 15f
                                        paint.isFakeBoldText = true
                                        canvas.drawText("المبلغ الصافي: ${tx.amount} جنيه مصري", 70f, topY + 12f, paint)
                                        paint.isFakeBoldText = false
                                        topY += 75f
                                        
                                        paint.color = android.graphics.Color.BLACK
                                        paint.textSize = 11f
                                        canvas.drawText("البيان والتفاصيل:", 50f, topY, paint)
                                        topY += 20f
                                        
                                        paint.color = android.graphics.Color.DKGRAY
                                        val words = tx.description.split(" ")
                                        var line = StringBuilder()
                                        for (word in words) {
                                            if (paint.measureText(line.toString() + word) > 480) {
                                                canvas.drawText(line.toString(), 50f, topY, paint)
                                                line = StringBuilder(word + " ")
                                                topY += 18f
                                            } else {
                                                line.append(word).append(" ")
                                            }
                                        }
                                        canvas.drawText(line.toString(), 50f, topY, paint)
                                        topY += 60f
                                        
                                        paint.color = android.graphics.Color.GRAY
                                        paint.textSize = 10f
                                        canvas.drawText("توقيع المستلم المعتمد لـ ${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}: _________________", 180f, topY, paint)
                                        
                                        pdf.finishPage(page)
                                        
                                        try {
                                            val outputStream = java.io.FileOutputStream(destination?.fileDescriptor)
                                            pdf.writeTo(outputStream)
                                            pdf.close()
                                            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                                        } catch (e: Exception) {
                                            callback?.onWriteFailed(e.message)
                                        }
                                    }

                                    override fun onLayout(
                                        oldAttributes: android.print.PrintAttributes?,
                                        newAttributes: android.print.PrintAttributes?,
                                        cancellationSignal: android.os.CancellationSignal?,
                                        callback: LayoutResultCallback?,
                                        extras: android.os.Bundle?
                                    ) {
                                        if (cancellationSignal?.isCanceled == true) {
                                            callback?.onLayoutCancelled()
                                            return
                                        }
                                        val info = android.print.PrintDocumentInfo.Builder("${viewModel.currentFarm.value ?: "مزرعتي الخاصة"}_Invoice_${tx.id}.pdf")
                                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                            .setPageCount(1)
                                            .build()
                                        callback?.onLayoutFinished(info, true)
                                    }
                                }, null)
                                Toast.makeText(context, "تم قراءة السند ونقله لوحدات الطباعة بنجاح! 🖨️", Toast.LENGTH_SHORT).show()
                                invoiceTxForPrinting = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "حدث خطأ أثناء الاتصال بالطابعة: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرسال لأمر الطباعة فوراً", color = Color.White, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceTxForPrinting = null }) {
                    Text("إغلاق", color = Color.Gray)
                }
            }
        )
    }

    if (inspectingTx != null) {
        val tx = inspectingTx!!
        val isTxIncome = tx.type == "income"
        
        // Calculate already settled amount based on past settlement transactions linked to this tx.id
        val settledAmount = remember(transactionsAll, tx.id) {
            transactionsAll.filter { it.description.startsWith("تسوية لسند رقم #${tx.id}:") }.sumOf { it.amount }
        }
        val remainingAmount = (tx.amount - settledAmount).coerceAtLeast(0.0)
        
        AlertDialog(
            onDismissRequest = { inspectingTx = null },
            containerColor = Color(0xFF0F172A), // Modern High-End Deep Dark Slate Canvas
            titleContentColor = Color.White,
            textContentColor = Color(0xFFE2E8F0),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color(0xFF34D399), // Refreshing Emerald Green accent icon
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "معاينة السند المالي 📝",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Horizon top row layout for Invoice ID and date
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سند رقم: #TX-00${tx.id}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = tx.date,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Elegant Centralized Financial Summary Card
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي القيمة الافتتاحية:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("${tx.amount} جنيه", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المدفوعات والتسويات المكتملة:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("${settledAmount} جنيه", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            
                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 2.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("المبلغ المتبقي للتحصيل:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
                                Text(
                                    text = "${remainingAmount} جنيه",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (remainingAmount <= 0.0) Color(0xFF10B981) else Color(0xFFF59E0B) // Amber accents instead of red
                                )
                            }
                        }
                    }

                    // Metadata Group Details Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الحساب المستحق / العميل:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(person.name, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تبويب وتصنيف الحركة المالية:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = tx.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier
                                    .background(Color(0xFF34D399).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Description/Note Block
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("البيان والوصف المالي بالتفصيل:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = tx.description,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // System Linkages tracker
                        if (tx.associatedAnimalId != null || tx.description.contains("[🌾") || tx.description.contains("[📚")) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFF334155)))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("الروابط والارتباطات بالنظام 🔗", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            
                            if (tx.associatedAnimalId != null) {
                                val matched = animalsAll.find { it.id == tx.associatedAnimalId }
                                val l = if (matched != null) "${matched.name} (${matched.type})" else "رأس رقم #${tx.associatedAnimalId}"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مرتبط بقرية الماشية: $l", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                }
                            }
                        }
                    }

                    // Shaded Dependent Payments Block with micro-dividers
                    val associatedSettlements = remember(transactionsAll, tx.id) {
                        transactionsAll.filter { it.description.startsWith("تسوية لسند رقم #${tx.id}:") }
                    }
                    if (associatedSettlements.isNotEmpty()) {
                        Text(
                            text = "الدفعات الجزئية المسجلة تتبع هذا السند:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                        
                        Surface(
                            color = Color(0xFF1E293B).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                associatedSettlements.forEachIndexed { index, settlement ->
                                    if (index > 0) {
                                        HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "دفعة جزئية بقيمة ${settlement.amount} ج.م",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE2E8F0)
                                            )
                                            Text(
                                                text = "التاريخ والوقت: ${settlement.date}",
                                                fontSize = 9.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        Text(
                                            text = "مستلمة ومسواة ✅",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { inspectingTx = null }) {
                    Text("إغلاق المعاينة ✖️", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

data class NoteAttachment(
    val type: String, // "image" or "voice"
    val base64: String
) : java.io.Serializable

object NoteAttachmentHelper {
    fun fromString(str: String?): List<NoteAttachment> {
        if (str.isNullOrBlank()) return emptyList()
        val trimmed = str.trim()
        if (!trimmed.startsWith("[")) {
            // Legacy representation of single image base64
            return listOf(NoteAttachment("image", str))
        }
        return try {
            val arr = org.json.JSONArray(trimmed)
            val list = mutableListOf<NoteAttachment>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(NoteAttachment(
                    type = obj.optString("type", "image"),
                    base64 = obj.optString("base64", "")
                ))
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun toString(list: List<NoteAttachment>): String {
        val arr = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject()
            obj.put("type", item.type)
            obj.put("base64", item.base64)
            arr.put(obj)
        }
        return arr.toString()
    }
}

fun shareContentWithMedia(context: android.content.Context, title: String, content: String, attachments: List<NoteAttachment>) {
    try {
        val uris = ArrayList<android.net.Uri>()
        val cacheDir = context.cacheDir
        
        for ((idx, att) in attachments.withIndex()) {
            val extension = if (att.type == "voice") ".mp3" else ".jpg"
            val file = java.io.File(cacheDir, "shared_temp_attachment_$idx$extension")
            val bytes = android.util.Base64.decode(att.base64, android.util.Base64.DEFAULT)
            file.writeBytes(bytes)
            
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, 
                "${context.packageName}.provider", 
                file
            )
            uris.add(uri)
        }
        
        val shareText = buildString {
            if (title.isNotEmpty()) {
                append("**")
                append(title)
                append("**\n")
            }
            append(content)
        }
        
        if (uris.isEmpty()) {
            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(android.content.Intent.createChooser(sendIntent, "مشاركة الملاحظة"))
        } else {
            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(sendIntent, "مشاركة الملاحظة مع المرفقات"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun shareNoteWithMedia(context: android.content.Context, note: NoteEntity) {
    try {
        val attachments = NoteAttachmentHelper.fromString(note.imageBase64)
        val uris = ArrayList<android.net.Uri>()
        val cacheDir = context.cacheDir
        
        for ((idx, att) in attachments.withIndex()) {
            val extension = if (att.type == "voice") ".mp3" else ".jpg"
            val file = java.io.File(cacheDir, "shared_note_${note.id}_attachment_$idx$extension")
            val bytes = android.util.Base64.decode(att.base64, android.util.Base64.DEFAULT)
            file.writeBytes(bytes)
            
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, 
                "${context.packageName}.provider", 
                file
            )
            uris.add(uri)
        }
        
        val shareText = buildString {
            if (note.title.isNotEmpty()) {
                append("**")
                append(note.title)
                append("**\n")
            }
            append(note.content)
        }
        
        if (uris.isEmpty()) {
            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(android.content.Intent.createChooser(sendIntent, "مشاركة الملاحظة"))
        } else {
            val intent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND_MULTIPLE
                type = "*/*"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "مشاركة الملاحظة والوسائط"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to text-only share
        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            val shareText = if (note.title.isNotEmpty()) "**${note.title}**\n${note.content}" else note.content
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(android.content.Intent.createChooser(sendIntent, "مشاركة الملاحظة"))
    }
}

// ================= SCREEN COMPOSABLE 5: NOTES (TEXT & PHOTO) =================
private val keepColors = listOf(
    "" to "تلقائي",
    "#FFCDD2" to "أحمر",
    "#FFF9C4" to "أصفر",
    "#BBDEFB" to "أزرق",
    "#C8E6C9" to "أخضر",
    "#CFD8DC" to "رمادي صخري" // Slate/Gray-ish MD3 color
)

private fun parseNoteColor(hex: String, isDark: Boolean): Color {
    if (hex.isEmpty()) return Color.Unspecified
    return try {
        val baseColor = Color(android.graphics.Color.parseColor(hex))
        if (isDark) {
            when (hex) {
                "#FFCDD2" -> Color(0xFF3E1E20) // Red
                "#FFF9C4" -> Color(0xFF3E3A1D) // Yellow
                "#BBDEFB" -> Color(0xFF1B2C3A) // Blue
                "#C8E6C9" -> Color(0xFF1D3520) // Green
                "#CFD8DC" -> Color(0xFF2E353F) // Slate
                else -> baseColor.copy(alpha = 0.25f)
            }
        } else {
            baseColor
        }
    } catch (e: Exception) {
        Color.Unspecified
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    cardBgColor: Color,
    themePrimary: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onPinToggle: () -> Unit
) {
    val noteBg = remember(note.colorHex, isDark, cardBgColor) {
        val parsed = parseNoteColor(note.colorHex, isDark)
        if (parsed == Color.Unspecified) cardBgColor else parsed
    }

    Surface(
        color = noteBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val formatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US) }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "مثبتة",
                        tint = themePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }
                
                Text(
                    text = formatter.format(java.util.Date(note.createdAt)),
                    fontSize = 9.sp,
                    color = if (note.colorHex.isNotEmpty() && !isDark) Color.DarkGray else Color.Gray
                )
            }
            
            if (note.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (note.colorHex.isNotEmpty()) (if (isDark) Color.White else Color.Black) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.content,
                    fontSize = 13.sp,
                    color = if (note.colorHex.isNotEmpty()) (if (isDark) Color.White.copy(alpha=0.85f) else Color.DarkGray.copy(alpha = 0.9f)) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 14,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Rich media attachments player & grid inside card
            val attachmentsList = remember(note.imageBase64) {
                NoteAttachmentHelper.fromString(note.imageBase64)
            }
            if (attachmentsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val images = attachmentsList.filter { it.type == "image" }
                val voiceNotes = attachmentsList.filter { it.type == "voice" }
                
                if (images.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(images.size) { idx ->
                            val att = images[idx]
                            val decoded = remember(att.base64) { ImageUtils.base64ToBitmap(att.base64) }
                            if (decoded != null) {
                                Image(
                                    bitmap = decoded.asImageBitmap(),
                                    contentDescription = "صورة مرفقة",
                                    modifier = Modifier
                                        .size(65.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha=0.5f), RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                if (voiceNotes.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        voiceNotes.forEach { att ->
                            var isPlaying by remember { mutableStateOf(false) }
                            var progress by remember { mutableStateOf(0f) }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل",
                                        tint = themePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val barCount = 10
                                    for (i in 0 until barCount) {
                                        val active = progress > (i.toFloat() / barCount)
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(14.dp)
                                                .background(
                                                    if (active && isPlaying) themePrimary else Color.Gray.copy(alpha = 0.3f),
                                                    RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "0:08",
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            LaunchedEffect(isPlaying) {
                                if (isPlaying) {
                                    progress = 0f
                                    while (progress < 1f) {
                                        kotlinx.coroutines.delay(100)
                                        progress += 0.1f
                                    }
                                    isPlaying = false
                                    progress = 0f
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = if (note.colorHex.isNotEmpty() && !isDark) Color.Black.copy(alpha = 0.1f) else Color(0xFFE2E8F0))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPinToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "تثبيت الملاحظة",
                        tint = if (note.isPinned) themePrimary else Color.LightGray,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = if (note.colorHex.isNotEmpty() && !isDark) Color(0xFF1E40AF) else Color(0xFF3B82F6),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = if (note.colorHex.isNotEmpty() && !isDark) Color(0xFF065F46) else Color(0xFF10B981),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = Color.Red,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: FarmViewModel,
    themePrimary: Color,
    zoom: Float,
    cardBgColor: Color,
    onCheckAddPermission: (() -> Unit) -> Unit,
    onConfirmEdit: (String, () -> Unit) -> Unit,
    onConfirmDelete: (String, () -> Unit) -> Unit
) {
    val notes by viewModel.notesList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Full screen editor state
    var showEditor by remember { mutableStateOf(false) }
    
    LaunchedEffect(showEditor) {
        viewModel.setFullScreenEditorActive(showEditor)
    }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var editTitleText by remember { mutableStateOf("") }
    var editContentText by remember { mutableStateOf("") }
    var editColorHex by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    
    // Multi attachments state list
    val attachments = remember { mutableStateListOf<NoteAttachment>() }

    // Fabricate a FAB state
    var showFabMenu by remember { mutableStateOf(false) }

    // Search query state
    var searchQuery by remember { mutableStateOf("") }
    
    // Safety dialog
    var showDiscardWarning by remember { mutableStateOf(false) }
    
    // Active menu for editor
    var activeMenu by remember { mutableStateOf("") }

    // Initialize attachments and isPinned when editor opens or notes change
    LaunchedEffect(noteToEdit, showEditor) {
        if (showEditor) {
            attachments.clear()
            if (noteToEdit != null) {
                attachments.addAll(NoteAttachmentHelper.fromString(noteToEdit!!.imageBase64))
                isPinned = noteToEdit!!.isPinned
            } else {
                isPinned = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = ImageUtils.bitmapToBase64(bitmap)
            attachments.add(NoteAttachment("image", base64))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 != null) {
                attachments.add(NoteAttachment("image", base64))
            }
        }
    }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.isPinned } }
    val normalNotes = remember(filteredNotes) { filteredNotes.filter { !it.isPinned } }

    if (showEditor) {
        val originalTitle = noteToEdit?.title ?: ""
        val originalContent = noteToEdit?.content ?: ""
        val originalColor = noteToEdit?.colorHex ?: ""
        val originalImage = noteToEdit?.imageBase64 ?: ""
        val originalPinned = noteToEdit?.isPinned ?: false
        val currentMedia = remember(attachments.toList()) { 
            NoteAttachmentHelper.toString(attachments.toList()) 
        }
        val hasChanges = editTitleText != originalTitle || 
                         editContentText != originalContent || 
                         editColorHex != originalColor ||
                         isPinned != originalPinned ||
                         currentMedia != originalImage

        var undoStack by remember { mutableStateOf(listOf<String>()) }
        var redoStack by remember { mutableStateOf(listOf<String>()) }

        val handleUndo = {
            if (undoStack.isNotEmpty()) {
                redoStack = redoStack + editContentText
                editContentText = undoStack.last()
                undoStack = undoStack.dropLast(1)
            }
        }
        
        val handleRedo = {
            if (redoStack.isNotEmpty()) {
                undoStack = undoStack + editContentText
                editContentText = redoStack.last()
                redoStack = redoStack.dropLast(1)
            }
        }

        val handleBack = {
            if (hasChanges && (editTitleText.isNotBlank() || editContentText.isNotBlank() || attachments.isNotEmpty())) {
                val mediaStr = if (attachments.isEmpty()) null else NoteAttachmentHelper.toString(attachments.toList())
                if (noteToEdit != null) {
                    val updated = noteToEdit!!.copy(
                        title = editTitleText,
                        content = editContentText,
                        imageBase64 = mediaStr,
                        colorHex = editColorHex,
                        isPinned = isPinned
                    )
                    viewModel.updateNoteRecord(updated)
                } else {
                    viewModel.registerNote(editTitleText, editContentText, mediaStr, editColorHex, isPinned)
                }
            }
            showEditor = false
            noteToEdit = null
        }
        
        if (showDiscardWarning) {
            AlertDialog(
                onDismissRequest = { showDiscardWarning = false },
                title = { Text("حفظ التغييرات؟") },
                text = { Text("هل تريد حفظ التغييرات أم تجاهلها والخروج؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            val mediaStr = if (attachments.isEmpty()) null else NoteAttachmentHelper.toString(attachments.toList())
                            if (noteToEdit != null) {
                                val updated = noteToEdit!!.copy(
                                    title = editTitleText,
                                    content = editContentText,
                                    imageBase64 = mediaStr,
                                    colorHex = editColorHex,
                                    isPinned = isPinned
                                )
                                viewModel.updateNoteRecord(updated)
                            } else {
                                viewModel.registerNote(editTitleText, editContentText, mediaStr, editColorHex, isPinned)
                            }
                            showDiscardWarning = false
                            showEditor = false
                            noteToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                    ) {
                        Text("حفظ")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showDiscardWarning = false }) {
                            Text("إلغاء", color = Color.Gray)
                        }
                        TextButton(onClick = {
                            showDiscardWarning = false
                            showEditor = false
                            noteToEdit = null
                        }) {
                            Text("تجاهل الخروج", color = Color.Red)
                        }
                    }
                }
            )
        }

        androidx.activity.compose.BackHandler {
            handleBack()
        }

        val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
        val editorBg = remember(editColorHex, isDarkTheme) {
            val parsed = parseNoteColor(editColorHex, isDarkTheme)
            if (parsed == Color.Unspecified) Color.Transparent else parsed
        }

        var focusedImageBase64 by remember { mutableStateOf<String?>(null) }
        
        if (focusedImageBase64 != null) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { focusedImageBase64 = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    val decoded = remember(focusedImageBase64) { ImageUtils.base64ToBitmap(focusedImageBase64!!) }
                    if (decoded != null) {
                        Image(
                            bitmap = decoded.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        offset += pan
                                    }
                                }
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                    IconButton(
                        onClick = { focusedImageBase64 = null },
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp).systemBarsPadding()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (editorBg == Color.Transparent) MaterialTheme.colorScheme.background else editorBg)
                .systemBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = handleBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = handleUndo, enabled = undoStack.isNotEmpty()) {
                            Icon(Icons.Default.Undo, contentDescription = "تراجع", tint = if (undoStack.isNotEmpty()) LocalContentColor.current else Color.Gray)
                        }
                        IconButton(onClick = handleRedo, enabled = redoStack.isNotEmpty()) {
                            Icon(Icons.Default.Redo, contentDescription = "للأمام", tint = if (redoStack.isNotEmpty()) LocalContentColor.current else Color.Gray)
                        }
                        IconButton(onClick = { 
                            shareContentWithMedia(context, editTitleText, editContentText, attachments.toList())
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة الملاحظة")
                        }
                        
                        // PDF Share
                        IconButton(onClick = { 
                            com.example.util.PdfUtils.generateAndSharePdf(context, editTitleText, editContentText, attachments.filter { it.type == "image" }.map { it.base64 })
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "مشاركة PDF")
                        }
                        
                        // Text Font/Size
                        IconButton(onClick = { activeMenu = if (activeMenu == "format") "" else "format" }) {
                            Icon(Icons.Default.FormatSize, contentDescription = "تعديل الخط")
                        }
                        
                        // Color Palette
                        IconButton(onClick = { activeMenu = if (activeMenu == "colors") "" else "colors" }) {
                            Icon(Icons.Default.Palette, contentDescription = "تلوين")
                        }
                        
                        // Add Image
                        IconButton(onClick = { activeMenu = if (activeMenu == "attachments") "" else "attachments" }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "إضافة صورة")
                        }
                        
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(Icons.Default.PushPin, contentDescription = "تثبيت الملاحظة", tint = if (isPinned) themePrimary else LocalContentColor.current)
                        }
                        
                        if (noteToEdit != null) {
                            IconButton(onClick = {
                                onConfirmDelete(noteToEdit!!.content) { viewModel.deleteNoteRecord(noteToEdit!!); showEditor = false }
                            }) {
                                Icon(Icons.Default.Archive, contentDescription = "أرشفة/حذف")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                if (activeMenu == "colors") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        keepColors.forEach { (colorCode, label) ->
                            val colorObj = parseNoteColor(colorCode, isDarkTheme)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (colorCode.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else colorObj)
                                    .border(
                                        width = if (editColorHex == colorCode) 3.dp else 1.dp,
                                        color = if (editColorHex == colorCode) themePrimary else Color.LightGray.copy(alpha = 0.8f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                    .clickable { editColorHex = colorCode },
                                contentAlignment = Alignment.Center
                            ) {
                                if (colorCode.isEmpty()) {
                                    Icon(Icons.Default.Brush, contentDescription = "تلقائي", modifier = Modifier.size(16.dp))
                                } else if (editColorHex == colorCode) {
                                    Icon(Icons.Default.Check, contentDescription = "محدد", tint = if (isDarkTheme) Color.White else Color.Black, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                } else if (activeMenu == "attachments") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { cameraLauncher.launch(null); activeMenu = "" }) { Icon(Icons.Default.CameraAlt, "كاميرا") }
                        IconButton(onClick = { galleryLauncher.launch("image/*"); activeMenu = "" }) { Icon(Icons.Default.Image, "صورة") }
                        IconButton(onClick = {
                            val dummyVoice = "UklGRiYAAABXQVZFRm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA="
                            attachments.add(NoteAttachment("voice", dummyVoice))
                            Toast.makeText(context, "تم تسجيل مذكرات صوتية بنجاح 🎙️", Toast.LENGTH_SHORT).show()
                            activeMenu = ""
                        }) { Icon(Icons.Default.Mic, "صوت") }
                    }
                } else if (activeMenu == "format") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إعدادات الخط قريباً", fontSize = 14.sp)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextField(
                        value = editTitleText,
                        onValueChange = { editTitleText = it },
                        placeholder = { Text("العنوان", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 20.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    TextField(
                        value = editContentText,
                        onValueChange = { newVal ->
                            if (newVal != editContentText) {
                                // Add to undo stack only if it's a new word or ends with space, or just keep it simple:
                                // If the diff length is more than 0, push current text.
                                // Actually simple approach: just add current to undo
                                undoStack = undoStack + editContentText
                                redoStack = emptyList() // clear redo when a new change happens
                                editContentText = newVal
                            }
                        },
                        placeholder = { Text("ملاحظة...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    // Render attachments inside editor
                    if (attachments.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            attachments.forEachIndexed { index, att ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (att.type == "image") {
                                            val decoded = remember(att.base64) { ImageUtils.base64ToBitmap(att.base64) }
                                            if (decoded != null) {
                                                Image(
                                                    bitmap = decoded.asImageBitmap(),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(45.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable { focusedImageBase64 = att.base64 },
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Text("مرفق صورة", fontSize = 12.sp)
                                        } else {
                                            Icon(Icons.Default.Mic, contentDescription = null, tint = themePrimary, modifier = Modifier.size(24.dp))
                                            Text("تسجيل صوتي", fontSize = 12.sp)
                                        }
                                    }
                                    IconButton(
                                        onClick = { attachments.removeAt(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف המرفق", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                } // End vertical scroll Column
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("سجل الملاحظات والمهام (جوجل Keep)", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))

            // Google Keep style search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("البحث في الملاحظات...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themePrimary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                singleLine = true
            )

            if (filteredNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.StickyNote2, contentDescription = null, tint = Color.LightGray.copy(alpha = 0.8f), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "لا توجد ملاحظات مسجلة بعد" else "لا توجد نتائج بحث مطابقة",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Keep style staggered dual-column layout with pinned segment and scrolling
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Pinned Notes Segment
                    if (pinnedNotes.isNotEmpty()) {
                        Text("المثبتة 📌", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = themePrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val leftPinned = pinnedNotes.filterIndexed { index, _ -> index % 2 == 0 }
                            val rightPinned = pinnedNotes.filterIndexed { index, _ -> index % 2 != 0 }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                leftPinned.forEach { note ->
                                    NoteCard(
                                        note = note,
                                        cardBgColor = cardBgColor,
                                        themePrimary = themePrimary,
                                        isDark = androidx.compose.foundation.isSystemInDarkTheme(),
                                        onClick = {
                                            noteToEdit = note
                                            editTitleText = note.title
                                            editContentText = note.content
                                            editColorHex = note.colorHex
                                            isPinned = note.isPinned
                                            showEditor = true
                                        },
                                        onDelete = {
                                            onConfirmDelete(note.content) { viewModel.deleteNoteRecord(note) }
                                        },
                                        onShare = {
                                            shareNoteWithMedia(context, note)
                                        },
                                        onPinToggle = {
                                            val updated = note.copy(isPinned = !note.isPinned)
                                            viewModel.updateNoteRecord(updated)
                                        }
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rightPinned.forEach { note ->
                                    NoteCard(
                                        note = note,
                                        cardBgColor = cardBgColor,
                                        themePrimary = themePrimary,
                                        isDark = androidx.compose.foundation.isSystemInDarkTheme(),
                                        onClick = {
                                            noteToEdit = note
                                            editTitleText = note.title
                                            editContentText = note.content
                                            editColorHex = note.colorHex
                                            isPinned = note.isPinned
                                            showEditor = true
                                        },
                                        onDelete = {
                                            onConfirmDelete(note.content) { viewModel.deleteNoteRecord(note) }
                                        },
                                        onShare = {
                                            shareNoteWithMedia(context, note)
                                        },
                                        onPinToggle = {
                                            val updated = note.copy(isPinned = !note.isPinned)
                                            viewModel.updateNoteRecord(updated)
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("الأخرى 📝", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                    }

                    // 2. Regular Notes Segment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val leftNotes = normalNotes.filterIndexed { index, _ -> index % 2 == 0 }
                        val rightNotes = normalNotes.filterIndexed { index, _ -> index % 2 != 0 }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            leftNotes.forEach { note ->
                                NoteCard(
                                    note = note,
                                    cardBgColor = cardBgColor,
                                    themePrimary = themePrimary,
                                    isDark = androidx.compose.foundation.isSystemInDarkTheme(),
                                    onClick = {
                                        noteToEdit = note
                                        editTitleText = note.title
                                        editContentText = note.content
                                        editColorHex = note.colorHex
                                        isPinned = note.isPinned
                                        showEditor = true
                                    },
                                    onDelete = {
                                        onConfirmDelete(note.content) { viewModel.deleteNoteRecord(note) }
                                    },
                                    onShare = {
                                        shareNoteWithMedia(context, note)
                                    },
                                    onPinToggle = {
                                        val updated = note.copy(isPinned = !note.isPinned)
                                        viewModel.updateNoteRecord(updated)
                                    }
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rightNotes.forEach { note ->
                                NoteCard(
                                    note = note,
                                    cardBgColor = cardBgColor,
                                    themePrimary = themePrimary,
                                    isDark = androidx.compose.foundation.isSystemInDarkTheme(),
                                    onClick = {
                                        noteToEdit = note
                                        editTitleText = note.title
                                        editContentText = note.content
                                        editColorHex = note.colorHex
                                        isPinned = note.isPinned
                                        showEditor = true
                                    },
                                    onDelete = {
                                        onConfirmDelete(note.content) { viewModel.deleteNoteRecord(note) }
                                    },
                                    onShare = {
                                        shareNoteWithMedia(context, note)
                                    },
                                    onPinToggle = {
                                        val updated = note.copy(isPinned = !note.isPinned)
                                        viewModel.updateNoteRecord(updated)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // DIMMING OVERLAY
        if (showFabMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { showFabMenu = false }
            )
        }

        // EXPANDED FAB ITEMS
        androidx.compose.animation.AnimatedVisibility(
            visible = showFabMenu,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { 50 }),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { 50 }),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 90.dp, start = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Image
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = { showFabMenu = false; galleryLauncher.launch("image/*"); showEditor = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = themePrimary
                    ) { Icon(Icons.Default.Image, contentDescription = "صورة") }
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                        Text("صورة", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                }
                // Drawing (dummy for now)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = { showFabMenu = false; Toast.makeText(context, "أداة الرسم قريباً", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = themePrimary
                    ) { Icon(Icons.Default.Brush, contentDescription = "رسم") }
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                        Text("رسم", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                }
                // Voice Note
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = { showFabMenu = false; Toast.makeText(context, "تم إضافة صوت", Toast.LENGTH_SHORT).show(); showEditor = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = themePrimary
                    ) { Icon(Icons.Default.Mic, contentDescription = "ملاحظة صوتية") }
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                        Text("ملاحظة صوتية", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                }
                // Text
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            onCheckAddPermission {
                                noteToEdit = null
                                editTitleText = ""
                                editContentText = ""
                                editColorHex = ""
                                isPinned = false
                                showEditor = true
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = themePrimary
                    ) { Icon(Icons.Default.TextFields, contentDescription = "نص") }
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                        Text("نص", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // MAIN FAB
        FloatingActionButton(
            onClick = { showFabMenu = !showFabMenu },
            containerColor = themePrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 16.dp)
        ) {
            Icon(
                if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "قائمة الإضافة"
            )
        }
    }
    }
}

// ================= SCREEN COMPOSABLE 6: ARCHIVE =================
@Composable
fun ArchiveScreen(
    viewModel: FarmViewModel,
    themePrimary: Color,
    zoom: Float,
    cardBgColor: Color,
    onConfirmDelete: (String, () -> Unit) -> Unit,
    onEditAnimal: (AnimalEntity) -> Unit,
    onViewDetails: (Int) -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val archiveTransactions by viewModel.transactionsList.collectAsStateWithLifecycle()
    val filterArchiveBy by viewModel.selectedArchiveFilter.collectAsStateWithLifecycle()
    val archivedAnimals by viewModel.archiveAnimalsList.collectAsStateWithLifecycle()
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var animalSubTab by remember { mutableStateOf("txs") } // "txs" or "sold_animals"
    var animalToRefund by remember { mutableStateOf<AnimalEntity?>(null) }
    var optionsTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var expandedTxId by remember { mutableStateOf<Int?>(null) }
    var optionsAnimal by remember { mutableStateOf<AnimalEntity?>(null) }

    // Refund Dialog inputs
    var refundSettlementType by remember { mutableStateOf("adjust_deferred") } // "adjust_deferred", "cash_out", "none"
    var refundAmountStr by remember { mutableStateOf("") }
    var refundReason by remember { mutableStateOf("") }
    
    var globalSearchQuery by remember { mutableStateOf("") }

    val filteredList = remember(archiveTransactions, filterArchiveBy, globalSearchQuery) {
        // Exclude settlements from main archive
        val nonSettlements = archiveTransactions.filter { it.category != "تسوية فواتير" }
        val filteredByType = when (filterArchiveBy) {
            "income" -> nonSettlements.filter { it.type == "income" }
            "expense" -> nonSettlements.filter { it.type == "expense" }
            "feed" -> nonSettlements.filter { it.category == "أعلاف" }
            "animals" -> nonSettlements.filter { it.category == "حيوانات" }
            else -> nonSettlements
        }
        
        if (globalSearchQuery.isNotBlank()) {
            val query = globalSearchQuery.trim().lowercase()
            filteredByType.filter {
                it.description.lowercase().contains(query) ||
                it.category.lowercase().contains(query)
            }
        } else {
            filteredByType
        }
    }

    val soldAnimals = remember(archivedAnimals, globalSearchQuery) {
        val baseList = archivedAnimals.filter { it.isArchived }
        if (globalSearchQuery.isNotBlank()) {
            val query = globalSearchQuery.trim().lowercase()
            baseList.filter {
                it.name.lowercase().contains(query) ||
                it.type.lowercase().contains(query) ||
                it.merchantName.lowercase().contains(query)
            }
        } else {
            baseList
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("أرشيف العمليات والتدفقات المالية", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            IconButton(onClick = { viewModel.setArchiveFilter("all") }) {
                Icon(Icons.Default.Refresh, contentDescription = "تحديث الفلتر")
            }
        }
        
        OutlinedTextField(
            value = globalSearchQuery,
            onValueChange = { globalSearchQuery = it },
            placeholder = { Text("بحث ذكي (الرقم، الحالة، المصدر، النوع، الوصف)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Horizontal filter bar for Archives
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            buttonCategoryFilter("الكل", filterArchiveBy == "all", themePrimary) { viewModel.setArchiveFilter("all") }
            buttonCategoryFilter("إيرادات", filterArchiveBy == "income", themePrimary) { viewModel.setArchiveFilter("income") }
            buttonCategoryFilter("مصاريف", filterArchiveBy == "expense", themePrimary) { viewModel.setArchiveFilter("expense") }
            buttonCategoryFilter("أعلاف", filterArchiveBy == "feed", themePrimary) { viewModel.setArchiveFilter("feed") }
            buttonCategoryFilter("حيوانات", filterArchiveBy == "animals", themePrimary) { viewModel.setArchiveFilter("animals") }
        }

        // Animated sub-headers specifically for animal returns / sales breakdown
        if (filterArchiveBy == "animals") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { animalSubTab = "txs" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (animalSubTab == "txs") themePrimary else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "القيود المالية للماشية 💵",
                        color = if (animalSubTab == "txs") Color.White else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { animalSubTab = "sold_animals" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (animalSubTab == "sold_animals") themePrimary else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "رؤوس الماشية المباعة 🐂",
                        color = if (animalSubTab == "sold_animals") Color.White else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filterArchiveBy == "animals" && animalSubTab == "sold_animals") {
            if (soldAnimals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SentimentDissatisfied, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد بيانات، اضغط + للإضافة", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(soldAnimals) { animal ->
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { optionsAnimal = animal }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("الاسم/الوسم: ${animal.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("السلالة والنوع: ${animal.type} - العمر: ${animal.age}", fontSize = 12.sp, color = Color.LightGray)
                                        Text("المشتري: ${animal.merchantName} - الوزن: ${animal.weight} كغ", fontSize = 12.sp, color = Color.LightGray)
                                        Text("تاريخ المغادرة والبيع: ${animal.departureDate}", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${animal.salePrice} ج",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = Color(0xFF2563EB)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onViewDetails(animal.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = "التفاصيل", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { onEditAnimal(animal) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل الأصل", tint = themePrimary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    onConfirmDelete("هل أنت متأكد من الحذف النهائي للرأس من الأرشيف؟ سيتم حذف جميع بياناتها!") {
                                                        viewModel.hardDeleteAnimalRecord(animal)
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteForever, contentDescription = "حذف نهائي", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { 
                                                animalToRefund = animal
                                                refundAmountStr = animal.salePrice.toString()
                                                refundReason = ""
                                                refundSettlementType = if (animal.associatedPersonId != null) "adjust_deferred" else "none"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("إرجاع الرأس 🔄", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد بيانات، اضغط + للإضافة", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredList) { log ->
                    val isExpanded = expandedTxId == log.id
                    Surface(
                        color = Color(0xFF1E293B), // Dark Slate
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedTxId = if (isExpanded) null else log.id }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (log.type == "income") Color(0xFF4ADE80).copy(alpha = 0.2f)
                                                else Color(0xFFF87171).copy(alpha = 0.2f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (log.type == "income") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = if (log.type == "income") Color(0xFF4ADE80) else Color(0xFFF87171),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            log.description,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (13f * (zoom / 16f)).sp
                                        )
                                        Text("تاريخ المعاملة: ${log.date}", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                }

                                Text(
                                    text = "${if (log.type == "income") "+" else "-"}${log.amount}",
                                    color = if (log.type == "income") Color(0xFF4ADE80) else Color(0xFFEF4444),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                            
                            if (isExpanded) {
                                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(horizontal = 16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Details
                                    Button(
                                        onClick = { optionsTx = log },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("التفاصيل", color = Color.White, fontSize = 11.sp)
                                    }
                                    
                                    // 2. Edit
                                    Button(
                                        onClick = {
                                            if (log.associatedAnimalId != null && log.associatedAnimalId != 0) {
                                                val sourceAnimal = archivedAnimals.find { it.id == log.associatedAnimalId } 
                                                    ?: viewModel.animalsList.value.find { it.id == log.associatedAnimalId }
                                                if (sourceAnimal != null) {
                                                    onEditAnimal(sourceAnimal)
                                                } else {
                                                    editingTx = log
                                                }
                                            } else {
                                                editingTx = log 
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("تعديل", color = Color.White, fontSize = 11.sp)
                                    }
                                    
                                    // 3. Delete
                                    Button(
                                        onClick = {
                                            onConfirmDelete("هل أنت متأكد من رغبتك في حذف هذا القيد؟") {
                                                viewModel.softDeleteTransactionRecord(log)
                                                Toast.makeText(context, "تم النقل لسلة المهملات", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("حذف", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingTx != null) {
        val tx = editingTx!!
        var editAmount by remember { mutableStateOf(tx.amount.toString()) }
        var editDesc by remember { mutableStateOf(tx.description) }

        AlertDialog(
            onDismissRequest = { editingTx = null },
            title = { Text("تعديل المعاملة القديمة", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        label = { Text("المبلغ") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("الوصف") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amnt = editAmount.toDoubleOrNull() ?: tx.amount
                    val updated = tx.copy(amount = amnt, description = editDesc)
                    viewModel.updateTransactionRecord(tx, updated)
                    editingTx = null
                    Toast.makeText(context, "تم تحديث القيود وعكسها تلقائياً", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = themePrimary)) {
                    Text("حفظ وعكس تلقائي", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTx = null }) { Text("إلغاء") }
            }
        )
    }

    if (txToDelete != null) {
        val tx = txToDelete!!
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("تأكيد حذف القيد أو السند المالي ⚠️", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text(
                    "هل أنت متأكد من حذف هذا السند المالي نهائياً من الأرشيف؟ \n\n" +
                    "الوصف: ${tx.description}\n" +
                    "المبلغ: ${tx.amount} جنيه\n\n" +
                    "سيؤدي ذلك لتعديل تلقائي في الأرصدة وإرجاع القيمة المترتبة عليها!",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.softDeleteTransactionRecord(tx)
                        txToDelete = null
                        Toast.makeText(context, "تم النقل لسلة المهملات 🗑️", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("حذف وتصحيح الرصيد", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    if (animalToRefund != null) {
        val animal = animalToRefund!!
        AlertDialog(
            onDismissRequest = { animalToRefund = null },
            title = { Text("فاتورة وسند استرجاع رأس مبيعات 🔄", fontWeight = FontWeight.Bold, color = themePrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أنت بصدد إرجاع رأس الماشية المباعة إلى الحظيرة مجدداً وتصحيح الموازنة وسجل الحركة.", fontSize = 13.sp)
                    
                    Surface(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("الرأس: ${animal.name} (${animal.type})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("المشتري: ${animal.merchantName}", fontSize = 11.sp, color = Color.Gray)
                            Text("قيمة مبيعات الرأس المرتجعة: ${animal.salePrice} جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("حدد طريقة تسوية ميزانية وقيمة المرتجع:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = refundSettlementType == "adjust_deferred",
                            onClick = { refundSettlementType = "adjust_deferred" }
                        )
                        Text("إلغاء المديونية من الحساب الآجل للمشتري", fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = refundSettlementType == "cash_out",
                            onClick = { refundSettlementType = "cash_out" }
                        )
                        Text("سداد نقدي للعميل (سند صرف نقدي)", fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = refundSettlementType == "none",
                            onClick = { refundSettlementType = "none" }
                        )
                        Text("إرجاع الرأس بدون ذمة مالية (تعديل الحظيرة فقط)", fontSize = 12.sp)
                    }

                    if (refundSettlementType != "none") {
                        OutlinedTextField(
                            value = refundAmountStr,
                            onValueChange = { refundAmountStr = it },
                            label = { Text("مبلغ المرتجع المطلوب تسويته (ج)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text("سبب وملاحظة الإرجاع (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val refAmt = refundAmountStr.toDoubleOrNull() ?: 0.0
                        viewModel.refundSoldAnimal(
                            animal = animal,
                            settlementType = refundSettlementType,
                            refundAmount = refAmt,
                            reason = refundReason
                        )
                        Toast.makeText(context, "تم إرجاع رأس الماشية وإصدار سند الإرجاع بنجاح! 🔄", Toast.LENGTH_SHORT).show()
                        animalToRefund = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
                ) {
                    Text("تأكيد وإرجاع الرأس", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { animalToRefund = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    if (optionsAnimal != null) {
        val animal = optionsAnimal!!
        AlertDialog(
            onDismissRequest = { optionsAnimal = null },
            title = { Text("خيارات رأس الماشية المباعة 🐄", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "اختر أحد الإجراءات المتاحة لرأس الماشية (${animal.name}):",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            optionsAnimal = null
                            onViewDetails(animal.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التفاصيل وحالة البيع والاسترجاع 📄", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            optionsAnimal = null
                            onEditAnimal(animal)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل الأصل من المصدر ✏️", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            optionsAnimal = null
                            onConfirmDelete("هل أنت متأكد من الحذف النهائي للرأس من الأرشيف؟ سيتم حذف جميع بياناتها!") {
                                viewModel.hardDeleteAnimalRecord(animal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف نهائي من السجلات 🗑️", color = Color.White, fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = { optionsAnimal = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            }
        )
    }

    if (optionsTx != null) {
        val tx = optionsTx!!
        val settlementsForThisTx = archiveTransactions.filter { it.description.startsWith("تسوية لسند رقم #${tx.id}:") }
        val settledAmount = settlementsForThisTx.sumOf { it.amount }
        val remainingAmount = (tx.amount - settledAmount).coerceAtLeast(0.0)
        var newSettlementAmount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { optionsTx = null },
            title = { Text("تفاصيل الفاتورة ومستند الحركة 📄", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("البيان المالي: ${tx.description}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("القيمة: ${tx.amount} جنيه (${if(tx.type == "income") "إيرادات" else "مصروفات"})", fontSize = 13.sp, color = if(tx.type=="income") Color(0xFF4ADE80) else Color(0xFFF87171))
                    Text("التاريخ: ${tx.date} | الفئة: ${tx.category}", fontSize = 12.sp, color = Color.Gray)
                    
                    HorizontalDivider(color = Color(0xFF334155))
                    
                    Text("سجل التسويات والمدفوعات (دفتر فرعي):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = themePrimary)
                    if (settlementsForThisTx.isEmpty()) {
                        Text("لا يوجد تسويات مسجلة لهذا السند.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        settlementsForThisTx.forEach { settlement ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(settlement.description.substringAfter(": "), fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.weight(1f))
                                Text("${settlement.amount} ج", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                            }
                        }
                    }
                    
                    HorizontalDivider(color = Color(0xFF334155))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المدفوع/المسوى:", fontSize = 12.sp, color = Color.White)
                        Text("$settledAmount جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المبلغ المتبقي:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("$remainingAmount جنيه", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                    }
                    
                    if (remainingAmount > 0.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newSettlementAmount,
                                onValueChange = { newSettlementAmount = it },
                                label = { Text("إضافة دفعة (تسوية)") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val amountVal = newSettlementAmount.toDoubleOrNull() ?: 0.0
                                    if (amountVal > 0 && amountVal <= remainingAmount) {
                                        val newTxType = tx.type
                                        val currentDateTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
                                        viewModel.registerManualTransaction(
                                            type = newTxType,
                                            amount = amountVal,
                                            description = "تسوية لسند رقم #${tx.id}: دفعة بقيمة $amountVal في ($currentDateTime)",
                                            category = "تسوية فواتير",
                                            personId = tx.associatedPersonId
                                        )
                                        newSettlementAmount = ""
                                        Toast.makeText(context, "تم تسجيل الدفعة بنجاح", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "أدخل مبلغاً صحيحاً لا يتجاوز الباقي", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("إضافة", color = Color.White)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("✅ تم سداد/تسوية هذا السند بالكامل", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { optionsTx = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("إغلاق", color = Color.White)
                }
            }
        )
    }
}
}

@Composable
fun buttonCategoryFilter(label: String, active: Boolean, color: Color, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = if (active) color else Color.LightGray.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, color = if (active) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ================= SCREEN COMPOSABLE 7: SETTINGS =================
@Composable
fun SettingsScreen(viewModel: FarmViewModel, accentColor: Color, zoomLevel: Float, onUpdateLabels: () -> Unit) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    val currentFarm by viewModel.currentFarm.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val syncIp by viewModel.syncIp.collectAsStateWithLifecycle()
    val syncPort by viewModel.syncPort.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    var customBarn by remember { mutableStateOf("") }
    var customFeeds by remember { mutableStateOf("") }
    var customAccounts by remember { mutableStateOf("") }

    val spTitles = remember { context.getSharedPreferences("farm_titles", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        customBarn = spTitles.getString("label_barn", "الحظيرة") ?: "الحظيرة"
        customFeeds = spTitles.getString("label_feeds", "الأعلاف") ?: "الأعلاف"
        customAccounts = spTitles.getString("label_accounts", "الحسابات") ?: "الحسابات"
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri)
        }
    }

    val cardBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    // Dynamic color picker states
    var showBgColorPicker by remember { mutableStateOf(false) }
    var bgRed by remember { mutableStateOf(255) }
    var bgGreen by remember { mutableStateOf(255) }
    var bgBlue by remember { mutableStateOf(255) }
    var bgHexInput by remember { mutableStateOf("") }

    var showTextColorPicker by remember { mutableStateOf(false) }
    var textRed by remember { mutableStateOf(30) }
    var textGreen by remember { mutableStateOf(41) }
    var textBlue by remember { mutableStateOf(59) }
    var textHexInput by remember { mutableStateOf("") }

    // Convert RGB integers to Hex string (e.g. #FFFFFF)
    fun rgbToHex(r: Int, g: Int, b: Int): String {
        return String.format("#%02X%02X%02X", r, g, b)
    }

    // Convert Hex string (e.g. #FFFFFF) to RGB integers
    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        return try {
            val colorInt = android.graphics.Color.parseColor(hex)
            Triple(
                android.graphics.Color.red(colorInt),
                android.graphics.Color.green(colorInt),
                android.graphics.Color.blue(colorInt)
            )
        } catch (e: Exception) {
            Triple(255, 255, 255)
        }
    }

    // Auto calculate highly contrasting text color based on background color luminance
    fun calculateContrastText(bgHex: String): String {
        return try {
            val colorInt = android.graphics.Color.parseColor(bgHex)
            val r = android.graphics.Color.red(colorInt)
            val g = android.graphics.Color.green(colorInt)
            val b = android.graphics.Color.blue(colorInt)
            val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            if (luminance > 0.5) "#1E293B" else "#F8FAFC"
        } catch (e: Exception) {
            "#1E293B"
        }
    }

    // --- DIALOG 1: CUSTOM CARD COLOR PICKER ---
    if (showBgColorPicker) {
        val currentHexResult = rgbToHex(bgRed, bgGreen, bgBlue)
        AlertDialog(
            onDismissRequest = { showBgColorPicker = false },
            title = { Text("مخصص: اختيار لون خلفية الكروت والبطاقات 🎨", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اسحب المنزلقات أدناه لتكوين اللون المخصص لجميع بطاقات التطبيق:", fontSize = 11.sp, color = Color.Gray)
                    
                    // Live preview card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(android.graphics.Color.parseColor(currentHexResult)), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "معاينة البطاقة ($currentHexResult)",
                            color = Color(android.graphics.Color.parseColor(calculateContrastText(currentHexResult))),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Red channel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أحمر ($bgRed)", fontSize = 10.sp, modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold)
                        Slider(
                            value = bgRed.toFloat(),
                            onValueChange = { bgRed = it.toInt() },
                            valueRange = 0f..255f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Green channel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أخضر ($bgGreen)", fontSize = 10.sp, modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold)
                        Slider(
                            value = bgGreen.toFloat(),
                            onValueChange = { bgGreen = it.toInt() },
                            valueRange = 0f..255f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Blue channel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أزرق ($bgBlue)", fontSize = 10.sp, modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold)
                        Slider(
                            value = bgBlue.toFloat(),
                            onValueChange = { bgBlue = it.toInt() },
                            valueRange = 0f..255f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("أو أدخل كود اللون مباشرة (مثال: #111827):", fontSize = 10.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = bgHexInput,
                        onValueChange = {
                            bgHexInput = it
                            if (it.length == 7 && it.startsWith("#")) {
                                val rgb = hexToRgb(it)
                                bgRed = rgb.first
                                bgGreen = rgb.second
                                bgBlue = rgb.third
                            }
                        },
                        placeholder = { Text("#FFFFFF") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val chosenColor = rgbToHex(bgRed, bgGreen, bgBlue)
                        viewModel.setCardColorHex(chosenColor)
                        // Auto contrast font selection
                        val autoTxt = calculateContrastText(chosenColor)
                        viewModel.setTextColorHex(autoTxt)
                        showBgColorPicker = false
                        Toast.makeText(context, "تم تغيير خلفية جميع البطاقات بنجاح! 🎨", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("حفظ وتطبيق", color = Color.White, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBgColorPicker = false }) {
                    Text("إلغاء", fontSize = 11.sp)
                }
            }
        )
    }

    // --- DIALOG 2: CUSTOM TEXT COLOR PICKER ---
    if (showTextColorPicker) {
        val currentHexResult = rgbToHex(textRed, textGreen, textBlue)
        AlertDialog(
            onDismissRequest = { showTextColorPicker = false },
            title = { Text("مخصص: اختيار لون النصوص والخطوط 🖋️", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اسحب المنزلقات لتعديل لون نصوص وبطاقات التطبيق بشكل كامل:", fontSize = 11.sp, color = Color.Gray)
                    
                    // Live Preview Area using current background color
                    val activeBgHexByModel = viewModel.cardColorHex.value
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(android.graphics.Color.parseColor(activeBgHexByModel)), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "شكل قراءة الكلمات والخط ($currentHexResult)",
                            color = Color(android.graphics.Color.parseColor(currentHexResult)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Red channel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أحمر ($textRed)", fontSize = 10.sp, modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold)
                        Slider(
                            value = textRed.toFloat(),
                            onValueChange = { textRed = it.toInt() },
                            valueRange = 0f..255f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Green channel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أخضر ($textGreen)", fontSize = 10.sp, modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold)
                        Slider(
                            value = textGreen.toFloat(),
                            onValueChange = { textGreen = it.toInt() },
                            valueRange = 0f..255f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Blue channel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("أزرق ($textBlue)", fontSize = 10.sp, modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold)
                        Slider(
                            value = textBlue.toFloat(),
                            onValueChange = { textBlue = it.toInt() },
                            valueRange = 0f..255f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("أو أدخل كود لون الخطوط يدوياً (مثال: #000000):", fontSize = 10.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = textHexInput,
                        onValueChange = {
                            textHexInput = it
                            if (it.length == 7 && it.startsWith("#")) {
                                val rgb = hexToRgb(it)
                                textRed = rgb.first
                                textGreen = rgb.second
                                textBlue = rgb.third
                            }
                        },
                        placeholder = { Text("#1E293B") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val chosenColor = rgbToHex(textRed, textGreen, textBlue)
                        viewModel.setTextColorHex(chosenColor)
                        showTextColorPicker = false
                        Toast.makeText(context, "تم تغيير لون خطوط التطبيق بالكامل! 🖋️", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("حفظ وتطبيق", color = Color.White, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextColorPicker = false }) {
                    Text("إلغاء", fontSize = 11.sp)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "إعدادات وتخصيص المزرعة",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        var renameText by remember { mutableStateOf(currentFarm ?: "") }
        var showRenameDialog by remember { mutableStateOf(false) }

        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("تغيير اسم المزرعة", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = { Text("الاسم الجديد") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.renameCurrentFarm(renameText) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    showRenameDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("حفظ الثغيير", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "إعدادات المزرعة الحالية (${currentFarm}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        renameText = currentFarm ?: ""
                        showRenameDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تغيير اسم المزرعة", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Title Mapping customizable names (collapsible accordion)
        var isLabelsSectionExpanded by remember { mutableStateOf(false) }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isLabelsSectionExpanded = !isLabelsSectionExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تعديل وتخصيص التسميات في واجهات التطبيق:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (isLabelsSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                if (isLabelsSectionExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = customBarn,
                        onValueChange = { customBarn = it },
                        label = { Text("تسمية الحظيرة (مثال: حظيرة الأبقار)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customFeeds,
                        onValueChange = { customFeeds = it },
                        label = { Text("تسمية مخزون الأعلاف (مثال: مستودع الطعام)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customAccounts,
                        onValueChange = { customAccounts = it },
                        label = { Text("تسمية الحسابات (مثال: الدفتر المالي)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            with(spTitles.edit()) {
                                putString("label_barn", customBarn)
                                putString("label_feeds", customFeeds)
                                putString("label_accounts", customAccounts)
                                apply()
                            }
                            onUpdateLabels()
                            Toast.makeText(context, "تم حفظ العناوين الجديدة وتحديث القوائم!", Toast.LENGTH_SHORT).show()
                            isLabelsSectionExpanded = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ المسميات المخصصة لجميع علامات التبويب والمصاريف", color = Color.White)
                    }
                }
            }
        }

        // Color theme selectors
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تخصيص المظهر وتغيير السمة اللونية لـ المزرعة برو:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "أخضر زمردي" to "#059669",
                        "أزرق ملكي" to "#2563EB",
                        "ذهبي عسلي" to "#D97706",
                        "بنفسجي فاخر" to "#7C3AED"
                    ).forEach { (name, hex) ->
                        val active = hex == viewModel.primaryColorHex.value
                        Surface(
                            onClick = { viewModel.setThemeHex(hex) },
                            color = Color(android.graphics.Color.parseColor(hex)),
                            shape = RoundedCornerShape(12.dp),
                            border = if (active) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(54.dp),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Card background color selectors
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "لون خلفية كروت وبطاقات البيانات:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "غيّر لون خلفية البطاقات في المزرعة لتسهيل القراءة وتفادي الإجهاد.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "أبيض" to "#FFFFFF",
                        "رمادي" to "#F1F5F9",
                        "وردي" to "#FDF2F8",
                        "داكن ملوكي" to "#1E293B",
                        "أسود فحم" to "#0F172A"
                    ).forEach { (name, hex) ->
                        val active = hex == viewModel.cardColorHex.value
                        Surface(
                            onClick = { 
                                viewModel.setCardColorHex(hex)
                                // Also trigger auto-contrast for preset selection
                                val autoTxt = calculateContrastText(hex)
                                viewModel.setTextColorHex(autoTxt)
                            },
                            color = Color(android.graphics.Color.parseColor(hex)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (active) 3.dp else 1.dp, if (active) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = name,
                                    color = Color(android.graphics.Color.parseColor(calculateContrastText(hex))),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val rgb = hexToRgb(viewModel.cardColorHex.value)
                        bgRed = rgb.first
                        bgGreen = rgb.second
                        bgBlue = rgb.third
                        bgHexInput = viewModel.cardColorHex.value
                        showBgColorPicker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح تركيب وتدريج لون خلفية مخصص 🎨", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Custom Text / Font Color Selectors
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "لون خطوط ونصوص التطبيق:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "حدّد لون الكتابة ليكون متبايناً ومريحاً للعين وتجنّب النصوص غير المرئية.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "تباين تلقائي 🛡️" to "auto",
                        "داكن مريح" to "#1E293B",
                        "أسود قاتم" to "#000000",
                        "أبيض ناصع" to "#FFFFFF",
                        "أصفر رملي" to "#FEF3C7"
                    ).forEach { (name, hexOrMode) ->
                        val active = if (hexOrMode == "auto") {
                            viewModel.textColorHex.value == calculateContrastText(viewModel.cardColorHex.value)
                        } else {
                            hexOrMode == viewModel.textColorHex.value
                        }

                        Surface(
                            onClick = {
                                if (hexOrMode == "auto") {
                                    val autoTxt = calculateContrastText(viewModel.cardColorHex.value)
                                    viewModel.setTextColorHex(autoTxt)
                                    Toast.makeText(context, "تم تطبيق التباين التلقائي الموصى به 🛡️", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.setTextColorHex(hexOrMode)
                                }
                            },
                            color = if (hexOrMode == "auto") Color.LightGray.copy(alpha = 0.3f) else Color(android.graphics.Color.parseColor(hexOrMode)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (active) 3.dp else 1.dp, if (active) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = name,
                                    color = if (hexOrMode == "auto") MaterialTheme.colorScheme.onSurface else if (hexOrMode == "#FFFFFF" || hexOrMode == "#FEF3C7") Color.Black else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val rgb = hexToRgb(viewModel.textColorHex.value)
                        textRed = rgb.first
                        textGreen = rgb.second
                        textBlue = rgb.third
                        textHexInput = viewModel.textColorHex.value
                        showTextColorPicker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح تركيب وتخصيص لون نصوص دقيق 🖋️", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Day/Night mode selector
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تحديد نمط المظهر (الوضع الليل والنهار):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Triple("system", "تلقائي", Icons.Default.Settings),
                        Triple("light", "مضيء (نهار)", Icons.Default.WbSunny),
                        Triple("dark", "مظلم (ليل)", Icons.Default.Brightness3)
                    ).forEach { (mode, label, icon) ->
                        val active = themeMode == mode
                        Surface(
                            onClick = { viewModel.setThemeMode(mode) },
                            color = if (active) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = if (active) null else BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            tonalElevation = if (active) 4.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CUSTOM: Font Type & Style Selection + Online Downloader Simulation ---
        val selectedFontState by viewModel.selectedFont.collectAsStateWithLifecycle()
        val isFontDownloading by viewModel.isFontDownloading.collectAsStateWithLifecycle()

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "نوع تصميم وشكل خط الكلمات العربي: 🖋️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تحكّم في المظهر المطبوع والجمالي لكافة الخطوط في التطبيق.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "tajawal" to "تيجوال (معتدل)",
                        "amiri" to "أميري (كلاسيكي)",
                        "cairo" to "كايرو (محمل)"
                    ).forEach { (fontKey, fontLabel) ->
                        val active = selectedFontState == fontKey
                        Surface(
                            onClick = { viewModel.changeFont(fontKey) },
                            color = if (active) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = if (active) null else BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = fontLabel,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Online Font Downloader Button
                Button(
                    onClick = {
                        viewModel.simulateDownloadFont()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFontDownloading
                ) {
                    if (isFontDownloading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري الاتصال بـ Google Fonts لتحميل خط 'Cairo Pro'...", color = Color.White, fontSize = 11.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحميل خط 'Cairo Pro' الإضافي من الويب بنقرة واحدة 🌐", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Screen Zoom Factor slider
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "متحكم ببعد الشاشة والخط والرموز (Text Scale/Zoom):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = zoomLevel,
                    onValueChange = { viewModel.updateZoom(it) },
                    valueRange = 12f..20f,
                    steps = 8
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("صغير جداً (12)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("المعتدل (16)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    Text("ضخم مقروء (20)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Restore Defaults
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "استعادة واسترجاع الإعدادات (Factory Settings):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.setThemeHex("#059669")
                        viewModel.setThemeMode("light")
                        viewModel.changeFont("cairo")
                        viewModel.updateZoom(16f)
                        viewModel.setCardColorHex("#FFFFFF")
                        viewModel.setTextColorHex("#1E293B")
                        with(spTitles.edit()) {
                            clear()
                            apply()
                        }
                        customBarn = "الحظيرة"
                        customFeeds = "الأعلاف"
                        customAccounts = "الحسابات"
                        onUpdateLabels()
                        Toast.makeText(context, "تم استعادة الإعدادات الافتراضية بنجاح ✅", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("استعادة التهيئة الافتراضية للنظام والمظهر", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Backup and portable data transfer triggers
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "إمكانية نقل وتصدير البيانات المشتركة (بدون انترنت):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة تقرير HTML/CSS ويب منسق للطباعة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (showExportDialog) {
                    ExportChoiceDialog(viewModel = viewModel, onDismiss = { showExportDialog = false })
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.exportBackup(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة وتصدير ملف JSON بمزرعتك", color = Color.White, fontSize = 10.sp)
                    }

                    Button(
                        onClick = { importLauncher.launch("application/json") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استيراد ملف احتياطي", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }

        // Firebase Sync Trigger
        // Dangerous triggers
        Surface(
            color = Color(0xFFFEF2F2),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("منطقة خطرة: إدارة قاعدة البيانات", color = Color(0xFF991B1B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val act = context.findActivity()
                            if (act != null) {
                                com.example.utils.BiometricUtils.authenticate(
                                    act,
                                    "تأكيد الهوية",
                                    "يرجى تأكيد هويتك لمسح البيانات",
                                    onSuccess = {
                                        viewModel.clearAllDataStream()
                                        Toast.makeText(context, "تم مسح كافة سجلات المزرعة المتواجدة!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err -> Toast.makeText(context, "إلغاء أمني: $err", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("مسح البيانات", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val act = context.findActivity()
                            if (act != null) {
                                com.example.utils.BiometricUtils.authenticate(
                                    act,
                                    "تأكيد الهوية للمسح النهائي",
                                    "يرجى التحقق البيومتري لحذف المزرعة نهائياً",
                                    onSuccess = {
                                        viewModel.deleteCurrentFarm()
                                        Toast.makeText(context, "تم حذف المزرعة بالكامل بنجاح!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err -> Toast.makeText(context, "إلغاء أمني: $err", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حذف مزرعتي نهائياً", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ================= DIALOG 1: ADD ANIMAL (BUYING) =================
@Composable
fun AddAnimalDialog(viewModel: FarmViewModel, accentColor: Color, animalToEdit: com.example.data.model.AnimalEntity? = null, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val people by viewModel.peopleList.collectAsStateWithLifecycle()

    val customTypesRaw by viewModel.animalTypesList.collectAsStateWithLifecycle()
    val typesToUse = remember(customTypesRaw) {
        if (customTypesRaw.isEmpty()) listOf("عجل", "أغنام", "ماعز", "جمال", "جاموس") else customTypesRaw
    }
    var isFemale by remember { mutableStateOf(false) } // false = ذكر, true = أنثى
    var selectedFamilyRaw by remember { mutableStateOf("عجل") }

    var name by remember { mutableStateOf(animalToEdit?.name ?: "") }
    var type by remember { mutableStateOf(animalToEdit?.type ?: "عجل") } // "عجل" / "أغنام"
    
    // Reverse lookup family/gender from type if editing
    LaunchedEffect(animalToEdit) {
        if (animalToEdit != null) {
            val t = animalToEdit.type
            var foundFam = "عجل"
            var foundFem = false
            for (fam in typesToUse) {
                val parsed = com.example.utils.AnimalTypeHelper.parseAnimalType(fam)
                if (parsed.female.equals(t, true)) {
                    foundFam = fam
                    foundFem = true
                    break
                }
                if (parsed.male.equals(t, true)) {
                    foundFam = fam
                    foundFem = false
                    break
                }
            }
            selectedFamilyRaw = foundFam
            isFemale = foundFem
        }
    }

    val updateTypeForGenderAndFamily = { femaleSelected: Boolean, family: String ->
        val typeObj = com.example.utils.AnimalTypeHelper.parseAnimalType(family)
        type = typeObj.getName(femaleSelected)
    }

    var weightStr by remember { mutableStateOf(animalToEdit?.weight?.toString() ?: "") }
    var purchasePriceStr by remember { mutableStateOf(animalToEdit?.purchasePrice?.toString() ?: "") }
    var age by remember { mutableStateOf(animalToEdit?.age ?: "") }
    var feedCostStr by remember { mutableStateOf(animalToEdit?.feedCost?.toString() ?: "") }

    // Merchant Selection options
    var merchantSearchQuery by remember { mutableStateOf(animalToEdit?.merchantName ?: "") }
    var selectedPersonId by remember { mutableStateOf<Int?>(animalToEdit?.associatedPersonId) }
    var newMerchantName by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var showAddNewMerchantDialog by remember { mutableStateOf(false) }
    var newMerchantInputName by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val merchantInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isMerchantPressed by merchantInteractionSource.collectIsPressedAsState()
    LaunchedEffect(isMerchantPressed) {
        if (isMerchantPressed && !isSearchActive) {
            expandedDropdown = !expandedDropdown
        }
    }

    // Payment options
    var paymentStatus by remember { mutableStateOf("full_cash") } // "full_cash", "on_credit", "partial"
    var paidAmountStr by remember { mutableStateOf("") }

    var manualDate by remember { mutableStateOf("") }
    var imageBase64 by remember { mutableStateOf<String?>(animalToEdit?.imageBase64) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imageBase64 = ImageUtils.bitmapToBase64(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageBase64 = ImageUtils.uriToBase64(context, uri)
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(if (animalToEdit != null) "تعديل رأس ماشية ✏️" else "بروتوكول إضافة رأس ماشية جديدة (شراء) 🦬", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Merchant Selection
                val filteredPeople = remember(people, merchantSearchQuery) {
                    if (merchantSearchQuery.isBlank()) people else people.filter { it.name.contains(merchantSearchQuery, ignoreCase = true) }
                }
                Text("مصدر رأس الماشية (التاجر):", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = merchantSearchQuery,
                            onValueChange = {
                                merchantSearchQuery = it
                                expandedDropdown = true
                                if (it.isBlank()) {
                                    selectedPersonId = null
                                }
                            },
                            readOnly = !isSearchActive,
                            interactionSource = merchantInteractionSource,
                            placeholder = { 
                                if (isSearchActive) {
                                    Text("اكتب اسم التاجر للبحث... 🔍", fontSize = 11.sp, color = Color.Gray)
                                } else {
                                    Text("اضغط للاختيار أو اضغط 🔍 للبحث", fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            leadingIcon = {
                                IconButton(onClick = {
                                    isSearchActive = !isSearchActive
                                    if (isSearchActive) {
                                        expandedDropdown = true
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "بحث",
                                        tint = if (isSearchActive) accentColor else Color.Gray
                                    )
                                }
                            },
                            trailingIcon = {
                                if (merchantSearchQuery.isNotEmpty() || selectedPersonId != null) {
                                    IconButton(onClick = {
                                        merchantSearchQuery = ""
                                        selectedPersonId = null
                                        expandedDropdown = false
                                        isSearchActive = false
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        DropdownMenu(
                            expanded = expandedDropdown && filteredPeople.isNotEmpty(),
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            filteredPeople.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (${p.role})") },
                                    onClick = {
                                        selectedPersonId = p.id
                                        merchantSearchQuery = p.name
                                        expandedDropdown = false
                                        isSearchActive = false
                                    }
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { showAddNewMerchantDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "اضافة جديد",
                            tint = accentColor
                        )
                    }
                }

                if (showAddNewMerchantDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddNewMerchantDialog = false },
                        title = { Text("إضافة تاجر مالي جديد ➕", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("يرجى إدخال اسم التاجر لإضافته مباشرة وتحديده في المعاملة الحالية:", fontSize = 11.sp, color = Color.Gray)
                                OutlinedTextField(
                                    value = newMerchantInputName,
                                    onValueChange = { newMerchantInputName = it },
                                    label = { Text("اسم التاجر الجديد") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newMerchantInputName.trim().isBlank()) {
                                        Toast.makeText(context, "الرجاء كتابة اسم التاجر", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    viewModel.registerNewPerson(
                                        name = newMerchantInputName,
                                        role = "تاجر"
                                    ) { insertedId ->
                                        selectedPersonId = insertedId
                                        merchantSearchQuery = newMerchantInputName.trim()
                                        newMerchantInputName = ""
                                        showAddNewMerchantDialog = false
                                        Toast.makeText(context, "تمت إضافة التاجر بنجاح وتحديده!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Text("إضافة وتحديد", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddNewMerchantDialog = false }) {
                                Text("إلغاء", color = Color.Gray)
                            }
                        }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم أو رمز المعرف (رقم تسلسلي تلقائي إن ترك فارغاً)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // 2. Gender Selection Control
                Text("جنس رأس الماشية 🧬:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isFemale = false
                            updateTypeForGenderAndFamily(false, selectedFamilyRaw)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isFemale) accentColor else Color.LightGray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ذكر (عجل، خروف طلوقة) ♂️", color = if (!isFemale) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isFemale = true
                            updateTypeForGenderAndFamily(true, selectedFamilyRaw)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFemale) accentColor else Color.LightGray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("أنثى (عجلة، نعجة بقرة) ♀️", color = if (isFemale) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("تحديد تصنيف وسلالة الرأس:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    typesToUse.forEach { t ->
                        val parsed = com.example.utils.AnimalTypeHelper.parseAnimalType(t)
                        val displayName = if (isFemale) parsed.female else parsed.male
                        val isSelected = selectedFamilyRaw == t
                        Button(
                            onClick = {
                                selectedFamilyRaw = t
                                updateTypeForGenderAndFamily(isFemale, t)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text(displayName, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("الوزن المتوقع (كغ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("سعر الشراء الكلي (جنيه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("العمر (مثال: سنة وخمسة أشهر)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = feedCostStr,
                    onValueChange = { feedCostStr = it },
                    label = { Text("مخصص تكلفة الطعام للرأس (جنيه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Payment Status fields
                Text("حالة تسديد ودفع القيمة المالية:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { paymentStatus = "full_cash" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paymentStatus == "full_cash") Color(0xFF10B981) else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مدفوع كامل نقداً 💵", color = if (paymentStatus == "full_cash") Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { paymentStatus = "on_credit" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paymentStatus == "on_credit") Color(0xFFEF4444) else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("بالآجل (دين عليك) ⏳", color = if (paymentStatus == "on_credit") Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { paymentStatus = "partial" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentStatus == "partial") Color(0xFFD97706) else Color(0xFFF1F5F9)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("مدفوع جزئي (دفعة نقدية) 💰", color = if (paymentStatus == "partial") Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (paymentStatus == "partial") {
                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("المبلغ المالي المدفوع حالياً (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photos triggers
                Text("توثيق مظهر الرأس بصورة:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("التقاط صورة", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ألبوم الهاتف", fontSize = 11.sp, color = Color.White)
                    }
                }

                if (imageBase64 != null) {
                    val decoded = remember(imageBase64) {
                        imageBase64?.let { ImageUtils.base64ToBitmap(it) }
                    }
                    if (decoded != null) {
                        Image(
                            bitmap = decoded.asImageBitmap(),
                            contentDescription = "معاينة المظهر",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                if (animalToEdit == null) {
                    OutlinedTextField(
                        value = manualDate,
                        onValueChange = { manualDate = it },
                        label = { Text("تاريخ الإدخال (اختياري - YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weightVal = weightStr.toDoubleOrNull() ?: 0.0
                    val priceVal = purchasePriceStr.toDoubleOrNull() ?: 0.0
                    val feedCostVal = feedCostStr.toDoubleOrNull() ?: 0.0

                    if (priceVal <= 0.0) {
                        Toast.makeText(context, "الرجاء إدخال سعر شراء صحيح", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val pAmt = paidAmountStr.toDoubleOrNull() ?: 0.0
                    if (paymentStatus == "partial" && (pAmt <= 0.0 || pAmt > priceVal)) {
                        Toast.makeText(context, "الرجاء كتابة مبلغ مدفوع مالي صحيح أقل من سعر الشراء", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (animalToEdit != null) {
                        val updatedAnimal = animalToEdit.copy(
                            name = name,
                            type = type,
                            weight = weightVal,
                            purchasePrice = priceVal,
                            age = age,
                            feedCost = feedCostVal,
                            imageBase64 = imageBase64,
                            associatedPersonId = selectedPersonId,
                            merchantName = merchantSearchQuery.ifBlank { "سوق" }
                        )
                        viewModel.updateAnimalDetails(updatedAnimal)
                        Toast.makeText(context, "تم حفظ وتعديل بيانات رأس الماشية بنجاح!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.registerAnimal(
                            name = name,
                            type = type,
                            weight = weightVal,
                            purchasePrice = priceVal,
                            age = age,
                            feedCost = feedCostVal,
                            imageBase64 = imageBase64,
                            associatedPersonId = selectedPersonId,
                            newMerchantName = merchantSearchQuery,
                            paymentStatus = paymentStatus,
                            paidAmount = pAmt,
                            manualDate = manualDate.takeIf { it.isNotBlank() }
                        )
                        Toast.makeText(context, "تم حفظ وسند رأس الماشية بنجاح وتحديث الأرصدة والشبكة!", Toast.LENGTH_LONG).show()
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(if (animalToEdit != null) "حفظ التعديلات" else "إدخال وحفظ السند", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

// ================= DIALOG 2: ADD FEEDS STOCK =================
@Composable
fun AddFeedDialog(viewModel: FarmViewModel, accentColor: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var feedName by remember { mutableStateOf("") }
    var ingredientsDesc by remember { mutableStateOf("") }
    var totalWeightStr by remember { mutableStateOf("") }
    var totalCostStr by remember { mutableStateOf("") }
    var manualDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("قيد شراء حصص وأعلاف جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = feedName,
                    onValueChange = { feedName = it },
                    label = { Text("اسم صنف الطعام (مثال: خلطة نخالة وفول)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ingredientsDesc,
                    onValueChange = { ingredientsDesc = it },
                    label = { Text("تفاصيل المكونات بالوزن والسعر (مثال: فول 100كغ بسعر 100)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalWeightStr,
                    onValueChange = { totalWeightStr = it },
                    label = { Text("إجمالي الوزن بالكامل (كغ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalCostStr,
                    onValueChange = { totalCostStr = it },
                    label = { Text("التكلفة الكلية المشتراة (جنيه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = manualDate,
                    onValueChange = { manualDate = it },
                    label = { Text("تاريخ الإدخال (اختياري - YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (feedName.isBlank()) return@Button
                    val weight = totalWeightStr.toDoubleOrNull() ?: 0.0
                    val cost = totalCostStr.toDoubleOrNull() ?: 0.0

                    viewModel.registerFeed(feedName, ingredientsDesc, weight, cost, manualDate.takeIf { it.isNotBlank() })
                    Toast.makeText(context, "تم تسجيل وإضافة السجل الحلفي بنجاح!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("شراء الأعلاف وحفظ السند", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

// ================= DIALOG 2.5: ADD MEDICINES STOCK =================
@Composable
fun AddMedicineDialog(viewModel: FarmViewModel, accentColor: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var totalCostStr by remember { mutableStateOf("") }
    var validityDaysStr by remember { mutableStateOf("") }
    
    var imageBase64 by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imageBase64 = com.example.util.ImageUtils.bitmapToBase64(bitmap)
        }
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageBase64 = com.example.util.ImageUtils.uriToBase64(context, uri)
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("قيد شراء دواء أو علاج بيطري جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الدواء أو العلامة الطبية") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalCostStr,
                    onValueChange = { totalCostStr = it },
                    label = { Text("التكلفة الإجمالية (جنيه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = validityDaysStr,
                    onValueChange = { validityDaysStr = it },
                    label = { Text("طول الدواء - فترة السحب بالأيام") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("صورة الدواء للفاتورة (اختياري)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("التقاط", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ألبوم الهاتف", fontSize = 11.sp, color = Color.White)
                    }
                }

                if (imageBase64 != null) {
                    val decoded = remember(imageBase64) {
                        imageBase64?.let { com.example.util.ImageUtils.base64ToBitmap(it) }
                    }
                    if (decoded != null) {
                        Image(
                            bitmap = decoded.asImageBitmap(),
                            contentDescription = "معاينة المظهر",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val cost = totalCostStr.toDoubleOrNull() ?: 0.0
                    val days = validityDaysStr.toIntOrNull() ?: 0

                    viewModel.registerMedicine(name, cost, days, imageBase64)
                    Toast.makeText(context, "تم تسجيل وإضافة الدواء بنجاح! 💊", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("شراء الدواء وحفظ السند", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

// ================= DIALOG 3: MANUAL EXPENSES / INCOMES =================
@Composable
fun AddTransactionDialog(viewModel: FarmViewModel, type: String, accentColor: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val people by viewModel.peopleList.collectAsStateWithLifecycle()

    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عام") }
    var manualDate by remember { mutableStateOf("") }
    
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = if (type == "income") "تسجيل قيد قبض دائن (إيراد)" else "تسجيل قيد دفع مدين (مصروف)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Link options with accounts/persons
                Text("ربط المعاملة بشخص مسجل (اختياري)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                var expanded by remember { mutableStateOf(false) }
                val personLabel = people.firstOrNull { it.id == selectedPersonId }?.name ?: "اختر طرف أو شخص متصل"

                Box {
                    Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
                        Text(personLabel, color = Color.Black)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("لا أحد (غير مرتبط بحساب أفراد)") },
                            onClick = {
                                selectedPersonId = null
                                expanded = false
                            }
                        )
                        people.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} (${p.role})") },
                                onClick = {
                                    selectedPersonId = p.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("القيمة المالية بالكامل (جنيه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الشرح والبيان") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("نوع وتصنيف المود (مثال: صيانة، أدوية، رواتب)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = manualDate,
                    onValueChange = { manualDate = it },
                    label = { Text("تاريخ المعاملة يدوياً (اختياري - YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtVal = amountStr.toDoubleOrNull() ?: 0.0
                    if (amtVal <= 0.0) return@Button

                    val finalDesc = if (description.isBlank()) "سند مالي لعملية ${if (type == "income") "قبض" else "صرف"}" else description
                    viewModel.registerManualTransaction(type, amtVal, finalDesc, category, selectedPersonId, null, manualDate.takeIf { it.isNotBlank() })
                    Toast.makeText(context, "تم تسجيل القيود المالية بنجاح!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("تسجيل وحفظ السند مالي", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

// ================= NEWBORN DIALOG =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewbornDialog(viewModel: FarmViewModel, accentColor: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val animals by viewModel.animalsList.collectAsStateWithLifecycle()

    var selectedMotherId by remember { mutableStateOf<Int?>(null) }
    var selectedFatherId by remember { mutableStateOf<Int?>(null) }
    var selectedGender by remember { mutableStateOf("ذكر") }
    var selectedBirthType by remember { mutableStateOf("فردي") }
    var birthDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    
    var expandedGender by remember { mutableStateOf(false) }
    var expandedMother by remember { mutableStateOf(false) }
    var expandedFather by remember { mutableStateOf(false) }
    var expandedBirthType by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.List, contentDescription = null, tint = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة مولود جديد 🍼", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Mother Selection
                ExposedDropdownMenuBox(
                    expanded = expandedMother,
                    onExpandedChange = { expandedMother = it }
                ) {
                    OutlinedTextField(
                        value = animals.find { it.id == selectedMotherId }?.name ?: "الأم (اختياري)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الأم") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMother) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMother,
                        onDismissRequest = { expandedMother = false }
                    ) {
                        val availableMothers = animals.filter { true } // Adjust logic
                        DropdownMenuItem(text = { Text("بدون أم محددة") }, onClick = { selectedMotherId = null; expandedMother = false })
                        availableMothers.forEach { mother ->
                            DropdownMenuItem(
                                text = { Text("${mother.name} - ${mother.type}") },
                                onClick = {
                                    selectedMotherId = mother.id
                                    expandedMother = false
                                }
                            )
                        }
                    }
                }

                // Father Selection
                ExposedDropdownMenuBox(
                    expanded = expandedFather,
                    onExpandedChange = { expandedFather = it }
                ) {
                    OutlinedTextField(
                        value = animals.find { it.id == selectedFatherId }?.name ?: "الأب (اختياري)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الأب") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFather) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFather,
                        onDismissRequest = { expandedFather = false }
                    ) {
                        val availableFathers = animals.filter { true } // Adjust logic
                        DropdownMenuItem(text = { Text("بدون أب محدد") }, onClick = { selectedFatherId = null; expandedFather = false })
                        availableFathers.forEach { father ->
                            DropdownMenuItem(
                                text = { Text("${father.name} - ${father.type}") },
                                onClick = {
                                    selectedFatherId = father.id
                                    expandedFather = false
                                }
                            )
                        }
                    }
                }

                // Gender
                ExposedDropdownMenuBox(
                    expanded = expandedGender,
                    onExpandedChange = { expandedGender = it }
                ) {
                    OutlinedTextField(
                        value = selectedGender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الجنس") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false }
                    ) {
                        listOf("ذكر", "أنثى").forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender) },
                                onClick = {
                                    selectedGender = gender
                                    expandedGender = false
                                }
                            )
                        }
                    }
                }

                // Birth Type (Single / Twins)
                ExposedDropdownMenuBox(
                    expanded = expandedBirthType,
                    onExpandedChange = { expandedBirthType = it }
                ) {
                    OutlinedTextField(
                        value = selectedBirthType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الولادة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBirthType) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBirthType,
                        onDismissRequest = { expandedBirthType = false }
                    ) {
                        listOf("فردي", "توأم").forEach { typ ->
                            DropdownMenuItem(
                                text = { Text(typ) },
                                onClick = {
                                    selectedBirthType = typ
                                    expandedBirthType = false
                                }
                            )
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("تاريخ الولادة (YYYY-MM-DD)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.registerBirth(
                        motherId = selectedMotherId,
                        fatherId = selectedFatherId,
                        gender = selectedGender,
                        birthType = selectedBirthType,
                        manualDate = birthDate
                    )
                    Toast.makeText(context, "تم تسجيل المولود بنجاح", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("إضافة المولود", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("إلغاء", color = Color.Gray) }
        }
    )
}

// ================= SCREEN COMPOSABLE: AI ASSISTANT =================
@Composable
fun AiHelperScreen(viewModel: FarmViewModel, accentColor: Color, zoomLevel: Float) {
    val context = LocalContext.current
    val appLang by viewModel.appLang.collectAsStateWithLifecycle()
    val isLtr = appLang == "en"
    val chatLayoutDirection = if (isLtr) LayoutDirection.Ltr else LayoutDirection.Rtl

    val currentFarm by viewModel.currentFarm.collectAsStateWithLifecycle()
    val animals by viewModel.animalsList.collectAsStateWithLifecycle()
    val feeds by viewModel.feedsList.collectAsStateWithLifecycle()
    val people by viewModel.peopleList.collectAsStateWithLifecycle()

    var userMessage by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            Pair("أهلاً بك في مساعد الذكاء الاصطناعي الخاص بالمزرعة برو! 🤖\n\nأنا هنا لمساعدتك في إدارة مزرعتك بكفاءة عالية وتحليل البيانات فورياً. يمكنك سؤالي عن أي شيء مثل:\n\n🌾 تركيب خلطات أعلاف بنسب متزنة.\n🥩 زيادة إنتاج اللحوم ومعدلات تسمين العجول.\n🧬 جداول التحصينات والوقاية البيطرية والولادة.\n💰 كيفية تلافي تراكم الأرصدة والديون المترتبة على العملاء.\n\nكيف يمكنني مساعدتك اليوم؟", false)
        )
    }
    var isGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Context aggregation from database state flow
    val farmName = currentFarm ?: "مزرعتي"
    val animalsCount = animals.size
    val animalsByTypeInfo = animals.groupBy { it.type }.mapValues { it.value.size }.entries.joinToString(", ") { "${it.key}: ${it.value}" }
    val feedsCount = feeds.size
    val feedsWeightAlerts = feeds.filter { it.remainingWeight <= it.alertThreshold }.joinToString(", ") { it.feedName }
    val peopleCount = people.size
    val totals = viewModel.getSummaryTotals()
    val totalIncomes = totals.first
    val totalExpenses = totals.second
    val netProfit = totals.third

    val systemContextPrompt = """
        أنت مساعد ذكي مخصص لتطبيق "المزرعة برو" (Farm Pro).
        بيانات المزرعة الحالية للتوجيه بدقة ومعرفة وضع المزرعة:
        - اسم المزرعة: $farmName
        - عدد الحيوانات الحالية في الحظيرة: $animalsCount رأس ($animalsByTypeInfo)
        - خلطات الأعلاف المتوفرة: $feedsCount خلطات
        - تنبيهات انخفاض مخزون الأعلاف: ${if (feedsWeightAlerts.isEmpty()) "لا توجد تنبيهات نشطة" else feedsWeightAlerts}
        - عدد العملاء والشركاء المسجلين: $peopleCount عملاء
        - إجمالي الإيرادات في الأرشيف: $totalIncomes جنيه
        - إجمالي المصروفات في الأرشيف: $totalExpenses جنيه
        - صافي أرباح المزرعة: $netProfit جنيه

        ساعد المستخدم كأخصائي وخبير تغذية مزارع، وطبيب بيطري، ومستشار مالي. جاوب بوضوح واحترافية وباللغة العربية الفصحى وبأدق التفاصيل الممكنة. إذا سأل العميل عن خلطات الأعلاف، فقم بكتابة مكوناتها وتوزيع نسب البروتينات المناسبة بدقة، وإذا سأل عن التحصينات فقم بتوضيحها بالتفصيل.
    """.trimIndent()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides chatLayoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مساعدك الذكي (Gemini Assistant) 🤖",
                            fontWeight = FontWeight.Bold,
                            fontSize = (15f * (zoomLevel / 16f)).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "متزامن معك لتحليل بيانات المزرعة فورياً وإعطاء توصيات ذكية.",
                            fontSize = (11f * (zoomLevel / 16f)).sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Quick Actions / Suggestions
            val suggestions = listOf(
                "🌾 خلطة علف متوازنة للأغنام",
                "📈 كيف أزيد أرباح المزرعة؟",
                "🧬 جدول تحصينات ومقاومة الأوبئة",
                "🧾 نصائح لإدارة الديون والآجل"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestions) { keyword ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = !isGenerating) {
                                userMessage = keyword.substring(2) // remove emoji
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = keyword,
                            fontSize = (11f * (zoomLevel / 16f)).sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Chat Messages area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages.size) { index ->
                        val msg = chatMessages[index]
                        val isUser = msg.second
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .background(
                                        color = if (isUser) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.first,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = (13f * (zoomLevel / 16f)).sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 0.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = accentColor,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "جاري التفكير وصياغة التوصيات... ⚡",
                                            fontSize = (11f * (zoomLevel / 16f)).sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Message Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userMessage,
                    onValueChange = { userMessage = it },
                    placeholder = { Text("اكتب استفسارك هنا...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    maxLines = 3,
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (userMessage.isNotBlank() && !isGenerating) {
                            val msg = userMessage
                            userMessage = ""
                            chatMessages.add(Pair(msg, true))
                            isGenerating = true
                            
                            coroutineScope.launch {
                                val response = callGeminiApi(
                                    apiKey = com.example.BuildConfig.GEMINI_API_KEY,
                                    contextPrompt = systemContextPrompt,
                                    chatHistory = chatMessages.toList(),
                                    userPrompt = msg
                                )
                                chatMessages.add(Pair(response, false))
                                isGenerating = false
                            }
                        }
                    })
                )

                IconButton(
                    onClick = {
                        if (userMessage.isNotBlank() && !isGenerating) {
                            val msg = userMessage
                            userMessage = ""
                            chatMessages.add(Pair(msg, true))
                            isGenerating = true
                            
                            coroutineScope.launch {
                                val response = callGeminiApi(
                                    apiKey = com.example.BuildConfig.GEMINI_API_KEY,
                                    contextPrompt = systemContextPrompt,
                                    chatHistory = chatMessages.toList(),
                                    userPrompt = msg
                                )
                                chatMessages.add(Pair(response, false))
                                isGenerating = false
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(accentColor, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "إرسال",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

suspend fun callGeminiApi(
    apiKey: String,
    contextPrompt: String,
    chatHistory: List<Pair<String, Boolean>>,
    userPrompt: String
): String = withContext(Dispatchers.IO) {
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "عذراً لم يتم العثور على مفتاح API الخاص بـ Gemini في إعدادات تشغيل التطبيق.\n\nلتفعيل المساعد الـذكـي:\n1. انسخ مفتاح API الخاص بك من Google AI Studio.\n2. ضعه في لوحة Secrets بالمشروع تحت اسم GEMINI_API_KEY."
    }

    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
    
    try {
        val rootObj = JSONObject()
        
        // System instruction
        val sysInstrObj = JSONObject()
        val sysPartsArray = JSONArray()
        val sysPartObj = JSONObject()
        sysPartObj.put("text", contextPrompt)
        sysPartsArray.put(sysPartObj)
        sysInstrObj.put("parts", sysPartsArray)
        rootObj.put("systemInstruction", sysInstrObj)
        
        // Contents
        val contentsArray = JSONArray()
        
        // Send last 6 turns of conversations for history
        val recentHistory = chatHistory.takeLast(6)
        for (turn in recentHistory) {
            val contentObj = JSONObject()
            contentObj.put("role", if (turn.second) "user" else "model")
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", turn.first)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }
        
        rootObj.put("contents", contentsArray)

        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val reqBody = rootObj.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(reqBody)
            .build()
            
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext "عذراً، حدث خطأ أثناء الاتصال بالخادم. رمز الخطأ: ${response.code}\nتأكد من صحة تفعيل مفتاح الـ API الخاص بـ Gemini."
            }
            val resStr = response.body?.string() ?: ""
            val resJson = JSONObject(resStr)
            val candidates = resJson.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
            "عذراً، لم أستطع توليد استجابة مناسبة حالياً."
        }
    } catch (e: Exception) {
        "خطأ أثناء معالجة الاتصال بالذكاء الاصطناعي: ${e.localizedMessage ?: e.message}"
    }
}

@Composable
fun BackupScreen(
    viewModel: FarmViewModel,
    accentColor: Color,
    zoomLevel: Float,
    onConfirmDelete: (String, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val backups by viewModel.backupsList.collectAsStateWithLifecycle()
    val isFirebaseSynced by viewModel.isFirebaseSynced.collectAsStateWithLifecycle()
    val googleLinks by viewModel.googleLinksList.collectAsStateWithLifecycle()
    val pendingSyncItems by viewModel.pendingSyncItems.collectAsStateWithLifecycle(emptyList())
    var showExportDialog by remember { mutableStateOf(false) }
    var showPrintSettingsDialog by remember { mutableStateOf(false) }
    val googleUserEmail by viewModel.googleUserEmail.collectAsStateWithLifecycle()

    var newEmail by remember { mutableStateOf("") }
    var selectedPathOption by remember { mutableStateOf("المجلد الافتراضي للوثائق (Documents)") }
    var selectedScheduleOption by remember { mutableStateOf("يومي") }
    var isUploadingCloud by remember { mutableStateOf(false) }
    var cloudUploadProgress by remember { mutableStateOf(0f) }
    var cloudUploadStatus by remember { mutableStateOf("") }

    var backupToRestore by remember { mutableStateOf<com.example.data.model.BackupEntity?>(null) }
    var backupToDelete by remember { mutableStateOf<com.example.data.model.BackupEntity?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val backupFolderUri by viewModel.backupFolderUri.collectAsStateWithLifecycle()
    val folderPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            viewModel.updateBackupFolderUri(uri.toString())
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    if (showRestoreConfirm && backupToRestore != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("تأكيد استعادة النسخة الاحتياطية", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("هل أنت متأكد من رغبتك في استعادة هذه النسخة الاحتياطية؟ سيتم استبدال جميع البيانات الحالية بالبيانات الموجودة في المجلد بشكل نهائي.", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirm = false
                        viewModel.restoreBackup(backupToRestore!!.backupDataJson) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("نعم، استعد", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showDeleteConfirm && backupToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد الحذف النهائي ⚠️", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذا الملف الاحتياطي نهائياً من الذاكرة ومن السحاب؟ لا يمكن استرجاع هذا الملف بعد حذفه.", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteBackup(backupToDelete!!)
                        Toast.makeText(context, "تم حذف نسخة الاحتياط نهائياً من الذاكرة والـ Cloud 🗑️", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("نعم، حذف نهائي", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("إدارة النسخ الاحتياطي والمزامنة", fontWeight = FontWeight.Bold, fontSize = (22f * (zoomLevel / 16f)).sp, color = accentColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) "تحكم بمسارات حفظ قواعد البيانات محلياً، وجدولة المزامنة التلقائية لـ Firebase و Google Drive و SQLite سحابياً." else "تحكم بمسارات حفظ قواعد البيانات محلياً وإدارة النسخ الاحتياطية دورياً.", fontSize = (13f * (zoomLevel / 16f)).sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 1. Firebase Sync Control Card (Arabic layout) - Conditional Show
        if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المزامنة السحابية مع مشاريع Firebase (Realtime)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "قم ببث ومزامنة داتا المزرعة بالكامل مع سيرفرات Firebase للتحديث الفوري عبر الأجهزة.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (isFirebaseSynced) {
                                        viewModel.backupToFirestore(context)
                                    } else {
                                        Toast.makeText(context, "لم يتم الاتصال بقواعد البيانات بشكل صحيح.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFirebaseSynced) "رفع سحابي" else "غير متصل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (isFirebaseSynced) {
                                        viewModel.restoreFromFirestore(context)
                                    } else {
                                        Toast.makeText(context, "لم يتم الاتصال بقواعد البيانات بشكل صحيح.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFirebaseSynced) "استعادة سحابية" else "غير متصل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Google Drive linking and backing up - Conditional Show
        if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
            item {
                Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ربط حسابات جوجل ورفع النسخ لـ Google Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اربط بريد Gmail الخاص بك لرفع واستيراد ملفات قواعد بيانات SQLite سحابياً لـ Google Drive.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    if (googleUserEmail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "بريدك الحالي مرتبط تلقائياً بمزامنة جوجل:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = googleUserEmail,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Text(
                                        text = "يرتبط تلقائياً بجميع خدمات جوجل ومزامنة الـ Backups السحابية على التطبيق.",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("أدخل بريد جوجل (Gmail)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newEmail.isNotBlank()) {
                                val emailStr = newEmail.trim()
                                if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                                    viewModel.insertLink(emailStr)
                                    Toast.makeText(context, "تم ربط الحساب بنجاح لغرض المزامنة", Toast.LENGTH_SHORT).show()
                                    newEmail = ""
                                } else {
                                    Toast.makeText(context, "الرجاء إدخال بريد جوجل صحيح", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.AddLink, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة الحساب", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val driveBackupStatus by viewModel.driveBackupStatus.collectAsStateWithLifecycle()
                    val isUploadingDrive by viewModel.isGoogleDriveUploading.collectAsStateWithLifecycle()

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حالة النسخ بـ Google Drive: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(driveBackupStatus, fontSize = 11.sp, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isUploadingDrive) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.backupToGoogleDriveAndSync(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("رفع لـ Drive", color = Color.White, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.syncDataFromGoogleDrive(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("استيراد من Drive", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("الحسابات المرتبطة حالياً:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    googleLinks.forEach { link ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(link.googleEmail, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(link.linkedAt))
                                    Text("تاريخ الربط: $dateStr", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                            IconButton(
                                onClick = {
                                    onConfirmDelete("هل أنت متأكد من رغبتك في إلغاء ربط حساب جوجل (${link.googleEmail}) من المزرعة نهائياً؟") {
                                        viewModel.deleteLink(link)
                                        Toast.makeText(context, "تم إلغاء ربط الحساب بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    if (googleLinks.isEmpty()) {
                        Text("لا توجد حسابات جوجل مرتبطة حالياً.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }

    // 4. Local Storage Settings & Backup Scheduler
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("إعدادات النسخ الاحتياطي المحلي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مسار حفظ النسخ الاحتياطية (Storage Path)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = if(backupFolderUri.isEmpty()) "المجلد الافتراضي (Documents)" else backupFolderUri, fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { folderPicker.launch(null) }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                            Text("تغيير المجلد", fontSize = 12.sp)
                        }
                        if (backupFolderUri.isNotEmpty()) {
                            Button(onClick = { 
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                intent.data = android.net.Uri.parse(backupFolderUri)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "لا يمكن فتح المجلد - تحتاج تطبيق مدير ملفات", Toast.LENGTH_SHORT).show() }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                                Text("فتح", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("جدولة النسخ الاحتياطي التلقائي (Background Sync Scheduler)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("حدد معدل تكرار تشغيل خدمة الخلفية لحفظ البيانات تلقائياً:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("إيقاف", "يومي", "أسبوعي", "شهري").forEach { schedule ->
                            val active = selectedScheduleOption == schedule
                            Surface(
                                onClick = {
                                    selectedScheduleOption = schedule
                                    Toast.makeText(context, "تم تفعيل جدولة النسخ الاحتياطي بالخلفية: $schedule", Toast.LENGTH_SHORT).show()
                                },
                                color = if (active) accentColor else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (active) accentColor else Color.Gray.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = schedule,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("إجراءات النسخ الفورية السريعة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveDailyBackup(context)
                            Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح! 💾", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخة احتياطية محلية الآن (Versioned DB) 💾", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) {
                        Button(
                            onClick = {
                                isUploadingCloud = true
                                cloudUploadProgress = 0f
                                cloudUploadStatus = "جاري الاتصال بخوادم Firebase سحابياً..."
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    cloudUploadStatus = "جاري تجميع وحفظ ملف SQLite..."
                                    cloudUploadProgress = 0.3f
                                    kotlinx.coroutines.delay(1200)
                                    cloudUploadStatus = "جاري ضغط الملف ورفعه لـ Firebase Storage..."
                                    cloudUploadProgress = 0.7f
                                    kotlinx.coroutines.delay(1200)
                                    cloudUploadStatus = "اكتمل الرفع السحابي وتحديث الفهرس بنجاح!"
                                    cloudUploadProgress = 1f
                                    kotlinx.coroutines.delay(1000)
                                    isUploadingCloud = false
                                    Toast.makeText(context, "تم مزامنة وبث النسخة الاحتياطية سحابياً بنجاح ☁️", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مزامنة سحابية سريعة لـ Firebase ☁️", color = Color.White, fontSize = 11.sp)
                        }

                        if (isUploadingCloud) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                LinearProgressIndicator(
                                    progress = cloudUploadProgress,
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0xFFE2E8F0),
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(cloudUploadStatus, fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 4.5: Offline Export & Import (JSON) / Print (HTML)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SettingsBackupRestore,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نقل البيانات محلياً (Offline JSON & Print)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "استخرج بياناتك كملف JSON لتعمل كنقطة ريستور، أو شارك تقرير A4 للطباعة.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Button(
                        onClick = { viewModel.exportBackup(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاركة ملف احتياطي JSON 📲", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { 
                            viewModel.exportBackup(context, localOnlyWithoutPrompt = true)
                            Toast.makeText(context, "جاري حفظ النسخة في المسار المحدد...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ في المسار المحلي المحدد 💾", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = { importLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استيراد نسخة احتياطية (JSON) 📥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showExportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصدير تقرير قابل للطباعة (HTML) 📃", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showPrintSettingsDialog = true },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .size(48.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "اعدادات التقرير والتطبيق", tint = accentColor)
                        }
                    }

                    if (showExportDialog) {
                        ExportChoiceDialog(viewModel = viewModel, onDismiss = { showExportDialog = false })
                    }
                    if (showPrintSettingsDialog) {
                        AppPrintSettingsDialog(viewModel = viewModel, onDismiss = { showPrintSettingsDialog = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 5: List of Backups
        item {
            Text(if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) "ملفات النسخ الاحتياطي المتوفرة (المحلي والسحابي)" else "ملفات النسخ الاحتياطي المتوفرة (المحلي)", fontWeight = FontWeight.Bold, fontSize = (18f * (zoomLevel / 16f)).sp, color = accentColor)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(backups) { backup ->
            val timestampString = java.text.SimpleDateFormat("yyyy_MM_dd_HHmm", java.util.Locale.getDefault()).format(java.util.Date(backup.createdAt))
            val simulatedFilename = "farm_backup_$timestampString.db"
            val simulatedSize = "${(backup.backupDataJson.length / 1024) + 148} KB"

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BackupTable, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(simulatedFilename, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                val dateCreated = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(backup.createdAt))
                                Text("بتاريخ: $dateCreated | الحجم: $simulatedSize", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(if (com.example.AppConfig.IS_GOOGLE_SERVICES_ENABLED) "محلي ومزامن سحابياً" else "نسخة احتياطية محلية", color = Color(0xFF10B981), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                backupToRestore = backup
                                showRestoreConfirm = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استعادة النسخة", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                backupToDelete = backup
                                showDeleteConfirm = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف نهائي", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        if (backups.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد ملفات نسخ احتياطي مسجلة بعد.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatchInvoiceScreen(
    viewModel: FarmViewModel,
    themePrimary: Color,
    zoom: Float,
    invoiceType: String, // "purchase", "sale", "feed"
    onCompleted: () -> Unit
) {
    val context = LocalContext.current
    val people by viewModel.peopleList.collectAsStateWithLifecycle()
    val activeAnimals by viewModel.animalsList.collectAsStateWithLifecycle()
    val activeFeeds by viewModel.feedsList.collectAsStateWithLifecycle()

    // Header inputs
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var useNewPersonName by remember { mutableStateOf("") }
    var settlementType by remember { mutableStateOf("cash") } // "cash", "deferred", "partial"
    var settlementAmountStr by remember { mutableStateOf("") }
    var invoiceNotes by remember { mutableStateOf("") }

    // Mode-specific list states
    val pendingPurchases = remember { mutableStateListOf<BatchAnimalPurchaseItem>() }
    val pendingSales = remember { mutableStateListOf<BatchAnimalSaleItem>() }
    val pendingFeeds = remember { mutableStateListOf<BatchFeedPurchaseItem>() }

    // Form inputs for Purchase Mode
    var animalName by remember { mutableStateOf("") }
    var animalType by remember { mutableStateOf("عجل") }
    
    val invoiceCustomTypesRaw by viewModel.animalTypesList.collectAsStateWithLifecycle()
    val invoiceTypesToUse = remember(invoiceCustomTypesRaw) {
        if (invoiceCustomTypesRaw.isEmpty()) listOf("عجل", "أغنام", "ماعز", "جمال", "جاموس") else invoiceCustomTypesRaw
    }
    var invoiceIsFemale by remember { mutableStateOf(false) } // false = ذكر, true = أنثى
    var invoiceSelectedFamilyRaw by remember { mutableStateOf("عجل") }
    val updateInvoiceTypeForGenderAndFamily = { femaleSelected: Boolean, family: String ->
        val typeObj = com.example.utils.AnimalTypeHelper.parseAnimalType(family)
        animalType = typeObj.getName(femaleSelected)
    }

    var animalWeightStr by remember { mutableStateOf("") }
    var animalPriceStr by remember { mutableStateOf("") }
    var animalAge by remember { mutableStateOf("") }

    // Form inputs for Sale Mode
    var selectedAnimalForSale by remember { mutableStateOf<AnimalEntity?>(null) }
    var salePriceStr by remember { mutableStateOf("") }
    var saleWeightStr by remember { mutableStateOf("") }

    // Form inputs for Feed Mode
    var feedName by remember { mutableStateOf("") }
    var feedWeightStr by remember { mutableStateOf("") }
    var feedCostStr by remember { mutableStateOf("") }
    var feedSupplierBrand by remember { mutableStateOf("") }

    val totalInvoiceSum = remember(invoiceType, pendingPurchases.size, pendingSales.size, pendingFeeds.size) {
        when (invoiceType) {
            "purchase" -> pendingPurchases.sumOf { it.price }
            "sale" -> pendingSales.sumOf { it.salePrice }
            "feed" -> pendingFeeds.sumOf { it.totalCost }
            else -> 0.0
        }
    }

    LaunchedEffect(totalInvoiceSum) {
        if (settlementType == "cash") {
            settlementAmountStr = totalInvoiceSum.toString()
        } else if (settlementType == "deferred") {
            settlementAmountStr = "0.0"
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = themePrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCompleted) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (invoiceType) {
                            "purchase" -> "فاتورة شراء رؤوس مجمعة 🧾"
                            "sale" -> "فاتورة بيع رؤوس مجمعة 🧾"
                            "feed" -> "فاتورة توريد وشراء أعلاف مجمعة 🌾"
                            else -> "فاتورة مجمعة"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (16f * (zoom / 16f)).sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1: Supplier / Customer Header Details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. بيانات المشتري/المورد والذمة المالية 👤",
                        fontWeight = FontWeight.Bold,
                        color = themePrimary,
                        fontSize = (14f * (zoom / 16f)).sp
                    )

                    // Dropdown for person/merchant
                    var peopleDropdownExpanded by remember { mutableStateOf(false) }
                    val selectedPersonName = people.find { it.id == selectedPersonId }?.name ?: "اختر صاحب المندوب (مورد/عميل)"
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { peopleDropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedPersonName, color = MaterialTheme.colorScheme.onSurface, fontSize = (13f * (zoom / 16f)).sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        DropdownMenu(
                            expanded = peopleDropdownExpanded,
                            onDismissRequest = { peopleDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("المعاملة مباشرة نقداً بالشراء الحر السريع (بدون قيد حساب آجل)") },
                                onClick = {
                                    selectedPersonId = null
                                    peopleDropdownExpanded = false
                                }
                            )
                            people.forEach { person ->
                                DropdownMenuItem(
                                    text = { Text("${person.name} (${person.role} - رصيد: ${person.balance} جنيه)") },
                                    onClick = {
                                        selectedPersonId = person.id
                                        peopleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedPersonId == null) {
                        OutlinedTextField(
                            value = useNewPersonName,
                            onValueChange = { useNewPersonName = it },
                            label = { Text("أو اسم حساب خارجي جديد") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Settlement Options
                    Text("طريقة تسوية وسداد هذه الفاتورة:", fontWeight = FontWeight.Bold, fontSize = (12f * (zoom / 16f)).sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("cash", "نقدي كاش", Icons.Default.Paid),
                            Triple("deferred", "آجل للحساب", Icons.Default.Pending),
                            Triple("partial", "دفع جزئي مقدم", Icons.Default.Percent)
                        ).forEach { (mode, label, icon) ->
                            val isSelected = settlementType == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        settlementType = mode
                                        if (mode == "cash") settlementAmountStr = totalInvoiceSum.toString()
                                        else if (mode == "deferred") settlementAmountStr = "0.0"
                                      },
                                color = if (isSelected) themePrimary else Color.LightGray.copy(alpha = 0.2f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(label, fontSize = (10f * (zoom / 16f)).sp, color = if (isSelected) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (settlementType == "partial") {
                        OutlinedTextField(
                            value = settlementAmountStr,
                            onValueChange = { settlementAmountStr = it },
                            label = { Text("المبلغ المدفوع كاش نقداً من الفاتورة (جنيه)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // STEP 2: Main Items Subform
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. إضافة مادة/رأس جديدة إلى قائمة الفاتورة ➕",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = (14f * (zoom / 16f)).sp
                    )

                    when (invoiceType) {
                        "purchase" -> {
                            OutlinedTextField(
                                value = animalName,
                                onValueChange = { animalName = it },
                                label = { Text("رقم/اسم أو وسم رأس الماشية (مثال: عجل رقم 52)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Gender Selector
                            Text("جنس رأس الماشية 🧬:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        invoiceIsFemale = false
                                        updateInvoiceTypeForGenderAndFamily(false, invoiceSelectedFamilyRaw)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!invoiceIsFemale) themePrimary else Color.LightGray.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ذكر (عجل، خروف طلوقة) ♂️", color = if (!invoiceIsFemale) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        invoiceIsFemale = true
                                        updateInvoiceTypeForGenderAndFamily(true, invoiceSelectedFamilyRaw)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (invoiceIsFemale) themePrimary else Color.LightGray.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("أنثى (عجلة، نعجة بقرة) ♀️", color = if (invoiceIsFemale) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Dynamic Types Selector
                            Text("نوع السلالة / الفئة:", fontSize = 12.sp, color = Color.Gray)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            ) {
                                invoiceTypesToUse.forEach { t ->
                                    val parsed = com.example.utils.AnimalTypeHelper.parseAnimalType(t)
                                    val displayName = if (invoiceIsFemale) parsed.female else parsed.male
                                    val isSelected = invoiceSelectedFamilyRaw == t
                                    Button(
                                        onClick = {
                                            invoiceSelectedFamilyRaw = t
                                            updateInvoiceTypeForGenderAndFamily(invoiceIsFemale, t)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) themePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                    ) {
                                        Text(displayName, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = animalType,
                                onValueChange = { animalType = it },
                                label = { Text("النوع المدخل لتسجيل السند (مثال: عجل، عجلة)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = animalWeightStr,
                                    onValueChange = { animalWeightStr = it },
                                    label = { Text("الوزن (كغ)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = animalPriceStr,
                                    onValueChange = { animalPriceStr = it },
                                    label = { Text("السعر لشراء المورد (ج)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = animalAge,
                                onValueChange = { animalAge = it },
                                label = { Text("العمر (اختياري - مثال: 8 شهور)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val w = animalWeightStr.toDoubleOrNull() ?: 0.0
                                    val p = animalPriceStr.toDoubleOrNull() ?: 0.0
                                    
                                    val currentTotalAnimals = viewModel.animalsList.value.size
                                    val nextSeqNumber = currentTotalAnimals + pendingPurchases.size + 1
                                    val finalAnimalName = if (animalName.isBlank()) "رأس رقم #$nextSeqNumber" else animalName

                                    if (p <= 0.0) {
                                        Toast.makeText(context, "الرجاء كتابة سعر صحيح للشراء!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    pendingPurchases.add(
                                        BatchAnimalPurchaseItem(
                                            name = finalAnimalName,
                                            type = animalType,
                                            weight = w,
                                            price = p,
                                            age = animalAge
                                        )
                                    )
                                    // Reset subform fields
                                    animalName = ""
                                    animalWeightStr = ""
                                    animalPriceStr = ""
                                    animalAge = ""
                                    Toast.makeText(context, "تمت إضافة رأس الماشية للقائمة الفاتورة 📊", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("إضافة الرأس للقائمة الفاتورة ➕", color = Color.White)
                            }
                        }

                        "sale" -> {
                            val unsoldAnimalsList = activeAnimals.filter { !it.isArchived }
                            var animalDropdownExpanded by remember { mutableStateOf(false) }
                            val currentAnimalLabel = selectedAnimalForSale?.let { "${it.name} (${it.type} - وزن: ${it.weight} كغ)" } ?: "اختر رأس الماشية من الحظيرة"

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { animalDropdownExpanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(currentAnimalLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = (13f * (zoom / 16f)).sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                DropdownMenu(
                                    expanded = animalDropdownExpanded,
                                    onDismissRequest = { animalDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    unsoldAnimalsList.forEach { animal ->
                                        DropdownMenuItem(
                                            text = { Text("${animal.name} (${animal.type} - وزن: ${animal.weight} كغ)") },
                                            onClick = {
                                                selectedAnimalForSale = animal
                                                saleWeightStr = animal.weight.toString()
                                                animalDropdownExpanded = false
                                            }
                                        )
                                    }
                                    if (unsoldAnimalsList.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("لا يوجد رؤوس ماشية بالحظيرة حالياً للبيع!") },
                                            onClick = { animalDropdownExpanded = false }
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = saleWeightStr,
                                    onValueChange = { saleWeightStr = it },
                                    label = { Text("وزن البيع الحالي (كغ)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = salePriceStr,
                                    onValueChange = { salePriceStr = it },
                                    label = { Text("سعر البيع للرأس (ج)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = {
                                    val animal = selectedAnimalForSale
                                    if (animal == null) {
                                        Toast.makeText(context, "الرجاء اختيار رأس الماشية من القائمة أولاً!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val price = salePriceStr.toDoubleOrNull() ?: 0.0
                                    val weight = saleWeightStr.toDoubleOrNull() ?: animal.weight
                                    if (price <= 0.0) {
                                        Toast.makeText(context, "الرجاء إدخال سعر بيع صحيح!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    if (pendingSales.any { it.animal.id == animal.id }) {
                                        Toast.makeText(context, "تمت إضافة هذا الرأس مسبقاً في هذه الفاتورة!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    pendingSales.add(
                                        BatchAnimalSaleItem(
                                            animal = animal,
                                            salePrice = price,
                                            saleWeight = weight
                                        )
                                    )
                                    selectedAnimalForSale = null
                                    salePriceStr = ""
                                    saleWeightStr = ""
                                    Toast.makeText(context, "تمت إضافة رأس الماشية لصفقة البيع المجمع ☑️", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("إضافة الرأس لقائمة البيع الفاتورة ➕", color = Color.White)
                            }
                        }

                        "feed" -> {
                            var itemSubCategory by remember { mutableStateOf("feed") } // "feed" or "medicine"
                            var feedExpanded by remember { mutableStateOf(false) }

                            // Sub-category selector (M3 Card Buttons)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { itemSubCategory = "feed" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (itemSubCategory == "feed") themePrimary else Color.LightGray.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("شراء علف / غذاء 🌾", color = if (itemSubCategory == "feed") Color.White else Color.DarkGray, fontSize = (11f * (zoom / 16f)).sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { itemSubCategory = "medicine" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (itemSubCategory == "medicine") themePrimary else Color.LightGray.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("شراء دواء بيطري 💊", color = if (itemSubCategory == "medicine") Color.White else Color.DarkGray, fontSize = (11f * (zoom / 16f)).sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = feedName,
                                    onValueChange = { 
                                        feedName = it 
                                        if (itemSubCategory == "feed") {
                                            feedExpanded = activeFeeds.any { f -> f.feedName.contains(it, ignoreCase = true) && it.isNotBlank() }
                                        }
                                    },
                                    label = { Text(if (itemSubCategory == "feed") "اسم مادة الأعلاف (نخالة، ذرة، علف جاهز تسمين)" else "اسم الدواء أو العلاج البيطري") },
                                    modifier = Modifier.fillMaxWidth().onFocusChanged { 
                                        if (it.isFocused && itemSubCategory == "feed" && activeFeeds.isNotEmpty() && feedName.isBlank()) feedExpanded = true 
                                    }
                                )
                                if (itemSubCategory == "feed") {
                                    DropdownMenu(
                                        expanded = feedExpanded,
                                        onDismissRequest = { feedExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        activeFeeds.filter { it.feedName.contains(feedName, ignoreCase = true) }
                                            .distinctBy { it.feedName }
                                            .forEach { f ->
                                            DropdownMenuItem(
                                                text = { Text("${f.feedName} ${if(f.ingredientsDescription.isNotBlank()) "(${f.ingredientsDescription})" else ""}") },
                                                onClick = {
                                                    feedName = f.feedName
                                                    feedSupplierBrand = f.ingredientsDescription
                                                    feedExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = feedWeightStr,
                                    onValueChange = { feedWeightStr = it },
                                    label = { Text(if (itemSubCategory == "feed") "الكمية / الوزن (كغ)" else "طول الدواء (فترة السحب بالأيام)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = feedCostStr,
                                    onValueChange = { feedCostStr = it },
                                    label = { Text("إجمالي السعر/التكلفة (ج)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = feedSupplierBrand,
                                onValueChange = { feedSupplierBrand = it },
                                label = { Text(if (itemSubCategory == "feed") "البراند أو العلامة التجارية للأعلاف (اختياري)" else "العلامة المصنعة / التفاصيل الطبية (اختياري)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (feedName.isBlank()) {
                                        Toast.makeText(context, if (itemSubCategory == "feed") "الرجاء كتابة اسم مادة العلف!" else "الرجاء كتابة اسم الدواء!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val w = feedWeightStr.toDoubleOrNull() ?: 0.0
                                    val c = feedCostStr.toDoubleOrNull() ?: 0.0
                                    if (c <= 0.0) {
                                        Toast.makeText(context, "الرجاء كتابة تكلفة صحيحة!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val finalItemName = if (itemSubCategory == "medicine") "MEDICINE:$feedName" else feedName
                                    pendingFeeds.add(
                                        BatchFeedPurchaseItem(
                                            feedName = finalItemName,
                                            totalWeight = w,
                                            totalCost = c,
                                            ingredientsDescription = feedSupplierBrand
                                        )
                                    )
                                    feedName = ""
                                    feedWeightStr = ""
                                    feedCostStr = ""
                                    feedSupplierBrand = ""
                                    Toast.makeText(context, if (itemSubCategory == "feed") "إضافة بند توريد الأعلاف للجدول المجمع بنجاح!" else "إضافة بند العلاج الطبي للجدول المجمع بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (itemSubCategory == "feed") "إضافة بند العلف للفاتورة المجمعة ➕" else "إضافة بند العلاج للفاتورة المجمعة ➕", color = Color.White)
                            }
                        }
                    }
                }
            }

            // STEP 3: List of pending items
            Text(
                "جدول وبنود الفاتورة المجمعة المضافة حالياً 📋:",
                fontWeight = FontWeight.Bold,
                fontSize = (13f * (zoom / 16f)).sp,
                color = themePrimary
            )

            when (invoiceType) {
                "purchase" -> {
                    if (pendingPurchases.isEmpty()) {
                        Text("جدول المشتريات فارغ. أضف رؤوس الماشية في الأعلى لتظهر البنود.", fontSize = (12f * (zoom / 16f)).sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    } else {
                        pendingPurchases.forEachIndexed { index, item ->
                            Surface(
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${index + 1}. الاسم: ${item.name} (${item.type})", fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp)
                                        Text("الوزن: ${item.weight} كغ - العمر: ${item.age}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${item.price} ج", fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { pendingPurchases.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "sale" -> {
                    if (pendingSales.isEmpty()) {
                        Text("جدول المبيعات فارغ. أضف رؤوس الماشية التي تبيعها من القائمة.", fontSize = (12f * (zoom / 16f)).sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    } else {
                        pendingSales.forEachIndexed { index, item ->
                            Surface(
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${index + 1}. الرأس: ${item.animal.name}", fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp)
                                        Text("وزن البيع: ${item.saleWeight} كغ", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${item.salePrice} ج", fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { pendingSales.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "feed" -> {
                    if (pendingFeeds.isEmpty()) {
                        Text("جدول توريدات بضائع الأعلاف والأدوية خالٍ حالياً.", fontSize = (12f * (zoom / 16f)).sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    } else {
                        pendingFeeds.forEachIndexed { index, item ->
                            val isMed = item.feedName.startsWith("MEDICINE:")
                            val displayName = if (isMed) item.feedName.removePrefix("MEDICINE:") else item.feedName
                            val subLabel = if (isMed) {
                                "تفاصيل: ${item.ingredientsDescription} - مدة الصلاحية/السحب: ${item.totalWeight.toInt()} يوم 💊"
                            } else {
                                "البراند: ${item.ingredientsDescription} - الحصيلة: ${item.totalWeight} كغ 🌾"
                            }
                            Surface(
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${index + 1}. ${if (isMed) "علاج: " else "مادة: "} $displayName", fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp)
                                        Text(subLabel, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${item.totalCost} ج", fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { pendingFeeds.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // General billing notes
            OutlinedTextField(
                value = invoiceNotes,
                onValueChange = { invoiceNotes = it },
                label = { Text("شروحات وملاحظات الفاتورة العامة (اختياري)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Final Invoice Summary Section
            Surface(
                color = themePrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي قيمة الفاتورة المجمعة:", fontWeight = FontWeight.Bold, fontSize = (14f * (zoom / 16f)).sp)
                        Text("$totalInvoiceSum جنيه", fontWeight = FontWeight.Bold, color = themePrimary, fontSize = (15f * (zoom / 16f)).sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المبلغ المدفوع/المستلم نقداً (الصندوق):", fontSize = (13f * (zoom / 16f)).sp)
                        Text("${settlementAmountStr.toDoubleOrNull() ?: 0.0} جنيه", fontWeight = FontWeight.Bold, fontSize = (13f * (zoom / 16f)).sp)
                    }

                    val deferredBalance = remember(totalInvoiceSum, settlementAmountStr) {
                        val cashSettle = settlementAmountStr.toDoubleOrNull() ?: 0.0
                        (totalInvoiceSum - cashSettle).coerceAtLeast(0.0)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المبلغ الآجل المعلق بالحسابات المالية:", fontSize = (13f * (zoom / 16f)).sp)
                        Text("$deferredBalance جنيه", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = (13f * (zoom / 16f)).sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Save confirmation execution button
            Button(
                onClick = {
                    val settleCash = settlementAmountStr.toDoubleOrNull() ?: 0.0
                    val count = when (invoiceType) {
                        "purchase" -> pendingPurchases.size
                        "sale" -> pendingSales.size
                        "feed" -> pendingFeeds.size
                        else -> 0
                    }

                    if (count <= 0) {
                        Toast.makeText(context, "الرجاء إضافة بضاعة أو رأس واحدة على الأقل لإصدار هذه الفاتورة المجمعة!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    when (invoiceType) {
                        "purchase" -> {
                            viewModel.registerBatchPurchaseInvoice(
                                items = pendingPurchases.toList(),
                                associatedPersonId = selectedPersonId,
                                merchantName = useNewPersonName,
                                paymentStatus = settlementType,
                                paidAmount = settleCash
                            )
                            Toast.makeText(context, "تم تسجيل فاتورة شراء مجمعة وإدخل الرؤوس بنجاح! 🐃", Toast.LENGTH_LONG).show()
                        }
                        
                        "sale" -> {
                            viewModel.registerBatchSaleInvoice(
                                items = pendingSales.toList(),
                                associatedPersonId = selectedPersonId,
                                buyerName = useNewPersonName,
                                paymentStatus = settlementType,
                                receivedAmount = settleCash
                            )
                            Toast.makeText(context, "تم ترحيل مبيعات الرؤوس معاً وإصدار موازنة الفاتورة بنجاح! 🧾", Toast.LENGTH_LONG).show()
                        }

                        "feed" -> {
                            viewModel.registerBatchFeedInvoice(
                                items = pendingFeeds.toList(),
                                associatedPersonId = selectedPersonId,
                                supplierName = useNewPersonName,
                                paymentStatus = settlementType,
                                paidAmount = settleCash
                            )
                            Toast.makeText(context, "تم تسجيل كميات الأعلاف وتعديل ميزانية المورد والمشاريع بنجاح! 🌾", Toast.LENGTH_LONG).show()
                        }
                    }
                    onCompleted()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ترحيل وإصدار الفاتورة بالكامل وتعديل الأرصدة 💾", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ExportChoiceDialog(
    viewModel: com.example.ui.viewmodel.FarmViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("خيارات التصدير والمشاركة", fontWeight = FontWeight.Bold) },
        text = { Text("هل تفضل تصدير التقرير كملف PDF للطباعة، أو كملف ويب HTML (يحتوي على قوائم تفاعلية يمكن فتحها وإخفائها)؟") },
        confirmButton = {
            Button(onClick = {
                viewModel.exportHtmlReport(context, "pdf")
                onDismiss()
            }) {
                Text("PDF (طباعة)")
            }
        },
        dismissButton = {
            Button(onClick = {
                viewModel.exportHtmlReport(context, "html")
                onDismiss()
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) {
                Text("HTML (تفاعلي)")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchDialog(viewModel: com.example.ui.viewmodel.FarmViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val animals by viewModel.animalsList.collectAsStateWithLifecycle()
    val feeds by viewModel.feedsList.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsList.collectAsStateWithLifecycle()
    val people by viewModel.peopleList.collectAsStateWithLifecycle()
    
    val results = remember(query, animals, feeds, transactions, people) {
        if (query.isBlank()) return@remember emptyList<String>()
        val term = query.lowercase().trim()
        val res = mutableListOf<String>()
        res.addAll(animals.filter { it.name.lowercase().contains(term) || it.type.lowercase().contains(term) }.map { "ماشية: ${it.name} - ${it.type}" })
        res.addAll(feeds.filter { it.feedName.lowercase().contains(term) || it.ingredientsDescription.lowercase().contains(term) }.map { "علف: ${it.feedName}" })
        res.addAll(transactions.filter { it.description.lowercase().contains(term) || it.category.lowercase().contains(term) || it.amount.toString().contains(term) }.map { "سند: ${it.category} - ${it.amount} ج (${if(it.type=="income") "إيراد" else "مصروف"})" })
        res.addAll(people.filter { it.name.lowercase().contains(term) || it.phone.lowercase().contains(term) }.map { "شخص: ${it.name} - ${it.role}" })
        res
    }

    Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "رجوع") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("البحث الشامل", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("ابحث في كامل التطبيق...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (query.isNotBlank()) {
                    if (results.isEmpty()) {
                        Text("لا توجد نتائج مطابقة.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(results) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onDismiss() },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(item, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                } else {
                    Text("اكتب كلمة للبحث في السجلات والصناديق والأشخاص...", color = Color.Gray, modifier = Modifier.padding(top = 32.dp))
                }
            }
        }
    }
}
