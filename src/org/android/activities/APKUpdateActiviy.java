package org.android.activities;

import org.android.task.APKDownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;


public class APKUpdateActiviy extends Activity {
	
	static private final Logger Log = LoggerFactory.getLogger(APKUpdateActiviy.class);

	private final Context context = this;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apk_update);
		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");
		TextView Txt_Wait = (TextView) findViewById(R.id.txt_wait);
		Txt_Wait.setTypeface(face);
		Txt_Wait.setText(getString(R.string.update));
		TextView dStatusText = (TextView) findViewById(R.id.txt_downloadStatus);
		MyApplication app = (MyApplication) getApplication();
		app.setDownloadStatusText(dStatusText);
		
		boolean forceUpdate = false;
		
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			try {
				forceUpdate = extras.getBoolean("forceUpdate");
			} catch (Exception e) {
				Log.error("Error retrieving bundle params " + e);
			}
		}
		//TODO: deferred implementation: check if file is already downloaded if yes then install
		//call download & install
		new APKDownloadTask(context, forceUpdate).execute();	
		}

	@Override
	protected void onRestart() {
		super.onRestart();
		return;
	}

	@Override
	protected void onResume() {
		super.onResume();
		return;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		return;
	}
	
	

	

}
