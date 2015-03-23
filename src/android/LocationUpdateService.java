package com.cowbell.cordova.geofence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public abstract class LocationUpdateService extends Service implements LocationListener {
	private static final String TAG = "LocationUpdateService";

	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	boolean canGetLocation = false;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000; // 1 km

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 10; // 1 minutes

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public LocationUpdateService(Context context) {
		this.mContext = context;
	}

	public void startMonitoring() {
		try {
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				Location location = null; // location

				// First get location from Network Provider
				if (isNetworkEnabled) {
					Log.d(TAG, "Network");
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled && location == null) {
					Log.d(TAG, "GPS Enabled");
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stopMonitoring() {
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location update " + location);
		new fetchFencesTask().execute(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private class fetchFencesTask extends AsyncTask<Location, Integer, Boolean> {

		private static final String url = "http://api.mexwave.endare.com:1338/api/v1/locations";
		
		private String getUrl(Location l) {
			return url + "?lon="  + l.getLongitude() + "&lat=" + l.getLatitude();
		}
		
		@Override
		protected Boolean doInBackground(Location... params) {
			try {
				Location l = params[0];
				
	            Log.i(TAG, "Posting  native location update: " + l);
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpGet request = new HttpGet(getUrl(l));

	            Log.d(TAG, "Fetching " + request.getURI().toString());
	            HttpResponse response = httpClient.execute(request);
	            Log.i(TAG, "Response received: " + response.getStatusLine());
	            if (response.getStatusLine().getStatusCode() == 200) {
	            	BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
	 
					String output, result = "";
					System.out.println("Output from Server .... \n");
					while ((output = br.readLine()) != null) {
						result += output;
					}
					System.out.println(result);
					
					Type listType = new TypeToken<List<GeofenceServerEntry>>() {}.getType();
					List<GeofenceServerEntry> serverEntries = new Gson().fromJson(result, listType);
					
					ArrayList<GeoNotification> fences = new ArrayList<GeoNotification>();
					for (int i = 0; i < serverEntries.size(); i++) {
						if (i >= 20) break;
						fences.add(serverEntries.get(i).generateGeoNotification(i));
					}
					fencesReceived(fences);
					
	                return true;
	            } else {
	                return false;
	            }
	        } catch (Throwable e) {
	            Log.w(TAG, "Exception posting location: " + e);
	            e.printStackTrace();
	            return false;
	        }
		}
	}
	
	protected abstract void fencesReceived(List<GeoNotification> fences);
}