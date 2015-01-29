package com.teamparkin.mtdapp.restadapters;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.dataclasses.MyLocation;

public class GoogleStreetViewAPIAdapter extends RESTJSONAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = GoogleStreetViewAPIAdapter.class
			.getSimpleName();

	private static final String URL_START = "https://maps.googleapis.com/maps/api/streetview";
	public static final int DEFAULT_WIDTH = 640;
	public static final int DEFAULT_HEIGHT = 640;
	private static final String DEFAULT_SIZE = "" + DEFAULT_WIDTH + "x"
			+ DEFAULT_HEIGHT;
	private static String KEY = "";

	private static GoogleStreetViewAPIAdapter mSingleton;

	private static BitmapDrawable BITMAP_DRAWABLE_BLANK = null;

	/**
	 * Returns a bitmap drawable of DEFAULT_WIDTH x DEFAULT_HEIGHT.
	 * 
	 * @param context
	 * @return
	 */
	public static BitmapDrawable getDefaultDrawable(Context context) {
		if (BITMAP_DRAWABLE_BLANK != null)
			return BITMAP_DRAWABLE_BLANK;
		// see other conf types
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		// this creates a MUTABLE bitmap
		Bitmap bmp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, conf);
		BITMAP_DRAWABLE_BLANK = new BitmapDrawable(context.getResources(), bmp);
		return BITMAP_DRAWABLE_BLANK;
	}

	public static synchronized GoogleStreetViewAPIAdapter getInstance(
			Context context) {
		if (mSingleton == null) {
			KEY = context.getResources().getString(R.string.api_key_streetview);
			mSingleton = new GoogleStreetViewAPIAdapter(context);
		}
		return mSingleton;
	}

	private GoogleStreetViewAPIAdapter(Context context) {
		this.mContext = context;
	}

	@Override
	/**
	 * command passed in should be ""
	 */
	protected String setupCommand(String command) {
		command = "";
		String url = URL_START + command;
		return url;
	}

	public String getStreetViewUrl(MyLocation location) {
		return getStreetViewUrl(location, DEFAULT_SIZE);
	}

	/**
	 * Returns a drawable from either a cached image or the StreetView API.
	 * 
	 * @param location
	 * @param size
	 *            - formatted "{width}x{height}", eg "600x400"
	 * @return
	 */
	public String getStreetViewUrl(MyLocation location, String size) {
		double latitude = location.getLatLng().latitude;
		double longitude = location.getLatLng().longitude;
		return getStreetViewUrl(latitude, longitude, size);
	}

	public String getStreetViewUrl(double latitude, double longitude) {
		return getStreetViewUrl(latitude, longitude, DEFAULT_SIZE);
	}

	public String getStreetViewUrl(double latitude, double longitude,
			String size) {
		Parameters parameters = new Parameters();
		parameters.put("key", KEY);
		parameters.put("size", size);
		parameters.put("location", toLocationString(latitude, longitude));
		parameters.put("sensor", false);

		String urlString = getUrlRequest(parameters);
		return urlString;
	}

	private String toLocationString(double lat, double lon) {
		return "" + lat + "," + lon;
	}

	private String getUrlRequest(Parameters parameters) {
		String command = setupCommand("");
		String params = "";
		Map<String, String> copy = new TreeMap<String, String>(
				parameters.getMap());
		int i = 0;
		int count = copy.size();
		for (Map.Entry<String, ?> entry : copy.entrySet()) {
			params += entry.getKey() + "=" + entry.getValue();
			i++;
			if (i < count) {
				params += "&";
			}
		}
		return command + "?" + params;
	}

}
