package org.android.boot;

import org.satsang.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.TelephonyManager;

public class BootReceiver extends BroadcastReceiver {
	static private final Logger Log = LoggerFactory.getLogger(BootReceiver.class);
	
	int sdcardRetryCount=3;
	@Override
	public void onReceive(Context context, Intent intent) {
		
		try {
			// TODO: have retry attempts. If this still fails display error?
			int i = 0;
			while(!isExternalStorageWritable() || !hasLatchedToNetwork(context)) {
				Log.trace("Not latched or storage not available retry count: " + i);
				Thread.sleep(2000);
				i++;
				if(i==sdcardRetryCount) break;
			}
			
		} catch (Exception e) {
			Log.error("Bootreceiver thread interrupted??");
		}
		CommonUtil.loadConfiguration(context);
		
		Intent livesatsang = new Intent(context, org.android.activities.Livesatsang.class);
		livesatsang.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		livesatsang.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		livesatsang.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		context.startActivity(livesatsang);
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	/*
	 * Checks if device has latched on to network
	 */
	private boolean hasLatchedToNetwork(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = tm.getNetworkType(); // 0 if network type is unknown
		if (tm.getSimState() == TelephonyManager.SIM_STATE_UNKNOWN
				|| networkType == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
			return false;
		} else {
			return true;
		}
	}
	

}
