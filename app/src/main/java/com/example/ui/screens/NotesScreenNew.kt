package com.example.ui.screens

import androidx.compose.ui.window.Dialog
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.NoteAttachment
import com.example.NoteAttachmentHelper
import com.example.data.model.NoteEntity
import com.example.ui.viewmodel.FarmViewModel
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreenNew(
    viewModel: FarmViewModel,
    appAccentColor: Color,
    onBack: () -> Unit
) {
    val notes by viewModel.notesList.collectAsStateWithLifecycle()
    val isRtl by viewModel.attendanceIsRtl.collectAsStateWithLifecycle() // Reuse RTL state if preferred, or hardcode true for Arabic
    val dialogColorHex by viewModel.notesSettingsColorHex.collectAsStateWithLifecycle()
    val dialogTextColorHex by viewModel.notesSettingsTextColorHex.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var showSettings by remember { mutableStateOf(false) }
    var showAddNote by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    
    // UI state
    val mainBgColor = Color(0xFFF1F5F9) // slate-100
    
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr) {
        if (showAddNote || noteToEdit != null) {
            // Full Screen Editor
            FullNoteEditor(
                note = noteToEdit,
                onSave = { entity ->
                    if (noteToEdit == null) {
                        viewModel.registerNote(entity.title, entity.content, entity.imageBase64, entity.colorHex, entity.isPinned)
                    } else {
                        viewModel.updateNoteRecord(noteToEdit!!.copy(
                            title = entity.title,
                            content = entity.content,
                            imageBase64 = entity.imageBase64,
                            colorHex = entity.colorHex,
                            isPinned = entity.isPinned
                        ))
                    }
                    showAddNote = false
                    noteToEdit = null
                },
                onCancel = {
                    showAddNote = false
                    noteToEdit = null
                },
                accentColor = appAccentColor
            )
        } else {
            // Main Notes Grid
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📝", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("مذكراتي الذكية", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text("أفكارك، مهامك، ورسوماتك في مكان واحد", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            Button(
                                onClick = { showAddNote = true },
                                colors = ButtonDefaults.buttonColors(containerColor = appAccentColor),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = mainBgColor)
                    )
                },
                containerColor = mainBgColor
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    if (notes.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("لا توجد مذكرات بعد! انقر على '+' للبدء.", color = Color.Gray)
                        }
                    } else {
                        // Keep pinned logic, we just sort it so pinned is first
                        val sortedNotes = notes.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.id })
                        
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Adaptive(160.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp
                        ) {
                            items(sortedNotes.size) { index ->
                                val note = sortedNotes[index]
                                NoteCard(
                                    note = note,
                                    onEdit = { noteToEdit = note },
                                    onDelete = { viewModel.deleteNoteRecord(note) },
                                    onTogglePin = { viewModel.updateNoteRecord(note.copy(isPinned = !note.isPinned)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        NotesSettingsDialog(
            viewModel = viewModel,
            dialogColorHex = dialogColorHex,
            dialogTextColorHex = dialogTextColorHex,
            accentColor = appAccentColor,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun NoteCard(note: NoteEntity, onEdit: () -> Unit, onDelete: () -> Unit, onTogglePin: () -> Unit) {
    val defaultSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val bgColor = try { if(note.colorHex.isNotEmpty()) Color(android.graphics.Color.parseColor(note.colorHex)) else defaultSurfaceVariant } catch(e: Exception) { defaultSurfaceVariant }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                if (note.title.isNotEmpty()) {
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                IconButton(onClick = onTogglePin, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if(note.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                        contentDescription = "تثبيت",
                        tint = if(note.isPinned) Color(0xFF2563EB) else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Image if present
            if (!note.imageBase64.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val attachments = NoteAttachmentHelper.fromString(note.imageBase64)
                if (attachments.isNotEmpty() && attachments[0].base64.isNotEmpty()) {
                    val base64String = attachments[0].base64
                    var bmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                    LaunchedEffect(base64String) {
                         try {
                              val bytes = Base64.decode(base64String, Base64.DEFAULT)
                              bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                         } catch(e: Exception) {}
                    }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                Text(formatter.format(Date(note.createdAt)), fontSize = 11.sp, color = Color.Gray)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// FULL SCREEN EDITOR //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullNoteEditor(
    note: NoteEntity?,
    onSave: (NoteEntity) -> Unit,
    onCancel: () -> Unit,
    accentColor: Color
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var colorHex by remember { mutableStateOf(note?.colorHex ?: "#FFFFFF") }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }
    val attachments = remember { mutableStateListOf<NoteAttachment>() }
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (note != null && !note.imageBase64.isNullOrEmpty()) {
            attachments.addAll(NoteAttachmentHelper.fromString(note.imageBase64))
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 != null) {
                attachments.add(NoteAttachment("image", base64))
            }
        }
    }

    val availableColors = listOf("#FFFFFF", "#FEF08A", "#BFDBFE", "#BBF7D0", "#FBCFE8", "#E9D5FF", "#FED7AA")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "إضافة ملاحظة جديدة 📝" else "تعديل الملاحظة 📝", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = "Close") }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(Icons.Default.PushPin, contentDescription = "تثبيت", tint = if (isPinned) accentColor else Color.Gray)
                    }
                    Button(
                        onClick = {
                            val entity = NoteEntity(
                                id = note?.id ?: 0,
                                farmName = note?.farmName ?: "",
                                title = title,
                                content = content,
                                imageBase64 = NoteAttachmentHelper.toString(attachments),
                                colorHex = colorHex,
                                createdAt = note?.createdAt ?: System.currentTimeMillis(),
                                isPinned = isPinned
                            )
                            onSave(entity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Column {
                Text("عنوان الملاحظة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("مثال: قائمة تسوق...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE2E8F0))
                )
            }
            
            // Content Type
            var contentType by remember { mutableStateOf("text") }
            Column {
                Text("نوع المحتوى", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val selectColor = accentColor
                    val unselectColor = MaterialTheme.colorScheme.surfaceVariant
                    val unselectTextColor = MaterialTheme.colorScheme.onSurface
                    Button(
                        onClick = { contentType = "numbered"; if (content.isEmpty()) content = "1. " },
                        colors = ButtonDefaults.buttonColors(containerColor = if(contentType == "numbered") selectColor else unselectColor, contentColor = if(contentType == "numbered") Color.White else unselectTextColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(60.dp),
                        border = BorderStroke(1.dp, if(contentType == "numbered") selectColor else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("قائمة مرقمة", fontSize = 12.sp)
                            Text("🔢", fontSize = 14.sp)
                        }
                    }
                    Button(
                        onClick = { contentType = "checklist"; if (content.isEmpty()) content = "- [ ] " },
                        colors = ButtonDefaults.buttonColors(containerColor = if(contentType == "checklist") selectColor else unselectColor, contentColor = if(contentType == "checklist") Color.White else unselectTextColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(60.dp),
                        border = BorderStroke(1.dp, if(contentType == "checklist") selectColor else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("قائمة مهام", fontSize = 12.sp)
                            Text("✅", fontSize = 14.sp)
                        }
                    }
                    Button(
                        onClick = { contentType = "text" },
                        colors = ButtonDefaults.buttonColors(containerColor = if(contentType == "text") selectColor else unselectColor, contentColor = if(contentType == "text") Color.White else unselectTextColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(60.dp),
                        border = BorderStroke(1.dp, if(contentType == "text") selectColor else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("نص عادي", fontSize = 12.sp)
                            Text("📄", fontSize = 14.sp)
                        }
                    }
                }
            }
            
            // Content
            Column {
                Text("محتوى الملاحظة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("اكتب أفكارك...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE2E8F0)),
                    maxLines = 15
                )
            }
            
            // Attachments
            if (attachments.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(attachments.size) { index ->
                        val base64String = attachments[index].base64
                        var bmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                        LaunchedEffect(base64String) {
                            try {
                                val bytes = Base64.decode(base64String, Base64.DEFAULT)
                                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch(e: Exception) {}
                        }
                        
                        if (bmp != null) {
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))) {
                                Image(
                                    bitmap = bmp!!.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { attachments.removeAt(index) },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha=0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Column {
                Text("إرفاق صورة للملاحظة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E8FF), contentColor = Color(0xFF7E22CE)),
                        elevation = null
                    ) {
                        Text("اختيار ملف", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(if (attachments.isEmpty()) "لم يتم اختيار أي ملف" else "تم اختيار صورة", color = Color.Gray, fontSize = 14.sp)
                }
            }
            
            Column {
                Text("لون خلفية الملاحظة", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    availableColors.forEach { c ->
                        val colorDef = try { Color(android.graphics.Color.parseColor(c)) } catch(e: Exception) { Color.White }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorDef)
                                .border(2.dp, if (colorHex == c) Color.Black else Color(0xFFE2E8F0), CircleShape)
                                .clickable { colorHex = c }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesSettingsDialog(
    viewModel: FarmViewModel,
    dialogColorHex: String,
    dialogTextColorHex: String,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val actualDialogColor = try { Color(android.graphics.Color.parseColor(dialogColorHex)) } catch(e: Exception) { Color.White }
    val actualDialogTextColor = try { Color(android.graphics.Color.parseColor(dialogTextColorHex)) } catch(e: Exception) { Color(0xFF1E293B) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = actualDialogColor,
            contentColor = actualDialogTextColor,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("⚙️ إعدادات المذكرات", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("لون خلفية صفحة الإعدادات للملاحظات:", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    var dialogBgInput by remember { mutableStateOf(dialogColorHex) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        IconButton(onClick = {
                            if (dialogBgInput.startsWith("#") && (dialogBgInput.length == 7 || dialogBgInput.length == 9)) {
                                viewModel.updateNotesSettingsColor(dialogBgInput)
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

                    Text("لون نصوص الإعدادات للملاحظات:", fontSize = 12.sp, color = actualDialogTextColor.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    var dialogTxtInput by remember { mutableStateOf(dialogTextColorHex) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        IconButton(onClick = {
                            if (dialogTxtInput.startsWith("#") && (dialogTxtInput.length == 7 || dialogTxtInput.length == 9)) {
                                viewModel.updateNotesSettingsTextColor(dialogTxtInput)
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

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تم", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
