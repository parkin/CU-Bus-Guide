package com.teamparkin.mtdapp.fragments;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.SettingsActivity;
import com.teamparkin.mtdapp.dataclasses.Departure;
import com.teamparkin.mtdapp.dataclasses.Itinerary;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.dataclasses.TripPlanParameters;
import com.teamparkin.mtdapp.fragments.MapV2Fragment.OnRemoveSelectedLocationListener;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener.OnLocationSelectedListener;
import com.teamparkin.mtdapp.listeners.OnRequestFragmentVisibilityListener;
import com.teamparkin.mtdapp.listeners.OnRequestPanelInfoListener;
import com.teamparkin.mtdapp.listeners.OnRequestTripPlanInfoListener;
import com.teamparkin.mtdapp.util.Util;

/**
 * This is a fragment that includes a MapV2Fragment with other views too!
 *
 * @author will
 */
public class MapV2ContainerFragment extends MyFragment {
    @SuppressWarnings("unused")
    private static final String TAG = MapV2ContainerFragment.class
            .getSimpleName();
    private static final int DEFAULT_MAP_PADDING_TOP = 10;
    private static final int DEFAULT_MAP_PADDING_LEFT = 10;
    private static final int DEFAULT_MAP_PADDING_RIGHT = 10;
    private static final int DEFAULT_MAP_PADDING_BOTTOM = 10;
    private View mView;
    private MapV2Fragment mMapFragment;
    private OnLocationSelectedListener mLocationSelectedListener;
    private OnRemoveSelectedLocationListener mRemoveSelectedLocationListener;
    private OnRequestFragmentVisibilityListener mRequestFragmentVisibilityListener;
    private OnRequestTripPlanInfoListener mRequestTripPlanInfoListener;
    private OnRequestItineraryInfoListener mRequestItineraryInfoListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.mapv2, container, false);
        mMapFragment = (MapV2Fragment) getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.mapv2);
        mMapFragment.getMap()
                .setPadding(
                        Util.getPixelsFromDips(getActivity(),
                                DEFAULT_MAP_PADDING_LEFT),
                        Util.getPixelsFromDips(getActivity(),
                                DEFAULT_MAP_PADDING_TOP),
                        Util.getPixelsFromDips(getActivity(),
                                DEFAULT_MAP_PADDING_RIGHT),
                        Util.getPixelsFromDips(getActivity(),
                                DEFAULT_MAP_PADDING_BOTTOM)
                                + requestPanelHeight()
                );

        initializeMapListeners();

        Uri loc = requestPanelLocation();
        if (loc != null) {
            mMapFragment.setLocationSelection(loc, true);
        }
        TripPlanParameters params = requestTripPlanParameters();
        if (params != null) {
            if (params.origin != null)
                mMapFragment.setTripOrigin(params.origin);
            if (params.destination != null)
                mMapFragment.setTripDestination(params.destination);
        }
        Itinerary itinerary = requestPanelItinerary();
        if (itinerary != null) {
            mMapFragment.setItinerary(itinerary);
        }


        // Hide the nav drawer icon if the user wants to.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        boolean hideNavDrawer = sharedPref.getBoolean(SettingsActivity
                .KEY_PREF_UI_MAP_HIDE_NAV_DRAWER_ICON, false);
        if (hideNavDrawer) {
            ImageView navDrawerImageView = (ImageView) mView.findViewById(R.id.mapv2_nav_drawer_image_view);
            navDrawerImageView.setVisibility(View.GONE);
        }

        return mView;
    }


    private void initializeMapListeners() {
        mMapFragment
                .setOnLocationSelectedListener(new MyFragmentsListener.OnLocationSelectedListener() {
                    @Override
                    public void onLocationSelected(Uri location) {
                        dispatchOnLocationSelected(location);
                    }
                });
        mMapFragment
                .setOnRemoveSelectedLocationListener(new MapV2Fragment.OnRemoveSelectedLocationListener() {
                    @Override
                    public void onRemoveSelectedLocationListener() {
                        dispatchOnRemoveSelectedLocation();
                    }
                });
        mMapFragment
                .setOnRequestPanelInfoListener(new OnRequestPanelInfoListener() {
                    @Override
                    public Uri onRequestPanelLocation() {
                        return requestPanelLocation();
                    }

                    @Override
                    public int onRequestPanelHeight() {
                        return requestPanelHeight();
                    }

                    @Override
                    public boolean onRequestPanelIsOpen() {
                        return requestPanelIsOpen();
                    }
                });
        mMapFragment
                .setOnRequestTripPlannerVisibility(new OnRequestFragmentVisibilityListener() {
                    @Override
                    public boolean onRequestShouldUpdateLocation() {
                        return requestShouldUpdateLocation();
                    }
                });
    }

    public void showDeparture(Departure departure) {
        mMapFragment.displayDeparture(departure, true);
    }

    @Override
    public boolean shouldShowActionBar() {
        return false;
    }

    public void setSelectedLocation(Uri location) {
        mMapFragment.setLocationSelection(location, true);
    }

    @Override
    public boolean onBack() {
        return mMapFragment.onBack();
    }

    @Override
    public void onBottomOverlayChange(float offset) {
        final FragmentActivity activity = getActivity();
        if (activity != null && mMapFragment != null && mMapFragment.getMap() != null) {
            int[] pixels = Util.getPixelsFromDips(activity, DEFAULT_MAP_PADDING_LEFT,
                    DEFAULT_MAP_PADDING_TOP, DEFAULT_MAP_PADDING_RIGHT, DEFAULT_MAP_PADDING_BOTTOM);
            mMapFragment.getMap().setPadding(
                    pixels[0],
                    pixels[1],
                    pixels[2],
                    (int) (pixels[3] + offset)
            );
        }
    }

    @Override
    public void onBottomOverlayHidden() {
        // Treat hiding the pane as a back press.
        mMapFragment.onBack();
    }

    public void setTripOrigin(MyLocation origin) {
        if (mMapFragment != null)
            mMapFragment.setTripOrigin(origin);
        else
            Log.e(TAG, "setTripOrigin map fragment null");
    }

    public void removeTripOrigin() {
        if (mMapFragment != null)
            mMapFragment.removeTripOrigin();
        else
            Log.e(TAG, "setTripOrigin map fragment null");
    }

    public void setTripDestination(MyLocation destination) {
        if (mMapFragment != null)
            mMapFragment.setTripDestination(destination);
        else
            Log.e(TAG, "setTripOrigin map fragment null");
    }

    public void removeTripDestination() {
        if (mMapFragment != null)
            mMapFragment.removeTripDestination();
        else
            Log.e(TAG, "setTripOrigin map fragment null");
    }

    public void setOnLocationSelectedListener(
            MyFragmentsListener.OnLocationSelectedListener listener) {
        mLocationSelectedListener = listener;
    }

    private void dispatchOnLocationSelected(Uri location) {
        if (mLocationSelectedListener != null)
            mLocationSelectedListener.onLocationSelected(location);
        else
            Log.i(TAG, "dispatchOnLocationSelected listener null");
    }

    public void setOnRemoveLocationSelectionListener(
            MapV2Fragment.OnRemoveSelectedLocationListener listener) {
        mRemoveSelectedLocationListener = listener;
    }

    private void dispatchOnRemoveSelectedLocation() {
        if (mRemoveSelectedLocationListener != null)
            mRemoveSelectedLocationListener.onRemoveSelectedLocationListener();
        else
            Log.i(TAG, "mRemoveSelectedLocationListener listener null");
    }

    public void setOnRequestFragmentVisibilityListener(
            OnRequestFragmentVisibilityListener listener) {
        mRequestFragmentVisibilityListener = listener;
    }

    private boolean requestShouldUpdateLocation() {
        if (mRequestFragmentVisibilityListener != null)
            return mRequestFragmentVisibilityListener
                    .onRequestShouldUpdateLocation();
        Log.e(TAG, "requestTripPlannerVisibility listener null");
        return true;
    }

    public void setOnRequestTripPlanInfoListener(
            OnRequestTripPlanInfoListener listener) {
        mRequestTripPlanInfoListener = listener;
    }

    public TripPlanParameters requestTripPlanParameters() {
        if (mRequestTripPlanInfoListener != null)
            return mRequestTripPlanInfoListener.onRequestTripPlanParameters();
        Log.e(TAG, "requestTripPlanParameters listener null");
        return null;
    }

    public void setDeparture(Departure departure) {
        mMapFragment.displayDeparture(departure, true);
    }

    /**
     * Set the itinerary on the Map fragment.
     *
     * @param itinerary
     */
    public void setItinerary(Itinerary itinerary) {
        mMapFragment.setItinerary(itinerary);
    }

    public void removeItinerary() {
        mMapFragment.removeItinerary();
    }

    public void setOnRequestItineraryInfoListener(
            OnRequestItineraryInfoListener listener) {
        mRequestItineraryInfoListener = listener;
    }

    private Itinerary requestPanelItinerary() {
        if (mRequestItineraryInfoListener != null)
            return mRequestItineraryInfoListener.onRequestPanelItinerary();
        Log.i(TAG, "requestPanelItineraryInfo listener null");
        return null;
    }

    @Override
    public boolean shouldExpandLocationPaneAfterShow() {
        return false;
    }

}
