package com.reading.trackitparenttabbed;

import java.util.Map.Entry;

import android.app.Dialog;
import android.graphics.Point;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.Marker;

public class ConvexHullMarker extends GeoFenceMarker implements
		Comparable<ConvexHullMarker>, Cloneable {

	private Point cartesianPoint;
	private Projection mapProjection;
	private String groupID;
	private ConvexHullMarker thisMarker;
	private String niceName;

	private static long marker_id_counter = -1;

	public ConvexHullMarker(Marker marker, MapSectionFragment thisFragment,
			Projection mapProjection, String groupID) {
		super(marker, thisFragment);

		// Give me a cartesian point for this latitude and longitude
		this.mapProjection = mapProjection;
		cartesianPoint = this.mapProjection.toScreenLocation(marker
				.getPosition());

		this.groupID = groupID;
		thisMarker = this;
		pos = marker.getPosition();
		ConvexHullMarker.marker_id_counter--;
		this.setMarker_id(ConvexHullMarker.marker_id_counter);
	}
	
	public String toString() {
		return "Coord("+this.marker.getPosition().latitude+","+this.marker.getPosition().longitude+")" + " Cart("+cartesianPoint.x+","+cartesianPoint.y+")";
	}

	public ConvexHullMarker(MapSectionFragment thisFragment,
			Projection mapProjection, String groupID) {
		super(thisFragment);

		// Give me a cartesian point for this latitude and longitude
		this.mapProjection = mapProjection;
		cartesianPoint = this.mapProjection.toScreenLocation(marker
				.getPosition());

		this.groupID = groupID;
		thisMarker = this;
	}

	public ConvexHullMarker(MapSectionFragment thisFragment, String groupID) {
		super(thisFragment);
		this.groupID = groupID;
		thisMarker = this;
	}

	public void refreshPoint() {
		this.mapProjection = thisFragment.map.getProjection();
		cartesianPoint = mapProjection.toScreenLocation(marker.getPosition());
	}

	public OnClickListener getSaveButton(final Dialog d) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConvexHullHandler handler = new ConvexHullHandler(
						thisFragment.getActivity());

				EditText textBox = (EditText) getDialog().findViewById(
						R.id.nicename);
				String niceName = textBox.getText().toString();

				// Copy nice name to all markers
				for (Entry<String, ConvexHullMarker> entry : thisFragment.convexMarkerLists
						.get(groupID).entrySet()) {
					ConvexHullMarker thisMarker = entry.getValue();
					thisMarker.setNiceName(niceName);
				}

				handler.setSingleGroup(thisFragment.convexMarkerLists
						.get(groupID));
				handler.setTappedMarker(thisMarker);
				handler.setAction(ConvexHullHandler.SAVE_ACTION);
				handler.setFragment(thisFragment);
				handler.execute(new String());
			}
		};
		return listener;
	}

	public OnClickListener getDeleteButton(final Dialog d) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				 ConvexHullHandler handler = new ConvexHullHandler(
				 thisFragment.getActivity());
				 handler.setTappedMarker(thisMarker);
				 handler.setAction(ConvexHullHandler.DELETE_ACTION);
				 handler.setFragment(thisFragment);
				 handler.execute(new String());
			}
		};
		return listener;
	}

	public ConvexHullMarker(MapSectionFragment thisFragment) {
		super(thisFragment);
	}

	public int getCartesianX() {
		return cartesianPoint.x;
	}

	public int getCartesianY() {
		return cartesianPoint.y;
	}

	// Sort on the x-coordinate of the cartesian point representation
	public int compareTo(ConvexHullMarker testPoint) {
		return this.cartesianPoint.x - testPoint.getCartesianX();
	}

	public ConvexHullMarker clone() {
		try {
			return (ConvexHullMarker) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getNiceName() {
		return niceName;
	}

	public void setNiceName(String niceName) {
		this.niceName = niceName;
	}
	
	public void setProject(Projection projection){
		this.mapProjection = projection;
	}
}
