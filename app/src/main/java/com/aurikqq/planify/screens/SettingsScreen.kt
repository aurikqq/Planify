package com.aurikqq.planify.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aurikqq.planify.AlarmScheduler
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.Repository
import com.aurikqq.planify.SETTINGS_SCREEN
import com.aurikqq.planify.checkUpdates
import com.aurikqq.planify.createNotificationChannel
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.SettingsScreenViewModel
import com.aurikqq.planify.viewmodels.SettingsScreenViewModelFactory
import kotlinx.coroutines.launch

data class SettingsScreenUiState (
    val plansNotificationsEnabled: Boolean = true,
    val plansNotificationsCooldown: Float = 2f,
    val resetNotificationsEnabled: Boolean = true,
//    val daysListPlacement: Int = 0,
)

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SettingsScreenViewModel = viewModel(
        factory = SettingsScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE
                ),
                context
            )
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val navBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(SETTINGS_SCREEN)
    }

    var tempPlansNotificationsEnabled by remember { mutableStateOf(uiState.plansNotificationsEnabled) }
    var tempPlansNotificationsCooldown by remember { mutableFloatStateOf(uiState.plansNotificationsCooldown) }
    var tempResetNotificationsEnabled by remember { mutableStateOf(uiState.resetNotificationsEnabled) }

//    DisposableEffect(navBackStackEntry) {
//        val lifecycle = navBackStackEntry.lifecycle
//
//        val observer = LifecycleEventObserver {_, event ->
//            if (event == Lifecycle.Event.ON_STOP) {
//                if (!uiState.plansNotificationsEnabled && tempPlansNotificationsEnabled) {
//                    viewModel.setPlansNotificationsEnabled(tempPlansNotificationsEnabled)
//                    viewModel.setPlansNotificationCooldown(tempPlansNotificationsCooldown)
//
//                    createNotificationChannel(context)
//                    AlarmScheduler.schedulePlansReset(context)
//                }
//                else if (uiState.plansNotificationsEnabled && !tempPlansNotificationsEnabled) {
//                    viewModel.setPlansNotificationsEnabled(tempPlansNotificationsEnabled)
//                    viewModel.setPlansNotificationCooldown(tempPlansNotificationsCooldown)
//
//                    AlarmScheduler.cancelNotifications(context)
//                }
//
//                if (!uiState.resetNotificationsEnabled && tempResetNotificationsEnabled) {
//                    /*TODO make cancelResetNotifications and add here*/
//                    viewModel.setResetNotificationsEnabled(tempResetNotificationsEnabled)
//
//                    AlarmScheduler.cancelResetNotifications(context)
//                }
//                else if (uiState.resetNotificationsEnabled && !tempResetNotificationsEnabled) {
//                    viewModel.setResetNotificationsEnabled(tempResetNotificationsEnabled)
//
//                    createNotificationChannel(context)
//                    AlarmScheduler.schedulePlansReset(context)
//                }
//            }
//        }
//        lifecycle.addObserver(observer)
//        onDispose {
//            lifecycle.removeObserver(observer)
//        }
//    }

    SettingsTopBar()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Spacer(Modifier.size(16.dp))

            SettingsCategory("Уведомления") {
                ListItem(
                    headlineContent = { Text("Напоминания о планах") },
                    trailingContent = {
                        Switch(
                            checked = tempPlansNotificationsEnabled,
                            onCheckedChange = {
                                tempPlansNotificationsEnabled = it
                                viewModel.setPlansNotificationsEnabled(tempPlansNotificationsEnabled)
                                viewModel.setPlansNotificationCooldown(tempPlansNotificationsCooldown)
                                if (tempPlansNotificationsEnabled) {
                                    createNotificationChannel(context)
                                    AlarmScheduler.schedulePlansReset(context)
                                }
                                else {
                                    AlarmScheduler.cancelNotifications(context)
                                }
                            }
                        ) },
                    leadingContent = { Icon(Icons.Default.Notifications, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Row {
                    Spacer(Modifier.size(40.dp))
                    ListItem(
                        headlineContent = {
                            var hours = tempPlansNotificationsCooldown
                            Column {
                                Row {
                                    Text("Отправлять раз в ")
                                    if (hours.toInt() != 1) {
                                        Text("${hours.toInt()} ", color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(text = when(hours.toInt()) {
                                        1, 21 -> "час"
                                        in 2..4, 22, 23 -> "часа"
                                        else -> "часов"
                                    },
                                        color = if (hours.toInt() == 1) MaterialTheme.colorScheme.primary
                                            else Color.Unspecified)
                                }
                                Spacer(Modifier.size(8.dp))

                                Slider(
                                    value = hours,
                                    onValueChange = { hours = it },
                                    valueRange = 1f..23f,
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
                ListItem(
                    headlineContent = { Text("Напоминания о сбросах") },
                    trailingContent = {
                        Switch(
                            checked = tempResetNotificationsEnabled,
                            onCheckedChange = { tempResetNotificationsEnabled = it }
                        ) },
                    leadingContent = { Icon(Icons.Default.NotificationsNone, null) },
                    supportingContent = { Text("Уведомления о ночном сбросе ежедневных планов") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            Spacer(Modifier.size(12.dp))
        }

        item {
            SettingsCategory("Внешний вид") {
                ListItem(
                    headlineContent = { Text("Тема приложения") },
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    },
                    leadingContent = { Icon(Icons.Default.InvertColors, null) },
                    supportingContent = { Text("Как на телефоне") },
                    colors = ListItemDefaults.colors(
                        headlineColor = ButtonDefaults.textButtonColors().disabledContentColor,
                        leadingIconColor = ButtonDefaults.textButtonColors().disabledContentColor,
                        supportingColor = ButtonDefaults.textButtonColors().disabledContentColor
                    )
                )

                ListItem(
                    headlineContent = { Text("Язык") },
                    leadingContent = { Icon(Icons.Default.Language, null) },
                    supportingContent = { Text("Русский") },
                    colors = ListItemDefaults.colors(
                        headlineColor = ButtonDefaults.textButtonColors().disabledContentColor,
                        leadingIconColor = ButtonDefaults.textButtonColors().disabledContentColor,
                        supportingColor = ButtonDefaults.textButtonColors().disabledContentColor
                    )
                )

//                ListItem(
//                    headlineContent = { Text("Расположение списка дней") },
//                    leadingContent = { Icon(Icons.Default.SwitchLeft, null) },
//                    supportingContent = { Text(if(uiState.daysListPlacement == 0) "Слева" else "Справа") },
//                    modifier = Modifier.clickable(
//                        enabled = true,
//                        onClick = { viewModel.setDaysListPlacement(if (uiState.daysListPlacement == 0) 1 else 0) },
//                        interactionSource = null,
//                        indication = ripple(bounded = true)
//                    )
//                )
            }
            Spacer(Modifier.size(12.dp))
        }

        item {
            SettingsCategory("Всякое", false) {
                ListItem(
                    headlineContent = { Text("Новое в этой версии") },
                    leadingContent = { Icon(Icons.Default.Info, null) },
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = {},
                        interactionSource = null,
                        indication = ripple(bounded = true)
                    )
                )

                ListItem(
                    headlineContent = { Text("Проверить обновления") },
                    leadingContent = { Icon(Icons.Default.Update, null) },
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = {
                            scope.launch {
                                checkUpdates(context)
                            } },
                        interactionSource = null,
                        indication = ripple(bounded = true)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() {
    MediumTopAppBar(
        title = { Text("Настройки") },
    )
}

@Composable
private fun SettingsCategory(titleText: String, isElevated: Boolean = true, content: @Composable () -> Unit) {
    if (isElevated) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = if (isSystemInDarkTheme()) 1.dp else 0.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    titleText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                content()
            }
        }
    }
    else {
        Surface(color = Color.Transparent) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    titleText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                content()
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SettingsPreview() {
    PlanifyTheme { SettingsScreen(rememberNavController()) }
}