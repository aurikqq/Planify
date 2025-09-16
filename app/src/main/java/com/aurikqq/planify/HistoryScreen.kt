package com.aurikqq.planify

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    val plansListJson = sharedPreferences.getString(KEY_DAILY_PLANS_HISTORY, "") ?: ""

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
            items(plansList) { plan ->
                var isCardExtended by remember { mutableStateOf(plan == plansList.first()) }
                Card(
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            clip = false
                        )
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = plan.second,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                            )

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                null,
                                modifier = Modifier
                                    .rotate(if (isCardExtended) 180f else 0f)
                                    .clickable(onClick = { isCardExtended = !isCardExtended })
                                    .animateItem()
                            )
                        }
                        if (isCardExtended) {
                            Text(
                                text = plan.first,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
}