package com.teamparkin.mtdapp.restadapters;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;

public class GooglePlacesAPIAdapter extends RESTJSONAdapter {
	private static final String TAG = GooglePlacesAPIAdapter.class
			.getSimpleName();

	private static final double DEFAULT_LAT = 40.03;
	private static final double DEFAULT_LON = -88.28;
	private static final int DEFAULT_RADIUS = 50000;

	private static final String URL_START = "https://maps.googleapis.com/maps/api/place/";
	private static final String SEARCH = "nearbysearch";
	private static final String JSON = "json";
	private static String KEY = "";

	private static GooglePlacesAPIAdapter mSingleton;

	private GooglePlacesAPIAdapter(Context context) {
		this.mContext = context;
	}

	public static synchronized GooglePlacesAPIAdapter getInstance(
			Context context) {
		if (mSingleton == null) {
			KEY = context.getResources().getString(R.string.api_key_places);
			mSingleton = new GooglePlacesAPIAdapter(context);
		}
		return mSingleton;
	}

	@Override
	protected String setupCommand(String command) {
		String url = URL_START + command + "/" + JSON;
		return url;
	}

	/**
	 * Constructs a google place search url using the default lat, default lon,
	 * and default radius.
	 * 
	 * @param name
	 * @return
	 */
	public String getSearchPlacesUrl(String name) {
		return getSearchPlacesUrl(DEFAULT_LAT, DEFAULT_LON, DEFAULT_RADIUS,
				name);
	}

	/**
	 * Constructs a google place search url with the given parameters.
	 * 
	 * @param lat
	 * @param lon
	 * @param radius
	 * @param keyword
	 * @return
	 */
	public String getSearchPlacesUrl(double lat, double lon, int radius,
			String keyword) {
		Parameters parameters = new Parameters();
		parameters.put("key", KEY);
		parameters.put("location", toLocationString(lat, lon));
		parameters.put("radius", radius);
		parameters.put("sensor", true);
		if (keyword != null && keyword.length() > 0)
			parameters.put("keyword", keyword); // use keyword instead of name
												// so addresses are searched as
												// well.

		String url = getHttpGetUrl(SEARCH, parameters);
		return url;
	}

	public void addPlacesToContentProvider(Context context, JSONObject jobj) {

		long timestamp = System.currentTimeMillis();
		try {
			JSONArray results = jobj.getJSONArray("results");
			JSONObject result;
			ArrayList<ContentProviderOperation> insertOperations = new ArrayList<ContentProviderOperation>();
			for (int i = 0; i < results.length(); i++) {
				// TODO add all the google place attributes.
				result = results.getJSONObject(i);
				ContentProviderOperation.Builder builder = parseNewInsert(result);
				insertOperations.add(builder.build());
			}
			// end the StopPointsDatabase operation
			try {
				context.getContentResolver().applyBatch(
						MyLocationContract.AUTHORITY, insertOperations);
			} catch (RemoteException e) {
				Log.e(TAG, "err apply batch: ", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				Log.e(TAG, "err apply batch: ", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JSONException e) {
			error = new RESTJSONAdapterError(e);
			Log.e(TAG, e.getMessage());
			return;
		}

		return;
	}

	private ContentProviderOperation.Builder parseNewInsert(JSONObject result)
			throws JSONException {
		JSONObject location = result.getJSONObject("geometry").getJSONObject(
				"location");
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(MyLocationContract.GooglePlaces.CONTENT_URI);
		if (result.has("events"))
			builder.withValue(MyLocationContract.GooglePlaces.EVENTS, result
					.getJSONArray("events").toString());
		String snippet = "";
		if (result.has("formatted_address")) {
			snippet = result.getString("formatted_address");
			builder.withValue(
					MyLocationContract.GooglePlaces.FORMATTED_ADDRESS, snippet);
		}
		if (result.has("icon"))
			builder.withValue(MyLocationContract.GooglePlaces.ICON,
					result.getString("icon"));
		builder.withValue(MyLocationContract.GooglePlaces.ID,
				result.getString("id"));
		builder.withValue(MyLocationContract.GooglePlaces.LAT,
				location.getDouble("lat"));
		builder.withValue(MyLocationContract.GooglePlaces.LON,
				location.getDouble("lng"));
		builder.withValue(MyLocationContract.GooglePlaces.NAME,
				result.getString("name"));
		if (result.has("opening_hours"))
			builder.withValue(MyLocationContract.GooglePlaces.OPENING_HOURS,
					result.getJSONObject("opening_hours").toString());
		if (result.has("photos"))
			builder.withValue(MyLocationContract.GooglePlaces.PHOTOS, result
					.getJSONArray("photos").toString());
		if (result.has("price_level"))
			builder.withValue(MyLocationContract.GooglePlaces.PRICE_LEVEL,
					result.getInt("price_level"));
		if (result.has("rating"))
			builder.withValue(MyLocationContract.GooglePlaces.RATING,
					result.getDouble("rating"));
		if (result.has("reference"))
			builder.withValue(MyLocationContract.GooglePlaces.REFERENCE,
					result.getString("reference"));
		builder.withValue(MyLocationContract.GooglePlaces.TYPE,
				MyLocationContract.LocationTypeCode.GOOGLE_PLACE);
		if (result.has("types"))
			builder.withValue(MyLocationContract.GooglePlaces.TYPES, result
					.getJSONArray("types").toString());
		if (result.has("vicinity")) {
			snippet = result.getString("vicinity");
			builder.withValue(MyLocationContract.GooglePlaces.VICINITY, snippet);
		}
		builder.withValue(MyLocationContract.GooglePlaces.SNIPPET, snippet);
		return builder;
	}

	private String toLocationString(double lat, double lon) {
		return "" + lat + "," + lon;
	}

	private void checkStatusCode(JSONObject jobj) {
		String s;
		try {
			s = jobj.getString("status");
			Log.i(TAG, "STATUS: " + s);
			if (!s.equals("OK"))
				error = new RESTJSONAdapterError(s);
		} catch (JSONException e) {
			error = new RESTJSONAdapterError(e);
		}
	}

}
