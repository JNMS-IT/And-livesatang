package org.satsang.ftp;

import java.io.File;
import java.util.List;

import org.android.activities.DownloadActivity;
import org.satsang.bo.MediaSchedule;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.satsang.util.CommonUtil;
import org.satsang.util.MD5Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DownloadManager {
	static private final Logger Log = LoggerFactory.getLogger(DownloadManager.class);
	
	
	private DownloadActivity downloadActivity = null;
	private int ftpRetries = 0;

	public DownloadManager(DownloadActivity downloadActivity) {
		this.downloadActivity = downloadActivity;
	}

	public boolean downloadMediaFiles(List<MediaSchedule> scheduleList) {
		Log.info("Inside download manager downloadMediaFiles");
		try {
			ftpRetries = getNumberOfRetries(); // TODO: retrieve from
												// configuration
			for (int i = 0; i < scheduleList.size(); i++) {
				int downloadstatus = downloadFiles(scheduleList.get(i));
				if (downloadstatus != Constants.FTP_DOWNLOAD_COMPLETE) {
					return false;
				}
			}
		} catch (Exception e) {
			Log.error("exception:" + e.getMessage(), e);
		}
		Log.info("Exiting:DownloadManager.downloadMediaFiles()");
		return false;
	}

	public int downloadFiles(MediaSchedule schedule) {
		Log.info("Entering downloadFiles(MediaSchedule schedule)");
		int i = 0;
		ftpRetries = getNumberOfRetries();
		int downloadStatus=Constants.FTP_RETRIES_FINISHED;
		while (i < ftpRetries) {
			if (!isOnline()) {
				return Constants.INTERNET_CONNECTIVITY_UNAVAILABLE;
			}
			// start download
			downloadStatus = downloadFile(schedule);
			String fileName = schedule.getFileName();
			if (downloadStatus == Constants.FTP_DOWNLOAD_COMPLETE) {
				
				Log.debug("Returning DOWNLOAD_COMPLETE for: " + fileName);
				// By Rohit - Delete Old Version Bhajans From DB if New one is Downloaded
				if(Constants.BHAJAN.equalsIgnoreCase(schedule.getScheduleType())) {
				Log.debug("Cleaning up the Old Bhajans from DB and File System");	
				DBAdapter dBAdapter = new DBAdapter();
				String downloadDir = ConfigurationLive.getValue("bhajan.download.directory");
				CommonUtil.deleteFiles(downloadDir,fileName);
				dBAdapter.deleteOldVersionBhajansFromDb(fileName);
	        	}
				return Constants.FTP_DOWNLOAD_COMPLETE;
			} else {
				Log.debug("Retrying download for: " + fileName + " count:" + i);
				try {
					Thread.sleep(getRetryInterval() * 1000);
				} catch (Exception e) {
					Log.error("Error while sleep between ftp retries");
				}
				i++;
			}

		}// end of while retries
		return downloadStatus;
	}

	private int downloadFile(MediaSchedule schedule) {
		Log.info("Entering downloadFile(schedule)");
        int returnValue;
		String downloadDir = "";
		if(Constants.BHAJAN.equalsIgnoreCase(schedule.getScheduleType())) {
			downloadDir = ConfigurationLive.getValue("bhajan.download.directory");
		}else {
			downloadDir = ConfigurationLive.getValue("local.media.files.directory"); 
		}
				
		File downloadedFile = new File(downloadDir + schedule.getFileName());
		long bytesDownloaded = 0;
		if (downloadedFile.exists() && downloadedFile.isFile()) {
			bytesDownloaded = downloadedFile.length();
		}
		Log.debug("file: " + schedule.getFileName() + " local size:" + bytesDownloaded + " schedule size:"
				+ schedule.getFileSize());
		if (bytesDownloaded < schedule.getFileSize()) {
				FTPDownloadWorker ftp = new FTPDownloadWorker(downloadActivity);
				returnValue = ftp.download(schedule.getFileName(), schedule.getServerPath(), downloadDir + schedule.getFileName(),
						bytesDownloaded);
				return returnValue;
		} else {
			String localFileChecksum = MD5Checksum.checkSum(downloadDir+schedule.getFileName());
			Log.debug("localFileChecksum: "+localFileChecksum + " Server checksum: " + schedule.getCheckSum());
			if(localFileChecksum != null && schedule.getCheckSum().equalsIgnoreCase(localFileChecksum)) {
				return Constants.FTP_DOWNLOAD_COMPLETE;	
			}else {
				return Constants.FILE_CHECKSUM_FAIL;
			}
			
		}

	}

	/** Internet connection check */
	private Boolean isOnline() {
		Log.info("Entering isOnline()");
		ConnectivityManager cm = (ConnectivityManager) downloadActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		int sleepInterval = sleepInterval();
		if (ni != null && ni.isConnected()) {
			return true;
		}else if(wifi.isAvailable()) {
			return true;
		}else {
			int i = 0;
			while (i < internetRetries()) {
				try {
					Log.debug("isOnline Sleep");
					Thread.sleep(sleepInterval * 1000);
					if (ni.isConnected()) {
						return true;
					}
				} catch (Exception e) {
					Log.error("Error in sleep while connecting to internet" + e.getMessage());
				}
				i++;
			}
			return false;
		}
	}

	/* Returns sleep interval in seconds */
	private int sleepInterval() {
		try {
			String sleep = ConfigurationLive.getValue("internet.connection.retry.sleep");
			return Integer.valueOf(sleep).intValue();
		} catch (Exception e) {
			Log.error("Error retrieving sleep interval" + e.getMessage());
		}
		return 1; // Return default 1 second
	}

	/* Returns number of retry pauses if internet is not connected */
	private int internetRetries() {
		try {
			String retries = ConfigurationLive.getValue("internet.connection.retry.attempts");
			return Integer.valueOf(retries).intValue();
		} catch (Exception e) {
			Log.error("Error retrieving retry attempts" + e.getMessage());
		}
		return 0;

	}

	/* Returns number of FTP download retries before giving up FTP download */
	private int getNumberOfRetries() {
		try {
			int retry = Integer.valueOf(ConfigurationLive.getValue("ftp.server.retry.attempts")).intValue();
			return retry;
		} catch (Exception e) {
			Log.error("Exception:" + e.getMessage(), e);
		}
		return 3;
	}

	/* Returns pause between two FTP retries */
	private int getRetryInterval() {
		try {
			int retryInterval = Integer.valueOf(ConfigurationLive.getValue("ftp.server.retry.interval")).intValue();
			return retryInterval;
		} catch (Exception e) {
			Log.error("Exception:" + e.getMessage(), e);
		}
		return 0;
	}

}
