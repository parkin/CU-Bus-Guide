package com.teamparkin.mtdapp.views;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.teamparkin.mtdapp.MySuggestionCursorAdapter;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.adapters.MyExpandableLocationCursorAdapter;
import com.teamparkin.mtdapp.contentproviders.MyLocationContentProvider;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.dataclasses.MyCurrLocation;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.dataclasses.TripPlanParameters;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.listeners.OnOriginDestinationChangedListener;
import com.teamparkin.mtdapp.util.Util;

public class TripPlannerHead extends LinearLayout {
	private static final String TAG = TripPlannerHead.class.getSimpleName();

	protected static final String TIMEPICKER_TAG = TAG + "_TIME_PICKER";
	protected static final String DATEPICKER_TAG = TAG + "_DATE_PICKER";

	private View mView;

	private FragmentActivity mActivity;

	private ArrayAdapter<String> mDepArrAdapter;
	private ArrayAdapter<String> mWalkAdapter;
	private ArrayAdapter<String> mLeastAdapter;

	private TripPlanParameters mParameters;

	private LocationManager locationManager;

	private MyCurrLocation mCurrLocation = MyCurrLocation.getInstance();

	private boolean textWasSet;

	private SearchView mAutoCompOrigin;
	private String mCurOrigSearchViewFilter;

	private SearchView mAutoCompDest;
	private String mCurrDestSearchViewFilter;

	private OnOriginDestinationChangedListener mOriginDestinationChangedListener;

	private boolean textWasSetDestination;

	private MySuggestionCursorAdapter mOrigSuggAdapter;
	private MySuggestionCursorAdapter mDestSuggAdapter;

	public TripPlannerHead(FragmentActivity activity) {
		this(activity, null);
		mActivity = activity;
	}

	public TripPlannerHead(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mView = inflater.inflate(R.layout.tripplanselect_head, this, true);

		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		mOrigSuggAdapter = new MySuggestionCursorAdapter(context, null, 0);
		mDestSuggAdapter = new MySuggestionCursorAdapter(context, null, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public TripPlannerHead(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setFragmentActivity(FragmentActivity activity) {
		mActivity = activity;
	}

	public void initializeViewFromParams(TripPlanParameters params) {
		mDepArrAdapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, params.getDepArrArray());
		mDepArrAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mLeastAdapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, params.getMinimizeArray());
		mLeastAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mWalkAdapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, params.getWalkArray());
		mWalkAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Button dateTimePickButton = (Button) mView
				.findViewById(R.id.tp_datetime_button);
		dateTimePickButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateTimeDialog();
			}

		});
		updateDateTimeText(params.calendar, params.getDepArrPos());

		Button optionsButton = (Button) mView
				.findViewById(R.id.tp_options_button);
		optionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showOptionsDialog();
			}
		});

		// Options button

		// origin autocomplete & button
		ImageButton originHelpButton = (ImageButton) mView
				.findViewById(R.id.tp_button_origin);
		originHelpButton.setOnClickListener(getHelpButtonOnClickListener(true));

		mAutoCompOrigin = (SearchView) mView
				.findViewById(R.id.tp_search_view_origin);
		// Theme the SearchView's AutoCompleteTextView drop down. For some
		// reason this wasn't working in styles.xml
		// SearchAutoComplete autoCompleteTextView = (SearchAutoComplete)
		// mAutoCompOrigin
		// .findViewById(R.id.search_src_text);
		// if (autoCompleteTextView != null) {
		// autoCompleteTextView
		// .setDropDownBackgroundResource(R.drawable.abc_search_dropdown_light);
		// }
		SearchManager searchManager = (SearchManager) mActivity
				.getSystemService(Context.SEARCH_SERVICE);
		// Assumes current activity is the searchable activity
		SearchableInfo searchableInfo = searchManager
				.getSearchableInfo(mActivity.getComponentName());
		mAutoCompOrigin.setSearchableInfo(searchableInfo);
		mAutoCompOrigin.setSubmitButtonEnabled(false);
		mAutoCompOrigin.setSuggestionsAdapter(mOrigSuggAdapter);
		mAutoCompOrigin
				.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

					@Override
					public boolean onQueryTextSubmit(String text) {
						// Return true to tell android we are handling the
						// submission here!
						return true;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						String newFilter = !TextUtils.isEmpty(newText) ? newText
								: null;
						// Don't do anything if the filter hasn't actually
						// changed.
						// Prevents restarting the loader when restoring state.
						if (mCurOrigSearchViewFilter == null
								&& newFilter == null) {
							return true;
						}
						if (mCurOrigSearchViewFilter != null
								&& mCurOrigSearchViewFilter.equals(newFilter)) {
							return true;
						}
						mCurOrigSearchViewFilter = newFilter;

						// If we set the text programatically, return true so no
						// suggestions pop up.
						if (textWasSet) {
							textWasSet = false;
							return true;
						}
						if (hasOrigin())
							removeOrigin();

						dispatchOnOriginTextChanged(newText);
						return true;
					}

				});
		mAutoCompOrigin
				.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

					CursorAdapter ca = mAutoCompOrigin.getSuggestionsAdapter();

					@Override
					public boolean onSuggestionSelect(int position) {
						return false;
					}

					@Override
					public boolean onSuggestionClick(int position) {
						Cursor c = (Cursor) ca.getItem(position);
						String id = c.getString(c
								.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
						int i = 0;
						for (String string : c.getColumnNames()) {
							Log.i(TAG, "column[" + i + "]: " + string);
							i++;
						}
						// return true to tell android that we are handling the
						// click here!
						Cursor c2 = getContext().getContentResolver().query(
								Uri.parse(id), null, null, null, null);
						if (c2.moveToFirst()) {
							MyLocation loc = MyLocationContentProvider
									.buildLocationFromCursor(c2);
							setOrigin(loc);
						}
						c2.close();
						return true;
					}
				});

		// destination autocomplete & progress bar & help button
		ImageButton destinationHelpButton = (ImageButton) mView
				.findViewById(R.id.tp_button_destination);
		destinationHelpButton
				.setOnClickListener(getHelpButtonOnClickListener(false));

		mAutoCompDest = (SearchView) mView
				.findViewById(R.id.tp_search_view_destination);
		mAutoCompDest.setSubmitButtonEnabled(false);
		// Theme the SearchView's AutoCompleteTextView drop down. For some
		// reason this wasn't working in styles.xml
		// autoCompleteTextView = (SearchAutoComplete) mAutoCompDest
		// .findViewById(R.id.search_src_text);
		// if (autoCompleteTextView != null) {
		// autoCompleteTextView
		// .setDropDownBackgroundResource(R.drawable.abc_search_dropdown_light);
		// }
		// Assumes current activity is the searchable activity
		mAutoCompDest.setSearchableInfo(searchableInfo);
		mAutoCompDest.setSuggestionsAdapter(mDestSuggAdapter);
		mAutoCompDest
				.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

					@Override
					public boolean onQueryTextSubmit(String text) {
						// Return true to tell android we are handling the
						// submission here!
						return true;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						String newFilter = !TextUtils.isEmpty(newText) ? newText
								: null;
						// Don't do anything if the filter hasn't actually
						// changed.
						// Prevents restarting the loader when restoring state.
						if (mCurrDestSearchViewFilter == null
								&& newFilter == null) {
							return true;
						}
						if (mCurrDestSearchViewFilter != null
								&& mCurrDestSearchViewFilter.equals(newFilter)) {
							return true;
						}
						mCurrDestSearchViewFilter = newFilter;

						// If we set the text programatically, return true so no
						// suggestions pop up.
						if (textWasSetDestination) {
							textWasSetDestination = false;
							return true;
						}
						// otherwise, get rid of origin
						if (hasDestination())
							removeDestination();

						dispatchOnDestinationTextChanged(newText);
						return true;
					}
				});
		mAutoCompDest
				.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

					CursorAdapter ca = mAutoCompDest.getSuggestionsAdapter();

					@Override
					public boolean onSuggestionSelect(int position) {
						return false;
					}

					@Override
					public boolean onSuggestionClick(int position) {
						Cursor c = (Cursor) ca.getItem(position);
						String id = c.getString(c
								.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
						// return true to tell android that we are handling the
						// click here!
						Cursor c2 = getContext().getContentResolver().query(
								Uri.parse(id), null, null, null, null);
						if (c2.moveToFirst()) {
							MyLocation loc = MyLocationContentProvider
									.buildLocationFromCursor(c2);
							setDestination(loc);
						}
						c2.close();
						return true;
					}
				});

		mParameters = (TripPlanParameters) Util.copyParcelable(params);
		if (mParameters.origin != null) {
			setOrigin(mParameters.origin);
		} else {
			removeOrigin();
		}
		if (mParameters.destination != null) {
			setDestination(mParameters.destination);
		} else {
			removeDestination();
		}
	}

	private OnClickListener getHelpButtonOnClickListener(final boolean origin) {
		return new OnClickListener() {
			@Override
			public void onClick(View view) {
				final CharSequence[] items = { "My Current Location",
						"My Favorites" };
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				if (origin)
					builder.setTitle("Choose Starting Point");
				else
					builder.setTitle("Choose Ending Point");
				builder.setItems(items, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {
						if (item == 0) {
							startLocationService();
							if (origin)
								setOrigin(mCurrLocation);
							else
								setDestination(mCurrLocation);
						} else if (item == 1) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									mActivity);

							final MyExpandableLocationCursorAdapter mCursorAdapter = new MyExpandableLocationCursorAdapter(
									getContext(), null, 0,
									R.layout.mylocationlistitem_card,
									R.id.mylocationlistitem_parent,
									R.id.mylocationlistitem_child);
							mCursorAdapter
									.setActionViewResId(R.id.mylocationlistitem_overflow_button);

							builder.setTitle("My Favorites");
							builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									Cursor c = mCursorAdapter.getCursor();
									if (c != null)
										c.close();
								}
							});
							Cursor c = getContext()
									.getContentResolver()
									.query(MyLocationContract.MyLocation.CONTENT_URI,
											null,
											MyLocationContract.MyLocation.FAVORITE
													+ "=1",
											null,
											MyLocationContract.MyLocation.TYPE
													+ " ASC, "
													+ MyLocationContract.MyLocation.NAME
													+ " ASC");
							mCursorAdapter.swapCursor(c);
							builder.setAdapter(mCursorAdapter,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
										}
									});
							final AlertDialog alert = builder.create();
							mCursorAdapter
									.setOnLocationSelectedListener(new MyFragmentsListener.OnLocationSelectedListener() {
										@Override
										public void onLocationSelected(
												Uri locationUri) {
											Cursor c = getContext()
													.getContentResolver()
													.query(locationUri, null,
															null, null, null);
											MyLocation location = null;
											if (c.moveToFirst())
												location = MyLocationContentProvider
														.buildLocationFromCursor(c);
											if (origin)
												setOrigin(location);
											else
												setDestination(location);

											c.close();

											alert.cancel();
										}
									});
							alert.show();

						}
					}

				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		};
	}

	public void removeOrigin() {
		mParameters.origin = null;
		mAutoCompOrigin.setBackgroundColor(Color.TRANSPARENT);
		textWasSet = true;
		mAutoCompOrigin.setQuery("", false);
		dispatchOnTripParametersUpdated(mParameters);
		dispatchOnOriginRemoved();
	}

	public void removeDestination() {
		mParameters.destination = null;
		mAutoCompDest.setBackgroundColor(Color.TRANSPARENT);
		textWasSetDestination = true;
		mAutoCompDest.setQuery("", false);
		dispatchOnTripParametersUpdated(mParameters);
		dispatchOnDestinationRemoved();
	}

	public void setOrigin(MyLocation origin) {
		textWasSet = true;
		if (mParameters != null)
			mParameters.origin = origin;
		int color = Color.GREEN;
		color = Color.argb(150, Color.red(color), Color.green(color),
				Color.blue(color));
		mAutoCompOrigin.setBackgroundColor(color);
		mAutoCompOrigin.setQuery(origin.getName(), false);

		dispatchOnTripParametersUpdated(mParameters);
		dispatchOnOriginSet(origin);
		// only request trips if the user changed something.
		if (mParameters != null && mParameters.hasAllParameters())
			dispatchOnTripRequested(mParameters);
	}

	public boolean hasDestination() {
		return mParameters.destination != null;
	}

	public boolean hasOrigin() {
		return mParameters.origin != null;
	}

	public void setDestination(MyLocation destination) {
		textWasSetDestination = true;
		if (mParameters != null)
			mParameters.destination = destination;
		int color = Color.MAGENTA;
		color = Color.argb(150, Color.red(color), Color.green(color),
				Color.blue(color));
		mAutoCompDest.setBackgroundColor(color);
		mAutoCompDest.setQuery(destination.getName(), false);

		dispatchOnTripParametersUpdated(mParameters);
		dispatchOnDestinationSet(destination);
		if (mParameters != null && mParameters.hasAllParameters())
			dispatchOnTripRequested(mParameters);
	}

	private void startLocationService() {
		Location lastKnownLocation = null;
		try {
			lastKnownLocation = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastKnownLocation == null)
				lastKnownLocation = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastKnownLocation == null)
				lastKnownLocation = locationManager
						.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = locationManager.getBestProvider(criteria, true);
		if (provider == null) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		// 0s refresh, only after changing by 0m
		locationManager.requestLocationUpdates(provider, 1000, 10,
				locationListener);

		if (lastKnownLocation == null) {
			Log.e(TAG, "Last known location null");
		} else {
			mCurrLocation.setLocation(lastKnownLocation);
		}
	}

	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location
			// provider.
			mCurrLocation.setLocation(location);
			mParameters.origin = mCurrLocation;
			dispatchOnTripParametersUpdated(mParameters);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	private OnTripParametersUpdateListener mTripParamsUpdateListener;

	private OnAutoCompleteTextChange mTextChangeListener;

	private void showOptionsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

		builder.setTitle("Trip Options");

		// Get the layout inflater
		LayoutInflater inflater = mActivity.getLayoutInflater();

		View v = inflater.inflate(R.layout.tripplanner_options_alert, null);
		final Spinner spinner = (Spinner) v
				.findViewById(R.id.tp_options_least_spinner);
		spinner.setAdapter(mLeastAdapter);
		spinner.setSelection(mParameters.getMinimizeArrPos());

		final Spinner spinnerWalk = (Spinner) v
				.findViewById(R.id.tp_options_walk_spinner);
		spinnerWalk.setAdapter(mWalkAdapter);
		spinnerWalk.setSelection(mParameters.getWalkArrPos());
		builder.setView(v)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						boolean wasAChange = false;
						if (mParameters.getMinimizeArrPos() != spinner
								.getSelectedItemPosition()) {
							mParameters.setMinimizeArrPos(spinner
									.getSelectedItemPosition());
							wasAChange = true;
						}
						if (mParameters.getWalkArrPos() != spinnerWalk
								.getSelectedItemPosition()) {
							mParameters.setWalkArrPos(spinnerWalk
									.getSelectedItemPosition());
							wasAChange = true;
						}

						if (wasAChange) {
							dispatchOnTripParametersUpdated(mParameters);
							if (mParameters.hasAllParameters())
								dispatchOnTripRequested(mParameters);
						}
						dialog.cancel();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
		builder.show();

	}

	private void showDateTimeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

		builder.setTitle("Choose Date and Time");

		// Get the layout inflater
		LayoutInflater inflater = mActivity.getLayoutInflater();

		View v = inflater.inflate(R.layout.tripplanner_datetime_alert, null);
		final Spinner spinner = (Spinner) v
				.findViewById(R.id.tp_datetime_alert_spinner);
		spinner.setAdapter(mDepArrAdapter);
		spinner.setSelection(mParameters.getDepArrPos());

		final Calendar calendar = (Calendar) mParameters.calendar.clone();

		final Button timeButton = (Button) v
				.findViewById(R.id.tp_datetime_alert_timepicker);
		timeButton.setText(Util.getTimeText(calendar));
		timeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog dialog = TimePickerDialog.newInstance(
						new TimePickerDialog.OnTimeSetListener() {

							@Override
							public void onTimeSet(RadialPickerLayout view,
									int hourOfDay, int minute) {
								calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
								calendar.set(Calendar.MINUTE, minute);
								timeButton.setText(Util.getTimeText(calendar));
							}
						}, mParameters.calendar.get(Calendar.HOUR_OF_DAY),
						mParameters.calendar.get(Calendar.MINUTE), false, true);
				dialog.show(mActivity.getSupportFragmentManager(),
						TIMEPICKER_TAG);
			}
		});

		final Button dateButton = (Button) v
				.findViewById(R.id.tp_datetime_alert_datepicker);
		dateButton.setText(Util.getDateText(mParameters.calendar));
		dateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialog dialog = DatePickerDialog.newInstance(
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(
									DatePickerDialog datePickerDialog,
									int year, int month, int day) {
								calendar.set(Calendar.YEAR, year);
								calendar.set(Calendar.MONTH, month);
								calendar.set(Calendar.DATE, day);
								dateButton.setText(Util.getDateText(calendar));
							}
						}, calendar.get(Calendar.YEAR), calendar
								.get(Calendar.MONTH), calendar
								.get(Calendar.DATE), true);
				dialog.show(mActivity.getSupportFragmentManager(),
						DATEPICKER_TAG);
			}
		});

		Button nowButton = (Button) v
				.findViewById(R.id.tp_datetime_alert_reload);
		nowButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendarRefresh = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY,
						calendarRefresh.get(Calendar.HOUR_OF_DAY));
				calendar.set(Calendar.MINUTE,
						calendarRefresh.get(Calendar.MINUTE));
				calendar.set(Calendar.YEAR, calendarRefresh.get(Calendar.YEAR));
				calendar.set(Calendar.MONTH,
						calendarRefresh.get(Calendar.MONTH));
				calendar.set(Calendar.DATE, calendarRefresh.get(Calendar.DATE));
				timeButton.setText(Util.getTimeText(calendarRefresh));
				dateButton.setText(Util.getDateText(calendarRefresh));
			}
		});

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(v)
				// Add action buttons
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						boolean wasAChange = false;
						if (!mParameters.calendar.equals(calendar)) {
							mParameters.calendar = calendar;
							wasAChange = true;
						}
						if (mParameters.getDepArrPos() != spinner
								.getSelectedItemPosition()) {
							mParameters.setDepArrPos(spinner
									.getSelectedItemPosition());
							wasAChange = true;
						}

						updateDateTimeText(mParameters.calendar,
								mParameters.getDepArrPos());

						if (wasAChange) {
							dispatchOnTripParametersUpdated(mParameters);
							if (mParameters.hasAllParameters())
								dispatchOnTripRequested(mParameters);
						}

						dialog.cancel();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
		builder.show();
	}

	/**
	 * Updates the date text. Note that January is month 0.
	 * 
	 * @param month
	 *            - the month that was set (0-11) for compatibility with
	 *            Calendar.
	 * @param day
	 * @param year
	 * @param hour
	 * @param minute
	 */
	private void updateDateTimeText(Calendar calendar, int depArrSpinnerPos) {
		Button b = (Button) mView.findViewById(R.id.tp_datetime_button);
		String text = "";
		text += mDepArrAdapter.getItem(depArrSpinnerPos);

		String dateText = Util.getDateText(calendar);
		text += " " + dateText;

		String timeString = Util.getTimeText(calendar);
		text += ", " + timeString;

		b.setText(text);
	}

	public void setOnOriginDestinationChangedListener(
			OnOriginDestinationChangedListener listener) {
		mOriginDestinationChangedListener = listener;
	}

	private void dispatchOnOriginSet(MyLocation origin) {
		if (mOriginDestinationChangedListener != null)
			mOriginDestinationChangedListener.onOriginSet(origin);
	}

	private void dispatchOnDestinationSet(MyLocation destination) {
		if (mOriginDestinationChangedListener != null)
			mOriginDestinationChangedListener.onDestinationSet(destination);
	}

	private void dispatchOnOriginRemoved() {
		if (mOriginDestinationChangedListener != null)
			mOriginDestinationChangedListener.onOriginRemoved();
	}

	private void dispatchOnDestinationRemoved() {
		if (mOriginDestinationChangedListener != null)
			mOriginDestinationChangedListener.onDestinationRemoved();
	}

	public void setOnTripParametersUpdateListener(
			OnTripParametersUpdateListener listener) {
		mTripParamsUpdateListener = listener;
	}

	private void dispatchOnTripParametersUpdated(TripPlanParameters parameters) {
		if (mTripParamsUpdateListener != null)
			mTripParamsUpdateListener.onTripParametersUpdated(parameters);
	}

	private void dispatchOnTripRequested(TripPlanParameters params) {
		if (mTripParamsUpdateListener != null)
			mTripParamsUpdateListener.onTripRequested(params);
	}

	public interface OnTripParametersUpdateListener {
		public void onTripParametersUpdated(TripPlanParameters parameters);

		public void onTripRequested(TripPlanParameters params);
	}

	public void setOnAutoCompleteTextChange(OnAutoCompleteTextChange listener) {
		mTextChangeListener = listener;
	}

	public void swapOriginCursor(Cursor newCursor) {
		mOrigSuggAdapter.swapCursor(newCursor);
	}

	public void swapDestinationCursor(Cursor newCursor) {
		mDestSuggAdapter.swapCursor(newCursor);
	}

	private void dispatchOnOriginTextChanged(String newText) {
		if (mTextChangeListener != null)
			mTextChangeListener.onOriginTextChanged(newText);
	}

	private void dispatchOnDestinationTextChanged(String newText) {
		if (mTextChangeListener != null)
			mTextChangeListener.onDestinationTextChanged(newText);
	}

	public interface OnAutoCompleteTextChange {
		/**
		 * Called when the origin searchView text changes.
		 * 
		 * @param newText
		 * @return
		 */
		public void onOriginTextChanged(String newText);

		public void onDestinationTextChanged(String newText);
	}

}
