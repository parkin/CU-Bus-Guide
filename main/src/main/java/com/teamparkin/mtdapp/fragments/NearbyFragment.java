package com.teamparkin.mtdapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache.Entry;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.adapters.MyExpandableLocationCursorAdapter;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.restadapters.GooglePlacesAPIAdapter;

import org.json.JSONObject;

import java.util.List;

public class NearbyFragment extends MyFragment implements
        LoaderCallbacks<Cursor> {
    private static final String TAG = NearbyFragment.class.getSimpleName();

    private static final int LOADER_ID = 88339913;

    private GooglePlacesAPIAdapter googlePlacesApi;

    private LocationManager locationManager;
    private Location lastLocation;
    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location
            // provider.

            // Only change locations when the lat and lon have changed
            if (!checkLocationsEqual(location, lastLocation)) {
                Button button = (Button) mView
                        .findViewById(R.id.nearbylist_update_button);
                button.setVisibility(View.VISIBLE);
            }
            lastLocation = location;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    private View mView;
    private MyFragmentsListener.OnLocationSelectedListener mLocationSelectedListener;

    private RequestQueue mRequestQueue;

    private MyExpandableLocationCursorAdapter mCursorAdapter;
    private View mFooterView;

    public NearbyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.nearbylist, container, false);

        ListView listView = (ListView) mView.findViewById(R.id.nearbylist_list);
        // Add footer. This must be called at least once before setAdapter for
        // some reason.
        mFooterView = inflater.inflate(R.layout.stoplist_footer_google, null);
        listView.addFooterView(mFooterView, null, false);

        mCursorAdapter = new MyExpandableLocationCursorAdapter(getActivity(),
                null, 0, R.layout.mylocationlistitem_card,
                R.id.mylocationlistitem_parent, R.id.mylocationlistitem_child);
        mCursorAdapter
                .setActionViewResId(R.id.mylocationlistitem_overflow_button);

        mCursorAdapter
                .setOnLocationSelectedListener(new MyFragmentsListener.OnLocationSelectedListener() {
                    @Override
                    public void onLocationSelected(Uri location) {
                        dispatchOnLocationSelected(location);
                    }
                });

        if (savedInstanceState == null) {
            // If no saved state, animate the entrance of the favorites list.
            SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
                    mCursorAdapter);
            swingBottomInAnimationAdapter.setAbsListView(listView);
            //swingBottomInAnimationAdapter.setAnimationDurationMillis(150);
            listView.setAdapter(swingBottomInAnimationAdapter);
        } else {
            // Do not animate when a saved state exists.
            listView.setAdapter(mCursorAdapter);
        }

        Button updateButton = (Button) mView
                .findViewById(R.id.nearbylist_update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button button = (Button) mView
                        .findViewById(R.id.nearbylist_update_button);
                button.setVisibility(View.GONE);
                Bundle bundle = new Bundle();
                bundle.putDouble("lat", lastLocation.getLatitude());
                bundle.putDouble("lon", lastLocation.getLongitude());
                getLoaderManager().restartLoader(LOADER_ID, bundle,
                        NearbyFragment.this);
                initializePlaces(lastLocation.getLatitude(),
                        lastLocation.getLongitude());
            }
        });
        updateButton.setVisibility(View.GONE);

        LinearLayout ll = (LinearLayout) mView.findViewById(R.id.nearby_bottom);
        setViewForMarginChange(ll);

        return mView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        googlePlacesApi = GooglePlacesAPIAdapter.getInstance(activity);

        mRequestQueue = Volley.newRequestQueue(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationService();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationService();
    }

    private void initializePlaces(final double lat, final double lon) {
        String urlPlace = googlePlacesApi.getSearchPlacesUrl(lat, lon, 500,
                null);

        // Check the cache first
        Entry cacheEntry = mRequestQueue.getCache().get(urlPlace);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            // Just return, don't need to add new things to the database.
            return;
        }

        setLoadingBoxVisibility(true, "Loading places...");

        JsonObjectRequest jsObjRequestPlace = new JsonObjectRequest(
                Request.Method.GET, urlPlace, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null && getActivity() != null)
                            googlePlacesApi.addPlacesToContentProvider(
                                    getActivity(), response);

                        setLoadingBoxVisibility(false, "");

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley error", error);
                setLoadingBoxVisibility(false, "");
            }
        }
        );
        jsObjRequestPlace.setShouldCache(true);
        jsObjRequestPlace.setTag(NearbyFragment.class.getName());
        mRequestQueue.add(jsObjRequestPlace);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRequestQueue.cancelAll(NearbyFragment.class.getName());
    }

    private void stopLocationService() {
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    private void startLocationService() {
        locationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        Location lastKnownLocation = null;
        lastKnownLocation = getMostRecentAndAccurateLocation();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        locationManager.requestLocationUpdates(
                locationManager.getBestProvider(criteria, true), 10000, 20,
                locationListener); // 10s refresh, only after changing by 20m

        if (lastKnownLocation == null) {
            Log.e(TAG, "Last known location null");
            setNoStopTextVisibility(true, "Waiting for location fix...");
        } else {
            // redo list if we are not at the current location or the data is
            // gone.
            if (!checkLocationsEqual(lastKnownLocation, lastLocation)
                    || mCursorAdapter.getCount() < 1) {
                lastLocation = lastKnownLocation;
                Bundle bundle = new Bundle();
                bundle.putDouble("lat", lastLocation.getLatitude());
                bundle.putDouble("lon", lastLocation.getLongitude());
                getLoaderManager().initLoader(LOADER_ID, bundle,
                        NearbyFragment.this);
                initializePlaces(lastLocation.getLatitude(),
                        lastLocation.getLongitude());
                setViewHeader(lastLocation.getLatitude(),
                        lastLocation.getLongitude());
            }
        }
    }

    /**
     * Returns the most accurate and timely previously detected location.
     * <p/>
     * see http://android-developers.blogspot.de/2011/06/deep-dive-into-location.html
     *
     * @return The most accurate and / or timely previously detected location.
     */
    private Location getMostRecentAndAccurateLocation() {
        List<String> matchingProviders = locationManager.getAllProviders();
        long minTime = 0;
        long bestTime = Long.MIN_VALUE;
        float bestAccuracy = Float.MAX_VALUE;
        Location bestResult = null;
        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (time < minTime &&
                        bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }
        return bestResult;
    }

    /**
     * Only compares latitudes and longitudes.
     *
     * @param loc1 First location to compare.
     * @param loc2 Second location to compare.
     * @return true if the two locations have the same lat/lng, false otherwise. Either location
     * being null returns false.
     */
    private boolean checkLocationsEqual(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null)
            return false;

        return loc1.getLatitude() == loc2.getLatitude() && loc1.getLongitude() == loc2.getLongitude();
    }

    private void setViewHeader(double lat, double lon) {
        TextView tv = (TextView) mView.findViewById(R.id.nearby_header_text);
        tv.setText("(" + lat + "," + lon + ")");
    }

    private void setNoStopTextVisibility(boolean b, String noStopText) {
        int visibility = (b) ? View.VISIBLE : View.GONE;
        TextView tv = (TextView) mView.findViewById(R.id.nearbylist_no_stops);
        tv.setVisibility(visibility);
        tv.setText(noStopText);

        int inverse = (b) ? View.INVISIBLE : View.VISIBLE;
        ListView list = (ListView) mView.findViewById(R.id.nearbylist_list);
        list.setVisibility(inverse);
    }

    private void setLoadingBoxVisibility(boolean visible, String text) {
        if (getActivity() == null) {
            return;
        }
        int visibility = (visible) ? View.VISIBLE : View.GONE;
        LinearLayout loadbox = (LinearLayout) mView
                .findViewById(R.id.nearbylist_loadingbox);
        loadbox.setVisibility(visibility);

        if (visible) {
            TextView tv = (TextView) mView.findViewById(R.id.TextView01);
            tv.setText(text);
        }
    }

    public void setOnLocationSelectedListener(
            MyFragmentsListener.OnLocationSelectedListener listener) {
        mLocationSelectedListener = listener;
    }

    private void dispatchOnLocationSelected(Uri location) {
        if (mLocationSelectedListener != null)
            mLocationSelectedListener.onLocationSelected(location);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOADER_ID:
                // This is called when a new Loader needs to be created. This
                // sample only has one Loader, so we don't care activity_about the ID.
                // First, pick the base URI to use depending on whether we are
                // currently filtering.
                Uri baseUri = MyLocationContract.MyLocation.CONTENT_URI;

                double lat = bundle.getDouble("lat");
                double lon = bundle.getDouble("lon");

                // calculate fudge factor for lat:
                // http://stackoverflow.com/questions/3695224/android-sqlite-getting-nearest-locations-with-latitude-and-longitude
                double fudge = Math.pow(Math.cos(Math.toRadians(lat)), 2);

                // Sort by distance squared, where the fudge factor takes into
                // account the latitude fudges Pythagorean theorm a little.
                String sortOrder = "((" + lat + " - "
                        + MyLocationContract.MyLocation.LAT + ") * (" + lat + " - "
                        + MyLocationContract.MyLocation.LAT + ") + (" + lon + " - "
                        + MyLocationContract.MyLocation.LON + ") * (" + lon + " - "
                        + MyLocationContract.MyLocation.LON + ") * " + fudge
                        + ") LIMIT 20";

                return new CursorLoader(getActivity(),
                        baseUri, null, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                setNoStopTextVisibility(cursor.getCount() < 1, "No search matches.");
                boolean needsGoogleFooter = false;
                if (cursor.moveToFirst()){
                    while(!cursor.isAfterLast()){
                        if (cursor.getInt(cursor.getColumnIndex(MyLocationContract.MyLocation
                                .TYPE)) == MyLocationContract.LocationTypeCode.GOOGLE_PLACE){
                            needsGoogleFooter = true;
                        }
                        cursor.moveToNext();
                    }
                }
                cursor.moveToFirst();
                mCursorAdapter.swapCursor(cursor);
                // check the cursor for GooglePlaces

                mFooterView.setVisibility(needsGoogleFooter ? View.VISIBLE : View.GONE);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID:
                mCursorAdapter.swapCursor(null);
                break;
        }
    }
}
