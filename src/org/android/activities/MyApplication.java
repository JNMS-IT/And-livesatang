package org.android.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.satsang.bo.MediaSchedule;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.TextView;

public class MyApplication extends Application {
	static private final Logger Log = LoggerFactory.getLogger(MyApplication.class);
	
	
	int meterPercentage;
	String dCountText = "";
	
	
	private static ArrayList<MediaSchedule> pravachanScheduleList = new ArrayList<MediaSchedule>();
	private static HashMap<String, MediaSchedule> playMap = new HashMap<String, MediaSchedule>();
	
	public void updatePravachanSchedule(List<MediaSchedule> list) {
		if(list != null) {
			pravachanScheduleList.clear();
			pravachanScheduleList.addAll(list);
		}
	}
	
	//TODO: Need to revise below code
	/*public void updateFileStatusDownloadComplete(String fileName){
		Log.trace("updateFileStatusDownloadComplete");
		dCountText = "";
		try {
			if(pravachanScheduleList != null && fileName != null && (!"".equalsIgnoreCase(fileName))) {
				Log.info("pravachanScheduleList not null");
				Iterator<MediaSchedule> itr = pravachanScheduleList.iterator();
				while (itr.hasNext()) {
					MediaSchedule schedule = itr.next();
					String downloadStatus = schedule.getDownloadStatus();
					if ((downloadStatus != null && "complete".equalsIgnoreCase(downloadStatus)) || (fileName.equalsIgnoreCase(schedule.getFileName()))) {
						dCountText = dCountText + "*";
					} else if("skipped".equalsIgnoreCase(downloadStatus)) {
						dCountText = dCountText + "x";
					} else {
						dCountText = dCountText + "o";
					}
				}	
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}*/
	
	public void setDownloadStatus() {
		Log.trace("Inside setDownloadStatus()");
		dCountText = "";
		String pravachanStatus="";
		String newsStatus="";
		String bhajanStatus="";
		try {
			if(pravachanScheduleList != null) {
				Iterator<MediaSchedule> itr = pravachanScheduleList.iterator();
				while (itr.hasNext()) {
					MediaSchedule schedule = itr.next();
					String downloadStatus = schedule.getDownloadStatus();
					Log.debug("Setting download status for " + schedule.getFileName());
					if(Constants.PRAVACHAN.equalsIgnoreCase(schedule.getScheduleType())) {
						if (downloadStatus != null && "complete".equalsIgnoreCase(downloadStatus)) {
							pravachanStatus = pravachanStatus + "*";
						} else if("skipped".equalsIgnoreCase(downloadStatus)) {
							pravachanStatus = pravachanStatus + "x";
						} else {
							pravachanStatus = pravachanStatus + "o";
						}	
					}else if(Constants.NEWS.equalsIgnoreCase(schedule.getScheduleType())) {
						//add to news
						if (downloadStatus != null && "complete".equalsIgnoreCase(downloadStatus)) {
							newsStatus = newsStatus + "*";
						} else if("skipped".equalsIgnoreCase(downloadStatus)) {
							newsStatus = newsStatus + "x";
						} else {
							newsStatus = newsStatus + "o";
						}	
					}else{
						//add to Bhajan
						if (downloadStatus != null && "complete".equalsIgnoreCase(downloadStatus)) {
							bhajanStatus = bhajanStatus + "*";
						} else if("skipped".equalsIgnoreCase(downloadStatus)) {
							bhajanStatus = bhajanStatus + "x";
						} else {
							bhajanStatus = bhajanStatus + "o";
						}
					}
					
				}
				if(!"".equalsIgnoreCase(pravachanStatus)) {
					dCountText=pravachanStatus;
				}
				if(!"".equalsIgnoreCase(newsStatus)) {
					dCountText=dCountText+"|"+newsStatus;
				}
				if(!"".equalsIgnoreCase(bhajanStatus)) {
					dCountText=dCountText+"|"+bhajanStatus;
				}
//				dCountText=pravachanStatus+"|"+newsStatus+"|"+bhajanStatus;
						
			}
			
		} catch (Exception e) {
			Log.error("Error in setDownloadStatus " + e);
		}
	}

	public void setDownloadStatusText(TextView dCountText2) {
		dCountText2.setText(dCountText);

	}

	public void setMeterPercentage(int meterPercentage) {
		this.meterPercentage = meterPercentage;
	}

	//TODO: Need to revise this to load configuration from database as well..?? Assuming Config hashmap is loaded at boot time
	public void setLocaleConfiguration(){
		Log.trace("Inside setLocaleConfiguration()");
		String strFileLocale = ConfigurationLive.getValue("local.default.locale");
		if(strFileLocale != null && (!"".equalsIgnoreCase(strFileLocale))){
			if (strFileLocale.trim().equalsIgnoreCase("ENG") || strFileLocale.trim().equalsIgnoreCase("EN") ) {
				strFileLocale = "en";
			} else if (strFileLocale.trim().equalsIgnoreCase("HIN")) {
				strFileLocale = "hi";
			} else {
				strFileLocale = "ma";
			}	
		}else strFileLocale="ma";
		
		String localeStr = new String(strFileLocale);
		Locale locale = new Locale(localeStr);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
	}

		
	public static HashMap<String, MediaSchedule> getPlayMap() {
		return playMap;
	}

	public static void setPlayMap(HashMap<String, MediaSchedule> playMap) {
		MyApplication.playMap = playMap;
	}
	
	

	@Override
	public void onTerminate() {
		super.onTerminate();
		/*try {
			DBAdapter dbAdp = new DBAdapter(getApplicationContext());
			dbAdp.close();	
		}catch(Exception e){
			
		}*/
		
	}
	
	

}