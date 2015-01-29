package com.teamparkin.mtdapp.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.teamparkin.mtdapp.MTDAppActivity;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.views.LocationItemView;

public abstract class MyLocationSlideupHeadFragment extends SlideupHeadFragment
        implements LoaderCallbacks<Cursor> {
    @SuppressWarnings("unused")
    private static final String TAG = MyLocationSlideupHeadFragment.class
            .getSimpleName();

    protected LocationItemView mLocationItemView;
    private OnNavigateSelectedListener mOnNavigateSelectedListener;

    private ImageButton mNavButton;

    private OnLocationSetListener mLocationSetListener;

    private static final int LOADER_HEAD_ID = 8431999;

    private Uri mLocationUri;

    public static MyLocationSlideupHeadFragment newInstance(Uri location) {
        MyLocationSlideupHeadFragment fragment = null;
        if (location.toString().contains(
                MyLocationContract.AbstractStops.CONTENT_URI.toString())) {
            fragment = new DeparturesFragment();
        } else {
            fragment = new PlaceBodyFragment();
        }
        return fragment;
    }

    public MyLocationSlideupHeadFragment() {
    }

    @Override
    public final View onCreateView(LayoutInflater inflater,
                                   ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        mLocationItemView = (LocationItemView) view
                .findViewById(R.id.location_slide_head);
        mLocationItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHeadClick(getLocation());
                dispatchOnDragViewClick(v);
            }
        });

        mNavButton = (ImageButton) view
                .findViewById(R.id.location_slide_navigate);
        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLocation() != null)
                    dispatchOnNavigateSelected(getLocation());
            }

        });

        View dragView = view.findViewById(R.id.location_head_dragview);

        setDragView(dragView);

        initializeBodyView(view, savedInstanceState);

        if (savedInstanceState != null) {
            Bundle bundle = new Bundle();
            mLocationUri = savedInstanceState.getParcelable("uri");
            bundle.putParcelable("uri", mLocationUri);
            getLoaderManager().initLoader(LOADER_HEAD_ID, bundle, this);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("uri", mLocationUri);
        super.onSaveInstanceState(outState);
    }

    protected abstract int getLayoutId();

    /**
     * This is called from MyLocationSlideupHeadFrag's onCreateView, with
     * mainView being the view that was inflated.
     *
     * @param mainView
     * @param savedInstanceState
     */
    protected abstract void initializeBodyView(View mainView,
                                               Bundle savedInstanceState);

    public abstract MyLocation getLocation();

    public Uri getLocationUri() {
        return mLocationUri;
    }

    protected void onHeadClick(MyLocation location) {
        final MTDAppActivity activity = (MTDAppActivity) getActivity();
        if (activity != null && activity.isPanelOpen())
            refillBody();
    }

    /**
     * Call when the fragments location has been set. This sets the properties
     * of the head LocationItemView and calls fillBody() if the slide up panel
     * isopen.
     *
     * @param location
     */
    public void setLocation(Uri location) {
        Log.i(TAG, "setLocation");
        if (location != null && !location.equals(mLocationUri)) {
            Log.i(TAG, "actually setLocation");
            Bundle bundle = new Bundle();
            bundle.putParcelable("uri", location);
            getLoaderManager().destroyLoader(LOADER_HEAD_ID);
            getLoaderManager().initLoader(LOADER_HEAD_ID, bundle, this);
            mLocationUri = location;
            this.isBodySet = false;
        }
    }

    protected void setLocationInfo(Cursor location) {
        if (mLocationItemView != null) {
            mLocationItemView.setLocationInfo(location);
        }
    }

    protected abstract void refillBody();

    /**
     * Returns true if the location is not of the correct type for the fragment;
     *
     * @param location
     * @return
     */
    public abstract boolean shouldReInitialize(Uri location);

    public void setOnNavigateSelectedListener(
            OnNavigateSelectedListener listener) {
        mOnNavigateSelectedListener = listener;
    }

    private void dispatchOnNavigateSelected(MyLocation location) {
        if (mOnNavigateSelectedListener != null)
            mOnNavigateSelectedListener.onNavigateSelected(location);
    }

    public interface OnNavigateSelectedListener {
        public void onNavigateSelected(MyLocation location);
    }

    public View getHeadView() {
        return mLocationItemView;
    }

    public void setOnLocationSetListener(OnLocationSetListener listener) {
        mLocationSetListener = listener;
    }

    protected void dispatchOnLocationSet(Uri location) {
        if (mLocationSetListener != null)
            mLocationSetListener.onLocationSet(location);
    }

    public interface OnLocationSetListener {
        public void onLocationSet(Uri location);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOADER_HEAD_ID:
                // This is called when a new Loader needs to be created. This
                // sample only has one Loader, so we don't care activity_about the ID.
                // First, pick the base URI to use depending on whether we are
                // currently filtering.
                Uri uri = bundle.getParcelable("uri");

                // Was having a null pointer issue here (hopefully here!) where uri was null.
                // https://play.google.com/apps/publish/?dev_acc=10397653172122434433#ErrorClusterDetailsPlace:c&p=com.teamparkin.mtdapp&s=new_status_desc&an&em&et=CRASH&tc=android.content.ContentResolver&tf=ContentResolver.java&tm=acquireProvider&ecn=java.lang.NullPointerException
                // Hopefully that is fixed, but if not, let's just set it to Stops and
                // Just return the first stop. So the following lines are just a stupid check.
                // This problem should be fixed by saving uri in onSaveInstanceState.
                /* Begin stupid check */
                String orderBy = null;
                if (uri == null){
                    uri = MyLocationContract.Stops.CONTENT_URI;
                    orderBy = MyLocationContract.Stops.DATA_ID + " LIMIT 1";
                }
                /* End stupid check */

                /*
			    * Note for some reason you can't query integers using
			    * selectionArgs...
			    */
                CursorLoader cursorLoader = new CursorLoader(getActivity(), uri,
                        null, null, null, orderBy);
                return cursorLoader;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_HEAD_ID:
                if (cursor.moveToFirst())
                    setLocationInfo(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_HEAD_ID:
                break;
        }
    }

}
