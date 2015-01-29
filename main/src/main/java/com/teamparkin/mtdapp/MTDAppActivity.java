package com.teamparkin.mtdapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.teamparkin.mtdapp.contentproviders.StopsProvider;
import com.teamparkin.mtdapp.databases.DatabaseAdapter;
import com.teamparkin.mtdapp.databases.MyLocationDatabaseHelper;
import com.teamparkin.mtdapp.dataclasses.Departure;
import com.teamparkin.mtdapp.dataclasses.Itinerary;
import com.teamparkin.mtdapp.dataclasses.MyLocation;
import com.teamparkin.mtdapp.dataclasses.TripPlanParameters;
import com.teamparkin.mtdapp.fragments.DeparturesFragment;
import com.teamparkin.mtdapp.fragments.FavoritesFragment;
import com.teamparkin.mtdapp.fragments.ItineraryBodyFrag;
import com.teamparkin.mtdapp.fragments.MapV2ContainerFragment;
import com.teamparkin.mtdapp.fragments.MapV2Fragment;
import com.teamparkin.mtdapp.fragments.MyFragment;
import com.teamparkin.mtdapp.fragments.MyLocationSlideupHeadFragment;
import com.teamparkin.mtdapp.fragments.NearbyFragment;
import com.teamparkin.mtdapp.fragments.OnRequestItineraryInfoListener;
import com.teamparkin.mtdapp.fragments.SlideupHeadFragment;
import com.teamparkin.mtdapp.fragments.TripPlanViewerFragment;
import com.teamparkin.mtdapp.fragments.VPRoutesFrag;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.listeners.OnOriginDestinationChangedListener;
import com.teamparkin.mtdapp.listeners.OnRequestFragmentVisibilityListener;
import com.teamparkin.mtdapp.listeners.OnRequestPanelInfoListener;
import com.teamparkin.mtdapp.listeners.OnRequestTripPlanInfoListener;
import com.teamparkin.mtdapp.restadapters.MTDAPIAdapter;
import com.teamparkin.mtdapp.views.MySlidingUpPanel;

import java.util.Calendar;

public class MTDAppActivity extends ActionBarActivity implements
        MyFragmentsListener, LoaderCallbacks<Cursor> {
    public static final int FRAG_SEARCH = 0;
    public static final int FRAG_FAVORITES = 1;
    public static final int FRAG_MAP = 2;
    public static final int FRAG_NEARBY = 3;
    public static final int FRAG_ROUTES = 5;
    private static final String TAG = MTDAppActivity.class.getSimpleName();
    private static final int GOOGLE_PLAY_SERVICES_ERROR_RESULT = 913;
    private static final int LOADER_SEARCH_ID = 9847;
    private static final String SAVED_STATE_ACTION_BAR_HIDDEN = "saved_state_action_bar_hidden";
    private static final String SAVED_STATE_PANE_VISIBLE = "saved_state_pane_visible";
    public static int currentFrag;

    private static Context mContext;

    private static Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            Object path = message.obj;
            if (message.arg1 == RESULT_OK && path != null) {
                Toast.makeText(mContext, "Download " + path.toString(),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "Download failed :(",
                        Toast.LENGTH_LONG).show();
            }

        }

    };

    /**
     * This is set by the action bar's onQueryTextChanged as the current text
     * filter of the action bar's searchView.
     */
    private String mCurSearchViewFilter = "";

    private MTDAPIAdapter mMtdApiAdapter;

    private MyFragment mCurrFrag;
    private SlideupHeadFragment mSlideupFrag;

    private SearchView mSearchView;

    private DrawerLayout mDrawerLayout;
    private MyDrawerListAdapter mDrawerListAdapter;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private MenuItem mSearchItem;

    private MySlidingUpPanel mSlideUpPanel;

    private boolean mShouldShowPaneAfterDragViewSet = false;

    private SlideupHeadFragment.OnDragViewSetListener mSlideUpHeadDragViewSetListener = new SlideupHeadFragment.OnDragViewSetListener() {

        @Override
        public void onDragViewSet(View dragView) {
            mSlideUpPanel.setDragView(dragView);
            if (mShouldShowPaneAfterDragViewSet) {
                mShouldShowPaneAfterDragViewSet = false;
                mSlideUpPanel.showPane();
            }
        }
    };

    private OnRequestPanelInfoListener mRequestPanelInfoListener;

    private CursorAdapter mSearchSuggestAadapter;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if (Build.VERSION.SDK_INT >= 19) {
        // getWindow()
        // .addFlags(
        // android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        // }

        // Make progress bar visible
        this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
                Window.PROGRESS_VISIBILITY_ON);

        setContentView(R.layout.main);

        // Get instances of the database so that they will check if they
        // are initialized.
        MyLocationDatabaseHelper.getInstance(this);

        mContext = getApplicationContext();
        mMtdApiAdapter = MTDAPIAdapter.getInstance(getApplicationContext());

        initializeNavigationDrawer();

        // initializeC2DM();
        // unregisterC2DM();

        setUpSlideUpPanel();

        getSupportActionBar().setTitle(mTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            // Initialize nav drawer/ initial fragment to favorites
            selectNavDrawerItemPosition(0);
        } else {
            mCurrFrag = (MyFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            mSlideupFrag = (SlideupHeadFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.slideup_container);
            boolean actionBarHidden = savedInstanceState.getBoolean(
                    SAVED_STATE_ACTION_BAR_HIDDEN, false);
            if (actionBarHidden)
                setActionBarVisible(false);
            else
                setActionBarVisible(true);

            setupMyFragmentListeners(mCurrFrag);

            if (mSlideupFrag != null) {
                // TODO change to initialize listeners
                setupSlideupHeadListeners(mSlideupFrag);
                if (savedInstanceState.getBoolean(SAVED_STATE_PANE_VISIBLE))
                    mShouldShowPaneAfterDragViewSet = true;
            }
        }

        // Increment the donate button show counter.
        final SharedPreferences settings = getSharedPreferences("appMetadata",
                0);
        int numTimesOnCreate = settings.getInt(
                "numberTimesOnCreateSinceDonateClicked", 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("numberTimesOnCreateSinceDonateClicked",
                numTimesOnCreate + 1);
        editor.commit();

        // Set up the default values for the settings.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }

    private void setUpSlideUpPanel() {
        mSlideUpPanel = (MySlidingUpPanel) findViewById(R.id.sliding_layout);
        mSlideUpPanel.setShadowDrawable(getResources().getDrawable(
                R.drawable.above_shadow));
        mSlideUpPanel.setAnchorPoint(0.3f);
        // mSlideUpPanel.setDragView(findViewById(R.id.slideup_container));
        // hide pane initially so other initializations go correctly.
        // mSlideUpPanel.hidePane();
        mSlideUpPanel.setEnableDragViewTouchEvents(true);
        mSlideUpPanel
                .setPanelSlideListener(new MySlidingUpPanel.PanelSlideListener() {
                    @Override
                    public void onPanelSlide(View panel, float slideOffset) {
                        if (slideOffset < 0.2) {
                            if (getSupportActionBar().isShowing()) {
                                setActionBarVisible(false);
                            }
                        } else {
                            if (!getSupportActionBar().isShowing()) {
                                setActionBarVisible(mCurrFrag
                                        .shouldShowActionBar());
                            }
                        }

                        if (slideOffset < 0.8) {
                            Fragment fragHead = getSupportFragmentManager()
                                    .findFragmentById(R.id.slideup_container);
                            if (fragHead instanceof SlideupHeadFragment) {
                                ((SlideupHeadFragment) fragHead).fillBody();
                            }
                        }
                    }

                    @Override
                    public void onPanelExpanded(View panel) {
                        mDrawerLayout
                                .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    }

                    @Override
                    public void onPanelCollapsed(View panel) {
                        mDrawerLayout
                                .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }

                    @Override
                    public void onPanelAnchored(View panel) {
                        mDrawerLayout
                                .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    }
                });
        mSlideUpPanel
                .setPaneShowHideListener(new MySlidingUpPanel.PaneShowHideListener() {
                    @Override
                    public void onShowPane(int paneHeight) {
                        if (mCurrFrag != null)
                            mCurrFrag.onBottomOverlayShown(paneHeight);
                    }

                    @Override
                    public void onPaneAnimated(float offset) {
                        if (mCurrFrag != null)
                            mCurrFrag.onBottomOverlayChange(offset);
                    }

                    @Override
                    public void onHidePane() {
                        Log.i(TAG, "onHidePane");
                        if (mCurrFrag != null)
                            mCurrFrag.onBottomOverlayHidden();
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_STATE_ACTION_BAR_HIDDEN,
                !getSupportActionBar().isShowing());
        outState.putBoolean(SAVED_STATE_PANE_VISIBLE,
                mSlideUpPanel.isPaneVisible());
    }

    private void initializeNavigationDrawer() {
        // configure the Navigation Drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerListAdapter = new MyDrawerListAdapter(this);
        mDrawerList.setAdapter(mDrawerListAdapter);
        // adapter.add(new SampleItem("Search", R.drawable.search));
        mDrawerListAdapter.add(new MyNavDrawerItem(FRAG_FAVORITES, "Favorites",
                R.drawable.ic_favorites_star));
        mDrawerListAdapter.add(new MyNavDrawerItem(FRAG_MAP, "Map",
                R.drawable.ic_map));
        mDrawerListAdapter.add(new MyNavDrawerItem(FRAG_NEARBY, "Nearby",
                R.drawable.ic_nearby));
        mDrawerListAdapter.add(new MyNavDrawerItem(FRAG_ROUTES, "Routes",
                R.drawable.ic_routes));
        // Set onClick listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = mDrawerTitle = getTitle();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
                setActionBarVisible(shouldShowActionBar());
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
                setActionBarVisible(true);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // show the action bar when the drawer is slightly open
                if (slideOffset < 0.1)
                    setActionBarVisible(shouldShowActionBar());
                else
                    setActionBarVisible(true);
                super.onDrawerSlide(drawerView, slideOffset);
            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
        }
        // If ACTION_VIEW, want to open up the location.
        else if (StopsProvider.ACTION_CLICK.equals(intent.getAction())) {
            Uri data = intent.getData();
            selectLocation(data);
            mSearchView.clearFocus();
            collapseSearchActionView();
        } else if (intent.hasExtra("widget_bundle")) {
            Bundle bundle = intent.getBundleExtra("widget_bundle");
            Uri stop = bundle.getParcelable("uri");

            selectLocation(stop);

            if (mCurrFrag instanceof MapV2ContainerFragment) {
                ((MapV2ContainerFragment) mCurrFrag).setSelectedLocation(stop);
            }
        }
    }

    private boolean shouldShowActionBar() {
        return !mSlideUpPanel.isOpen() && mCurrFrag.shouldShowActionBar();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void collapseSearchActionView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mSearchItem.collapseActionView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GOOGLE_PLAY_SERVICES_ERROR_RESULT) {
            // if we are getting a request code, check that the error has been
            // resolved.
            checkGooglePlayServices();
        }
    }

    /**
     * Selects the item at position in the Navigation Drawer. Then updates the
     * current fragment accordingly.
     *
     * @param position - The position to select in the Navigation Drawer.
     * @return true if a change was made.
     */
    private boolean selectNavDrawerItemPosition(int position) {
        boolean contentSwitched = switchContent(mDrawerListAdapter.getItem(position).getFragId());
        // Highlight the selected item, update the title, and close the
        // drawer
        mDrawerList.setItemChecked(position, true);
        mTitle = getCurrFragmentTitle();
        getSupportActionBar().setTitle(mTitle);
        mDrawerLayout.closeDrawer(mDrawerList);
        return contentSwitched;
    }

    /**
     * Selects the item with fragId in the Navigation Drawer. Then updates the
     * current fragment accordingly.
     *
     * @param fragId - The id of the fragment to select.
     */
    private void selectNavDrawerItemFragId(int fragId) {
        selectNavDrawerItemPosition(mDrawerListAdapter
                .getPositionByFragId(fragId));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        // Note: ensures correct drawer icon
        mDrawerToggle.syncState();
    }

    @SuppressLint({"InlinedApi", "NewApi"})
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        mSearchItem = menu.findItem(R.id.menu_search);

        // Get the SearchView and set the searchable configuration
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));

        // Create an empty adapter we will use to display the loaded data.
        mSearchSuggestAadapter = new MySuggestionCursorAdapter(this, null, 0);

        // mSearchSuggestAadapter = new MySuggestionsAdapter(this, mSearchView,
        // searchManager.getSearchableInfo(getComponentName()),
        // new WeakHashMap<String, Drawable.ConstantState>());

        mSearchView.setSuggestionsAdapter(mSearchSuggestAadapter);
        mSearchView
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String arg0) {
                        // we don't care activity_about this
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // Called when the action bar search text has changed.
                        // Update
                        // the search filter, and restart the loader to do a new
                        // query
                        // with this filter.
                        String newFilter = !TextUtils.isEmpty(newText) ? newText
                                : null;
                        // Don't do anything if the filter hasn't actually
                        // changed.
                        // Prevents restarting the loader when restoring state.
                        if (mCurSearchViewFilter == null && newFilter == null) {
                            return true;
                        }
                        if (mCurSearchViewFilter != null
                                && mCurSearchViewFilter.equals(newFilter)) {
                            return true;
                        }
                        mCurSearchViewFilter = newFilter;
                        getSupportLoaderManager().restartLoader(
                                LOADER_SEARCH_ID, null, MTDAppActivity.this);
                        // restarting the loader will call onLoaderFinished and
                        // will swap the cursor, so return true.
                        return true;
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final SharedPreferences settings = getSharedPreferences(
                    "appMetadata", 0);
            int numTimesOnCreate = settings.getInt(
                    "numberTimesOnCreateSinceDonateClicked", -1);

            if (numTimesOnCreate > 15) {
                MenuItem donateItem = menu.findItem(R.id.menu_donate);
                donateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }

        getSupportLoaderManager().initLoader(LOADER_SEARCH_ID, null, this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (!mSlideUpPanel.isOpen()) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }

        // Handle your other action bar items...
        switch (item.getItemId()) {
            case R.id.menu_donate:
                startDonateActivity();

                // reset the donate counter, since the user has clicked it!
                final SharedPreferences settings = getSharedPreferences(
                        "appMetadata", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("numberTimesOnCreateSinceDonateClicked", 0);
                editor.commit();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    invalidateOptionsMenu();
                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startDonateActivity() {
        Intent myIntent = new Intent(this, DonateActivity.class);
        startActivity(myIntent);
    }

    public void removeLocationSelection() {
        mSlideUpPanel.hidePane();
    }

    /**
     * Switches the Slideup fragment to a DepartureFragment if location is an
     * AbstractStop or a PlaceBodyFragment if location is a Place. Shows/Expands
     * the panel if appropriate.
     *
     * @param location The uri of the location.
     */
    private void selectLocation(final Uri location) {
        switchInfoPaneHeadToLocation(location);

        // auto expand the pane to the anchor if it is a stop and our current
        // frag says we can, eg FavoritesFrag.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shouldExpandFully = sharedPref.getBoolean(SettingsActivity
                .KEY_PREF_UI_CATEGORY_SLIDE_UP_DEFAULT_HEIGHT, false);
        if (mCurrFrag.shouldExpandLocationPaneAfterShow()) {
            if (!mSlideUpPanel.isPaneVisible()) {
                mSlideUpPanel.showPane(true, shouldExpandFully);
            } else
                expandPane();
        } else
            mSlideUpPanel.showPane();
    }

    private void selectDeparture(Departure departure) {
        selectNavDrawerItemFragId(FRAG_MAP);
        mSlideUpPanel.collapsePane();
        if (mCurrFrag instanceof MapV2ContainerFragment)
            ((MapV2ContainerFragment) mCurrFrag).setDeparture(departure);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            // Check if we need to close the drawer
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }
        if (mSlideUpPanel.isPaneVisible() && mSlideUpPanel.isOpen()) {
            // check if we need to collapse the panel
            mSlideUpPanel.collapsePane();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // if we have a back stack, make sure it is processed first.
            Fragment frag = getSupportFragmentManager().findFragmentById(
                    R.id.slideup_container);
            boolean wasConsumed = false;
            if (frag != null && frag instanceof SlideupHeadFragment) {
                wasConsumed = ((SlideupHeadFragment) frag)
                        .handleOnBackPressed();
            }
            if (!wasConsumed) {
                super.onBackPressed();
                return;
            }
        }
        if (mSlideUpPanel.isPaneVisible() && !mSlideUpPanel.isOpen()) {
            // check if we need to hide the panel
            mSlideUpPanel.hidePane();
            return;
        } if (mCurrFrag.onBack()) {
            // If the mCurrFrag view pager does handle the back, then
            // just return
            return;
        }

        // Check if the user has set a home screen
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean homeScreenEnabled = sharedPref.getBoolean(SettingsActivity
                .KEY_PREF_UI_NAVIGATION_HOME_SCREEN_ENABLED, false);

        if (homeScreenEnabled) {
            String home_screen = sharedPref.getString(SettingsActivity
                    .KEY_PREF_UI_NAVIGATION_HOME_SCREEN, "100");
            int pos = Integer.parseInt(home_screen);
            boolean wasSwitched = selectNavDrawerItemPosition(pos);
            if (wasSwitched) {
                return;
            }
        }

        super.onBackPressed();
    }

    private boolean isItineraryVisible() {
        if (!mSlideUpPanel.isPaneVisible())
            return false;
        Fragment frag = getSupportFragmentManager().findFragmentById(
                R.id.slideup_container);
        return frag != null && frag instanceof ItineraryBodyFrag;
    }

    private boolean isTripPlannerVisible() {
        if (!mSlideUpPanel.isPaneVisible())
            return false;
        Fragment frag = getSupportFragmentManager().findFragmentById(
                R.id.slideup_container);
        return frag != null && frag instanceof TripPlanViewerFragment;
    }

    public boolean isPanelOpen() {
        return mSlideUpPanel.isOpen();
    }

    public MyLocation getPanelLocation() {
        if (mSlideUpPanel.isPaneVisible()) {
            Fragment frag = getSupportFragmentManager().findFragmentById(
                    R.id.slideup_container);
            if (frag != null && frag instanceof MyLocationSlideupHeadFragment)
                return ((MyLocationSlideupHeadFragment) frag).getLocation();

        }
        return null;
    }

    public TripPlanParameters getPanelTripPlanParameters() {
        if (mSlideUpPanel.isPaneVisible()) {
            Fragment frag = getSupportFragmentManager().findFragmentById(
                    R.id.slideup_container);
            if (frag != null && frag instanceof TripPlanViewerFragment)
                return ((TripPlanViewerFragment) frag).getTripParameters();

        }
        return null;
    }

    public void expandPane() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shouldExpandFully = sharedPref.getBoolean(SettingsActivity
                .KEY_PREF_UI_CATEGORY_SLIDE_UP_DEFAULT_HEIGHT, false);
        if (shouldExpandFully)
            mSlideUpPanel.expandPane();
        else
            mSlideUpPanel.expandPaneToAnchor();
    }

    /**
     * Returns the Slide Up Pane's dragView height, or 0 if the pane is not
     * visible.
     *
     * @return
     */
    private int getPanelHeight() {
        return (mSlideUpPanel != null && mSlideUpPanel.isPaneVisible()) ? mSlideUpPanel
                .getPanelHeight() : 0;
    }

    private void setupSlideupHeadListeners(SlideupHeadFragment fragment) {
        fragment.setOnDragViewSetListener(mSlideUpHeadDragViewSetListener);
        fragment.setOnRequestPanelInfoListener(getOnRequestPanelInfoListener());

        if (fragment instanceof ItineraryBodyFrag) {
            ((ItineraryBodyFrag) fragment)
                    .setOnItineraryChangedListener(new ItineraryBodyFrag.OnItineraryChangedListener() {
                        @Override
                        public void onItinerarySet(Itinerary itinerary) {
                            Fragment frag = getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container);
                            if (frag instanceof MapV2ContainerFragment)
                                ((MapV2ContainerFragment) frag)
                                        .setItinerary(itinerary);
                        }

                        @Override
                        public void onItineraryRemoved() {
                            Fragment frag = getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container);
                            if (frag instanceof MapV2ContainerFragment)
                                ((MapV2ContainerFragment) frag)
                                        .removeItinerary();
                        }
                    });
        } else if (fragment instanceof TripPlanViewerFragment) {
            ((TripPlanViewerFragment) fragment)
                    .setOnItineraryClickListener(new TripPlanViewerFragment.OnItineraryClickListener() {
                        @Override
                        public void onItineraryClick(Itinerary itinerary,
                                                     TripPlanParameters tripParams) {
                            switchInfoHeadToItinerary(itinerary, tripParams);
                        }
                    });
            ((TripPlanViewerFragment) fragment)
                    .setOnOriginDestinationChangeListener(new OnOriginDestinationChangedListener() {

                        @Override
                        public void onOriginSet(MyLocation origin) {
                            if (mCurrFrag instanceof MapV2ContainerFragment)
                                ((MapV2ContainerFragment) mCurrFrag)
                                        .setTripOrigin(origin);
                        }

                        @Override
                        public void onOriginRemoved() {
                            if (mCurrFrag instanceof MapV2ContainerFragment)
                                ((MapV2ContainerFragment) mCurrFrag)
                                        .removeTripOrigin();
                        }

                        @Override
                        public void onDestinationSet(MyLocation destination) {
                            if (mCurrFrag instanceof MapV2ContainerFragment)
                                ((MapV2ContainerFragment) mCurrFrag)
                                        .setTripDestination(destination);
                        }

                        @Override
                        public void onDestinationRemoved() {
                            if (mCurrFrag instanceof MapV2ContainerFragment)
                                ((MapV2ContainerFragment) mCurrFrag)
                                        .removeTripDestination();
                        }
                    });
        } else if (fragment instanceof MyLocationSlideupHeadFragment) {
            ((MyLocationSlideupHeadFragment) fragment)
                    .setOnNavigateSelectedListener(new MyLocationSlideupHeadFragment.OnNavigateSelectedListener() {
                        @Override
                        public void onNavigateSelected(MyLocation location) {
                            switchInfoToNavigation(location);
                        }
                    });
            ((MyLocationSlideupHeadFragment) fragment)
                    .setOnLocationSetListener(new MyLocationSlideupHeadFragment.OnLocationSetListener() {
                        @Override
                        public void onLocationSet(Uri location) {
                            Fragment frag = getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container);
                            if (frag instanceof MapV2ContainerFragment)
                                ((MapV2ContainerFragment) frag)
                                        .setSelectedLocation(location);
                        }
                    });
            ((MyLocationSlideupHeadFragment) fragment)
                    .setDragViewOnClickListener(new SlideupHeadFragment.OnDragViewClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!mSlideUpPanel.isOpen()) {
                                expandPane();
                            }
                        }
                    });

            if (fragment instanceof DeparturesFragment)
                ((DeparturesFragment) fragment)
                        .setOnDepartureClickListener(new DeparturesFragment.OnDepartureClickListener() {
                            @Override
                            public void onDepartureClick(Departure departure) {
                                selectDeparture(departure);
                            }
                        });

        }
    }

    protected void switchInfoHeadToItinerary(Itinerary itinerary,
                                             TripPlanParameters tripParams) {
        ItineraryBodyFrag slideFrag = null;

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = supportFragmentManager
                .beginTransaction();
        slideFrag = new ItineraryBodyFrag();

        setupSlideupHeadListeners(slideFrag);

        transaction.replace(R.id.slideup_container, slideFrag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);
        transaction.commit();
        supportFragmentManager.executePendingTransactions();

        slideFrag.setItinerary(itinerary, tripParams);
        mSlideupFrag = slideFrag;
    }

    protected void switchInfoToNavigation(MyLocation location) {
        TripPlanViewerFragment slideFrag = null;

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = supportFragmentManager
                .beginTransaction();
        slideFrag = new TripPlanViewerFragment();

        setupSlideupHeadListeners(slideFrag);

        transaction.replace(R.id.slideup_container, slideFrag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);
        transaction.commit();
        supportFragmentManager.executePendingTransactions();
        mSlideUpPanel.setDragView(slideFrag.getDragView());
        slideFrag.setDestination(location);
        mSlideupFrag = slideFrag;

    }

    private void switchInfoPaneHeadToLocation(final Uri location) {
        Fragment fragHead = getSupportFragmentManager().findFragmentById(
                R.id.slideup_container);

        MyLocationSlideupHeadFragment slideFrag = null;

        if (fragHead != null
                && fragHead instanceof MyLocationSlideupHeadFragment
                && !((MyLocationSlideupHeadFragment) fragHead)
                .shouldReInitialize(location)) {
            Log.i(TAG, "not recreating");
            slideFrag = ((MyLocationSlideupHeadFragment) fragHead);
        } else {
            Log.i(TAG, "recreating");
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            slideFrag = MyLocationSlideupHeadFragment.newInstance(location);

            Bundle trial = new Bundle();
            trial.putString("testest", "testing");
            slideFrag.setArguments(trial);

            setupSlideupHeadListeners(slideFrag);

            FragmentTransaction transaction = supportFragmentManager
                    .beginTransaction();

            transaction.replace(R.id.slideup_container, slideFrag);
            transaction
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.commit();
            supportFragmentManager.executePendingTransactions();
        }
        slideFrag.setLocation(location);

        mSlideupFrag = slideFrag;
        mSlideUpPanel.setDragView(slideFrag.getDragView());
    }

    /**
     * Switches the foreground content to the fragment specified by id.
     *
     * @param id
     * @return true if content was indeed switched.
     */
    public boolean switchContent(int id) {
        boolean needsTransaction = false;
        switch (id) {
            case FRAG_FAVORITES:
                if (!(mCurrFrag instanceof FavoritesFragment)) {
                    currentFrag = FRAG_FAVORITES;
                    FavoritesFragment frag = new FavoritesFragment();
                    mCurrFrag = frag;
                    needsTransaction = true;
                }
                break;
            case FRAG_MAP:
                if (!(mCurrFrag instanceof MapV2ContainerFragment)) {
                    currentFrag = FRAG_MAP;
                    MapV2ContainerFragment frag = new MapV2ContainerFragment();
                    mCurrFrag = frag;
                    needsTransaction = true;
                }
                break;
            case FRAG_NEARBY:
                if (!(mCurrFrag instanceof NearbyFragment)) {
                    currentFrag = FRAG_NEARBY;
                    NearbyFragment frag = new NearbyFragment();
                    mCurrFrag = frag;
                    needsTransaction = true;
                }
                break;
            case FRAG_ROUTES:
                if (!(mCurrFrag instanceof VPRoutesFrag)) {
                    currentFrag = FRAG_ROUTES;
                    VPRoutesFrag frag = new VPRoutesFrag();
                    mCurrFrag = frag;
                    needsTransaction = true;
                }
                break;

        }
        if (needsTransaction) {
            mSlideUpPanel.collapsePane();

            // have to destroy and recreate the MapV2Frag so it is visible when
            // we switch back to VPMapFrag. had to add this in addition to the
            // hack in MapV2Container from
            // http://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi
            // It's ok, probably want to destroy the fragment anyway to free up
            // some memory.
            MapV2Fragment f = (MapV2Fragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapv2);
            if (f != null)
                getSupportFragmentManager().beginTransaction().remove(f)
                        .commit();

            // Every MyFragment needs listeners
            setupMyFragmentListeners(mCurrFrag);

            FragmentManager supportFragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = supportFragmentManager
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, mCurrFrag);
            // transaction.addToBackStack(null);
            transaction
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.commit();
            supportFragmentManager.executePendingTransactions();
            // Only set action bar visible if drawer is closed. otherwise the
            // drawer closing event will control the action bar.
            if (!mDrawerLayout.isDrawerOpen(mDrawerList))
                setActionBarVisible(mCurrFrag.shouldShowActionBar());
        }
        return needsTransaction;
    }

    private void setupMyFragmentListeners(MyFragment fragment) {
        // every MyFragment will need this.
        fragment.setOnRequestPanelInfoListener(getOnRequestPanelInfoListener());

        // These fragments will need onLocationSelectedListeners
        if (fragment instanceof FavoritesFragment) {
            ((FavoritesFragment) fragment)
                    .setOnLocationSelectedListener(new MyFragmentsListener.OnLocationSelectedListener() {
                        @Override
                        public void onLocationSelected(Uri location) {
                            selectLocation(location);
                        }
                    });
        } else if (fragment instanceof MapV2ContainerFragment) {
            ((MapV2ContainerFragment) fragment)
                    .setOnLocationSelectedListener(new MyFragmentsListener.OnLocationSelectedListener() {
                        @Override
                        public void onLocationSelected(Uri location) {
                            selectLocation(location);
                        }
                    });
            ((MapV2ContainerFragment) fragment)
                    .setOnRemoveLocationSelectionListener(new MapV2Fragment.OnRemoveSelectedLocationListener() {
                        @Override
                        public void onRemoveSelectedLocationListener() {
                            mSlideUpPanel.hidePane();
                        }
                    });
            ((MapV2ContainerFragment) fragment)
                    .setOnRequestFragmentVisibilityListener(new OnRequestFragmentVisibilityListener() {
                        @Override
                        public boolean onRequestShouldUpdateLocation() {
                            // only allow setting location on map if the trip
                            // planner and itinerary are NOT visible.
                            return !isTripPlannerVisible()
                                    && !isItineraryVisible();
                        }
                    });
            ((MapV2ContainerFragment) fragment)
                    .setOnRequestTripPlanInfoListener(new OnRequestTripPlanInfoListener() {
                        @Override
                        public TripPlanParameters onRequestTripPlanParameters() {
                            Fragment frag = getSupportFragmentManager()
                                    .findFragmentById(R.id.slideup_container);
                            if (frag instanceof TripPlanViewerFragment) {
                                return ((TripPlanViewerFragment) frag)
                                        .getTripParameters();
                            } else if (frag instanceof ItineraryBodyFrag) {
                                return ((ItineraryBodyFrag) frag)
                                        .getTripParameters();
                            }
                            return null;
                        }
                    });
            ((MapV2ContainerFragment) fragment)
                    .setOnRequestItineraryInfoListener(new OnRequestItineraryInfoListener() {
                        @Override
                        public Itinerary onRequestPanelItinerary() {
                            Fragment frag = getSupportFragmentManager()
                                    .findFragmentById(R.id.slideup_container);
                            if (frag instanceof ItineraryBodyFrag) {
                                return ((ItineraryBodyFrag) frag)
                                        .getItinerary();
                            }
                            return null;
                        }
                    });
        } else if (fragment instanceof NearbyFragment) {
            ((NearbyFragment) fragment)
                    .setOnLocationSelectedListener(new MyFragmentsListener.OnLocationSelectedListener() {
                        @Override
                        public void onLocationSelected(Uri location) {
                            selectLocation(location);
                        }

                    });
        } else if (fragment instanceof VPRoutesFrag) {
            ((VPRoutesFrag) fragment)
                    .setOnRequestActionBarVisibleListener(new MyFragmentsListener.OnRequestActionBarVisibleListener() {
                        @Override
                        public void onRequestActionBarVisible(boolean visible) {
                            setActionBarVisible(visible);
                        }
                    });
        }

    }

    private OnRequestPanelInfoListener getOnRequestPanelInfoListener() {
        if (mRequestPanelInfoListener == null)
            mRequestPanelInfoListener = new OnRequestPanelInfoListener() {
                @Override
                public Uri onRequestPanelLocation() {
                    if (mSlideUpPanel.isPaneVisible()) {
                        Fragment frag = getSupportFragmentManager()
                                .findFragmentById(R.id.slideup_container);
                        if (frag instanceof MyLocationSlideupHeadFragment) {
                            return ((MyLocationSlideupHeadFragment) frag)
                                    .getLocationUri();
                        }
                    }
                    return null;
                }

                @Override
                public int onRequestPanelHeight() {
                    return getPanelHeight();
                }

                @Override
                public boolean onRequestPanelIsOpen() {
                    return mSlideUpPanel.isOpen();
                }
            };
        return mRequestPanelInfoListener;
    }

    public String getCurrFragmentTitle() {
        switch (currentFrag) {
            case FRAG_FAVORITES:
                return "Favorites";
            case FRAG_MAP:
                return "Map";
            case FRAG_NEARBY:
                return "Nearby";
            case FRAG_SEARCH:
                return "Search";
            case FRAG_ROUTES:
                return "Routes";

            default:
                return "CU Bus Guide";
        }
    }

    private void setActionBarVisible(boolean visible) {
        if (visible) {
            getSupportActionBar().show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                getSupportActionBar().hide();
        }
    }

    private void checkForGTFSUpdate() {
        boolean isInitializing = checkDatabases(); // will initialize databases
        // if they are bad.
        if (isInitializing)
            return;
        final SharedPreferences settings = getSharedPreferences(
                DatabaseAdapter.DbOk, 0);
        int initialized = settings.getInt(DatabaseAdapter.DbOkStops,
                DatabaseAdapter.IS_BAD);
        if (initialized != DatabaseAdapter.IS_OK) {
            // the databases have not been initialized
            Log.d(TAG, "DatabaseAdapter not OK");
            return;
        }
        // If we've made it this far, databases are OK and we just need to check
        // for GTfs update.

        // Only check once per 24 hours.
        long currTime = System.currentTimeMillis();
        long prevTime = settings.getLong(DatabaseAdapter.DbLastGTFSCheck,
                currTime - 10 * 86400000);
        // if prev time < 24 hours (in milliseconds), do nothing.
        if (currTime - prevTime < 86400000) {
            Log.i(TAG,
                    "checkForGTFSUpdate not waited long enough. Only waited "
                            + (currTime - prevTime) + "/86400000 milliseconds"
            );
            return;
        }
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(DatabaseAdapter.DbLastGTFSCheck, currTime);
        editor.commit();

        final Context context = this;
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String lastGtfsUpdate = mMtdApiAdapter.getLastFeedUpdate();
                if (mMtdApiAdapter.hasError() || lastGtfsUpdate == null) {
                    Log.d(TAG, "getLastFeedUpdate return error");
                    return null;
                }
                String lastStopsUpdate = settings.getString(
                        DatabaseAdapter.DbStopsUpdateTime,
                        "2012-01-24T15:57:09-06:00");
                String lastRoutesUpdate = settings.getString(
                        DatabaseAdapter.DbRoutesUpdateTime,
                        "2012-01-24T15:57:09-06:00");
                boolean needsUpdate = needsGTFSUpdate(lastGtfsUpdate,
                        lastStopsUpdate, lastRoutesUpdate);
                if (needsUpdate) {
                    Log.i(TAG, "NEEDS GTFS UPDATE");
                    Intent intent = new Intent(context, MtdGtfsService.class);
                    // Create a new messenger for the communication back
                    Messenger messenger = new Messenger(mHandler);
                    intent.putExtra("MESSENGER", messenger);
                    intent.putExtra("operation", "updateStopsData");
                    startService(intent);

                    intent = new Intent(context, MtdGtfsService.class);
                    // Create a new messenger for the communication back
                    messenger = new Messenger(mHandler);
                    intent.putExtra("MESSENGER", messenger);
                    intent.putExtra("operation", "updateRoutesData");
                    startService(intent);
                } else {
                    Log.i(TAG, "GTFS UPDATE NOT NEEDED");
                }
                return null;
            }

        };
        task.execute();
    }

    /**
     * Checks to see if the activity was closed during a database operation,
     * like downloading or updating stops. If so, set's the database to invalid
     * so we can retry. If databases are bad, this method tries to redownload
     * everything.
     * <p/>
     * returns true if initializing a database.
     */
    private boolean checkDatabases() {
        SharedPreferences settings = getSharedPreferences(DatabaseAdapter.DbOk,
                0);

        boolean ret = false;

        int stopsDb = settings.getInt(DatabaseAdapter.DbOkStops,
                DatabaseAdapter.IS_BAD);
        if (!(stopsDb == DatabaseAdapter.IS_OK
                || stopsDb == DatabaseAdapter.IS_WORKING || stopsDb == DatabaseAdapter.IS_UPDATING)) {
            Intent intent = new Intent(this, MtdGtfsService.class);
            // Create a new messenger for the communication back
            Messenger messenger = new Messenger(mHandler);
            intent.putExtra("MESSENGER", messenger);
            intent.putExtra("operation", "initializeStops");
            startService(intent);
            ret = true;
        }
        int routesDb = settings.getInt(DatabaseAdapter.DbOkRoutes,
                DatabaseAdapter.IS_BAD);
        if (!(routesDb == DatabaseAdapter.IS_OK
                || routesDb == DatabaseAdapter.IS_WORKING || routesDb == DatabaseAdapter.IS_UPDATING)) {
            Intent intent = new Intent(this, MtdGtfsService.class);
            // Create a new messenger for the communication back
            Messenger messenger = new Messenger(mHandler);
            intent.putExtra("MESSENGER", messenger);
            intent.putExtra("operation", "initializeRoutes");
            startService(intent);
            ret = true;
        }
        return ret;
    }

    /**
     * Compares Dates in strings, of format like 2012-01-24T15:57:09-06:00.
     * Returns true if lastGtfsUpdate is after either lastStopsUpdate or
     * lastRouteUpdate.
     *
     * @param lastGtfsUpdate
     * @param lastStopsUpdate
     * @param lastRoutesUpdate
     * @return
     */
    private boolean needsGTFSUpdate(String lastGtfsUpdate,
                                    String lastStopsUpdate, String lastRoutesUpdate) {
        String[] split = lastGtfsUpdate.split("T");
        split = split[0].split("-");
        int lastGtfs = Integer.parseInt(split[0] + split[1] + split[2]);
        split = lastStopsUpdate.split("T");
        split = split[0].split("-");
        int lastStops = Integer.parseInt(split[0] + split[1] + split[2]);
        split = lastRoutesUpdate.split("T");
        split = split[0].split("-");
        int lastRoutes = Integer.parseInt(split[0] + split[1] + split[2]);
        return lastGtfs > lastStops || lastGtfs > lastRoutes;
    }

    @Override
    protected void onDestroy() {
        // Check to make sure the app wasn't force closed during database
        // initialization!
        SharedPreferences settings = getSharedPreferences(DatabaseAdapter.DbOk,
                0);
        SharedPreferences.Editor editor = settings.edit();
        int stopsDb = settings.getInt(DatabaseAdapter.DbOkStops,
                DatabaseAdapter.IS_BAD);
        if (stopsDb != DatabaseAdapter.IS_OK) {
            editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_BAD);
        }
        int routesDb = settings.getInt(DatabaseAdapter.DbOkRoutes,
                DatabaseAdapter.IS_BAD);
        if (routesDb != DatabaseAdapter.IS_OK) {
            editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_BAD);
        }
        editor.commit();

        shaveDatabaseCachesIfNeeded();

        super.onDestroy();
    }

    /**
     * Starts a service that will count the number of nonfavorite googleplaces,
     * and delete places not recently accessed until the table is below a
     * certain size.
     */
    private void shaveDatabaseCachesIfNeeded() {
        Intent intent = new Intent(this, DatabaseClearingService.class);
        intent.setAction(DatabaseClearingService.SHAVE_PLACE_CACHE);
        startService(intent);
    }

    // private void unregisterC2DM() {
    // C2DMessaging.unregister(this);
    // }
    //
    // private void initializeC2DM() {
    // C2DMessaging.register(this, "teamparkinmtdapp@gmail.com");
    // }

    @Override
    protected void onResume() {
        super.onResume();
        checkForGTFSUpdate();
        checkGooglePlayServices();
    }

    /**
     * Checks that the google play services library on the device is up to date.
     * If the result code is SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
     * or SERVICE_DISABLED, then call getErrorDialog() to display an error
     * message to the user, which allows the user to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     * <p/>
     * See https://developer.android.com/google/play-services/setup.html
     */
    private void checkGooglePlayServices() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        int[] checks = new int[]{ConnectionResult.SERVICE_MISSING,
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
                ConnectionResult.SERVICE_DISABLED};
        for (int i : checks) {
            if (result == i) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result,
                        this, GOOGLE_PLAY_SERVICES_ERROR_RESULT);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        MTDAppActivity.this.finish();
                    }
                });
                dialog.show();
                break;
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        // mDatabase.closeDatabase();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_SEARCH_ID:
                // This is called when a new Loader needs to be created. This
                // sample only has one Loader, so we don't care activity_about the ID.
                // First, pick the base URI to use depending on whether we are
                // currently filtering.
                Uri baseUri;
                if (mCurSearchViewFilter != null) {
                    baseUri = Uri.withAppendedPath(
                            StopsProvider.CONTENT_URI_SUGGEST,
                            Uri.encode(mCurSearchViewFilter));
                } else {
                    baseUri = StopsProvider.CONTENT_URI_SUGGEST;
                }

                CursorLoader cursorLoader = new CursorLoader(this, baseUri,
                        StopsProvider.DEFAULT_CURSOR_COLUMNS, null, null, null);
                return cursorLoader;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_SEARCH_ID:
                mSearchSuggestAadapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        switch (loader.getId()) {
            case LOADER_SEARCH_ID:
                // data is not available anymore, delete reference
                mSearchSuggestAadapter.swapCursor(null);
                break;
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        private int year;
        private int month;
        private int day;
        private DatePickerDialog.OnDateSetListener listener;

        public DatePickerFragment() {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
            listener = this;
        }

        public void setInitialDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), listener, year, month,
                    day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        }
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectNavDrawerItemPosition(position);
        }

    }

    private class MyNavDrawerItem {
        private String tag;
        private int iconRes;
        private int fragId;

        public MyNavDrawerItem(int fragId, String tag, int iconRes) {
            this.fragId = fragId;
            this.tag = tag;
            this.iconRes = iconRes;
        }

        @SuppressWarnings("unused")
        public String getTag() {
            return tag;
        }

        @SuppressWarnings("unused")
        public int getIconRes() {
            return iconRes;
        }

        public int getFragId() {
            return fragId;
        }
    }

    public class MyDrawerListAdapter extends ArrayAdapter<MyNavDrawerItem> {

        public MyDrawerListAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.menu_row, null);
            }
            TextView title = (TextView) convertView
                    .findViewById(R.id.row_title);
            title.setText(getItem(position).tag);
            title.setCompoundDrawablesWithIntrinsicBounds(
                    getItem(position).iconRes, 0, 0, 0);

            return convertView;
        }

        /**
         * Returns the position of the FragId id in the Nav Drawer.
         *
         * @param id
         * @return
         */
        public int getPositionByFragId(int id) {
            int count = getCount();
            for (int position = 0; position < count; position++) {
                MyNavDrawerItem item = getItem(position);
                if (item.getFragId() == id)
                    return position;
            }
            return -1;
        }

    }

    public class MySimpleCursorAdapter extends SimpleCursorAdapter {

        public MySimpleCursorAdapter(Context context, int layout, Cursor c,
                                     String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

    }
}