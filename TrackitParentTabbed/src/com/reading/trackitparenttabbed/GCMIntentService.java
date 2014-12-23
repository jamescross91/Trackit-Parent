package com.reading.trackitparenttabbed;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	public GCMIntentService() {
		super("714729619832");
	}

	private static final String TAG = "===GCMIntentService===";
	public static final String LOCATION_UPDATE_KEY = "Loc";
	public static final String GEOFENCE_CROSS_KEY = "Geo";
	public static final String MARKER_RELOAD_KEY = "Marker";
	public static final String DEVICE_DELETE_KEY = "Delete";

	@Override
	protected void onRegistered(Context arg0, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.i(TAG, "unregistered = " + arg1);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "new message= ");

		Bundle bundle = intent.getExtras();
		String locstring = (String) bundle.get(LOCATION_UPDATE_KEY);

		if (locstring != null) {
			Log.i(TAG, "new locmessage= " + locstring);

			Intent locIntent = new Intent(
					context.getString(R.string.loc_update_broadcast_action));
			locIntent.putExtras(bundle);
			context.sendBroadcast(locIntent);
		}
		String geostring = (String) bundle.get(GEOFENCE_CROSS_KEY);
		if (geostring != null) {
			
			//Get the default notification sound
			Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Geofence has been crossed!")
					.setContentText(geostring)
					.setSound(uri);

			 Intent resultIntent = new Intent(context, TabbedActivity.class);
			 TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			 stackBuilder.addParentStack(TabbedActivity.class);
			 stackBuilder.addNextIntent(resultIntent);
			 PendingIntent resultPendingIntent =
			 stackBuilder.getPendingIntent(0,
			 PendingIntent.FLAG_UPDATE_CURRENT);
			
			 mBuilder.setContentIntent(resultPendingIntent);
			
			Log.i(TAG, "new geomessage = " + geostring);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(1, mBuilder.build());
		}
		
		String markerReload = (String) bundle.get(MARKER_RELOAD_KEY);
		if(markerReload != null){
			Intent geoIntent = new Intent(
					context.getString(R.string.marker_update_broadcast_action));
			geoIntent.setAction(context.getString(R.string.marker_update_broadcast_action));
			geoIntent.putExtras(bundle);
			context.sendBroadcast(geoIntent);
		}
		
		String deviceDelete = (String) bundle.get(DEVICE_DELETE_KEY);
		if(deviceDelete != null){
			Intent deleteIntent = new Intent(context.getString(R.string.device_delete_broadcast_action));
			deleteIntent.putExtras(bundle);
			context.sendBroadcast(deleteIntent);
			
			Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("This device has been deleted and you have been logged out")
					.setContentText(geostring)
					.setSound(uri);
			
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(1, mBuilder.build());
		}
	}

	@Override
	protected void onError(Context arg0, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		return super.onRecoverableError(context, errorId);
	}
}
