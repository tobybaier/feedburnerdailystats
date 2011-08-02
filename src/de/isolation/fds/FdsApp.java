package de.isolation.fds;

import android.app.Application;
import android.content.Intent;

public class FdsApp extends Application {
	private FeedburnerDailyStatsService service;
	private Intent intent;
	private String latestDay = "0";

	public FeedburnerDailyStatsService getService() {
		return service;
	}

	public void setService(FeedburnerDailyStatsService service) {
		this.service = service;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public String getLatestDay() {
		return latestDay;
	}

	public void setLatestDay(String latestDay) {
		this.latestDay = latestDay;
	}

	public void startNotificationService() {
		if (getIntent() == null) {
			Intent svc = new Intent(this, FeedburnerDailyStatsService.class);
			startService(svc);
			setIntent(svc);
		}
	}
	
	public void stopNotificationService() {
        if (getIntent() != null) {
			stopService(getIntent());
		}
	}
	
}
