package com.reading.trackitparenttabbed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DummySectionFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// // Create a new TextView and set its text to the fragment's section
		// // number argument value.
		// TextView textView = new TextView(getActivity());
		// textView.setGravity(Gravity.CENTER);
		// textView.setText(Integer.toString(getArguments().getInt(
		// ARG_SECTION_NUMBER)));
		// return textView;

		// View view = super.onCreateView(inflater, container,
		// savedInstanceState);
		View view2 = inflater.inflate(R.layout.menu, null);
		
		return view2;
	}
}