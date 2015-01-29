package com.teamparkin.mtdapp.fragments;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.teamparkin.mtdapp.R;

public class RouteFragment extends MyFragment {
	@SuppressWarnings("unused")
	private static final String TAG = RouteFragment.class.getSimpleName();

	private View mView;

	private Bundle mBundle;

	private String mJSONArray;
	private String mTitle;

	private OnRouteItemClickListener mRouteItemClickListener;

	public void setBundle(Bundle bundle) {
		mBundle = bundle;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.routedisplay, container, false);
		ListView lv = (ListView) mView.findViewById(R.id.routelist_list);
		setViewForMarginChange(lv);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("savedJson")) {
				mBundle = savedInstanceState;
			}
		}
		initializeFromBundle(mBundle);
		return mView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("savedJson", mJSONArray);
		outState.putString("title", mTitle);
	}

	public void initializeFromBundle(Bundle bundle) {
		if (bundle != null && bundle.containsKey("savedJson")) {
			TextView tv = (TextView) mView.findViewById(R.id.routelist_header);
			mTitle = bundle.getString("title");
			tv.setText(mTitle);
			mJSONArray = bundle.getString("savedJson");
			initializeList(mJSONArray);
		} else if (bundle != null && bundle.containsKey("title")) {
			TextView tv = (TextView) mView.findViewById(R.id.routelist_header);
			mTitle = bundle.getString("title");
			tv.setText(mTitle);
			mJSONArray = bundle.getString("JSONArray");
			initializeList(mJSONArray);
		} else {
			TextView tv = (TextView) mView.findViewById(R.id.routelist_header);
			mTitle = "Choose a time period below:";
			tv.setText(mTitle);
			mJSONArray = "";
			try {
				Resources res = getResources();
				InputStream in_s = res.openRawResource(R.raw.route_pdf_info);

				byte[] b = new byte[in_s.available()];
				in_s.read(b);
				mJSONArray = new String(b);
			} catch (Exception e) {
				// e.printStackTrace();
				return;
			}
			initializeList(mJSONArray);
		}
	}

	private void initializeList(String jsonArrayString) {
		JSONArray jarr = null;
		try {
			jarr = new JSONArray(jsonArrayString);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ListView lv = (ListView) mView.findViewById(R.id.routelist_list);
		RouteArrayAdapter adapter = new RouteArrayAdapter(getActivity(), jarr);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				JSONObject item = null;
				item = (JSONObject) parent.getItemAtPosition(position);
				if (item.has("mapurl")) {
					Bundle bundle = new Bundle();
					try {
						bundle.putString("mapurl", item.getString("mapurl"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					dispatchOnRouteMapRequested(bundle);
				} else if (item.has("sub")) {
					Bundle bundle = new Bundle();
					bundle.putString("title", ((TextView) view
							.findViewById(R.id.routedisplay_item_text))
							.getText().toString());
					try {
						bundle.putString("JSONArray", item.getJSONArray("sub")
								.toString());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					dispatchOnRouteItemSelected(bundle);
				}
			}
		});
	}

	private class RouteArrayAdapter extends BaseAdapter implements ListAdapter {

		private final Activity activity;
		private final JSONArray jsonArray;

		public RouteArrayAdapter(Activity activity, JSONArray jsonArray) {
			this.activity = activity;
			this.jsonArray = jsonArray;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				row = inflater.inflate(R.layout.routedisplay_item, parent,
						false);
			}

			JSONObject jobj = (JSONObject) getItem(position);

			TextView text = (TextView) row
					.findViewById(R.id.routedisplay_item_text);
			TextView numberView = (TextView) row
					.findViewById(R.id.routedisplay_item_numbertext);
			numberView.setVisibility(View.GONE);
			String name = "";
			String number = "";
			String backColor = "";
			String textColor = "";
			try {
				name = jobj.getString("name");
				if (jobj.has("number")) {
					number = jobj.getString("number");
				}
				if (jobj.has("backColor")) {
					backColor = jobj.getString("backColor");
				}
				if (jobj.has("textColor")) {
					textColor = jobj.getString("textColor");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			text.setText(name);
			if (backColor.length() > 0 && textColor.length() > 0) {
				numberView.setText(number);
				numberView.setBackgroundColor(Color.parseColor(backColor));
				numberView.setTextColor(Color.parseColor(textColor));
				numberView.setVisibility(View.VISIBLE);
			}

			return row;
		}

		@Override
		public int getCount() {
			return jsonArray.length();
		}

		@Override
		public Object getItem(int position) {
			return jsonArray.optJSONObject(position);
		}

		@Override
		public long getItemId(int position) {
			JSONObject jsonObject = (JSONObject) getItem(position);
			return jsonObject.optLong("id");
		}

	}

	public void setOnRouteItemClickListener(OnRouteItemClickListener listener) {
		mRouteItemClickListener = listener;
	}

	private void dispatchOnRouteItemSelected(Bundle args) {
		if (mRouteItemClickListener != null)
			mRouteItemClickListener.onRouteItemSelected(args);
		else {
			Log.e(TAG, "dispatchOnRouteItemSelected listener null");
		}
	}

	private void dispatchOnRouteMapRequested(Bundle args) {
		if (mRouteItemClickListener != null)
			mRouteItemClickListener.onRouteMapRequested(args);
		else {
			Log.e(TAG, "dispatchOnRouteMapRequested listener null");
		}
	}

	public interface OnRouteItemClickListener {
		public void onRouteItemSelected(Bundle args);

		public void onRouteMapRequested(Bundle args);
	}

}
