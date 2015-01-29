package com.teamparkin.mtdapp.restadapters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public abstract class RESTJSONAdapter {
	private static final String TAG = RESTJSONAdapter.class.getSimpleName();

	protected RESTJSONAdapterError error;

	protected Context mContext;

	protected abstract String setupCommand(String command);

	protected JSONObject httpPost(String command, Parameters parameters) {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null || cm.getActiveNetworkInfo() == null) {
			error = new RESTJSONAdapterError(
					"No internet connection.  Please check your data/wifi connections.");
			return null;
		}
		String uri = setupCommand(command);
		Log.d("C2DM", "Sending registration ID to my application server");
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		JSONObject json = null;
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			// Get the deviceID
			for (String string : parameters.keySet()) {
				nameValuePairs.add(new BasicNameValuePair(string, parameters
						.get(string)));
			}
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() >= 300) {
				error = new RESTJSONAdapterError("Server error:\n"
						+ response.getStatusLine().toString());
				return null;
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			String result = "";
			while ((line = rd.readLine()) != null) {
				result = result + line;
			}
			json = new JSONObject(result);
		} catch (IOException e) {
			error = new RESTJSONAdapterError(e);
			return null;
		} catch (JSONException e) {
			error = new RESTJSONAdapterError(e);
			return null;
		}

		return json;
	}

	protected String formatHttpGetParameters(Parameters parameters) {
		error = null;
		String urlQuery = "";
		for (String string : parameters.keySet()) {
			if (urlQuery.length() > 0)
				urlQuery += "&";
			urlQuery = urlQuery + string + "=" + forURL(parameters.get(string));
		}
		return urlQuery;
	}

	protected String getHttpGetUrl(String command, Parameters parameters) {
		String url = setupCommand(command);
		url = url + "?" + formatHttpGetParameters(parameters);
		return url;
	}

	protected JSONObject httpGet(String command, Parameters parameters) {
		String url = getHttpGetUrl(command, parameters);
		return httpGet(url);
	}

	protected JSONObject httpGet(String url) {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null || cm.getActiveNetworkInfo() == null) {
			error = new RESTJSONAdapterError(
					"No internet connection.  Please check your data/wifi connections.");
			return null;
		}
		Log.i(TAG, "url: " + url);
		HttpClient httpclient = new DefaultHttpClient();
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		JSONObject json = null;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			String responseStatusString = response.getStatusLine().toString();
			if (response.getStatusLine().getStatusCode() >= 300) {
				error = new RESTJSONAdapterError("Server error:\n"
						+ responseStatusString);
				return null;
			}
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry activity_about connection release
			if (entity != null) {
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				if (hasError())
					return null;
				// A Simple JSONObject Creation
				json = new JSONObject(result);
				instream.close();
			} else {
				error = new RESTJSONAdapterError("HttpEntity null");
				return null;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "clientProtocolException");
			error = new RESTJSONAdapterError("ClientProtocolException: "
					+ e.getMessage());
			return null;
		} catch (IOException e) {
			error = new RESTJSONAdapterError("IOException: " + e.getMessage());
			return null;
		} catch (JSONException e) {
			error = new RESTJSONAdapterError("JSONException: " + e.getMessage());
			return null;
		} catch (Exception e) {
			error = new RESTJSONAdapterError("Exception: " + e.getMessage());
			return null;
		}
		return json;
	}

	protected String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			error = new RESTJSONAdapterError(e);
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				error = new RESTJSONAdapterError(e);
				return null;
			}
		}
		return sb.toString();
	}

	/**
	 * Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.
	 * 
	 * <P>
	 * Used to ensure that HTTP query strings are in proper form, by escaping
	 * special characters such as spaces.
	 * 
	 * <P>
	 * It is important to note that if a query string appears in an
	 * <tt>HREF</tt> attribute, then there are two issues - ensuring the query
	 * string is valid HTTP (it is URL-encoded), and ensuring it is valid HTML
	 * (ensuring the ampersand is escaped).
	 */
	protected String forURL(String aURLFragment) {
		String result = null;
		try {
			result = URLEncoder.encode(aURLFragment, "UTF-8");
			//
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("UTF-8 not supported", ex);
		}
		return result;
	}

	public boolean hasError() {
		return error != null;
	}

	public String getErrorMessage() {
		if (hasError())
			return error.getErrorMessage();
		return "No error";
	}

}