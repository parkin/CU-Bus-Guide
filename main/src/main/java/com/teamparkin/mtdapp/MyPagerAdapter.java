package com.teamparkin.mtdapp;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

public class MyPagerAdapter extends FragmentStatePagerAdapter {
	private static final String TAG = MyPagerAdapter.class.getSimpleName();

	private ArrayList<Fragment> mFragments;
	private ArrayList<Integer> mFragmentIds;

	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<Fragment>();
		mFragmentIds = new ArrayList<Integer>();
	}

	@Override
	public Fragment getItem(int position) {
		if (position < 0 || position >= mFragments.size())
			return null;
		return mFragments.get(position);
	}

	public ArrayList<Integer> getFragmentIds() {
		return mFragmentIds;
	}

	public Fragment getItemByFragmentId(int id) {
		if (mFragmentIds.contains(id))
			return mFragments.get(mFragmentIds.indexOf(id));
		return null;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container,
				position);
		Log.i(TAG, "instantiateItem: " + fragment.getClass().getSimpleName());
		mFragments.set(position, fragment);
		return fragment;
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	/**
	 * Returns the position of the fragment with fragId in the ViewPager, -1
	 * otherwise.
	 * 
	 * @param fragId
	 * @return
	 */
	public int getIdPosition(int fragId) {
		int count = 0;
		for (Integer integer : mFragmentIds) {
			if (integer.equals(fragId))
				return count;
			count++;
		}
		return -1;
	}

	// /**
	// * Override the adapter method getItemPosition (shown below). When we
	// * call mAdapter.notifyDataSetChanged(); the ViewPager interrogates the
	// * adapter to determine what has changed in terms of positioning. We use
	// * this method to say that everything has changed so reprocess all your
	// * view positioning.
	// * http://stackoverflow.com/questions/10396321/remove-fragment
	// * -page-from-viewpager-in-android
	// */
	// @Override
	// public int getItemPosition(Object object) {
	// return PagerAdapter.POSITION_NONE;
	// }

	/**
	 * Returns the fragment id of the fragment at position.
	 * 
	 * @param position
	 * @return
	 */
	public int getFragmentId(int position) {
		return mFragmentIds.get(position);
	}

	/**
	 * Adds the fragment to the adapter. Must call notifyDatasetChanged when you
	 * want the changes to be shown.
	 * 
	 * @param fragment
	 */
	public void add(Fragment fragment, int id) {
		mFragments.add(fragment);
		mFragmentIds.add(id);
		this.notifyDataSetChanged();
	}

	/**
	 * Removes all of the fragments. You must call notifyDataSetChanged when you
	 * want the changes to take effect.
	 */
	public void removeAllFragments() {
		mFragments.clear();
		mFragmentIds.clear();
		this.notifyDataSetChanged();
	}

	/**
	 * Returns true if the fragment id is already in the viewpager.
	 * 
	 * @param id
	 * @return
	 */
	public boolean containsId(int id) {
		return mFragmentIds.contains(id);
	}

	public ArrayList<Fragment> getFragments() {
		return mFragments;
	}

}