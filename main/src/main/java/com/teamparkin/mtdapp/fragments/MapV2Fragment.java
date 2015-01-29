package com.teamparkin.mtdapp.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nineoldandroids.animation.ObjectAnimator;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContentProvider;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.dataclasses.Departure;
import com.teamparkin.mtdapp.dataclasses.Itinerary;
import com.teamparkin.mtdapp.dataclasses.Leg;
import com.teamparkin.mtdapp.dataclasses.LegEndpoint;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.dataclasses.RouteShape;
import com.teamparkin.mtdapp.dataclasses.RouteShapePoint;
import com.teamparkin.mtdapp.dataclasses.ServiceLeg;
import com.teamparkin.mtdapp.dataclasses.StopPoint;
import com.teamparkin.mtdapp.dataclasses.Trip;
import com.teamparkin.mtdapp.dataclasses.WalkLeg;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener.OnLocationSelectedListener;
import com.teamparkin.mtdapp.listeners.OnRequestFragmentVisibilityListener;
import com.teamparkin.mtdapp.listeners.OnRequestPanelInfoListener;
import com.teamparkin.mtdapp.restadapters.MTDAPIAdapter;
import com.teamparkin.mtdapp.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapV2Fragment extends SupportMapFragment implements
        OnMarkerClickListener, OnCameraChangeListener,
        OnInfoWindowClickListener, OnMarkerDragListener, OnMapClickListener,
        LoaderCallbacks<Cursor> {
    private static final String TAG = MapV2Fragment.class.getSimpleName();

    private static final int LOADER_ID = 77449238;
    private static final float MIN_ZOOM_FOR_NONFAVS = 15.0f;
    private static final float MIN_ZOOM_FOR_STOPPOINTS = 17.0f;
    private static final long eventsTimeout = 50L;
    private static final int ID_NORMAL = 0;
    private static final int ID_HYBRID = 1;
    private static final int ID_SATELLITE = 2;
    private static final int ID_TERRAIN = 3;
    // when a departure is clicked so it's
    // polyline goes up front
    private static final int ID_NONFAV_STOPS = 4;
    private static final int ID_FAV_STOPS = 5;
    private static final int ID_NONFAV_PLACES = 6;
    private static final int ID_FAV_PLACES = 7;
    // Trip Planning
    private static final String TRIP_ORIGIN_TITLE = "Trip Origin";
    private static final String TRIP_DESTINATION_TITLE = "Trip Destination";
    // waiting for user
    private static final int TRIP_NOTHING = 0;
    // tap to select origin
    private static final int TRIP_ORIGIN = 1;
    private static final int TRIP_DESTINATION = 2;
    private static final int DEFAULT_POLYLINE_WIDTH_DP = 3;
    // stops that are currently markers
    private static Map<String, Marker> mMarkerByUri;
    // bus markers
    private static Map<Departure, Marker> mBusMarkerByDeparture;
    // routes
    private static Map<String, Polyline> mPolylinesByShapeId;
    private static List<String> mShapeIds;
    private static int mZIndex = 0; // z index. for polylines. will add to this
    private static CameraPosition previosCameraPosition = null;
    private static boolean SHOW_NONFAV_STOPS = true;
    private static boolean SHOW_FAV_STOPS = true;
    private static boolean SHOW_NONFAV_PLACES = true;
    private static boolean SHOW_FAV_PLACES = true;
    private static boolean MARKER_CHANGE = false;
    private static boolean TRIP_PLANNER_VISIBLE = false;
    private static int TRIP_PLANNER_STATUS = 0;
    // whether or not this activity came from TripPlanSelector
    private static boolean TRIP_PLAN_SELECTOR = false;
    private MTDAPIAdapter mMtdAdapter;
    private GoogleMap mMap;
    private Timer event_delay_timer = new Timer();
    private AsyncTask<Void, Void, List<List<Uri>>> mTask = null;
    private MyLocation busTrackerStop = null;
    private GroundOverlay busTrackerGroundOverlay = null;
    private Marker mMarkerSelected = null;
    private MyLocation mLocationSelected = null;
    // origin and destination
    private MyLocation mOrigin = null;
    private Marker mMarkerOrigin = null;
    private MyLocation mDestination = null;
    private Marker mMarkerDestination = null;
    private OnLocationSelectedListener mLocationSelectedListener;
    private OnRemoveSelectedLocationListener mRemoveSelectedLocationListener;
    private OnRequestFragmentVisibilityListener mRequestTripPlanVisListener;
    private OnRequestPanelInfoListener mRequestPanelInfoListener;
    private Itinerary mItinerary;
    private List<Marker> mItineraryMarkers;
    private List<Polyline> mItineraryPolylines;
    private Cursor mCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMarkerByUri = new HashMap<String, Marker>();
        mPolylinesByShapeId = new HashMap<String, Polyline>();
        mShapeIds = new ArrayList<String>();
        mBusMarkerByDeparture = new HashMap<Departure, Marker>();
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMap = getMap();

        mMtdAdapter = MTDAPIAdapter.getInstance(getActivity());

        if (mMap != null) {
            setUpMap();
            previosCameraPosition = mMap.getCameraPosition();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            Log.e(TAG, "VIEW NULL :(");
        }
        setMapTransparent((ViewGroup) view);
        // View view = inflater.inflate(R.layout.mapfrag, container, false);
        // mMap = getMap();
        // setUpMapIfNeeded();
        return view;
    }

    ;

    @Override
    public void onPause() {
        super.onPause();
        cacheCameraState();
    }

    /**
     * Save the camera position so it can be recalled the next time the user
     * opens a map.
     */
    public void cacheCameraState() {
        CameraPosition position = mMap.getCameraPosition();
        SharedPreferences.Editor editor = getActivity().getPreferences(
                Activity.MODE_PRIVATE).edit();
        editor.putFloat("mapv2lat", (float) position.target.latitude);
        editor.putFloat("mapv2lon", (float) position.target.longitude);
        editor.putFloat("mapv2zoom", (float) Math.round(position.zoom));
        editor.commit();
    }

    private void setMapTransparent(ViewGroup group) {
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                setMapTransparent((ViewGroup) child);
            } else if (child instanceof SurfaceView) {
                child.setBackgroundColor(0x00000000);
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setInfoWindowAdapter(new LocationInfoWindowAdapter(getActivity()
                .getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        // set the map to the previously viewed location or the default one.
        SharedPreferences prefs = getActivity().getPreferences(
                Activity.MODE_PRIVATE);
        double lat = prefs.getFloat("mapv2lat", 40.108013f);
        double lon = prefs.getFloat("mapv2lon", -88.227253f);
        float zoom = prefs.getFloat("mapv2zoom", 12.0f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),
                zoom));
        previosCameraPosition = mMap.getCameraPosition();
        MARKER_CHANGE = true;
        Bundle bundle = new Bundle();
        bundle.putParcelable("cameraPosition", previosCameraPosition);
        getLoaderManager().initLoader(LOADER_ID, bundle, MapV2Fragment.this);
    }

    private void showDepartures(Map<StopPoint, List<Departure>> map) {
        List<Departure> departures = new ArrayList<Departure>();
        for (StopPoint stopPoint : map.keySet()) {
            departures.addAll(map.get(stopPoint));
        }
        // remove all marker's title/snippet
        for (Departure id : mBusMarkerByDeparture.keySet()) {
            mBusMarkerByDeparture.get(id).setTitle("");
            mBusMarkerByDeparture.get(id).setSnippet("");
        }
        List<Departure> departuresToRemove = new ArrayList<Departure>();
        // go through the list of currently displayed buses to see if we
        // should remove any. Also check if we should remove the trip
        for (Departure departure : mBusMarkerByDeparture.keySet()) {
            // Remove the markers on the map if they are no longer
            // needed. Only do this once for the double for loop.
            if (!departures.contains(departure)) {
                departuresToRemove.add(departure);
            }
        }
        // Actually remove here so we dont get a concurrent modification
        // exception
        for (Departure departure : departuresToRemove) {
            Marker marker = mBusMarkerByDeparture.get(departure);
            if (marker != null)
                marker.remove();
            mBusMarkerByDeparture.remove(departure);
        }
        List<String> tripIdsToRemove = new ArrayList<String>();
        // go through all the tripIds to see if we need to remove any
        for (String tripId : mShapeIds) {

            boolean shouldRemove = true;
            // Check if we should remove the trip
            for (Departure departure : departures) {
                if (tripId.equalsIgnoreCase(departure.getTrip().getShape_id()
                        .trim())) {
                    shouldRemove = false; // this trip will be displayed, so
                    // don't remove!!
                    break;
                }
            }
            if (shouldRemove) {
                tripIdsToRemove.add(tripId);
            }
        }
        // Actually remove the trips
        for (String tripId : tripIdsToRemove) {
            mShapeIds.remove(tripId);
            Polyline line = mPolylinesByShapeId.get(tripId);
            if (line != null) {
                line.remove();
            }
            mPolylinesByShapeId.remove(tripId);
        }
        // iterate over stoppoints to add to list
        for (StopPoint stoppoint : map.keySet()) {
            if (stoppoint == null) {
                Log.e(TAG, "fillDeparturesLinearLayout StopPoint is null :(");
            }
            // Add makers to list. Add the to map if they are not already there.
            for (Departure departure : map.get(stoppoint)) {
                if (!mBusMarkerByDeparture.containsKey(departure)) {
                    addBusToMap(departure, false);
                    mBusMarkerByDeparture.get(departure).setSnippet(
                            "" + departure.getExpected_mins() + " min to "
                                    + stoppoint.getName()
                    );
                } else {
                    Marker marker = mBusMarkerByDeparture.get(departure);
                    // otherwise we must change the current marker's position
                    marker.setPosition(departure.getLatLng());
                    String snippet = marker.getSnippet();
                    if (marker.getTitle().length() < 1) {
                        marker.setTitle(departure.getHeadsign());
                        marker.setSnippet(departure.getExpected_mins()
                                + " min to " + stoppoint.getName());
                    } else {
                        marker.setSnippet(snippet + ",\n"
                                + departure.getExpected_mins() + " min to "
                                + stoppoint.getName());
                    }
                }
                // add the trip polyline if needed
                addRouteShapePolyline(departure, Util.getPixelsFromDips(
                        getActivity(), DEFAULT_POLYLINE_WIDTH_DP));
            }
        }
    }

    /**
     * This should only be called when you want to display 1 bus/route pair at a
     * time.
     *
     * @param departure
     * @param clearOtherDepartureMapObjects
     */
    @SuppressLint("NewApi")
    public void displayDeparture(Departure departure,
                                 boolean clearOtherDepartureMapObjects) {
        if (clearOtherDepartureMapObjects) {
            List<Departure> depsToRemove = new ArrayList<Departure>();
            for (Departure departure2 : mBusMarkerByDeparture.keySet()) {
                if (!departure2.equals(departure))
                    depsToRemove.add(departure2);
            }
            for (Departure departure2 : depsToRemove) {
                mBusMarkerByDeparture.get(departure2).remove();
                mBusMarkerByDeparture.remove(departure2);
            }
            Trip trip = departure.getTrip();
            String shapeId = "";
            if (trip != null) {
                shapeId = trip.getShape_id();
            }
            List<String> shapesToRemove = new ArrayList<String>();
            for (String id : mShapeIds) {
                if (!id.equals(shapeId)) {
                    shapesToRemove.add(id);
                }
            }
            for (String id : shapesToRemove) {
                mShapeIds.remove(id);
                if (mPolylinesByShapeId.get(id) != null)
                    mPolylinesByShapeId.get(id).remove();
                mPolylinesByShapeId.remove(id);
            }
        }
        if (departure == null )
            return;

        if (departure.getLatLng() != null && departure.getLatLng().latitude != 0.0
                && departure.getLatLng().longitude != 0.0) {
            addBusToMap(departure, clearOtherDepartureMapObjects);
            Uri panelUri = requestPanelLocation();
            if (panelUri != null) {
                Cursor c = getActivity().getContentResolver().query(panelUri,
                        null, null, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        MyLocation panelLoc = MyLocationContentProvider
                                .buildLocationFromCursor(c);
                        animateToBoundsOfLatLngs(departure.getLatLng(),
                                panelLoc.getLatLng());
                    }
                    c.close();
                }
            } else
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        departure.getLatLng(), MIN_ZOOM_FOR_NONFAVS - 1));
        } else { // invalid bus location :(
            Toast.makeText(getActivity(), "Bus location invalid.",
                    Toast.LENGTH_SHORT).show();
        }
        if (departure.getTrip() == null
                || departure.getTrip().getShape_id() == null
                || departure.getTrip().getShape_id().length() < 1) {
            Toast.makeText(getActivity(),
                    "No route info supplied for departure.", Toast.LENGTH_SHORT)
                    .show();
        } else {
            addRouteShapePolyline(departure, Util.getPixelsFromDips(
                    getActivity(), DEFAULT_POLYLINE_WIDTH_DP));
        }
    }

    @SuppressLint("NewApi")
    private void animateToBoundsOfLatLngs(LatLng... latLngs) {
        if (latLngs == null || latLngs.length < 1)
            return;
        Builder builder = new LatLngBounds.Builder();

        for (LatLng latLng : latLngs) {
            builder.include(latLng);
        }
        int panelHeight = requestPanelHeight();

        int padding_dp = 50;
        float scale = getActivity().getResources().getDisplayMetrics().density;
        int padding_px = (int) (padding_dp * scale + 0.5f);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = 0;
        int height = 0;
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y - panelHeight;
        } else {
            width = display.getWidth();
            height = display.getHeight() - panelHeight;
        }
        CameraUpdate newLatLngBounds = null;
        newLatLngBounds = CameraUpdateFactory.newLatLngBounds(builder.build(),
                width, height, padding_px);
        mMap.animateCamera(newLatLngBounds);
    }

    private void addRouteShapePolyline(Departure departure, int width) {
        addRouteShapePolyline(departure.getTrip(), departure.getRoute()
                .getRoute_color(), width);
    }

    /**
     * Asks for a RouteShape from the MTDApi and adds a polyline for the shape
     * between its begin and end stops. If there is an error downloading a
     * RouteShape, it just adds a straight line between begin and end. Note this
     * adds the polyline to mItineraryPolylines
     *
     * @param serviceLeg
     * @param width
     */
    private void addRouteShapeOnServiceLeg(final ServiceLeg serviceLeg,
                                           final int width) {
        AsyncTask<Void, Void, RouteShape> task = new AsyncTask<Void, Void, RouteShape>() {
            @Override
            protected RouteShape doInBackground(Void... params) {
                RouteShape shape = mMtdAdapter.getShapeBetweenStops(serviceLeg
                        .getBegin().getStopId(), serviceLeg.getEnd()
                        .getStopId(), serviceLeg.getTrip().getShape_id());
                if (mMtdAdapter.hasError())
                    Log.e(TAG, mMtdAdapter.getErrorMessage());
                return shape;
            }

            @Override
            protected void onPostExecute(RouteShape shape) {
                Polyline line = null;
                // If we couldn't get a routeShape from MTD, just add a straight
                // line between the endpoints.
                if (shape != null) {
                    line = drawRouteShapePolyline(shape, serviceLeg.getRoute()
                            .getRoute_color(), serviceLeg.getTrip()
                            .getShape_id(), width);
                } else {
                    String err = "No MTD shape for "
                            + serviceLeg.getTrip().getShape_id();
                    Log.e(TAG, err);
                    Toast.makeText(getActivity(), err, Toast.LENGTH_SHORT)
                            .show();
                    line = addPolylineBetweenLegEndpoints(serviceLeg, width);
                }
                mItineraryPolylines.add(line);
            }

        };
        task.execute();
    }

    private void addRouteShapePolyline(final Trip trip,
                                       final String route_color, final int width) {
        if (trip == null || route_color == null) {
            return;
        }
        // setLoadingBarVisibility(true);
        final String shapeId = trip.getShape_id().trim();
        if (mShapeIds.contains(shapeId)) {
            Polyline line = mPolylinesByShapeId.get(shapeId);
            if (line != null) {
                mZIndex++;
                line.setZIndex(mZIndex);
            }
            return;
        }
        mShapeIds.add(shapeId);
        AsyncTask<Trip, Void, RouteShape> task = new AsyncTask<Trip, Void, RouteShape>() {
            @Override
            protected RouteShape doInBackground(Trip... params) {
                return mMtdAdapter.getShape(params[0].getShape_id());
            }

            @Override
            protected void onPostExecute(RouteShape shape) {
                if (shape != null) {
                    Polyline line = drawRouteShapePolyline(shape, route_color,
                            shapeId, width);
                    mPolylinesByShapeId.put(shapeId, line);
                } else {
                    Log.e(TAG, "Could not get route shape!");
                    Toast.makeText(getActivity(), "Could not get route shape.",
                            Toast.LENGTH_LONG).show();
                }
            }

        };
        task.execute(trip);
    }

    public void clearBusOverlays() {
        for (Departure departure : mBusMarkerByDeparture.keySet()) {
            mBusMarkerByDeparture.get(departure).remove();
        }
        mBusMarkerByDeparture.clear();
    }

    public void clearRouteShapePolylines() {
        for (String id : mShapeIds) {
            if (mPolylinesByShapeId != null) {
                Polyline pl = mPolylinesByShapeId.get(id);
                if (pl != null)
                    pl.remove();
            }
        }
        mShapeIds.clear();
        mPolylinesByShapeId.clear();
    }

    /**
     * Draws a route shape polyline for the given parameters.
     *
     * @param shape
     * @param route_color String, no alpha channel or pound sign, eg "e6e6e6". An alpha
     *                    channel "#AA" will be added.
     * @param shapeId
     * @param width
     * @return
     */
    private Polyline drawRouteShapePolyline(RouteShape shape,
                                            String route_color, String shapeId, int width) {
        List<LatLng> list = new ArrayList<LatLng>();
        for (RouteShapePoint point : shape.getShapePoints()) {
            list.add(point.getLatLng());
        }
        return drawPolyLine(list, Color.parseColor("#AA" + route_color), width);
        // setLoadingBarVisibility(false);
    }

    /**
     * Draws a polyline on the map from the latLngs list, using color.
     *
     * @param latLngs
     * @param color   Eg color int parsed by Color.parseColor
     */
    private Polyline drawPolyLine(List<LatLng> latLngs, int color, int width) {
        if (latLngs == null)
            return null;
        return mMap.addPolyline(new PolylineOptions().addAll(latLngs)
                .width(width).color(color).zIndex(mZIndex));
    }

    private void addBusToMap(Departure departure, boolean isNotBusTracking) {
        Marker busMarker = null;
        // Redo the marker, unless we are busTracking, then let that handle
        // redoing the marker
        if (mBusMarkerByDeparture.containsKey(departure)) {
            busMarker = mBusMarkerByDeparture.get(departure);
            if (isNotBusTracking) {
                busMarker.setPosition(departure.getLatLng());
                busMarker.setTitle(departure.getHeadsign());
                busMarker.setSnippet(departure.getExpected_mins() + " min");
            }
        } else {
            busMarker = mMap.addMarker(new MarkerOptions()
                    .position(departure.getLatLng())
                    .title(departure.getHeadsign())
                    .snippet(departure.getExpected_mins() + " min")
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .fromResource(android.R.drawable.presence_online)));
            mBusMarkerByDeparture.put(departure, busMarker);
        }
        if (busMarker != null)
            busMarker.showInfoWindow();
        // mBusDepartures.add(departure);
    }

    @Override
    public void onMapClick(LatLng latlng) {
        if (!requestShouldUpdateLocation()) {
            // do not allow map clicks if the trip planner is visible.
            return;
        }
        // Only remove the location selection if there is no route polylines or
        // bus markers. If there are, pressing back will remove everything.
        if (mBusMarkerByDeparture.size() < 1 && mPolylinesByShapeId.size() < 1) {
            removeLocationSelection();
            dispatchOnRemoveSelectedLocation();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (!requestShouldUpdateLocation()) {
            // do not allow marker clicks if the trip planner is visible.
            return true;
        }

        // check if the marker belongs to a departure
        if (mBusMarkerByDeparture != null
                && mBusMarkerByDeparture.containsValue(marker)) {
            marker.showInfoWindow();
            // if it is a departure, bring its polyline to the foreground
            bringDeparturePolylineToFront(marker);
            return true;
        }

        String id = marker.getTitle();
        // make sure the id is from one of the MyLocation markers.
        if (mMarkerByUri.containsKey(id)) {
            Uri uri = Uri.parse(id);
            setLocationSelectionWithCallback(uri, false);
        }

        return true;
    }

    private void setLocationSelectionWithCallback(Uri location,
                                                  boolean animateToLocation) {
        setLocationSelection(location, animateToLocation);
        dispatchOnLocationSelected(location);
    }

    public void setLocationSelection(Uri locationUri, boolean animateToLocation) {

        // get the location
        MyLocation location = null;
        Cursor c = getActivity().getContentResolver().query(locationUri, null,
                null, null, null);
        if (c.moveToFirst())
            location = MyLocationContentProvider.buildLocationFromCursor(c);
        c.close();
        if (location == null)
            return;

        boolean shouldAdd = true;
        // remove the previously selected marker, if it's not the same
        if (mMarkerSelected != null) {
            if (location != null && !location.equals(mLocationSelected)) {
                mMarkerSelected.remove();
                clearBusOverlays();
                clearRouteShapePolylines();
                removeTripDestination();
                removeTripOrigin();
            } else
                shouldAdd = false;
        }
        if (mLocationSelected != null)
            Log.i(TAG, "loc equal ? " + mLocationSelected.equals(location));
        mLocationSelected = location;
        if (shouldAdd) {
            // add the newly selected marker
            mMarkerSelected = mMap.addMarker(new MarkerOptions()
                    .position(location.getLatLng())
                    .title(location.getName())
                    .snippet(location.getId())
                    .icon(BitmapDescriptorFactory.defaultMarker(location
                            .getMarkerHue())));
            mMarkerSelected.setAlpha(0.0f);
            ObjectAnimator.ofFloat(mMarkerSelected, "alpha", 0.0f, 1.f)
                    .setDuration(300).start();

        }
        if (animateToLocation)
            animateToLocation(location);
    }

    /**
     * Removes the selected marker from the map. Hides the activity's
     * slideUpPane. Returns true if marker was removed.
     *
     * @return
     */
    private boolean removeLocationSelection() {
        boolean ret = mMarkerSelected != null || mLocationSelected != null;
        if (mMarkerSelected != null) {
            mMarkerSelected.remove();
        }
        mMarkerSelected = null;
        mLocationSelected = null;
        return ret;
    }

    public void animateToLocation(MyLocation location) {
        boolean isFav = MyLocationContentProvider.isFavorite(getActivity(),
                location);
        // animate to the location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                location.getLatLng(), MIN_ZOOM_FOR_NONFAVS + 1.0f));
        if (isFav) {
            SHOW_FAV_PLACES = true;
            SHOW_FAV_STOPS = true;
        } else {
            SHOW_NONFAV_PLACES = true;
            SHOW_NONFAV_STOPS = true;
        }
    }

    /**
     * If the marker belongs to a departure, this method brings the polyline
     * associated with that departure to the foreground.
     *
     * @param marker
     */
    private void bringDeparturePolylineToFront(Marker marker) {
        if (marker.getSnippet().contains("min to")) {
            for (Departure departure : mBusMarkerByDeparture.keySet()) {
                if (departure.getHeadsign().equalsIgnoreCase(marker.getTitle())) {
                    Polyline line = mPolylinesByShapeId.get(departure.getTrip()
                            .getShape_id());
                    if (line != null) {
                        mZIndex++;
                        line.setZIndex(mZIndex);
                    }
                }
            }
        }
    }

    @Override
    public void onCameraChange(final CameraPosition position) {
        updateMarkers(position);
    }

    private void updateMarkers(final CameraPosition position) {
        // if (position.zoom >= mMinZoomForNonFavs) {
        // if (previosZoom < mMinZoomForNonFavs) {
        // // we've just zoomed in, so reset the hashmap
        // }
        event_delay_timer.cancel();
        event_delay_timer = new Timer();
        event_delay_timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (mTask != null) {
                    mTask.cancel(true);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("cameraPosition", position);
                        getLoaderManager().restartLoader(LOADER_ID, bundle,
                                MapV2Fragment.this);
                    }
                });
            }

        }, eventsTimeout);
        // } else if (previosZoom < mMinZoomForNonFavs) {
        // event_delay_timer.cancel();
        // }
        previosCameraPosition = position;
    }

    /**
     * Adds a marker to the map and to mMarkerByUri
     *
     * @param uri
     * @param latLng
     * @param resId
     * @param anchorPosition
     */
    private void addMarkerToMap(Uri uri, LatLng latLng, int resId,
                                float[] anchorPosition, boolean favorite) {
        BitmapDescriptor bitmapDescriptor = null;
        bitmapDescriptor = BitmapDescriptorFactory.fromResource(resId);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .anchor(anchorPosition[0], anchorPosition[1]).position(latLng)
                .title(uri.toString()).snippet(favorite ? "1" : "0")
                .icon(bitmapDescriptor));
        mMarkerByUri.put(uri.toString(), marker);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTitle().equalsIgnoreCase(TRIP_ORIGIN_TITLE)) {
            createOriginDestinationAlertDialog(marker, true);
            return;
        } else if (marker.getTitle().equalsIgnoreCase(TRIP_DESTINATION_TITLE)) {
            createOriginDestinationAlertDialog(marker, false);
            return;
        }
    }

    private void createOriginDestinationAlertDialog(final Marker marker,
                                                    final boolean isOrigin) {
        CharSequence[] items = new CharSequence[2];
        items[0] = "Choose New Location";
        items[1] = "Remove";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(isOrigin ? TRIP_ORIGIN_TITLE : TRIP_DESTINATION_TITLE);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    TRIP_PLANNER_STATUS = isOrigin ? TRIP_ORIGIN
                            : TRIP_DESTINATION;
                    Toast.makeText(getActivity(),
                            "Tap map to select location.", Toast.LENGTH_SHORT)
                            .show();
                } else if (item == 1) {
                    if (marker != null) {
                        marker.remove();
                        if (isOrigin) {
                            mOrigin = null;
                            // TODO finish implementing me
                        } else {
                            mDestination = null;
                            // TODO finish implementing me
                        }
                    }
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void setBusTrackerVisible(boolean visible, MyLocation location) {
        if (visible) {
            // setTripPlannerVisible(false);
            if (location != null) {
                busTrackerStop = location;
                if (busTrackerGroundOverlay != null) {
                    busTrackerGroundOverlay.remove();
                }
                busTrackerGroundOverlay = mMap
                        .addGroundOverlay(new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory
                                        .fromResource(R.drawable.target))
                                .zIndex(-1f)
                                .position(location.getLatLng(), 300f)
                                .transparency(0.5f)); // TODO
            }
        } else {
            if (busTrackerGroundOverlay != null) {
                busTrackerGroundOverlay.remove();
                busTrackerGroundOverlay = null;
                clearBusOverlays();
                clearRouteShapePolylines();
            }
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // TODO
        // setTripPlannerText(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // TODO fix
        // LatLng position = marker.getPosition();
        // String name = "(lat,lng): " + position.latitude + ", "
        // + position.longitude;
        // LocationMarkerPair lmp = null;
        // boolean isOrigin = false;
        // if (marker.getTitle().equalsIgnoreCase(TRIP_ORIGIN_TITLE)) {
        // lmp = new LocationMarkerPair(new GenericLocation(TRIP_ORIGIN_TITLE,
        // name, position), marker, false);
        // isOrigin = true;
        // } else if
        // (marker.getTitle().equalsIgnoreCase(TRIP_DESTINATION_TITLE)) {
        // lmp = new LocationMarkerPair(new GenericLocation(
        // TRIP_DESTINATION_TITLE, name, position), marker, false);
        // } else
        // return;
        // TODO
        // setTripPlannerText(lmp, isOrigin);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // TODO
        // setTripPlannerText(marker);
    }

    public boolean onBack() {
        clearBusOverlays();
        clearRouteShapePolylines();
        return removeTripDestination() || removeTripOrigin()
                || removeLocationSelection();
    }

    private float getOriginDestinationMarkerHue(boolean isOrigin) {
        return isOrigin ? BitmapDescriptorFactory.HUE_GREEN
                : BitmapDescriptorFactory.HUE_ROSE;
    }

    private void animateToOriginAndDestination() {
        if (mOrigin != null && mDestination != null)
            animateToBoundsOfLatLngs(mOrigin.getLatLng(),
                    mDestination.getLatLng());
        else if (mOrigin != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mOrigin
                    .getLatLng()));
        else if (mDestination != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mDestination
                    .getLatLng()));
    }

    public void setTripOrigin(MyLocation origin) {
        clearBusOverlays();
        clearRouteShapePolylines();
        removeLocationSelection();
        boolean shouldAdd = true;
        // remove the previously selected marker
        if (mMarkerOrigin != null) {
            if (mOrigin != null && !mOrigin.equals(origin)) {
                mMarkerOrigin.remove();
                clearBusOverlays();
                clearRouteShapePolylines();
            } else
                shouldAdd = false;
        }
        mOrigin = origin;
        if (shouldAdd) {
            // add the newly selected marker
            mMarkerOrigin = mMap
                    .addMarker(new MarkerOptions()
                            .position(origin.getLatLng())
                            .title(origin.getName())
                            .snippet(origin.getId())
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(getOriginDestinationMarkerHue(true))));
            mMarkerOrigin.setAlpha(0.0f);
            ObjectAnimator.ofFloat(mMarkerOrigin, "alpha", 0.0f, 1.f)
                    .setDuration(300).start();
        }
        animateToOriginAndDestination();
    }

    public boolean removeTripOrigin() {
        boolean b = mMarkerOrigin != null || mOrigin != null;
        if (mMarkerOrigin != null)
            mMarkerOrigin.remove();
        mMarkerOrigin = null;
        mOrigin = null;
        animateToOriginAndDestination();
        return b;
    }

    public void setTripDestination(MyLocation destination) {
        clearBusOverlays();
        clearRouteShapePolylines();
        removeLocationSelection();
        boolean shouldAdd = true;
        // remove the previously selected marker
        if (mMarkerDestination != null) {
            if (mDestination != null && !mDestination.equals(destination)) {
                mMarkerDestination.remove();
                clearBusOverlays();
                clearRouteShapePolylines();
            } else
                shouldAdd = false;
        }
        mDestination = destination;
        if (shouldAdd) {
            // add the newly selected marker
            mMarkerDestination = mMap
                    .addMarker(new MarkerOptions()
                            .position(destination.getLatLng())
                            .title(destination.getName())
                            .snippet(destination.getId())
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(getOriginDestinationMarkerHue(false))));
            mMarkerDestination.setAlpha(0.0f);
            ObjectAnimator.ofFloat(mMarkerDestination, "alpha", 0.0f, 1.f)
                    .setDuration(300).start();
        }
        animateToOriginAndDestination();
    }

    public boolean removeTripDestination() {
        boolean b = mMarkerDestination != null || mDestination != null;
        if (mMarkerDestination != null)
            mMarkerDestination.remove();
        mMarkerDestination = null;
        mDestination = null;
        animateToOriginAndDestination();
        return b;
    }

    public void setOnLocationSelectedListener(
            MyFragmentsListener.OnLocationSelectedListener listener) {
        mLocationSelectedListener = listener;
    }

    private void dispatchOnLocationSelected(Uri location) {
        if (mLocationSelectedListener != null)
            mLocationSelectedListener.onLocationSelected(location);
        else
            Log.e(TAG, "dispatchOnLocationselected listener null");
    }

    public void setOnRemoveSelectedLocationListener(
            OnRemoveSelectedLocationListener listener) {
        mRemoveSelectedLocationListener = listener;
    }

    private void dispatchOnRemoveSelectedLocation() {
        if (mRemoveSelectedLocationListener != null)
            mRemoveSelectedLocationListener.onRemoveSelectedLocationListener();
        else
            Log.e(TAG, "dispatchOnRemoveSelectedLocation listener null");
    }

    public void setOnRequestTripPlannerVisibility(
            OnRequestFragmentVisibilityListener listener) {
        mRequestTripPlanVisListener = listener;
    }

    private boolean requestShouldUpdateLocation() {
        if (mRequestTripPlanVisListener != null)
            return mRequestTripPlanVisListener.onRequestShouldUpdateLocation();
        Log.e(TAG, "requestTripPlannerVisibility listener null");
        return true;
    }

    public void setOnRequestPanelInfoListener(
            OnRequestPanelInfoListener listener) {
        mRequestPanelInfoListener = listener;
    }

    private int requestPanelHeight() {
        if (mRequestPanelInfoListener != null)
            return mRequestPanelInfoListener.onRequestPanelHeight();
        Log.e(TAG, "requestPanelHeight listener null");
        return 0;
    }

    private Uri requestPanelLocation() {
        if (mRequestPanelInfoListener != null)
            return mRequestPanelInfoListener.onRequestPanelLocation();
        Log.e(TAG, "requestPanelLocation listener null");
        return null;
    }

    /**
     * Displays the itinerary as polylines for the legs and markers for the in
     * between points. Sets up info windows describing what to do at each point.
     *
     * @param itinerary
     */
    public void setItinerary(Itinerary itinerary) {
        // only consume if the itineraries are not equal
        if (itinerary != null && itinerary.equals(mItinerary)) {
            return;
        }

        mItinerary = (Itinerary) Util.copyParcelable(itinerary);

        clearItineraryMarkersAndPolylines();

        mItineraryMarkers = new ArrayList<Marker>();

        mItineraryPolylines = new ArrayList<Polyline>();

        int width = Util.getPixelsFromDips(getActivity(),
                DEFAULT_POLYLINE_WIDTH_DP);

        int legCount = itinerary.getLegs().size();
        int i = 0;
        // Add the route shapes, increasing the polyline width as you go.
        for (Leg leg : itinerary.getLegs()) {
            // Add markers for the leg endpoints
            if (i < legCount - 1) {
                LegEndpoint end = leg.getEnd();
                Marker markerEnd = mMap.addMarker((new MarkerOptions())
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.ic_leg_circle))
                        .position(end.getLatLng()).anchor(0.49f, 0.5f));
                mItineraryMarkers.add(markerEnd);
            }

            // Add polylines for the legs
            if (leg instanceof ServiceLeg) {
                ServiceLeg sLeg = (ServiceLeg) leg;

                addRouteShapeOnServiceLeg(sLeg, width);
            } else if (leg instanceof WalkLeg) {
                Polyline line = addPolylineBetweenLegEndpoints(leg, width);
                mItineraryPolylines.add(line);
                // TODO
            }

            width += 2;
            i++;
        }

    }

    /**
     * Adds a polyline between the endpoints of this leg. Makes the alpha
     * channel #AA
     *
     * @param leg
     * @param width
     * @return
     */
    private Polyline addPolylineBetweenLegEndpoints(Leg leg, int width) {
        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(leg.getBegin().getLatLng());
        latLngs.add(leg.getEnd().getLatLng());
        // add just a straight line between the two points
        Polyline line = drawPolyLine(latLngs,
                Color.parseColor("#AA" + leg.getMainColor()), width);
        return line;
    }

    private void clearItineraryMarkersAndPolylines() {
        // clear the old stuff
        if (mItineraryMarkers != null) {
            for (Marker marker : mItineraryMarkers) {
                marker.remove();
            }
            mItineraryMarkers.clear();
        }
        if (mItineraryPolylines != null) {
            for (Polyline polyline : mItineraryPolylines)
                polyline.remove();
            mItineraryPolylines.clear();
        }
    }

    public void removeItinerary() {
        clearItineraryMarkersAndPolylines();
        mItinerary = null;
    }

    private SelectionAndSelArgs getSelection(CameraPosition position) {
        // TODO finish for all cases.
        String selection;
        if (SHOW_FAV_STOPS && SHOW_FAV_STOPS
                && position.zoom >= MIN_ZOOM_FOR_NONFAVS && SHOW_FAV_STOPS
                && SHOW_NONFAV_PLACES) {
            // Show everything
            selection = null;
        } else if (SHOW_FAV_STOPS && SHOW_FAV_STOPS
                && position.zoom >= MIN_ZOOM_FOR_NONFAVS && SHOW_FAV_STOPS
                && SHOW_NONFAV_PLACES) {
            // Show everything
        }
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                Uri uri = MyLocationContract.MyLocation.CONTENT_URI;

                boolean added = false;
                CameraPosition position = args.getParcelable("cameraPosition");
                ArrayList<String> selArgs = new ArrayList<String>();
                String selection = "";
                if (SHOW_FAV_PLACES) {
                    if (added)
                        selection += " OR ";
                    selection += "((" + MyLocationContract.MyLocation.TYPE + "="
                            + MyLocationContract.LocationTypeCode.GOOGLE_PLACE
                            + " OR " + MyLocationContract.MyLocation.TYPE + "="
                            + MyLocationContract.LocationTypeCode.USER_PLACE
                            + ") AND " + MyLocationContract.MyLocation.FAVORITE
                            + "=1" + ")";
                    added = true;
                }
                if (SHOW_NONFAV_PLACES && position.zoom >= MIN_ZOOM_FOR_NONFAVS) {
                    if (added)
                        selection += " OR ";
                    selection += "((" + MyLocationContract.MyLocation.TYPE + "="
                            + MyLocationContract.LocationTypeCode.GOOGLE_PLACE
                            + " OR " + MyLocationContract.MyLocation.TYPE + "="
                            + MyLocationContract.LocationTypeCode.USER_PLACE
                            + ") AND " + MyLocationContract.MyLocation.FAVORITE
                            + "=0" + ")";
                    added = true;
                }
                if (SHOW_FAV_STOPS) {
                    if (added)
                        selection += " OR ";
                    selection += "((" + MyLocationContract.MyLocation.TYPE + "="
                            + MyLocationContract.LocationTypeCode.STOP + " OR "
                            + MyLocationContract.MyLocation.TYPE + "="
                            + MyLocationContract.LocationTypeCode.STOPPOINT
                            + ") AND " + MyLocationContract.MyLocation.FAVORITE
                            + "=1" + ")";
                    added = true;
                }
                if (SHOW_NONFAV_STOPS && position.zoom >= MIN_ZOOM_FOR_NONFAVS) {
                    if (added)
                        selection += " OR ";
                    if (position.zoom >= MIN_ZOOM_FOR_STOPPOINTS)
                        selection += "(" + MyLocationContract.MyLocation.TYPE + "="
                                + MyLocationContract.LocationTypeCode.STOPPOINT
                                + " AND " + MyLocationContract.MyLocation.FAVORITE
                                + "=0" + ")";
                    else
                        selection += "(" + MyLocationContract.MyLocation.TYPE + "="
                                + MyLocationContract.LocationTypeCode.STOP
                                + " AND " + MyLocationContract.MyLocation.FAVORITE
                                + "=0" + ")";
                    added = true;
                }

                // add the latLng requirements
                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                final double latTop = bounds.northeast.latitude;
                final double latBot = bounds.southwest.latitude;
                final double lonLeft = bounds.southwest.longitude;
                final double lonRight = bounds.northeast.longitude;
                selection += " AND " + MyLocationContract.MyLocation.LAT
                        + " BETWEEN ? AND ? AND "
                        + MyLocationContract.MyLocation.LON + " BETWEEN ? AND ?";
                selArgs.add("" + latBot);
                selArgs.add("" + latTop);
                selArgs.add("" + lonLeft);
                selArgs.add("" + lonRight);

                CursorLoader cursorLoader = new CursorLoader(getActivity(), uri,
                        null, selection,
                        selArgs.toArray(new String[selArgs.size()]),
                        MyLocationContract.MyLocation.DATA_ID + " ASC");
                return cursorLoader;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID:
                mCursor = data;
                // otherwise compare the cursors, delete old markers, add new
                // markers.
                updateMarkersFromCursor(mCursor);
                break;
        }
    }

    private void updateMarkersFromCursor(Cursor newCursor) {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        List<String> urisInCursor = new ArrayList<String>();
        Uri uri;
        // add new in bounds ones
        if (newCursor.moveToFirst()) {
            while (!newCursor.isAfterLast()) {
                uri = Uri
                        .withAppendedPath(
                                MyLocationContentProvider
                                        .getUriFromType(newCursor.getInt(newCursor
                                                .getColumnIndex(MyLocationContract.MyLocation.TYPE))),
                                ""
                                        + newCursor.getInt(newCursor
                                        .getColumnIndex(MyLocationContract.MyLocation.DATA_ID))
                        );
                int type = newCursor.getInt(newCursor
                        .getColumnIndex(MyLocationContract.MyLocation.TYPE));
                int favoriteValue = newCursor
                        .getInt(newCursor
                                .getColumnIndex(MyLocationContract.MyLocation.FAVORITE));
                // only add when the map doesn't have the marker
                if (!mMarkerByUri.containsKey(uri.toString())) {
                    double lat = newCursor.getDouble(newCursor
                            .getColumnIndex(MyLocationContract.MyLocation.LAT));
                    double lon = newCursor.getDouble(newCursor
                            .getColumnIndex(MyLocationContract.MyLocation.LON));
                    // TODO add correct icon and anchor
                    addMarkerToMap(uri, new LatLng(lat, lon),
                            MyLocationContract.getLocationTypeMapDrawableResId(
                                    type, favoriteValue == 1), new float[]{
                                    0.5f, 0.5f}, favoriteValue == 1
                    );
                } else {
                    Marker marker = mMarkerByUri.get(uri.toString());
                    String snippet = marker.getSnippet();
                    int fav = Integer.parseInt(snippet);
                    if (favoriteValue != fav) {
                        // remove and replace the marker to update its icon
                        marker.remove();
                        double lat = newCursor
                                .getDouble(newCursor
                                        .getColumnIndex(MyLocationContract.MyLocation.LAT));
                        double lon = newCursor
                                .getDouble(newCursor
                                        .getColumnIndex(MyLocationContract.MyLocation.LON));
                        // TODO add correct icon and anchor
                        addMarkerToMap(uri, new LatLng(lat, lon),
                                MyLocationContract
                                        .getLocationTypeMapDrawableResId(type,
                                                favoriteValue == 1),
                                new float[]{0.5f, 0.5f}, favoriteValue == 1
                        );
                    }
                }
                urisInCursor.add(uri.toString());

                newCursor.moveToNext();
            }
        }

        List<String> urisToRemove = new ArrayList<String>();
        // Remove out of bounds ones
        for (String uriInMap : mMarkerByUri.keySet()) {
            Marker marker = mMarkerByUri.get(uriInMap);
            if (marker != null
                    && (!urisInCursor.contains(uriInMap) || !isInBounds(bounds,
                    marker))) {
                marker.remove();
                urisToRemove.add(uriInMap);
            }
        }
        for (String uriToRemove : urisToRemove) {
            mMarkerByUri.remove(uriToRemove);
        }
    }

    /**
     * Returns true if the marker is within the bounds, false otherwise.
     *
     * @param bounds
     * @param marker
     * @return
     */
    private boolean isInBounds(LatLngBounds bounds, Marker marker) {
        final double latTop = bounds.northeast.latitude;
        final double latBot = bounds.southwest.latitude;
        final double lonLeft = bounds.southwest.longitude;
        final double lonRight = bounds.northeast.longitude;
        LatLng position = marker.getPosition();
        return position.latitude < latTop && position.latitude > latBot
                && position.longitude > lonLeft
                && position.longitude < lonRight;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        switch (loader.getId()) {
            case LOADER_ID:
                // data is not available anymore, delete reference
                for (String uri : mMarkerByUri.keySet()) {
                    Marker marker = mMarkerByUri.get(uri);
                    marker.remove();
                }
                mMarkerByUri.clear();
                mCursor = null;
                break;
        }
    }

    public interface OnRemoveSelectedLocationListener {
        public void onRemoveSelectedLocationListener();
    }

    private static class SelectionAndSelArgs {
        private String selection;
        private String[] selArgs;

        public SelectionAndSelArgs(String selection, String[] selArgs) {
            this.selection = selection;
            this.selArgs = selArgs;
        }
    }

    private class LocationInfoWindowAdapter implements
            GoogleMap.InfoWindowAdapter {

        LayoutInflater inflater = null;

        LocationInfoWindowAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (marker == null)
                return null;
            // is it a bus marker?
            if (marker.getSnippet().contains("min to")) {
                View popup = inflater.inflate(R.layout.bus_info_window, null);
                TextView tv = (TextView) popup
                        .findViewById(R.id.busInfoWindowTitle);
                tv.setText(marker.getTitle());
                tv = (TextView) popup.findViewById(R.id.busInfoWindowSnippet);
                tv.setText(marker.getSnippet());
                return popup;
            }
            if (!mMarkerByUri.containsKey(marker.getTitle()))
                return null;

            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // return null so the default info-window frame will be used.
            return null;
        }

    }
}
