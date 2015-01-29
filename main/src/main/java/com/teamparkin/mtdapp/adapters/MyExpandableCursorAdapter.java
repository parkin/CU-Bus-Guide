package com.teamparkin.mtdapp.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.listeners.OnExpandRequestedListener;
import com.teamparkin.mtdapp.views.TouchImageView;

public abstract class MyExpandableCursorAdapter extends CursorAdapter implements
		OnExpandRequestedListener {

	private int mViewLayoutResId;
	private int mTitleParentResId;
	private int mContentParentResId;
	private int mActionViewResId;
	private List<Long> mVisibleIds;

	private OnExpandRequestedListener mExpandListener;

	private int mLimit;
	private Map<Long, View> mExpandedViews;

	public MyExpandableCursorAdapter(Context context, Cursor c, int flags,
			int layoutResId, int titleParentResId, int contentParentResId) {
		super(context, c, flags);
		mViewLayoutResId = layoutResId;
		mTitleParentResId = titleParentResId;
		mContentParentResId = contentParentResId;

		mVisibleIds = new ArrayList<Long>();
		mExpandedViews = new HashMap<Long, View>();

		mExpandListener = this;
	}

	/**
	 * Set the resource id of the child {@link View} contained in the View
	 * returned by {@link #getTitleView(int, View, ViewGroup)} that will be the
	 * actuator of the expand / collapse animations.<br>
	 * If there is no View in the title View with given resId, a
	 * {@link NullPointerException} is thrown.</p> Default behavior: the whole
	 * title View acts as the actuator.
	 * 
	 * @param resId
	 *            the resource id.
	 */
	public void setActionViewResId(int resId) {
		mActionViewResId = resId;
	}

	public void setExpandListener(OnExpandRequestedListener listener) {
		mExpandListener = listener;
	}

	/**
	 * Set the maximum number of items allowed to be expanded. When the
	 * (limit+1)th item is expanded, the first expanded item will collapse.
	 * 
	 * @param limit
	 *            the maximum number of items allowed to be expanded. Use <= 0
	 *            for no limit.
	 */
	public void setLimit(int limit) {
		mLimit = limit;
		mVisibleIds.clear();
		mExpandedViews.clear();
		notifyDataSetChanged();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int position = cursor.getPosition();

		ViewHolder viewHolder = (ViewHolder) view.getTag();

		if (mLimit > 0) {
			if (mVisibleIds.contains(getItemId(position))) {
				mExpandedViews.put(getItemId(position), view);
			} else if (mExpandedViews.containsValue(view)
					&& !mVisibleIds.contains(getItemId(position))) {
				mExpandedViews.remove(getItemId(position));
			}
		}

		View titleView = getTitleView(cursor, viewHolder.titleView,
				viewHolder.titleParent);
		if (titleView != viewHolder.titleView) {
			viewHolder.titleParent.removeAllViews();
			viewHolder.titleParent.addView(titleView);

			if (mActionViewResId == 0) {
				view.setOnClickListener(new TitleViewOnClickListener(
						viewHolder.contentParent));
			} else {
				view.findViewById(mActionViewResId).setOnClickListener(
						new TitleViewOnClickListener(viewHolder.contentParent));
			}
		}
		viewHolder.titleView = titleView;

		View contentView = getContentView(cursor, viewHolder.contentView,
				viewHolder.contentParent);
		if (contentView != viewHolder.contentView) {
			viewHolder.contentParent.removeAllViews();
			viewHolder.contentParent.addView(contentView);
		}
		viewHolder.contentView = contentView;

		viewHolder.contentParent.setVisibility(mVisibleIds
				.contains(getItemId(position)) ? View.VISIBLE : View.GONE);
		viewHolder.contentParent.setTag(getItemId(position));

		ViewGroup.LayoutParams layoutParams = viewHolder.contentParent
				.getLayoutParams();
		layoutParams.height = LayoutParams.WRAP_CONTENT;
		viewHolder.contentParent.setLayoutParams(layoutParams);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(mViewLayoutResId,
				parent, false);

		ViewHolder viewHolder = new ViewHolder();
		viewHolder.titleParent = (ViewGroup) view
				.findViewById(mTitleParentResId);
		viewHolder.contentParent = (ViewGroup) view
				.findViewById(mContentParentResId);

		view.setTag(viewHolder);

		return view;
	}

	/**
	 * Get a View that displays the <b>title of the data</b> at the specified
	 * position in the data set. You can either create a View manually or
	 * inflate it from an XML layout file. When the View is inflated, the parent
	 * View (GridView, ListView...) will apply default layout parameters unless
	 * you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the title of the data at the specified
	 *         position.
	 */
	public abstract View getTitleView(Cursor cursor, View convertView,
			ViewGroup parent);

	/**
	 * Get a View that displays the <b>content of the data</b> at the specified
	 * position in the data set. You can either create a View manually or
	 * inflate it from an XML layout file. When the View is inflated, the parent
	 * View (GridView, ListView...) will apply default layout parameters unless
	 * you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the content of the data at the specified
	 *         position.
	 */
	public abstract View getContentView(Cursor cursor, View convertView,
			ViewGroup parent);

	private static class ViewHolder {
		ViewGroup titleParent;
		ViewGroup contentParent;
		View titleView;
		View contentView;
	}

	private class TitleViewOnClickListener implements View.OnClickListener {

		private View mContentParent;

		private TitleViewOnClickListener(View contentParent) {
			this.mContentParent = contentParent;
		}

		@Override
		public void onClick(View view) {
			boolean isVisible = mContentParent.getVisibility() == View.VISIBLE;
			if (!isVisible && mLimit > 0 && mVisibleIds.size() >= mLimit) {
				Long firstId = mVisibleIds.get(0);
				View firstEV = mExpandedViews.get(firstId);
				if (firstEV != null) {
					ViewHolder firstVH = ((ViewHolder) firstEV.getTag());
					ViewGroup contentParent = firstVH.contentParent;
					View container = mExpandListener
							.onCollapseRequested(contentParent);
					ExpandCollapseHelper.animateCollapsing(container,
							contentParent);
					mExpandedViews.remove(mVisibleIds.get(0));
				}
				mVisibleIds.remove(mVisibleIds.get(0));
			}

			if (isVisible) {
				View v = mContentParent
						.findViewById(R.id.mylocationlistitem_child_image);
				if (v != null && v instanceof TouchImageView) {
					TouchImageView tiv = (TouchImageView) v;
					if (tiv.getCurrentZoom() != 1.0) {
						tiv.setImageDrawable(tiv.getDrawable());
					}
				}
				View container = mExpandListener
						.onCollapseRequested(mContentParent);
				ExpandCollapseHelper.animateCollapsing(container,
						mContentParent);
				mVisibleIds.remove(mContentParent.getTag());
				mExpandedViews.remove(mContentParent.getTag());
			} else {
				View v = mExpandListener.onExpandRequested(mContentParent);
				if (v != null) {
					ExpandCollapseHelper.animateExpanding(v, mContentParent);
				} else {
					ExpandCollapseHelper.animateExpanding(mContentParent);
				}
				mVisibleIds.add((Long) mContentParent.getTag());

				if (mLimit > 0) {
					View parent = (View) mContentParent.getParent();
					mExpandedViews.put((Long) mContentParent.getTag(), parent);
				}
			}
		}
	}

	private static class ExpandCollapseHelper {

		public static void animateCollapsing(final View view) {
			int origHeight = view.getHeight();

			ValueAnimator animator = createHeightAnimator(view, origHeight, 0);
			animator.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animator) {
					view.setVisibility(View.GONE);
				}
			});
			animator.start();
		}

		public static void animateCollapsing(final View container,
				final View view) {
			int origHeight = view.getHeight();
			if (container != null) {

				int start = container.getHeight();
				int end = start - origHeight;

				ValueAnimator animator = createHeightAnimator(container, start,
						end);
				animator.addListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animator) {
						view.setVisibility(View.GONE);
					}
				});
				animator.start();
			} else {
				animateCollapsing(view);
			}
		}

		public static void animateExpanding(final View view) {
			view.setVisibility(View.VISIBLE);

			int widthSpec = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int heightSpec = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			view.measure(widthSpec, heightSpec);

			ValueAnimator animator = createHeightAnimator(view, 0,
					view.getMeasuredHeight());
			animator.start();
		}

		public static void animateExpanding(final View container,
				final View view) {
			view.setVisibility(View.VISIBLE);

			int widthSpec = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int heightSpec = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			view.measure(widthSpec, heightSpec);

			int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			container.measure(widthMeasureSpec, heightMeasureSpec);

			ValueAnimator animator = createHeightAnimator(container,
					container.getMeasuredHeight(), view.getMeasuredHeight()
							+ container.getMeasuredHeight());
			animator.start();
		}

		public static ValueAnimator createHeightAnimator(final View view,
				int start, int end) {
			ValueAnimator animator = ValueAnimator.ofInt(start, end);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					int value = (Integer) valueAnimator.getAnimatedValue();

					ViewGroup.LayoutParams layoutParams = view
							.getLayoutParams();
					layoutParams.height = value;
					view.setLayoutParams(layoutParams);
				}
			});
			return animator;
		}
	}

}
