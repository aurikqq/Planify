package com.aurikqq.planify.widgets.textwidget

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.Repository
import com.aurikqq.planify.TextWidgetDataTypes
//import com.aurikqq.planify.setTextWidgetData
import com.aurikqq.planify.ui.theme.PlanifyTheme
import kotlinx.coroutines.launch

class WidgetSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PlanifyTheme {
                Scaffold(topBar = { TopBar() }, modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SetupContent(Modifier.padding(innerPadding))
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopBar() {
        TopAppBar(
            title = {
                Text("Выбери, что отображать в виджете")
            }
        )
    }

    @Composable
    fun SetupContent(modifier: Modifier) {
        val context = LocalContext.current

        val repo = Repository(
            context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE),
            context
        )

        val notes = repo.getNotesList()
        val days = repo.getDaysList()

        LazyVerticalGrid (
            columns = GridCells.Adaptive(minSize = 400.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (days.isNotEmpty()) {
                item {
                    Text("Планы", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(32.dp))
                }

                items(days) { day ->
                    Card(
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .widthIn(max = 800.dp)
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 120.dp)
                            .padding(8.dp)
                            .clickable(
                                enabled = true,
                                onClick = {
                                    lifecycleScope.launch {
                                        //setTextWidgetData(TextWidgetDataTypes.PLANS, day.second, context)
                                    }
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = day.second,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                )
                            }
                            Text(
                                text = day.first,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.size(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.size(16.dp))
                }
            }
            if (notes.isNotEmpty()) {
                item {
                    Text("Записи", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(32.dp))
                }

                items(notes) { note ->
                    Card(
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .widthIn(max = 400.dp)
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 120.dp)
                            .padding(8.dp)
                            .clickable(
                                enabled = true,
                                onClick = {
                                    lifecycleScope.launch {
                                        //setTextWidgetData(TextWidgetDataTypes.NOTE, note.id, context)
                                        Log.d("Widget", "Started setting, type: note, id: ${note.id}")
                                    }
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = note.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                )
                            }
                            Text(
                                text = note.text,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.size(12.dp))
            }
        }
    }
}