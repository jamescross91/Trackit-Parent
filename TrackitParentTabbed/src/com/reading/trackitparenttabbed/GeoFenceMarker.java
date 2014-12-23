package com.reading.trackitparenttabbed;

import android.app.Dialog;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public abstract class GeoFenceMarker{

	protected Marker marker;
	protected LatLng pos;
	private Button saveButton;
	private Button deleteButton;
	private Dialog dialog;
	private long marker_id = NEVER_SAVED;
	protected MapSectionFragment thisFragment;

	public static final int NEVER_SAVED = -1;
	
	public GeoFenceMarker(Marker marker, MapSectionFragment thisFragment){
		this.setMarker(marker);
		this.thisFragment = thisFragment;
	}
	
	public GeoFenceMarker(MapSectionFragment thisFragment){
		this.thisFragment = thisFragment;
	}
	
	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public Button getSaveButton() {
		return saveButton;
	}

	public void setSaveButton(Button saveButton) {
		this.saveButton = saveButton;
	}

	public Button getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(Button deleteButton) {
		this.deleteButton = deleteButton;
	}

	public long getMarker_id() {
		return marker_id;
	}

	public void setMarker_id(long marker_id) {
		this.marker_id = marker_id;
	}

	public Dialog getDialog() {
		return dialog;
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	public MapSectionFragment getThisFragment() {
		return thisFragment;
	}

	public void setThisFragment(MapSectionFragment thisFragment) {
		this.thisFragment = thisFragment;
	}
	
	public LatLng getPos() {
		return pos;
	}

	public void setPos(LatLng pos) {
		this.pos = pos;
	}
}
