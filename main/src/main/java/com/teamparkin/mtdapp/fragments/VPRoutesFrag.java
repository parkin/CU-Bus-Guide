package com.teamparkin.mtdapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VPRoutesFrag extends MyViewPagerFragment {
	@SuppressWarnings("unused")
	private static final String TAG = VPRoutesFrag.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Log.i(TAG, "vp sIS null? " + (savedInstanceState == null)
				+ ", mPA null? " + (mPagerAdapter == null) + ", mPA size: "
				+ mPagerAdapter.getCount());

		for (Fragment fragment : getFragmentManager().getFragments()) {
			if (fragment != null && fragment.getClass() != null)
				Log.i(TAG, "frag: " + fragment.getClass().getSimpleName());
			else if (fragment != null)
				Log.i(TAG, "frag ts: " + fragment.toString());
		}

		// MyViewPagerFragment handles re-adding old fragments after screen
		// rotation. So only create new fragment if no savedInstanceState.
		RouteFragment frag = null;
		if (savedInstanceState == null) {
			frag = new RouteFragment();
			frag.setOnRouteItemClickListener(getNewOnRouteItemClickListener());
			mPagerAdapter.add(frag, ROUTES);
		} else {
			// Make sure MyFragmentListeners are set in each applicable fragment
			// in the viewpager.
			frag = (RouteFragment) mPagerAdapter.getItemByFragmentId(ROUTES);
			Log.i(TAG, "frag null? " + (frag == null));
			if (frag != null)
				frag.setOnRouteItemClickListener(getNewOnRouteItemClickListener());
			frag = (RouteFragment) mPagerAdapter
					.getItemByFragmentId(ROUTES_LVL_1);
			if (frag != null)
				frag.setOnRouteItemClickListener(getNewOnRouteItemClickListener());
		}

		return view;
	}

	private RouteFragment.OnRouteItemClickListener getNewOnRouteItemClickListener() {
		return new RouteFragment.OnRouteItemClickListener() {
			@Override
			public void onRouteMapRequested(Bundle args) {
				routeMapRequested(args);
			}

			@Override
			public void onRouteItemSelected(Bundle args) {
				routeItemSelected(args);
			}
		};
	}

	public void routeItemSelected(Bundle optionsBundle) {
		if (!mPagerAdapter.containsId(ROUTES_LVL_1)) {
			RouteFragment frag = new RouteFragment();
			frag.setBundle(optionsBundle);
			frag.setOnRouteItemClickListener(getNewOnRouteItemClickListener());
			mPagerAdapter.add(frag, ROUTES_LVL_1);
			// mPagerAdapter.notifyDataSetChanged();
		} else {
			RouteFragment frag = (RouteFragment) mPagerAdapter
					.getItem(mPagerAdapter.getIdPosition(ROUTES_LVL_1));
			frag.initializeFromBundle(optionsBundle);
		}
		mViewPager.setCurrentItem(mPagerAdapter.getIdPosition(ROUTES_LVL_1),
				true);
	}

	public void routeMapRequested(Bundle optionsBundle) {
		if (!mPagerAdapter.containsId(ROUTE_MAP)) {
			ImageWebViewFragment frag = new ImageWebViewFragment();
			frag.setBundle(optionsBundle);
			mPagerAdapter.add(frag, ROUTE_MAP);
			// mPagerAdapter.notifyDataSetChanged();
		} else {
			ImageWebViewFragment frag = (ImageWebViewFragment) mPagerAdapter
					.getItem(mPagerAdapter.getIdPosition(ROUTE_MAP));
			frag.initializeFromBundle(optionsBundle);
		}
		mViewPager.setCurrentItem(mPagerAdapter.getIdPosition(ROUTE_MAP), true);
	}

	@Override
	public boolean shouldShowActionBar() {
		// hide the action bar when the route map is displayed
		return mViewPager.getCurrentItem() != 2;
	}

}
