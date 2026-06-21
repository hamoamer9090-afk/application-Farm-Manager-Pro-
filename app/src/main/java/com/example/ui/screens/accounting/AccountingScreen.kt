package com.example.ui.screens.accounting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountingItem
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: FarmViewModel,
    accentColor: Color,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("جهات الاتصال", "الدفتر", "المصروفات", "الشركاء")
    
    val allItems by viewModel.accountingItemsList.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المحاسبة والإدارة المالية", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Future proof: Add dialog to add an item based on the selected tab
                    val type = when(selectedTab) {
                        0 -> "CONTACT"
                        1 -> "LEDGER"
                        2 -> "EXPENSE"
                        else -> "PARTNER"
                    }
                    viewModel.addAccountingItem(type, "عنصر جديد", 0.0, "تمت الإضافة من الشاشة")
                },
                containerColor = accentColor
            ) {
                Icon(Icons.Default.Add, "إضافة", tint = Color.White)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Contacts, contentDescription = null) },
                    label = { Text("الجهات") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = accentColor, indicatorColor = accentColor.copy(alpha = 0.2f))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("الدفتر") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = accentColor, indicatorColor = accentColor.copy(alpha = 0.2f))
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    label = { Text("المصروفات") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = accentColor, indicatorColor = accentColor.copy(alpha = 0.2f))
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Handshake, contentDescription = null) },
                    label = { Text("الشركاء") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = accentColor, indicatorColor = accentColor.copy(alpha = 0.2f))
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> ContactsPage(allItems.filter { it.type == "CONTACT" }, viewModel)
                1 -> LedgerPage(allItems.filter { it.type == "LEDGER" }, viewModel)
                2 -> ExpensesPage(allItems.filter { it.type == "EXPENSE" }, viewModel)
                3 -> PartnershipPage(allItems.filter { it.type == "PARTNER" }, viewModel)
            }
        }
    }
}

@Composable
fun ExpandableCard(item: AccountingItem, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            if (item.amount > 0) {
                Text("المبلغ: ${item.amount} ريال", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("التفاصيل:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (item.notes.isEmpty()) "لا توجد ملاحظات" else item.notes, fontSize = 14.sp)
                if (!item.isSynced) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudOff, "غير متزامن", modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("في انتظار المزامنة", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsPage(items: List<AccountingItem>, viewModel: FarmViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("جهات الاتصال", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if(items.isEmpty()) {
            Text("لا توجد جهات اتصال", color = Color.Gray)
        } else {
            LazyColumn {
                items(items) { item ->
                    ExpandableCard(item) {
                        viewModel.deleteAccountingItem(item)
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerPage(items: List<AccountingItem>, viewModel: FarmViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("دفتر الديون والأرصدة (Ledger)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if(items.isEmpty()) {
            Text("الدفتر فارغ", color = Color.Gray)
        } else {
            LazyColumn {
                items(items) { item ->
                    ExpandableCard(item) {
                        viewModel.deleteAccountingItem(item)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesPage(items: List<AccountingItem>, viewModel: FarmViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("إدارة المصروفات", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if(items.isEmpty()) {
            Text("لا توجد مصروفات مسجلة", color = Color.Gray)
        } else {
            LazyColumn {
                items(items) { item ->
                    ExpandableCard(item) {
                        viewModel.deleteAccountingItem(item)
                    }
                }
            }
        }
    }
}

@Composable
fun PartnershipPage(items: List<AccountingItem>, viewModel: FarmViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("الشركاء والأسهم", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if(items.isEmpty()) {
            Text("لا يوجد شركاء", color = Color.Gray)
        } else {
            LazyColumn {
                items(items) { item ->
                    ExpandableCard(item) {
                        viewModel.deleteAccountingItem(item)
                    }
                }
            }
        }
    }
}
