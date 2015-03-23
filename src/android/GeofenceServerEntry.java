package com.cowbell.cordova.geofence;

import android.util.Log;

import com.google.gson.Gson;

public class GeofenceServerEntry {

	private static String TAG = "GeofenceServerEntry";

	public class Location {
		public double lon;
		public double lat;
	}

    public String _id;
    public String name;
    public Location location;
    public int radius;

    private GeoNotification n;

    public GeofenceServerEntry() {
    }

    public GeoNotification generateGeoNotification(int index) {
    	n = new GeoNotification();
    	n.id = this._id;
    	n.latitude = this.location.lat;
    	n.longitude = this.location.lon;
    	n.radius = this.radius;
    	n.transitionType = 3;
    	n.notification = new Notification();
    	n.notification.id = index;
    	n.notification.title = "MexWave";
    	n.notification.text = "Checkin at " + name;
    	n.notification.openAppOnClick = true;

    	String notificationDataJson = "{" +
							              "\"id\":\"" + this._id + "\", " +
							              "\"name\": \""+ this.name + "\"" +
							          "}";
    	Log.d(TAG, notificationDataJson);
    	n.notification.data = new Gson().fromJson(notificationDataJson, Object.class);

    	return n;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static GeofenceServerEntry fromJson(String json) {
        if (json == null)
            return null;
        return new Gson().fromJson(json, GeofenceServerEntry.class);
    }
}
