package org.satsang.live.config;

public class Constants {

	public static String configFile = "liveSatsang.conf";
	public static final int DEFAULT_FTP_TIMEOUT_INTERVAL = 3000;
	// Max video length in minutes
	public static final int DEFAULT_MAX_VIDEO_LENGTH_MINS = 60;
	// Max video length in milliseconds
	public static final int DEFAULT_MAX_VIDEO_LENGTH_MILLISEC = 3600000;
	// Sleep time for dialer 10 SEC
	public static final int DIALER_SLEEP_TIME = 10000;
	// Default purging interval in days
	public static final int DEFAULT_PURGE_INTERVAL_IN_DAYS = 45;
	// Default URL connection timeout in milliseconds
	public static final int DEFAULT_URL_CONNECTION_TIMEOUT = 1000;
	public static final int DEFAULT_URL_READ_TIMEOUT = 1000;
//	public static final String SATSANG_FILE="/mnt/sdcard/livesatsang/local/";
	
	//Mantra constants
	public static final long MANTRA_AUDIO_LENGTH=30*60*1000; // milli seconds
	public static final long MANTRA_CLIP_LENGTH=14420l; //length of audio clip to loop milli seconds
	
	//Marquee types
	public static final String MARQUEE_TYPE_PRAVACHAN="P";
	public static final String MARQUEE_TYPE_NEWS="N";
	
	//TODO: change to true after testing. Also this 
	public static final boolean MODE_IS_LIVE=false;
	
	//Keys used for schedule updates
	public static final String CONF_UPDATE="CONF_UPDATE";
	public static final String APK_UPDATE="APK_UPDATE";
	public static final String MARQUEE_UPDATE="MARQUEE_UPDATE";	
	
	public static final int APK_NO_UPDATE=0;
	public static final int APK_UPDATE_AVAILABLE=1;
	public static final int APK_FORCE_UPDATE=2;
	
	public static final String BHAJAN="B";
	public static final String PRAVACHAN="P";
	public static final String NEWS="N";
	public static final String MANTRA="M";
	
	public static final int CHECKSUM_RETRY_COUNT=2;
	public static final int MAX_PRAVACHAN_SCHEDULE_FILES=4;
	
	
	//Constants for FTP download statuses
	
	public static final int FTP_DOWNLOAD_COMPLETE=200;
	public static final int NO_SCHEDULE_FOUND=1;
	public static final int INCOMPLETE_SCHEDULE=2;
	public static final int FTP_DOWNLOAD_PENDING=3;
	public static final int FILE_MIGHT_NOT_FOUND_ON_SERVER =4;
	public static final int SOCKET_ERROR=5;
	public static final int SOCKET_TIMEOUT_ERROR=6;
	public static final int IO_EXCEPTION =7;
	public static final int FTP_CONNECTION_CLOSED=8;
	public static final int OTHER_FTP_EXCEPTION =9;
	public static final int INTERNET_CONNECTIVITY_UNAVAILABLE=10;
	public static final int DOWNLOAD_TASK_EXCEPTION=11;
	public static final int FILE_CHECKSUM_FAIL = 12;
	public static final int FTP_RETRIES_FINISHED = 13;
	public static final int LANG_CODE_CHANGE_PURGE_TASK_DIFF_IN_MIN = 65;
	
	//TODO: deferred
	/* Move shared preferences keys and parameters passed between activities here.
	 * Shared preferences: a) all scheduled tasks are stored in schedule.tasks.xx shared preference.
	 * Each subtask is stored with suffix schedule.tasks.configServer, schedule.tasks.marqServer, schedule.tasks.apkUpdate etc
	 * b) apk download details are stored in schedule.tasks.apkUdpate.xx.
	 * Sub details of apkUpdate are stored with suffix schedule.tasks.apkUpdate.url, schedule.tasks.apkUpdate.token etc*/
	
	

}
