package com.teamparkin.mtdapp.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContentProvider;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.StopPoints;
import com.teamparkin.mtdapp.dataclasses.AbstractStop;
import com.teamparkin.mtdapp.dataclasses.Departure;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.listeners.MyCheckedChangeListener;
import com.teamparkin.mtdapp.listeners.MyDepartureFragmentListener;
import com.teamparkin.mtdapp.restadapters.MTDAPIAdapter;
import com.teamparkin.mtdapp.util.Util;
import com.teamparkin.mtdapp.views.LocationItemView;

public class DeparturesFragment extends MyLocationSlideupHeadFragment implements
		LoaderCallbacks<Cursor> {
	private static final String TAG = DeparturesFragment.class.getSimpleName();

	private static final int LAYOUT_ID = R.layout.displaystop;

	private static final int LOADER_ID = 3919304;
	private static final String EXTRA_STOPPOINT_IDS = "stopPointIds";

	private AbstractStop mStop;

	private MTDAPIAdapter apiAdapter;
	private static Activity mActivity;

	private Cursor mCursor;
	private Map<String, ArrayList<Departure>> departures;
	// map vehicle id to view position
	private Map<String, Map<String, Integer>> stopPointToVehiIdToViewPos;
	private Map<String, Integer> departuresToViewNum;
	private Map<String, Integer> stoppointToHeight;

	private Map<String, ViewGroup> stoppointToView;

	private View mView;
	private MyDepartureFragmentListener mUpdateListener = null;

	private String mCurrTimeString;

	private OnDepartureClickListener mDepartureClickListener;

	private ViewGroup mDepartureListParent;

	private LayoutParams mCardLayoutParams;

	private LayoutParams mDepartureLayoutParams;

	private RequestQueue mRequestQueue;

	private boolean mRefreshDepartures;

	public void setUpdateListener(MyDepartureFragmentListener listener) {
		mUpdateListener = listener;
	}

	public void removeUpdateListener() {
		mUpdateListener = null;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		apiAdapter = MTDAPIAdapter.getInstance(mActivity);

		mCardLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		int marginTopBottom = Util.getPixelsFromDips(getActivity(), 5);
		int marginLeftRight = Util.getPixelsFromDips(getActivity(), 10);
		mCardLayoutParams.setMargins(marginLeftRight, marginTopBottom,
				marginLeftRight, marginTopBottom);

		mDepartureLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		marginTopBottom = Util.getPixelsFromDips(getActivity(), 2);
		mDepartureLayoutParams.setMargins(0, marginTopBottom, 0,
				marginTopBottom);

		mRequestQueue = Volley.newRequestQueue(activity);
	}

	@Override
	protected int getLayoutId() {
		return LAYOUT_ID;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRequestQueue.cancelAll(TAG);
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (departures != null && mStop != null) {
			outState.putParcelable("stop", mStop);
			ArrayList<String> stopPoints = new ArrayList<String>(
					departures.keySet());
			outState.putStringArrayList("stopPoints", stopPoints);
			for (String sp : stopPoints) {
				outState.putParcelableArrayList("departures:" + sp,
						departures.get(sp));
			}
			outState.putString("currTime", mCurrTimeString);
		}
	}

	@Override
	protected void initializeBodyView(View mainView, Bundle savedInstanceState) {
		mView = mainView;
		mDepartureListParent = (ViewGroup) mView
				.findViewById(R.id.display_stops_list);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey("stopPoints")) {
			clearMaps();
			ArrayList<String> stopPoints = savedInstanceState
					.getStringArrayList("stopPoints");
			for (String sp : stopPoints) {
				ArrayList<Departure> departuresList = savedInstanceState
						.getParcelableArrayList("departures:" + sp);
				departures.put(sp, departuresList);
			}

		}

		if (savedInstanceState != null
				&& savedInstanceState.containsKey("stop")) {
			mStop = savedInstanceState.getParcelable("stop");
		}
		if (savedInstanceState != null
				&& savedInstanceState.containsKey("currTime")) {
			mCurrTimeString = savedInstanceState.getString("currTime");
		}

		if (departures != null) {
			TextView updateTime = (TextView) mainView
					.findViewById(R.id.displaystop_text_updatetime);
			updateTime.setText(mCurrTimeString);
			Bundle bundle = new Bundle();
			bundle.putStringArrayList(EXTRA_STOPPOINT_IDS,
					new ArrayList<String>(departures.keySet()));

			mRefreshDepartures = true;
			getLoaderManager().initLoader(LOADER_ID, bundle,
					DeparturesFragment.this);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// inflater.inflate(R.menu.departure_fragment_menu, menu);
	}

	private void setDepartures(final AbstractStop stop) {
		if (stop == null || mView == null) {
			Log.e(TAG, "setDepartures stop null");
			return;
		}
		stopPointToVehiIdToViewPos = new LinkedHashMap<String, Map<String, Integer>>();
		stoppointToHeight = new HashMap<String, Integer>();

		for (String id : departures.keySet()) {
			Map<String, Integer> vehiIdToViewPos = new HashMap<String, Integer>();
			stoppointToHeight.put(id, stoppointToView.get(id).getChildCount());
			for (Departure departure : departures.get(id)) {
				vehiIdToViewPos.put(
						departure.getVehicle_id() + ","
								+ departure.getScheduled(),
						departuresToViewNum.get(departure.getVehicle_id()
								+ departure.getExpected_mins()));
			}
			stopPointToVehiIdToViewPos.put(id, vehiIdToViewPos);
		}
		setListVisibility(true, true, false, "");
		// timeOfAccess = System.currentTimeMillis();

		String url = apiAdapter.getDeparturesByStopUrl(stop.getId());

		JsonObjectRequest jsObjReques = new JsonObjectRequest(
				Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						departures = apiAdapter
								.parseDeparturesByStopFromJSON(response);

						TextView updateTime = (TextView) mView
								.findViewById(R.id.displaystop_text_updatetime);
						updateTime.setText(getCurrTimeString());

						if (departures == null) {
							Log.e(TAG, "departures null");
							setListVisibility(false, false, true,
									"No data or wifi connection available.");
							clearMaps();
							return;
						}
						if (departures.size() < 1) {
							setListVisibility(false, false, true,
									"No upcoming departures.");
							clearMaps();
							return;
						}

						Bundle bundle = new Bundle();
						bundle.putStringArrayList(EXTRA_STOPPOINT_IDS,
								new ArrayList<String>(departures.keySet()));

                        // make sure the fragment is attached
                        if (DeparturesFragment.this.isAdded()) {
                            mRefreshDepartures = true;
                            if (getLoaderManager().getLoader(LOADER_ID) == null) {
                                getLoaderManager().initLoader(LOADER_ID, bundle,
                                        DeparturesFragment.this);
                            } else {
                                getLoaderManager().restartLoader(LOADER_ID, bundle,
                                        DeparturesFragment.this);
                            }
                        }

						// fillDeparturesLinearLayout(true);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Volley error", error);
						setListVisibility(false, false, true,
								"No data or wifi connection available.");
						clearMaps();
					}
				});
		jsObjReques.setShouldCache(false);
        jsObjReques.setTag(TAG);
        mRequestQueue.add(jsObjReques);
	}

	private void fillDeparturesLinearLayout(boolean animate) {
		clearDepartureListLayout();
		if (apiAdapter.hasError()) {
			setListVisibility(false, false, true, apiAdapter.getErrorMessage());
			clearMaps();
			return;
		}
		if (departures == null) {
			Log.e(TAG, "departures null");
			setListVisibility(false, false, true,
					"No data or wifi connection available.");
			clearMaps();
			return;
		}
		if (departures.size() < 1) {
			setListVisibility(false, false, true, "No upcoming departures.");
			clearMaps();
			return;
		}

		ViewGroup stoppointDepartureList = null;
		departuresToViewNum = new HashMap<String, Integer>();
		stoppointToView = new HashMap<String, ViewGroup>();

		mCursor.moveToFirst();
		while (!mCursor.isAfterLast()) {
			if (mActivity == null) {
				Log.e(TAG, "activity null");
				setListVisibility(false, false, true,
						"Error grabbing MTD data.");
				clearMaps();
				return;
			}

			String id = mCursor.getString(mCursor
					.getColumnIndex(MyLocationContract.StopPoints.ID));

			View stoppointDepartureLayout = LayoutInflater.from(mActivity)
					.inflate(R.layout.stoppoint_departure_list, null);
			stoppointDepartureLayout = setStopPointItemProperties(
					stoppointDepartureLayout, mCursor);
			mDepartureListParent.addView(stoppointDepartureLayout);

			stoppointDepartureLayout.setLayoutParams(mCardLayoutParams);

			stoppointDepartureList = (ViewGroup) stoppointDepartureLayout
					.findViewById(R.id.display_stoppoint_list);
			if (stoppointDepartureList == null || stoppointToView == null
					|| departures == null) {
				Log.e(TAG, "weird null error");
				Log.e(TAG, "stoppointDepartureList: "
						+ (stoppointDepartureList == null));
				Log.e(TAG, "stoppointToView: " + (stoppointToView == null));
				Log.e(TAG, "departures: " + (departures == null));
				setListVisibility(false, false, true,
						"Error grabbing MTD data.");
				clearMaps();
				return;
			}
			stoppointDepartureList.removeAllViews();
			stoppointToView.put(id, stoppointDepartureList);

			long offset = 0;
			int count = 0;
			for (Departure departure : departures.get(id)) {
				if (mActivity == null) {
					Log.e(TAG, "activity null 2");
					setListVisibility(false, false, true,
							"Error grabbing MTD data.");
					clearMaps();
					return;
				}
				View view = LayoutInflater.from(mActivity).inflate(
						R.layout.displaystop_item, null);

				view.setLayoutParams(mDepartureLayoutParams);

				view = setDepartureItemProperties(view, departure);
				stoppointDepartureList.addView(view);
				if (stopPointToVehiIdToViewPos.containsKey(id)) {
					if (stopPointToVehiIdToViewPos.get(id).containsKey(
							departure.getVehicle_id() + ","
									+ departure.getScheduled())) {
						int start = stopPointToVehiIdToViewPos.get(id).get(
								departure.getVehicle_id() + ","
										+ departure.getScheduled());
						if (animate && start != count) {
							Animation animation = new TranslateAnimation(
									Animation.RELATIVE_TO_SELF, 0.0f,
									Animation.RELATIVE_TO_SELF, 0.0f,
									Animation.RELATIVE_TO_SELF,
									1.0f * (start - count),
									Animation.RELATIVE_TO_SELF, 0.0f);
							animation.setDuration(700);
							view.startAnimation(animation);
						}

					} else if (animate) {
						view.setAnimation(setLayoutAnim_slidedown2(offset, 400));
					}
				} else if (animate) {
					view.setAnimation(setLayoutAnim_slidedown2(offset, 0));
				}
				offset += 100;
				departuresToViewNum.put(
						departure.getVehicle_id()
								+ departure.getExpected_mins(), count);
				count++;
			}
			mCursor.moveToNext();
		}
		setListVisibility(true, false, false, "");
		if (mUpdateListener != null)
			mUpdateListener.onDepartureUpdated(departures);
	}

	private void clearDepartureListLayout() {
		mDepartureListParent.removeAllViews();
	}

	private void clearMaps() {
		if (departures != null)
			departures.clear();
		departures = new LinkedHashMap<String, ArrayList<Departure>>();
		// map vehicle id to view position
		stopPointToVehiIdToViewPos = new LinkedHashMap<String, Map<String, Integer>>();
		departuresToViewNum = new HashMap<String, Integer>();
		stoppointToHeight = new HashMap<String, Integer>();

		stoppointToView = new HashMap<String, ViewGroup>();
	}

	public Animation setLayoutAnim_slidedown2(long startOffset,
			long initialOffset) {

		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		// Animation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
		animation.setDuration(700);
		animation.setStartOffset(startOffset + initialOffset);

		return animation;
	}

	public Animation setLayoutAnim_slidedown(long startOffset) {

		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		// Animation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
		animation.setDuration(700);
		animation.setStartOffset(startOffset + 100);
		animation.setZAdjustment(Animation.ZORDER_BOTTOM);

		return animation;
	}

	private View setStopPointItemProperties(View view, Cursor cursor) {
		LocationItemView headerItem = (LocationItemView) view
				.findViewById(R.id.display_stoppoint_stoppoint);

		headerItem.setLocationInfo(cursor);

		final Uri uri = MyLocationContentProvider.getUriFromCursor(cursor);

		// Don't let user click header. StickyScrollView not ready for that yet.
		headerItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setLocation(uri);
				dispatchOnLocationSet(uri);
			}
		});

		View headWrapper = view
				.findViewById(R.id.display_stoppoint_stoppoint_wrap);
		headWrapper.setTag("sticky-nonconstant");

		return view;
	}

	public MyCheckedChangeListener getStopFavoriteCheckBoxListener(
			final AbstractStop stop) {
		// MyCheckedChangeListener listener = new MyCheckedChangeListener() {
		// @Override
		// public void onCheckedChanged(boolean isChecked) {
		// if (isChecked) {
		// DatabaseAdapter.getInstance(getActivity()).setFavorite(
		// stop, true);
		// } else {
		// DatabaseAdapter.getInstance(getActivity()).setFavorite(
		// stop, false);
		// }
		// }
		// };
		// return listener;
		// TODO implement
		return null;
	}

	private String getCurrTimeString() {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		int am_pm = c.get(Calendar.AM_PM);
		String ampm = (am_pm == Calendar.AM) ? " AM" : " PM";
		String min = "" + minute;
		String sec = "" + second;
		int hourInt = (hour == 0) ? 12 : hour;
		min = (min.length() > 1) ? min : "0" + min; // put zero if front of
													// single digit minutes
		sec = (sec.length() > 1) ? sec : "0" + sec;

		mCurrTimeString = "" + hourInt + ":" + min + ":" + sec + ampm;

		return mCurrTimeString;
	}

	private View setDepartureItemProperties(View view, Object obj) {
		TextView routeId = (TextView) view
				.findViewById(R.id.displaystopitem_id);
		TextView name = (TextView) view.findViewById(R.id.displaystopitem_name);
		TextView tripHeadsign = (TextView) view
				.findViewById(R.id.displaystopitem_tripheadsign);
		TextView minutesNumber = (TextView) view
				.findViewById(R.id.displaystopitem_minutes_numberr);
		TextView minutesText = (TextView) view
				.findViewById(R.id.displaystopitem_minutes_text);
		View button = view.findViewById(R.id.departurelistitem_button);
		ImageView istop = (ImageView) view.findViewById(R.id.istop);
		if (obj == null) {
			Log.e("DisplayStops", "Array object is null");
		} else if (obj instanceof Departure) {
			Departure departure = (Departure) obj;
			String headsign[] = departure.getHeadsign().split(" ");

			int mins = departure.getExpected_mins();
			routeId.setText(headsign[0]);
			String nameText = headsign[1];
			for (int i = 2; i < headsign.length; i++) {
				nameText += " " + headsign[i];
			}
			if (departure.getTrip() != null)
				tripHeadsign.setText(departure.getTrip().getTrip_headsign());
			else
				tripHeadsign.setText("");
			name.setText(nameText);
			if (mins < 2) {
				if (mins < 1) {
					minutesNumber.setText("due");
					minutesText.setText("");
				} else {
					minutesNumber.setText("" + mins);
					minutesText.setText(" min");
				}
			} else {
				minutesNumber.setText("" + mins);
				minutesText.setText(" min");
			}
			button.setOnClickListener(getStopListener(departure));
			// button.setOnLongClickListener(getLongClicListener(view));

			boolean isIstop = departure.isIstop();
			istop.setVisibility(departure.isIstop() ? View.VISIBLE : View.GONE);
			if (isIstop) {
				button.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage("This stop is an iStop. No identification or fare is required to board this bus!");
						builder.show();
						return true;
					}
				});
			}
		}
		view.setClickable(true);
		return view;
	}

	// private OnLongClickListener getLongClicListener(View view) {
	// OnLongClickListener listener = new OnLongClickListener() {
	// @Override
	// public boolean onLongClick(View v) {
	// v.startAnimation(setLayoutAnim_swap(v.getTop()));
	// return true;
	// }
	// };
	// return listener;
	// }

	public AnimationSet setLayoutAnim_swap(int change) {

		AnimationSet set = new AnimationSet(true);

		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, -1.0f * change);
		// Animation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, -1.0f
		// * (change));
		// Animation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
		animation.setDuration(700);
		animation.setZAdjustment(Animation.ZORDER_BOTTOM);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		set.addAnimation(animation);

		// LayoutAnimationController controller = new
		// LayoutAnimationController(set, 0.05f);
		// controller.setOrder(LayoutAnimationController.ORDER_REVERSE);

		return set;
	}

	private OnClickListener getStopListener(final Departure departure) {
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				dispatchOnDepartureClick(departure);
			}

		};
		return listener;
	}

	private void setLoadingBoxVisibility(boolean b) {
		int visibility = (b) ? View.VISIBLE : View.INVISIBLE;
		LinearLayout loadbox = (LinearLayout) mView
				.findViewById(R.id.display_stop_loadingbox);
		loadbox.setVisibility(visibility);

	}

	private void setListVisibility(boolean list, boolean loadingbox,
			boolean nodepartures, String noDeparturesMessage) {
		setNoDeparturedTextVisibility(nodepartures, noDeparturesMessage);
		setLoadingBoxVisibility(loadingbox);
		setListVisibility(list);
	}

	private void setListVisibility(boolean b) {
		int inverse = (b) ? View.VISIBLE : View.INVISIBLE;
		((ViewGroup) mView.findViewById(R.id.display_stops_list))
				.setVisibility(inverse);
	}

	private void setNoDeparturedTextVisibility(boolean b, String message) {
		int visibility = (b) ? View.VISIBLE : View.GONE;
		TextView tv = (TextView) mView
				.findViewById(R.id.display_stops_no_stops);
		tv.setVisibility(visibility);
		tv.setText(message);
	}

	public void setOnDepartureClickListener(OnDepartureClickListener listener) {
		mDepartureClickListener = listener;
	}

	public void dispatchOnDepartureClick(Departure departure) {
		if (mDepartureClickListener != null)
			mDepartureClickListener.onDepartureClick(departure);
	}

	public interface OnDepartureClickListener {
		public void onDepartureClick(Departure departure);
	}

	@Override
	public MyLocation getLocation() {
		return mStop;
	}

	private void setAbstractStop(AbstractStop stop) {
		if (stop != null && !stop.equals(mStop)) {
			// if we have a new stop, clear the departure list
			clearMaps();
			clearDepartureListLayout();

			mStop = stop;
			if (requestPanelIsOpen())
				fillBody();
		}
	}

	@Override
	public void fillBody() {
		if (!isBodySet && mStop != null) {
			refillBody();
		}
	}

	@Override
	protected void refillBody() {
		this.isBodySet = true;
		setDepartures(mStop);
	}

	@Override
	public void setLocation(Uri locationUri) {
		if (locationUri.toString().contains(
				MyLocationContract.AbstractStops.CONTENT_URI.toString())) {
			super.setLocation(locationUri);
			Cursor c = getActivity().getContentResolver().query(locationUri,
					null, null, null, null);
			if (c.moveToFirst()) {
				MyLocation location = MyLocationContentProvider
						.buildLocationFromCursor(c);
				setAbstractStop((AbstractStop) location);
			}
			c.close();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loader_id, Bundle bundle) {
		switch (loader_id) {
		case LOADER_ID:
			// This is called when a new Loader needs to be created. This
			// sample only has one Loader, so we don't care activity_about the ID.
			// First, pick the base URI to use depending on whether we are
			// currently filtering.

			ArrayList<String> ids = bundle
					.getStringArrayList(EXTRA_STOPPOINT_IDS);

			String select = "";
			int count = ids.size();
			String[] selectArgs = new String[count];

			for (int i = 0; i < count; i++) {
				selectArgs[i] = ids.get(i);

				select += MyLocationContract.StopPoints.ID + " = ?";
				if (i < count - 1)
					select += " OR ";
			}

			Uri baseUri = StopPoints.CONTENT_URI;

			CursorLoader cursorLoader = new CursorLoader(getActivity(),
					baseUri, null, select, selectArgs,
					MyLocationContract.StopPoints.ID + " ASC");
			return cursorLoader;
		default:
			return super.onCreateLoader(loader_id, bundle);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		super.onLoadFinished(loader, cursor);
		switch (loader.getId()) {
		case LOADER_ID:
			mCursor = cursor;
			if (mRefreshDepartures)
				fillDeparturesLinearLayout(true);
			mRefreshDepartures = false;
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		super.onLoaderReset(loader);
		switch (loader.getId()) {
		case LOADER_ID:
			mCursor = null;
			break;
		}
	}

	@Override
	public boolean shouldReInitialize(Uri location) {
		return !location.toString().contains(
				MyLocationContract.AbstractStops.CONTENT_URI.toString());
	}

}