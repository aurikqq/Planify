package com.aurikqq.planify.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aurikqq.planify.AlarmScheduler
import com.aurikqq.planify.CONSTANT_PLANS_SCREEN
import com.aurikqq.planify.HISTORY_SCREEN
import com.aurikqq.planify.NavRail
import com.aurikqq.planify.PLANS_SCREEN
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.R
import com.aurikqq.planify.Repository
import com.aurikqq.planify.RequestNotificationsPermission
import com.aurikqq.planify.TabsBar
import com.aurikqq.planify.UpdateLabel
import com.aurikqq.planify.createNotificationChannel
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.MainScreenViewModel
import com.aurikqq.planify.viewmodels.MainScreenViewModelFactory
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale

data class PlansScreenUiState(
    val days: List<Pair<String, String>> = emptyList(),
    val currentDate: String = "",
    val selectedDate: String = "",
    val selectedPickerDate: String = "",
    val plansForSelectedDate: String = "",
    val havePlans: Boolean = false,
    val isDaysListEditing: Boolean = false,
    val isDatePickerShown: Boolean = false,
    val isPlanEditing: Boolean = false,
    val tempPlanInput: String = "",
    val isFirstLaunch: Boolean = true,
    val isUpdatePopupShown: Boolean = false
)

@Composable
fun DaysListItem(
    date: String,
    isSelected: Boolean = false,
    isDaysListEditing: Boolean = false,
    onRemove: () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.background
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = date,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp)
        )

        if (isDaysListEditing) {
            TextButton(onClick = onRemove) {
                Icon(Icons.Default.Close, null)
            }
        }
    }
}

@Composable
fun DaysList(
    viewModel: MainScreenViewModel = viewModel(),
    navController: NavController,
    onDateSelected: (String) -> Unit,
    ) {
    val uiState by viewModel.uiState.collectAsState()
    val orientation = LocalConfiguration.current.navigation

    Column(modifier = Modifier
        .width(256.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, bottom = 16.dp)
                .size(256.dp, 32.dp)
                .background(MaterialTheme.colorScheme.surface)
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

        Row {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                NavRail(navController)
            }

            LazyColumn {
                item {
                    DaysListItem("сегодня", uiState.currentDate == uiState.selectedPickerDate, onClick = { onDateSelected(uiState.currentDate) })
                }
                items(uiState.days) { day ->
                    val selected = day.second == uiState.selectedPickerDate
                    DaysListItem(day.second, selected, uiState.isDaysListEditing,
                        {
                            viewModel.removeDay(day)
                        },
                        { onDateSelected(day.second) })
                }
            }
        }
    }
}

fun reformatDate(date: String): String? {
    val formatWithYear = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
    val formatWithoutYear = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("d MMMM")
        .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
        .toFormatter(Locale.getDefault())
    val formatWithWeekday = DateTimeFormatter.ofPattern("d MMMM, EEE", Locale.getDefault())
    val outputFormat = DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())

    return try {
        val date = LocalDate.parse(date, formatWithYear)
        date.format(outputFormat)
    }
    catch (_: DateTimeParseException) {
        try {
            val date = LocalDate.parse(date, formatWithoutYear.withLocale(Locale.getDefault()))
                .withYear(LocalDate.now().year)
            date.format(outputFormat)
        }
        catch (_: DateTimeParseException) {
            try {
                val date = LocalDate.parse(date, formatWithWeekday.withLocale(Locale.getDefault()))
                    .withYear(LocalDate.now().year)
                date.format(outputFormat)
            }
            catch (_: DateTimeParseException) {
                Log.e("DateReformatting", "Unable to reformat date")
                null
            }
        }
    }
}

// удаление и перестановка дней
// если день наступает - он становится сегодняшним, если проходит - идёт в историю
// для каждого дня - свои планы

// в истории и списке дней берётся сегодняшняя дата, поэтому в историю пишутся сегодняшние планы на место вчерашних, а в список добавляется сегодняшний день
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

    if (!uiState.isUpdatePopupShown) {
        AlertDialog(
            onDismissRequest = { viewModel.updatePopupShown() },
            title = {
                Text("Что поменялось в этой версии:")
            },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "- Переработка записей - теперь их может быть сколько угодно одновременно\n" +
                                    "- Появилась наработка для будущей возможности записывать планы на любые дни\n" +
                                    "- Архитектура приложения почти полностью переписана (не касается опыта использования, но масштабное изменение кода)\n" +
                                    "- Аннигилировано несколько багов\n" +
                                    "- В интерфейсе поменялась пара мелочей\n" +
                                    "- поменял версию на 0.2.4, а то в прошлый раз забыл(\n" +
                                    "и, может, что-то ещё, чего я не помню...\n\n" +
                                    "Стоило бы сделать ещё пару исправлений вроде сохранения введённых, но не запомненных планов, чтобы не терять их, но не хватило времени. Могут быть баги!!"
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updatePopupShown() }) {
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
                            modifier = Modifier.size(256.dp).padding(8.dp)
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
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    composable(route = PLANS_SCREEN) {
                        DailyPlansScreen(
                            uiState,
                            { viewModel.onTempPlanInputChange(it) },
                            {
                                viewModel.saveNewPlans()
                                viewModel.tempPlans("")
                                AlarmScheduler.scheduleRepeatingAlarm(context)
                                AlarmScheduler.scheduleAlarm(context)
                                AlarmScheduler.schedulePlansReset(context)
                            },
                            { viewModel.addPlans() },
                            { viewModel.startEditingPlans() },
                            { viewModel.endEditingPlans() }
                        )
                    }
                    composable(route = CONSTANT_PLANS_SCREEN) {
                        NotesScreen()
                    }
                    composable(route = HISTORY_SCREEN) {
                        HistoryScreen()
                    }
                }
            } }
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
                                uiState,
                                { viewModel.onTempPlanInputChange(it) },
                                {
                                    viewModel.saveNewPlans()
                                    viewModel.tempPlans("")
                                    AlarmScheduler.scheduleRepeatingAlarm(context)
                                    AlarmScheduler.scheduleAlarm(context)
                                    AlarmScheduler.schedulePlansReset(context)
                                },
                                { viewModel.addPlans() },
                                { viewModel.startEditingPlans() },
                                { viewModel.endEditingPlans() }
                            )
                        }
                        composable(route = CONSTANT_PLANS_SCREEN) {
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
fun DailyPlansScreen(
    uiState: PlansScreenUiState,
    onPlanInputChange: (String) -> Unit,
    onSetPlans: () -> Unit,
    onAddPlans: () -> Unit,
    onEditPlans: () -> Unit,
    onEditCancel: () -> Unit,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (uiState.havePlans) Arrangement.Top else Arrangement.Center,
        contentPadding = if (uiState.havePlans) PaddingValues(
            start = 24.dp,
            top = 96.dp,
            end = 24.dp,
            bottom = 32.dp
        )
        else PaddingValues(start = 24.dp, top = 32.dp, end = 24.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (uiState.havePlans) {
            item {
                Card(
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            clip = false
                        )
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.plans_card_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            text = uiState.plansForSelectedDate,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
        item {
            if (uiState.isPlanEditing) {
                OutlinedTextField(
                    value = uiState.tempPlanInput,
                    label = { Text(stringResource(R.string.label_edit_plans)) },
                    onValueChange = onPlanInputChange,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 120.dp)
                        .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                        .animateItem(placementSpec = spring()),
                )
            } else {
                OutlinedTextField(
                    value = uiState.tempPlanInput,
                    label = {
                        Text(
                            text = if (uiState.havePlans) stringResource(R.string.label_add_plans) else stringResource(
                                R.string.label_set_plans
                            )
                        )
                    },
                    onValueChange = onPlanInputChange,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 120.dp)
                        .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                        .animateItem(placementSpec = spring()),
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            if (!uiState.havePlans) {
                Button(
                    onClick = onSetPlans,
                    enabled = uiState.tempPlanInput.isNotBlank(),
                    modifier = Modifier.animateItem(placementSpec = spring())
                ) {
                    Text(text = stringResource(R.string.button_set_plans))
                }
            } else {
                Row {
                    ElevatedButton(
                        onClick = onAddPlans,
                        enabled = uiState.tempPlanInput.isNotBlank() && !uiState.isPlanEditing,
                        modifier = Modifier.animateItem(placementSpec = spring())
                    ) {
                        Text(text = stringResource(R.string.button_add_plans))
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    if (uiState.isPlanEditing) {
                        ElevatedButton(
                            onClick = onEditCancel,
                            enabled = uiState.tempPlanInput.isNotBlank(),
                            modifier = Modifier
                                .animateItem(placementSpec = spring())
                        ) {
                            Text(stringResource(R.string.button_finish_editing))
                        }
                    } else {
                        ElevatedButton(
                            onClick = onEditPlans,
                            modifier = Modifier.animateItem(placementSpec = spring())
                        ) {
                            Text(stringResource(R.string.button_edit_plans))
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.size(48.dp))
            Text(
                text = if (uiState.havePlans)
                    stringResource(R.string.bottom_text_have_plans)
                else if (uiState.isFirstLaunch)
                    stringResource(R.string.bottom_text_first_launch)
                else
                    stringResource(R.string.bottom_text_no_plans),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier
                    .width(260.dp)
                    .animateItem(placementSpec = spring())
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, locale = "ru")
@Composable
fun PlansPreview() {
    PlanifyTheme {
        MainScreen(navController = rememberNavController())
    }
}

// TODO
// implement planning for other days
// menu on the left for every day with adding days here