package com.reading.trackitparenttabbed;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

public class MapSectionFragment extends SupportMapFragment {

	public GoogleMap map;

	private boolean loaded = false;
	private MapSectionFragment thisFrag = this;
	public boolean tapToAdd = false;
	public String activeGroupId = "-1";
	protected MapSectionFragment thisFragment = this;
	protected Projection projection;

	// Google map marker ID to custom marker object
	public HashMap<String, RadialGeoFenceMarker> markers = new HashMap<String, RadialGeoFenceMarker>();
	// Google Map Marker ID to Marker for fast lookup
	public HashMap<String, Marker> radialMarkerIds = new HashMap<String, Marker>();

	// Geofence ID to map containing: Google map marker ID to custom marker
	// object
	public HashMap<String, HashMap<String, ConvexHullMarker>> convexMarkerLists = new HashMap<String, HashMap<String, ConvexHullMarker>>();
	// Google Map Marker ID to Marker for fast lookup
	public HashMap<String, ConvexHullMarker> convexMarkerIds = new HashMap<String, ConvexHullMarker>();
	// Group id to associated polyline
	public HashMap<String, Polyline> convexLines = new HashMap<String, Polyline>();

	// Device ID to Marker
	public HashMap<String, Marker> locMarkers = new HashMap<String, Marker>();
	// Google Map Marker ID to Marker for fast lookup
	public HashMap<String, Marker> locMarkerIds = new HashMap<String, Marker>();
	
	public TabbedActivity activity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity()
				.registerReceiver(receiver, new IntentFilter("TrackiTLoc"));

		getActivity().registerReceiver(receiver,
				new IntentFilter("TrackiTMarker"));
		
		activity = (TabbedActivity) getActivity();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = super.onCreateView(inflater, container, savedInstanceState);

		FrameLayout frameLayout = new FrameLayout(getActivity());
		frameLayout.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		((ViewGroup) view).addView(frameLayout, new ViewGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		map = getMap();
		map.setOnMarkerClickListener(getMarkerClickListener());
		map.setOnMarkerDragListener(getDragListener());
		map.setOnCameraChangeListener(cameraListener);
		map.setMyLocationEnabled(true);

		projection = map.getProjection();

		if (!loaded) {

			// Load Radial and Convex Geofences from the database
			RadialGeofenceHandler handler1 = new RadialGeofenceHandler(
					getActivity());
			handler1.setAction(RadialGeofenceHandler.LOAD_ACTION);
			handler1.setFragment(this);
			handler1.execute(new String());

			ConvexHullHandler handler2 = new ConvexHullHandler(getActivity());
			handler2.setAction(ConvexHullHandler.LOAD_ACTION);
			handler2.setFragment(this);
			handler2.execute(new String());

			loaded = true;
		}

		// The map just loaded, instead of waiting for an update to be pushed
		// out request the latest device location from the server
		LocationRequester requester = new LocationRequester(getActivity());
		requester.execute(new String());

		map.setOnMapClickListener(getMapClickListener());

		return view;
	}

	public OnCameraChangeListener cameraListener = new OnCameraChangeListener() {

		@Override
		public void onCameraChange(CameraPosition arg0) {

			// On map drag/pan/zoom recompute the convex hulls with updated
			// projections
			for (Entry<String, HashMap<String, ConvexHullMarker>> entry : convexMarkerLists
					.entrySet()) {

				HashMap<String, ConvexHullMarker> thisGroup = entry.getValue();
				String group_id = entry.getKey();

				// Get the list of markers comprising the convex hull
				ArrayList<ConvexHullMarker> markerList = new ArrayList<ConvexHullMarker>(
						thisGroup.values());
				ConvexHull convexHullWorker = new ConvexHull(thisFrag);

				ArrayList<ConvexHullMarker> convexHull = new ArrayList<ConvexHullMarker>();

				// Otherwise those 3 points are the convex hull :-)
				if (markerList.size() > 3) {

					// Refresh each point based on the current map
					// projection
					for (int i = 0; i < markerList.size(); i++) {
						ConvexHullMarker marker = markerList.get(i);
						marker.refreshPoint();
					}

					// Compute the convex hull
					convexHull = convexHullWorker.computeDCHull(markerList);

					// Draw the pre-sorted hull
					convexHullWorker.drawHull(convexHull, true, group_id);
				}
			}
		}
	};

	public OnMapClickListener getMapClickListener() {
		OnMapClickListener listener = new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng pos) {
				if (tapToAdd) {
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position(pos);
					markerOptions.title("Added by tap");
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.convexmarker));
					markerOptions.draggable(true);

					// Set the relative position on the marker to the map so the
					// map location related to the base of the flag
					markerOptions.anchor((float) 0.15, (float) 0.95);

					// Get a projection for the map, this allows us to convert
					// points from geodesic to cartesian space
					projection = map.getProjection();

					// Add a new marker to the google map, and create a new
					// custom convex hull marker
					Marker thisMarker = map.addMarker(markerOptions);
					ConvexHullMarker convexMarker = new ConvexHullMarker(
							thisMarker, thisFragment, projection, activeGroupId);

					// Add the on click dialog and buttons
					final Dialog dialog = new Dialog(getActivity());
					dialog.setContentView(R.layout.convexdialog);
					dialog.setTitle("Group Properties");
					Button b = (Button) dialog.findViewById(R.id.button1);
					b.setOnClickListener(convexMarker.getSaveButton(dialog));
					Button b2 = (Button) dialog.findViewById(R.id.button2);
					b2.setOnClickListener(convexMarker.getDeleteButton(dialog));
					convexMarker.setDialog(dialog);

					HashMap<String, ConvexHullMarker> thisGroup = new HashMap<String, ConvexHullMarker>();

					// Get me the list of markers for this convex group
					if (convexMarkerLists.containsKey(activeGroupId)) {
						thisGroup = convexMarkerLists.get(activeGroupId);
					}

					// Add this marker to the group
					thisGroup.put(thisMarker.getId(), convexMarker);

					// Add the marker ID map for fast lookup in the on marker
					// click listener
					convexMarkerIds.put(thisMarker.getId(), convexMarker);

					// Get the list of markers comprising the convex hull
					ArrayList<ConvexHullMarker> markerList = new ArrayList<ConvexHullMarker>(
							thisGroup.values());
					ConvexHull convexHullWorker = new ConvexHull(thisFrag);

					ArrayList<ConvexHullMarker> convexHull = new ArrayList<ConvexHullMarker>();

					// Otherwise those 3 points are the convex hull :-)
					if (markerList.size() > 3) {

						// Refresh each point based on the current map
						// projection
						for (int i = 0; i < markerList.size(); i++) {
							ConvexHullMarker marker = markerList.get(i);
							marker.refreshPoint();
						}

						// Compute the convex hull
						convexHull = convexHullWorker.computeDCHull(markerList);

						// Draw the pre-sorted hull
						convexHullWorker.drawHull(convexHull, true,
								activeGroupId);
					}

					// Then add the group back into the main list
					convexMarkerLists.put(activeGroupId, thisGroup);
				}
			}
		};

		return listener;
	}

	public void addMarker() {
		LatLng startPos = map.getCameraPosition().target;
		Marker marker = map.addMarker(new MarkerOptions().position(startPos)
				.title("Geofence").snippet("snippet").draggable(true));

		RadialGeoFenceMarker geoMarker = new RadialGeoFenceMarker(marker, this);
		geoMarker.setMarker(marker);
		geoMarker.setPos(startPos);

		geoMarker.setPoly(map.addPolyline(geoMarker
				.getOptions(startPos, (double) geoMarker.getSliderProgress())
				.color(Color.RED).width(2)));

		final Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(R.layout.geodialog);
		dialog.setTitle("Geofence Properties");
		Button b = (Button) dialog.findViewById(R.id.button1);
		b.setOnClickListener(geoMarker.getSaveButton(dialog, geoMarker));
		Button b2 = (Button) dialog.findViewById(R.id.button2);
		b2.setOnClickListener(geoMarker.getDeleteButton(dialog, geoMarker));
		geoMarker.setDialog(dialog);

		map.setOnMarkerClickListener(getMarkerClickListener());
		map.setOnMarkerDragListener(getDragListener());

		markers.put(marker.getId(), geoMarker);
	}

	public OnMarkerClickListener getMarkerClickListener() {
		OnMarkerClickListener listener = new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				if (markers.containsKey(marker.getId())) {

					final RadialGeoFenceMarker geoMarker = markers.get(marker
							.getId());
					Dialog dialog = geoMarker.getDialog();
					dialog.show();
					final TextView value = (TextView) dialog
							.findViewById(R.id.textView10);
					SeekBar seekbar = (SeekBar) dialog
							.findViewById(R.id.geoSeekBar);
					seekbar.setProgress((int) geoMarker.getSliderProgress() / 10);
					value.setText(String.valueOf(geoMarker.getSliderProgress()
							+ "m"));

					seekbar.setOnSeekBarChangeListener(geoMarker
							.getSeekBarChangedListener(value));

					return true;
				}
				if (convexMarkerIds.containsKey(marker.getId())) {
					// Callback for convex markers
					ConvexHullMarker thisMarker = convexMarkerIds.get(marker
							.getId());
					Dialog dialog = thisMarker.getDialog();
					EditText text = (EditText) dialog
							.findViewById(R.id.nicename);
					text.setText(thisMarker.getNiceName());
					dialog.show();
					return true;
				}
				return false;
			}

		};

		return listener;
	}

	public OnMarkerDragListener getDragListener() {
		OnMarkerDragListener listener = new OnMarkerDragListener() {

			@Override
			public void onMarkerDrag(Marker marker) {
				if (markers.containsKey(marker.getId())) {
					RadialGeoFenceMarker geoMarker = markers
							.get(marker.getId());
					geoMarker.setPos(marker.getPosition());
					Polyline poly = geoMarker.getPoly();
					poly.remove();
					geoMarker.setPoly(map.addPolyline(geoMarker
							.getOptions(geoMarker.getPos(),
									(double) geoMarker.getSliderProgress())
							.color(Color.RED).width(2)));
				}
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				if (markers.containsKey(marker.getId())) {
					RadialGeoFenceMarker geoMarker = markers
							.get(marker.getId());
					geoMarker.setPos(marker.getPosition());
					Polyline poly = geoMarker.getPoly();
					poly.remove();
					geoMarker.setPoly(map.addPolyline(geoMarker
							.getOptions(geoMarker.getPos(),
									(double) geoMarker.getSliderProgress())
							.color(Color.RED).width(2)));
				}

				if (convexMarkerIds.containsKey(marker.getId())) {

					ConvexHullMarker thisMarker = convexMarkerIds.get(marker
							.getId());
					
					convexMarkerIds.remove(marker.getId());
					
					//Remove the existing polyline
					Polyline poly = convexLines.get(thisMarker.getGroupID());
					poly.remove();
					
					thisMarker.setMarker(marker);
					thisMarker.setPos(marker.getPosition());

					String group_id = thisMarker.getGroupID();
					
					HashMap<String, ConvexHullMarker> thisGroup = convexMarkerLists
							.get(group_id);

					// Get the list of markers comprising the convex hull
					ArrayList<ConvexHullMarker> markerList = new ArrayList<ConvexHullMarker>(
							thisGroup.values());
					ConvexHull convexHullWorker = new ConvexHull(thisFrag);

					ArrayList<ConvexHullMarker> convexHull = new ArrayList<ConvexHullMarker>();

					// Otherwise those 3 points are the convex hull :-)
					if (markerList.size() > 3) {

						// Refresh each point based on the current map
						// projection
						for (int i = 0; i < markerList.size(); i++) {
							ConvexHullMarker refreshMarker = markerList.get(i);
							refreshMarker.refreshPoint();
						}

						// Compute the convex hull
						convexHull = convexHullWorker.computeDCHull(markerList);

						// Draw the pre-sorted hull
						convexHullWorker.drawHull(convexHull, true, group_id);
					}
					
					convexMarkerLists.put(activeGroupId, thisGroup);
					convexMarkerIds.put(marker.getId(), thisMarker);
				}
			}

			@Override
			public void onMarkerDragStart(Marker arg0) {
			}
		};

		return listener;
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().compareTo(
					context.getString(R.string.loc_update_broadcast_action)) == 0) {
				Bundle b = intent.getExtras();
				String message = (String) b.get("Loc");

				try {
					JSONObject json = new JSONObject(message);
					double longitude = json.getDouble("longitude");
					double latitude = json.getDouble("latitude");
					String device_id = json.getString("device_id");
					String model = json.getString("device_model");

					Date d = new Date();
					CharSequence s = DateFormat.format(
							"kk:mm on EEEE, MMMM d, yyyy ", d.getTime());

					Marker posMarker = locMarkers.get(device_id);

					if (posMarker != null)
						posMarker.remove();

					posMarker = map.addMarker(new MarkerOptions()
							.position(new LatLng(latitude, longitude))
							.title(model)
							.snippet("As of " + s)
							.draggable(false)
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.htc_evo_4g)));

					locMarkers.put(device_id, posMarker);
					locMarkerIds.put(posMarker.getId(), posMarker);

					map.moveCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(latitude, longitude), 14));

					// Makes the marker bounce bounce bounce when an update
					// comes in!

					final Handler handler = new Handler();
					final long startTime = SystemClock.uptimeMillis();
					final long duration = 2000;

					Projection proj = map.getProjection();
					final LatLng markerLatLng = posMarker.getPosition();
					Point startPoint = proj.toScreenLocation(markerLatLng);
					startPoint.offset(0, -100);
					final LatLng startLatLng = proj
							.fromScreenLocation(startPoint);

					final Interpolator interpolator = new BounceInterpolator();
					final Marker bounceMarker = posMarker;
					handler.post(new Runnable() {
						@Override
						public void run() {
							long elapsed = SystemClock.uptimeMillis()
									- startTime;
							float t = interpolator
									.getInterpolation((float) elapsed
											/ duration);
							double lng = t * markerLatLng.longitude + (1 - t)
									* startLatLng.longitude;
							double lat = t * markerLatLng.latitude + (1 - t)
									* startLatLng.latitude;
							bounceMarker.setPosition(new LatLng(lat, lng));

							if (t < 1.0) {
								// Post again 16ms later.
								handler.postDelayed(this, 16);
							}
						}
					});

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction().compareTo(
					context.getString(R.string.marker_update_broadcast_action)) == 0) {

				Bundle b = intent.getExtras();
				String message = (String) b.get("Marker");

				for (Entry<String, RadialGeoFenceMarker> entry : markers
						.entrySet()) {
					RadialGeoFenceMarker marker = entry.getValue();
					marker.getMarker().remove();
					marker.getPoly().remove();
					markers.remove(message);
				}

				RadialGeofenceHandler handler1 = new RadialGeofenceHandler(
						getActivity());
				handler1.setAction(RadialGeofenceHandler.LOAD_ACTION);
				handler1.setFragment(thisFrag);
				handler1.execute(new String());
			}
		}
	};

}
