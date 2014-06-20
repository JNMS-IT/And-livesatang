package org.android.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.satsang.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;

public class APKDownloadTask extends AsyncTask<Void, Void, String[]> {
	static private final Logger Log = LoggerFactory.getLogger(APKDownloadTask.class);
	
	final String libs = "export LD_LIBRARY_PATH=/vendor/lib:/system/lib";
	private Context context;
	private boolean install;
	
	public APKDownloadTask(Context context, boolean install) {
		this.context=context;
		this.install=install;
	}


	protected String[] doInBackground(Void... v) {
		Log.info("APKDownloadTask.doInBackground()");

		File sdcard = Environment.getExternalStorageDirectory();
		FileOutputStream fos = null;
		SharedPreferences pref = context.getSharedPreferences("schedule.tasks", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		try {
			String file = "live";
			int BUFFER_SIZE = 50 * 2048;
			
			String serverURL = pref.getString("schedule.tasks.apkUpdate.server.url", null);
			String token = pref.getString("schedule.tasks.apkUpdate.download.token", null);
			if(serverURL != null && token != null) {
				String macId = CommonUtil.getMacId(context);
				String parameters = "?mac=" + macId + "&tkn=" + token;
				//TODO: Use encryption of request parameters pending and decrypt APK file
				URL url = new URL(serverURL + parameters);
				
				URLConnection ucon = url.openConnection();
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is, 50 * 2048);
				Log.debug("Got response from server");
				fos = new FileOutputStream(sdcard + "/" + file);
				byte[] baf = new byte[BUFFER_SIZE];
				int current = 0;
				while (current != -1) {
					fos.write(baf, 0, current);
					current = bis.read(baf, 0, BUFFER_SIZE);

				}
				fos.close();
				editor.putBoolean("schedule.tasks.apkUpdate.isDownloaded", true);
				editor.commit();
				Log.debug("APK download complete");
				if(install) {
					Log.debug("now installing apk....");
					String[] cmd = { libs, "pm install -r /mnt/sdcard/live" };
					Log.debug("before calling execute_as_root");
					CommonUtil.execute_as_root(cmd);	
				}
	
			} else {
				Log.error("Server url and/or download token is null ");
			}
			
						
		} catch (Exception e) {
			Log.error("Exception:" + e);
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (Exception e) {
				Log.error("Exception finally " + e);
			}
		}
		
		return null;
	}
	
	
}
