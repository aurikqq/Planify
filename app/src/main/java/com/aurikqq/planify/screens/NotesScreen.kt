package com.aurikqq.planify

import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.NotesScreenViewModel
import com.aurikqq.planify.viewmodels.NotesScreenViewModelFactory

data class NotesScreenUiState(
    val notes: String = "",
    val haveNotes: Boolean = false,
    val isEditing: Boolean = false,
    val tempNotes: String = ""
)

@Composable
fun NotesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val viewModel: NotesScreenViewModel = viewModel(
        factory = NotesScreenViewModelFactory(
            PlansRepository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE
                ),
                context
            )
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(start = 24.dp, top = 96.dp, end = 24.dp, bottom = 32.dp),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (uiState.haveNotes) {
            item {
                if (!uiState.isEditing) {
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
                                text = uiState.notes,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = uiState.tempNotes,
                        onValueChange = { viewModel.onTempNotesInput(it) },
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
                if (!uiState.isEditing) {
                    ElevatedButton(
                        onClick = { viewModel.startEditing() }
                    ) {
                        Text(stringResource(R.string.button_edit_plans))
                    }
                } else {
                    ElevatedButton(
                        onClick = { viewModel.endEditing() }
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
                    value = uiState.tempNotes,
                    onValueChange = { viewModel.onTempNotesInput(it) },
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
                    onClick = { viewModel.setNotes() },
                    enabled = uiState.tempNotes.isNotBlank(),
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
        NotesScreen()
    }
}