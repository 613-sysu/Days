package com.example.jushalo.days;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class DaysWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.days_widget);
        views.setTextViewText(R.id.appwidget_text1, widgetText);
        views.setTextViewText(R.id.appwidget_title, widgetText);
        views.setTextViewText(R.id.appwidget_check, widgetText);
        views.setTextViewText(R.id.appwidget_days, widgetText);
        views.setTextViewText(R.id.remind_text, widgetText);
        views.setImageViewResource(R.id.imageView, R.mipmap.logo);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent clickInt = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, clickInt, 0);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.days_widget);
        rv.setOnClickPendingIntent(R.id.imageView, pi);
        appWidgetManager.updateAppWidget(appWidgetIds,rv);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.days_widget);

        if (intent.getAction().equals("com.example.jushalo.days.receiver")) {
            whichday day = (whichday) intent.getSerializableExtra("which");
            if (day != null) {
                rv.setTextViewText(R.id.appwidget_text1, "距离");
                String title = day.getTitle();
                String checkstr = day.getCheck_str();
                String howmanydays = day.getDays() + "天";
                String remind = day.getRemind_str();
                rv.setTextViewText(R.id.appwidget_title, title);
                rv.setTextViewText(R.id.appwidget_check, checkstr);
                rv.setTextViewText(R.id.appwidget_days, howmanydays);
                rv.setTextViewText(R.id.remind_text, remind);
            } else {
                rv.setTextViewText(R.id.appwidget_text1, "空空如也");
                rv.setTextViewText(R.id.appwidget_title, "");
                rv.setTextViewText(R.id.appwidget_check, "");
                rv.setTextViewText(R.id.appwidget_days, "");
                rv.setTextViewText(R.id.remind_text, "赶紧去添加记录吧！");
            }
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName conponentName = new ComponentName(context, DaysWidget.class);
            appWidgetManager.updateAppWidget(conponentName, rv);
        }
    }
}

