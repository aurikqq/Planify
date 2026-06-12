package com.aurikqq.planify.widgets.textwidget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.Repository

class WidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget()
}

class Widget : GlanceAppWidget() {
    val previewSizeMode = SizeMode.Responsive(
        setOf(
            DpSize(245.dp, 115.dp), // 4x2
            DpSize(260.dp, 180.dp) // medium width, height w/ header
        )
    )

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(130.dp, 306.dp),
            DpSize(624.dp, 276.dp)
        )
    )

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    val noteId = stringPreferencesKey("noteId")
    val noteTitle = stringPreferencesKey("noteTitle")
    val noteText = stringPreferencesKey("noteText")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            //WidgetContent()
        }
    }

    suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            //WidgetContent()
            /* TODO
            replace with UI that is undependent from currentState etc.*/
        }
    }

    @Composable
    fun WidgetContent(prefs: Preferences) {
        val context = LocalContext.current
        val intent = Intent(context, WidgetSetupActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val noteId = prefs[noteId].orEmpty()
        val noteTitle = prefs[noteText].orEmpty()
        val noteText = prefs[noteText].orEmpty()

        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (noteTitle.isNotBlank()) {
                Column {
                    Text(noteTitle)
                    Text(noteText)
                }
            }
            else {
                Button(text = "Нажми для настройки", onClick = { context.startActivity(intent) })
            }

        }
    }
}
