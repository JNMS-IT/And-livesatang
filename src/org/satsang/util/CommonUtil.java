package org.satsang.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.android.activities.R;
import org.satsang.bo.MediaSchedule;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;

public class CommonUtil {
	static private final Logger Log = LoggerFactory.getLogger(CommonUtil.class);

	public static String getMacId(Context context){
		String macId = ConfigurationLive.getValue("live.satsang.macID");
		if(macId == null){
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			macId = tm.getDeviceId();
		}
		return macId;
	}
	
	public static int getLocalVersion(Context context){
		int version = 0;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			Log.error("Package info retrieval exception:" + e.getMessage());
		} catch (Exception e) {
			Log.error("Exception getLocalVerions: " + e);
		}
		return version;
	}
	
	/*
	 * This method checks shared preferences. If status is not set or is null it
	 * means status is active (as we don't need to update the status for each
	 * server call.
	 */
	public static String checkSevaKendraStatus(Context context) {
		String status;
		try {
			SharedPreferences prefs = context.getSharedPreferences("org.sevakendra", Context.MODE_PRIVATE);
			status = prefs.getString("org.sevakendra.status", "");
			if (status == null || "".equals(status) || "active".equalsIgnoreCase(status)) {
				return "active";
			} else {
				return "inactive";
			}
		} catch (Exception e) {
			Log.error("Error checking sevakendra status");
		}

		return "active";

	}
	
	/* Force data connection on
	 * 
	 */
	public static boolean turnDataON(Context context) {
		Log.info("turning on data connection");
		final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		try {
			final Class<?> conmanClass = Class.forName(conman.getClass().getName());
			final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField.get(conman);
			final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod(
					"setMobileDataEnabled", Boolean.TYPE);
			Log.info("setting mobile data enabled method to true");
			setMobileDataEnabledMethod.setAccessible(true);
			setMobileDataEnabledMethod.invoke(iConnectivityManager, true);
			System.out.println("setting data connection on done.....");
			return true;
		} catch (Exception e) {
			Log.error("Exception:" + e);
		}
		return false;
		
	}
	
	/* Check if airplane mode is on? */
	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(), Global.AIRPLANE_MODE_ON, 0) != 0;

	}
	
	public static boolean isSIMPresent(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
		if(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
			return false;
		}else return true;
	}
	/*
	 * This method checks SIM status and network type and returns true is sim
	 * and network is identified. Not sure if SIM_STATE_NETWORK_LOCKED state
	 * should also be checked? we can also check if getNetworkOperator is not
	 * blank/null
	 */
	public static boolean hasLatchedToNetwork(Context context) {

		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		// System.out.println("1:" + tm.getSimState() + " n/w type" +
		// tm.getNetworkType() + " operator code" + tm.getNetworkOperator() +
		// " ?" + tm.getNetworkOperatorName() + " number:" +
		// tm.getLine1Number());
		int networkType = tm.getNetworkType(); // 0 if network type is unknown
		if (tm.getSimState() == TelephonyManager.SIM_STATE_UNKNOWN
				|| networkType == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
			return false;
		} else {
			// Settings.System.putInt(context.getContentResolver(),
			// Settings.System.AUTO_TIME, 1);// Resetting time to AutoTime
			return true;
		}
	}
	
	/** Internet connection check */
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			return true;
		}else if(wifi.isAvailable()) {
			return true;
		}else {
			return false;
		}
	}
	
	/** Test data connection by calling url
	 * 
	 */
	public static boolean testConnection() {
		try {
			// make a URL to a known source
			URL url = new URL("http://www.google.com");
			// open a connection to that source
			HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
			urlConnect.setConnectTimeout(800);
			Object objData = urlConnect.getContent();
			objData.toString();
			return true;
			// urlConnect.getContentLength();
			
		} catch (Exception e) {
			
		}
		return false;

	}
	
	public static MediaSchedule getDefaultBhajan() {
		MediaSchedule schedule = new MediaSchedule();
		String defaultBhajanFile = ConfigurationLive.getValue("bhajan.default.file");
		if(defaultBhajanFile != null && (!"".equalsIgnoreCase(defaultBhajanFile))) {
			schedule.setFileName(defaultBhajanFile);
		}else{
			schedule.setFileName("/mnt/sdcard/livesatsang/local/satsang1.mp4");	
		}
		
		schedule.setVideoLength(1797);
		schedule.setDownloadStatus("complete");
		schedule.setScheduleType(Constants.BHAJAN);
		return schedule;
	}
	
	public static MediaSchedule getDefaultPravachan() {
		MediaSchedule schedule = new MediaSchedule();
		schedule.setFileName("mantra/mantra.mp3");
		schedule.setVideoLength(1800);
		schedule.setDownloadStatus("complete");
		schedule.setScheduleType(Constants.PRAVACHAN);
		return schedule;
	}
	
	public static void execute_as_root(String[] commands) {
		Log.debug("execute_as_root() commands: "+ commands);
		try {
			Process p = Runtime.getRuntime().exec("su");
			InputStream es = p.getErrorStream();
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			for (String command : commands) {
				os.writeBytes(command + "\n");
			}
			os.writeBytes("exit\n");
			os.flush();
			os.close();

			int read;
			byte[] buffer = new byte[4096];
			String output = new String();
			while ((read = es.read(buffer)) > 0) {
				output += new String(buffer, 0, read);
			}

			p.waitFor();
			Log.debug(output.trim() + " (" + p.exitValue() + ")");
			/*
			 * } catch (IOException e) { System.out.println("IOException:" +
			 * e.getMessage()); } catch (InterruptedException e) {
			 * System.out.println("InterruptedException:" + e.getMessage());
			 */
		} catch (Exception e) {
			Log.error("Exception:" + e);
		}
	}
	
	public static void loadConfiguration(Context context) {
		Log.trace("Entering loadConfiguration(Context context)");
		ConfigurationLive.readConfigFile();
		HashMap<String,String> config = new HashMap<String, String>();
		
		String ftpServer = context.getResources().getString(R.string.media_satsangFiles_hostname);
		String ftpPort = context.getResources().getString(R.string.media_satsangFiles_port);
		String ftpUser = context.getResources().getString(R.string.ftp_server_username);
		String ftpPass = context.getResources().getString(R.string.ftp_server_password);
		
		Log.trace("Loading configuration from xml");
		config.put("media.satsangFiles.hostname", ftpServer);
		config.put("media.satsangFiles.port",ftpPort);
		config.put("ftp.server.username", ftpUser);
		config.put("ftp.server.password", ftpPass);
		//New properties not present on sdcard on deployed v3 tabs
		Log.trace("Loading v4 additional properties");
		//TODO:Final release change following values
		config.put("live.satsang.schedule.url", "http://124.124.83.147:8080/live_santsang/auth");
		config.put("live.satsang.upload.url", "http://124.124.83.147:8080/live_santsang/log");
		config.put("live.satsang.configuration.url", "http://124.124.83.147:8080/live_santsang/data");
		config.put("live.satsang.apkToken.url", "http://124.124.83.147:8080/live_santsang/update");
		config.put("live.satsang.marquee.url", "http://124.124.83.147:8080/live_santsang/getMarquee");
		config.put("news.download.directory", "/sdcard/livesatsang/news/");
		config.put("satsang.error.log.fileName", "/sdcard/livesatsang/schedule/error.dat");
		config.put("live.satsang.play.start", "08:45");
		config.put("live.satsang.play.duration", "180");
		config.put("bhajan.default.file", "/mnt/sdcard/livesatsang/local/satsang1.mp4");
		config.put("bhajan.download.directory", "/mnt/sdcard/livesatsang/bhajan/");
		
		ConfigurationLive.addValues(config);
		Log.trace("Loading configuration from database");
		try {
			DBAdapter dbAdp = new DBAdapter(context);
			HashMap<String,String> dbConfig = dbAdp.getConfigurationFromDatabase();	
			ConfigurationLive.addValues(dbConfig);	
		}catch (Exception e) {
			Log.error("Exception reading configuration from database " + e);
		}
		
		Log.debug("Loaded config from all sources");
	}
	
	public static void deleteFile(String fileName) {
		System.gc();
		File file = new File(fileName);
		file.delete();
	}
	
	public static void deleteFiles(String directory,String fileNotToDelete)
	{
		File file = new File(directory);
        File[] files = file.listFiles();
        // By Rohit - As Delete a File code is already there below,what I saw from my projects
     	// is sometimes file is not deleted from system, may be because there is some unused handle associated with it
        // So its always better to call System.gc() before it
        System.gc();
        for (File f:files) 
        {if (f.isFile() && f.exists() && f.getName() != null && ! "".equals(f.getName()) && ! f.getName().equalsIgnoreCase(fileNotToDelete)) 
            { f.delete();
            }
        }
	}
}
