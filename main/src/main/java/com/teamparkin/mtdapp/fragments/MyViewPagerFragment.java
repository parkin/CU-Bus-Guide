package com.teamparkin.mtdapp.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teamparkin.mtdapp.CustomViewPager;
import com.teamparkin.mtdapp.MyPagerAdapter;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.animations.ZoomOutPageTransformer;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener.OnRequestActionBarVisibleListener;
import com.viewpagerindicator.UnderlinePageIndicator;

/**
 * Wraps a ViewPager in a Fragment.
 * 
 * @author will
 * 
 */
public abstract class MyViewPagerFragment extends MyFragment {
	@SuppressWarnings("unused")
	private static final String TAG = MyViewPagerFragment.class.getSimpleName();

	protected ViewPager mViewPager;
	protected MyPagerAdapter mPagerAdapter;

	private OnRequestActionBarVisibleListener mRequestABVisibleListener;

	// ID's for child fragments.
	public static final int FAVORITES = 1000;
	public static final int DEPARTURES = 1001;
	public static final int MAP = 1002;
	public static final int NEARBY = 1003;
	public static final int ROUTES = 1004;
	public static final int TRIP_PLANNER = 1005;
	public static final int TRIP_VIEWER = 1006;
	public static final int ROUTES_LVL_1 = 1007;
	public static final int ROUTE_MAP = 1008;

	/**
	 * Creates the view. Initializes the ViewPager and the MyPagerAdapter.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		view = inflater.inflate(R.layout.my_viewpager_fragment, container,
				false);
		mViewPager = (CustomViewPager) view.findViewById(R.id.myViewPager);
		mPagerAdapter = new MyPagerAdapter(getChildFragmentManager());

		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);

		// add page indicator
		UnderlinePageIndicator indicator = (UnderlinePageIndicator) view
				.findViewById(R.id.myIndicator);
		indicator.setViewPager(mViewPager);

		indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				dispatchOnRequestActionBarVisible(shouldShowActionBar());
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		if (savedInstanceState != null) {
			ArrayList<Integer> ids = savedInstanceState
					.getIntegerArrayList("fragids");
			for (Integer id : ids) {
				Fragment frag = getChildFragmentManager().getFragment(
						savedInstanceState, "MVPF" + id);
				mPagerAdapter.add(frag, id);
			}
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save the FragIDs on a screen orientation change.
		outState.putIntegerArrayList("fragids", mPagerAdapter.getFragmentIds());
		int i = 0;
		for (Fragment fragment : mPagerAdapter.getFragments()) {
			getChildFragmentManager().putFragment(outState,
					"MVPF" + mPagerAdapter.getFragmentId(i), fragment);
			i++;
		}
	}

	public boolean shouldShowActionBar() {
		return true;
	}

	/**
	 * Returns getCurrentItem of the view pager.
	 * 
	 * @return
	 */
	public int getCurrentItem() {
		return mViewPager.getCurrentItem();
	}

	public Fragment getFragmentById(int id) {
		return mPagerAdapter.getItemByFragmentId(id);
	}

	/**
	 * If view pager is at first item, returns false. Otherwise, sets the view
	 * pager's current item to getCurrentItem() - 1 and returns true.
	 * 
	 * @return true if the back press was consumed. false otherwise.
	 */
	public boolean onBack() {
		if (mViewPager.getCurrentItem() > 0) {
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
			return true;
		}
		return false;
	}

	public void addFragment(Fragment fragment, int id) {
		mPagerAdapter.add(fragment, id);
	}

	@Override
	public void onBottomOverlayChange(float offset) {
		// pass this on to each page
		for (int i = 0; i < mPagerAdapter.getCount(); i++) {
			Fragment frag = mPagerAdapter.getItem(i);
			if (frag instanceof MyFragment) {
				((MyFragment) frag).onBottomOverlayChange(offset);
			}
		}
	}

	@Override
	public void onBottomOverlayShown(int offset) {
		// pass this on to each page
		for (int i = 0; i < mPagerAdapter.getCount(); i++) {
			Fragment frag = mPagerAdapter.getItem(i);
			if (frag instanceof MyFragment) {
				((MyFragment) frag).onBottomOverlayShown(offset);
			}
		}
	}

	@Override
	public void onBottomOverlayHidden() {
		// pass this on to each page
		for (int i = 0; i < mPagerAdapter.getCount(); i++) {
			Fragment frag = mPagerAdapter.getItem(i);
			if (frag instanceof MyFragment) {
				((MyFragment) frag).onBottomOverlayHidden();
			}
		}
	}

	public void setOnRequestActionBarVisibleListener(
			MyFragmentsListener.OnRequestActionBarVisibleListener listener) {
		mRequestABVisibleListener = listener;
	}

	public void dispatchOnRequestActionBarVisible(boolean visible) {
		if (mRequestABVisibleListener != null)
			mRequestABVisibleListener.onRequestActionBarVisible(visible);
		else
			Log.e(TAG, "dispatchOnRequestActionBarVisible listener null");
	}

}
