package com.reading.trackitparenttabbed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RadialGeofenceHandler extends Network {
	public static final int SAVE_ACTION = 0;
	public static final int DELETE_ACTION = 1;
	public static final int LOAD_ACTION = 2;

	public static final String ACTION_SUCCESS = "success";
	public static final String ACTION_FAILED = "failed";

	private RadialGeoFenceMarker oneMarker;
	private ArrayList<RadialGeoFenceMarker> markers = new ArrayList<RadialGeoFenceMarker>();
	private int action;
	private MapSectionFragment fragment;

	public RadialGeofenceHandler(Context context) {
		super(context);
	}

	public RadialGeoFenceMarker getOneMarker() {
		return oneMarker;
	}

	public void setOneMarker(RadialGeoFenceMarker oneMarker) {
		this.oneMarker = oneMarker;
	}

	private HttpResponse savePoint() {

		String android_id = Secure.getString(thisContext.getContentResolver(),
				Secure.ANDROID_ID);

		SharedPreferences auth = thisContext.getSharedPreferences(
				thisContext.getString(R.string.authentication), 0);
		String authToken = auth.getString("authToken", "");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", android_id));
		pairs.add(new BasicNameValuePair("latitude", Double.toString(oneMarker
				.getPos().latitude)));
		pairs.add(new BasicNameValuePair("longitude", Double.toString(oneMarker
				.getPos().longitude)));
		pairs.add(new BasicNameValuePair("radius", Double.toString(oneMarker
				.getSliderProgress())));
		pairs.add(new BasicNameValuePair("auth_token", authToken));
		pairs.add(new BasicNameValuePair("marker_id", Long.toString(oneMarker
				.getMarker_id())));

		return networkExec(formatURL(), pairs);
	}

	private boolean processSaveResponse(HttpResponse response) {

		try {
			// Get the JSon response from the server
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONObject json = new JSONObject(responseBody);
			if (json.has("failure")) {
				return false;
				// Device did not authenticate
			} else {
				oneMarker.setMarker_id(json.getLong("marker_id"));
				Log.i("Radial Geofence Handler", "Marker successfully saved");
			}
		} catch (Exception e) {
			Log.e("Radial Geofence Handler", "Failed to parse Json object");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private HttpResponse loadPoints() {

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

	private boolean processLoadResponse(HttpResponse response) {

		try {
			// Get the JSon response from the server
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONObject json = new JSONObject(responseBody);
			if (json.has("failure")) {
				return false;
				// Device did not authenticate
			} else {
				Iterator<?> keys = json.keys();

				while (keys.hasNext()) {
					String key = (String) keys.next();
					JSONObject thisObject = (JSONObject) json.get(key);

					RadialGeoFenceMarker marker = new RadialGeoFenceMarker(fragment);
					double lat = thisObject.getDouble("lat");
					double lng = thisObject.getDouble("lng");
					marker.setPos(new LatLng(lat, lng));
					marker.setSliderProgress(thisObject.getDouble("radius"));
					marker.setMarker_id(thisObject.getLong("marker_id"));

					markers.add(marker);
				}
			}
		} catch (Exception e) {
			Log.e("Radial Geofence Handler", "Failed to parse Json object");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private HttpResponse deletePoint() {

		String android_id = Secure.getString(thisContext.getContentResolver(),
				Secure.ANDROID_ID);

		SharedPreferences auth = thisContext.getSharedPreferences(
				thisContext.getString(R.string.authentication), 0);
		String authToken = auth.getString("authToken", "");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", android_id));
		pairs.add(new BasicNameValuePair("auth_token", authToken));
		pairs.add(new BasicNameValuePair("marker_id", Long.toString(oneMarker
				.getMarker_id())));

		return networkExec(formatURL(), pairs);
	}

	private boolean processDeleteResponse(HttpResponse response) {
		try {
			// Get the JSon response from the server
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONObject json = new JSONObject(responseBody);
			if (json.has("failure")) {
				return false;
				// Device did not authenticate or some other failure
			} else {
				return true;
			}
		} catch (Exception e) {
			Log.e("Radial Geofence Handler", "Failed to parse Json object");
			e.printStackTrace();
			return false;
		}
	}

	private String formatURL() {
		String URL = new String();
		switch (action) {
		case SAVE_ACTION:
			URL = thisContext.getString(R.string.server_root)
					+ thisContext.getString(R.string.geofence_save_url);
			break;
		case DELETE_ACTION:
			URL = thisContext.getString(R.string.server_root)
					+ thisContext.getString(R.string.geofence_delete_url);
			break;
		case LOAD_ACTION:
			URL = thisContext.getString(R.string.server_root)
					+ thisContext.getString(R.string.geofence_load_url);
			break;
		}

		return URL;
	}

	@Override
	protected void onPreExecute() {
		if ((action == (SAVE_ACTION) || (action == DELETE_ACTION))) {
			oneMarker.getDialog().findViewById(R.id.textView10)
					.setVisibility(View.INVISIBLE);
			oneMarker.getDialog().findViewById(R.id.button1)
					.setVisibility(View.INVISIBLE);
			oneMarker.getDialog().findViewById(R.id.button2)
					.setVisibility(View.INVISIBLE);
			oneMarker.getDialog().findViewById(R.id.geoSeekBar)
					.setVisibility(View.INVISIBLE);
			oneMarker.getDialog().findViewById(R.id.progressBar1)
					.setVisibility(View.VISIBLE);
			oneMarker.getDialog().findViewById(R.id.textViewErr)
					.setVisibility(View.INVISIBLE);

		}
	}

	@Override
	protected String doInBackground(String... params) {
		switch (action) {
		case SAVE_ACTION: {
			HttpResponse response = savePoint();
			if (!processSaveResponse(response)) {
				return ACTION_FAILED;
			}
			break;
		}
		case LOAD_ACTION: {
			HttpResponse response = loadPoints();
			processLoadResponse(response);
		}
			break;
		case DELETE_ACTION: {
			HttpResponse response = deletePoint();
			if (!processDeleteResponse(response))
				return ACTION_FAILED;
		}
			break;
		}

		return ACTION_SUCCESS;
	}

	@Override
	protected void onPostExecute(String result) {
		if ((action == (SAVE_ACTION) || (action == DELETE_ACTION))) {
			oneMarker.getDialog().findViewById(R.id.textView10)
					.setVisibility(View.VISIBLE);
			oneMarker.getDialog().findViewById(R.id.button1)
					.setVisibility(View.VISIBLE);
			oneMarker.getDialog().findViewById(R.id.button2)
					.setVisibility(View.VISIBLE);
			oneMarker.getDialog().findViewById(R.id.geoSeekBar)
					.setVisibility(View.VISIBLE);
			oneMarker.getDialog().findViewById(R.id.progressBar1)
					.setVisibility(View.INVISIBLE);

			if (result.compareTo(ACTION_FAILED) == 0) {
				oneMarker.getDialog().findViewById(R.id.textViewErr)
						.setVisibility(View.VISIBLE);
				TextView err = (TextView) oneMarker.getDialog().findViewById(
						R.id.textViewErr);
				err.setTextColor(Color.RED);
				err.setText("Operation failed - error connecting to server, or server error");
			} else {
				oneMarker.getDialog().findViewById(R.id.textViewErr)
						.setVisibility(View.VISIBLE);
				TextView err = (TextView) oneMarker.getDialog().findViewById(
						R.id.textViewErr);
				err.setTextColor(Color.GREEN);
				err.setText("Operation Successful");
			}
		}
		
		if((action == DELETE_ACTION) && result.compareTo(ACTION_SUCCESS) == 0){
			oneMarker.getMarker().remove();
			oneMarker.getDialog().cancel();
			oneMarker.getPoly().remove();
		}

		if (action == LOAD_ACTION) {
			addMarkers();
		}
	}

	private void addMarkers() {
		for (int i = 0; i < markers.size(); i++) {

			RadialGeoFenceMarker geoMarker = markers.get(i);

			Marker marker = fragment.map.addMarker(new MarkerOptions()
					.position(geoMarker.getPos()).title("Geofence")
					.snippet("snippet").draggable(true));
			
			geoMarker.setMarker(marker);

			geoMarker.setPoly(fragment.map.addPolyline(geoMarker
					.getOptions(geoMarker.getPos(),
							(double) geoMarker.getSliderProgress())
					.color(Color.RED).width(2)));

			final Dialog dialog = new Dialog(fragment.getActivity());
			dialog.setContentView(R.layout.geodialog);
			dialog.setTitle("Geofence Properties");
			Button b = (Button) dialog.findViewById(R.id.button1);
			b.setOnClickListener(geoMarker.getSaveButton(dialog, geoMarker));
			Button b2 = (Button) dialog.findViewById(R.id.button2);
			b2.setOnClickListener(geoMarker.getDeleteButton(dialog, geoMarker));
			geoMarker.setDialog(dialog);
			
			fragment.markers.put(marker.getId(), geoMarker);
		}
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public ArrayList<RadialGeoFenceMarker> getMarkers() {
		return markers;
	}

	public void setMarkerss(ArrayList<RadialGeoFenceMarker> markers) {
		this.markers = markers;
	}

	public MapSectionFragment getFragment() {
		return fragment;
	}

	public void setFragment(MapSectionFragment fragment) {
		this.fragment = fragment;
	}

}
