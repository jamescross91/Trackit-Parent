package com.reading.trackitparenttabbed;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

	Context context;
	private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

	public SectionsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.context = context;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a DummySectionFragment (defined as a static inner class
		// below) with the page number as its lone argument.

		Fragment fragment;
		if (position == 0) {
			fragment = new MapSectionFragment();
			return fragment;
		}
		if (position == 1) {
			fragment = new MenuFragment();
			return fragment;
		} else {
			fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
		}

		return fragment;
	}

	@Override
	public int getCount() {
		// Show 2 total pages.
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return context.getString(R.string.title_section1).toUpperCase();
		case 1:
			return context.getString(R.string.title_section2).toUpperCase();
		case 2:
			return context.getString(R.string.title_section3).toUpperCase();
		}
		return null;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container,
				position);
		TabInfo info = new TabInfo(position, fragment.getTag());
		mTabs.add(info);
		return fragment;
	}

	public String getTabTag(int position) {
		for (int i = 0; i < mTabs.size(); i++) {
			if (mTabs.get(i).tabPos == position)
				return mTabs.get(i).tabTag;
		}
		return new String();
	}

	class TabInfo {
		public int tabPos;
		public String tabTag;

		public TabInfo(int tabPos, String tabTag) {
			this.tabPos = tabPos;
			this.tabTag = tabTag;
		}
	}
}