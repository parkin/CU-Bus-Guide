package com.teamparkin.mtdapp.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.dataclasses.Itinerary;
import com.teamparkin.mtdapp.dataclasses.Leg;
import com.teamparkin.mtdapp.dataclasses.TripPlanParameters;
import com.teamparkin.mtdapp.util.Util;
import com.teamparkin.mtdapp.views.ItineraryView;

public class ItineraryBodyFrag extends SlideupHeadFragment {
	private static final String TAG = ItineraryBodyFrag.class.getSimpleName();

	private Itinerary mItinerary;
	private GridLayout mGridLayout;

	private TripPlanParameters mTripParams;

	private ItineraryView mItineraryView;

	private OnItineraryChangedListener mItineraryChangedListener;

	private static int COLUMN_3_MARGIN_LEFT_PIX = 3;

	private static int COLUMN_1_PADDING_DIP = 10;

	private static int COLUMN_3_PADDING_TOP_BOTTOM_DIP = 15;
	private static int COLUMN_3_PADDING_LEFT_RIGHT_DIP = 6;

	public ItineraryBodyFrag() {
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.itinerary_body_frag, null);

		mItineraryView = (ItineraryView) view
				.findViewById(R.id.itinerary_slideup_head_itinerary);

		setDragView(mItineraryView);

		mGridLayout = (GridLayout) view.findViewById(R.id.itin_body_body);

		return view;
	}

	public void setItinerary(Itinerary itinerary, TripPlanParameters tripParams) {
		mItinerary = (Itinerary) Util.copyParcelable(itinerary);
		mTripParams = (TripPlanParameters) Util.copyParcelable(tripParams);
		isBodySet = false;
		if (mItineraryView != null) {
			mItineraryView.setItinerary(mItinerary);
			if (requestPanelIsOpen())
				fillBody();
		}
		dispatchItinerarySet(itinerary);
	}

	public void fillBody() {
		if (!isBodySet && mItinerary != null && mTripParams != null
				&& mTripParams.hasAllParameters()) {
			isBodySet = true;
			fillGridLayout(mItinerary, mTripParams);
		}
	}

	private void fillGridLayout(Itinerary itinerary,
			TripPlanParameters tripParams) {
		if (mGridLayout == null || itinerary == null)
			return;

		Log.i(TAG, "fillGridLayout");

		mGridLayout.removeAllViews();

		String name = "";
		String time = "";

		int row = 0;
		// add origin stuff
		time = Util.getTimeText(itinerary.getLeg(0).getBegin().getCalendar());
		TextView tv = getNewTimeTextView(time, row);
		mGridLayout.addView(tv);

		TextView tv2 = getNewNameTextView(tripParams.origin.getName(), row);
		mGridLayout.addView(tv2);

		row++;

		// add first divider
		mGridLayout.addView(getNewDividerView(row));

		row++;

		int i = 0;
		int count = itinerary.getLegs().size();
		for (Leg leg : itinerary.getLegs()) {
			// Add the travel info for the leg.
			// add the image for the travel info
			mGridLayout.addView(getNewLegImageView(leg.getImageResourceId(),
					row));

			// add the description of the travel info
			mGridLayout.addView(getNewLegDescriptionView(leg, row));

			row++;

			// add a divider
			mGridLayout.addView(getNewDividerView(row));

			// add tile to origin
			View v = getNewTiledView(leg.getMainColor(), row - 3, 4);
			mGridLayout.addView(v);

			row++;

			// Now add the end point info
			if (i < count - 1) {
				name = leg.getEnd().getName();
				time = Util.getTimeText(itinerary.getLeg(i + 1).getBegin()
						.getCalendar());
			} else {
				name = mTripParams.destination.getName();
				time = Util.getTimeText(leg.getEnd().getCalendar());
			}

			mGridLayout.addView(getNewTimeTextView(time, row));

			tv2 = getNewNameTextView(name, row);
			if (i < count - 1)
				tv2.setBackgroundColor(Color.parseColor("#f0f0f0"));
			mGridLayout.addView(tv2);

			row++;

			// add the endpoint info divider
			if (i < count - 1) {
				mGridLayout.addView(getNewDividerView(row));
				row++;
			}

			i++;
		}

	}

	private View getNewTiledView(String color, int row, int span) {

		// Code to tile something vertically

		// View view2 = new View(getActivity());

		ImageView view2 = new ImageView(getActivity());

		if (!color.contains("#"))
			color = "#" + color;

		view2.setBackgroundColor(Color.parseColor(color));

		// BitmapDrawable TileMe = new BitmapDrawable(getResources(),
		// BitmapFactory.decodeResource(getResources(),
		// R.drawable.ic_circle_blue));
		// TileMe.setTileModeY(Shader.TileMode.REPEAT);
		//
		// GradientDrawable d = new GradientDrawable();
		// d.sett

		// view2.setImageDrawable(TileMe);
		// view2.setBackgroundDrawable(TileMe);

		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.height = 0;
		// need this line, otherwise the drawable will expand in width for some
		// reason
		params.width = Util.getPixelsFromDips(getActivity(), 8);
		params.columnSpec = GridLayout.spec(1);
		params.rowSpec = GridLayout.spec(row, span);
		params.setGravity(Gravity.FILL_VERTICAL);
		view2.setLayoutParams(params);

		return view2;
	}

	private View getNewLegDescriptionView(Leg leg, int row) {

		View view = getActivity().getLayoutInflater().inflate(
				R.layout.leg_description, null);

		TextView main = (TextView) view.findViewById(R.id.leg_description_main);
		main.setText(leg.getMainInfo());

		TextView minor = (TextView) view
				.findViewById(R.id.leg_description_minor);
		minor.setText(leg.getMinorInfo());

		int marginLeft = Util.getPixelsFromDips(getActivity(),
				COLUMN_3_MARGIN_LEFT_PIX);
		final int paddingPixels = Util.getPixelsFromDips(getActivity(),
				COLUMN_3_PADDING_TOP_BOTTOM_DIP);
		final int paddingPixelsLR = Util.getPixelsFromDips(getActivity(),
				COLUMN_3_PADDING_LEFT_RIGHT_DIP);

		view.setPadding(paddingPixelsLR, paddingPixels, paddingPixelsLR,
				paddingPixels);
		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.leftMargin = marginLeft;
		params.height = GridLayout.LayoutParams.WRAP_CONTENT;
		params.width = 0;
		params.columnSpec = GridLayout.spec(2);
		params.rowSpec = GridLayout.spec(row);
		params.setGravity(Gravity.LEFT | Gravity.FILL_HORIZONTAL);
		view.setLayoutParams(params);

		return view;
	}

	private View getNewLegImageView(int resId, int row) {
		final int paddingPixels = Util.getPixelsFromDips(getActivity(),
				COLUMN_1_PADDING_DIP);

		ImageView iv = new ImageView(getActivity());
		iv.setBackgroundResource(resId);

		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.setMargins(0, paddingPixels, 0, paddingPixels);
		params.height = GridLayout.LayoutParams.WRAP_CONTENT;
		params.width = GridLayout.LayoutParams.WRAP_CONTENT;
		params.columnSpec = GridLayout.spec(0);
		params.rowSpec = GridLayout.spec(row);
		params.setGravity(Gravity.CENTER);
		iv.setLayoutParams(params);

		return iv;
	}

	private View getNewDividerView(int row) {
		final int dividerHeightPix = 1;
		final int marginLeftPix = Util.getPixelsFromDips(getActivity(),
				COLUMN_3_MARGIN_LEFT_PIX);

		View space = new View(getActivity());
		space.setBackgroundColor(getActivity().getResources().getColor(
				R.color.LightGrey));
		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.leftMargin = marginLeftPix;
		params.height = dividerHeightPix;
		params.width = 0;
		params.columnSpec = GridLayout.spec(2);
		params.rowSpec = GridLayout.spec(row);
		params.setGravity(Gravity.FILL_HORIZONTAL);
		space.setLayoutParams(params);
		return space;
	}

	private TextView getNewTimeTextView(String time, int row) {
		final int paddingPixels = Util.getPixelsFromDips(getActivity(),
				COLUMN_1_PADDING_DIP);

		TextView tv = new TextView(getActivity());
		tv.setText(time);
		tv.setTypeface(null, Typeface.BOLD);
		tv.setPadding(paddingPixels, paddingPixels, paddingPixels,
				paddingPixels);
		GridLayout.LayoutParams param = new GridLayout.LayoutParams();
		param.height = GridLayout.LayoutParams.WRAP_CONTENT;
		param.width = GridLayout.LayoutParams.WRAP_CONTENT;
		param.columnSpec = GridLayout.spec(0);
		param.rowSpec = GridLayout.spec(row);
		param.setGravity(Gravity.CENTER);
		tv.setLayoutParams(param);
		return tv;
	}

	private TextView getNewNameTextView(String name, int row) {
		final int paddingPixels = Util.getPixelsFromDips(getActivity(),
				COLUMN_3_PADDING_TOP_BOTTOM_DIP);
		final int paddingPixelsLR = Util.getPixelsFromDips(getActivity(),
				COLUMN_3_PADDING_LEFT_RIGHT_DIP);
		final int marginLeftPix = Util.getPixelsFromDips(getActivity(),
				COLUMN_3_MARGIN_LEFT_PIX);

		TextView tv = new TextView(getActivity());
		tv.setText(name);
		tv.setPadding(paddingPixelsLR, paddingPixels, paddingPixelsLR,
				paddingPixels);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		GridLayout.LayoutParams param = new GridLayout.LayoutParams();
		param.leftMargin = marginLeftPix;
		param.height = GridLayout.LayoutParams.WRAP_CONTENT;
		param.width = 0;
		param.columnSpec = GridLayout.spec(2);
		param.rowSpec = GridLayout.spec(row);
		param.setGravity(Gravity.FILL_HORIZONTAL | Gravity.CENTER_VERTICAL
				| Gravity.LEFT);
		tv.setLayoutParams(param);
		return tv;
	}

	public Itinerary getItinerary() {
		return mItinerary;
	}

	public TripPlanParameters getTripParameters() {
		return mTripParams;
	}

	@Override
	public View getHeadView() {
		return mItineraryView;
	}

	@Override
	public boolean handleOnBackPressed() {
		// On back should remove this fragment, so let the listener know that
		// the itinerary is being removed.
		dispatchItineraryRemoved();
		return false;
	}

	public void setOnItineraryChangedListener(
			OnItineraryChangedListener listener) {
		mItineraryChangedListener = listener;
	}

	private void dispatchItineraryRemoved() {
		if (mItineraryChangedListener != null)
			mItineraryChangedListener.onItineraryRemoved();
		else
			Log.e(TAG, "dispatchItineraryRemoved listener null");
	}

	private void dispatchItinerarySet(Itinerary itinerary) {
		if (mItineraryChangedListener != null)
			mItineraryChangedListener.onItinerarySet(itinerary);
		else
			Log.e(TAG, "dispatchItineraryChange listener null");
	}

	public interface OnItineraryChangedListener {
		public void onItinerarySet(Itinerary itinerary);

		public void onItineraryRemoved();
	}

}
