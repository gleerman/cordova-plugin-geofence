package com.cowbell.cordova.geofence;

public class GeofenceSettings {
	
	private static GeofenceSettings instance;
	
	public static GeofenceSettings getInstance() {
		if (instance == null) instance = new GeofenceSettings();
		return instance;
	}

	private boolean pushNotifications;
	private boolean tracking;
	
	private GeofenceSettings() {
		pushNotifications = true;
		tracking = true;
	}

	public boolean getPushNotifications() {
		return pushNotifications;
	}

	public void setPushNotifications(boolean pushNotifications) {
		this.pushNotifications = pushNotifications;
	}

	public boolean getTracking() {
		return tracking;
	}

	public void setTracking(boolean tracking) {
		this.tracking = tracking;
	}
}
