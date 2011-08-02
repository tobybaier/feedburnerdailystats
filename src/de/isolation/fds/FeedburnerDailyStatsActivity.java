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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedburnerDailyStatsActivity extends Activity { 
	public static final String SETTINGSNAME = "FeedburnerDailyStatsSettings";
	public static final String FEEDURI = "feedUri";
	public static final String DATE = "date";
	public static final String CIRCULATION = "circulation";
	public static final int APP_ID = 2342; 
	private static final String HITS = "hits";
	private static final String DOWNLOADS = "downloads";
	private static final String REACH = "reach";
	public static final String BASEURL = "https://feedburner.google.com/api/awareness/1.0/GetFeedData?uri=";    
	private static final String TAG = "fds";

	private HttpClient hc;
	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, this.getClass()+":onCreate");
        super.onCreate(savedInstanceState);
        hc = new DefaultHttpClient();
        setContentView(R.layout.main);
        // remove notification
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(2342);
        // get last uri from settings and set to input field
        SharedPreferences settings =  
        		getSharedPreferences(SETTINGSNAME, MODE_PRIVATE);
        String lastUri = settings.getString(FEEDURI, "");
        EditText input = (EditText) findViewById(R.id.editText1);
        input.setText(lastUri);
        
        // initialize notification checkbox
        final CheckBox notifyCheckbox = (CheckBox) findViewById(R.id.notifyCheckbox);
        final FdsApp app = ((FdsApp)getApplicationContext());
        if (app.getService() != null) {
        	notifyCheckbox.setChecked(true);
        } else {
        	notifyCheckbox.setChecked(false);
        }
        // add listener to check box
        notifyCheckbox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    app.startNotificationService();
                } else {
                    app.stopNotificationService();
                }
            }
        });
        
        // link the image
        ImageView img = (ImageView)findViewById(R.id.epImage);
        img.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://einschlafen-podcast.de"));
                startActivity(intent);
            }
        });
        
    }
    
    

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		hc.getConnectionManager().shutdown();
		hc = null;
	}



	/**
     * get stats from Feedburner
     */
    public void clickHandler(View view) {
    	// get uri from input field
    	String uri = new StringBuffer(((EditText) findViewById(R.id.editText1)).getText()).toString();
    	// store it in settings
        SharedPreferences settings =  
        		getSharedPreferences(SETTINGSNAME, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();  
        prefEditor.putString(FEEDURI, uri);
        prefEditor.commit();
        
        // get stats from feedburner
    	String result = null;
    	try {
    		HttpGet get = new HttpGet(BASEURL + uri);
    		HttpResponse response = hc.execute(get);
    		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    			result = EntityUtils.toString(response.getEntity());	
    		}
    	} catch (IOException e) {
    		Log.e(TAG, "IOException: "+ e.getMessage(), e);
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
	
}