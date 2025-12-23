package com.aurikqq.planify.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.Repository
import com.aurikqq.planify.snowfall
import com.aurikqq.planify.viewmodels.HistoryScreenViewModel
import com.aurikqq.planify.viewmodels.HistoryScreenViewModelFactory

data class HistoryScreenUiState(
    val plansList: MutableList<Pair<String, String>> = mutableListOf(),
    val email: String = "",
    val isSignedIn: Boolean = false
)

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val viewModel: HistoryScreenViewModel = viewModel(
        factory = HistoryScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE
                ),
                context
            )
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSignedIn) {
        LaunchedEffect(Unit) {
            viewModel.getPlansFromDatabase()
        }
    }

    val height by animateDpAsState(if (uiState.plansList.isNotEmpty()) 24.dp else 48.dp, tween())
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(
            start = 8.dp, top = height, end = 8.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp + 32.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .snowfall()
    ) {
        if (uiState.plansList.isNotEmpty()) {
            items(uiState.plansList) { plan ->
                var isCardExpanded by remember { mutableStateOf(plan == uiState.plansList.last()) }
                val deg by animateFloatAsState(if (isCardExpanded) 180f else 0f)
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 120.dp)
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = plan.second,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                            )
                            IconButton(
                                onClick = { isCardExpanded = !isCardExpanded },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    null,
                                    modifier = Modifier
                                        .rotate(deg)
                                        .animateItem()
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = isCardExpanded,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                        ) {
                            Text(
                                text = plan.first,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row (horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                onClick = {
                                    viewModel.removeFromHistory(plan.second)
                                    uiState.plansList.remove(plan)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
            }
        } else {
            item {
                Text(
                    text = "Здесь будут твои планы,\nоставшиеся в прошлом...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}