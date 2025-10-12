package com.aurikqq.planify.screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aurikqq.planify.AlarmScheduler
import com.aurikqq.planify.NavRail
import com.aurikqq.planify.R
import com.aurikqq.planify.createNotificationChannel
import com.aurikqq.planify.viewmodels.PlansScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
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
    uiState: PlansScreenUiState,
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
}

@Composable
fun DaysList(
    viewModel: PlansScreenViewModel = viewModel(),
    navController: NavController,
    onDateSelected: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val orientation = LocalConfiguration.current.navigation

    for (day in uiState.days) {
        if (day.second == uiState.currentDate) {
            viewModel.removeDay(day)
        }
    }

    Column(modifier = Modifier
        .width(256.dp)
    ) {
        Row {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                NavRail(navController)
            }

            Column {
                DaysListItem(uiState, "сегодня", uiState.currentDate == uiState.selectedPickerDate,
                    onClick = { onDateSelected(uiState.currentDate) })

                for (day in uiState.days) {
                    val selected = day.second == uiState.selectedPickerDate
                    val format = DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault())

                    val itemDate = LocalDate.parse(day.second, format)
                    val currentDate = LocalDate.now()
                    val difference = ChronoUnit.DAYS.between(currentDate, itemDate)
                    val date: String

                    if (difference != 1L) {
                        val outputFormat =
                            if (difference in 2..7) DateTimeFormatter.ofPattern(
                                "d MMMM, E",
                                Locale.getDefault()
                            )
                            else if (currentDate.year == itemDate.year) DateTimeFormatter.ofPattern(
                                "d MMMM",
                                Locale.getDefault()
                            )
                            else DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

                        date = LocalDate.parse(day.second, format)
                            .format(outputFormat)
                    }
                    else {
                        date = "завтра"
                    }

                    DaysListItem(uiState, date, selected, uiState.isDaysListEditing,
                        {
                            if (uiState.days.size != 1) {
                                if (uiState.selectedPickerDate == day.second) {
                                    onDateSelected(uiState.days[uiState.days.indexOf(day) - 1].second)
                                }
                            }
                            else {
                                viewModel.setIsDaysListEditing(false)
                                onDateSelected(uiState.currentDate)
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
    viewModel: PlansScreenViewModel = viewModel()
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
                    onValueChange = { viewModel.onTempPlanInputChange(it) },
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
                    onValueChange = { viewModel.onTempPlanInputChange(it) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 120.dp)
                        .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                        .animateItem(placementSpec = spring()),
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            if (!uiState.havePlans) {
                Button(
                    onClick = {
                        viewModel.saveNewPlans()
                        viewModel.tempPlans("")
                        createNotificationChannel(context)
                        AlarmScheduler.schedulePlansReset(context)
                    },
                    enabled = uiState.tempPlanInput.isNotBlank(),
                    modifier = Modifier.animateItem(placementSpec = spring())
                ) {
                    Text(text = stringResource(R.string.button_set_plans))
                }
            } else {
                Row {
                    ElevatedButton(
                        onClick = { viewModel.addPlans() },
                        enabled = uiState.tempPlanInput.isNotBlank() && !uiState.isPlanEditing,
                        modifier = Modifier.animateItem(placementSpec = spring())
                    ) {
                        Text(text = stringResource(R.string.button_add_plans))
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    if (uiState.isPlanEditing) {
                        ElevatedButton(
                            onClick = { viewModel.endEditingPlans() },
                            enabled = uiState.tempPlanInput.isNotBlank(),
                            modifier = Modifier
                                .animateItem(placementSpec = spring())
                        ) {
                            Text(stringResource(R.string.button_finish_editing))
                        }
                    } else {
                        ElevatedButton(
                            onClick = { viewModel.startEditingPlans() },
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

@Composable
fun DayLabel(uiState: PlansScreenUiState, drawerState: DrawerState, scope: CoroutineScope) {
    var day: String
    try {
        day = LocalDate.parse(uiState.selectedPickerDate,
            DateTimeFormatter.ofPattern("dd_MM_yyyy", Locale.getDefault()))
            .format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))
    }
    catch (_: DateTimeParseException) {
        day = uiState.selectedPickerDate
    }

    val color = ButtonDefaults.textButtonColors().contentColor

    Row(verticalAlignment = Alignment.Top, modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp)
    ) {
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
}