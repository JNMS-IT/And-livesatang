package org.android.activities;

import java.util.List;

import org.santsang.core.LiveSatsangService;
import org.santsang.schedule.ScheduleManager;
import org.satsang.audit.AuditConstants;
import org.satsang.audit.EventHandler;
import org.satsang.bo.MediaSchedule;
import org.satsang.database.DatabaseManager;
import org.satsang.database.LSDatabaseHelper;
import org.satsang.live.config.Constants;
import org.satsang.util.CommonUtil;
import org.satsang.util.ConfigUtil;
import org.satsang.util.DateUtil;
import org.satsang.web.WebAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.widget.TextView;


public class Livesatsang extends Activity {

	static private final Logger Log = LoggerFactory.getLogger(Livesatsang.class);

	private final Context context = this;
	TextView dStatusText;
	TextView txtWelcome;
	MyApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_livesatsang);
		Log.info("OnCreate");
		// Initializing DatabaseManager By Rohit
		LSDatabaseHelper db = new LSDatabaseHelper(this);
		DatabaseManager.initializeInstance(db);
		//TODO: set locale after loading assuming locale and configuration is set at boot
		CommonUtil.loadConfiguration(context);
		app = (MyApplication) getApplication();
		app.setLocaleConfiguration();
		
		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");
		txtWelcome = (TextView) findViewById(R.id.txt_maint_start);
		txtWelcome.setTypeface(face);
		txtWelcome.setText(getString(R.string.welcome));
		if(!Constants.MODE_IS_LIVE) {
			new StartActivity().execute();
		}else {
			if(validateState()) {
				new StartActivity().execute();
			}
		}
		
	}
	
	
	private boolean validateState() {
		if (!CommonUtil.isSIMPresent(context)) {
			System.out.println("SIM Card not present");
			/*Intent intent = new Intent(getApplicationContext(), End.class);
			intent.putExtra("status", getString(R.string.simcard));
			currentActivity.startActivity(intent);*/
			txtWelcome.setText(getString(R.string.simcard));
			return false;
			
		}else if (CommonUtil.isAirplaneModeOn(context)) {
			/*Intent intent = new Intent(getApplicationContext(), End.class);
			intent.putExtra("status", getString(R.string.aeroplane));
			currentActivity.startActivity(intent);*/
			txtWelcome.setText(getString(R.string.aeroplane));
			return false;
		}else if (!"active".equalsIgnoreCase(CommonUtil.checkSevaKendraStatus(context))) {
			/*Intent intent = new Intent(getApplicationContext(), End.class);
			intent.putExtra("status", getString(R.string.servicestop));
			currentActivity.startActivity(intent);*/
			txtWelcome.setText(getString(R.string.servicestop));
			return false;
		}
		return true;
	}

	class StartActivity extends AsyncTask<String, String, String> {

			
		/**
		 * Check Satsang schedule etc in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			
			if(Constants.MODE_IS_LIVE) {
				Settings.System.putInt(context.getContentResolver(),Global.AUTO_TIME, 1);
				Settings.System.putInt(context.getContentResolver(),Global.AUTO_TIME_ZONE, 1);	
			}
			
			//Refresh auth manually.. as auto refresh would be once in 8 or 10 hours.
			WebAdapter webAdp = new WebAdapter(context);
			webAdp.synchronize();
			
			
			ScheduleManager praMgr = new ScheduleManager();
			List<MediaSchedule> list = praMgr.getAllPravachanRecords(context);
			
			app.updatePravachanSchedule(list);
			app.setDownloadStatus();
			publishProgress("");
			
			startService(new Intent(getApplicationContext(),LiveSatsangService.class));
			//check if santsang needs to be played
			try {
				boolean isPlayDay = ConfigUtil.checkScheduleFromConfiguration();
				int playWindow = ConfigUtil.getPlayWindow();
				if(isPlayDay && playWindow != -1) {
					Log.debug("Inside play window calling santsang activity");
					Intent santsang = new Intent(context, SantsangActivity.class);
					context.startActivity(santsang);
				}else if(playWindow == -1) {
					Intent download = new Intent(context, DownloadActivity.class);
					context.startActivity(download);
				}
			}catch(Exception e) {
				Log.error("Exception checking schedule from configuration" + e);
				EventHandler.appendAuditToFile("106", "Config not found", AuditConstants.AUDIT_TYPE_ERROR);
				Intent intent = new Intent(context, End.class);
				intent.putExtra("status", getString(R.string.err106));
				context.startActivity(intent);
			}
			
//			finish();
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			/*Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");

			TextView TxtWelcome = (TextView) currentActivity.findViewById(R.id.txt_maint_start);
			TxtWelcome.setTypeface(face);
			TxtWelcome.setText(getString(R.string.welcome));*/
			
			dStatusText = (TextView) findViewById(R.id.txt_downloadStatus);
			app.setDownloadStatusText(dStatusText);
		}


		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
//			app.setDownloadStatus();
//			dStatusText = (TextView) findViewById(R.id.txt_downloadStatus);
//			app.setDownloadStatusText(dStatusText);
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		return;
	}


	@Override
	protected void onRestart() {
		super.onRestart();
		return;
	}


	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("on Resume");
		((MyApplication) getApplication()).setLocaleConfiguration();
//		validateState();
		return;
	}


}
