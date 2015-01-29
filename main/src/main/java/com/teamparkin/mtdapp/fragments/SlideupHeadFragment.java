package com.teamparkin.mtdapp.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.listeners.OnRequestPanelInfoListener;

public abstract class SlideupHeadFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = SlideupHeadFragment.class.getSimpleName();

	private View mDragView;
	private OnDragViewClickListener mDragViewClickListener = null;

	protected boolean isBodySet = false;

	private OnDragViewSetListener mDragViewSetListener;

	private OnRequestPanelInfoListener mRequestPanelInfoListener;

	public SlideupHeadFragment() {
	}

	public View getDragView() {
		return mDragView;
	}

	protected void setDragView(View view) {
		mDragView = view;
		dispatchOnDragViewSet(view);
	}

	private void dispatchOnDragViewSet(View view) {
		if (mDragViewSetListener != null)
			mDragViewSetListener.onDragViewSet(view);
	}

	protected boolean shouldAddTransactionToBackStack() {
		return false;
	}

	public abstract View getHeadView();

	/**
	 * Fill the body fragment if not already done.
	 */
	public abstract void fillBody();

	public void setDragViewOnClickListener(OnDragViewClickListener listener) {
		mDragViewClickListener = listener;
	}

	public void removeDragViewOnClickListener() {
		mDragViewClickListener = null;
	}

	protected void dispatchOnDragViewClick(View v) {
		if (mDragViewClickListener != null)
			mDragViewClickListener.onClick(v);
	}

	public interface OnDragViewClickListener {
		public void onClick(View v);
	}

	public void setOnDragViewSetListener(OnDragViewSetListener listener) {
		mDragViewSetListener = listener;
	}

	public interface OnDragViewSetListener {
		public void onDragViewSet(View dragView);
	}

	/**
	 * Returns true if the back was consumed.
	 * 
	 * @return
	 */
	public boolean handleOnBackPressed() {
		return false;
	}

	public void setOnRequestPanelInfoListener(
			OnRequestPanelInfoListener listener) {
		mRequestPanelInfoListener = listener;
	}

	protected boolean requestPanelIsOpen() {
		if (mRequestPanelInfoListener != null)
			return mRequestPanelInfoListener.onRequestPanelIsOpen();
		Log.e(TAG, "requestPanelIsOpen listener null");
		return false;
	}
	
}
