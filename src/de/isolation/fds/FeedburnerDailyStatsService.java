package de.isolation.fds;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class FeedburnerDailyStatsService extends android.app.Service {
	private Timer timer = new Timer();
	private static final long INTERVAL = 1000 * 60 * 30; // every 30 minutes
	private HttpClient hc;

	@Override
	public IBinder onBind(Intent arg0) {
		// no IPC
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		hc = new DefaultHttpClient();
		// start the service with stored URI
		SharedPreferences settings =  
				getSharedPreferences(FeedburnerDailyStatsActivity.SETTINGSNAME, MODE_PRIVATE);
		String uri = settings.getString(FeedburnerDailyStatsActivity.FEEDURI, "");
		startservice(uri);
	}
	private void startservice(final String uri) {
		timer.scheduleAtFixedRate( new TimerTask() {
			private NotificationManager mManager;
			public void run() {
				String result = null;
				try {
					HttpGet get = new HttpGet(FeedburnerDailyStatsActivity.BASEURL + uri);
					HttpResponse response = hc.execute(get);
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						result = EntityUtils.toString(response.getEntity());	
					}
				} catch (IOException e) {
					// logging???
				}

				// first let's see whether we have already notified about this...
				
				FdsApp app = (FdsApp)getApplicationContext();
				String lastNotifiedSetting = app.getLatestDay();
				String newDate = getValue(result, FeedburnerDailyStatsActivity.DATE);
				// if there is new information, extract and notify
				if (!lastNotifiedSetting.equals(newDate)) {
					app.setLatestDay(newDate);
					// now notify
					mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					String numOfSubscribers = getValue(result, FeedburnerDailyStatsActivity.CIRCULATION);
					Intent contentIntent = new Intent(app, FeedburnerDailyStatsActivity.class);
					contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					Notification notification = new Notification(R.drawable.logo64,"Yesterdays subscribers: "+numOfSubscribers, System.currentTimeMillis());
					notification.setLatestEventInfo(FeedburnerDailyStatsService.this,
							"Feedburner Daily Stats",
							"Subscribers for " + newDate + ": "+numOfSubscribers,
							PendingIntent.getActivity(FeedburnerDailyStatsService.this, 0, contentIntent,
									PendingIntent.FLAG_CANCEL_CURRENT));
					long[] vibration = {0,100,200,20,20,20,20,20,20,20}; // funny buzzing :)
					notification.vibrate = vibration;
					mManager.notify(FeedburnerDailyStatsActivity.APP_ID, notification);
				} 
			}

			String getValue(String response, String label) {
				String value = "-0";
				if (response != null && response.indexOf(label) > 0) {
					int pos = response.indexOf(label) + label.length() + 2;
					int nextPos = response.indexOf("\"", pos+1);
					value = response.substring(pos, nextPos); 
				}
				return value;
			}
		}, 0, INTERVAL);


	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hc.getConnectionManager().shutdown();
		hc = null;
		if (timer != null) {
			timer.cancel();
		}
	}	
}
