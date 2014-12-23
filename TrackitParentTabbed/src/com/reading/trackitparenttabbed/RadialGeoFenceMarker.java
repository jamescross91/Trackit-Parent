package com.reading.trackitparenttabbed;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class RadialGeoFenceMarker extends GeoFenceMarker{

	private TextView value;
	private SeekBar seekbar;
	private Polyline poly;
	private double sliderProgress = 0.05;
	private ProgressBar progBar;

	public static final int NEVER_SAVED = -1;
	
	public RadialGeoFenceMarker(Marker marker, MapSectionFragment thisFragment){
		super(marker, thisFragment);
	}
	
	public RadialGeoFenceMarker(MapSectionFragment thisFragment){
		super(thisFragment);
	}
	
	public PolylineOptions getOptions(LatLng pos, double rad) {
		double Rad = 6371000d; // earth's mean radius in m
		double d = rad / Rad; // radius given in km
		double lat1 = Math.toRadians(pos.latitude);
		double lon1 = Math.toRadians(pos.longitude);

		PolylineOptions options = new PolylineOptions();
		for (int x = 0; x <= 360; x = x + 4) {
			double brng = Math.toRadians(x);
			double latitudeRad = Math.asin(Math.sin(lat1) * Math.cos(d)
					+ Math.cos(lat1) * Math.sin(d) * Math.cos(brng));
			double longitudeRad = (lon1 + Math.atan2(
					Math.sin(brng) * Math.sin(d) * Math.cos(lat1), Math.cos(d)
							- Math.sin(lat1) * Math.sin(latitudeRad)));
			options.add(new LatLng(Math.toDegrees(latitudeRad), Math
					.toDegrees(longitudeRad)));
		}

		return options;
	}

	public OnClickListener getSaveButton(final Dialog d,
			final RadialGeoFenceMarker geoMarker) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				RadialGeofenceHandler handler = new RadialGeofenceHandler(
						thisFragment.getActivity());
				handler.setOneMarker(geoMarker);
				handler.setAction(RadialGeofenceHandler.SAVE_ACTION);
				handler.execute(new String());
			}
		};
		return listener;
	}

	public OnClickListener getDeleteButton(final Dialog d,
			final RadialGeoFenceMarker geoMarker) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				RadialGeofenceHandler handler = new RadialGeofenceHandler(
						thisFragment.getActivity());
				handler.setOneMarker(geoMarker);
				handler.setAction(RadialGeofenceHandler.DELETE_ACTION);
				handler.execute(new String());
			}
		};
		return listener;
	}

	public OnSeekBarChangeListener getSeekBarChangedListener(
			final TextView value) {
		OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				sliderProgress = ((double) progress * 10);
				value.setText(String.valueOf(sliderProgress)
						+ "m");

				poly.remove();
				PolylineOptions opts = getOptions(pos, sliderProgress).color(Color.RED).width(2);
				poly = thisFragment.map.addPolyline(opts);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		};

		return listener;
	}

	public ProgressBar getProgBar() {
		return progBar;
	}

	public void setProgBar(ProgressBar progBar) {
		this.progBar = progBar;
	}

	public SeekBar getSeekbar() {
		return seekbar;
	}

	public void setSeekbar(SeekBar seekbar) {
		this.seekbar = seekbar;
	}

	public TextView getValue() {
		return value;
	}

	public void setValue(TextView value) {
		this.value = value;
	}

	public Polyline getPoly() {
		return poly;
	}

	public void setPoly(Polyline poly) {
		this.poly = poly;
	}

	public double getSliderProgress() {
		return sliderProgress;
	}

	public void setSliderProgress(double sliderProgress) {
		this.sliderProgress = sliderProgress;
	}
}
