package com.aurikqq.planify.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import coil.compose.AsyncImage
import com.aurikqq.planify.AlarmScheduler
import com.aurikqq.planify.CHANGELOG
import com.aurikqq.planify.R
import com.aurikqq.planify.snowfall
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.SettingsScreenViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.delay
import java.security.SecureRandom
import java.util.Base64

data class SettingsScreenUiState (
    val plansNotificationsEnabled: Boolean = true,
    val plansNotificationsCooldown: Float = 2f,
    val resetNotificationsEnabled: Boolean = true,
    val isDarkThemeOn: Boolean = false,
    val isSignedIn: Boolean = false,
    val email: String = "null"
)

@SuppressLint("NewApi")
@Composable
fun SettingsScreen(viewModel: SettingsScreenViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    //val scope = rememberCoroutineScope()

    var isChangelogShown by remember { mutableStateOf(false) }
    var isThemeModalSheetShown by remember { mutableStateOf(false) }
    var isSignInBottomSheetShown by remember { mutableStateOf(false) }
    var isSyncAlertModalShown by remember { mutableStateOf(false) }

    var tempPlansNotificationsEnabled by remember { mutableStateOf(uiState.plansNotificationsEnabled) }
    var tempPlansNotificationsCooldown by remember { mutableFloatStateOf(uiState.plansNotificationsCooldown) }
    var tempResetNotificationsEnabled by remember { mutableStateOf(uiState.resetNotificationsEnabled) }

    Scaffold(topBar = { SettingsTopBar() }) { innerPadding ->
        if (isChangelogShown) {
            AlertDialog(
                onDismissRequest = { isChangelogShown = false },
                title = {
                    Text("\uD83C\uDF84 Что поменялось в этой версии:")
                },
                text = {
                LazyColumn {
                    item {
                        Text(CHANGELOG)
                    }
                    item {
                        AsyncImage(
                            model = "https://i.pinimg.com/736x/26/90/2d/26902dc92d66500f3bab3f602d3fe4f2.jpg",
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

        if (isSignInBottomSheetShown) {
            SignInBottomSheet(viewModel, "714660007842-dkrp22efm0qaek80jtr0lnokg5vtajv1.apps.googleusercontent.com")
        }

        if (isSyncAlertModalShown) {
            SyncAlertModal(onClick = {
                isSignInBottomSheetShown = true
                isSyncAlertModalShown = false
            },
            onDismiss = { isSyncAlertModalShown = false })
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = 80.dp)
                )
                .snowfall()
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                item {
                    if (!uiState.isSignedIn) {
                        SignInOffer { isSyncAlertModalShown = true }
                    } else {
                        AccountInfo(uiState, viewModel)
                    }
                }
            }


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
                SettingsCategory("Поведение") {
                    ListItem(
                        headlineContent = { Text("Быстрая запись") },
                        trailingContent = {
                            Switch(
                                checked = false,
                                enabled = false,
                                onCheckedChange = { }
                            )
                        },
                        leadingContent = { Icon(Icons.Default.Keyboard, null) },
                        supportingContent = { Text(
                            "При входе сразу появится клавиатура, если нет планов"
                            //"Если ничего не запланировано, при открытии приложения сразу появляется клавиатура, чтобы записать свои дела можно было ещё быстрее"
                        ) },
                        modifier = Modifier.clickable(
                            enabled = true,
                            onClick = {  },
                            interactionSource = null,
                            indication = ripple(bounded = true)
                        )
                    )

                    ListItem(
                        headlineContent = { Text("Быстрое дополнение") },
                        trailingContent = {
                            Switch(
                                checked = false,
                                enabled = false,
                                onCheckedChange = { }
                            )
                        },
                        leadingContent = { Icon(Icons.Default.AddTask, null) },
                        supportingContent = { Text(
                            "При входе сразу появится клавиатура, если есть планы"
                        ) },
                        modifier = Modifier.clickable(
                            enabled = true,
                            onClick = {  },
                            interactionSource = null,
                            indication = ripple(bounded = true)
                        )
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

//                    ListItem(
//                        headlineContent = { Text("Проверить обновления") },
//                        leadingContent = { Icon(Icons.Default.Update, null) },
//                        modifier = Modifier.clickable(
//                            enabled = true,
//                            onClick = {
//                                scope.launch {
//                                    val whaaatIsItAnUpdate = checkUpdates(context)
//                                    val text = when (whaaatIsItAnUpdate) {
//                                        false -> "Обновлений не нашлось..."
//                                        true -> "Обновления нашлись! Перезайди в Planify, чтобы скачать"
//                                    }
//                                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
//                                }
//                            },
//                            interactionSource = null,
//                            indication = ripple(bounded = true)
//                        )
//                    )
                }
            }
        }
    }
}

@Composable
fun RollingNumberText(targetValue: Int) {
    var currentValue by remember { mutableIntStateOf(targetValue) }
    var direction by remember { mutableIntStateOf(0) }

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
            if (direction > 0) {
                (slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn()).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetY = { fullHeight -> -fullHeight }
                    ) + fadeOut())
            } else {
                (slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { fullHeight -> -fullHeight }
                ) + fadeIn()).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetY = { fullHeight -> fullHeight }
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

@Composable
fun SignInOffer(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        //tonalElevation = if (isSystemInDarkTheme()) 1.dp else 0.dp,
        //shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                "Синхронизация с Google",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Твои планы могут синхронизироваться на всех твоих устройстах! " +
                            "Для этого просто войди через Google одним касанием - и не забывай о своих делах нигде." +
                            "\n\nВсе твои данные остаются при тебе.\nПока что синхронизация тестируется."
                )

                Spacer(Modifier.size(24.dp))

                Box(Modifier.padding(bottom = 8.dp)) {
                    Button(
                        onClick = onClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonColors(
                            containerColor = Color(0xFFFFFFFF),
                            contentColor = Color(0xFF1F1F1F),
                            disabledContentColor = ButtonDefaults.buttonColors().disabledContentColor,
                            disabledContainerColor = ButtonDefaults.buttonColors().disabledContainerColor
                        ),
                        modifier = Modifier
                            .height(40.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(R.drawable.g_logo), null)
                            Spacer(Modifier.size(10.dp))
                            Text(
                                "Войти через Google",
                                fontFamily = FontFamily(Font(R.font.roboto_medium)),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountInfo(uiState: SettingsScreenUiState, viewModel: SettingsScreenViewModel) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        //tonalElevation = if (isSystemInDarkTheme()) 1.dp else 0.dp,
        //shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                "Твой аккаунт",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(uiState.email, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.size(12.dp))
                Text("Планы и записи синхронизируются с другими твоими устройствами через этот аккаунт Google - всё в сохранности.")
                Spacer(Modifier.size(12.dp))

                Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { viewModel.logOut() }) {
                        Text("Выйти")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModalSheet(uiState: SettingsScreenUiState, viewModel: SettingsScreenViewModel, onDismiss: () -> Unit) {
    val pics = listOf(R.drawable.snowflake, R.drawable.heart, R.drawable.christmas_hat, R.drawable.christmas_tree)

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
                    Image(painterResource(pics.random()), null, modifier = Modifier.size(32.dp))
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
fun SyncAlertModal(onClick: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Постой...")
        },
        text = {
            Text("Если в аккаунте, куда ты войдёшь, есть сохранённые планы или записи, то они перезапишут те, что сохранены на этом устройстве!\n\n" +
                    "Запиши их куда-нибудь, чтобы потом перенести в аккаунт.")
        },
        confirmButton = {
            ElevatedButton(onClick = onClick) {
                Text("Продолжай")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмени")
            }
        }
    )
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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun SignInBottomSheet(viewModel: SettingsScreenViewModel, webClientId: String) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        signIn(viewModel, request, context)
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun signIn(viewModel: SettingsScreenViewModel,request: GetCredentialRequest, context: Context): Exception? {
    val credentialManager = CredentialManager.create(context)
    val failureMessage = "Unable to sign in"
    val e: Exception? = null
    val TAG = "Sign In"

    delay(250)
    try {
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val email = GoogleIdTokenCredential.createFrom(credential.data).id

            viewModel.logIn(email)
        }

        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Sign in successful!")
    } catch (e: GetCredentialException) {
        Toast.makeText(context, "Не получилось войти...", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Failure getting credentials", e)

    } catch (e: GoogleIdTokenParsingException) {
        Toast.makeText(context, "Не получилось войти...", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Issue with parsing received GoogleIdToken", e)

    } catch (e: NoCredentialException) {
        Toast.makeText(context, "Не получилось войти...", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: No credentials found", e)
        return e

    } catch (e: GetCredentialCustomException) {
        Toast.makeText(context, "Не получилось войти...", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Issue with custom credential request", e)

    } catch (e: GetCredentialCancellationException) {
        Log.e(TAG, "$failureMessage: Sign-in was cancelled", e)
    }
    return e
}

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}