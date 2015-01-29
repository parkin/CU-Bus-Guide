package com.teamparkin.mtdapp.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.StopPoints;
import com.teamparkin.mtdapp.dataclasses.Departure;
import com.teamparkin.mtdapp.restadapters.MTDAPIAdapter;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetViewsService extends RemoteViewsService {
    @SuppressWarnings("unused")
    private static final String TAG = WidgetViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i(TAG, "onGetViewFactory");
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                0);
        if (intent.hasExtra(MtdAppWidgetProvider.STOP_ID)) {
            String stopId = intent.getStringExtra(MtdAppWidgetProvider.STOP_ID);
            return new WidgetDepartureListProvider(this, widgetId, stopId);

        } else {
            return new WidgetStopListProvider(this);
        }
    }

}

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class WidgetDepartureListProvider implements RemoteViewsFactory {
    private static final String TAG = WidgetDepartureListProvider.class
            .getSimpleName();

    private Map<String, ArrayList<Departure>> departures;
    private Context mContext;
    private RequestQueue mRequestQueue;
    private String mStopId;
    private boolean changeCameFromSelf = false;
    private boolean isLoading = false;
    private MTDAPIAdapter mMtdApiAdapter;

    private String lastUpdated = "";

    private Cursor mCursor;

    private int mWidgetId;

    public WidgetDepartureListProvider(Context context, int widgetId,
                                       String stopId) {
        mWidgetId = widgetId;
        mContext = context;
        mStopId = stopId;
        mRequestQueue = Volley.newRequestQueue(context);
        mMtdApiAdapter = MTDAPIAdapter.getInstance(context);
    }

    @Override
    public int getCount() {
        if (isLoading)
            return 1;
        if (departures != null) {
            int count = 0;
            for (String string : departures.keySet()) {
                count++; // 1 view for the stoppoint
                count += departures.get(string).size(); // this many
                // departure views
                // per stoppoint.
            }
            // +1 for the last updated row. +1 for no departures coming, if needed.
            return count + 1 + ((departures.size() < 1) ? 1 : 0);
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // If we are loading, just return the loading view.
        if (isLoading) {
            return new RemoteViews(
                    mContext.getPackageName(), R.layout.widget_loading_row);
        }
        // if position is zero, this is the time text
        if (position == 0) {
            RemoteViews remoteViews = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.widget_lastupdated_time_row);
            remoteViews.setTextViewText(R.id.widget_last_update_time,
                    "Last updated " + lastUpdated);
            return remoteViews;
        }

        if (position == 1 && (departures == null || departures.size() < 1)) {
            RemoteViews remoteViews = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.widget_lastupdated_time_row);
            remoteViews.setTextViewText(R.id.widget_last_update_time,
                    "No upcoming departures.");
            return remoteViews;
        }

        Object item = getItemAt(position - 1); // -1 because the first row is
        // the last updated time text.

        RemoteViews remoteView = null;

        if (item instanceof Integer) {
            remoteView = new RemoteViews(mContext.getPackageName(),
                    R.layout.stoplist_item_widget);

            if (mCursor.moveToPosition((Integer) item)) {

                String name = mCursor.getString(mCursor
                        .getColumnIndex(MyLocationContract.StopPoints.NAME));
                String code = mCursor.getString(mCursor
                        .getColumnIndex(MyLocationContract.StopPoints.CODE));

                boolean isFav = mCursor.getInt(mCursor.getColumnIndex(
                        StopPoints.FAVORITE)) == 1;

                remoteView.setTextViewText(R.id.widget_stop_name, name);
                remoteView.setTextViewText(R.id.widget_stop_code, code);

                remoteView.setViewVisibility(R.id.widget_stop_star,
                        isFav ? View.VISIBLE : View.GONE);
            }

        } else if (item instanceof Departure) {
            remoteView = new RemoteViews(mContext.getPackageName(),
                    R.layout.displaystop_item);

            Departure departure = (Departure) item;
            String headsign[] = departure.getHeadsign().split(" ");

            int mins = departure.getExpected_mins();
            remoteView.setTextViewText(R.id.displaystopitem_id, headsign[0]);

            String nameText = headsign[1];
            for (int i = 2; i < headsign.length; i++) {
                nameText += " " + headsign[i];
            }
            remoteView.setTextViewText(R.id.displaystopitem_name, nameText);

            String headSignText = "";
            if (departure.getTrip() != null)
                headSignText = departure.getTrip().getTrip_headsign();
            remoteView.setTextViewText(R.id.displaystopitem_tripheadsign,
                    headSignText);

            String minutesNumber;
            String minutesText = "";
            if (mins < 2) {
                if (mins < 1) {
                    minutesNumber = "due";
                } else {
                    minutesNumber = "" + mins;
                    minutesText = " min";
                }
            } else {
                minutesNumber = "" + mins;
                minutesText = " min";
            }
            remoteView.setTextViewText(R.id.displaystopitem_minutes_numberr,
                    minutesNumber);
            remoteView.setTextViewText(R.id.displaystopitem_minutes_text,
                    minutesText);

            remoteView.setViewVisibility(R.id.istop,
                    departure.isIstop() ? View.VISIBLE : View.GONE);
        }

        return remoteView;
    }

    private Object getItemAt(int position) {
        int count = 0;
        int stoppointCount = 0;
        for (String string : departures.keySet()) {
            count++; // 1 view for the stoppoint
            if (position < count) {
                return stoppointCount;
            }
            int currPos = count;
            count += departures.get(string).size(); // this many
            // departure views
            // per stoppoint.
            if (position < count)
                return departures.get(string).get(position - currPos);
            stoppointCount++;
        }
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // Departure view + stoppoint view + last updated text + loading view
        return 4;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called
        // immediately after
        // onCreate(), we do nothing here.
    }

    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged:: changeCameFromSelf: "
                + changeCameFromSelf);

        // Revert back to our process' identity so we can work with our
        // content provider
        // http://stackoverflow.com/questions/9497270/widget-with-content-provider-impossible-to-use-readpermission
        final long identityToken = Binder.clearCallingIdentity();

        if (!changeCameFromSelf) {
            if (departures != null) {
                departures.clear();
                departures = null;
            }
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

            // We first put the loading view, say the change came from us.
            isLoading = true;

            lastUpdated = "";
            String url = mMtdApiAdapter.getDeparturesByStopUrl(mStopId);
            JsonObjectRequest jsObjReques = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            departures = mMtdApiAdapter
                                    .parseDeparturesByStopFromJSON(response);

                            // Tell the widget provider to update the last
                            // updated time.
                            final Date currentTime = new Date();

                            SimpleDateFormat curFormater = new SimpleDateFormat(
                                    "EEE, MMM d, yyyy hh:mm:ss a");

                            lastUpdated = curFormater.format(currentTime);

                            // check whether stoppoints are favorited
                            if (departures != null && departures.size() > 0) {
                                String selection = "";
                                int count = departures.size();
                                String[] selectArgs = new String[count];

                                int i = 0;
                                for (String id : departures.keySet()) {
                                    selection += MyLocationContract.StopPoints.ID
                                            + " = ?";
                                    if (i < count - 1)
                                        selection += " OR ";
                                    selectArgs[i] = id;
                                    i++;
                                }
                                mCursor = mContext.getContentResolver().query(
                                        StopPoints.CONTENT_URI, null,
                                        selection, selectArgs, null);
                            }
                            isLoading = false;
                            changeCameFromSelf = true;
                            AppWidgetManager mgr = AppWidgetManager
                                    .getInstance(mContext);
                            if (mgr != null) {
                                mgr.notifyAppWidgetViewDataChanged(mWidgetId,
                                        R.id.widget_stoplist_lv);
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    isLoading = false;
                    changeCameFromSelf = true;
                    AppWidgetManager mgr = AppWidgetManager
                            .getInstance(mContext);
                    Log.i(TAG, "widget onResponse");
                    if (mgr != null) {
                        mgr.notifyAppWidgetViewDataChanged(mWidgetId,
                                R.id.widget_stoplist_lv);
                    }
                }
            }
            );
            jsObjReques.setShouldCache(false);
            mRequestQueue.add(jsObjReques);

        } else {
            changeCameFromSelf = false;
        }

        // Restore the identity - not sure if it's needed since we're going
        // to return right here, but it just *seems* cleaner
        // http://stackoverflow.com/questions/9497270/widget-with-content-provider-impossible-to-use-readpermission
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null)
            mCursor.close();
    }

}

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class WidgetStopListProvider implements RemoteViewsFactory {
    @SuppressWarnings("unused")
    private static final String TAG = WidgetViewsService.class.getSimpleName();

    private Context context;
    private Cursor mCursor;

    public WidgetStopListProvider(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteView = new RemoteViews(context.getPackageName(),
                R.layout.stoplist_item_widget);

        String name = "Unknown";
        String code = "Unknown";
        String id = "";

        if (mCursor.moveToPosition(position)) {
            name = mCursor.getString(mCursor
                    .getColumnIndex(MyLocationContract.AbstractStops.NAME));
            code = mCursor.getString(mCursor
                    .getColumnIndex(MyLocationContract.AbstractStops.CODE));
            id = mCursor.getString(mCursor
                    .getColumnIndex(MyLocationContract.AbstractStops.ID));
        }

        remoteView.setTextViewText(R.id.widget_stop_name, name);
        remoteView.setTextViewText(R.id.widget_stop_code, code);

        // Set the click intent so that we can handle it and show a toast
        // message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putString(MtdAppWidgetProvider.STOP_ID, id);
        fillInIntent.putExtras(extras);
        remoteView.setOnClickFillInIntent(R.id.stoplist_item_widget,
                fillInIntent);

        return remoteView;
    }

    @Override
    public int getViewTypeCount() {
        // return the number of types of layouts that will inhabit the listview.
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called
        // immediately after
        // onCreate(), we do nothing here.
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null)
            mCursor.close();

        // Revert back to our process' identity so we can work with our
        // content provider
        // http://stackoverflow.com/questions/9497270/widget-with-content-provider-impossible-to-use-readpermission
        final long identityToken = Binder.clearCallingIdentity();

        mCursor = context.getContentResolver().query(
                MyLocationContract.AbstractStops.CONTENT_URI, null,
                MyLocationContract.AbstractStops.FAVORITE + "=1", null,
                MyLocationContract.AbstractStops.NAME + " ASC");

        // Restore the identity - not sure if it's needed since we're going
        // to return right here, but it just *seems* cleaner
        // http://stackoverflow.com/questions/9497270/widget-with-content-provider-impossible-to-use-readpermission
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
}
