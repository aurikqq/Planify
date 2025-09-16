package com.aurikqq.planify

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aurikqq.planify.ui.theme.PlanifyTheme
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


var historyDate = ""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    createNotificationChannel(context)
    RequestNotificationsPermission()

    Row(modifier = modifier) {
        Column {
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
//                    modifier = Modifier.clickable(onClick = {
//                        val workManager = WorkManager.getInstance(context)
//                        val resetRequest = OneTimeWorkRequestBuilder<PlansReset>().build()
//                        workManager.enqueueUniqueWork(
//                            "plan_reset",
//                            ExistingWorkPolicy.KEEP,
//                            resetRequest
//                        )
//                    })
                )
            }

            NavRail(navController)
        }

        Column {
            TabsBar(navController)

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = PLANS_SCREEN,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(route = PLANS_SCREEN) {
                        DailyPlansScreen(modifier)
                    }
                    composable(route = CONSTANT_PLANS_SCREEN) {
                        ConstantPlansScreen()
                    }
                    composable(route = HISTORY_SCREEN) {
                        HistoryScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun DailyPlansScreen(modifier: Modifier) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    var plans by rememberSaveable {
        mutableStateOf(sharedPreferences.getString(KEY_PLANS, "") ?: "")
    }
    var havePlans by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean(KEY_HAVE_PLANS, false))
    }
    var isFirstLaunch by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true))
    }
    var tempPlans by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var tempPlansBeforeEdit by remember { mutableStateOf("")}

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (havePlans) Arrangement.Top else Arrangement.Center,
        contentPadding = if (havePlans) PaddingValues(
            start = 24.dp,
            top = 96.dp,
            end = 24.dp,
            bottom = 32.dp
        )
        else PaddingValues(start = 24.dp, top = 32.dp, end = 24.dp),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (havePlans) {
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
                            text = if (!isEditing) plans else tempPlansBeforeEdit,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
        item {
            if (isEditing) {
                OutlinedTextField(
                    value = plans,
                    label = { Text(stringResource(R.string.label_edit_plans)) },
                    onValueChange = { plans = it },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 120.dp)
                        .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                        .animateItem(placementSpec = spring()),
                )
            } else {
                OutlinedTextField(
                    value = tempPlans,
                    label = {
                        Text(
                            text = if (havePlans) stringResource(R.string.label_add_plans) else stringResource(
                                R.string.label_set_plans
                            )
                        )
                    },
                    onValueChange = { tempPlans = it },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 120.dp)
                        .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                        .animateItem(placementSpec = spring()),
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            if (!havePlans) {
                Button(
                    onClick = {
                        historyDate =
                            SimpleDateFormat(
                                "d MMMM yyyy, EEEE",
                                Locale.getDefault()
                            ).format(
                                Date()
                            )
                        val plansWithDateSet = Pair(tempPlans, historyDate)
                        var plansListJson =
                            sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "") ?: ""
                        val plansList =
                            if (plansListJson.isNotBlank()) Json.decodeFromString<MutableList<Pair<String, String>>>(plansListJson)
                            else mutableListOf()
                        plansList.add(0, plansWithDateSet)
                        plansListJson = Json.encodeToString(plansList)

                        sharedPreferences.edit(commit = true) {
                            putString(KEY_PLANS, tempPlans)
                            putBoolean(KEY_HAVE_PLANS, true)
                            putBoolean(KEY_IS_FIRST_LAUNCH, false)
                            putString(KEY_DAILY_PLANS_HISTORY, plansListJson)
                        }
                        plans = tempPlans
                        havePlans = true
                        tempPlans = ""

                        Handler(Looper.getMainLooper()).postDelayed({
                            schedulePlanReminders(context.applicationContext)
                            scheduleReset(context.applicationContext)
                            AlarmScheduler.scheduleAlarm(context.applicationContext)
                        }, 200)
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_added_plans),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    enabled = tempPlans.isNotBlank(),
                    modifier = Modifier.animateItem(placementSpec = spring())
                ) {
                    Text(text = stringResource(R.string.button_set_plans))
                }
            } else {
                Row {
                    ElevatedButton(
                        onClick = {
                            val dailyPlansHistory =
                                sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "")
                                    ?: ""
                            val plansList =
                                Json.decodeFromString<MutableList<Pair<String, String>>>(
                                    dailyPlansHistory
                                )
                            plansList[plansList.lastIndex] = plansList.last().copy(
                                first = plans + "\n" + tempPlans
                            )
                            val jsonPlansList = Json.encodeToString(plansList)
                            sharedPreferences.edit {
                                putString(KEY_PLANS, plans + "\n" + tempPlans)
                                putString(KEY_DAILY_PLANS_HISTORY, jsonPlansList)

                            }
                            plans = plans + "\n" + tempPlans
                            tempPlans = ""
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_added_plans),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = tempPlans.isNotBlank() && !isEditing,
                        modifier = Modifier.animateItem(placementSpec = spring())
                    ) {
                        Text(text = stringResource(R.string.button_add_plans))
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    if (isEditing) {
                        ElevatedButton(
                            onClick = {
                                val dailyPlansHistory =
                                    sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "")
                                        ?: ""
                                val plansList =
                                    Json.decodeFromString<MutableList<Pair<String, String>>>(
                                        dailyPlansHistory
                                    )
                                plansList[plansList.lastIndex] = plansList.last().copy(
                                    first = plans
                                )
                                val jsonNewPlansPair = Json.encodeToString(plansList)
                                isEditing = false
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.toast_edited_plans),
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPreferences.edit {
                                    putString(KEY_PLANS, plans)
                                    putString(KEY_DAILY_PLANS_HISTORY, jsonNewPlansPair)
                                }
                            },
                            enabled = plans.isNotBlank(),
                            modifier = Modifier
                                .animateItem(placementSpec = spring())
                        ) {
                            Text(stringResource(R.string.button_finish_editing))
                        }
                    } else {
                        ElevatedButton(
                            onClick = {
                                tempPlansBeforeEdit = plans
                                isEditing = true
                            },
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
                text = if (havePlans)
                    stringResource(R.string.bottom_text_have_plans)
                else if (isFirstLaunch)
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
