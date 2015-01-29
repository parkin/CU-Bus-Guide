package com.teamparkin.mtdapp.fragments;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

import com.teamparkin.mtdapp.listeners.OnRequestPanelInfoListener;

public abstract class MyFragment extends Fragment {
	private static final String TAG = MyFragment.class.getSimpleName();

	private View mViewForMarginChange;

	int mDefaultTopMargin = 0;
	int mDefaultLeftMargin = 0;
	int mDefaultRightMargin = 0;
	int mDefaultBottomMargin = 0;

	private OnRequestPanelInfoListener mRequestPanelInfoListener;

	public boolean shouldShowActionBar() {
		return true;
	}

	/**
	 * Call this when the back button is pressed. Returns true if back event
	 * consumed and activity should do nothing after.
	 * 
	 * @return
	 */
	public boolean onBack() {
		return false;
	}

	/**
	 * Sets the initial padding of mView to be it's current padding plus extra
	 * initial padding bottom if the sliding pane is visible.
	 * 
	 * @param view
	 */
	protected void setViewForMarginChange(View view) {
		mViewForMarginChange = view;
		if (mViewForMarginChange != null) {
			int extraInitialPadding = requestPanelHeight();
			ViewGroup.MarginLayoutParams params = (MarginLayoutParams) mViewForMarginChange
					.getLayoutParams();
			mDefaultTopMargin = params.topMargin;
			mDefaultBottomMargin = params.bottomMargin;
			mDefaultLeftMargin = params.leftMargin;
			mDefaultRightMargin = params.rightMargin;
			params.setMargins(mDefaultLeftMargin, mDefaultTopMargin,
					mDefaultRightMargin, mDefaultBottomMargin
							+ extraInitialPadding);
			mViewForMarginChange.setLayoutParams(params);
		}
	}

	/**
	 * Override this to handle when the bottom overlay over this fragment is
	 * animated and its offset is changing.
	 * 
	 * @param offset
	 */
	public void onBottomOverlayChange(float offset) {
	}

	public void onBottomOverlayShown(int offset) {
		if (mViewForMarginChange != null) {
			ViewGroup.MarginLayoutParams params = (MarginLayoutParams) mViewForMarginChange
					.getLayoutParams();
			params.setMargins(mDefaultLeftMargin, mDefaultTopMargin,
					mDefaultRightMargin, mDefaultBottomMargin + offset);
			mViewForMarginChange.setLayoutParams(params);
		}
	}

	public void onBottomOverlayHidden() {
		this.onBottomOverlayShown(0);
	}

	public boolean shouldExpandLocationPaneAfterShow() {
		return true;
	}

	public void setOnRequestPanelInfoListener(
			OnRequestPanelInfoListener listener) {
		mRequestPanelInfoListener = listener;
	}

	protected int requestPanelHeight() {
		if (mRequestPanelInfoListener != null)
			return mRequestPanelInfoListener.onRequestPanelHeight();
		Log.i(TAG, "requestPanelHeight listener null");
		return 0;
	}

	protected Uri requestPanelLocation() {
		if (mRequestPanelInfoListener != null)
			return mRequestPanelInfoListener.onRequestPanelLocation();
		else
			Log.e(TAG, "requestPanelLocation listener null");
		return null;
	}

	protected boolean requestPanelIsOpen() {
		if (mRequestPanelInfoListener != null)
			return mRequestPanelInfoListener.onRequestPanelIsOpen();
		Log.e(TAG, "requestPanelIsOpen listener null");
		return false;
	}

}
