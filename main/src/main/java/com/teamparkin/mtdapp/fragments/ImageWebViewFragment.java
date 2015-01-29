package com.teamparkin.mtdapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.views.StreetViewTouchImageView;

public class ImageWebViewFragment extends MyFragment {
	private static final String TAG = ImageWebViewFragment.class
			.getSimpleName();

	private Activity mActivity;
	private View mView;

	private Bundle mBundle;

	long maxCacheSize = 1024 * 1024 * 2;

	private StreetViewTouchImageView mTouchImageView;

	public void setBundle(Bundle bundle) {
		mBundle = bundle;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		Picasso.with(activity).setDebugging(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = (View) inflater.inflate(R.layout.image_webview, container,
				false);
		mTouchImageView = (StreetViewTouchImageView) mView
				.findViewById(R.id.image_webview_image);
		if (savedInstanceState == null) {

			initializeFromBundle(mBundle);

		} else {
			initializeFromBundle(savedInstanceState);
		}
		return mView;
	}

	public void initializeFromBundle(Bundle bundle) {
		String mapurl = bundle.getString("mapurl");
		Log.i(TAG, "mapurl: " + mapurl);
		mTouchImageView.setUrl(mapurl);
		Picasso.with(mActivity).load(mapurl).into(mTouchImageView);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("mapurl", mTouchImageView.getUrl());
	}
}
