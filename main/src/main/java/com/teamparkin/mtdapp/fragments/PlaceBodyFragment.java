package com.teamparkin.mtdapp.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContentProvider;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.dataclasses.Place;
import com.teamparkin.mtdapp.restadapters.GoogleStreetViewAPIAdapter;
import com.teamparkin.mtdapp.views.StreetViewTouchImageView;

public class PlaceBodyFragment extends MyLocationSlideupHeadFragment {
	@SuppressWarnings("unused")
	private static final String TAG = PlaceBodyFragment.class.getSimpleName();

	private static final int LAYOUT_ID = R.layout.place_body_frag;

	private StreetViewTouchImageView mStreetViewImage;
	private MyLocation mLocation;

	public PlaceBodyFragment() {
	}

	@Override
	protected int getLayoutId() {
		return LAYOUT_ID;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mLocation != null)
			outState.putParcelable("location", mLocation);
	}

	@Override
	protected void initializeBodyView(View mainView, Bundle savedInstanceState) {
		mStreetViewImage = (StreetViewTouchImageView) mainView
				.findViewById(R.id.place_body_streetview);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey("location"))
			mLocation = savedInstanceState.getParcelable("location");

		// TODO might have to uncomment
		// if (mLocation != null) {
		// isBodySet = false;
		// setLocationInfo(mLocation);
		// fillBody();
		// }
	}

	@Override
	public void setLocation(Uri locationUri) {
		super.setLocation(locationUri);
		Cursor c = getActivity().getContentResolver().query(locationUri, null,
				null, null, null);
		if (c.moveToFirst())
			mLocation = MyLocationContentProvider.buildLocationFromCursor(c);
		c.close();
		if (requestPanelIsOpen() && mLocation != null)
			fillBody();
	}

	public MyLocation getLocation() {
		return mLocation;
	}

	@Override
	protected void refillBody() {
	}

	@Override
	public void fillBody() {
		if (!isBodySet && mLocation != null) {
			isBodySet = true;
			Picasso.with(getActivity()).setDebugging(true);
			Picasso.with(getActivity())
					.load(GoogleStreetViewAPIAdapter.getInstance(getActivity())
							.getStreetViewUrl(mLocation))
					.placeholder(
							GoogleStreetViewAPIAdapter
									.getDefaultDrawable(getActivity()))
					.into(mStreetViewImage);
		}
	}

	@Override
	public boolean shouldReInitialize(Uri location) {
		return !location.toString().contains(
				MyLocationContract.Places.CONTENT_URI.toString());
	}

}
