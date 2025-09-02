package com.aurikqq.planify

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.serialization.json.Json

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    val plansListJson = sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "") ?: ""

    Box {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(start = 24.dp, top = 96.dp, end = 24.dp, bottom = 32.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (plansListJson.isNotBlank()) {
                val plansList =
                    Json.decodeFromString<MutableList<Pair<String, String>>>(plansListJson)
                Log.d("HistoryScreen", plansList.toString())
                items(plansList) { plan ->
                    Card(
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = plan.second,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = plan.first,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(24.dp))
                }
            } else {
                item {
                    Text(
                        text = "Пока что ты ничего не записал...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(32.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { navController.navigate(PLANS_SCREEN) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.History, null)
                Spacer(Modifier.size(8.dp))
                Text("Дневные планы")
            }
            TextButton(
                onClick = { navController.navigate(CONSTANT_PLANS_SCREEN) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Checklist, null)
                Spacer(Modifier.size(8.dp))
                Text("Постоянные планы")
            }
        }
    }
}