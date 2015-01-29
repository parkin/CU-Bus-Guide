package com.teamparkin.mtdapp.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.teamparkin.mtdapp.MTDAppActivity;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.StopsProvider;
import com.teamparkin.mtdapp.dataclasses.Itinerary;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.dataclasses.Stop;
import com.teamparkin.mtdapp.dataclasses.TripPlanParameters;
import com.teamparkin.mtdapp.listeners.OnOriginDestinationChangedListener;
import com.teamparkin.mtdapp.restadapters.MTDAPIAdapter;
import com.teamparkin.mtdapp.util.Util;
import com.teamparkin.mtdapp.views.ItineraryView;
import com.teamparkin.mtdapp.views.TripPlannerHead;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Fragment for displaying trip possibilities from CUMTD.
 *
 * @author will
 */
public class TripPlanViewerFragment extends SlideupHeadFragment implements
        LoaderCallbacks<Cursor> {
    private static final String TAG = TripPlanViewerFragment.class
            .getSimpleName();
    private static final int LOADER_ORIGIN_ID = 3819;
    private static final int LOADER_DEST_ID = 3820;
    private Activity mActivity;
    private MTDAPIAdapter mMtdApiAdapter;
    private boolean isInitializingHeadView;

    private ArrayList<Itinerary> mItineraries;

    private String mCurrDestSearchViewFilter;
    private String mCurOrigSearchViewFilter;
    private View mView;
    private MySimpleArrayAdapter mListAdapter;
    private OnItineraryClickListener mItineraryClickListener;
    private TripPlanParameters mTripParams;
    private OnOriginDestinationChangedListener mOriginDestinationChangedListener;
    private TripPlannerHead mTripPlanHead;
    private ListView mListView;
    private RequestQueue mRequestQueue;

    public TripPlanViewerFragment() {

    }

    public void setTripPlanParams(TripPlanParameters params) {
        mTripParams = (TripPlanParameters) Util.copyParcelable(params);
        initializeItinerariesList(params);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        getLoaderManager().initLoader(LOADER_ORIGIN_ID, null, this);
        getLoaderManager().initLoader(LOADER_DEST_ID, null, this);

        mRequestQueue = Volley.newRequestQueue(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.tripplanviewer, container, false);

        mListView = (ListView) mView.findViewById(R.id.tripplanviewer_listview);
        if (mListAdapter == null)
            mListAdapter = new MySimpleArrayAdapter(getActivity());

        mTripPlanHead = (TripPlannerHead) mView
                .findViewById(R.id.tripplanviewer_head);
        mTripPlanHead.setFragmentActivity(getActivity());

        mTripPlanHead
                .setOnOriginDestinationChangedListener(new OnOriginDestinationChangedListener() {
                    @Override
                    public void onOriginSet(MyLocation origin) {
                        dispatchOnOriginSet(origin);
                    }

                    @Override
                    public void onOriginRemoved() {
                        // Clear the list when an origin/departure is removed.
                        if (!isInitializingHeadView) {
                            clearListView();
                        }
                        dispatchOnOriginRemoved();
                    }

                    @Override
                    public void onDestinationSet(MyLocation destination) {
                        dispatchOnDestinationSet(destination);
                    }

                    @Override
                    public void onDestinationRemoved() {
                        // Clear the list when an origin/departure is removed.
                        if (!isInitializingHeadView) {
                            clearListView();
                        }
                        dispatchOnDestinationRemoved();
                    }
                });

        mTripPlanHead
                .setOnTripParametersUpdateListener(new TripPlannerHead.OnTripParametersUpdateListener() {
                    @Override
                    public void onTripRequested(TripPlanParameters params) {
                        if (!isInitializingHeadView) {
                            isBodySet = false;
                            if (((MTDAppActivity) getActivity()).isPanelOpen()) {
                                isBodySet = false;
                                fillBody(params);
                            }
                        }
                    }

                    @Override
                    public void onTripParametersUpdated(
                            TripPlanParameters parameters) {
                        if (!isInitializingHeadView)
                            mTripParams = (TripPlanParameters) Util
                                    .copyParcelable(parameters);
                    }
                });

        mTripPlanHead
                .setOnAutoCompleteTextChange(new TripPlannerHead.OnAutoCompleteTextChange() {

                    @Override
                    public void onOriginTextChanged(String newText) {
                        String newFilter = !TextUtils.isEmpty(newText) ? newText
                                : null;
                        // Don't do anything if the filter hasn't actually
                        // changed.
                        // Prevents restarting the loader when restoring state.
                        if (mCurOrigSearchViewFilter == null
                                && newFilter == null) {
                            return;
                        }
                        if (mCurOrigSearchViewFilter != null
                                && mCurOrigSearchViewFilter.equals(newFilter)) {
                            return;
                        }
                        mCurOrigSearchViewFilter = newFilter;

                        getLoaderManager().restartLoader(LOADER_ORIGIN_ID,
                                null, TripPlanViewerFragment.this);
                    }

                    @Override
                    public void onDestinationTextChanged(String newText) {
                        String newFilter = !TextUtils.isEmpty(newText) ? newText
                                : null;
                        // Don't do anything if the filter hasn't actually
                        // changed.
                        // Prevents restarting the loader when restoring state.
                        if (mCurrDestSearchViewFilter == null
                                && newFilter == null) {
                            return;
                        }
                        if (mCurrDestSearchViewFilter != null
                                && mCurrDestSearchViewFilter.equals(newFilter)) {
                            return;
                        }
                        mCurrDestSearchViewFilter = newFilter;

                        getLoaderManager().restartLoader(LOADER_DEST_ID, null,
                                TripPlanViewerFragment.this);
                    }
                });

        if (savedInstanceState != null) {
            mItineraries = savedInstanceState
                    .getParcelableArrayList("itineraries");
            mTripParams = savedInstanceState.getParcelable("tripParams");
        }
        if (mTripParams == null) {
            mTripParams = new TripPlanParameters(getActivity(),
                    R.array.walk_distance, R.array.plan_for_least,
                    R.array.Arrive_Depart);
        }

        if (mItineraries != null) {
            isBodySet = true;
            fillItinerariesLinearLayout(mItineraries, false);
        }

        View dragView = mTripPlanHead.findViewById(R.id.tp_dragview);
        setDragView(dragView);

        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        mMtdApiAdapter = MTDAPIAdapter.getInstance(mActivity);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Android restores the the SearchView state and adds incorrect text.
        // Must initialize the view params after that occurs (ie here) so the
        // TripPlannerHead will have correct origin/destination.
        // Note: use isInitializingHeadView so that we don't request new MTD
        // itineraries.
        if (mTripParams != null) {
            isInitializingHeadView = true;
            mTripPlanHead.initializeViewFromParams(mTripParams);
        }
        isInitializingHeadView = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Do this here. Android will call setText or something on the
        // SearchViews in the head sometime before here. So wait until here to
        // say that we are done initializing the head.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("itineraries", mItineraries);
        outState.putParcelable("tripParams", mTripParams);
    }

    private void initializeItinerariesList(final TripPlanParameters tripParams) {
        if (tripParams == null)
            Log.i(TAG, "initializeItinerariesList tripParams null");
        if (mView == null) {
            Log.i(TAG, "mView null");
            return;
        }
        clearListView();
        final LinearLayout lin = (LinearLayout) mView
                .findViewById(R.id.tripplanviewer_loadingview);
        lin.setVisibility(View.VISIBLE);
        TextView tv = (TextView) mView
                .findViewById(R.id.tripplanviewer_errortext);
        tv.setVisibility(View.GONE);

        String url = null;
        if ((mTripParams.origin instanceof Stop)
                && (mTripParams.destination instanceof Stop)) {
            url = mMtdApiAdapter.getPlannedTripsByStopsUrl(tripParams);
        } else
            url = mMtdApiAdapter.getPlannedTripsByLatLonUrl(tripParams);

        JsonObjectRequest jsObjReques = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Itinerary> itineraries = mMtdApiAdapter
                                .parseItinerariesFromJson(response);

                        if (itineraries == null || itineraries.size() < 1) {
                            TextView tv = (TextView) mView
                                    .findViewById(R.id.tripplanviewer_errortext);
                            tv.setVisibility(View.VISIBLE);
                            if (mMtdApiAdapter.hasError()) {
                                tv.setText(mMtdApiAdapter.getErrorMessage());
                            } else {
                                tv.setText("No itineraries available.");
                            }
                        } else
                            fillItinerariesLinearLayout(itineraries, true);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley error", error);
                TextView tv = (TextView) mView
                        .findViewById(R.id.tripplanviewer_errortext);
                tv.setVisibility(View.VISIBLE);
                tv.setText(error.getMessage());
            }
        });
        jsObjReques.setShouldCache(true);
        mRequestQueue.add(jsObjReques);

    }

    private void fillItinerariesLinearLayout(ArrayList<Itinerary> itineraries,
                                             boolean animate) {
        if (itineraries == null || itineraries.size() < 1) {
            return;
        }
        clearListView();
        mItineraries = itineraries;

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                Itinerary itinerary = mListAdapter.getItem(position);
                dispatchOnItineraryClick(itinerary, mTripParams);
            }
        });

        mListAdapter.addAllItems(itineraries);

        if (animate) {
            SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
                    mListAdapter);
            swingBottomInAnimationAdapter.setAbsListView(mListView);
            //swingBottomInAnimationAdapter.setAnimationDurationMillis(150);
            mListView.setAdapter(swingBottomInAnimationAdapter);
        } else {
            mListView.setAdapter(mListAdapter);
        }

        // TODO
        LinearLayout lin = (LinearLayout) mView
                .findViewById(R.id.tripplanviewer_loadingview);
        lin.setVisibility(View.GONE);

    }

    public void setOnItineraryClickListener(OnItineraryClickListener listener) {
        mItineraryClickListener = listener;
    }

    public void dispatchOnItineraryClick(Itinerary itinerary,
                                         TripPlanParameters tripParams) {
        if (mItineraryClickListener != null)
            mItineraryClickListener.onItineraryClick(itinerary, tripParams);
    }

    public TripPlanParameters getTripParameters() {
        return mTripParams;
    }

    @Override
    public View getHeadView() {
        return mTripPlanHead;
    }

    public void setDestination(MyLocation location) {
        if (mTripPlanHead != null)
            mTripPlanHead.setDestination(location);
    }

    private void fillBody(TripPlanParameters params) {
        if (params != null && params.hasAllParameters()) {
            isBodySet = true;
            setTripPlanParams(params);
        }
    }

    @Override
    public void fillBody() {
        if (!isBodySet && mTripParams != null && mTripPlanHead != null) {
            fillBody(mTripParams);
        }
    }

    public void setOnOriginDestinationChangeListener(
            OnOriginDestinationChangedListener listener) {
        mOriginDestinationChangedListener = listener;
    }

    private void dispatchOnOriginSet(MyLocation origin) {
        if (mOriginDestinationChangedListener != null)
            mOriginDestinationChangedListener.onOriginSet(origin);
        else
            Log.i(TAG, "dispatchOnOriginSet listener null");
    }

    private void dispatchOnDestinationSet(MyLocation destination) {
        if (mOriginDestinationChangedListener != null)
            mOriginDestinationChangedListener.onDestinationSet(destination);
        else
            Log.i(TAG, "dispatchOnDestinationSet listener null");
    }

    private void dispatchOnOriginRemoved() {
        if (mOriginDestinationChangedListener != null)
            mOriginDestinationChangedListener.onOriginRemoved();
        else
            Log.i(TAG, "dispatchOnOriginRemoved listener null");
    }

    private void dispatchOnDestinationRemoved() {
        if (mOriginDestinationChangedListener != null)
            mOriginDestinationChangedListener.onDestinationRemoved();
        else
            Log.i(TAG, "dispatchOnDestinationRemoved listener null");
    }

    private void clearListView() {
        if (mListAdapter != null)
            mListAdapter.clear();
    }

    @Override
    public boolean handleOnBackPressed() {
        mTripPlanHead.removeOrigin();
        mTripPlanHead.removeDestination();

        clearListView();

        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case LOADER_ORIGIN_ID:
                // This is called when a new Loader needs to be created. This
                // sample only has one Loader, so we don't care activity_about the ID.
                // First, pick the base URI to use depending on whether we are
                // currently filtering.
                Uri baseUri;
                if (mCurOrigSearchViewFilter != null) {
                    baseUri = Uri.withAppendedPath(
                            StopsProvider.CONTENT_URI_SUGGEST,
                            Uri.encode(mCurOrigSearchViewFilter));
                } else {
                    baseUri = StopsProvider.CONTENT_URI_SUGGEST;
                }

                CursorLoader cursorLoader = new CursorLoader(this.getActivity(),
                        baseUri, null, null, null, null);
                return cursorLoader;
            case LOADER_DEST_ID:
                Uri baseUri2;
                if (mCurrDestSearchViewFilter != null) {
                    baseUri2 = Uri.withAppendedPath(
                            StopsProvider.CONTENT_URI_SUGGEST,
                            Uri.encode(mCurrDestSearchViewFilter));
                } else {
                    baseUri2 = StopsProvider.CONTENT_URI_SUGGEST;
                }

                CursorLoader cursorLoader2 = new CursorLoader(this.getActivity(),
                        baseUri2, null, null, null, null);
                return cursorLoader2;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ORIGIN_ID:
                mTripPlanHead.swapOriginCursor(data);
                break;
            case LOADER_DEST_ID:
                mTripPlanHead.swapDestinationCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ORIGIN_ID:
                mTripPlanHead.swapOriginCursor(null);
                break;
            case LOADER_DEST_ID:
                mTripPlanHead.swapDestinationCursor(null);
        }
    }

    public interface OnItineraryClickListener {
        public void onItineraryClick(Itinerary itinerary,
                                     TripPlanParameters tripParams);
    }

    public class MySimpleArrayAdapter extends ArrayAdapter<Itinerary> {
        private final Context context;

        public MySimpleArrayAdapter(Context context) {
            this(context, new ArrayList<Itinerary>());
        }

        public MySimpleArrayAdapter(Context context, ArrayList<Itinerary> values) {
            super(context, R.layout.triplist_item, values);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.triplist_item, null);
            }
            ItineraryView itinView = (ItineraryView) view
                    .findViewById(R.id.triplist_item_itineraryview);

            itinView.setItinerary(getItem(position));

            return view;
        }

        @SuppressLint("NewApi")
        public void addAllItems(Collection<? extends Itinerary> collection) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                super.addAll(collection);
            } else {
                for (Itinerary itinerary : collection) {
                    add(itinerary);
                }
            }
        }

    }
}
