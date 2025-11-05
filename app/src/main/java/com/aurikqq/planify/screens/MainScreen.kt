package com.aurikqq.planify.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aurikqq.planify.HISTORY_SCREEN
import com.aurikqq.planify.NOTES_SCREEN
import com.aurikqq.planify.PLANS_SCREEN
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.R
import com.aurikqq.planify.Repository
import com.aurikqq.planify.RequestNotificationsPermission
import com.aurikqq.planify.TabsBar
import com.aurikqq.planify.checkUpdates
import com.aurikqq.planify.downloadApk
import com.aurikqq.planify.installApk
import com.aurikqq.planify.viewmodels.PlansScreenViewModel
import com.aurikqq.planify.viewmodels.PlansScreenViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    viewModel: PlansScreenViewModel,
    uiState: PlansScreenUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val orientation = LocalConfiguration.current.navigation
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }

    val chosenDates = mutableListOf<String>()
    for (date in uiState.days) { chosenDates.add(date.second) }

    var selectedTab by rememberSaveable { mutableStateOf(PLANS_SCREEN) }
    var isHistoryShown by remember { mutableStateOf(false) }
    var preHistoryScreen by remember { mutableStateOf(PLANS_SCREEN) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val selectedDate = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val today = LocalDate.now()

                val isFuture = selectedDate.isAfter(today)
                val isChosen = chosenDates.contains(selectedDate.format(
                    DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())
                ))

                return isFuture && !isChosen
            }
        }
    )

    RequestNotificationsPermission()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
        if (alarmManager?.canScheduleExactAlarms() == false) {
            Intent().also { intent ->
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.isUpdatePopupShown) {
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog) {
        UpdateDialog {
            viewModel.updatePopupShown(true)
            showUpdateDialog = false
        }
    }

    if (uiState.isDatePickerShown) {
        val selectedDate = datePickerState.selectedDateMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        DatePickerDialog(
            onDismissRequest = { viewModel.hideDatePicker() },
            confirmButton = {
                ElevatedButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            val dayDate = date.format(
                                DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())
                            )

                            viewModel.selectDate(dayDate)
                            viewModel.getDateFromPicker(dayDate)
                            viewModel.hideDatePicker()
                        }
                    },
                    enabled = if (uiState.days.isNotEmpty()) { !chosenDates.any {
                        LocalDate.parse(it, DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())) == selectedDate } }
                        else true
                ) {
                    Text("Готово")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDatePicker() }
                ) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            modifier = modifier.fillMaxSize(),
            drawerContent = {
                Column(modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                ) {
                    Row (
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp, bottom = 16.dp)
                            .size(256.dp, 32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_with_top),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Planify",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            DaysList(
                                viewModel,
                                navController,
                                {
                                    viewModel.selectDate(it)
                                    scope.launch {
                                        drawerState.close()
                                        viewModel.setIsDaysListEditing(false)
                                    }
                                }
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .padding(start = 4.dp, bottom = 12.dp)
                    ) {
                        Spacer(Modifier.size(12.dp))

                        TextButton(onClick = { viewModel.showDatePicker() }) {
                            Icon(
                                Icons.Default.Add,
                                null,
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Добавить день")
                        }

                        TextButton(onClick = { viewModel.setIsDaysListEditing(!uiState.isDaysListEditing) }) {
                            Icon(
                                Icons.Default.EditCalendar,
                                null,
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Изменить список")
                        }
                    }
                }
            }
        ) {
            Column {
                TopBar(uiState, drawerState, scope, isHistoryShown) {
                    isHistoryShown = !isHistoryShown
                    if (selectedTab != HISTORY_SCREEN) {
                        preHistoryScreen = selectedTab
                        selectedTab = HISTORY_SCREEN
                    }
                    else {
                        selectedTab = preHistoryScreen
                    }
                }

                AnimatedVisibility(visible = selectedTab == PLANS_SCREEN || selectedTab == NOTES_SCREEN,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    TabsBar(context, viewModel, uiState) { route ->
                        selectedTab = route
                    }
                }

                Surface {
                    when (selectedTab) {
                        PLANS_SCREEN -> DailyPlansScreen(
                            context,
                            uiState,
                            viewModel
                        )
                        NOTES_SCREEN -> NotesScreen()
                        HISTORY_SCREEN -> HistoryScreen()
                    }
                }
            }
        }
    }
    else {
        Row(modifier = modifier.fillMaxSize()) {
            Column {
                DaysList(
                    viewModel,
                    navController,
                    {
                        viewModel.selectDate(it)
                        scope.launch {
                            drawerState.close()
                            viewModel.setIsDaysListEditing(false)
                        }
                    }
                )
                Spacer(Modifier.size(12.dp))
                TextButton(onClick = { viewModel.showDatePicker() }) {
                    Icon(
                        Icons.Default.Add,
                        null,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Добавить день")
                }
                TextButton(onClick = { /*viewModel.editDaysList()*/ }) {
                    Icon(
                        Icons.Default.EditCalendar,
                        null,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Изменить список")
                }
            }

            Column {
                TabsBar(context, viewModel, uiState) { route ->
                    selectedTab = route
                }

                Surface {
                    when (selectedTab) {
                        PLANS_SCREEN -> DailyPlansScreen(
                            context,
                            uiState,
                            viewModel
                        )
                        NOTES_SCREEN -> NotesScreen()
                        HISTORY_SCREEN -> HistoryScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateDialog(onClickOrDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClickOrDismiss,
        title = {
            Text("Что поменялось в этой версии:")
        },
        text = {
            LazyColumn {
                item {
                    Text("• Готов список дней - теперь можно распланировать наперёд любой день.\n" +
                            "      - Планы для каждого дня отдельные, сами дни в любой момент можно добавить или удалить\n" +
                            "      - Планы на будущее не сохраняются в историю (зачем в истории будущее...), пока этот день не пройдёт\n" +
                            "      - То, что было сохранено на конкретный день, останется, когда этот день наступит - об этом напомнит уведомление\n" +
                            "      - Ну и, естественно, интерфейсик красивый постарался сделать\n\n" +
                            "• Выбранная в списке дата показывается сверху.\n\n" +
                            "• Куча разных фиксов, доработки в интерфейсе, улучшение кода, бе-бе-бе..."
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClickOrDismiss) {
                Text("Понял")
            }
        }
    )
}

@Composable
fun UpdateLabel() {
    val context = LocalContext.current
    var isUpdateAvailable by rememberSaveable { mutableStateOf(false) }
    var isDownloading by rememberSaveable { mutableStateOf(false) }
    var dlProgress by rememberSaveable { mutableIntStateOf(0) }
    var apk by rememberSaveable { mutableStateOf<File?>(null) }

    val viewModel: PlansScreenViewModel = viewModel(
        factory = PlansScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE),
                context
            )))

    LaunchedEffect(Unit) {
        isUpdateAvailable = checkUpdates(context)
    }

    if (isUpdateAvailable) {
        if (!isDownloading) {
            if (dlProgress < 100f) {
                FilledTonalButton(
                    onClick = {
                        isDownloading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            apk = downloadApk(context) { dlProgress = it.toInt() }
                            withContext(Dispatchers.Main) {
                                isDownloading = false
                                Toast.makeText(context, "Обновление скачано!", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Text("Скачать обновление")
                }
            }
            else {
                FilledTonalButton(
                    onClick = {
                        if (installApk(context, apk)) {
                            viewModel.updatePopupShown(false)
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Text("Нажми, чтобы обновить Planify!")
                }
            }
        }
        else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text("Скачивание...", color = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.size(32.dp))
                Text("$dlProgress%", color = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.size(12.dp))
                CircularProgressIndicator(
                    progress = { dlProgress / 100f },
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}