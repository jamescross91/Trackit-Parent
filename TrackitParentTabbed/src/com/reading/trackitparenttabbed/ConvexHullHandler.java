package com.reading.trackitparenttabbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

public class ConvexHullHandler extends Network {
	public static final int SAVE_ACTION = 0;
	public static final int DELETE_ACTION = 1;
	public static final int LOAD_ACTION = 2;

	public static final String ACTION_SUCCESS = "success";
	public static final String ACTION_FAILED = "failed";

	// Single group if we are saving - group id to markers
	private HashMap<String, ConvexHullMarker> singleGroup = new HashMap<String, ConvexHullMarker>();

	// The marker we actually tapped - so we can update the dialog as required
	private ConvexHullMarker tappedMarker;

	// List of lists if we are loading
	private HashMap<String, HashMap<String, ConvexHullMarker>> convexMarkerLists = new HashMap<String, HashMap<String, ConvexHullMarker>>();
	private int action;
	private MapSectionFragment fragment;
	private String returnedGroup_id;

	public ConvexHullHandler(Context context) {
		super(context);
	}

	private HttpResponse saveGroup() {

		// $$$$$$$$$$$$$$$$$ MUST ONLY SAVE THE HULL $$$$$$$$$$$$$$$$$$$$$$$

		String android_id = Secure.getString(thisContext.getContentResolver(),
				Secure.ANDROID_ID);

		SharedPreferences auth = thisContext.getSharedPreferences(
				thisContext.getString(R.string.authentication), 0);
		String authToken = auth.getString("authToken", "");

		// Parent JSON object which will contain some auth data and then another
		// object containing all of the markers:
		// {Device ID, Auth Token, {Group ID{marker_id{lat, long},
		// marker_id2{lat2, long2}}}}
		JSONObject object = new JSONObject();
		try {
			object.put("device_id", android_id);
			object.put("auth_token", authToken);

			JSONObject markers = new JSONObject();
			String groupID = "";

			for (Entry<String, ConvexHullMarker> entry : singleGroup.entrySet()) {
				ConvexHullMarker thisMarker = entry.getValue();
				groupID = thisMarker.getGroupID();
				JSONObject thisObject = new JSONObject();

				thisObject.put("latitude", thisMarker.getPos().latitude);
				thisObject.put("longitude", thisMarker.getPos().longitude);
				thisObject.put("marker_id", thisMarker.getMarker_id());

				String niceName = thisMarker.getNiceName();
				if (niceName != null) {
					thisObject.put("nice_name", thisMarker.getNiceName());
				}

				markers.put(Long.toString(thisMarker.getMarker_id()),
						thisObject);
			}

			object.put("group_id", groupID);
			object.put(groupID, markers);
		} catch (JSONException e) {
			Log.e("Convex Handler", e.toString());
		}

		return networkExec(formatURL(), object);
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

				// Get the markers object and iterate over it
				JSONObject markers = json.getJSONObject("markers");
				Iterator<?> keys = markers.keys();

				returnedGroup_id = String.valueOf(json.get("group_id"));

				while (keys.hasNext()) {
					String new_id = (String) keys.next();
					JSONObject thisMarker = (JSONObject) markers.get(new_id);

					// Are we getting a response for the first time?
					if (thisMarker.has("old_marker_id")) {

						// If yes, iterate over the hashmap and update the
						// relavent entry
						for (Entry<String, ConvexHullMarker> entry : singleGroup
								.entrySet()) {
							ConvexHullMarker instanceMarker = entry.getValue();
							if (instanceMarker.getMarker_id() == thisMarker
									.getLong("old_marker_id")) {
								instanceMarker.setMarker_id(thisMarker
										.getLong("marker_id"));

								instanceMarker.setGroupID(returnedGroup_id);
								break;
							}
						}
					}
				}

				Log.i("Radial Geofence Handler", "Marker successfully saved");
			}
		} catch (Exception e) {
			Log.e("Radial Geofence Handler", "Failed to parse Json object");
			e.printStackTrace();
			return false;
		}

		for (Entry<String, ConvexHullMarker> entry : singleGroup.entrySet()) {
			ConvexHullMarker instanceMarker = entry.getValue();

			Log.i("convex", "marker id, group id");
			Log.i("convex", String.valueOf(instanceMarker.getMarker_id()));
			Log.i("convex", instanceMarker.getGroupID());
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
				Iterator<?> groups = json.keys();

				// Loop over each group
				while (groups.hasNext()) {
					String groupID = (String) groups.next();
					JSONObject thisGroup = (JSONObject) json.get(groupID);
					HashMap<String, ConvexHullMarker> groupList = new HashMap<String, ConvexHullMarker>();

					Iterator<?> markers = thisGroup.keys();

					// Loop over each marker in the group, add it to a hashmap
					// of marker id to marker object
					while (markers.hasNext()) {
						JSONObject thisMarkerObj = thisGroup
								.getJSONObject((String) markers.next());

						ConvexHullMarker thisMarker = new ConvexHullMarker(
								fragment, groupID);

						double lat = thisMarkerObj.getDouble("lat");
						double lng = thisMarkerObj.getDouble("lng");
						Long marker_id = thisMarkerObj.getLong("marker_id");
						thisMarker.setPos(new LatLng(lat, lng));
						thisMarker.setNiceName(thisMarkerObj
								.getString("nicename"));

						groupList.put(Long.toString(marker_id), thisMarker);
					}

					// Add the group to the overal map of groups
					convexMarkerLists.put(groupID, groupList);
				}
			}
		} catch (Exception e) {
			Log.e("Convex Handler", "Failed to parse Json object");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private HttpResponse deleteGroup() {

		String android_id = Secure.getString(thisContext.getContentResolver(),
				Secure.ANDROID_ID);

		SharedPreferences auth = thisContext.getSharedPreferences(
				thisContext.getString(R.string.authentication), 0);
		String authToken = auth.getString("authToken", "");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", android_id));
		pairs.add(new BasicNameValuePair("auth_token", authToken));
		pairs.add(new BasicNameValuePair("group_id", tappedMarker.getGroupID()));

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
			Log.e("Convex Hull Handler", "Failed to parse Json object");
			e.printStackTrace();
			return false;
		}
	}

	private String formatURL() {
		String URL = new String();
		switch (action) {
		case SAVE_ACTION:
			URL = thisContext.getString(R.string.server_root)
					+ thisContext.getString(R.string.convex_save_url);
			break;
		case DELETE_ACTION:
			URL = thisContext.getString(R.string.server_root)
					+ thisContext.getString(R.string.convex_delete_url);
			break;
		case LOAD_ACTION:
			URL = thisContext.getString(R.string.server_root)
					+ thisContext.getString(R.string.convex_load_url);
			break;
		}

		return URL;
	}

	@Override
	protected void onPreExecute() {
		if ((action == (SAVE_ACTION) || (action == DELETE_ACTION))) {
			tappedMarker.getDialog().findViewById(R.id.textView10)
					.setVisibility(View.INVISIBLE);
			tappedMarker.getDialog().findViewById(R.id.button1)
					.setVisibility(View.INVISIBLE);
			tappedMarker.getDialog().findViewById(R.id.button2)
					.setVisibility(View.INVISIBLE);
			tappedMarker.getDialog().findViewById(R.id.progressBar1)
					.setVisibility(View.VISIBLE);
			tappedMarker.getDialog().findViewById(R.id.textViewErr)
					.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected String doInBackground(String... params) {
		switch (action) {
		case SAVE_ACTION: {
			if(singleGroup.size() < 4){
				return ACTION_FAILED;
			}
			HttpResponse response = saveGroup();
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
			HttpResponse response = deleteGroup();
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
			tappedMarker.getDialog().findViewById(R.id.button1)
					.setVisibility(View.VISIBLE);
			tappedMarker.getDialog().findViewById(R.id.button2)
					.setVisibility(View.VISIBLE);
			tappedMarker.getDialog().findViewById(R.id.progressBar1)
					.setVisibility(View.INVISIBLE);

			if (result.compareTo(ACTION_FAILED) == 0) {
				tappedMarker.getDialog().findViewById(R.id.textViewErr)
						.setVisibility(View.VISIBLE);
				TextView err = (TextView) tappedMarker.getDialog()
						.findViewById(R.id.textViewErr);
				err.setTextColor(Color.RED);
				err.setText("Operation failed - error connecting to server, or server error");
			} else {
				tappedMarker.getDialog().findViewById(R.id.textViewErr)
						.setVisibility(View.VISIBLE);
				TextView err = (TextView) tappedMarker.getDialog()
						.findViewById(R.id.textViewErr);
				err.setTextColor(Color.GREEN);
				err.setText("Operation Successful");
			}
		}

		if ((action == SAVE_ACTION) && result.compareTo(ACTION_SUCCESS) == 0) {
			// Update the fragment
			Log.i("convexhull", "$$$$$$$$$$$$$$$");

			for (Entry<String, ConvexHullMarker> entry : singleGroup.entrySet()) {
				ConvexHullMarker instanceMarker = entry.getValue();
				
				//This has now been saved so disable dragging
				instanceMarker.getMarker().setDraggable(false);
				fragment.convexMarkerIds.put(returnedGroup_id, instanceMarker);
			}
			
			fragment.convexMarkerLists.put(returnedGroup_id, singleGroup);

			//Remove the old ones as this has been updated
			fragment.convexMarkerLists.remove("-1");
			
			if(fragment.convexLines.containsKey("-1")){
				Polyline deadPoly = fragment.convexLines.get("-1");
				deadPoly.remove();
				
				fragment.convexLines.remove("-1");
			}
			
			
			//Disable freeform drawing, re-enable the button
			fragment.tapToAdd = false;
			fragment.activity.onConvexSave();
		}

		if ((action == DELETE_ACTION) && result.compareTo(ACTION_SUCCESS) == 0) {

			// Get the group id for the tapped marker, close its dialog
			String group_id = tappedMarker.getGroupID();
			tappedMarker.getDialog().cancel();

			// Remove the associated polys
			Polyline groupPoly = fragment.convexLines.get(group_id);
			groupPoly.remove();
			fragment.convexLines.remove(group_id);
			
			// Loop over and remove the markers from the map
			HashMap<String, ConvexHullMarker> groupMarkers = fragment.convexMarkerLists
					.get(group_id);
			for (Entry<String, ConvexHullMarker> entry : groupMarkers
					.entrySet()) {
				ConvexHullMarker thisMarker = entry.getValue();
				Marker mapMarker = thisMarker.getMarker();
				mapMarker.remove();

				// Also remove the marker from the index
				fragment.convexMarkerIds.remove(mapMarker.getId());
			}

			// Remove the marker from the hashmap
			fragment.convexMarkerLists.remove(group_id);
			
			//Disable freeform drawing, re-enable the button
			fragment.tapToAdd = false;
			fragment.activity.onConvexSave();
		}

		if (action == LOAD_ACTION) {
			addMarkers();
		}
	}

	private void addMarkers() {

		HashMap<String, HashMap<String, ConvexHullMarker>> updatedLists = new HashMap<String, HashMap<String, ConvexHullMarker>>();

		for (Entry<String, HashMap<String, ConvexHullMarker>> group : convexMarkerLists
				.entrySet()) {
			HashMap<String, ConvexHullMarker> thisGroup = group.getValue();

			if (thisGroup.size() > 3) {
				String groupID = group.getKey();

				for (Entry<String, ConvexHullMarker> marker : thisGroup
						.entrySet()) {
					ConvexHullMarker thisMarker = marker.getValue();
					thisMarker.setGroupID(groupID);

					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position(thisMarker.getPos());
					markerOptions.title("Added by tap");
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.convexmarker));

					// Set the relative position on the marker to the map so the
					// map location related to the base of the flag
					markerOptions.anchor((float) 0.15, (float) 0.95);

					Marker mapMarker = fragment.map.addMarker(markerOptions);
					thisMarker.setMarker(mapMarker);

					Dialog dialog = new Dialog(fragment.getActivity());
					dialog.setContentView(R.layout.convexdialog);
					dialog.setTitle("Freeform Geofence Properties");
					Button b = (Button) dialog.findViewById(R.id.button1);
					b.setOnClickListener(thisMarker.getSaveButton(dialog));
					Button b2 = (Button) dialog.findViewById(R.id.button2);
					b2.setOnClickListener(thisMarker.getDeleteButton(dialog));
					thisMarker.setDialog(dialog);
					EditText name = (EditText) dialog
							.findViewById(R.id.nicename);
					name.setText(thisMarker.getNiceName());

					thisMarker.setProject(fragment.projection);
					thisMarker.refreshPoint();

					// Update the google map index
					fragment.convexMarkerIds.put(mapMarker.getId(), thisMarker);
				}

				// Draw the unsorted hull
				ConvexHull worker = new ConvexHull(fragment);
				ArrayList<ConvexHullMarker> convexHull = worker
						.computeDCHull((new ArrayList<ConvexHullMarker>(
								thisGroup.values())));

				worker.drawHull(convexHull, true, groupID);

				updatedLists.put(groupID, thisGroup);
			}
		}

		// Update the fragment
		fragment.convexMarkerLists = updatedLists;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public MapSectionFragment getFragment() {
		return fragment;
	}

	public void setFragment(MapSectionFragment fragment) {
		this.fragment = fragment;
	}

	public void setSingleGroup(HashMap<String, ConvexHullMarker> singleGroup) {
		this.singleGroup = singleGroup;
	}

	public void setTappedMarker(ConvexHullMarker marker) {
		this.tappedMarker = marker;
	}

}
