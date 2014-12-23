package com.reading.trackitparenttabbed;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;

public class LocationRequester extends Network {

	public LocationRequester(Context context) {
		super(context);
	}

	public HttpResponse requestLocations() {

		String android_id = Secure.getString(thisContext.getContentResolver(),
				Secure.ANDROID_ID);

		SharedPreferences auth = thisContext.getSharedPreferences(
				thisContext.getString(R.string.authentication), 0);
		String authToken = auth.getString("authToken", "");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", android_id));
		pairs.add(new BasicNameValuePair("auth_token", authToken));

		return networkExec(formatURL(), pairs);
	}

	private String formatURL() {
		String URL = thisContext.getString(R.string.server_root)
				+ thisContext.getString(R.string.location_load_url);

		return URL;
	}

	@Override
	protected String doInBackground(String... params) {
		requestLocations();

		return null;
	}

}
