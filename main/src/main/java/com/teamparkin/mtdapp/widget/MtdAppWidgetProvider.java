package com.teamparkin.mtdapp.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.teamparkin.mtdapp.MTDAppActivity;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.AbstractStops;

/**
 * Our data observer just notifies an update for all weather widgets when it
 * detects a change.
 */
class FavoritesDataProviderObserver extends ContentObserver {
	private AppWidgetManager mAppWidgetManager;
	private Context mContext;
	private int mWidgetId;

	/**
	 * 
	 * @param context
	 * @param mgr
	 * @param cn
	 * @param h
	 * @param widgetId
	 *            Note we associate an observer with a particular widget.
	 */
	FavoritesDataProviderObserver(Context context, AppWidgetManager mgr,
			ComponentName cn, Handler h, int widgetId) {
		super(h);
		mWidgetId = widgetId;
		mContext = context;
		mAppWidgetManager = mgr;
	}

	@Override
	public void onChange(boolean selfChange) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			final Intent intentSend = new Intent(mContext,
					WidgetViewsServicePreHoneycomb.class);
			intentSend.setAction(MtdAppWidgetProvider.REFRESH_ACTION);
			// intentSend.putExtra(COUNTER_CODE, counter);
			// and sent it back to re-build with favlist with new counter
			mContext.startService(intentSend);
		} else {
			// The data has changed, so notify the widget that the collection
			// view
			// needs to be updated.
			// In response, the factory's onDataSetChanged() will be called
			// which
			// will requery the
			// cursor for the new data.
			mAppWidgetManager.notifyAppWidgetViewDataChanged(mWidgetId,
					R.id.widget_stoplist_lv);
		}
	}
}

public class MtdAppWidgetProvider extends AppWidgetProvider {
	@SuppressWarnings("unused")
	private static final String TAG = MtdAppWidgetProvider.class
			.getSimpleName();

	public static final String SCROLLDOWN = "com.teamparkin.mtdapp.widget.MtdAppWidgetProvider.SCROLLDOWN";
	public static final String SCROLLUP = "com.teamparkin.mtdapp.widget.MtdAppWidgetProvider.SCROLLUP";
	public static final String BUILD_FAVLIST = "com.teamparkin.mdtapp.widget.MtdAppWidgetProvider.BUILD_FAVLIST";
	public static final String STOP_ID = "com.teamparkin.mdtapp.widget.MtdAppWidgetProvider.STOP_ID";
	public static final String REFRESH_ACTION = "com.teamparkin.mtdapp.widget.MTDAppWidgetProvider.REFRESH_ACTION";
	public static final String CLICK_LIST_ACTION = "com.teamparkin.mtdapp.widget.MTDAppWidgetProvider.CLICK_LIST_ACTION";
	public static final String CLICK_UP_ACTION = "com.teamparkin.mtdapp.widget.MTDAppWidgetProvider.UP_ACTION";

	private static HandlerThread sWorkerThread;
	private static Handler sWorkerQueue;
	private static SparseArray<FavoritesDataProviderObserver> sDataObservers;

	public MtdAppWidgetProvider() {
		// Start the worker thread
		sWorkerThread = new HandlerThread("MtdAppWidgetProvider-worker");
		sWorkerThread.start();
		sWorkerQueue = new Handler(sWorkerThread.getLooper());
		sDataObservers = new SparseArray<FavoritesDataProviderObserver>();
	}

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action.equals(REFRESH_ACTION)) {
			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			final ComponentName cn = new ComponentName(context,
					MtdAppWidgetProvider.class);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				final Intent intentSend = new Intent(context,
						WidgetViewsServicePreHoneycomb.class);
				intentSend.setAction(action);
				// intentSend.putExtra(COUNTER_CODE, counter);
				// and sent it back to re-build with favlist with new counter
				context.startService(intentSend);
			} else
				mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
						R.id.widget_stoplist_lv); // TODO change so not just api
													// >=
													// 11
		} else if (action.equals(SCROLLUP) || action.equals(SCROLLDOWN)) {
			final Intent intentSend = new Intent(context,
					WidgetViewsServicePreHoneycomb.class);
            intentSend.putExtras(intent);
			intentSend.setAction(action);
			// intentSend.putExtra(COUNTER_CODE, counter);
			// and sent it back to re-build with favlist with new counter
			context.startService(intentSend);
		} else if (action.equals(CLICK_LIST_ACTION)) {
			int widgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

			String stopId = intent.getStringExtra(STOP_ID);

			// Unregister the content observer
			final ContentResolver r = context.getContentResolver();
			if (sDataObservers.get(widgetId) != null)
				r.unregisterContentObserver(sDataObservers.get(widgetId));

			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			RemoteViews layout = null;
			layout = buildDepartureListLayout(context, widgetId, stopId);
			mgr.updateAppWidget(widgetId, layout);
		} else if (action.equals(CLICK_UP_ACTION)) {
			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			int widgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

			RemoteViews layout = buildFavList(context, widgetId);
			mgr.updateAppWidget(widgetId, layout);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// in case of multiple widget instances, loop for all present Ids
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			RemoteViews layout = null;
			// if the device is running gingerbread or lower, initialize that
			// specific layout.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				// If we are below honeycomb, just start the
				// MtdAppWidgetService, that's what it's meant for.
				// intent starts the WidgetService, which handles all of the
				// RemoteView
				// objects. BUILD_FAVLIST action leads to the favorite stops
				// list
				// and is called every automatic update (currently 2 hours) and
				// when
				// the
				// widget is first run
				Intent intent = new Intent(context,
						WidgetViewsServicePreHoneycomb.class);
				intent.setAction(BUILD_FAVLIST);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetIds[i]);
				context.startService(intent);
			} else {
				// By default just load the favorites list on a widget update.
				layout = buildFavList(context, appWidgetIds[i]);
				appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
			}
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	private RemoteViews buildDepartureListLayout(Context context, int widgetId,
			String stopId) {
		// Specify the service to provide data for the collection widget.
		// Note that we need to
		// embed the appWidgetId via the data otherwise it will be ignored.
		final Intent intent = new Intent(context, WidgetViewsService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		intent.putExtra(STOP_ID, stopId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widgetlayout_departures);

		// Set the empty view to be displayed if the collection is empty. It
		// must be a sibling
		// view of the collection view.
		updateViews.setEmptyView(R.id.widget_stoplist_lv, R.id.empty_view);

		updateViews.setTextViewText(R.id.empty_view, "No upcoming departures.");

		setupStaticButtons(context, updateViews, widgetId);

		// Set the text of the banner of the widget to the name of the stop.
		Cursor c = context.getContentResolver().query(
				AbstractStops.CONTENT_URI,
				new String[] { MyLocationContract.AbstractStops.NAME },
				MyLocationContract.AbstractStops.ID + " = ?",
				new String[] { stopId }, null);
		String name = null;
		if (c.moveToFirst()) {
			name = c.getString(c
					.getColumnIndex(MyLocationContract.AbstractStops.NAME));
		} else {
			name = stopId;
		}
		if (c != null) {
			c.close();
		}
		updateViews.setTextViewText(R.id.widget_banner_text, name);

		// Bind the click intent for the up button on the widget
		final Intent clickUpIntent = new Intent(context,
				MtdAppWidgetProvider.class);
		clickUpIntent.setAction(MtdAppWidgetProvider.CLICK_UP_ACTION);
		clickUpIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		final PendingIntent clickUpPendingIntent = PendingIntent.getBroadcast(
				context, widgetId, clickUpIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.widget_button_topleft,
				clickUpPendingIntent);

		// Bind the click intent for the refresh button
		final Intent refreshIntent = new Intent(context,
				MtdAppWidgetProvider.class);
		refreshIntent.setAction(MtdAppWidgetProvider.REFRESH_ACTION);
		refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
				context, widgetId, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.widget_button_topright,
				refreshPendingIntent);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			updateViews.setRemoteAdapter(widgetId, R.id.widget_stoplist_lv,
					intent);
		} else
			updateViews.setRemoteAdapter(R.id.widget_stoplist_lv, intent);

		return updateViews;
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	private RemoteViews buildFavList(Context context, int widgetId) {

		// Register for external updates to the data to trigger an update of the
		// widget. When using
		// content providers, the data is often updated via a background
		// service, or in response to
		// user interaction in the main app. To ensure that the widget always
		// reflects the current
		// state of the data, we must listen for changes and update ourselves
		// accordingly.
		final ContentResolver r = context.getContentResolver();
		final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		if (sDataObservers.get(widgetId) == null) {
			final ComponentName cn = new ComponentName(context,
					MtdAppWidgetProvider.class);
			FavoritesDataProviderObserver sDataObserver = new FavoritesDataProviderObserver(
					context, mgr, cn, sWorkerQueue, widgetId);
			sDataObservers.put(widgetId, sDataObserver);
		}
		r.registerContentObserver(AbstractStops.CONTENT_URI, true,
				sDataObservers.get(widgetId));

		// Notify the dataset changed here, the widget component might already
		// be active.
		mgr.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_stoplist_lv);

		// Specify the service to provide data for the collection widget.
		// Note that we need to
		// embed the appWidgetId via the data otherwise it will be ignored.
		final Intent intent = new Intent(context, WidgetViewsService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widgetlayout);

		// Set the empty view to be displayed if the collection is empty. It
		// must be a sibling
		// view of the collection view.
		updateViews.setEmptyView(R.id.widget_stoplist_lv, R.id.empty_view);

		setupStaticButtons(context, updateViews, widgetId);

		// Bind a click listener template for the contents of the stop
		// list. Note that we
		// need to update the intent's data if we set an extra, since the
		// extras will be
		// ignored otherwise.
		final Intent onClickIntent = new Intent(context,
				MtdAppWidgetProvider.class);
		onClickIntent.setAction(MtdAppWidgetProvider.CLICK_LIST_ACTION);
		onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		onClickIntent.setData(Uri.parse(onClickIntent
				.toUri(Intent.URI_INTENT_SCHEME)));
		final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(
				context, widgetId, onClickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setPendingIntentTemplate(R.id.widget_stoplist_lv,
				onClickPendingIntent);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			updateViews.setRemoteAdapter(widgetId, R.id.widget_stoplist_lv,
					intent);
		} else
			updateViews.setRemoteAdapter(R.id.widget_stoplist_lv, intent);

		return updateViews;
	}

	private void setupStaticButtons(Context context, RemoteViews remoteViews,
			int widgetId) {
		// Have clicking on the header open the app
		Intent openApp = new Intent(context, MTDAppActivity.class);
		PendingIntent PI = PendingIntent.getActivity(context, widgetId,
				openApp, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_header, PI);

	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);

		// Make sure to unregister the observers when we delete a widget.
		final ContentResolver r = context.getContentResolver();
		for (int widgetId : appWidgetIds) {
			if (sDataObservers.get(widgetId) != null) {
				r.unregisterContentObserver(sDataObservers.get(widgetId));
				sDataObservers.remove(widgetId);
			}
		}
	}

}
