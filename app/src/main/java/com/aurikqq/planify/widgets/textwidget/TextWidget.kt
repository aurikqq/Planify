package com.aurikqq.planify.widgets.textwidget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
import androidx.glance.text.Text
import com.aurikqq.planify.PREFERENCES_NAME
import com.aurikqq.planify.Repository

class WidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget()
}

class Widget : GlanceAppWidget() {
    override val previewSizeMode = SizeMode.Responsive(
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

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            //WidgetContent()
            /* TODO
            replace with UI that is undependent from currentState etc.*/
        }
    }

    @Composable
    fun WidgetContent() {
        val context = LocalContext.current
        val intent = Intent(context, WidgetSetupActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val repo = Repository(context.getSharedPreferences(
            PREFERENCES_NAME, Context.MODE_PRIVATE), context)
        val title = repo.getTextWidgetTitle()
        val text = repo.getTextWidgetText()
        Log.d("Wigdet", "Widget info - title: $title, text: $text. title.isNotBlank = ${title.isNotBlank()}")

        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (title.isNotBlank()) {
                Column {
                    Text(title)
                    Text(text)
                }
            }
            else {
                Button(text = "Нажми для настройки", onClick = { context.startActivity(intent) })
            }

        }
    }
}
