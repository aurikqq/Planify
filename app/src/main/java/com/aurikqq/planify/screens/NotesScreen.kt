package com.aurikqq.planify.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.R
import com.aurikqq.planify.Repository
import com.aurikqq.planify.components.AnimatedButton
import com.aurikqq.planify.components.AnimatedElevatedButton
import com.aurikqq.planify.components.AnimatedTonalButton
import com.aurikqq.planify.components.PlansUnit
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.Note
import com.aurikqq.planify.viewmodels.NotesScreenViewModel
import com.aurikqq.planify.viewmodels.NotesScreenViewModelFactory

data class NotesScreenUiState(
    val notes: MutableList<Note> = mutableListOf(),
    val tempNoteTitle: String = "",
    val tempNote: String = "",
    val isAddingNote: Boolean = false,
    val isEditing: Boolean = false,
    val isSignedIn: Boolean = false,
    val email: String = "",
    val plansPrefix: String = "--",
    val isNotesButtonAtEnd: Boolean = true
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

    val height by animateDpAsState(if (uiState.notes.isNotEmpty()) 24.dp else 48.dp, tween())
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(
            start = 8.dp, top = height, end = 8.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp + 32.dp
        ),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .animateContentSize()
            //.snowfall()
    ) {
        if (uiState.notes.isNotEmpty()) {
            if (!uiState.isNotesButtonAtEnd) {
                addNoteSection(uiState, viewModel)
                item { Spacer(Modifier.size(16.dp)) }
            }

            items(uiState.notes, key = { it.id }) { note ->
                NoteCard(note, viewModel, uiState,
                    keyboardController, focusRequester, Modifier)
            }

            if (uiState.isNotesButtonAtEnd) {
                addNoteSection(uiState, viewModel)
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
                    stringResource(R.string.notes_empty_message),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.size(24.dp))

                EmptyNoteCard(uiState, viewModel, Modifier.animateItem(placementSpec = spring()))

                Spacer(modifier = Modifier.size(24.dp))
            }
            item {
                val isEnabled = uiState.tempNote.isNotBlank() && uiState.tempNoteTitle.isNotBlank()

                AnimatedButton(
                    onClick = { viewModel.setNote() },
                    enabled = isEnabled
                ) {
                    Text(stringResource(R.string.button_set_plans))
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.addNoteSection(
    uiState: NotesScreenUiState,
    viewModel: NotesScreenViewModel
) {
    item {
        if (!uiState.isAddingNote) {
            val isEnabled = !uiState.isEditing

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedButton(
                    onClick = { viewModel.isAddingNote(true) },
                    enabled = isEnabled
                ) {
                    Text(stringResource(R.string.label_add_note))
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyNoteCard(uiState, viewModel, Modifier)

                Spacer(Modifier.size(32.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ElevatedButton(
                        onClick = { viewModel.isAddingNote(false) }
                    ) {
                        Text(stringResource(R.string.button_cancel))
                    }

                    val isSetEnabled =
                        uiState.tempNote.isNotBlank() && uiState.tempNoteTitle.isNotBlank()

                    AnimatedButton(
                        onClick = {
                            viewModel.setNote()
                            viewModel.isAddingNote(false)
                        },
                        enabled = isSetEnabled
                    ) {
                        Text(stringResource(R.string.button_add_plans))
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note, viewModel: NotesScreenViewModel, uiState: NotesScreenUiState,
    keyboardController: SoftwareKeyboardController?, focusRequester: FocusRequester, modifier: Modifier
) {
    var isRemoving by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (isRemoving) 0.5f else 1f)

    AnimatedVisibility(
        visible = !isRemoving,
        exit = fadeOut() + shrinkVertically(
            animationSpec = tween(300)
        ),
        modifier = modifier
    ) {
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 120.dp)
                .alpha(alpha)
                .padding(8.dp)
        ) {
            var isEditing by remember { mutableStateOf(false) }
            var isExpanded by rememberSaveable(note.id) { mutableStateOf(note.isExpanded) }
            val deg by animateFloatAsState(if (isExpanded) 180f else 0f)

            Column(modifier = Modifier.padding(16.dp)) {
                if (!isEditing) {
                    Row(
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
                                val updatedNote = note.copy(isExpanded = !note.isExpanded)
                                viewModel.setNote(updatedNote)
                                isExpanded = !isExpanded
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown, null,
                                modifier = modifier
                                    .rotate(deg)
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                    ) {
                        Column {
                            note.text.lines().forEachIndexed { index, str ->
                                if (str.isNotBlank()) {
                                    val prefix = uiState.plansPrefix
                                    if (str.replace(" ", "").startsWith(prefix)) {
                                        val isDone = str.contains("$prefix*")
                                        val cleanText = if (isDone) {
                                            str.replaceFirst("$prefix*", "").trim()
                                        } else {
                                            str.replaceFirst(prefix, "").trim()
                                        }

                                        PlansUnit(
                                            isDone = isDone,
                                            text = cleanText,
                                            onClick = { viewModel.toggleNotePlanCompletion(note, index) }
                                        )
                                    } else {
                                        Text(
                                            text = str,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = note.title,
                        onValueChange = {
                            viewModel.onNoteTitleEditingInput(note, it)
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                    OutlinedTextField(
                        value = note.text,
                        onValueChange = {
                            viewModel.onNoteTextEditingInput(note, it)
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
                        Row {
                            if (isExpanded) {
                                val isChangeEnabled = !uiState.isEditing && !uiState.isAddingNote
                                if (isSystemInDarkTheme()) {
                                    AnimatedTonalButton(
                                        onClick = {
                                            isEditing = true
                                            viewModel.isEditing(true)
                                        },
                                        enabled = isChangeEnabled
                                    ) {
                                        Text(stringResource(R.string.button_change))
                                    }
                                }
                                else {
                                    AnimatedElevatedButton(
                                        onClick = {
                                            isEditing = true
                                            viewModel.isEditing(true)
                                        },
                                        enabled = isChangeEnabled,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(stringResource(R.string.button_change))
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.removeNote(note)
                                //isRemoving = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }
                    } else {
                        if (isSystemInDarkTheme()) {
                            AnimatedElevatedButton(
                                onClick = {
                                    isEditing = false
                                    viewModel.setNote(note)
                                    viewModel.isEditing(false)

                                    //viewModel.updateWidget(note)
                                }
                            ) {
                                Text(stringResource(R.string.button_finish_editing))
                            }
                        }
                        else {
                            AnimatedButton(
                                onClick = {
                                    isEditing = false
                                    viewModel.setNote(note)
                                    viewModel.isEditing(false)

                                    //viewModel.updateWidget(note)
                                }
                            ) {
                                Text(stringResource(R.string.button_finish_editing))
                            }
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.size(16.dp))
}

@Composable
fun EmptyNoteCard(uiState: NotesScreenUiState, viewModel: NotesScreenViewModel, modifier: Modifier) {
// modifier cause spring animation is impossible to use outside of lazy column, I guess
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .widthIn(max = 800.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 120.dp)
            .animateContentSize()
            .padding(8.dp)
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
                placeholder = { Text(stringResource(R.string.hint_note_title)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = modifier
                    .height(52.dp)
                    .defaultMinSize(minWidth = 240.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = uiState.tempNote,
                onValueChange = { viewModel.onNoteTextInput(it) },
                placeholder = { Text(stringResource(R.string.hint_note_text)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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