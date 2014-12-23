package com.reading.trackitparenttabbed;

public class ChildDeviceDetail {
	private String device_id;
	private Double altitude;
	private double battery;
	private boolean is_Charging;
	private double bearing;
	private String data_connection;
	private double velocity;
	private double longitude;
	private double latitude;
	private String location_source;
	private double accuracy;
	private String network;
	private String make;
	private String model;

	public ChildDeviceDetail(String device_id, double altitude, double battery,
			boolean is_Charging, double bearing, String data_connection,
			double velocity, double longitude, double latitude,
			String location_source, double accuracy, String network, String make, String model) {
		
		this.device_id = device_id;
		this.altitude = altitude;
		this.battery = battery;
		this.is_Charging = is_Charging;
		this.bearing = bearing;
		this.data_connection = data_connection;
		this.velocity = velocity;
		this.longitude = longitude;
		this.latitude = latitude;
		this.location_source = location_source;
		this.accuracy = accuracy;
		this.network = network;
		this.make = make;
		this.model = model;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public double getBattery() {
		return battery;
	}

	public void setBattery(double battery) {
		this.battery = battery;
	}

	public boolean isIs_Charging() {
		return is_Charging;
	}

	public void setIs_Charging(boolean is_Charging) {
		this.is_Charging = is_Charging;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public String getData_connection() {
		return data_connection;
	}

	public void setData_connection(String data_connection) {
		this.data_connection = data_connection;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getLocation_source() {
		return location_source;
	}

	public void setLocation_source(String location_source) {
		this.location_source = location_source;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}
	
	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

}
