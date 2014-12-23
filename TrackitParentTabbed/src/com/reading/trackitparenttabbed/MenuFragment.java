package com.reading.trackitparenttabbed;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MenuFragment extends ListFragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	private ArrayList<String> values = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private HashMap<String, ChildDeviceDetail> details = new HashMap<String, ChildDeviceDetail>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity()
				.registerReceiver(receiver, new IntentFilter("TrackiTLoc"));

		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, android.R.id.text1, values);
		
		setListAdapter(adapter);

		getActivity()
				.registerReceiver(receiver, new IntentFilter("TrackiTLoc"));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String combined_id = adapter.getItem(position);
		String device_id = combined_id.split(":")[1];
		ChildDeviceDetail thisChild = details.get(device_id);

		Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(R.layout.detaildialog);
		dialog.setTitle(thisChild.getModel());

		TextView lat = (TextView) dialog.findViewById(R.id.textView6);
		lat.setText(String.valueOf(thisChild.getLatitude()));

		TextView lng = (TextView) dialog.findViewById(R.id.textView7);
		lng.setText(String.valueOf(thisChild.getLongitude()));

		TextView alt = (TextView) dialog.findViewById(R.id.textView8);
		alt.setText(String.valueOf(thisChild.getAltitude()));

		TextView bear = (TextView) dialog.findViewById(R.id.textView9);
		bear.setText(String.valueOf(thisChild.getBearing()));

		TextView vel = (TextView) dialog.findViewById(R.id.textView11);
		vel.setText(String.valueOf(thisChild.getVelocity()));

		TextView src = (TextView) dialog.findViewById(R.id.textView15);
		src.setText(String.valueOf(thisChild.getLocation_source()));

		TextView acc = (TextView) dialog.findViewById(R.id.textView16);
		acc.setText(String.valueOf(thisChild.getAccuracy()));

		TextView bat = (TextView) dialog.findViewById(R.id.textView17);
		bat.setText(String.valueOf(thisChild.getBattery()));

		TextView conn = (TextView) dialog.findViewById(R.id.textView18);
		conn.setText(String.valueOf(thisChild.getData_connection()));

		TextView cell = (TextView) dialog.findViewById(R.id.textView19);
		cell.setText(String.valueOf(thisChild.getNetwork()));

		dialog.show();
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
					String device_id = json.getString("device_id");
					Double altitude = json.getDouble("altitude");
					double battery = json.getDouble("battery");
					boolean is_Charging = json.getBoolean("is_charging");
					double bearing = json.getDouble("bearing");
					String data_connection = json.getString("data_connection");
					double velocity = json.getDouble("velocity");
					double longitude = json.getDouble("longitude");
					double latitude = json.getDouble("latitude");
					String location_source = json.getString("location_source");
					double accuracy = json.getDouble("accuracy");
					String network = json.getString("network");
					String make = json.getString("device_make");
					String model = json.getString("device_model");
					
					String id = getId(device_id, model);

					if (!existsInList(id)) {
						adapter.add(id);
						
						ChildDeviceDetail thisChild = new ChildDeviceDetail(
								device_id, altitude, battery, is_Charging,
								bearing, data_connection, velocity, longitude,
								latitude, location_source, accuracy, network, make, model);

						// If it exists, update the details
						if (details.containsKey(device_id)) {
							details.remove(device_id);
						}
						details.put(device_id, thisChild);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private String getId(String device_id, String model){
		return model + "				:" + device_id;
	}
	
	private boolean existsInList(String device_id) {
		for (int i = 0; i < values.size(); i++) {
			String id = values.get(i);
			if (id.compareTo(device_id) == 0) {
				return true;
			}
		}

		return false;
	}

}