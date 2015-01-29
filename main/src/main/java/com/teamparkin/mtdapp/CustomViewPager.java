package com.teamparkin.mtdapp;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * Use this Custom ViewPager so disable scrolling gestures on maps
 * 
 * @author will
 * 
 */
public class CustomViewPager extends ViewPager {
	@SuppressWarnings("unused")
	private static final String TAG = CustomViewPager.class.getSimpleName();

	long ninetyPercent;

	private static boolean shouldOverrideScrolling() {
		// On my Triumph (2.3), you could NOT scroll left/right on MapV2Fragment
		// with just a regular ViewPager. On my Nexus 5 (4.4) you CAN!
		return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB;
	}
	
	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeNinetyPercent();
	}

	public CustomViewPager(Context context) {
		super(context);
		initializeNinetyPercent();
	}

	private void initializeNinetyPercent() {
		// Get the screen width
		// get the width
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		int screenWidth = metrics.widthPixels;
		ninetyPercent = Math.round((1.0 * screenWidth) * 0.9);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if ((MTDAppActivity.currentFrag == MTDAppActivity.FRAG_MAP || MTDAppActivity.currentFrag == MTDAppActivity.FRAG_ROUTES)
				&& (x <= ninetyPercent) && shouldOverrideScrolling()) {
			return true;
		}
		return super.canScroll(v, checkV, dx, x, y);
	}
}