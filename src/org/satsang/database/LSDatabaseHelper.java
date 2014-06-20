package org.satsang.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class LSDatabaseHelper extends SQLiteOpenHelper {
	static private final Logger Log = LoggerFactory.getLogger(LSDatabaseHelper.class);
	
	private static final String DATABASE_NAME = "/mnt/sdcard/livesatsang/livesatsang.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String CONFIG = "Create Table IF NOT EXISTS Config(" +
			"key TEXT PRIMARY KEY, " +
			"value TEXT NOT NULL, " +
			"updateDateTime TEXT " +
			")";
	
	
		
	private static final String UPDATE_SCHEDULES = "Create Table IF NOT EXISTS Update_Schedules(" +
			"scheduleKey TEXT PRIMARY KEY UNIQUE, " +
			"value INTEGER, " + //0=no update, 1=update available, 2=force update 
			"serverUpdateDateTime TEXT, " +
			"localUpdateDateTime TEXT, " +
			"localUpdateStatus TEXT " +
			")";
	
	private static final String MEDIA_SCHEDULES = "Create Table IF NOT EXISTS Media_Schedules(" +
			"scheduleDate TEXT NOT NULL, " +
			"fileName TEXT PRIMARY KEY UNIQUE, " +
			"scheduleTime TEXT NOT NULL, " +
			"videoLength INTEGER, " +
			"fileSize INTEGER NOT NULL, " +
			"checkSum TEXT NOT NULL, " +
			"langCode TEXT, " +
			"serverPath TEXT NOT NULL, " +
			"lastModifiedDateTime TEXT NOT NULL, " +
			"scheduleType TEXT NOT NULL, " + //B=Bhajan, P=Pravachan, N=News
			"mediaType TEXT NOT NULL, " + //A=Audio, V=Video
			"downloadStatus TEXT, " +
			"mediaPlayStatus TEXT, " +
			"checkSumStatus TEXT, " +
			"downloadedBytes INTEGER, " +
			"checkSumRetryCount INTEGER, " +
			"version TEXT " +
			")";
	
	private static final String BHAJAN = "Create Table IF NOT EXISTS Bhajan(" +
			"fileName TEXT NOT NULL UNIQUE, " +
			"videoLength INTEGER NOT NULL, " +
			"fileSize INTEGER NOT NULL, " +
			"checkSum TEXT NOT NULL, " +
			"serverPath TEXT NOT NULL, " +
			"version INTEGER NOT NULL, " +
			"checkSumStatus TEXT, " +
			"downloadStatus TEXT, " +
			"retryCount INTEGER, " +
			"playStatus TEXT " +
			")";
	
	private static final String NEWS = "Create Table IF NOT EXISTS News(" +
			"fileName TEXT NOT NULL UNIQUE, " +
			"videoLength INTEGER NOT NULL, " +
			"fileSize INTEGER NOT NULL, " +
			"checkSum TEXT NOT NULL, " +
			"serverPath TEXT NOT NULL, " +
			"checkSumStatus TEXT, " +
			"downloadStatus TEXT " +
			"retryCount INTEGER, " +
			"playStatus TEXT " +
			")";
	
	private static final String ERROR_LOG = "Create Table IF NOT EXISTS Error_Log(" +
			"errorDateTime TEXT NOT NULL, " +
			"errorCode TEXT NOT NULL, " +
			"errorDescription TEXT " +
			")";
	
	private static final String MARQUEE = "CREATE TABLE IF NOT EXISTS Marquee (msg_id integer primary key, msg_txt text, act_dt text, " 
			+ "exp_dt text, rnw_dt text, msg_spd text, msg_rot text, msg_prt text, lang_cd text, txt_siz text, is_act text, msg_type text);";
	
	public LSDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, DATABASE_NAME, null, version);
	}

	public LSDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.info("Creating database tables");
		try {
			db.execSQL(CONFIG);
			db.execSQL(UPDATE_SCHEDULES);
			db.execSQL(MEDIA_SCHEDULES);
			db.execSQL(ERROR_LOG);
			db.execSQL(BHAJAN);
			db.execSQL(NEWS);
			db.execSQL(MARQUEE);
		}catch(Exception e){
			Log.error("failed to create database"+e);
		}
		
	}

	
	@Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.debug("Upgrading database from version " + oldVersion + " to "+ newVersion);
	    try {
	    	db.execSQL("DROP TABLE IF EXISTS " + CONFIG);
		    db.execSQL("DROP TABLE IF EXISTS " + UPDATE_SCHEDULES);
		    db.execSQL("DROP TABLE IF EXISTS " + MEDIA_SCHEDULES);
		    db.execSQL("DROP TABLE IF EXISTS " + ERROR_LOG);
		    db.execSQL("DROP TABLE IF EXISTS " + BHAJAN);
		    db.execSQL("DROP TABLE IF EXISTS " + NEWS);
		    db.execSQL("DROP TABLE IF EXISTS " + MARQUEE);
		    onCreate(db);
	    }catch(Exception e){
	    	Log.error("failed to upgrade database"+e);
	    }
	  }

}
