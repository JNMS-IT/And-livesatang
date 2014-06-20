package org.satsang.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.android.activities.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satsang.bo.MediaSchedule;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.satsang.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class DBAdapter {
	static private final Logger Log = LoggerFactory.getLogger(DBAdapter.class);

	
	private Context context;
	/* Table names */
	public static final String CONFIG_TABLE = "Config";
	public static final String MEDIA_SCHEDULES_TABLE = "Media_Schedules";
	public DBAdapter()
	{
		
	}
	public DBAdapter(Context context) {
		this.context = context;
	}

	
	public HashMap<String,String> getConfigurationFromDatabase(){
		Log.info("Entering getConfigurationFromDatabase()");
		HashMap<String,String> map = new HashMap<String,String>();
		String sql = "SELECT * from "+ CONFIG_TABLE;
		Cursor mtCursor = null;
		
		try {
			SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
			mtCursor = database.rawQuery(sql, null);
				if(mtCursor.getCount() > 0) {
					mtCursor.moveToFirst();
					do {
						map.put(mtCursor.getString(mtCursor.getColumnIndex("key")), mtCursor.getString(mtCursor.getColumnIndex("value")));	
					}while(mtCursor.moveToNext());
					Log.debug("Returning configuration map");
					return map;
				}
		}catch(Exception e){
			Log.error("getConfigurationFromDatabase"+e);
		}finally {
			if(mtCursor != null) mtCursor.close();
			DatabaseManager.getInstance().closeDatabase();
		}
		return null;
	}
	

	public void insertConfigurationFromServer(JSONArray msgList){
		Log.info("Entering insertConfigurationFromServer()");
		String sql = "Insert or Replace into " + CONFIG_TABLE + " (key, value, updateDateTime) values (?,?,?)";
		SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
	    SQLiteStatement stmt = database.compileStatement(sql);
		    try {
		    	database.beginTransaction();
		    	for(int i=0;i<msgList.length();i++){
		        	JSONObject message = msgList.getJSONObject(i);
		    				stmt.clearBindings();
		    				stmt.bindString(1,message.getString("key"));
		    				stmt.bindString(2,message.getString("value")); 
		    				stmt.bindString(3, DateUtil.getFormattedCurrentDate().toString());
		    				Log.debug("inserted configuration from server: " + message.getString("key"));
		    				stmt.execute();
		        }
		    }catch (JSONException jse) {
		    	Log.error("jse storing configuration from server:" + jse);
		    }catch(Exception e) {
		    	Log.error("exception storing configuration from server: " + e);
		    }finally {
		    	if(database != null) {
		    		database.setTransactionSuccessful();
			        database.endTransaction();
		    	}
				DatabaseManager.getInstance().closeDatabase();
		    }
		}
	
	public void updateConfiguration(String key, String value) {
		/*ContentValues cv = new ContentValues();
		cv.put("key", key);
		cv.put("value", value);
		try {
			cv.put("updateDateTime", DateUtil.getFormattedCurrentDate().toString());	
		}catch(Exception e){
			
		}
		database.update("Config", cv, "key="+key, null);*/
		Log.info("Entering updateConfiguration()");
		String sql = "Insert or Replace into Config (key,value,updateDateTime) values (?,?,?)";
		SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
		try {
			SQLiteStatement stmt = database.compileStatement(sql);
			database.beginTransaction();
			stmt.bindString(1, key);
			stmt.bindString(2, value);
			stmt.bindString(3, DateUtil.getFormattedCurrentDateString());
			stmt.execute();
		}catch(Exception e) {
			Log.error("Error updating configuration" + e);
		}finally {
	    	if(database != null) {
	    		database.setTransactionSuccessful();
		        database.endTransaction();
	    	}
			DatabaseManager.getInstance().closeDatabase();
	    }
	}
	
	// Deletes the Records From Table As per Query Passed to it.
	private void deleteRecordsAsPerQuery(String deleteSql)
	{
		 Log.info("Inside deleteRecordsAsPerQuery Method");
		 SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
		 database.execSQL(deleteSql);
		 DatabaseManager.getInstance().closeDatabase();
		 Log.info("After sqlDelete:" + deleteSql);
		 Log.info("Existing deleteRecordsAsPerQuery Method");
	}
	
	public void deleteOldVersionBhajansFromDb(String latestBhajanFile)
	{
		 Log.info("Deleting Old Version Bhajans From DB");
		 String delete = "DELETE FROM "+ MEDIA_SCHEDULES_TABLE+ " WHERE  scheduleType=\'"+Constants.BHAJAN + "\' and fileName <>\'"+latestBhajanFile + "\';";
		 deleteRecordsAsPerQuery(delete);
		 Log.debug("After Deleting Old Version Bhajans From DB");
	}
	
	/* Synchronize schedule with server */
	public void synchronize(JSONArray msgList){
		Log.info("Entering synchronize(JSONArray msgList)");
		SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
		
		String sql = "Insert or Replace into " + MEDIA_SCHEDULES_TABLE+ " (scheduleDate, fileName, scheduleTime, videoLength, fileSize, checkSum, langCode, serverPath," +
		"lastModifiedDateTime, scheduleType, mediaType, version) values(?,?,?,?,?,?,?,?,?,?,?)";
		
	    SQLiteStatement stmt = database.compileStatement(sql);
		    try {
		    	database.beginTransaction();
		    	for(int i=0;i<msgList.length();i++){
		        	JSONObject message = msgList.getJSONObject(i);
		        			stmt.clearBindings();
		    				stmt.bindString(1,message.getString("vDt"));
		    				stmt.bindString(2,message.getString("vNm")); 
		    				stmt.bindString(3,message.getString("vTime"));
		    				stmt.bindLong(4,message.getLong("vLeng"));
		    				stmt.bindLong(5,message.getLong("vSize"));
		    				stmt.bindString(6,message.getString("vChksum"));
		    				stmt.bindString(7,"NOUSE"); //TODO: update this
		    				stmt.bindString(8,message.getString("vPath")); 
		    				stmt.bindString(9,"NOUSE");
		    				stmt.bindString(10,message.getString("vType"));
		    				stmt.bindString(11,message.getString("mType"));
		    				// By Rohit
		    				stmt.bindString(12,message.getString("vVersion"));
		    				stmt.execute();
		    				Log.debug("stored schedule:" + message.getString("vNm"));
		        }
		    }catch (JSONException jse) {
		    	Log.error("Error synchronizing schedule from server:" + jse);
		    }catch(Exception e) {
		    	Log.error("exception storing schedule from server: " + e);
		    }finally {
		    	if(database != null) {
		    		database.setTransactionSuccessful();
			        database.endTransaction();
		    	}
				DatabaseManager.getInstance().closeDatabase();
		    }
	}
	
	public void cleanUpSchedules() {
		//String delete = "DELETE FROM "+ MEDIA_SCHEDULES_TABLE+ " WHERE  scheduleDate  < date('now') AND scheduleType <>\'"+Constants.BHAJAN + "\';";
		// Delete all the Media Records Till Date Except Bhajans
		String delete = "DELETE FROM "+ MEDIA_SCHEDULES_TABLE+ " WHERE scheduleDate  <= date('now') AND scheduleType <>\'"+Constants.BHAJAN + "\';";
		deleteRecordsAsPerQuery(delete);
	}
	
	public ArrayList<MediaSchedule> getAllSchedulesToCleanUp(){
		Log.info("Entering getAllSchedules()");
		//String sql = "Select * from " + MEDIA_SCHEDULES_TABLE+ " WHERE  scheduleDate  < date('now') AND scheduleType <>\'"+Constants.BHAJAN + "\';";
		// As Media Record Till Date are already deleted Below query will fetch everything except deleted by cleanUpSchedules()
		// All the files which are related to above cleanUpSchedules() will be deleted by ScheduleHelper.cleanUpFiles() 
		String sql = "Select * from " + MEDIA_SCHEDULES_TABLE+ " WHERE scheduleType <>\'"+Constants.BHAJAN + "\';";
		Log.debug("sql:" + sql);
		return getMediaScheduleListAsPerQuery(sql);
	}
	
	/* This method returns old bhajans that needs to be cleaned up in Sunday Clean Up Activity if Clean up for Bhajan is failed while Downloading*/
	public ArrayList<MediaSchedule> getOldVersionBhajansFromDb() {
		Log.info("Entering getOldVersionBhajansFromDb()");
		String sql = "SELECT * FROM "+ MEDIA_SCHEDULES_TABLE +" WHERE scheduleType=\'"+Constants.BHAJAN + "\' order by version desc";
		Log.debug("getOldVersionBhajansFromDb sql: " + sql);
        return getMediaScheduleListAsPerQuery(sql);
		}
	
	public ArrayList<MediaSchedule> getAllSchedules(){
		Log.info("Entering getAllSchedules()");
		//By Rohit - Filter Criteria for Lang Code Added
		String sql = "Select * from " + MEDIA_SCHEDULES_TABLE + " where langCode = "+ ConfigurationLive.getValue("local.default.locale") +" order by (scheduleType = 'B') asc, Date(scheduleDate) asc";
		Log.debug("sql:" + sql);
		return getMediaScheduleListAsPerQuery(sql);
	}
	
	//TODO: create method which take cursor parameter and populates MediaSchedule object
	public ArrayList<MediaSchedule> getScheduleByType(String scheduleType){
		Log.info("getScheduleByType(String scheduleType)");
		String sql = "Select * from " + MEDIA_SCHEDULES_TABLE + " where scheduleType=\'"+ scheduleType + "\'";
		Log.debug("sql:" + sql);
		return getMediaScheduleListAsPerQuery(sql);
	}
	
	 
	
	/* This method returns pravachan and news based on current date */
	public HashMap<String,MediaSchedule> getScheduleToPlay() {
		Log.info("Entering getScheduleToPlay()");
//		String sql = "SELECT * FROM Media_Schedules WHERE scheduleDate = date('now') and downloadStatus='complete'";
		String sql = "select * from "+ MEDIA_SCHEDULES_TABLE +" where date(scheduleDate)=date('now') and scheduleType in('P','N') and downloadStatus='complete'"; 
		Log.debug("getScheduleToPlay sql:" + sql);
		return getMediaScheduleMapAsPerQuery(sql);
	}
	
	// Below Method returns the Single Media Schedule Record From DB Provided Developer is sure that Query being passed will return Single Record
	private MediaSchedule getSingleMediaScheduleAsPerQuery(String sql)
	{
		Log.info("Entering getSingleMediaScheduleAsPerQuery()");
		Cursor mtCursor = null;
		MediaSchedule mediaSchedule = null;
		try {
			SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
			mtCursor = database.rawQuery(sql, null);
			if(mtCursor != null && mtCursor.getCount() > 0) {
			 mtCursor.moveToFirst();
			 mediaSchedule = new MediaSchedule(mtCursor);
		    Log.debug("MediaSchedule:" +mediaSchedule.getFileName());
			} 
		}catch(Exception e) {
			Log.error("Exception in getSingleMediaScheduleAsPerQuery " + e);
		}finally {
			if(mtCursor != null) mtCursor.close();
			DatabaseManager.getInstance().closeDatabase();
		}
		Log.info("Existing getSingleMediaScheduleAsPerQuery()");
		return mediaSchedule;	
	}
	
	// Below Method returns the Media Schedules From DB And It Creates Map out of it
	private HashMap<String,MediaSchedule> getMediaScheduleMapAsPerQuery(String sql)
	{
		Log.info("Entering getMediaScheduleMapAsPerQuery()");
		Cursor mtCursor = null;
		HashMap<String, MediaSchedule> map = null;
		try {
			SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
			mtCursor = database.rawQuery(sql, null);
			int count = mtCursor.getCount();
			if(count > 0){
				map = new HashMap<String,MediaSchedule>(count);
				while(mtCursor.moveToNext()) {
					MediaSchedule schedule = new MediaSchedule(mtCursor);
					Log.debug("added schedule to play:" +schedule.getFileName());
					map.put(schedule.getScheduleType(),schedule);
				}
			}
			
		}catch(Exception e) {
			Log.error("Exception in getScheduleToPlay" + e);
		}finally {
			if(mtCursor != null) mtCursor.close();
			DatabaseManager.getInstance().closeDatabase();
		}
		Log.info("Existing getMediaScheduleMapAsPerQuery()");
		return map;
	}
	
	// Below Method returns the Media Schedules From DB And It Creates List out of it
	private ArrayList<MediaSchedule> getMediaScheduleListAsPerQuery(String sql)
	{
		Log.info("Entering getMediaScheduleMapAsPerQuery()");
		Cursor mtCursor = null;
		ArrayList<MediaSchedule> list = null;
		try {
			SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
			mtCursor = database.rawQuery(sql, null);
			int count = mtCursor.getCount();
			if(count > 0){
				list = new ArrayList<MediaSchedule>(count);
				while(mtCursor.moveToNext()) {
					MediaSchedule schedule = new MediaSchedule(mtCursor);
					Log.debug("added schedule to play:" +schedule.getFileName());
					list.add(schedule);
				}
			}
		}catch(Exception e) {
			Log.error("Exception in getScheduleToPlay" + e);
		}finally {
			if(mtCursor != null) mtCursor.close();
			DatabaseManager.getInstance().closeDatabase();
		}
		Log.info("Existing getMediaScheduleMapAsPerQuery()");
		return list;
	}
	
	
	/* This method returns downloaded bhajan */
	public MediaSchedule getDownloadedBhajan() {
		Log.info("Entering getDownloadedBhajan()");
//		String sql = "SELECT * FROM Media_Schedules WHERE scheduleType=\'"+Constants.BHAJAN + "\' AND downloadStatus='complete'";
		String sql = "SELECT * FROM "+ MEDIA_SCHEDULES_TABLE +" WHERE scheduleType=\'"+Constants.BHAJAN + "\'";
		Log.debug("getDownloadedBhajan sql: " + sql);
        return getSingleMediaScheduleAsPerQuery(sql);
		}
	
	public synchronized void updateMediaSchedule(ArrayList<MediaSchedule> scheduleList) {
		Log.info("Entering updateMediaSchedule(ArrayList<MediaSchedule> scheduleList)");
		try {
			Iterator<MediaSchedule> itr = scheduleList.iterator();
			while(itr.hasNext()) {
				MediaSchedule schedule = itr.next();
				ContentValues cv = new ContentValues();
				cv.put("checkSumRetryCount", schedule.getCheckSumRetryCount());
				cv.put("downloadStatus", schedule.getDownloadStatus());
				cv.put("mediaPlayStatus", schedule.getMediaPlayStatus());
				cv.put("downloadedBytes", schedule.getDownloadedBytes());
				cv.put("checkSumStatus", schedule.getCheckSumStatus());
				SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
				database.update(MEDIA_SCHEDULES_TABLE, cv, "fileName=\'"+schedule.getFileName()+"\'",null);
				DatabaseManager.getInstance().closeDatabase();
			}
		}catch(Exception e) {
			Log.error("Exception in updateMediaSchedule"+e);
		}
	}
	
	
	//Marquee related
	public void syncMarquee(JSONArray msgList) {
		Log.info("Entering syncMarquee(JSONArray msgList)");
		String sql = "Insert or Replace into Marquee (msg_id, msg_txt, act_dt, exp_dt, rnw_dt, msg_spd, msg_rot, msg_prt," +
				"lang_cd, txt_siz, is_act, msg_type) values(?,?,?,?,?,?,?,?,?,?,?,?)";
	          	SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
			    SQLiteStatement stmt = database.compileStatement(sql);
				    try {
				    	database.beginTransaction();
				    	for(int i=0;i<msgList.length();i++){
				        	JSONObject message = msgList.getJSONObject(i);
				    				stmt.clearBindings();
				    				stmt.bindString(1,message.getString("msg_id"));
				    				stmt.bindString(2,message.getString("msg_txt")); 
				    				stmt.bindString(3,message.getString("act_dt"));
				    				stmt.bindString(4,message.getString("exp_dt"));
				    				stmt.bindString(5,"2050-07-07"); //??
				    				stmt.bindString(6,message.getString("msg_spd"));
				    				stmt.bindString(7,message.getString("msg_rot")); 
				    				stmt.bindString(8,message.getString("msg_prt")); 
				    				stmt.bindString(9,message.getString("lang_cd"));
				    				stmt.bindString(10,message.getString("fnt_size"));
				    				stmt.bindString(11,message.getString("is_act"));
				    				if(Constants.MARQUEE_TYPE_PRAVACHAN.equalsIgnoreCase(message.getString("msg_type"))) {
				    					stmt.bindString(12, Constants.MARQUEE_TYPE_PRAVACHAN);	
				    				}else {
				    					stmt.bindString(12, Constants.MARQUEE_TYPE_NEWS);
				    				}
				    				
				    				stmt.execute();
				    				Log.debug("stored marquee:" + message.getString("msg_id"));
				    				
				        }
				    }catch (JSONException jse) {
				    	Log.error("Error synchronizing marquee from server:" + jse);
				    }catch(Exception e) {
				    	Log.error("exception storing marquee from server: " + e);
				    }finally {
				    	if(database != null) {
				    		database.setTransactionSuccessful();
					        database.endTransaction();
				    	}
						DatabaseManager.getInstance().closeDatabase();
				    }
	}
	
	public String getMarquee(String marqueeType) {
		Log.info("Entering getMarquee()");
		String strTickr = "";
		String deleteSql = "DELETE FROM Marquee WHERE exp_dt <date('now')" + ";"; //delete expired messages
		String sql = "SELECT msg_txt , msg_id FROM Marquee WHERE is_act='Y' and msg_type=\'" + marqueeType + "\' AND Date(act_dt) <= Date('now') AND  Date(exp_dt ) >= Date('now') " 
				+ " ORDER BY msg_prt";
		Log.debug("getMarquee SQL: " + sql);
		Cursor cursor = null;
		try {
			SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
			database.execSQL(deleteSql);
			cursor = database.rawQuery(sql, null);
			if (cursor.getCount() > 1) {
				if (cursor.moveToFirst()) {
					do {
						if (!(cursor.getString(cursor.getColumnIndex("msg_id")).equals("1"))) {
							if (strTickr.length() == 0) {
								strTickr = cursor.getString(0);
							} else {
								strTickr = strTickr + " * *  " + cursor.getString(0);
							}
						}
					} while (cursor.moveToNext());
				}
			} else if (cursor.getCount() == 1) {
				// set default msg from server
				if (cursor.moveToFirst()) {
					if ((cursor.getString(cursor.getColumnIndex("msg_id")).equals("1"))) {
						strTickr = "  * * * *  " + cursor.getString(0) + " * *  ";
					}
				}
			} else {
				// If No Record is found, show Default Message.
				if(Constants.MARQUEE_TYPE_PRAVACHAN.equalsIgnoreCase(marqueeType)) {
					strTickr = getDefaultMarquee(); // hard coded default msg if not	
				}
				

			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			if(Constants.MARQUEE_TYPE_PRAVACHAN.equalsIgnoreCase(marqueeType)) {
				strTickr = getDefaultMarquee(); // hard coded default msg if not	
			}
			
		} finally {
			if(cursor != null) cursor.close();
			DatabaseManager.getInstance().closeDatabase();
		}
		Log.debug("Returning Marquee");
		return strTickr;
	}
	
	/* Method to retrieve default marquee message */
	private String getDefaultMarquee() {
		Resources res = context.getResources();
		return " * * " + res.getString(R.string.welcome) + " * * ";
	}
}