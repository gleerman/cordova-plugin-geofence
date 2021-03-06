package com.cowbell.cordova.geofence;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class GeofencePlugin extends CordovaPlugin {
    public static final String TAG = "GeofencePlugin";
    private GeoNotificationManager geoNotificationManager;
    private Context context;
    private LocationUpdateService locationUpdateService;
    protected static Boolean isInBackground = true;
    private static CordovaWebView webView = null;

    /**
     * @param cordova
     *            The context of the main Activity.
     * @param webView
     *            The associated CordovaWebView.
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        GeofencePlugin.webView = webView;
        context = this.cordova.getActivity().getApplicationContext();
        Logger.setLogger(new Logger(TAG, context, false));
        geoNotificationManager = new GeoNotificationManager(context);
    }

    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "GeofencePlugin execute action: " + action + " args: "
                + args.toString());

        if (action.equals("addOrUpdate")) {
            List<GeoNotification> geoNotifications = new ArrayList<GeoNotification>();
            for (int i = 0; i < args.length(); i++) {
                GeoNotification not = parseFromJSONObject(args.getJSONObject(i));
                if (not != null) {
                    geoNotifications.add(not);
                }
            }
            geoNotificationManager.addGeoNotifications(geoNotifications,
                    callbackContext);
        } else if (action.equals("remove")) {
            List<String> ids = new ArrayList<String>();
            for (int i = 0; i < args.length(); i++) {
                ids.add(args.getString(i));
            }
            geoNotificationManager.removeGeoNotifications(ids, callbackContext);
        } else if (action.equals("removeAll")) {
            geoNotificationManager.removeAllGeoNotifications(callbackContext);
        } else if (action.equals("getWatched")) {
            List<GeoNotification> geoNotifications = geoNotificationManager
                    .getWatched();
            Gson gson = new Gson();
            callbackContext.success(gson.toJson(geoNotifications));
        } else if (action.equals("initialize")) {
        	locationUpdateService = new LocationUpdateService(context) {
				@Override
				protected void fencesReceived(final List<GeoNotification> fences) {
					geoNotificationManager.removeAllGeoNotifications(new CallbackContext(null, webView) {
						@Override
						public void success() {
							Log.d(TAG, "Adding " + fences.size() + " fences");
							geoNotificationManager.addGeoNotifications(fences, null);
						}
					});
				}
			};
			locationUpdateService.startMonitoring();
        } else if (action.equals("enablePushNotifications")) {
            for (int i = 0; i < args.length(); i++) {
            	GeofenceSettings.getInstance().setPushNotifications(args.getBoolean(i));
            }
        } else if (action.equals("enableTracking")) {
            for (int i = 0; i < args.length(); i++) {
            	GeofenceSettings.getInstance().setTracking(args.getBoolean(i));
            	if (args.getBoolean(i)) {
            		locationUpdateService.startMonitoring();
            	}
            	else {
            		geoNotificationManager.removeAllGeoNotifications(callbackContext);
            		locationUpdateService.stopMonitoring();
            	}
            }
        } else {
            return false;
        }
        return true;

    }

    private GeoNotification parseFromJSONObject(JSONObject object) {
        GeoNotification geo = null;
        geo = GeoNotification.fromJson(object.toString());
        return geo;
    }

    public static void fireRecieveTransition(List<GeoNotification> notifications) {
        Gson gson = new Gson();
        String js = "setTimeout('geofence.receiveTransition("
                + gson.toJson(notifications) + ")',0)";
        if (webView == null) {
            Log.d(TAG, "Webview is null");
        } else {
            webView.sendJavascript(js);
        }
    }

}
