package com.aurikqq.planify.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aurikqq.planify.AlarmScheduler
import com.aurikqq.planify.CHANGELOG
import com.aurikqq.planify.R
import com.aurikqq.planify.SETTINGS_SCREEN
import com.aurikqq.planify.checkUpdates
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.SettingsScreenViewModel
import kotlinx.coroutines.launch

data class SettingsScreenUiState (
    val plansNotificationsEnabled: Boolean = true,
    val plansNotificationsCooldown: Float = 2f,
    val resetNotificationsEnabled: Boolean = true,
    val isDarkThemeOn: Boolean = false
)

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsScreenViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val navBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(SETTINGS_SCREEN)
    }

    var isChangelogShown by remember { mutableStateOf(false) }
    var isThemeModalSheetShown by remember { mutableStateOf(false) }

    var tempPlansNotificationsEnabled by remember { mutableStateOf(uiState.plansNotificationsEnabled) }
    var tempPlansNotificationsCooldown by remember { mutableFloatStateOf(uiState.plansNotificationsCooldown) }
    var tempResetNotificationsEnabled by remember { mutableStateOf(uiState.resetNotificationsEnabled) }

    Scaffold(topBar = { SettingsTopBar() }) { innerPadding ->
        if (isChangelogShown) {
            AlertDialog(
                onDismissRequest = { isChangelogShown = false },
                title = {
                    Text("Что поменялось в этой версии:")
                },
                text = {
                LazyColumn {
                    item {
                        Text(CHANGELOG)
                    }
                    item {
                        AsyncImage(
                            model = "https://i.pinimg.com/736x/89/2b/4d/892b4ddaa245216690b0e066a500a5d1.jpg",
                            contentDescription = null
                        )
                    }
                }
                },
                confirmButton = {
                    TextButton(onClick = { isChangelogShown = false }) {
                        Text("Понял")
                    }
                }
            )
        }

        if (isThemeModalSheetShown) {
            ThemeModalSheet(uiState, viewModel) { isThemeModalSheetShown = false }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 80.dp
                )
        ) {
            item {
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
                                        //createNotificationChannel(context)
                                        AlarmScheduler.scheduleRepeatingAlarm(context)
                                    } else {
                                        AlarmScheduler.cancelNotifications(context)
                                    }
                                }
                            )
                        },
                        leadingContent = { Icon(Icons.Default.Notifications, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    Row {
                        Spacer(Modifier.size(40.dp))
                        ListItem(
                            headlineContent = {
                                var hours by remember { mutableFloatStateOf(tempPlansNotificationsCooldown) }
                                Column {
                                    Row {
                                        RollingNumberText(hours.toInt())
                                    }
                                    Spacer(Modifier.size(8.dp))

                                    Slider(
                                        value = hours,
                                        onValueChange = { hours = it },
                                        onValueChangeFinished = {
                                            viewModel.setPlansNotificationCooldown(hours)
                                            tempPlansNotificationsCooldown = hours
                                            //createNotificationChannel(context)
                                            AlarmScheduler.scheduleRepeatingAlarm(context)
                                        },
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
                                onCheckedChange = {
                                    tempResetNotificationsEnabled = it

                                    viewModel.setResetNotificationsEnabled(tempResetNotificationsEnabled)
                                    if (tempResetNotificationsEnabled) {
                                        //createNotificationChannel(context)
                                        AlarmScheduler.schedulePlansReset(context)
                                    } else {
                                        AlarmScheduler.cancelResetNotifications(context)
                                    }
                                }
                            )
                        },
                        leadingContent = { Icon(Icons.Default.NotificationsNone, null) },
                        supportingContent = { Text("Уведомления о ночном сбросе ежедневных планов") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
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
                        supportingContent = { Text(
                            text = when(uiState.isDarkThemeOn) {
                                true -> "Тёмная"
                                false -> "Светлая"
                            }
                        ) },
                        modifier = Modifier.clickable(
                            enabled = true,
                            onClick = { isThemeModalSheetShown = true },
                            interactionSource = null,
                            indication = ripple(bounded = true)
                        )
                    )
                }
            }

            item {
                SettingsCategory("Всякое", false) {
                    ListItem(
                        headlineContent = { Text("Новое в этой версии") },
                        leadingContent = { Icon(Icons.Default.Info, null) },
                        modifier = Modifier.clickable(
                            enabled = true,
                            onClick = { isChangelogShown = true },
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
                                    val whaaatIsItAnUpdate = checkUpdates(context)
                                    val text = when (whaaatIsItAnUpdate) {
                                        false -> "Обновлений не нашлось..."
                                        true -> "Обновления нашлись! Вернись к планам, чтобы скачать"
                                    }
                                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                                }
                            },
                            interactionSource = null,
                            indication = ripple(bounded = true)
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun RollingNumberText(
    targetValue: Int,
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableStateOf(targetValue) }
    var direction by remember { mutableStateOf(0) }

    LaunchedEffect(targetValue) {
        if (targetValue != currentValue) {
            direction = if (targetValue > currentValue) 1 else -1
            currentValue = targetValue
        }
    }

    Text("Отправлять раз в ")

    AnimatedContent(
        targetState = currentValue,
        transitionSpec = {
            // Явно именуем параметры initialOffsetY / targetOffsetY, чтобы избежать
            // несоответствий с другими перегрузками slideIn/slideOut.
            if (direction > 0) {
                // Новое число выезжает снизу -> старое уходит наверх
                (slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { fullHeight -> fullHeight } // start below
                ) + fadeIn()).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetY = { fullHeight -> -fullHeight } // go above
                    ) + fadeOut())
            } else {
                // Новое число выезжает сверху -> старое уходит вниз
                (slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { fullHeight -> -fullHeight } // start above
                ) + fadeIn()).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetY = { fullHeight -> fullHeight } // go below
                    ) + fadeOut())
            }
        },
        label = "rollingNumber"
    ) { value ->
        if (value != 1) {
            Text(
                "$value ",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    Text(
        text = when (targetValue) {
            1, 21 -> "час"
            in 2..4, 22, 23 -> "часа"
            else -> "часов"
        },
        color = if (targetValue == 1) MaterialTheme.colorScheme.primary
        else Color.Unspecified
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModalSheet(uiState: SettingsScreenUiState, viewModel: SettingsScreenViewModel, onDismiss: () -> Unit) {
    val isInDarkTheme = uiState.isDarkThemeOn
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(modifier = Modifier.height(240.dp)) {
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    PlanifyTheme(darkTheme = false) {
                        ThemeBox("Светлая", !isInDarkTheme) {
                            viewModel.setIsInDarkTheme(false)
                        }
                    }

                    PlanifyTheme(darkTheme = true) {
                        ThemeBox("Тёмная", isInDarkTheme) {
                            viewModel.setIsInDarkTheme(true)
                        }
                    }
                }

                Spacer(Modifier.size(24.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Image(painterResource(R.drawable.pixil_frame_0), null, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

@Composable
fun ThemeBox(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(140.dp, 180.dp)
                .border(
                    animateDpAsState(if (isSelected) 4.dp else 0.dp).value,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .padding(animateDpAsState(if (isSelected) 4.dp else 0.dp).value)
                .background(MaterialTheme.colorScheme.background)
                .clickable(onClick = onClick)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .fillMaxWidth()
                            .height(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp, 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )

                    }
                    HorizontalDivider()
                }

                Box(
                    modifier = Modifier
                        .size(80.dp, 88.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDefaults.cardColors().containerColor)
                )
                Spacer(Modifier.size(8.dp))

                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(BottomAppBarDefaults.containerColor)
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = title,
            color = animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified).value,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() {
    TopAppBar(
        title = { Text("Настройки") },
        windowInsets = WindowInsets(0,0, 0, 0)
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
        Surface(color = Color.Transparent, modifier = Modifier.padding(horizontal = 16.dp)) {
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

//@Composable
//fun FakeNavController(): NavHostController {
//    val context = LocalContext.current
//    val navController = remember { NavHostController(context) }
//
//    navController.navigatorProvider.addNavigator(ComposeNavigator())
//
//    val navGraph = navController.createGraph(startDestination = "settings") {
//        composable("settings") { }
//    }
//
//    navController.graph = navGraph
//    return navController
//}
//
//
//@Preview(showSystemUi = true, showBackground = true)
//@Composable
//fun SettingsPreview() {
//    val context = LocalContext.current
//    val settingsViewModel: SettingsScreenViewModel = viewModel(
//        factory = SettingsScreenViewModelFactory(
//            Repository(
//                context.getSharedPreferences(
//                    PREFERENCES_NAME, Context.MODE_PRIVATE
//                ),
//                context
//            )
//        )
//    )
//
//    //PlanifyTheme { SettingsScreen(settingsViewModel) }
//}