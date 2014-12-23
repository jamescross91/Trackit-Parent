package com.reading.trackitparenttabbed;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class GCMUpdate extends Network {

	private HttpResponse response;
	private boolean ok;

	public GCMUpdate(Context context) {
		super(context);
	}

	public HttpResponse updateToken() {

		GCMRegistrar.checkDevice(thisContext);
		GCMRegistrar.checkManifest(thisContext);
		final String regId = GCMRegistrar.getRegistrationId(thisContext);
		if (regId.equals("")) {
			GCMRegistrar.register(thisContext,
					thisContext.getString(R.string.gcm_project_id));
		} else {
			Log.v("GCM", "Already registered");
		}

		String reg = GCMRegistrar.getRegistrationId(thisContext);

		String android_id = Secure.getString(thisContext.getContentResolver(),
				Secure.ANDROID_ID);

		SharedPreferences auth = thisContext.getSharedPreferences(
				thisContext.getString(R.string.authentication), 0);
		String authToken = auth.getString("authToken", "");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", android_id));
		pairs.add(new BasicNameValuePair("gcm_token", reg));
		pairs.add(new BasicNameValuePair("auth_token", authToken));

		return networkExec(formatLoginURL(), pairs);
	}

	private String formatLoginURL() {
		String URL = thisContext.getString(R.string.server_root)
				+ thisContext.getString(R.string.gcm_update_url);

		return URL;
	}

	@Override
	protected String doInBackground(String... params) {
		this.response = updateToken();
		process();

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (ok) {
			Toast.makeText(thisContext,
					"Server updated with latest device information",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(
					thisContext,
					"Failed to update the server.  Updates may fail.  Please check your network connection.",
					Toast.LENGTH_LONG).show();
		}
	}

	protected void process() {
		// Get the JSon response from the server
		String responseBody;
		ok = true;
		try {
			if (response == null) {
				ok = false;
			}

			responseBody = EntityUtils.toString(response.getEntity());
			JSONObject json = new JSONObject(responseBody);
			if (json.has("failure")) {
				ok = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ok = false;
		}

	}

}
