package org.android.task;

import java.net.InetAddress;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.satsang.util.CommonUtil;
import org.satsang.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

public class DateTimeUpdateTask extends AsyncTask<Void, Void, String[]> {
	static private final Logger Log = LoggerFactory.getLogger(DateTimeUpdateTask.class);
	
	private final String NTP_HOST="time-a.nist.gov"; //set it to Nanij NTP server later?
	final String libs = "export LD_LIBRARY_PATH=/vendor/lib:/system/lib";
	private Context context;
	
	public DateTimeUpdateTask(Context context) {
		this.context=context;
	}


	protected String[] doInBackground(Void... v) {
		Log.trace("DateTimeUpdateTask:doInBackground called");
		NTPUDPClient client = new NTPUDPClient();
		client.setDefaultTimeout(10000);
		try{
			long oldTime = System.currentTimeMillis();
			long currentTimeStamp=0;
			try {
				InetAddress hostAddr = InetAddress.getByName(NTP_HOST);
				TimeInfo info = client.getTime(hostAddr);
				currentTimeStamp = info.getMessage().getReceiveTimeStamp().getTime();
				Log.debug("got NTP time:" + currentTimeStamp);	
			}catch(Exception e){
				Log.error("NTPException:" + e);
			}
			//check currentTimestamp is not zero or greater than oldTime
	        if(currentTimeStamp >= oldTime){
	        	Log.debug("currentTime > oldTime:" + currentTimeStamp);
	        	String fmtDt = DateUtil.getShellFormattedDate(oldTime);
		        if(fmtDt != null) {
		        	Log.debug("Setting datetime to: "+fmtDt);
			        String[] cmd = { libs, "date -s \"" + fmtDt + "\"" };
					CommonUtil.execute_as_root(cmd);	
		        }
	        }else {
	        	Log.debug("setting NITZTime:");
	        	Settings.System.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 1);
	        }
	        
		}catch(Exception e){
			Log.error("Error synchronizing time:" + e);
		}finally {
			try {
				client.close();	
			}catch(Exception e){
				Log.error("Error in finally:" + e);
			}
			
		}
		return null;
	}
	
}
