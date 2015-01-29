package com.teamparkin.mtdapp.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.teamparkin.mtdapp.MTDAppActivity;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContentProvider;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;

/**
 * This class is the service class to update the widget on pre-Honeycomb
 * devices. preHoneycomb can't use RemoteViewsService, so we must use this
 * instead.
 *
 * @author will
 */
public class WidgetViewsServicePreHoneycomb extends Service {
    @SuppressWarnings("unused")
    private static final String TAG = WidgetViewsServicePreHoneycomb.class
            .getSimpleName();
    private static final String SHARED_PREFS_NAME = "com.teamparkin.mtdapp.widget.MtdAppWidgetService.SharedPrefs";
    private static final String COUNT_KEY = "COUNT";
    private static final String KEY_INDEX_TO_DISPLAY = "indexToDisplay";
    RemoteViews updateViews;

    private RemoteViews getRemoteViews() {
        if (updateViews == null)
            updateViews = new RemoteViews(this.getPackageName(),
                    R.layout.widgetlayout);
        return updateViews;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return super.onStartCommand(intent, flags, startId);
        // Build Update
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(this,
                MtdAppWidgetProvider.class);

        // int widgetId =
        // intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        // AppWidgetManager.INVALID_APPWIDGET_ID);
        // //Action cases
        int indexToDisplay = intent.getIntExtra(KEY_INDEX_TO_DISPLAY, 0);
        String action = intent.getAction();
        if (action.equals(MtdAppWidgetProvider.BUILD_FAVLIST) ||
                action.equals(MtdAppWidgetProvider.SCROLLDOWN) ||
                action.equals(MtdAppWidgetProvider.SCROLLUP)) {
            // if the device is running gingerbread or lower, initialize that
            // specific layout.
            setupStaticButtonsPreHoneycomb(indexToDisplay);
            buildFavListPreHoneycomb(thisWidget, manager, indexToDisplay);
        } else if (action.equals(MtdAppWidgetProvider.REFRESH_ACTION)) {
            setupStaticButtonsPreHoneycomb(0);
            buildFavListPreHoneycomb(thisWidget, manager, 0);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // TODO finish doing these methods!!!!
    // Update builder method
    private void buildFavListPreHoneycomb(ComponentName thisWidget,
                                          AppWidgetManager manager, int indexToDisplay) {
        Intent displayStop;
        getRemoteViews().removeAllViews(R.id.widget_stoplist);
        Cursor cursor = getContentResolver().query(
                MyLocationContract.AbstractStops.CONTENT_URI,
                null,
                MyLocationContract.AbstractStops.FAVORITE + "=1",
                null,
                MyLocationContract.AbstractStops.TYPE + " ASC, " + MyLocationContract.AbstractStops.NAME + " ASC");
        int count = cursor.getCount();
        int pos = indexToDisplay % count;
        if (pos < 0)
            pos = indexToDisplay - pos;
        if (cursor.moveToFirst() && cursor.move(pos)) {
            // while
            RemoteViews insert = new RemoteViews(this.getPackageName(),
                    R.layout.stoplist_item_widget);
            displayStop = new Intent(this, MTDAppActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("uri", MyLocationContentProvider.getUriFromCursor(cursor));
            displayStop.putExtra("widget_bundle", bundle);
            PendingIntent PI = PendingIntent.getActivity(this, 0, displayStop,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            insert.setTextViewText(
                    R.id.widget_stop_name,
                    cursor.getString(cursor
                            .getColumnIndex(MyLocationContract.AbstractStops.NAME)));
            insert.setTextViewText(
                    R.id.widget_stop_code,
                    cursor.getString(cursor
                            .getColumnIndex(MyLocationContract.AbstractStops.SNIPPET)));
            insert.setOnClickPendingIntent(R.id.stoplist_item_widget, PI);

            getRemoteViews().setTextViewText(R.id.stoplist_item_count_widget, ""
                    + (pos + 1) + "/" + count);
            getRemoteViews().addView(R.id.widget_stoplist, insert);
            // final Intent onRefreshIntent = new Intent(context,
            // MtdAppWidgetProvider.class);
            // onRefreshIntent.setAction(MtdAppWidgetProvider.REFRESH)
        } else {
            RemoteViews insert = new RemoteViews(this.getPackageName(),
                    R.layout.textview);
            insert.setTextViewText(R.id.textview_text, "No favorites yet!");
            Intent openApp = new Intent(getApplicationContext(),
                    MTDAppActivity.class);
            PendingIntent PI = PendingIntent.getActivity(
                    getApplicationContext(), 0, openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            getRemoteViews().setOnClickPendingIntent(R.id.widget_stoplist, PI);
            getRemoteViews().addView(R.id.widget_stoplist, insert);
        }
        // setupStaticButtons(counter, updateViews);
        manager.updateAppWidget(thisWidget, getRemoteViews());
    }

    /**
     * The Pre-Honeycomb layout has extra buttons, so call this to initialize
     * their intents.
     *
     * @param indexToDisplay
     */
    private void setupStaticButtonsPreHoneycomb(int indexToDisplay) {
        RemoteViews views = getRemoteViews();

        // Have clicking on the header open the app
        Intent openApp = new Intent(this, MTDAppActivity.class);
        PendingIntent PI = PendingIntent.getActivity(getApplicationContext(),
                0, openApp, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_header, PI);

        // set onClick for previous button
        Intent onPrevIntent = new Intent(this,
                MtdAppWidgetProvider.class);
        onPrevIntent.putExtra(KEY_INDEX_TO_DISPLAY, indexToDisplay - 1);
        onPrevIntent.setAction(MtdAppWidgetProvider.SCROLLDOWN);
        PendingIntent onClickPrevingIntent = PendingIntent.getBroadcast(this,
                0, onPrevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.prev, onClickPrevingIntent);
        // set onClick for next button
        Intent onNextIntent = new Intent(this,
                MtdAppWidgetProvider.class);
        onNextIntent.setAction(MtdAppWidgetProvider.SCROLLUP);
        onNextIntent.putExtra(KEY_INDEX_TO_DISPLAY, indexToDisplay + 1);
        PendingIntent onClickNextingIntent = PendingIntent.getBroadcast(this,
                0, onNextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.next, onClickNextingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service
        return null;
    }
}
