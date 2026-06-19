package com.aurikqq.planify.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.planify.AlarmScheduler
import com.aurikqq.planify.R
import com.aurikqq.planify.components.AnimatedButton
import com.aurikqq.planify.components.AnimatedElevatedButton
import com.aurikqq.planify.components.PlansUnit
import com.aurikqq.planify.createNotificationChannel
import com.aurikqq.planify.isTablet
import com.aurikqq.planify.viewmodels.PlansScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration

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
    val isUpdatePopupShown: Boolean = true,
    val plansNotificationsEnabled: Boolean = true,
    val resetNotificationsEnabled: Boolean = true,
    val isSignedIn: Boolean = false,
    val email: String = "null",
    val plansPrefix: String = "",
    val isPermissionDialogShown: Boolean = false
)

@Composable
fun DaysListItem(
    uiState: PlansScreenUiState,
    date: String,
    isSelected: Boolean = false,
    isDaysListEditing: Boolean = false,
    onRemove: () -> Unit = {},
    onClick: () -> Unit
) {
    Box {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable(onClick = onClick)
        ) {
            val color = MaterialTheme.colorScheme.secondaryContainer

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(if (isSelected) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp))
                    .background(if (isSelected) color.copy(alpha = 0.75f) else Color.Transparent)
                    .fillMaxWidth(if (isDaysListEditing) 0.75f else 1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    )
                }

                Text(
                    text = date,
                    color = if (uiState.days.isNotEmpty())
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(start = if (isSelected) 16.dp else 0.dp)
                )
            }

            if (isDaysListEditing) {
                TextButton(
                    onClick = onRemove,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        }

//        if (isSelected) {
//            Image(
//                painterResource(
//                    R.drawable.snowflake
//                ),
//                null,
//                modifier = Modifier
//                    .size(32.dp)
//                    .offset(animateDpAsState(if (isDaysListEditing) 164.dp else 228.dp).value, 0.dp)
//                    .rotate(15f)
//            )
//        }
    }
}

@Composable
fun DaysList(
    viewModel: PlansScreenViewModel = viewModel(),
    onDateSelected: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.days, uiState.currentDate) {
        uiState.days.forEach { day ->
            if (day.second == uiState.currentDate) {
                viewModel.removeDay(day, false)
            }
        }
    }

    Column(modifier = Modifier
        .width(256.dp)
    ) {
        Row {
            Column {
                DaysListItem(uiState, stringResource(R.string.label_today), uiState.currentDate == uiState.selectedPickerDate,
                    onClick = { onDateSelected(uiState.currentDate) })

                for (day in uiState.days) {
                    val selected = day.second == uiState.selectedPickerDate
                    val format = DateTimeFormatter.ofPattern("dd_MM_yyyy", LocalLocale.current.platformLocale)

                    val itemDate = LocalDate.parse(day.second, format)
                    val currentDate = LocalDate.now()
                    val difference = ChronoUnit.DAYS.between(currentDate, itemDate)
                    val date: String

                    if (difference != 1L) {
                        val pattern = if (difference in 2..7) "dMMMM E"
                                     else if (currentDate.year == itemDate.year) "dMMMM"
                                     else "dMMMM yyyy"
                        val outputFormat = DateTimeFormatter.ofPattern(
                            android.text.format.DateFormat.getBestDateTimePattern(LocalLocale.current.platformLocale, pattern),
                            LocalLocale.current.platformLocale
                        )

                        date = LocalDate.parse(day.second, format)
                            .format(outputFormat)
                    }
                    else {
                        date = stringResource(R.string.label_tomorrow)
                    }

                    DaysListItem(uiState, date, selected, uiState.isDaysListEditing,
                        {
                            if (uiState.days.size != 1) {
                                if (uiState.selectedPickerDate == day.second) {
                                    onDateSelected(uiState.days[uiState.days.indexOf(day) - 1].second)
                                }
                            }
                            else {
                                onDateSelected(uiState.currentDate)
                                viewModel.setIsDaysListEditing(false)
                            }
                            viewModel.removeDay(day)
                        },
                        { onDateSelected(day.second) }
                    )
                }
            }
        }
    }
}

@Composable
fun DailyPlansScreen(
    context: Context,
    uiState: PlansScreenUiState,
    viewModel: PlansScreenViewModel = viewModel(),
    //onPlansRendered: (Rect) -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }
        viewModel.hidePermissionDialog()
    }

    val checkPermissions = {
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        val hasAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true

        hasNotificationPermission && hasAlarmPermission
    }

    if (uiState.isPermissionDialogShown) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePermissionDialog() },
            title = { Text(stringResource(R.string.permission_dialog_title)) },
            text = { Text(stringResource(R.string.permission_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (!alarmManager.canScheduleExactAlarms()) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                context.startActivity(intent)
                            }
                        }
                        viewModel.hidePermissionDialog()
                    }
                }) {
                    Text(stringResource(R.string.permission_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hidePermissionDialog() }) {
                    Text(stringResource(R.string.permission_dialog_dismiss))
                }
            }
        )
    }

    val top by animateDpAsState(if (uiState.havePlans) 24.dp else 48.dp, tween())
    val bottomLabels = listOf(
        stringResource(R.string.bottom_text_no_plans_0),
        stringResource(R.string.bottom_text_no_plans_1),
        stringResource(R.string.bottom_text_no_plans_2),
        //stringResource(R.string.bottom_text_no_plans_xmas),
        //stringResource(R.string.bottom_text_no_plans_xmas_2),
        //stringResource(R.string.bottom_text_no_plans_xmas_3),
        //stringResource(R.string.bottom_text_no_plans_xmas_4),
        //stringResource(R.string.bottom_text_no_plans_xmas_5)
    )

    val labelText = remember { bottomLabels.random() }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (uiState.havePlans) Arrangement.Top else Arrangement.Center,
        contentPadding = PaddingValues(
            start = 8.dp, top = top, end = 8.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp + 32.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .animateContentSize()
            //.snowfall()
    ) {
        if (uiState.havePlans && uiState.plansForSelectedDate.isNotBlank()) {
            item {
                Card(
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            clip = false
                        )
//                        .onGloballyPositioned { coords ->
//                            val position = coords.positionInRoot()
//                            val size = coords.size
//
//                            val rect = Rect(
//                                position.x.toInt(),
//                                position.y.toInt(),
//                                (position.x + size.width).toInt(),
//                                (position.y + size.height).toInt()
//                            )
//                            onPlansRendered(rect)
//                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.plans_card_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        )
                        uiState.plansForSelectedDate.lines().forEachIndexed { index, str ->
                            if (str.isNotBlank()) {
                                if (str.replace(" ", "").startsWith(uiState.plansPrefix)) {
                                    val prefix = uiState.plansPrefix
                                    val isDone = str.contains("$prefix*")
                                    val cleanText = if (isDone) {
                                        str.replaceFirst("$prefix*", "").trim()
                                    } else {
                                        str.replaceFirst(prefix, "").trim()
                                    }

                                    PlansUnit(
                                        isDone = isDone,
                                        text = cleanText,
                                        onClick = { viewModel.togglePlanCompletion(index) }
                                    )
                                } else {
                                    Text(
                                        text = str,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
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
                    onValueChange = { viewModel.onTempPlanInputChange(it) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .padding(8.dp)
                        .defaultMinSize(minHeight = 120.dp)
                        .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                )
            } else {
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 120.dp)
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.tempPlanInput,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        label = {
                            Text(
                                text = if (uiState.havePlans) stringResource(R.string.label_add_plans) else stringResource(
                                    R.string.label_set_plans
                                )
                            )
                        },
                        onValueChange = { viewModel.onTempPlanInputChange(it) },
                        modifier = Modifier
                            .padding(16.dp)
                            .defaultMinSize(minHeight = 120.dp)
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                    )
                }

            }
            Spacer(modifier = Modifier.size(16.dp))
            if (!uiState.havePlans) {
                val isEnabled = uiState.tempPlanInput.isNotBlank()

                AnimatedButton(
                    onClick = {
                        if (!checkPermissions()) {
                            viewModel.showPermissionDialog()
                        }
                        else {
                            viewModel.updateAccount()
                            if (uiState.isSignedIn && viewModel.isOnline()) {
                                viewModel.sendPlansToDatabase(uiState.tempPlanInput)
                                Log.d("DB", "Send (${uiState.tempPlanInput})")
                            }
                            viewModel.saveNewPlans()
                            viewModel.tempPlans("")

                            createNotificationChannel(context)
                            AlarmScheduler.schedulePlansReset(context)
                        }
                    },
                    enabled = isEnabled
                ) {
                    Text(text = stringResource(R.string.button_set_plans))
                }
            } else {
                Row {
                    val isAddEnabled = uiState.tempPlanInput.isNotBlank() && !uiState.isPlanEditing

                    AnimatedElevatedButton(
                        onClick = {
                            if (!checkPermissions()) {
                                viewModel.showPermissionDialog()
                                return@AnimatedElevatedButton
                            }

                            viewModel.addPlans()
                            if (uiState.isSignedIn && viewModel.isOnline())
                                viewModel.sendPlansToDatabase("${uiState.plansForSelectedDate}\n${uiState.tempPlanInput}")
                            viewModel.tempPlans("")
                        },
                        enabled = isAddEnabled
                    ) {
                        Text(text = stringResource(R.string.button_add_plans))
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    if (uiState.isPlanEditing) {
                        val isFinishEnabled = uiState.tempPlanInput.isNotBlank()

                        AnimatedElevatedButton(
                            onClick = {
                                if (!checkPermissions()) {
                                    viewModel.showPermissionDialog()
                                    return@AnimatedElevatedButton
                                }

                                viewModel.endEditingPlans()
                                if (uiState.isSignedIn && viewModel.isOnline())
                                    viewModel.sendPlansToDatabase(uiState.tempPlanInput)
                                viewModel.tempPlans("")
                            },
                            enabled = isFinishEnabled
                        ) {
                            Text(stringResource(R.string.button_finish_editing))
                        }
                    } else {
                        ElevatedButton(
                            onClick = { viewModel.startEditingPlans() },
                        ) {
                            Text(stringResource(R.string.button_edit_plans))
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.size(32.dp))

            Text(
                text = if (uiState.havePlans)
                    stringResource(R.string.bottom_text_have_plans)
                else if (uiState.isFirstLaunch)
                    stringResource(R.string.bottom_text_first_launch)
                else {
                    labelText
                }
                    ,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}

@SuppressLint("NonObservableLocale")
@Composable
fun TopBar(
    uiState: PlansScreenUiState,
    drawerState: DrawerState,
    scope: CoroutineScope,
    isHistoryShown: Boolean,
    onHistoryClick: () -> Unit
) {
    var day: String
    try {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMM")
        day = LocalDate.parse(uiState.selectedPickerDate,
            DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault()))
            .format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }
    catch (_: Exception) {
        day = uiState.selectedPickerDate
    }

    val color = ButtonDefaults.textButtonColors().contentColor

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        if (!isTablet()) {
            TextButton(onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(Icons.Default.Menu, null)
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = day,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .drawBehind {
                            drawRoundRect(
                                color,
                                topLeft = Offset(0f, size.height + 4.dp.toPx()),
                                size = Size(size.width, 3.dp.toPx()),
                                cornerRadius = CornerRadius(50f, 50f)
                            )
                        }
                )
            }
        }
        else {
            Text(
                text = day,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier
                    .drawBehind {
                        drawRoundRect(
                            color,
                            topLeft = Offset(0f, size.height + 4.dp.toPx()),
                            size = Size(size.width, 3.dp.toPx()),
                            cornerRadius = CornerRadius(50f, 50f)
                        )
                    }
            )
        }

        IconButton(
            onClick = {
                onHistoryClick()
            },
            shape = RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = animateColorAsState(targetValue = if (isHistoryShown) MaterialTheme.colorScheme.onSecondaryContainer
                                                                    else MaterialTheme.colorScheme.primary).value
            ),
            modifier = Modifier.padding(end = 16.dp)
        ) {
            AnimatedVisibility(visible = !isHistoryShown,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                Icon(
                    Icons.Default.History,
                    null,
                )
            }
            AnimatedVisibility(visible = isHistoryShown) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    null,
                )
            }
        }
    }
}