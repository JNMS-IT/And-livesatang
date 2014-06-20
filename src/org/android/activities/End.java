package org.android.activities;

import org.android.phone.CDUSSDService;
import org.satsang.bo.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class End extends Activity {
	static private final Logger Log = LoggerFactory.getLogger(End.class);
	
	Typeface face;
	TextView end;
//	private Thread mSplashThread;
	String imei;
	private final Context context = this;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end);
		MyApplication app = (MyApplication) getApplication();
		app.setLocaleConfiguration();
		face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");
		end = (TextView) findViewById(R.id.txt_end);
		end.setTypeface(face);
		TextView dStatusText = (TextView) findViewById(R.id.txt_downloadStatus);
		
		Singleton.getInstance().setSantsangInProgress(false);
		Intent ussdService = new Intent(this, CDUSSDService.class);
		this.stopService(ussdService);
		app.setDownloadStatusText(dStatusText);
		Bundle extras = getIntent().getExtras();
		String resultText = null;
		if (extras != null) {
			try {
				resultText = (String) extras.get("status");
				Log.debug("Received result:" + resultText);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (resultText == null) {
			end.setText(getString(R.string.end));
		} else {
			((TextView) end).setText(resultText);
		}
		/*TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE); 
		imei = tm.getDeviceId();
		getApplication().getCacheDir().deleteOnExit();
		// The thread to wait for splash screen events
		mSplashThread = new Thread() {
			@Override
			public void run() {
				// TODO: Upload audit events to server using WebAdapter OR let ScheduleReceiver to it.
				try {
					WebCommHandler webComm = new WebCommHandler();
					webComm.uploadAuditRecord(imei);
				} catch (Exception e) {
					// e.printStackTrace();
				}
				// Wait and exit
				try {
					synchronized (this) {
						// Wait given period of time or exit on touch
						wait(12000);
					}
				} catch (InterruptedException ex) {
				}

				// Run next activity
				getApplication().getCacheDir().delete();
				getApplication().getFilesDir().delete();
			}
		};
		mSplashThread.start();*/
	}
	
	

	@Override
	protected void onRestart() {
		super.onRestart();
		Singleton.getInstance().setSantsangInProgress(false);
		((MyApplication) getApplication()).setLocaleConfiguration();
		return;
	}



	@Override
	protected void onResume() {
		super.onResume();
		Singleton.getInstance().setSantsangInProgress(false);
		((MyApplication) getApplication()).setLocaleConfiguration();
		
		return;
	}



	@Override
	public void onBackPressed() {
		return;
	}


}
