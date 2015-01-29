package com.teamparkin.mtdapp.contentproviders;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Cache.Entry;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.restadapters.GooglePlacesAPIAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class StopsProvider extends ContentProvider {
    public static final String ACTION_CLICK = "com.teamparkin.mtdapp.intent.action_view_search_click";
    public static final String[] DEFAULT_CURSOR_COLUMNS = new String[]{"_ID",
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_ICON_2,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA};
    public static final String AUTHORITY = "com.teamparkin.mtdapp.contentproviders.StopsProvider";
    @SuppressWarnings("unused")
    private static final String TAG = StopsProvider.class.getSimpleName();
    private static final String BASE_PATH_SUGGEST = SearchManager.SUGGEST_URI_PATH_QUERY;
    /**
     * Uri for searching just the stopsDatabase
     */
    public static final Uri CONTENT_URI_SUGGEST = Uri.parse("content://"
            + AUTHORITY + "/" + BASE_PATH_SUGGEST);
    // used for the UriMacher
    private static final int SUGGEST = 1000;
    private static final int SUGGEST_QUERY = 1001;
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_SUGGEST, SUGGEST);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_SUGGEST + "/*", SUGGEST_QUERY);
    }

    private static final int SUGGESTION_THRESHOLD = 3; // set SuggestThreshold
    /**
     * Keeps a reference to the last query asked for.
     */
    private static String lastQuery = "";
    private GooglePlacesAPIAdapter mGPlacesAdapter;
    // here instead of in
    // xml so we can show
    // favorites as
    // suggestions on empty
    // search view
    private RequestQueue mRequestQueue;

    private Context mContext;

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override
    public String getType(Uri arg0) {
        return null;
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    @Override
    public boolean onCreate() {
        mGPlacesAdapter = GooglePlacesAPIAdapter.getInstance(this.getContext());
        return true;
    }

    @Override
    public Cursor query(final Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "search uri: " + uri.toString());

        // Defining a cursor object with columns id, SUGGEST_COLUMN_TEXT_1,
        // SUGGEST_COLUMN_INTENT_EXTRA_DATA
        MatrixCursor mc = new MatrixCursor(DEFAULT_CURSOR_COLUMNS);
        // make sure that potential listeners are getting notified
        mc.setNotificationUri(getContext().getContentResolver(), uri);

        List<Cursor> cursors = new ArrayList<Cursor>();

        String queriedName = uri.getLastPathSegment();
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case SUGGEST:
                // If we haven't appended a querey, only display the favorites.
                Cursor c = getContext().getContentResolver().query(
                        MyLocationContract.MyLocation.CONTENT_URI,
                        null,
                        MyLocationContract.MyLocation.FAVORITE + " = 1",
                        null,
                        MyLocationContract.MyLocation.TYPE + " ASC, "
                                + MyLocationContract.MyLocation.NAME + " ASC"
                );
                cursors.add(c);
                break;
            case SUGGEST_QUERY:
                // make sure the queried name is long enough, if not just return
                // nothing.
                if (queriedName.length() < SUGGESTION_THRESHOLD)
                    return mc;
                lastQuery = queriedName;

                // Only search AbstractStops here! should change if you want to
                // search user places, other types of locations.
                // Note GooglePlaces is searched on its own below.
                String sort = MyLocationContract.AbstractStops.FAVORITE + " DESC, "
                        + MyLocationContract.AbstractStops.NAME + " COLLATE NOCASE";

                String select = "(";

                // only search Stops by name, not StopPoints
                select += MyLocationContract.AbstractStops.TYPE + "=" + MyLocationContract
                        .LocationTypeCode.STOP;

                // Split the query into words so words don't have to be in order.
                String[] words = queriedName.split(" ");
                for (String word : words) {
                    select += " AND " + MyLocationContract.AbstractStops.NAME + " LIKE \"%"
                            + word + "%\"";
                }
                select += ")";

                // Also search for the code. Example code is mtd0100
                if (queriedName.length() > 3) {
                    select += " OR (" + MyLocationContract.AbstractStops.CODE
                            + " LIKE \"%" + queriedName + "%\")";
                }

                Cursor stopsCursor = getContext().getContentResolver().query(
                        MyLocationContract.AbstractStops.CONTENT_URI, null, select,
                        null, sort);
                cursors.add(stopsCursor);

                List<String> ids = check(uri, queriedName);
                if (ids != null && ids.size() > 0) {
                    String inString = "(";
                    int i = 0;
                    for (String string : ids) {
                        if (i > 0)
                            inString += ", ";
                        inString += "'" + string + "'";
                        i++;
                    }
                    inString += ")";

                    // only get the places that are in the results of the cached
                    // google place search
                    Cursor placeCursor = getContext().getContentResolver().query(
                            MyLocationContract.GooglePlaces.CONTENT_URI, null,
                            MyLocationContract.GooglePlaces.ID + " IN " + inString,
                            null, sort);
                    cursors.add(placeCursor);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        int i = 0;

        for (Cursor cursor : cursors) {
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int type = cursor
                            .getInt(cursor
                                    .getColumnIndex(MyLocationContract.MyLocation.TYPE));
                    mc.addRow(new Object[]{
                            i++,
                            cursor.getString(cursor
                                    .getColumnIndex(MyLocationContract.MyLocation.NAME)),
                            cursor.getString(cursor
                                    .getColumnIndex(MyLocationContract.MyLocation.SNIPPET)),
                            (type == MyLocationContract.LocationTypeCode.STOP || type == MyLocationContract.LocationTypeCode.STOPPOINT) ? R.drawable.bus_icon
                                    : R.drawable.place_icon,
                            (cursor.getInt(cursor
                                    .getColumnIndex(MyLocationContract.MyLocation.FAVORITE)) == 1) ? android.R.drawable.star_big_on
                                    : null, ACTION_CLICK,
                            MyLocationContentProvider.getUriFromCursor(cursor)});
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }

        return mc;
    }

    /**
     * Checks the volley cache for the places search and returns a list of
     * places if it was cached. If not, starts a new volley request and returns
     * null.
     *
     * @param uri
     * @param queriedName
     * @return
     */
    private List<String> check(final Uri uri, final String queriedName) {
        boolean needsNetworkRequest = true;
        // places = mGPlacesAdapter.searchPlaces(selectionArgs[0]);
        String searchPlacesUrl = mGPlacesAdapter
                .getSearchPlacesUrl(queriedName);

        // Check the cache first
        Entry cacheEntry = mRequestQueue.getCache().get(searchPlacesUrl);
        Log.i(TAG, "volley cache entry null ? " + (cacheEntry == null));
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            Log.i(TAG,
                    "volley cache entry refreshNeeded ? "
                            + cacheEntry.refreshNeeded() + ", expired? "
                            + cacheEntry.isExpired()
            );
            JSONObject ob = null;

            try {
                // parse the cache entry to json string then to a json
                // object
                ob = new JSONObject(new String(cacheEntry.data,
                        HttpHeaderParser
                                .parseCharset(cacheEntry.responseHeaders)
                ));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (ob != null) {
                needsNetworkRequest = false;
                List<String> ids = new ArrayList<String>();
                try {
                    JSONArray results = ob.getJSONArray("results");
                    JSONObject result;
                    for (int i = 0; i < results.length(); i++) {
                        // TODO add all the google place attributes.
                        result = results.getJSONObject(i);
                        ids.add(result.getString("id"));
                    }

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }
                return ids;
            }

        }
        if (needsNetworkRequest) {
            JsonObjectRequest jsObjReques = new JsonObjectRequest(
                    Request.Method.GET, searchPlacesUrl, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "lastQuery: " + lastQuery
                                    + ", queriedName: " + queriedName);
                            // Add the results to the place database.

                            mGPlacesAdapter.addPlacesToContentProvider(
                                    mContext, response);

                            // only notify listeners activity_about this particular
                            // search if 1) the last query is
                            // the querey that we searched here, 2) the queried
                            // name has results (Note: response will have
                            // results even if ZERO_RESULTS)
                            if (lastQuery.equals(queriedName)
                                    && response.has("results")) {
                                // notify listeners that we have a response
                                getContext().getContentResolver().notifyChange(
                                        uri, null);
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Volley error", error);
                }
            }
            );
            jsObjReques.setShouldCache(true);
            mRequestQueue.add(jsObjReques);
        }
        return null;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }

}
