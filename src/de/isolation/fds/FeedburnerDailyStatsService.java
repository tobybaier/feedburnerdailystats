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
	private static final long INTERVAL = 1000 * 60 * 10; // every 10 minutes
	private static final String LASTNOTIFIED = "lastNotified";
	private static final int APP_ID = 2342; 

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences settings =  
				getSharedPreferences(FeedburnerDailyStatsActivity.getSettingsname(), MODE_PRIVATE);
		String uri = settings.getString(FeedburnerDailyStatsActivity.getFeeduri(), "");
		startservice(uri);
	}
	private void startservice(final String uri) {
		timer.scheduleAtFixedRate( new TimerTask() {
			private NotificationManager mManager;
			public void run() {
				String result = null;
				try {
					HttpClient hc = new DefaultHttpClient();
					HttpGet get = new HttpGet(FeedburnerDailyStatsActivity.getBaseurl() + uri);
					HttpResponse response = hc.execute(get);
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						result = EntityUtils.toString(response.getEntity());	
					}
				} catch (IOException e) {
					// logging???
				}

				// first let's see whether we have already notified about this...
				SharedPreferences settings =  
						getSharedPreferences(FeedburnerDailyStatsActivity.getSettingsname(), MODE_PRIVATE);
				String lastNotifiedSetting = settings.getString(LASTNOTIFIED, "");
				String newDate = getValue(result, FeedburnerDailyStatsActivity.getDate());
				if (!lastNotifiedSetting.equals(newDate)) {
					SharedPreferences.Editor prefEditor = settings.edit(); 
					prefEditor.putString(LASTNOTIFIED, newDate);
					// now notify
					mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					String numOfSubscribers = getValue(result, FeedburnerDailyStatsActivity.getCirculation());
					Intent contentIntent = new Intent(FeedburnerDailyStatsService.this, FeedburnerDailyStatsActivity.class);
					Notification notification = new Notification(R.drawable.icon,"Yesterdays subscribers: "+numOfSubscribers, System.currentTimeMillis());
					notification.setLatestEventInfo(FeedburnerDailyStatsService.this,
							"Feedburner Daily Stats",
							"Subscribers for " + newDate + ": "+numOfSubscribers,
							PendingIntent.getActivity(FeedburnerDailyStatsService.this, 0, contentIntent,
									PendingIntent.FLAG_CANCEL_CURRENT));
					long[] vibration = {0,100,200,20,20,20,20,20,20,20};
					notification.vibrate = vibration;
					mManager.notify(APP_ID, notification);
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
		// TODO Auto-generated method stub
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
	}	
}