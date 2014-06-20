package org.santsang.core;


import java.util.ArrayList;
import java.util.Date;

import org.android.activities.APKUpdateActiviy;
import org.android.activities.DownloadActivity;
import org.android.activities.SantsangActivity;
import org.android.task.APKDownloadTask;
import org.android.task.DateTimeUpdateTask;
import org.santsang.schedule.ScheduleHelper;
import org.satsang.audit.AuditConstants;
import org.satsang.bo.MediaSchedule;
import org.satsang.bo.Singleton;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.satsang.util.CommonUtil;
import org.satsang.util.ConfigUtil;
import org.satsang.util.DateUtil;
import org.satsang.web.WebAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

/** This class runs at schedule interval and check for activities/tasks that need to be performed
 * Using task keys each of the task invocation time interval is checked and that task is executed.
 * Some of the tasks are update date time using configuration, marquee, apk update, upload errorLog, upload audit and update data,
 * If server update date is > than local update date then data is to be fetched for config, marquee, apk 
 * Auth, NTP date time update will be called after every 12 and 4 hours.
 * @author SG
 *
 */
public class ScheduleReceiver extends BroadcastReceiver{
	
	static private final Logger Log = LoggerFactory.getLogger(ScheduleReceiver.class);
	//TODO: Add weekly clean up files based on reverse lookup...call ScheduleHelper.cleanUpFiles
	
	/*private final long REFRESH_INTERVAL = 1000*60*15;//MILLIS*SECONDS*MINUTES
	private static long AUTH_REFRESH_INTERVAL = 1000*60*60*12; //12 HOURS REFRESH
	private static long NTP_REFRESH_INTERVAL = 1000*60*60*6; //6 HOURS REFRESH
	private static long LAST_REFRESH = 0; //Last refresh timestamp
*/	
	private final long REFRESH_INTERVAL = 1000*60*15;//MILLIS*SECONDS*MINUTES
	private static long AUTH_REFRESH_INTERVAL = 1000*60*60; //10 sec refresh
	private static long NTP_REFRESH_INTERVAL = 1000*60*30; //5 sec REFRESH
	private static long LAST_NTP_REFRESH = 0; //Last NTP refresh timestamp
	private static long LAST_AUTH_REFRESH=0; //Last Auth refresh
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.trace("onReceive: "+ DateUtil.getServerFormatCurrentDateTimeString());
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TEST");
		wl.acquire();
		new ExecuteSchedules(context).execute();
		wl.release();	
	}
	
	public void setAlarm(Context context){
		Log.info("ScheduleReceiver Alarm set: "+ DateUtil.getServerFormatCurrentDateTimeString());
		AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ScheduleReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), REFRESH_INTERVAL, pi);
        new ExecuteSchedules(context).execute();
		
	}
	
	public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, ScheduleReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
	
	private class ExecuteSchedules extends AsyncTask<Void, Void, String[]> {
		private Context context;
		ExecuteSchedules(Context context){
			this.context=context;
		}
		
		protected String[] doInBackground(Void... v) {
			Log.trace("ExecuteSchedules:doInBackground called");
			CommonUtil.loadConfiguration(context);
			//check if current time is 8:45 then call play video OR IF PLAY WINDOW IS DISPLAYED THEN DON'T LAUNCH DOWNLOAD
			boolean isDownloadInProgress = Singleton.getInstance().isDownloadInProgress();
			
			SharedPreferences pref = context.getSharedPreferences("schedule.tasks", Context.MODE_PRIVATE);
			Log.info("isDownloadInProgress? " + isDownloadInProgress);
			try {
				String sevaKendraStatus = CommonUtil.checkSevaKendraStatus(context);
				Log.info("sevaKendra Status is " + sevaKendraStatus);
				if("active".equalsIgnoreCase(sevaKendraStatus)) {
					boolean isPlayDay = ConfigUtil.checkScheduleFromConfiguration();
					int playWindow = ConfigUtil.getPlayWindow();
					if(isPlayDay) {
						Log.info("play window: " + playWindow + " current time: " + new Date());
						/*if(playWindow != -1) {
							Log.info("Inside play window");
							//Start Santsang? Let user launch santsang 
							Intent santsang = new Intent(context, SantsangActivity.class);
							santsang.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							santsang.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							santsang.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							context.startActivity(santsang);
						//TODO: check if internet is available and if download pending then only call DownloadActivity	
						}else*/ if(playWindow == -1 && !isDownloadInProgress) {
							Log.info("Download not in progress");
							
							refreshTasks();
							//relaunch DownloadActivity
							Intent download = new Intent(context, DownloadActivity.class);
							//TODO: bring download activity to foreground
							download.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							download.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							download.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							context.startActivity(download);
						}
					} else if(!isDownloadInProgress) {
						Log.info("Not play day and download not in progress");
						//TODO: Open close connections assuming no db activity is in process to avoid db connection leakage 
						// By Rohit - No Need to close Open Connections as new Database Manager added will take care of that
						
						//check force update or soft update is needed or not
						int apkForceUpdate = pref.getInt("schedule.tasks.apkUpdate", 0);
						boolean apkDownloadStatus = pref.getBoolean("schedule.tasks.apkUpdate.isDownloaded", false);
						if(apkForceUpdate==2 || apkForceUpdate == 1) {
							Intent apkUpdate = new Intent(context, APKUpdateActiviy.class);
							apkUpdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							apkUpdate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							apkUpdate.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							apkUpdate.putExtra("forceUpdate", true);
							context.startActivity(apkUpdate);
						} else if (apkForceUpdate == 1 && apkDownloadStatus ) {
							//silently install and exit
							Log.debug("Installing silently in background");
							final String libs = "export LD_LIBRARY_PATH=/vendor/lib:/system/lib";
							String[] cmd = { libs, "pm install -r /mnt/sdcard/live" };
							CommonUtil.execute_as_root(cmd);
						} else {
							refreshTasks();
							Intent download = new Intent(context, DownloadActivity.class);
							download.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							download.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							download.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							context.startActivity(download);
						}
						
					}
				}
				
			}catch(Exception e) {
				Log.error("No play configuration found: " + e);
			}
			
			
			
			
			return null;
		}
		
		// By Rohit - To Clean Up Videos from DB
		private void tasksAfterSantsangFinished()
		{
			Log.info("Calling tasksAfterSantsangFinished()");
			int diffInMinutes = ConfigUtil.getScheduleTimeFromConfiguration();
			if(diffInMinutes == Constants.LANG_CODE_CHANGE_PURGE_TASK_DIFF_IN_MIN)
			{
				ScheduleHelper helper = new ScheduleHelper();
				DBAdapter dbAdapter = new DBAdapter(context);
				// Clean Up Media Except Bhajans
				dbAdapter.cleanUpSchedules();
				helper.cleanUpFiles(dbAdapter.getAllSchedulesToCleanUp(), ConfigurationLive.getValue("local.media.files.directory"));
				// Fallback Method - Clean Up Bhajans
				//Below Query will fetch all the records Sorted by Version Desc
				ArrayList<MediaSchedule> oldVersionBhajans = dbAdapter.getOldVersionBhajansFromDb();
				// So If above list contains more than One Record It means There are Bhajan files of less version needs to be deleted
				if(oldVersionBhajans.size() > 1)
				{
					// This will return latest version Media Schedule Record as Query is sorted by Version Desc
					MediaSchedule mediaSchedule = oldVersionBhajans.get(0);
					if("complete".equalsIgnoreCase(mediaSchedule.getCheckSumStatus()) && "complete".equalsIgnoreCase(mediaSchedule.getDownloadStatus()))
					{
						String downloadDir = ConfigurationLive.getValue("bhajan.download.directory");
						String fileName = mediaSchedule.getFileName();
						CommonUtil.deleteFiles(downloadDir,fileName);
						dbAdapter.deleteOldVersionBhajansFromDb(fileName);
					}
				}
			}
			Log.info("After tasksAfterSantsangFinished()");
		}
		
		private void refreshTasks() {
			//TODO: move below code to method and call it before Download activity is invoked so download get refreshed data.
			//TODO: testing pending.. check elapsed interval.
			Log.info("Calling refreshTasks()");
			// By Rohit - To Clean Up Videos from DB
			tasksAfterSantsangFinished();
			long elapTime = SystemClock.elapsedRealtime();
			long ntpDiff = elapTime - LAST_NTP_REFRESH;
			long authDiff = elapTime - LAST_AUTH_REFRESH;
			SharedPreferences pref = context.getSharedPreferences("schedule.tasks", Context.MODE_PRIVATE);
			
			if(ntpDiff >= NTP_REFRESH_INTERVAL) {
				//call NTP date time update task
				if(Constants.MODE_IS_LIVE) {
					Log.info(DateUtil.getServerFormatCurrentDateTimeString() + " : NTP Refresh being called");
					new DateTimeUpdateTask(context).execute();	
				}
				LAST_NTP_REFRESH = elapTime;
			}
			if(authDiff >= AUTH_REFRESH_INTERVAL) {
				//call auth service, upload error log, upload audit data. 
				Log.info(DateUtil.getServerFormatCurrentDateTimeString() + " : AUTH Refresh being called");
				LAST_AUTH_REFRESH=elapTime;
				
				WebAdapter webAdp = new WebAdapter(context);
				webAdp.synchronize();
				//upload events
				webAdp.uploadEvents(AuditConstants.AUDIT_TYPE_AUDIT);
				webAdp.uploadEvents(AuditConstants.AUDIT_TYPE_ERROR);
				
				//check configuration download
				try {
					String confSrvDate = pref.getString("schedule.tasks.configServer", "");
					String confLocalDate = pref.getString("schedule.tasks.configLocal", "");
					Log.trace("confSrvDate: " + confSrvDate + " confLocalDate: " + confLocalDate);
					if(confLocalDate != null && !"".equalsIgnoreCase(confLocalDate)) {
						long diff = DateUtil.compareDateTime(confLocalDate, confSrvDate);
						if(diff <= 0) {
							// Invoke configuration update task
							webAdp.downloadConfiguration();
						}	
					}else webAdp.downloadConfiguration(); //if null value for local conf date then download
				}catch(Exception e) {
					Log.error("Exception in configuration"+e);
				}
				
				//check marquee download
				try {
					String marqSrvDate = pref.getString("schedule.tasks.marqServer", "");
					String marqLocalDate = pref.getString("schedule.tasks.marqLocal", "");
					Log.trace("Marquee: " + marqSrvDate + " " + marqLocalDate);
					if(marqLocalDate != null && !"".equalsIgnoreCase(marqLocalDate)) {
						long diff = DateUtil.compareDateTime(marqLocalDate, marqSrvDate);
						if(diff <= 0) {
							// Invoke marquee update task
							webAdp.downloadMarquee();
						}	
					}else webAdp.downloadMarquee();
				}catch(Exception e) {
					Log.error("Exception in marquee check " + e);
				}
				
				//TODO: deferred: if apkUpdate value is 1 then download in background. Once download is complete then call install
				//check apk update
				try {
					//below value is used to identify if new version is available on server using auth service.
					int apkValue = pref.getInt("schedule.tasks.auth.apkVersion", 0); 
					String apkToken = pref.getString("schedule.tasks.apkUpdate.download.token", null);
					boolean apkDownloadStatus = pref.getBoolean("schedule.tasks.apkUpdate.isDownloaded", false);
					//if apkValue = 0 no download, 1=update available and 2=force update
					if(apkValue > 0) {
						Log.debug("apkValue:" + apkValue);
						if(apkToken != null) {
							Log.debug("apkToken not null");
							if(!apkDownloadStatus) {
								Log.debug("apkDownloadStatus:" + apkDownloadStatus);
								new APKDownloadTask(context, false).execute();
							}
						}else {
							//download token
							Log.debug("apkToken is null");
							webAdp.downloadAPKUpdateInfo();
						}
						webAdp.downloadAPKUpdateInfo();
					}	
				}catch(Exception e) {
					Log.error("Exception in apk update " + e);
				}
				
				
			}
		}
		
		
	}

}
