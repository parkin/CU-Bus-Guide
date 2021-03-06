package com.teamparkin.mtdapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.sothree.slidinguppanel.library.R;

public class MySlidingUpPanel extends ViewGroup {

	private static final String TAG = MySlidingUpPanel.class.getSimpleName();

	/**
	 * Default peeking out panel height
	 */
	private static final int DEFAULT_PANEL_HEIGHT = 0; // dp;

	/**
	 * Default height of the shadow above the peeking out panel
	 */
	private static final int DEFAULT_SHADOW_HEIGHT = 4; // dp;

	/**
	 * If no fade color is given by default it will fade to 80% gray.
	 */
	private static final int DEFAULT_FADE_COLOR = 0x99000000;

	/**
	 * Default Minimum velocity that will be detected as a fling
	 */
	private static final int DEFAULT_MIN_FLING_VELOCITY = 400; // dips per
																// second

	/**
	 * Minimum velocity that will be detected as a fling
	 */
	private int mMinFlingVelocity = DEFAULT_MIN_FLING_VELOCITY;

	/**
	 * The fade color used for the panel covered by the slider. 0 = no fading.
	 */
	private int mCoveredFadeColor = DEFAULT_FADE_COLOR;

	/**
	 * The paint used to dim the main layout when sliding
	 */
	private final Paint mCoveredFadePaint = new Paint();

	/**
	 * Drawable used to draw the shadow between panes.
	 */
	private Drawable mShadowDrawable;

	/**
	 * The size of the overhang in pixels.
	 */
	private int mPanelHeight = -1;

	/**
	 * The size of the shadow in pixels.
	 */
	private int mShadowHeight = -1;

	/**
	 * True if a panel can slide with the current measurements
	 */
	private boolean mCanSlide;

	/**
	 * If provided, the panel can be dragged by only this view. Otherwise, the
	 * entire panel can be used for dragging.
	 */
	private View mDragView;

	/**
	 * If provided, the panel can be dragged by only this view. Otherwise, the
	 * entire panel can be used for dragging.
	 */
	private int mDragViewResId = -1;

	/**
	 * The child view that can slide, if any.
	 */
	private View mSlideableView;

	/**
	 * Current state of the slideable view.
	 */
	private enum SlideState {
		EXPANDED, COLLAPSED, ANCHORED
	}

	private SlideState mSlideState = SlideState.COLLAPSED;

	/**
	 * How far the panel is offset from its expanded position. range [0, 1]
	 * where 0 = expanded, 1 = collapsed.
	 */
	private float mSlideOffset;

	/**
	 * How far in pixels the slideable panel may move.
	 */
	private int mSlideRange;

	/**
	 * A panel view is locked into internal scrolling or another condition that
	 * is preventing a drag.
	 */
	private boolean mIsUnableToDrag;

	/**
	 * Flag indicating that sliding feature is enabled\disabled
	 */
	private boolean mIsSlidingEnabled;

	/**
	 * Flag indicating if a drag view can have its own touch events. If set to
	 * true, a drag view can scroll horizontally and have its own click
	 * listener.
	 * 
	 * Default is set to false.
	 */
	private boolean mIsUsingDragViewTouchEvents;

	/**
	 * Threshold to tell if there was a scroll touch event.
	 */
	private final int mScrollTouchSlop;

	/**
	 * Flag is Transparent. Default is true.
	 */
	private boolean mIsTransparent = true;

	/**
	 * If panel is transparent, set default panel color
	 */
	private static final int DEFAULT_PANEL_COLOR_TRANSPARENT = 0x00FFFFFF;

	private float mInitialMotionX;
	private float mInitialMotionY;
	private float mAnchorPoint = 0.f;

	private PanelSlideListener mPanelSlideListener;

	private PaneShowHideListener mPaneShowHideListener;

	private final ViewDragHelper mDragHelper;

	/**
	 * Stores whether or not the pane was expanded the last time it was
	 * slideable. If expand/collapse operations are invoked this state is
	 * modified. Used by instance state save/restore.
	 */
	private boolean mFirstLayout = true;

	private final Rect mTmpRect = new Rect();

	private ObjectAnimator mTranslationYAnimator;

	private boolean mIsHiding = false;

	/**
	 * Listener for monitoring events activity_about sliding panes.
	 */
	public interface PanelSlideListener {
		/**
		 * Called when a sliding pane's position changes.
		 * 
		 * @param panel
		 *            The child view that was moved
		 * @param slideOffset
		 *            The new offset of this sliding pane within its range, from
		 *            0-1
		 */
		public void onPanelSlide(View panel, float slideOffset);

		/**
		 * Called when a sliding pane becomes slid completely collapsed. The
		 * pane may or may not be interactive at this point depending on if it's
		 * shown or hidden
		 * 
		 * @param panel
		 *            The child view that was slid to an collapsed position,
		 *            revealing other panes
		 */
		public void onPanelCollapsed(View panel);

		/**
		 * Called when a sliding pane becomes slid completely expanded. The pane
		 * is now guaranteed to be interactive. It may now obscure other views
		 * in the layout.
		 * 
		 * @param panel
		 *            The child view that was slid to a expanded position
		 */
		public void onPanelExpanded(View panel);

		public void onPanelAnchored(View panel);
	}

	/**
	 * No-op stubs for {@link PanelSlideListener}. If you only want to implement
	 * a subset of the listener methods you can extend this instead of implement
	 * the full interface.
	 */
	public static class SimplePanelSlideListener implements PanelSlideListener {
		@Override
		public void onPanelSlide(View panel, float slideOffset) {
		}

		@Override
		public void onPanelCollapsed(View panel) {
		}

		@Override
		public void onPanelExpanded(View panel) {
		}

		@Override
		public void onPanelAnchored(View panel) {
		}
	}

	/**
	 * Listen for events activity_about showing/hiding the pane and animation during.
	 * 
	 * @author will
	 * 
	 */
	public interface PaneShowHideListener {

		/**
		 * Called when the pane is shown.
		 */
		public void onShowPane(int paneHeight);

		/**
		 * Called when the pane is hidden.
		 */
		public void onHidePane();

		/**
		 * Called during the animation of the showing/hiding of the pane.
		 * 
		 * @param offset
		 *            The distance from the bottom of the screen (in pixels) of
		 *            the top of the sliding panel.
		 */
		public void onPaneAnimated(float offset);

	}

	public MySlidingUpPanel(Context context) {
		this(context, null);
	}

	public MySlidingUpPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MySlidingUpPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs,
					R.styleable.SlidingUpPanelLayout);

			if (ta != null) {
				mPanelHeight = ta.getDimensionPixelSize(
						R.styleable.SlidingUpPanelLayout_collapsedHeight, -1);
				mShadowHeight = ta.getDimensionPixelSize(
						R.styleable.SlidingUpPanelLayout_shadowHeight, -1);

				mMinFlingVelocity = ta.getInt(
						R.styleable.SlidingUpPanelLayout_flingVelocity,
						DEFAULT_MIN_FLING_VELOCITY);
				mCoveredFadeColor = ta.getColor(
						R.styleable.SlidingUpPanelLayout_fadeColor,
						DEFAULT_FADE_COLOR);

				mDragViewResId = ta.getResourceId(
						R.styleable.SlidingUpPanelLayout_dragView, -1);
			}

			ta.recycle();
		}

		final float density = context.getResources().getDisplayMetrics().density;
		if (mPanelHeight == -1) {
			mPanelHeight = (int) (DEFAULT_PANEL_HEIGHT * density + 0.5f);
		}
		if (mShadowHeight == -1) {
			mShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);
		}

		setWillNotDraw(false);

		mDragHelper = ViewDragHelper.create(this, 0.5f,
				new DragHelperCallback());
		mDragHelper.setMinVelocity(mMinFlingVelocity * density);

		mCanSlide = true;
		mIsSlidingEnabled = true;

		setCoveredFadeColor(DEFAULT_FADE_COLOR);

		ViewConfiguration vc = ViewConfiguration.get(context);
		mScrollTouchSlop = vc.getScaledTouchSlop();
	}

	/**
	 * Set the Drag View after the view is inflated
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (mDragViewResId != -1) {
			mDragView = findViewById(mDragViewResId);
		}
	}

	/**
	 * Set the color used to fade the pane covered by the sliding pane out when
	 * the pane will become fully covered in the expanded state.
	 * 
	 * @param color
	 *            An ARGB-packed color value
	 */
	public void setCoveredFadeColor(int color) {
		mCoveredFadeColor = color;
		invalidate();
	}

	/**
	 * @return The ARGB-packed color value used to fade the fixed pane
	 */
	public int getCoveredFadeColor() {
		return mCoveredFadeColor;
	}

	public View getDragView() {
		return mDragView;
	}

	/**
	 * Set the collapsed panel height in pixels
	 * 
	 * @param val
	 *            A height in pixels
	 */
	public void setPanelHeight(int val) {
		mPanelHeight = val;
		requestLayout();
	}

	/**
	 * @return The current collapsed panel height
	 */
	public int getPanelHeight() {
		return mPanelHeight;
	}

	public void setPaneShowHideListener(PaneShowHideListener listener) {
		mPaneShowHideListener = listener;
	}

	void dispatchOnShowPane(int paneHeight) {
		if (mPaneShowHideListener != null)
			mPaneShowHideListener.onShowPane(paneHeight);
	}

	void dispatchOnHidePane() {
		if (mPaneShowHideListener != null)
			mPaneShowHideListener.onHidePane();
	}

	void dispatchOnPaneAnimated(float offset) {
		if (mPaneShowHideListener != null)
			mPaneShowHideListener.onPaneAnimated(offset);
	}

	/**
	 * Set the draggable view portion. Use to null, to allow the whole panel to
	 * be draggable
	 * 
	 * @param dragView
	 *            A view that will be used to drag the panel.
	 */
	public void setDragView(View dragView) {
		if (dragView == null)
			return;
		mDragView = dragView;

		// Measure the new DragView's height.
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(
				ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED);
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(
				ViewGroup.LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED);

		mDragView.measure(widthMeasureSpec, heightMeasureSpec);

		mPanelHeight = mDragView.getMeasuredHeight();
	}

	/**
	 * Set transparent flag
	 * 
	 */
	public void setIsTransparent(boolean mIsTransparent) {
		this.mIsTransparent = mIsTransparent;
	}

	/**
	 * Set an anchor point where the panel can stop during sliding
	 * 
	 * @param anchorPoint
	 *            A value between 0 and 1, determining the position of the
	 *            anchor point starting from the top of the layout.
	 */
	public void setAnchorPoint(float anchorPoint) {
		if (anchorPoint > 0 && anchorPoint < 1)
			mAnchorPoint = anchorPoint;
	}

	/**
	 * Set the shadow for the sliding panel
	 * 
	 */
	public void setShadowDrawable(Drawable drawable) {
		mShadowDrawable = drawable;
	}

	public void setPanelSlideListener(PanelSlideListener listener) {
		mPanelSlideListener = listener;
	}

	void dispatchOnPanelSlide(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelSlide(panel, mSlideOffset);
		}
	}

	void dispatchOnPanelExpanded(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelExpanded(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void dispatchOnPanelCollapsed(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelCollapsed(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void dispatchOnPanelAnchored(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelAnchored(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void updateObscuredViewVisibility() {
		if (getChildCount() == 0) {
			return;
		}
		final int leftBound = getPaddingLeft();
		final int rightBound = getWidth() - getPaddingRight();
		final int topBound = getPaddingTop();
		final int bottomBound = getHeight() - getPaddingBottom();
		final int left;
		final int right;
		final int top;
		final int bottom;
		if (mSlideableView != null && hasOpaqueBackground(mSlideableView)) {
			left = mSlideableView.getLeft();
			right = mSlideableView.getRight();
			top = mSlideableView.getTop();
			bottom = mSlideableView.getBottom();
		} else {
			left = right = top = bottom = 0;
		}
		View child = getChildAt(0);
		final int clampedChildLeft = Math.max(leftBound, child.getLeft());
		final int clampedChildTop = Math.max(topBound, child.getTop());
		final int clampedChildRight = Math.min(rightBound, child.getRight());
		final int clampedChildBottom = Math.min(bottomBound, child.getBottom());
		final int vis;
		if (clampedChildLeft >= left && clampedChildTop >= top
				&& clampedChildRight <= right && clampedChildBottom <= bottom) {
			vis = INVISIBLE;
		} else {
			vis = VISIBLE;
		}
		child.setVisibility(vis);
	}

	void setAllChildrenVisible() {
		for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == INVISIBLE) {
				child.setVisibility(VISIBLE);
			}
		}
	}

	private static boolean hasOpaqueBackground(View v) {
		final Drawable bg = v.getBackground();
		if (bg != null) {
			return bg.getOpacity() == PixelFormat.OPAQUE;
		}
		return false;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mFirstLayout = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mFirstLayout = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"Width must have an exact value or MATCH_PARENT");
		} else if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"Height must have an exact value or MATCH_PARENT");
		}

		int layoutHeight = heightSize - getPaddingTop() - getPaddingBottom();

		final int childCount = getChildCount();

		if (childCount > 2) {
			Log.e(TAG,
					"onMeasure: More than two child views are not supported.");
		}

		// We'll find the current one below.
		mSlideableView = null;
		mCanSlide = false;

		// First pass. Measure based on child LayoutParams width/height.
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int height = layoutHeight;
			if (child.getVisibility() == GONE) {
				lp.dimWhenOffset = false;
				continue;
			}

			if (i == 1) {
				lp.slideable = true;
				lp.dimWhenOffset = true;
				mSlideableView = child;
				mCanSlide = true;
			}
			// remove these lines so that the pane overlays the main content.
			// else {
			// height -= panelHeight;
			// }

			int childWidthSpec;
			int childHeightSpec;

			if (mIsTransparent && i == 0) {
				childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
						MeasureSpec.EXACTLY);
				childHeightSpec = MeasureSpec.makeMeasureSpec(heightSize,
						MeasureSpec.EXACTLY);
			} else {
				if (lp.width == LayoutParams.WRAP_CONTENT) {
					childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
							MeasureSpec.AT_MOST);
				} else if (lp.width == LayoutParams.MATCH_PARENT) {
					childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
							MeasureSpec.EXACTLY);
				} else {
					childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width,
							MeasureSpec.EXACTLY);
				}

				if (lp.height == LayoutParams.WRAP_CONTENT) {
					childHeightSpec = MeasureSpec.makeMeasureSpec(height,
							MeasureSpec.AT_MOST);
				} else if (lp.height == LayoutParams.MATCH_PARENT) {
					childHeightSpec = MeasureSpec.makeMeasureSpec(height,
							MeasureSpec.EXACTLY);
				} else {
					childHeightSpec = MeasureSpec.makeMeasureSpec(lp.height,
							MeasureSpec.EXACTLY);
				}
			}

			child.measure(childWidthSpec, childHeightSpec);
		}

		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int slidingTop = getSlidingTop();

		final int childCount = getChildCount();

		if (mFirstLayout) {
			switch (mSlideState) {
			case EXPANDED:
				mSlideOffset = mCanSlide ? 0.f : 1.f;
				break;
			case ANCHORED:
				mSlideOffset = mCanSlide ? mAnchorPoint : 1.f;
				break;
			default:
				mSlideOffset = 1.f;
				break;
			}
		}

		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);

			if (mIsTransparent && i == 1) {
				child.setBackgroundColor(DEFAULT_PANEL_COLOR_TRANSPARENT);
			}

			if (child.getVisibility() == GONE) {
				continue;
			}

			final LayoutParams lp = (LayoutParams) child.getLayoutParams();
			final int childHeight = child.getMeasuredHeight();

			if (lp.slideable) {
				mSlideRange = childHeight - mPanelHeight;
			}

			final int childTop = lp.slideable ? slidingTop
					+ (int) (mSlideRange * mSlideOffset) : paddingTop;
			final int childBottom = childTop + childHeight;
			final int childLeft = paddingLeft;
			final int childRight = childLeft + child.getMeasuredWidth();

			child.layout(childLeft, childTop, childRight, childBottom);
		}

		if (mFirstLayout) {
			updateObscuredViewVisibility();
		}

		mFirstLayout = false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Recalculate sliding panes and their details
		if (h != oldh) {
			mFirstLayout = true;
		}
	}

	/**
	 * Set sliding enabled flag
	 * 
	 * @param enabled
	 *            flag value
	 */
	public void setSlidingEnabled(boolean enabled) {
		mIsSlidingEnabled = enabled;
	}

	/**
	 * Set if the drag view can have its own touch events. If set to true, a
	 * drag view can scroll horizontally and have its own click listener.
	 * 
	 * Default is set to false.
	 */
	public void setEnableDragViewTouchEvents(boolean enabled) {
		mIsUsingDragViewTouchEvents = enabled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);

		if (!mCanSlide || !mIsSlidingEnabled
				|| (mIsUnableToDrag && action != MotionEvent.ACTION_DOWN)) {
			mDragHelper.cancel();
			return super.onInterceptTouchEvent(ev);
		}

		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			mDragHelper.cancel();
			return false;
		}

		final float x = ev.getX();
		final float y = ev.getY();
		boolean interceptTap = false;

		// Intercept touch events and do nothing if touching above an open panel
		if (isOpen() && y < mSlideableView.getTop()) {
			mDragHelper.cancel();
			return true;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mIsUnableToDrag = false;
			mInitialMotionX = x;
			mInitialMotionY = y;
			if (isDragViewUnder((int) x, (int) y)
					&& !mIsUsingDragViewTouchEvents) {
				interceptTap = true;
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final float adx = Math.abs(x - mInitialMotionX);
			final float ady = Math.abs(y - mInitialMotionY);
			final int dragSlop = mDragHelper.getTouchSlop();

			// Handle any horizontal scrolling on the drag view.
			if (mIsUsingDragViewTouchEvents) {
				if (adx > mScrollTouchSlop && ady < mScrollTouchSlop) {
					return super.onInterceptTouchEvent(ev);
				}
				// Intercept the touch if the drag view has any vertical scroll.
				// onTouchEvent will determine if the view should drag
				// vertically.
				else if (ady > mScrollTouchSlop) {
					interceptTap = isDragViewUnder((int) x, (int) y);
				}
			}

			if ((ady > dragSlop && adx > ady)
					|| !isDragViewUnder((int) x, (int) y)) {
				mDragHelper.cancel();
				mIsUnableToDrag = true;
				return false;
			}
			break;
		}
		}

		final boolean interceptForDrag = mDragHelper
				.shouldInterceptTouchEvent(ev);

		return interceptForDrag || interceptTap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!mCanSlide || !mIsSlidingEnabled) {
			return super.onTouchEvent(ev);
		}

		mDragHelper.processTouchEvent(ev);

		final int action = ev.getAction();
		boolean wantTouchEvents = true;

		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			mInitialMotionX = x;
			mInitialMotionY = y;
			break;
		}

		case MotionEvent.ACTION_UP: {
			final float x = ev.getX();
			final float y = ev.getY();
			final float dx = x - mInitialMotionX;
			final float dy = y - mInitialMotionY;
			final int slop = mDragHelper.getTouchSlop();
			View dragView = mDragView != null ? mDragView : mSlideableView;
			if (dx * dx + dy * dy < slop * slop
					&& isDragViewUnder((int) x, (int) y)) {
				dragView.playSoundEffect(SoundEffectConstants.CLICK);
				if (!isExpanded() && !isAnchored()) {
					expandPane(mAnchorPoint);
				} else {
					collapsePane();
				}
				break;
			}
			break;
		}
		}

		return wantTouchEvents;
	}

	private boolean isDragViewUnder(int x, int y) {
		View dragView = mDragView != null ? mDragView : mSlideableView;
		if (dragView == null)
			return false;
		int[] viewLocation = new int[2];
		dragView.getLocationOnScreen(viewLocation);
		int[] parentLocation = new int[2];
		this.getLocationOnScreen(parentLocation);
		int screenX = parentLocation[0] + x;
		int screenY = parentLocation[1] + y;
		return screenX >= viewLocation[0]
				&& screenX < viewLocation[0] + dragView.getWidth()
				&& screenY >= viewLocation[1]
				&& screenY < viewLocation[1] + dragView.getHeight();
	}

	private boolean expandPane(View pane, int initialVelocity,
			float mSlideOffset) {
		if (mFirstLayout || smoothSlideTo(mSlideOffset, initialVelocity)) {
			return true;
		}
		return false;
	}

	private boolean collapsePane(View pane, int initialVelocity) {
		if (mFirstLayout || smoothSlideTo(1.f, initialVelocity)) {
			return true;
		}
		return false;
	}

	private int getSlidingTop() {
		if (mSlideableView != null) {
			return getMeasuredHeight() - getPaddingBottom()
					- mSlideableView.getMeasuredHeight();
		}

		return getMeasuredHeight() - getPaddingBottom();
	}

	/**
	 * Collapse the sliding pane if it is currently slideable. If first layout
	 * has already completed this will animate.
	 * 
	 * @return true if the pane was slideable and is now collapsed/in the
	 *         process of collapsing
	 */
	public boolean collapsePane() {
		return collapsePane(mSlideableView, 0);
	}

	/**
	 * Expand the sliding pane if it is currently slideable. If first layout has
	 * already completed this will animate.
	 * 
	 * @return true if the pane was slideable and is now expanded/in the process
	 *         of expanding
	 */
	public boolean expandPane() {
		return expandPane(0);
	}

	/**
	 * Expand the sliding pane to the anchor position if the pane is currently
	 * slideable. If first layout has already completed this will animate.
	 * 
	 * @return true if the pane was slideable and is now expanded/in the process
	 *         of expanding
	 */
	public boolean expandPaneToAnchor() {
		return expandPane(mAnchorPoint);
	}

	/**
	 * Partially expand the sliding pane up to a specific offset
	 * 
	 * @param mSlideOffset
	 *            Value between 0 and 1, where 0 is completely expanded.
	 * @return true if the pane was slideable and is now expanded/in the process
	 *         of expanding
	 */
	public boolean expandPane(float mSlideOffset) {
		if (!isPaneVisible()) {
			showPane();
		}
		return expandPane(mSlideableView, 0, mSlideOffset);
	}

	/**
	 * Check if the layout is completely expanded.
	 * 
	 * @return true if sliding panels are completely expanded
	 */
	public boolean isExpanded() {
		return mSlideState == SlideState.EXPANDED;
	}

	/**
	 * Check if the layout is anchored in an intermediate point.
	 * 
	 * @return true if sliding panels are anchored
	 */
	public boolean isAnchored() {
		return mSlideState == SlideState.ANCHORED;
	}

	public boolean isOpen() {
		return isExpanded() || isAnchored();
	}

	/**
	 * Check if the content in this layout cannot fully fit side by side and
	 * therefore the content pane can be slid back and forth.
	 * 
	 * @return true if content in this layout can be expanded
	 */
	public boolean isSlideable() {
		return mCanSlide;
	}

	public boolean isPaneVisible() {
		if (getChildCount() < 2) {
			return false;
		}
		View slidingPane = getChildAt(1);
		return slidingPane.getVisibility() == View.VISIBLE;
	}

	public void showPane(final boolean expandAfter, final boolean expandFully) {
		if (getChildCount() < 2 || mDragView == null) {
			return;
		}
		// set the overlay pane height
		// if (mDragView != null) {
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(
				ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED);
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(
				ViewGroup.LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED);

		mDragView.setVisibility(View.VISIBLE);

		mDragView.measure(widthMeasureSpec, heightMeasureSpec);

		mPanelHeight = mDragView.getMeasuredHeight();
		// }

		mSlideableView = getChildAt(1);
		if (!isPaneVisible()) {
			final int dvHeight = mDragView.getMeasuredHeight();
			mTranslationYAnimator = ObjectAnimator.ofFloat(mSlideableView,
					"translationY", dvHeight, 0);
			mTranslationYAnimator
					.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							// Used to make sure draw is called so the position
							// of the shadow is updated.
							MySlidingUpPanel.this.invalidate();
							dispatchOnPaneAnimated(dvHeight
									- ((Float) animation.getAnimatedValue()));
						}
					});
			mTranslationYAnimator.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					dispatchOnShowPane(mPanelHeight);
					if (expandAfter) {
                        if (expandFully)
                            expandPane();
                        else
                            expandPaneToAnchor();
                    }
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			mTranslationYAnimator.setDuration(expandAfter ? 100 : 300).start();
		} else if (!isOpen() && expandAfter) {
            if (expandFully)
                expandPane();
            else
                expandPaneToAnchor();
		}
		mSlideableView.setVisibility(View.VISIBLE);

		requestLayout();
	}

	public void showPane() {
		showPane(false, true);
	}

	public void hidePane() {
		if (mSlideableView == null) {
			return;
		}
		if (mDragView != null && isPaneVisible() && !mIsHiding) {
			mIsHiding = true;
			dispatchOnHidePane();
			final int dvHeight = mDragView.getMeasuredHeight();
			mTranslationYAnimator = ObjectAnimator.ofFloat(mSlideableView,
					"translationY", 0, dvHeight);
			mTranslationYAnimator.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mIsHiding = false;
					mSlideableView.setVisibility(View.GONE);
					requestLayout();
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			mTranslationYAnimator
					.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							// Used to make sure draw is called so the position
							// of the shadow is updated.
							MySlidingUpPanel.this.invalidate();
							dispatchOnPaneAnimated(dvHeight
									- ((Float) animation.getAnimatedValue()));
						}
					});
			mTranslationYAnimator.setDuration(300).start();
		}
	}

	private void onPanelDragged(int newTop) {
		final int topBound = getSlidingTop();
		mSlideOffset = (float) (newTop - topBound) / mSlideRange;
		if (mIsTransparent && mSlideOffset < 0.4f && mSlideableView != null) {
			// Fade to opaque as the panel is completely expanded.
			int alpha = Color.alpha(DEFAULT_PANEL_COLOR_TRANSPARENT);
			// Do this so that if there is a fast scroll, we will still
			// hopefully end with the correct alpha
			int alphaToSet = mSlideOffset < 0.2f ? (int) (alpha + (255 - alpha)
					* (1 - mSlideOffset / 0.2f)) : alpha;
			mSlideableView.setBackgroundColor(Color.argb(alphaToSet,
					Color.red(DEFAULT_PANEL_COLOR_TRANSPARENT),
					Color.green(DEFAULT_PANEL_COLOR_TRANSPARENT),
					Color.blue(DEFAULT_PANEL_COLOR_TRANSPARENT)));
		}
		dispatchOnPanelSlide(mSlideableView);
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		final LayoutParams lp = (LayoutParams) child.getLayoutParams();
		boolean result;
		final int save = canvas.save(Canvas.CLIP_SAVE_FLAG);

		boolean drawScrim = false;

		if (mCanSlide && !lp.slideable && mSlideableView != null) {
			// Clip against the slider; no sense drawing what will immediately
			// be covered.
			canvas.getClipBounds(mTmpRect);
			mTmpRect.bottom = Math
					.min(mTmpRect.bottom, mSlideableView.getTop());
			if (!mIsTransparent)
				canvas.clipRect(mTmpRect);
			if (mSlideOffset < 1) {
				drawScrim = true;
			}
		}

		result = super.drawChild(canvas, child, drawingTime);
		canvas.restoreToCount(save);

		if (drawScrim) {
			final int baseAlpha = (mCoveredFadeColor & 0xff000000) >>> 24;
			final int imag = (int) (baseAlpha * (1 - mSlideOffset));
			final int color = imag << 24 | (mCoveredFadeColor & 0xffffff);
			mCoveredFadePaint.setColor(color);
			canvas.drawRect(mTmpRect, mCoveredFadePaint);
		}

		return result;
	}

	/**
	 * Smoothly animate mDraggingPane to the target X position within its range.
	 * 
	 * @param slideOffset
	 *            position to animate to
	 * @param velocity
	 *            initial velocity in case of fling, or 0.
	 */
	boolean smoothSlideTo(float slideOffset, int velocity) {
		if (!mCanSlide) {
			// Nothing to do.
			return false;
		}

		final int topBound = getSlidingTop();
		int y = (int) (topBound + slideOffset * mSlideRange);

		if (mDragHelper.smoothSlideViewTo(mSlideableView,
				mSlideableView.getLeft(), y)) {
			// set the state here. don't want to wait until the animation is
			// done.
			if (slideOffset == mAnchorPoint) {
				mSlideState = SlideState.ANCHORED;
				dispatchOnPanelAnchored(mSlideableView);
			} else if (slideOffset == 0) {
				mSlideState = SlideState.EXPANDED;
				dispatchOnPanelExpanded(mSlideableView);
			} else if (slideOffset == 1.f) {
				mSlideState = SlideState.COLLAPSED;
				dispatchOnPanelCollapsed(mSlideableView);
			}
			setAllChildrenVisible();
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			if (!mCanSlide) {
				mDragHelper.abort();
				return;
			}

			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public void draw(Canvas c) {
		super.draw(c);

		if (mSlideableView == null) {
			// No need to draw a shadow if we don't have one.
			return;
		}
		float translationY = (Float) ((mTranslationYAnimator != null && mTranslationYAnimator
				.isRunning()) ? mTranslationYAnimator.getAnimatedValue() : 0.0f);

		final int right = mSlideableView.getRight();
		final int top = (int) (mSlideableView.getTop() + translationY - mShadowHeight);
		final int bottom = (int) (mSlideableView.getTop() + translationY);
		final int left = mSlideableView.getLeft();

		if (mShadowDrawable != null) {
			mShadowDrawable.setBounds(left, top, right, bottom);
			mShadowDrawable.draw(c);
		}
	}

	/**
	 * Tests scrollability within child views of v given a delta of dx.
	 * 
	 * @param v
	 *            View to test for horizontal scrollability
	 * @param checkV
	 *            Whether the view v passed should itself be checked for
	 *            scrollability (true), or just its children (false).
	 * @param dx
	 *            Delta scrolled in pixels
	 * @param x
	 *            X coordinate of the active touch point
	 * @param y
	 *            Y coordinate of the active touch point
	 * @return true if child views of v can be scrolled by delta of dx.
	 */
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof ViewGroup) {
			final ViewGroup group = (ViewGroup) v;
			final int scrollX = v.getScrollX();
			final int scrollY = v.getScrollY();
			final int count = group.getChildCount();
			// Count backwards - let topmost views consume scroll distance
			// first.
			for (int i = count - 1; i >= 0; i--) {
				final View child = group.getChildAt(i);
				if (x + scrollX >= child.getLeft()
						&& x + scrollX < child.getRight()
						&& y + scrollY >= child.getTop()
						&& y + scrollY < child.getBottom()
						&& canScroll(child, true, dx,
								x + scrollX - child.getLeft(), y + scrollY
										- child.getTop())) {
					return true;
				}
			}
		}
		return checkV && ViewCompat.canScrollHorizontally(v, -dx);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams();
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p) {
		return p instanceof MarginLayoutParams ? new LayoutParams(
				(MarginLayoutParams) p) : new LayoutParams(p);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams && super.checkLayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		ss.mSlideState = mSlideState;

		return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		mSlideState = ss.mSlideState;
	}

	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			if (mIsUnableToDrag) {
				return false;
			}

			return ((LayoutParams) child.getLayoutParams()).slideable;
		}

		@Override
		public void onViewDragStateChanged(int state) {
			int anchoredTop = (int) (mAnchorPoint * mSlideRange);

			if (mDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {
				if (mSlideOffset == 0) {
					if (mSlideState != SlideState.EXPANDED) {
						updateObscuredViewVisibility();
						dispatchOnPanelExpanded(mSlideableView);
						mSlideState = SlideState.EXPANDED;
					}
				} else if (mSlideOffset == (float) anchoredTop
						/ (float) mSlideRange) {
					if (mSlideState != SlideState.ANCHORED) {
						updateObscuredViewVisibility();
						dispatchOnPanelAnchored(mSlideableView);
						mSlideState = SlideState.ANCHORED;
					}
				} else if (mSlideState != SlideState.COLLAPSED) {
					dispatchOnPanelCollapsed(mSlideableView);
					mSlideState = SlideState.COLLAPSED;
				}
			}
		}

		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			// Make all child views visible in preparation for sliding things
			// around
			setAllChildrenVisible();
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			onPanelDragged(top);
			invalidate();
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			int top = getSlidingTop();

			if (mAnchorPoint != 0) {
				int anchoredTop = (int) (mAnchorPoint * mSlideRange);
				float anchorOffset = (float) anchoredTop / (float) mSlideRange;

				if (yvel > 0
						|| (yvel == 0 && mSlideOffset >= (1f + anchorOffset) / 2)) {
					top += mSlideRange;
				} else if (yvel == 0 && mSlideOffset < (1f + anchorOffset) / 2
						&& mSlideOffset >= anchorOffset / 2) {
					top += mSlideRange * mAnchorPoint;
				}

			} else if (yvel > 0 || (yvel == 0 && mSlideOffset > 0.5f)) {
				top += mSlideRange;
			}

			mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
			invalidate();
		}

		@Override
		public int getViewVerticalDragRange(View child) {
			return mSlideRange;
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			final int topBound = getSlidingTop();
			final int bottomBound = topBound + mSlideRange;

			return Math.min(Math.max(top, topBound), bottomBound);
		}
	}

	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		private static final int[] ATTRS = new int[] { android.R.attr.layout_weight };

		/**
		 * True if this pane is the slideable pane in the layout.
		 */
		boolean slideable;

		/**
		 * True if this view should be drawn dimmed when it's been offset from
		 * its default position.
		 */
		boolean dimWhenOffset;

		Paint dimPaint;

		public LayoutParams() {
			super(MATCH_PARENT, MATCH_PARENT);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(android.view.ViewGroup.LayoutParams source) {
			super(source);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		public LayoutParams(LayoutParams source) {
			super(source);
		}

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

			final TypedArray a = c.obtainStyledAttributes(attrs, ATTRS);
			a.recycle();
		}

	}

	static class SavedState extends BaseSavedState {
		SlideState mSlideState;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			try {
				mSlideState = Enum.valueOf(SlideState.class, in.readString());
			} catch (IllegalArgumentException e) {
				mSlideState = SlideState.COLLAPSED;
			}
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeString(mSlideState.toString());
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
