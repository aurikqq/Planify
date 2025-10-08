package com.aurikqq.planify.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aurikqq.planify.HISTORY_SCREEN
import com.aurikqq.planify.NOTES_SCREEN
import com.aurikqq.planify.PLANS_SCREEN
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.Repository
import com.aurikqq.planify.RequestNotificationsPermission
import com.aurikqq.planify.TabsBar
import com.aurikqq.planify.checkUpdates
import com.aurikqq.planify.createNotificationChannel
import com.aurikqq.planify.downloadApk
import com.aurikqq.planify.installApk
import com.aurikqq.planify.viewmodels.MainScreenViewModel
import com.aurikqq.planify.viewmodels.MainScreenViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orientation = LocalConfiguration.current.navigation

    val viewModel: MainScreenViewModel = viewModel(
        factory = MainScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE),
                context
            )))

    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis > Date().time
            }
        }
    )

    createNotificationChannel(context)
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
        AlertDialog(
            onDismissRequest = {
                viewModel.updatePopupShown(true)
                showUpdateDialog = false
            },
            title = {
                Text("Что поменялось в этой версии:")
            },
            text = {
                LazyColumn {
                    item {
                        Text(buildAnnotatedString {
                            append("- Введено автообновление - теперь Planify при запуске проверяет, есть ли новая версия, и, если повезёт ")
                            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                                append("и я ещё не спятил/спился")
                            }
                            append(", предложит обновиться.\n" +
                                    "- Разные фиксы, может, что-то ещё полезное, хззабыл")
                        })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showUpdateDialog = false
                    viewModel.updatePopupShown(true)
                }) {
                    Text("Понял")
                }
            }
        )
    }

    if (uiState.isDatePickerShown) {
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
                                DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())
                            ) /*TODO*/ // make different patterns depending on date

                            viewModel.changeDateSelectedInPicker(dayDate)
                            viewModel.getDateFromPicker(dayDate)
                            viewModel.hideDatePicker()
                        }
                    }
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
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        DaysList(
                            viewModel,
                            navController,
                            {
                                viewModel.selectDate(it)
                                scope.launch { drawerState.close() }
                            },
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

                        TextButton(onClick = { viewModel.setIsDaysListEditing() }) {
                            Icon(
                                Icons.Default.EditCalendar,
                                null,
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Изменить список")
                        }

                        Spacer(Modifier.size(32.dp))
                        Text(
                            "В будущем можно будет записывать планы на другие дни. " +
                                    "А пока это просто полурабочий список таких дней. " +
                                    "Лучше особо ничего не трогать, потому что работает не всё",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(256.dp)
                                .padding(8.dp)
                        )
                    }
                }
            }) {
            Column {
                TabsBar(navController)

                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = PLANS_SCREEN,
                        enterTransition = { slideInHorizontally { it } + fadeIn() },
                        exitTransition = { slideOutHorizontally { -it } + fadeOut() },
                        popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
                        popExitTransition = { slideOutHorizontally { it } + fadeOut() },
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        composable(route = PLANS_SCREEN) {
                            DailyPlansScreen(
                                context,
                                uiState,
                                viewModel
                            )
                        }
                        composable(route = NOTES_SCREEN) {
                            NotesScreen()
                        }
                        composable(route = HISTORY_SCREEN) {
                            HistoryScreen()
                        }
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
                        scope.launch { drawerState.close() }
                    },
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
                TabsBar(navController)

                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = PLANS_SCREEN,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(route = PLANS_SCREEN) {
                            DailyPlansScreen(
                                context,
                                uiState,
                                viewModel
                            )
                        }
                        composable(route = NOTES_SCREEN) {
                            NotesScreen()
                        }
                        composable(route = HISTORY_SCREEN) {
                            HistoryScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateLabel() {
    val context = LocalContext.current
    var isUpdateAvailable by rememberSaveable { mutableStateOf(false) }
    var isDownloading by rememberSaveable { mutableStateOf(false) }
    var dlProgress by rememberSaveable { mutableIntStateOf(0) }
    var apk by rememberSaveable { mutableStateOf<File?>(null) }

    val viewModel: MainScreenViewModel = viewModel(
        factory = MainScreenViewModelFactory(
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
                    Text("Обновить Planify!")
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