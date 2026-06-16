package com.heathen.ialemus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.heathen.ialemus.MainActivity
import com.heathen.ialemus.R

class IalemusPlaybackWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildRemoteViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, IalemusPlaybackWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.heathen.ialemus.widget.ACTION_REFRESH"

        fun buildRemoteViews(context: Context): RemoteViews {
            val snapshot = WidgetStateStore(context).read()
            val views = RemoteViews(context.packageName, R.layout.ialemus_playback_widget)
            views.setTextViewText(R.id.widget_brand, "IALEMUS")
            views.setTextViewText(R.id.widget_status, if (snapshot.isPlaying) "PLAYING" else "STANDBY")
            views.setTextViewText(R.id.widget_title, snapshot.title)
            views.setTextViewText(R.id.widget_artist, snapshot.artist)
            views.setTextViewText(
                R.id.widget_transport,
                if (snapshot.isPlaying) "▮▮ PAUSE" else "▶ PLAY",
            )

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)
            views.setOnClickPendingIntent(R.id.widget_transport, pending)
            return views
        }

        fun refreshAll(context: Context) {
            val intent = Intent(context, IalemusPlaybackWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
