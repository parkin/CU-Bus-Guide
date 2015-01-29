package com.teamparkin.mtdapp.contentproviders;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.LocationTypeCode;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.StopPoints;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.Stops;
import com.teamparkin.mtdapp.databases.AbstractStopTable;
import com.teamparkin.mtdapp.databases.FavoritesTable;
import com.teamparkin.mtdapp.databases.GooglePlacesTable;
import com.teamparkin.mtdapp.databases.MyLocationDatabaseHelper;
import com.teamparkin.mtdapp.databases.MyLocationTable;
import com.teamparkin.mtdapp.databases.PlacesTable;
import com.teamparkin.mtdapp.databases.StopPointsTable;
import com.teamparkin.mtdapp.databases.StopsTable;
import com.teamparkin.mtdapp.databases.UserPlacesTable;
import com.teamparkin.mtdapp.dataclasses.GooglePlace;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.dataclasses.Place;
import com.teamparkin.mtdapp.dataclasses.Stop;
import com.teamparkin.mtdapp.dataclasses.StopPoint;
import com.teamparkin.mtdapp.dataclasses.UserPlace;
import com.teamparkin.mtdapp.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyLocationContentProvider extends ContentProvider {
    private static final String TAG = MyLocationContentProvider.class
            .getSimpleName();
    // used for the UriMacher
    private static final int STOPS = 100;
    private static final int STOPS_ID = 101;
    private static final int STOPPOINTS = 200;
    private static final int STOPPOINTS_ID = 201;
    private static final int ABSTRACT_STOPS = 300;
    private static final int ABSTRACT_STOPS_ID = 301;
    private static final int PLACES = 400;
    private static final int PLACES_ID = 401;
    private static final int GOOGLE_PLACES = 500;
    private static final int GOOGLE_PLACES_ID = 501;
    private static final int GOOGLE_PLACES_COUNT = 502;
    private static final int USER_PLACES = 600;
    private static final int USER_PLACES_ID = 601;
    private static final int LOCATIONS = 700;
    private static final int LOCATIONS_ID = 701;
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        // does order matter here?
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_LOCATIONS, LOCATIONS);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_ABSTRACT_STOPS, ABSTRACT_STOPS);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_STOPS, STOPS);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_STOPPOINTS, STOPPOINTS);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_PLACES, PLACES);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_GOOGLE_PLACES, GOOGLE_PLACES);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_GOOGLE_PLACES_COUNT, GOOGLE_PLACES_COUNT);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_USER_PLACES, USER_PLACES);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_STOPS + "/#", STOPS_ID);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_STOPPOINTS + "/#", STOPPOINTS_ID);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_GOOGLE_PLACES + "/#",
                GOOGLE_PLACES_ID);
        sURIMatcher
                .addURI(MyLocationContract.AUTHORITY,
                        MyLocationContract.BASE_PATH_USER_PLACES + "/#",
                        USER_PLACES_ID);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_ABSTRACT_STOPS + "/#",
                ABSTRACT_STOPS_ID);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_PLACES + "/#", PLACES_ID);
        sURIMatcher.addURI(MyLocationContract.AUTHORITY,
                MyLocationContract.BASE_PATH_LOCATIONS + "/#", LOCATIONS_ID);
    }

    private static final ProjectionMap sMyLocationProjectionMap = ProjectionMap
            .builder().add(MyLocationContract.MyLocation.DATA_ID)
            .add(MyLocationContract.MyLocation.ID)
            .add(MyLocationContract.MyLocation.LAT)
            .add(MyLocationContract.MyLocation.LON)
            .add(MyLocationContract.MyLocation.NAME)
            .add(MyLocationContract.MyLocation.TYPE)
            .add(MyLocationContract.MyLocation.SNIPPET)
            .add(MyLocationContract.MyLocation.FAVORITE)
            .add(MyLocationContract.MyLocation.TIMESTAMP).build();

    /* Define projection maps **************************** */
    private static final ProjectionMap sAbstractStopsProjectionMap = ProjectionMap
            .builder().addAll(sMyLocationProjectionMap)
            .add(MyLocationContract.AbstractStops.CODE).build();
    private static final ProjectionMap sStopsProjectionMap = ProjectionMap
            .builder().addAll(sAbstractStopsProjectionMap).build();
    private static final ProjectionMap sStopPointsProjectionMap = ProjectionMap
            .builder().addAll(sAbstractStopsProjectionMap).build();
    private static final ProjectionMap sPlacesProjectionMap = ProjectionMap
            .builder().addAll(sMyLocationProjectionMap).build();
    /**
     * A projection map containing solely the google_places table columns,
     * without the row_id column.
     */
    private static final ProjectionMap sGooglePlacesSoloNoIdProjectionMap = ProjectionMap
            .builder().add(MyLocationContract.GooglePlaces.EVENTS)
            .add(MyLocationContract.GooglePlaces.FORMATTED_ADDRESS)
            .add(MyLocationContract.GooglePlaces.ICON)
            .add(MyLocationContract.GooglePlaces.OPENING_HOURS)
            .add(MyLocationContract.GooglePlaces.PHOTOS)
            .add(MyLocationContract.GooglePlaces.PRICE_LEVEL)
            .add(MyLocationContract.GooglePlaces.RATING)
            .add(MyLocationContract.GooglePlaces.REFERENCE)
            .add(MyLocationContract.GooglePlaces.TYPES)
            .add(MyLocationContract.GooglePlaces.VICINITY).build();
    /**
     * A ProjectionMap containing solely the columns in google_places_detail,
     * without the row_id.
     */
    private static final ProjectionMap sGooglePlacesDetailsSoloNoIdProjectionMap = ProjectionMap
            .builder()
            .add(MyLocationContract.GooglePlaces.DetailsColumns.ADDRESS_COMPONENTS)
            .add(MyLocationContract.GooglePlaces.DetailsColumns.FORMATTED_PHONE_NUMBER)
            .add(MyLocationContract.GooglePlaces.DetailsColumns.INTERNATIONAL_PHONE_NUMBER)
            .add(MyLocationContract.GooglePlaces.DetailsColumns.REVIEWS)
            .add(MyLocationContract.GooglePlaces.DetailsColumns.TYPES)
            .add(MyLocationContract.GooglePlaces.DetailsColumns.UTD_OFFSET)
            .add(MyLocationContract.GooglePlaces.DetailsColumns.WEBSITE)
            .build();
    /**
     * A ProjectionMap with all of the columns in mylocation (_id from here),
     * place, google_places, and google_places_details.
     */
    private static final ProjectionMap sGooglePlacesProjectionMap = ProjectionMap
            .builder().addAll(sPlacesProjectionMap)
            .addAll(sGooglePlacesSoloNoIdProjectionMap)
            .addAll(sGooglePlacesDetailsSoloNoIdProjectionMap).build();
    private static final ProjectionMap sUserPlacesProjectionMap = ProjectionMap
            .builder().addAll(sPlacesProjectionMap)
            .add(MyLocationContract.UserPlaces.COMMENT).build();
    // Databases
    private MyLocationDatabaseHelper mLocationDb;

    /**
     * Returns a MyLocation object made from the first row. Note that this
     * cursor must have all of the available columns in it (can get by passing
     * null to projection). It must also be pointed at an available row, so call
     * c.moveToFirst before passing if you have to.
     *
     * @param c
     * @return
     */
    public static MyLocation buildLocationFromCursor(Cursor c) {
        MyLocation loc = null;
        if (c != null) {
            int type = c.getInt(c
                    .getColumnIndex(MyLocationContract.MyLocation.TYPE));
            switch (type) {
                case MyLocationContract.LocationTypeCode.STOP:
                    loc = buildStopFromCursor(c);
                    break;
                case MyLocationContract.LocationTypeCode.STOPPOINT:
                    loc = buildStopPointFromCursor(c);
                    break;
                case MyLocationContract.LocationTypeCode.GOOGLE_PLACE:
                    loc = buildGooglePlaceFromCursor(c);
                    break;
                case MyLocationContract.LocationTypeCode.USER_PLACE:
                    loc = buildUserPlaceFromCursor(c);
                default:
                    throw new IllegalArgumentException(
                            "Cursor does not have correct KEY_TYPE column or value");
            }
        }
        return loc;
    }

    /**
     * Note that his calls moveToFirst, so your cursor will not be at the same
     * position after calling this.
     *
     * @param c
     * @return
     */
    public static List<MyLocation> buildLocationListFromCursor(Cursor c) {
        List<MyLocation> locs = new ArrayList<MyLocation>();
        if (c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                locs.add(buildLocationFromCursor(c));
                c.moveToNext();
            }
        }
        return locs;
    }

    /**
     * Note this calls c.moveToFirst, your cursor position will be messed up
     * calling this.
     *
     * @param c
     * @return
     */
    public static List<Stop> buildStopListFromCursor(Cursor c) {
        List<Stop> stops = new ArrayList<Stop>();
        if (c.moveToFirst()) {
            do {
                stops.add(buildStopFromCursor(c));
            } while (c.moveToNext());
        }
        return stops;
    }

    public static Stop buildStopFromCursor(Cursor c) {
        String name = c.getString(c
                .getColumnIndex(MyLocationContract.Stops.NAME));
        String id = c.getString(c.getColumnIndex(MyLocationContract.Stops.ID));
        String code = c.getString(c
                .getColumnIndex(MyLocationContract.Stops.CODE));
        double lat = c
                .getDouble(c.getColumnIndex(MyLocationContract.Stops.LAT));
        double lng = c
                .getDouble(c.getColumnIndex(MyLocationContract.Stops.LON));
        long rowId = c.getLong(c
                .getColumnIndex(MyLocationContract.Stops.DATA_ID));
        return new Stop(id, name, new LatLng(lat, lng), code, rowId);
    }

    public static List<StopPoint> buildStopPointListFromCursor(Cursor c) {
        List<StopPoint> stopPoints = new ArrayList<StopPoint>();
        if (c.moveToFirst()) {
            do {
                stopPoints.add(buildStopPointFromCursor(c));
            } while (c.moveToNext());
        }
        return stopPoints;
    }

    public static StopPoint buildStopPointFromCursor(Cursor c) {
        String name = c.getString(c
                .getColumnIndex(MyLocationContract.StopPoints.NAME));
        String id = c.getString(c
                .getColumnIndex(MyLocationContract.StopPoints.ID));
        String code = c.getString(c
                .getColumnIndex(MyLocationContract.StopPoints.CODE));
        double lat = c.getDouble(c
                .getColumnIndex(MyLocationContract.StopPoints.LAT));
        double lng = c.getDouble(c
                .getColumnIndex(MyLocationContract.StopPoints.LON));
        long rowId = c.getLong(c
                .getColumnIndex(MyLocationContract.Stops.DATA_ID));
        return new StopPoint(id, lat, lng, name, code, rowId);
    }

    public static List<UserPlace> buildUserPlaceListFromCursor(Cursor c) {
        List<UserPlace> userPlaces = new ArrayList<UserPlace>();
        if (c.moveToFirst()) {
            do {
                userPlaces.add(buildUserPlaceFromCursor(c));
            } while (c.moveToNext());
        }
        return userPlaces;
    }

    public static UserPlace buildUserPlaceFromCursor(Cursor c) {
        String name = c.getString(c
                .getColumnIndex(MyLocationContract.UserPlaces.NAME));
        String id = c.getString(c
                .getColumnIndex(MyLocationContract.UserPlaces.ID));
        String comment = c.getString(c
                .getColumnIndex(MyLocationContract.UserPlaces.COMMENT));
        double lat = c.getDouble(c
                .getColumnIndex(MyLocationContract.UserPlaces.LAT));
        double lng = c.getDouble(c
                .getColumnIndex(MyLocationContract.UserPlaces.LON));
        long rowId = c.getLong(c
                .getColumnIndex(MyLocationContract.Stops.DATA_ID));
        return new UserPlace(name, id, new LatLng(lat, lng), comment, rowId);
    }

    public static List<Place> buildPlaceListFromCursor(Cursor c) {
        List<Place> googlePlaces = new ArrayList<Place>();
        if (c.moveToFirst()) {
            do {
                googlePlaces.add(buildPlaceFromCursor(c));
            } while (c.moveToNext());
        }
        return googlePlaces;
    }

    public static Place buildPlaceFromCursor(Cursor c) {
        Place loc = null;
        if (c != null) {
            int type = c.getInt(c
                    .getColumnIndex(MyLocationContract.MyLocation.TYPE));
            Log.i(TAG, "buildPlace type: " + type);
            switch (type) {
                case MyLocationContract.LocationTypeCode.GOOGLE_PLACE:
                    loc = buildGooglePlaceFromCursor(c);
                    break;
                case MyLocationContract.LocationTypeCode.USER_PLACE:
                    loc = buildUserPlaceFromCursor(c);
                default:
                    throw new IllegalArgumentException(
                            "Cursor does not have correct KEY_TYPE column or value");
            }
        }
        return loc;
    }

    public static List<GooglePlace> buildGooglePlaceListFromCursor(Cursor c) {
        List<GooglePlace> googlePlaces = new ArrayList<GooglePlace>();
        if (c.moveToFirst()) {
            do {
                googlePlaces.add(buildGooglePlaceFromCursor(c));
            } while (c.moveToNext());
        }
        return googlePlaces;
    }

    public static GooglePlace buildGooglePlaceFromCursor(Cursor c) {
        GooglePlace.Builder builder = new GooglePlace.Builder();

        // Set MyLocation stuff
        String name = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.NAME));
        String id = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.ID));
        double lat = c.getDouble(c
                .getColumnIndex(MyLocationContract.GooglePlaces.LAT));
        double lng = c.getDouble(c
                .getColumnIndex(MyLocationContract.GooglePlaces.LON));
        builder.setName(name);
        builder.setId(id);
        builder.setLatLng(new LatLng(lat, lng));

        // Set Place stuff

        // Set GooglePlace stuff
        String events = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.EVENTS));
        String formattedAddress = c
                .getString(c
                        .getColumnIndex(MyLocationContract.GooglePlaces.FORMATTED_ADDRESS));
        String icon = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.ICON));
        String openingHours = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.OPENING_HOURS));
        String photos = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.PHOTOS));
        int priceLevel = c.getInt(c
                .getColumnIndex(MyLocationContract.GooglePlaces.PRICE_LEVEL));
        double rating = c.getDouble(c
                .getColumnIndex(MyLocationContract.GooglePlaces.RATING));
        String reference = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.REFERENCE));
        String types = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.TYPES));
        String vicinity = c.getString(c
                .getColumnIndex(MyLocationContract.GooglePlaces.VICINITY));

        long rowId = c.getLong(c
                .getColumnIndex(MyLocationContract.Stops.DATA_ID));

        builder.setEvents(events);
        builder.setFormattedAddress(formattedAddress);
        builder.setIcon(icon);
        builder.setOpeningHours(openingHours);
        builder.setPhotos(photos);
        builder.setPriceLevel(priceLevel);
        builder.setRating(rating);
        builder.setReference(reference);
        builder.setTypes(types);
        builder.setVicinity(vicinity);
        builder.setRowId(rowId);

        return builder.build();
    }

    /**
     * Adds the place.
     *
     * @param context
     * @param place
     * @param timestamp
     */
    public static void addGooglePlace(Context context, GooglePlace place,
                                      long timestamp) {
        ContentValues cv = new ContentValues();
        cv.put(MyLocationTable.Columns.ID, place.getId());
        cv.put(MyLocationTable.Columns.LAT, place.getLatLng().latitude);
        cv.put(MyLocationTable.Columns.LON, place.getLatLng().longitude);
        cv.put(MyLocationTable.Columns.NAME, place.getName());
        cv.put(MyLocationTable.Columns.TYPE,
                MyLocationContract.LocationTypeCode.GOOGLE_PLACE);

        cv.put(GooglePlacesTable.Columns.EVENTS, place.getEvents());
        cv.put(GooglePlacesTable.Columns.FORMATTED_ADDRESS,
                place.getFormattedAddress());
        cv.put(GooglePlacesTable.Columns.ICON, place.getIcon());
        cv.put(GooglePlacesTable.Columns.OPENING_HOURS, place.getOpeningHours());
        cv.put(GooglePlacesTable.Columns.PHOTOS, place.getPhotos());
        cv.put(GooglePlacesTable.Columns.PRICE_LEVEL, place.getPriceLevel());
        cv.put(GooglePlacesTable.Columns.RATING, place.getRating());
        cv.put(GooglePlacesTable.Columns.REFERENCE, place.getReference());
        cv.put(GooglePlacesTable.Columns.TYPES, place.getTypes());
        cv.put(GooglePlacesTable.Columns.VICINITY, place.getVicinity());
        // TODO fix
        // cv.put(PlacesTable.KEY_TIMESTAMP, timestamp);

        context.getContentResolver().insert(
                MyLocationContract.GooglePlaces.CONTENT_URI, cv);
    }

    /**
     * Deletes the place by id. Returns the number of rows affected.
     *
     * @param context
     * @param location
     * @return
     */
    public static int deleteLocation(Context context, MyLocation location) {
        Uri uri = Uri
                .withAppendedPath(
                        com.teamparkin.mtdapp.contentproviders.MyLocationContract.MyLocation.CONTENT_URI,
                        location.getId());
        return context.getContentResolver().delete(uri, null, null);
    }

    public static void setFavorite(Context context, int id, boolean favorite) {
        Uri uri = Uri.withAppendedPath(
                MyLocationContract.MyLocation.CONTENT_URI, "" + id);
        ContentValues cv = new ContentValues();
        cv.put(MyLocationContract.MyLocation.FAVORITE, favorite ? 1 : 0);
        context.getContentResolver().update(uri, cv, null, null);
    }

    public static boolean isFavorite(Context context, String id) {
        Uri uri = MyLocationContract.MyLocation.CONTENT_URI;
        Cursor c = context.getContentResolver().query(uri, null,
                MyLocationContract.MyLocation.ID + " = ?", new String[]{id},
                null);
        boolean favorite = false;
        if (c.moveToFirst())
            favorite = true;
        c.close();
        return favorite;
    }

    public static boolean isFavorite(Context context, MyLocation location) {
        return isFavorite(context, location.getId());
    }

    public static Cursor getAllNonFavoritePlacesOnScreen(Context context,
                                                         LatLngBounds bounds) {
        final double latTop = bounds.northeast.latitude;
        final double latBot = bounds.southwest.latitude;
        final double lonLeft = bounds.southwest.longitude;
        final double lonRight = bounds.northeast.longitude;
        Uri uri = MyLocationContract.Places.CONTENT_URI;
        Cursor csp = context.getContentResolver().query(
                uri,
                null,
                MyLocationContract.Places.FAVORITE + " != ? AND "
                        + MyLocationContract.Places.LAT + " <? AND "
                        + MyLocationContract.Places.LAT + " >? AND "
                        + MyLocationContract.Places.LON + " >? AND "
                        + MyLocationContract.Places.LON + " <?",
                new String[]{"" + 1, "" + latTop, "" + latBot, "" + lonLeft,
                        "" + lonRight},
                MyLocationContract.Places.NAME + " COLLATE NOCASE");
        return csp;
    }

    public static Cursor getAllNonFavoriteStopsOnScreen(Context context,
                                                        LatLngBounds bounds) {
        final double latTop = bounds.northeast.latitude;
        final double latBot = bounds.southwest.latitude;
        final double lonLeft = bounds.southwest.longitude;
        final double lonRight = bounds.northeast.longitude;
        Uri uri = Stops.CONTENT_URI;
        Cursor csp = context.getContentResolver().query(
                uri,
                null,
                MyLocationContract.Stops.FAVORITE + " != ? AND "
                        + MyLocationContract.Stops.LAT + " <? AND "
                        + MyLocationContract.Stops.LAT + " >? AND "
                        + MyLocationContract.Stops.LON + " >? AND "
                        + MyLocationContract.Stops.LON + " <?",
                new String[]{"" + 1, "" + latTop, "" + latBot, "" + lonLeft,
                        "" + lonRight},
                MyLocationContract.Stops.NAME + " COLLATE NOCASE");
        return csp;
    }

    public static Cursor getAllNonFavoriteStopPointsOnScreen(Context context,
                                                             LatLngBounds bounds) {
        final double latTop = bounds.northeast.latitude;
        final double latBot = bounds.southwest.latitude;
        final double lonLeft = bounds.southwest.longitude;
        final double lonRight = bounds.northeast.longitude;
        Uri uri = StopPoints.CONTENT_URI;
        Cursor csp = context.getContentResolver().query(
                uri,
                null,
                MyLocationContract.StopPoints.FAVORITE + " != ? AND "
                        + MyLocationContract.StopPoints.LAT + " <? AND "
                        + MyLocationContract.StopPoints.LAT + " >? AND "
                        + MyLocationContract.StopPoints.LON + " >? AND "
                        + MyLocationContract.StopPoints.LON + " <?",
                new String[]{"" + 1, "" + latTop, "" + latBot, "" + lonLeft,
                        "" + lonRight},
                MyLocationContract.StopPoints.NAME + " COLLATE NOCASE");
        return csp;
    }

    // **** Below we have some static helper methods for classes.

    public static List<MyLocation> getAllFavoriteLocations(Context context) {
        Cursor cursor = context
                .getContentResolver()
                .query(com.teamparkin.mtdapp.contentproviders.MyLocationContract.MyLocation.CONTENT_URI,
                        null, MyLocationContract.MyLocation.FAVORITE + " = ?",
                        new String[]{"" + 1},
                        MyLocationContract.MyLocation.NAME + " ASC");
        List<MyLocation> locs = buildLocationListFromCursor(cursor);
        cursor.close();
        return locs;
    }

    /**
     * Returns the content uri of the given type code.
     *
     * @param type
     * @return
     */
    public static Uri getUriFromType(int type) {
        switch (type) {
            case LocationTypeCode.STOP:
                return MyLocationContract.Stops.CONTENT_URI;
            case LocationTypeCode.STOPPOINT:
                return MyLocationContract.StopPoints.CONTENT_URI;
            case LocationTypeCode.GOOGLE_PLACE:
                return MyLocationContract.GooglePlaces.CONTENT_URI;
            case LocationTypeCode.USER_PLACE:
                return MyLocationContract.UserPlaces.CONTENT_URI;
        }
        return null;
    }

    public static Uri getUriFromCursor(Cursor c) {
        int type = c.getInt(c
                .getColumnIndex(MyLocationContract.MyLocation.TYPE));
        long rowId = c.getLong(c
                .getColumnIndex(MyLocationContract.MyLocation.DATA_ID));
        return Uri.withAppendedPath(getUriFromType(type), "" + rowId);
    }

    @Override
    public boolean onCreate() {
        mLocationDb = MyLocationDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int uriType = sURIMatcher.match(uri);

        Cursor c;

        SQLiteDatabase db = mLocationDb.getReadableDatabase();
        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType) {
            case LOCATIONS:
                queryBuilder.setProjectionMap(sMyLocationProjectionMap);
                queryBuilder.setTables(MyLocationDatabaseHelper.Views.MYLOCATIONS);
                break;

            case ABSTRACT_STOPS:
                queryBuilder.setProjectionMap(sAbstractStopsProjectionMap);
                queryBuilder
                        .setTables(MyLocationDatabaseHelper.Views.ABSTRACT_STOPS);
                break;

            case STOPS_ID:
                queryBuilder.appendWhere(MyLocationContract.Stops.DATA_ID + "="
                        + uri.getLastPathSegment());
            case STOPS:
                queryBuilder.setProjectionMap(sStopsProjectionMap);
                queryBuilder.setTables(MyLocationDatabaseHelper.Views.STOPS);
                break;

            case STOPPOINTS_ID:
                queryBuilder.appendWhere(MyLocationContract.StopPoints.DATA_ID
                        + "=" + uri.getLastPathSegment());
            case STOPPOINTS:
                queryBuilder.setProjectionMap(sStopPointsProjectionMap);
                queryBuilder.setTables(MyLocationDatabaseHelper.Views.STOPPOINTS);
                break;

            case PLACES:
                queryBuilder.setProjectionMap(sPlacesProjectionMap);
                queryBuilder.setTables(MyLocationDatabaseHelper.Views.PLACES);
                break;

            case GOOGLE_PLACES_ID:
                queryBuilder.appendWhere(MyLocationContract.GooglePlaces.DATA_ID
                        + "=" + uri.getLastPathSegment());
            case GOOGLE_PLACES:
                queryBuilder.setProjectionMap(sGooglePlacesProjectionMap);
                queryBuilder
                        .setTables(MyLocationDatabaseHelper.Views.GOOGLE_PLACES);
                break;

            case GOOGLE_PLACES_COUNT:
                queryBuilder.setTables(MyLocationDatabaseHelper.Views.GOOGLE_PLACES);
                projection = new String[]{"Count(*)"};
                break;

            case USER_PLACES_ID:
                queryBuilder.appendWhere(MyLocationContract.UserPlaces.DATA_ID
                        + "=" + uri.getLastPathSegment());
            case USER_PLACES:
                queryBuilder.setProjectionMap(sUserPlacesProjectionMap);
                queryBuilder.setTables(MyLocationDatabaseHelper.Views.USER_PLACES);
                break;

            default:
                throw new IllegalArgumentException("Invalid uri: " + uri.toString());
        }

        c = queryBuilder.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        boolean favorite = false;
        if (values.containsKey(MyLocationContract.MyLocation.FAVORITE)) {
            favorite = values
                    .getAsInteger(MyLocationContract.MyLocation.FAVORITE) == 1;
            values.remove(MyLocationContract.MyLocation.FAVORITE);
        }

        // Get the MyLocation content values from 'values' and remove the keys
        // from 'values'.
        ContentValues myLocationVals = new ContentValues();
        myLocationVals.put(MyLocationTable.Columns.ID,
                values.getAsString(MyLocationContract.MyLocation.ID));
        myLocationVals.put(MyLocationTable.Columns.LAT,
                values.getAsDouble(MyLocationContract.MyLocation.LAT));
        myLocationVals.put(MyLocationTable.Columns.LON,
                values.getAsDouble(MyLocationContract.MyLocation.LON));
        myLocationVals.put(MyLocationTable.Columns.NAME,
                values.getAsString(MyLocationContract.MyLocation.NAME));
        myLocationVals.put(MyLocationTable.Columns.SNIPPET,
                values.getAsString(MyLocationContract.MyLocation.SNIPPET));

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mLocationDb.getWritableDatabase();
        ContentValues absStopVals;
        ContentValues placeVals;
        long absId = 0;
        long placeId = 0;
        long id = 0;
        switch (uriType) {
            case STOPS:
                myLocationVals.put(MyLocationTable.Columns.TYPE,
                        MyLocationContract.LocationTypeCode.STOP);
                id = sqlDB.insert(MyLocationTable.TABLE, null, myLocationVals);
                if (id == -1) {
                    Log.e(TAG, "1: Error inserting into MyLocationTable:: id = "
                            + id + " should be: " + id);
                    break;
                }

                absStopVals = new ContentValues();
                absStopVals.put(AbstractStopTable.Columns.LOCATION_ID, id);
                absStopVals.put(AbstractStopTable.Columns.CODE,
                        values.getAsString(MyLocationContract.AbstractStops.CODE));
                absId = sqlDB.insert(AbstractStopTable.TABLE, null, absStopVals);
                if (absId == -1 && id != -1) {
                    Log.e(TAG, "2: Error inserting into AbstractStopsTable:: id = "
                            + absId);
                }

                ContentValues stopVals = new ContentValues();
                stopVals.put(StopsTable.Columns.DATA_ID, id);
                long stopId = sqlDB.insert(StopsTable.TABLE, null, stopVals);
                if (stopId == -1 && id != -1) {
                    Log.e(TAG, "3: Error inserting into StopsTable:: id = "
                            + stopId + " should be: " + id);
                }

                break;
            case STOPPOINTS:
                myLocationVals.put(MyLocationTable.Columns.TYPE,
                        MyLocationContract.LocationTypeCode.STOPPOINT);
                id = sqlDB.insert(MyLocationTable.TABLE, null, myLocationVals);
                if (id == -1) {
                    Log.e(TAG, "4: Error inserting into MyLocationTable:: id = "
                            + id);
                    break;
                }

                absStopVals = new ContentValues();
                absStopVals.put(AbstractStopTable.Columns.CODE,
                        values.getAsString(MyLocationContract.AbstractStops.CODE));
                absStopVals.put(AbstractStopTable.Columns.LOCATION_ID, id);
                absId = sqlDB.insert(AbstractStopTable.TABLE, null, absStopVals);
                if (absId == -1 && id != -1) {
                    Log.e(TAG, "5: Error inserting into AbstractStopsTable:: id = "
                            + absId);
                }

                ContentValues stopPointVals = new ContentValues();
                stopPointVals.put(StopsTable.Columns.DATA_ID, id);
                long stopPointId = sqlDB.insert(StopPointsTable.TABLE, null,
                        stopPointVals);
                if (stopPointId == -1 && id != -1) {
                    Log.e(TAG, "5: Error inserting into StopPointTable:: id = "
                            + stopPointId);
                }
                break;
            case GOOGLE_PLACES:
                myLocationVals.put(MyLocationTable.Columns.TYPE,
                        MyLocationContract.LocationTypeCode.GOOGLE_PLACE);
                id = sqlDB.insert(MyLocationTable.TABLE, null, myLocationVals);
                if (id == -1) {
                    Log.e(TAG, "7: Error inserting into MyLocationTable:: id = "
                            + id);
                    break;
                }

                placeVals = new ContentValues();
                placeVals.put(PlacesTable.Columns.AUTO_ID, id);
                placeId = sqlDB.insert(PlacesTable.TABLE, null, placeVals);
                if (placeId == -1) {
                    Log.e(TAG, "8: Error inserting into PlacesTable:: id = "
                            + placeId);
                }

                ContentValues gPlaceVals = new ContentValues();
                gPlaceVals.put(GooglePlacesTable.Columns.AUTO_ID, id);
                gPlaceVals.put(GooglePlacesTable.Columns.EVENTS, values
                        .getAsString(MyLocationContract.GooglePlaceColumns.EVENTS));
                gPlaceVals
                        .put(GooglePlacesTable.Columns.FORMATTED_ADDRESS,
                                values.getAsString(MyLocationContract.GooglePlaceColumns.FORMATTED_ADDRESS));
                gPlaceVals.put(GooglePlacesTable.Columns.ICON, values
                        .getAsString(MyLocationContract.GooglePlaceColumns.ICON));
                gPlaceVals
                        .put(GooglePlacesTable.Columns.OPENING_HOURS,
                                values.getAsString(MyLocationContract.GooglePlaceColumns.OPENING_HOURS));
                gPlaceVals.put(GooglePlacesTable.Columns.PHOTOS, values
                        .getAsString(MyLocationContract.GooglePlaceColumns.PHOTOS));
                gPlaceVals
                        .put(GooglePlacesTable.Columns.PRICE_LEVEL,
                                values.getAsInteger(MyLocationContract.GooglePlaceColumns.PRICE_LEVEL));
                gPlaceVals.put(GooglePlacesTable.Columns.RATING, values
                        .getAsDouble(MyLocationContract.GooglePlaceColumns.RATING));
                gPlaceVals
                        .put(GooglePlacesTable.Columns.REFERENCE,
                                values.getAsString(MyLocationContract.GooglePlaceColumns.REFERENCE));
                gPlaceVals.put(GooglePlacesTable.Columns.TYPES, values
                        .getAsString(MyLocationContract.GooglePlaceColumns.TYPES));
                gPlaceVals
                        .put(GooglePlacesTable.Columns.VICINITY,
                                values.getAsString(MyLocationContract.GooglePlaceColumns.VICINITY));
                long gPlaceId = sqlDB.insert(GooglePlacesTable.TABLE, null,
                        gPlaceVals);
                if (gPlaceId == -1) {
                    Log.e(TAG, "9: Error inserting into GooglePlacesTable:: id = "
                            + gPlaceId);
                }
                break;
            case USER_PLACES:
                myLocationVals.put(MyLocationTable.Columns.TYPE,
                        MyLocationContract.LocationTypeCode.USER_PLACE);
                id = sqlDB.insert(MyLocationTable.TABLE, null, myLocationVals);
                if (id == -1) {
                    Log.e(TAG, "10: Error inserting into MyLocationTable:: id = "
                            + id);
                    break;
                }

                placeVals = new ContentValues();
                placeVals.put(PlacesTable.Columns.AUTO_ID, id);
                placeId = sqlDB.insert(PlacesTable.TABLE, null, placeVals);
                if (placeId == -1) {
                    Log.e(TAG, "11: Error inserting into PlacesTable:: id = "
                            + placeId);
                }

                ContentValues userPlaceVals = new ContentValues();
                userPlaceVals.put(UserPlacesTable.Columns.AUTO_ID, id);
                userPlaceVals.put(UserPlacesTable.Columns.COMMENT,
                        values.getAsString(MyLocationContract.UserPlaces.COMMENT));
                long userPlaceId = sqlDB.insert(GooglePlacesTable.TABLE, null,
                        userPlaceVals);
                if (userPlaceId == -1) {
                    Log.e(TAG, "12: Error inserting into UserPlaceTable:: id = "
                            + userPlaceId);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // If we've actually inserted a row.
        if (id != -1) {
            if (favorite) {
                ContentValues favVals = new ContentValues();
                favVals.put(FavoritesTable.Columns.VALUE, 1);
                favVals.put(FavoritesTable.Columns.AUTO_ID, id);
                long favId = sqlDB.insert(FavoritesTable.TABLE, null, favVals);
                if (favId == -1)
                    Log.e(TAG, "error inserting into fav table");
            }

            // Notify any listeners
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return Uri.withAppendedPath(uri, "" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mLocationDb.getWritableDatabase();
        sqlDB.beginTransaction();
        int rowsDeleted = 0;

        Cursor c;
        // Only allow deletion of places.
        switch (uriType) {
            case GOOGLE_PLACES_ID:
                // add the id to the where
                if (selection == null)
                    selection = "";
                if (selection.length() > 0)
                    selection += " AND ";
                selection += MyLocationContract.GooglePlaces.DATA_ID + "="
                        + uri.getLastPathSegment();
            case GOOGLE_PLACES:
                c = query(MyLocationContract.GooglePlaces.CONTENT_URI,
                        new String[]{MyLocationContract.GooglePlaces.DATA_ID},
                        selection, selectionArgs, null);
                // TODO fix foreign key constraint
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        long rowId = c
                                .getLong(c
                                        .getColumnIndex(MyLocationContract.GooglePlaces.DATA_ID));
                        rowsDeleted += sqlDB.delete(GooglePlacesTable.TABLE,
                                GooglePlacesTable.Columns.AUTO_ID + "=" + rowId,
                                null);
                        c.moveToNext();
                    }
                }
                c.close();
                break;
            case USER_PLACES_ID:
                if (selection == null)
                    selection = "";
                if (selection.length() > 0)
                    selection += " AND ";
                selection += MyLocationContract.UserPlaces.DATA_ID + "="
                        + uri.getLastPathSegment();
            case USER_PLACES:
                c = query(MyLocationContract.UserPlaces.CONTENT_URI,
                        new String[]{MyLocationContract.UserPlaces.DATA_ID},
                        selection, selectionArgs, null);
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        long rowId = c
                                .getLong(c
                                        .getColumnIndex(MyLocationContract.UserPlaces.DATA_ID));
                        rowsDeleted += sqlDB
                                .delete(UserPlacesTable.TABLE,
                                        UserPlacesTable.Columns.AUTO_ID + "="
                                                + rowId, null);
                        c.moveToNext();
                    }
                }
                c.close();
                break;
        }
        sqlDB.setTransactionSuccessful();
        sqlDB.endTransaction();

        if (rowsDeleted > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mLocationDb.getWritableDatabase();
        int rowsUpdated = 0;

        Uri uriToNotify;

        // TODO IMPLEMENT ME! AFTER LOVING COLLEEN!
        switch (uriType) {
            case STOPS_ID:
                uriToNotify = uri;
                break;
            case STOPPOINTS_ID:
                uriToNotify = uri;
                break;
            case GOOGLE_PLACES_ID:
                uriToNotify = uri;
                break;
            case USER_PLACES_ID:
                uriToNotify = uri;
                break;
            default:
                throw new IllegalArgumentException("update: Unrecognized uri: "
                        + uri.toString());
        }
        long id = Long.parseLong(uri.getLastPathSegment());
        boolean favorite = false;
        if (values.containsKey(MyLocationContract.MyLocation.FAVORITE)) {
            Integer asInteger = values
                    .getAsInteger(MyLocationContract.MyLocation.FAVORITE);
            favorite = asInteger == 1;
            ContentValues favVals = new ContentValues();
            favVals.put(FavoritesTable.Columns.AUTO_ID, id);
            favVals.put(FavoritesTable.Columns.VALUE, favorite ? 1 : 0);
            // If we want to favorite, add it to the favorites.
            if (favorite) {
                long rowId = sqlDB.insert(FavoritesTable.TABLE, null, favVals);
                if (rowId != -1)
                    rowsUpdated += 1;
            } else {
                // otherwise delete it.
                rowsUpdated += sqlDB.delete(FavoritesTable.TABLE,
                        FavoritesTable.Columns.AUTO_ID + " = " + id, null);
            }
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uriToNotify, null);
        }

        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * Performs the work provided in a single transaction
     */
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations) {
        ContentProviderResult[] result = new ContentProviderResult[operations
                .size()];
        int i = 0;
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mLocationDb.getWritableDatabase();
        // Begin a transaction
        db.beginTransaction();
        try {
            for (ContentProviderOperation operation : operations) {
                // Chain the result for back references
                result[i++] = operation.apply(this, result, i);
            }

            db.setTransactionSuccessful();
        } catch (OperationApplicationException e) {
            Log.e(TAG, "batch failed: " + e.getLocalizedMessage());
        } finally {
            db.endTransaction();
        }

        return result;
    }

    class MyCursorWrapper extends CursorWrapper {

        private int mColumnCount;
        private ArrayList<String> mExtraStaticCols;
        private ArrayList<Object> mExtraStaticValues;
        private String[] mColumnNames;

        public MyCursorWrapper(Cursor cursor,
                               Map<String, Object> extraStaticCols) {
            super(cursor);
            mColumnCount = cursor.getColumnCount();
            mExtraStaticCols = new ArrayList<String>();
            mExtraStaticValues = new ArrayList<Object>();
            if (extraStaticCols != null) {
                for (String columnName : extraStaticCols.keySet()) {
                    mExtraStaticCols.add(columnName);
                    mExtraStaticValues.add(extraStaticCols.get(columnName));
                }
            }
            String[] extraNames = new String[extraStaticCols.size()];
            int i = 0;
            for (String string : mExtraStaticCols) {
                extraNames[i] = string;
                i++;
            }
            mColumnNames = Util.appendStringArrays(cursor.getColumnNames(),
                    extraNames);

        }

        @Override
        public int getColumnCount() {
            return super.getColumnCount() + 1;
        }

        @Override
        public String[] getColumnNames() {
            // MergeCursor getColumnIndex calls getColumnNames, so need to
            // override this instead of getColumnIndex for compatibility with
            // MergeCursor.
            return mColumnNames;
        }

        @Override
        public int getInt(int columnIndex) {
            if (columnIndex < mColumnCount)
                return super.getInt(columnIndex);
            return (Integer) mExtraStaticValues.get(columnIndex - mColumnCount);
        }

        @Override
        public int getColumnIndexOrThrow(String columnName)
                throws IllegalArgumentException {
            int index = super.getColumnIndex(columnName);
            if (index < 0) {
                int arrIndex = mExtraStaticCols.indexOf(columnName);
                if (arrIndex < 0)
                    throw new IllegalArgumentException(columnName
                            + " not found in cursor.");
                index = mColumnCount + arrIndex;
            }
            return index;
        }

        @Override
        public int getColumnIndex(String columnName) {
            int index = super.getColumnIndex(columnName);
            if (index < 0) {
                index = mColumnCount + mExtraStaticCols.indexOf(columnName);
            }
            return index;
        }
    }
}
