package com.aurikqq.planify

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit

const val PREFERENCES_NAME = "com.aurikqq.planify.AppPreferences"
const val KEY_PLANS = "user_plans"
const val KEY_HAVE_PLANS = "does_user_have_plans"
const val KEY_IS_FIRST_LAUNCH = "is_first_launch"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Plans(modifier: Modifier = Modifier) {
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
    var isDebugEnabled by remember { mutableStateOf(false) }
    createNotificationChannel(context)
    RequestNotificationsPermission()
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (havePlans) Arrangement.Top else Arrangement.Center,
        contentPadding = if (havePlans) PaddingValues(start = 24.dp, top = 96.dp, end = 24.dp, bottom = 32.dp)
                        else PaddingValues(start = 24.dp, top = 32.dp, end = 24.dp),
        modifier = modifier
            .fillMaxSize()
    ) {
        if (havePlans) {
            item {
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.plans_card_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        isDebugEnabled = !isDebugEnabled
                                        Toast.makeText(context, if (isDebugEnabled) "Debug enabled. No functionality so far" else "Debug disabled", Toast.LENGTH_SHORT).show()
                                    })
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
                        .heightIn(max = 180.dp)
                        .animateItem(placementSpec = spring()),
                )
            }
            else {
                OutlinedTextField(
                    value = tempPlans,
                    label = { Text(text = if (havePlans) stringResource(R.string.label_add_plans) else stringResource(R.string.label_set_plans)) },
                    onValueChange = { tempPlans = it },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 120.dp)
                        .heightIn(max = 180.dp)
                        .animateItem(placementSpec = spring()),
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            if (!havePlans) {
                Button(onClick = {
                    sharedPreferences.edit(commit = true) {
                        putString(KEY_PLANS, tempPlans)
                        putBoolean(KEY_HAVE_PLANS, true)
                        putBoolean(KEY_IS_FIRST_LAUNCH, false)
                    }
                    plans = tempPlans
                    havePlans = true
                    tempPlans = ""
                    Handler(Looper.getMainLooper()).postDelayed({
                        schedulePlanReminders(context.applicationContext)
                        scheduleReset(context.applicationContext)
                        scheduleResetNotification(context.applicationContext)
                    }, 200)
                    Toast.makeText(context,
                        context.getString(R.string.toast_added_plans), Toast.LENGTH_SHORT).show()
                },
                    enabled = tempPlans.isNotBlank(),
                    modifier = Modifier.animateItem(placementSpec = spring())
                ) {
                    Text(text = stringResource(R.string.button_set_plans))
                }
            }
            else {
                Row {
                    ElevatedButton(
                        onClick = {
                            sharedPreferences.edit {
                                putString(KEY_PLANS, plans + "\n" + tempPlans)
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
                                isEditing = false 
                                Toast.makeText(context,
                                    context.getString(R.string.toast_edited_plans), Toast.LENGTH_SHORT).show()
                                sharedPreferences.edit {
                                    putString(KEY_PLANS, plans)
                                }
                            },
                            enabled = plans.isNotBlank(),
                            modifier = Modifier
                                .animateItem(placementSpec = spring())
                        ) {
                            Text(stringResource(R.string.button_finish_editing))
                        }
                    }
                    else {
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
                text =  if (havePlans)
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
    Plans()
}

// TODO

//DONE ! fix data disappearing on relaunch

// DONE polish icon
// DONE translate strings
// DONE make plans editing
// DONE fix notifications icon and launch screen
// DONE make lang independent from system
// DONE make debug menu
// clean up code

// add settings