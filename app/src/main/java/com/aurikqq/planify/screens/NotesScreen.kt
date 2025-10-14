package com.aurikqq.planify.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.R
import com.aurikqq.planify.Repository
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.Note
import com.aurikqq.planify.viewmodels.NotesScreenViewModel
import com.aurikqq.planify.viewmodels.NotesScreenViewModelFactory

data class NotesScreenUiState(
    val notes: MutableList<Note> = mutableListOf(),
    val tempNoteTitle: String = "",
    val tempNote: String = "",
    val isAddingNote: Boolean = false,
    val isEditing: Boolean = false
)

@Composable
fun NotesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val viewModel: NotesScreenViewModel = viewModel(
        factory = NotesScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE
                ),
                context
            )
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(start = 24.dp, top = 96.dp, end = 24.dp, bottom = 32.dp),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (uiState.notes.isNotEmpty()) {
            items(uiState.notes) { note ->
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
                        .animateContentSize(spring())
                ) {
                    var isEditing by remember { mutableStateOf(false) } // human, i remember you're genocides
                    var isExpanded by remember { mutableStateOf(note.isExpanded) }
                    val deg by animateFloatAsState(if (isExpanded) 0f else 180f)

                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!isEditing) {
                            Row (
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    note.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                )
                                IconButton(
                                    onClick = {
                                        note.isExpanded = !note.isExpanded
                                        viewModel.setNote(note)
                                        isExpanded = !isExpanded
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown, null,
                                        modifier = Modifier
                                            .rotate(deg)
                                            .animateItem()
                                    )
                                }
                            }
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically (
                                    expandFrom = Alignment.Top,
                                    animationSpec = tween()
                                ) + fadeIn(),
                                exit = shrinkVertically(
                                    shrinkTowards = Alignment.Top,
                                    animationSpec = tween()
                                ) + fadeOut()
                            ) {
                                Text(
                                    text = note.text,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else {
                            BasicTextField(
                                value = note.title,
                                onValueChange = {
                                    viewModel.onNoteTitleEditingInput(it)
                                    note.title = it },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .fillMaxWidth()
                            )

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                            BasicTextField(
                                value = note.text,
                                onValueChange = {
                                    viewModel.onNoteTextEditingInput(it)
                                    note.text = it },
                                textStyle = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))

                        Row(
                            horizontalArrangement = if (isExpanded) Arrangement.SpaceBetween else Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!isEditing) {
                                if (isExpanded)
                                    FilledTonalButton(
                                        onClick = {
                                            isEditing = true
                                            viewModel.isEditing(true)
                                        },
                                        enabled = !uiState.isEditing && !uiState.isAddingNote
                                    ) {
                                        Text("Поменять")
                                    }

                                IconButton(
                                    onClick = { viewModel.removeNote(note) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null)
                                }
                            } else {
                                ElevatedButton(
                                    onClick = {
                                        isEditing = false
                                        viewModel.setNote(note)
                                        viewModel.isEditing(false)
                                    }
                                ) {
                                    Text(stringResource(R.string.button_finish_editing))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.size(32.dp))
            }

            item {
                if (!uiState.isAddingNote) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { viewModel.isAddingNote(true) },
                            enabled = !uiState.isEditing
                        ) {
                            Text("Добавить запись")
                        }
                    }
                }
                else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyNoteCard(uiState, viewModel, Modifier.animateItem(placementSpec = spring()))

                        Spacer(Modifier.size(32.dp))

                        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                            ElevatedButton(
                                onClick = { viewModel.isAddingNote(false) }
                            ) {
                                Text("Отмени")
                            }

                            Button(
                                onClick = {
                                    viewModel.setNote()
                                    viewModel.isAddingNote(false)
                                },
                                enabled = uiState.tempNote.isNotBlank() && uiState.tempNoteTitle.isNotBlank()
                            ) {
                                Text(stringResource(R.string.button_add_plans))
                            }
                        }
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
                    "Записи - раздел, где ты можешь оставить что угодно, " +
                            "и сохранённое здесь не удалится.\n" +
                            "Тут может быть то, что тебе надо\nзапомнить или сделать не сегодня.\n\n" +
                            "Также, можно делать несколько отдельных записей.\n",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.size(32.dp))

                EmptyNoteCard(uiState, viewModel, Modifier.animateItem(placementSpec = spring()))

                Spacer(modifier = Modifier.size(32.dp))
            }
            item {
                Button(
                    onClick = { viewModel.setNote() },
                    enabled = uiState.tempNote.isNotBlank() && uiState.tempNoteTitle.isNotBlank(),
                    modifier = Modifier.animateItem(placementSpec = spring())
                ) {
                    Text(stringResource(R.string.button_set_plans))
                }
            }
        }
    }
}

@Composable
fun EmptyNoteCard(uiState: NotesScreenUiState, viewModel: NotesScreenViewModel, modifier: Modifier) {
// modifier cause spring animation is impossible to use outside of lazy column, I guess
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.tempNoteTitle,
                onValueChange = { viewModel.onNoteTitleInput(it) },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
                placeholder = { Text("Как назвать запись?") },
                modifier = modifier
                    .height(52.dp)
                    .defaultMinSize(minWidth = 240.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = uiState.tempNote,
                onValueChange = { viewModel.onNoteTextInput(it) },
                placeholder = { Text("Что стоит запомнить?") },
                modifier = modifier
                    .defaultMinSize(minHeight = 120.dp)
                    .sizeIn(maxHeight = 800.dp)
                    .fillMaxWidth()
            )
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