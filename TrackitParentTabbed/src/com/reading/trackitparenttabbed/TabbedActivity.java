package com.reading.trackitparenttabbed;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TabbedActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	CustomViewPager mViewPager;

	private static final int ADD_RADIAL_INDEX = 0;
	private static final int CONVEX_INDEX = 1;
	private static final int REFRESH_INDEX = 2;

	// Menu items list
	public ArrayList<MenuItem> menus = new ArrayList<MenuItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(), this);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (CustomViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		// Check we are registered for GCM, and send the latest version of the
		// key to the server
		updateGCM();

		this.registerReceiver(receiver, new IntentFilter("DeviceDelete"));
	}

	private void updateGCM() {
		GCMUpdate update = new GCMUpdate(this);
		update.execute(new String());
	}

	@Override
	public void onBackPressed() {

	}

	public void login(View view) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Add the action bar menu items to the action bar and the index

		MenuItem mi = menu.add(0, ADD_RADIAL_INDEX, 0, "Add Radial Boundary");
		mi.setIcon(android.R.drawable.ic_menu_add);
		mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menus.add(mi);

		MenuItem mi2 = menu.add(0, CONVEX_INDEX, 0, "Add Freeform Boundary");
		mi2.setIcon(android.R.drawable.ic_menu_mapmode);
		mi2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menus.add(mi2);

		MenuItem mi3 = menu.add(0, REFRESH_INDEX, 0, "Refresh Locations");
		mi3.setIcon(R.drawable.ic_menu_refresh);
		mi3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menus.add(mi3);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_RADIAL_INDEX: {
			addGeofence();
		}
			break;
		case CONVEX_INDEX: {
			// Enable freeform drawing
			onFreeForm();
		}
			break;
		case REFRESH_INDEX: {
			reloadDevices();
		}
			break;

		}
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.

		// Going to the map fragment enable to action bar items
		if (tab.getPosition() == 0) {
			for (int i = 0; i < menus.size(); i++) {
				MenuItem item = menus.get(i);
				item.setVisible(true);
			}
		}

		// Going to the options tab disable the action bar items
		if (tab.getPosition() == 1) {
			for (int i = 0; i < menus.size(); i++) {
				MenuItem item = menus.get(i);
				item.setVisible(false);
			}
		}
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	private void reloadDevices() {
		LocationRequester requester = new LocationRequester(this);
		requester.execute(new String());
	}

	private void addGeofence() {
		MapSectionFragment fragment = (MapSectionFragment) getSupportFragmentManager()
				.findFragmentByTag(mSectionsPagerAdapter.getTabTag(0));

		fragment.addMarker();
	}

	private void onFreeForm() {
		// Enable freeform drawing
		MapSectionFragment fragment = (MapSectionFragment) getSupportFragmentManager()
				.findFragmentByTag(mSectionsPagerAdapter.getTabTag(0));

		fragment.tapToAdd = true;

		// Grey out and disable the button
		MenuItem item = menus.get(CONVEX_INDEX);
		Drawable resIcon = getResources().getDrawable(
				android.R.drawable.ic_menu_mapmode);
		resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

		item.setEnabled(false);
		item.setIcon(resIcon);
	}

	public void onConvexSave() {
		// Re-enable the button
		MenuItem item = menus.get(CONVEX_INDEX);
		item.setEnabled(true);
		item.setIcon(android.R.drawable.ic_menu_mapmode);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().compareTo(
					context.getString(R.string.device_delete_broadcast_action)) == 0) {

				SharedPreferences prefs = context.getSharedPreferences(
						context.getString(R.string.authentication), 0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove("authToken");
				editor.remove("device_id");
				editor.remove("authenticated");
				editor.commit();

				Intent loginIntent = new Intent(context, MainActivity.class);
				startActivity(loginIntent);
			}
		}
	};

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */

}
