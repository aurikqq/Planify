package com.aurikqq.planify

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.aurikqq.planify.ui.theme.PlanifyTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConstantPlansScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    var haveConstantPlans by rememberSaveable { mutableStateOf(sharedPreferences.getBoolean(KEY_HAVE_CONSTANT_PLANS, false)) }
    var constantPlans by remember { mutableStateOf(sharedPreferences.getString(KEY_CONSTANT_PLANS, "") ?: "") }
    var isEditing by remember { mutableStateOf(false) }
    var tempConstantPlans by remember { mutableStateOf("") }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(start = 24.dp, top = 96.dp, end = 24.dp, bottom = 32.dp),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (haveConstantPlans) {
            item {
                if (!isEditing) {
                    Card(
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier
                            .widthIn(max = 800.dp)
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 120.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                clip = false
                            )
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Вот то, что ты сохранил:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = constantPlans,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = constantPlans,
                        onValueChange = { constantPlans = it },
                        label = { "Изменяй и властвуй..." },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 120.dp)
                            .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                            .animateItem(placementSpec = spring())
                    )
                }
                Spacer(modifier = Modifier.size(32.dp))
            }

            item {
                if (!isEditing) {
                    ElevatedButton(
                        onClick = {
                            isEditing = true
                        }
                    ) {
                        Text(stringResource(R.string.button_edit_plans))
                    }
                } else {
                    ElevatedButton(
                        onClick = {
                            isEditing = false
                            sharedPreferences.edit { putString(KEY_CONSTANT_PLANS, constantPlans) }
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_added_plans),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Text(stringResource(R.string.button_finish_editing))
                    }
                }
            }
        } else {
            item {
                Icon(
                    Icons.AutoMirrored.Filled.HelpOutline,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "Постояные планы - раздел, где ты можешь записать что угодно, " +
                            "и записи не удалятся, пока ты их не изменишь.\n" +
                            "Тут может быть то, что тебе надо\nзапомнить или сделать не сегодня.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 16.dp)
                )
                Spacer(modifier = Modifier.size(32.dp))
                OutlinedTextField(
                    value = tempConstantPlans,
                    onValueChange = { tempConstantPlans = it },
                    label = { "Что стоит запомнить?" },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 120.dp)
                        .sizeIn(maxWidth = 800.dp, maxHeight = 600.dp)
                        .animateItem(placementSpec = spring())
                )
                Spacer(modifier = Modifier.size(32.dp))
            }
            item {
                Button(
                    onClick = {
                        sharedPreferences.edit {
                            putString(KEY_CONSTANT_PLANS, tempConstantPlans)
                            putBoolean(KEY_HAVE_CONSTANT_PLANS, true)
                        }

                        constantPlans = tempConstantPlans
                        haveConstantPlans = true
                        tempConstantPlans = ""

                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_set_plans), Toast.LENGTH_SHORT
                        ).show()

                    },
                    enabled = tempConstantPlans.isNotBlank(),
                    modifier = Modifier.animateItem(placementSpec = spring())
                ) {
                    Text(stringResource(R.string.button_set_plans))
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, locale = "ru")
@Composable
fun ConstantPlansPreview() {
    PlanifyTheme {
        ConstantPlansScreen()
    }
}