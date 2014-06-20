package org.santsang.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.time.DateUtils;
import org.satsang.bo.MediaSchedule;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.Constants;
import org.satsang.util.CommonUtil;
import org.satsang.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

public class ScheduleManager {
	static private final Logger Log = LoggerFactory.getLogger(ScheduleManager.class);
	
	public ArrayList<MediaSchedule> getAllPravachanRecords(Context context) {
		Log.info("Entering getAllPravachanRecords(Context context)");
		DBAdapter dbAdapter = new DBAdapter(context);
		
		try {
			ArrayList<MediaSchedule> scheduleList = dbAdapter.getAllSchedules();
			if(scheduleList != null) {
				Log.debug("schedule list size:" + scheduleList.size());
				ScheduleHelper helper = new ScheduleHelper();
				ArrayList<MediaSchedule> pList = helper.syncScheduleFiles(scheduleList);
				dbAdapter.updateMediaSchedule(pList);
				//dbAdapter.cleanUpSchedules(); //TODO: deferred: move this clean up to separate task which need to be invoked every sunday after santsang
//				Collections.sort(pList, new MediaScheduleComparator());
				//TODO: test if delete from database works fine else remove using below code
				/*Iterator<MediaSchedule> itr = pList.iterator();
				while(itr.hasNext()){
					MediaSchedule schedule = itr.next();
					int compare = DateUtil.compareDate(schedule.getScheduleDate());
					if(compare == -1) pList.remove(schedule); 
				}*/
				Log.info("returning pravachan list");
				return pList;	
			}
		}catch(Exception e){
			Log.error("Error in getAllPravachanRecords"+e.getMessage(),e);
		}
		return null;
	}
 
	/* Below is standby method if db query does not work. Get all schedules and check in list if schedule is for today's play day */
	public ArrayList<MediaSchedule> getScheduleToPlay(Context context) {
		Log.info("Entering getPravachanScheduleToPlay(Context context)");
		try {
			ArrayList<MediaSchedule> playList = new ArrayList<MediaSchedule>();
			DBAdapter dbAdapter = new DBAdapter(context);
			//Get downloaded bhajan
			MediaSchedule bhajan = dbAdapter.getDownloadedBhajan();
			if(bhajan != null) {
				playList.add(bhajan);
			}else {
				//Add default copied bhajan
				playList.add(CommonUtil.getDefaultBhajan());
			}
			ArrayList<MediaSchedule> scheduleList = dbAdapter.getAllSchedules();
			if(scheduleList != null) {
				Log.debug("shcedule play list not null"+ scheduleList.size());
				Iterator<MediaSchedule> itr = scheduleList.iterator();
				while (itr.hasNext()) {
					MediaSchedule schedule = itr.next();
					Date schDate = DateUtil.parseDate(schedule.getScheduleDate());
					Log.debug("Checking schedule for: "+ schedule.getFileName());
					if (DateUtils.isSameDay(schDate, DateUtil.getFormattedCurrentDate()) && schedule.getDownloadStatus().equalsIgnoreCase("complete") && !Constants.BHAJAN.equalsIgnoreCase(schedule.getScheduleType())) {
						Log.debug("Adding schedule to playList : "+schedule.getFileName());
						playList.add(schedule);
					}	
				}
				return playList;
			}
			
		} catch (Exception e) {
			Log.error("Exception retrieving pravachan schedule file to play" + e.getMessage(), e);
		}
		Log.info("Returning null pravachan play schedule file");
		return null;
	}
	
	
	/* INUSE  */
	public HashMap<String, MediaSchedule> getMediaToPlay(Context context) {
		Log.info("Entering getPravachanScheduleToPlay(Context context)");
		try {
			HashMap<String, MediaSchedule> playMap = new HashMap<String,MediaSchedule>();
			DBAdapter dbAdapter = new DBAdapter(context);
			//Get downloaded bhajan
			MediaSchedule bhajan = dbAdapter.getDownloadedBhajan();
			if(bhajan != null) {
				playMap.put(Constants.BHAJAN, bhajan);
			}else {
				//Add default copied bhajan
				playMap.put(Constants.BHAJAN, CommonUtil.getDefaultBhajan());
			}
			HashMap<String,MediaSchedule> map = dbAdapter.getScheduleToPlay();
			if(map != null) {
				MediaSchedule pravachan = map.get(Constants.PRAVACHAN);
				if(pravachan != null) {
					playMap.put(Constants.PRAVACHAN, pravachan);
				}else {
					playMap.put(Constants.MANTRA, CommonUtil.getDefaultPravachan());
				}
				
				MediaSchedule news = map.get(Constants.NEWS);
				if(news != null) {
					playMap.put(Constants.NEWS, news);
				}
			}
			
			return playMap;
		} catch (Exception e) {
			Log.error("Exception retrieving pravachan schedule file to play" + e.getMessage(), e);
		}
		Log.info("Returning null pravachan play schedule file");
		return null;
	}
	
	public MediaSchedule getPravachanScheduleToPlay(Context context) {
		Log.info("Entering getPravachanScheduleToPlay(Context context)");
		try {
			DBAdapter dbAdapter = new DBAdapter(context);
			ArrayList<MediaSchedule> scheduleList = dbAdapter.getScheduleByType(Constants.PRAVACHAN);
			if(scheduleList != null) {
				Log.debug("shcedule play list not null"+ scheduleList.size());
				Iterator<MediaSchedule> itr = scheduleList.iterator();
				while (itr.hasNext()) {
					MediaSchedule schedule = itr.next();
					Date schDate = DateUtil.parseDate(schedule.getScheduleDate());
					Log.debug("Checking schedule for: "+ schedule.getFileName());
					if (DateUtils.isSameDay(schDate, DateUtil.getFormattedCurrentDate()) && schedule.getDownloadStatus().equalsIgnoreCase("complete")) {
						Log.debug("Returning file: "+schedule.getFileName()+" to play");
						return schedule;
					}	
				}	
			}
			
		} catch (Exception e) {
			Log.error("Exception retrieving pravachan schedule file to play" + e.getMessage(), e);
		}
		Log.info("Returning null pravachan play schedule file");
		return null;
	}
	
	/*public void play(Context context) {
		
		DBAdapter dbAdp = new DBAdapter(context);
		dbAdp.open();
		MediaSchedule bhajan = dbAdp.getDownloadedBhajan();
		MediaSchedule pravachan = null;
		MediaSchedule news = null;
		if(bhajan == null) {
			bhajan = CommonUtil.getDefaultBhajan();
		}
		
		ArrayList<MediaSchedule> playList =  dbAdp.getScheduleToPlay();
		Iterator<MediaSchedule> itr = playList.iterator();
		while(itr.hasNext()) {
			MediaSchedule sch = itr.next();
			if(Constants.PRAVACHAN.equalsIgnoreCase(sch.getScheduleType())) {
				pravachan = sch;
			}
			if(Constants.NEWS.equalsIgnoreCase(sch.getScheduleType())) {
				news = sch;
			}
		}
		
		process(context, bhajan, pravachan, news);
		
	}*/

	
	//NOT IN USE MOVED BELOW CODE TO SANTSANGACTIVITY
	/*private void process(Context context, MediaSchedule bhajan, MediaSchedule pravachan, MediaSchedule news) {
		
		long bhajanVideoLength = bhajan.getVideoLength();
		long pravachanVideoLength = pravachan.getVideoLength(); //TODO: if pravachan is empty add Mantra audio length
		long newsVideoLength = news.getVideoLength();
		
		int timeDiffInSeconds = ConfigUtil.getScheduleTimeFromConfiguration() * 60;
		if(timeDiffInSeconds <= 0) {
			//check if bhajan and pravachan has elaspsed. If yes then call news timeDiffInSeconds >= (satsangSchedule.getVideoLength())
			timeDiffInSeconds = -timeDiffInSeconds;
			if(timeDiffInSeconds >= (bhajanVideoLength+pravachanVideoLength+newsVideoLength)) {
				Intent intent = new Intent(context, End.class);
				intent.putExtra("status", context.getString(R.string.santasangfinish));
				context.startActivity(intent);
			} else if (timeDiffInSeconds >= (bhajanVideoLength+pravachanVideoLength)){
				//play news
				String newsDir = ConfigurationLive.getValue("news.download.directory");
				Intent intent = new Intent(context, NewsVideoActivity.class);
				intent.putExtra("fileName", newsDir+news.getFileName());
				intent.putExtra("seekInterval", timeDiffInSeconds - news.getVideoLength());
				context.startActivity(intent);
			} else if (timeDiffInSeconds >= bhajanVideoLength) {
				//play pravachan or Mantra
				if(pravachan == null) {
					Intent intent = new Intent(context, MantraActivity.class);
					intent.putExtra("seekInterval", timeDiffInSeconds - Constants.MANTRA_AUDIO_LENGTH);
					context.startActivity(intent);
				}else {
					Intent intent = new Intent(context, PlayVideo.class);
					context.startActivity(intent);
				}
			} else {
				//play santsang
				Intent intent = new Intent(context, PlayVideo.class);
				context.startActivity(intent);
			}
		}else {
			// Play satsang in normal mode
			Intent intent = new Intent(context, PlayVideo.class);
			context.startActivity(intent);
		}	
	}*/
		
	

	public void updateMediaSchedule(Context context, MediaSchedule schedule) {
		Log.info("Entering updateMediaSchedule(Context context, MediaSchedule schedule)");
		try {
			DBAdapter dbAdapter = new DBAdapter(context);
			ArrayList<MediaSchedule> scheduleList = new ArrayList<MediaSchedule>();
			scheduleList.add(schedule);
			dbAdapter.updateMediaSchedule(scheduleList);
		}catch(Exception e) {
			Log.error("Error in updatePravachanSchedule"+e.getMessage(),e);
		}
		
	}
	
	public void updateScheduleList(Context context, ArrayList<MediaSchedule> scheduleList){
		try {
			DBAdapter dbAdapter = new DBAdapter(context);
			Log.info("Updating schedule List");
			dbAdapter.updateMediaSchedule(scheduleList);	
		}catch(Exception e) {
			Log.error("Exception in udpateScheduleList " + e);
		}
		
	}
	
	
	
}
