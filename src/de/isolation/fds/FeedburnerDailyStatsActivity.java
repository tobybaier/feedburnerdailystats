package de.isolation.fds;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class FeedburnerDailyStatsActivity extends Activity {
	private static final String SETTINGSNAME = "FeedburnerDailyStatsSettings";
	private static final String FEEDURI = "feedUri";
	private static final String DATE = "date";
	private static final String CIRCULATION = "circulation";
	private static final String HITS = "hits";
	private static final String DOWNLOADS = "downloads";
	private static final String REACH = "reach";
	private static final String BASEURL = "https://feedburner.google.com/api/awareness/1.0/GetFeedData?uri=";
	Intent svc;
    
	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(2342);
        setContentView(R.layout.main);
        SharedPreferences settings =  
        		getSharedPreferences(getSettingsname(), MODE_PRIVATE);
        String lastUri = settings.getString(getFeeduri(), "");
        EditText input = (EditText) findViewById(R.id.editText1);
        input.setText(lastUri);
        
    }

	/**
     * get stats from Feedburner
     */
    public void clickHandler(View view) {
    	String uri = new StringBuffer(((EditText) findViewById(R.id.editText1)).getText()).toString();
        SharedPreferences settings =  
        		getSharedPreferences(getSettingsname(), MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();  
        prefEditor.putString(getFeeduri(), uri);
        prefEditor.commit();
    	String result = null;
    	try {
    		HttpClient hc = new DefaultHttpClient();
    		HttpGet get = new HttpGet(BASEURL + uri);
    		HttpResponse response = hc.execute(get);
    		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    			result = EntityUtils.toString(response.getEntity());	
    		}
    	} catch (IOException e) {
    		// logging???
    	}
    	getValue(result, DATE, (TextView) findViewById(R.id.dateView));
    	getValue(result, CIRCULATION, (TextView) findViewById(R.id.subsView));
    	getValue(result, HITS, (TextView) findViewById(R.id.hitsView));
    	getValue(result, DOWNLOADS, (TextView) findViewById(R.id.downloadsView));
    	getValue(result, REACH, (TextView) findViewById(R.id.reachView));
    	
    }

	public static void getValue(String response, String value, TextView label) {
		if (response != null && response.indexOf(value) > 0) {
    		int pos = response.indexOf(value) + value.length() + 2;
    		int nextPos = response.indexOf("\"", pos+1);
    		String result = response.substring(pos, nextPos); 
    		label.setText(result);
    	} else {
    		label.setText("cannot find stats!");
    	}
	}
	
	public void startButtonHandler() {
		if (svc == null) {
			svc = new Intent(this, FeedburnerDailyStatsService.class);
			startService(svc);
		}
	}
	
	public void stopButtonHandler() {
		if (svc != null) {
			stopService(svc);
		}
		svc = null;
	}

	public static String getFeeduri() {
		return FEEDURI;
	}

	public static String getSettingsname() {
		return SETTINGSNAME;
	}

	public static String getBaseurl() {
		return BASEURL;
	}
	
	public static String getCirculation() {
		return CIRCULATION;
	}
	
	public static String getDate() {
		return DATE;
	}
}