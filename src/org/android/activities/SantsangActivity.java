package org.android.activities;

import java.io.File;
import java.util.HashMap;

import org.android.phone.CDUSSDService;
import org.satsang.bo.MediaSchedule;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.satsang.util.CommonUtil;
import org.satsang.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;


public class SantsangActivity extends Activity {

	static private final Logger Log = LoggerFactory.getLogger(SantsangActivity.class);

	private final SantsangActivity context = this;
	
	TextView dStatusText;
	TextView TxtWelcome;
	MyApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_livesatsang);
		Log.info("OnCreate");
		new ProcessSchedule().execute();
		
	}

	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		return;
	}


	@Override
	protected void onRestart() {
		super.onRestart();
		return;
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.trace("On resume called");
		((MyApplication) getApplication()).setLocaleConfiguration();
		return;
	}

	/**
	 * This task processes schedule files and depending on time invokes appropriate activity
	 * 
	 * */
	class ProcessSchedule extends AsyncTask<String, String, String> {
		
		@Override
		protected String doInBackground(String... f_url) {
			Log.debug("Inside ProcessSchedule.doInBackground()");
			try {
				Intent ussdService = new Intent(context, CDUSSDService.class);
				context.startService(ussdService);
			}catch(Exception e) {
				Log.error("Error starting USSD service"+e);
			}
			//process schedules and identify what to play
			boolean playMantra = false;
			boolean playNews = false;
			
			long bhajanVideoLength= 0;
			long pravachanVideoLength= 0;
			long newsVideoLength= 0;
//			ScheduleManager manager = new ScheduleManager();
//			HashMap<String, MediaSchedule> playMap = manager.getMediaToPlay(context);
			HashMap<String, MediaSchedule> playMap = new HashMap<String, MediaSchedule>();
			String downloadDir = ConfigurationLive.getValue("local.media.files.directory");
			try {
				DBAdapter dbAdapter = new DBAdapter(context);
				//Get downloaded bhajan
				boolean useDefaultBhajan=true;
				MediaSchedule bhajan = dbAdapter.getDownloadedBhajan();
				Log.debug("processing bhajan");
				if(bhajan != null) {
					String downloadedBhajanFile = ConfigurationLive.getValue("bhajan.download.directory")+bhajan.getFileName();
					File file = new File(downloadedBhajanFile);
					if(file.exists() && "complete".equalsIgnoreCase(bhajan.getDownloadStatus())) {
						Log.info("Using downloaded bhajan: " + downloadedBhajanFile);
						bhajan.setFileName(ConfigurationLive.getValue("bhajan.download.directory")+bhajan.getFileName());
						playMap.put(Constants.BHAJAN, bhajan);
						bhajanVideoLength = bhajan.getVideoLength();
						useDefaultBhajan=false;
					}
				}
				
				if(useDefaultBhajan) {
					//Add default copied bhajan
					bhajan = CommonUtil.getDefaultBhajan();
					playMap.put(Constants.BHAJAN, bhajan);
					bhajanVideoLength = bhajan.getVideoLength();
				}
				HashMap<String,MediaSchedule> map = dbAdapter.getScheduleToPlay();
				MediaSchedule pravachan = null;
				MediaSchedule news = null;
				Log.debug("processing pravachan");
				if(map != null) {
					pravachan = map.get(Constants.PRAVACHAN);
					if(pravachan != null) {
						pravachan.setFileName(downloadDir+pravachan.getFileName());
						File pravachanFile = new File(pravachan.getFileName());
						if(pravachanFile.exists()) {
							playMap.put(Constants.PRAVACHAN, pravachan);
							pravachanVideoLength = pravachan.getVideoLength();	
						}else {
							pravachan = CommonUtil.getDefaultPravachan();
							playMap.put(Constants.MANTRA, pravachan);
							pravachanVideoLength = pravachan.getVideoLength(); 
							playMantra=true;
						}
						 
					}
					
					news = map.get(Constants.NEWS);
					if(news != null) {
						news.setFileName(downloadDir+news.getFileName());
						File newsFile = new File(news.getFileName());
						if(newsFile.exists()) {
							playMap.put(Constants.NEWS, news);
							newsVideoLength= news.getVideoLength();	
						}
						
					}
				}else {
					pravachan = CommonUtil.getDefaultPravachan();
					playMap.put(Constants.MANTRA, pravachan);
					pravachanVideoLength = pravachan.getVideoLength(); 
					playMantra=true;
				}
				MyApplication.setPlayMap(playMap);
				Log.debug("Map set in MyApplication");
				int timeDiffInSeconds = ConfigUtil.getScheduleTimeFromConfiguration() * 60;
				Log.debug("timeDiff:" + timeDiffInSeconds);
				if(timeDiffInSeconds <= 0) {
					//check if bhajan and pravachan has elaspsed. If yes then call news timeDiffInSeconds >= (satsangSchedule.getVideoLength())
					timeDiffInSeconds = -timeDiffInSeconds;
					if(timeDiffInSeconds >= (bhajanVideoLength+pravachanVideoLength+newsVideoLength)) {
						Intent intent = new Intent(context, End.class);
						intent.putExtra("status", context.getString(R.string.santasangfinish));
						context.startActivity(intent);
					} else if (timeDiffInSeconds >= (bhajanVideoLength+pravachanVideoLength)){
						//play news
						Intent intent = new Intent(context, NewsVideoActivity.class);
						intent.putExtra("seekInterval", ((long) (timeDiffInSeconds - (bhajanVideoLength + pravachanVideoLength))));
						context.startActivity(intent);
					} else if (timeDiffInSeconds >= bhajanVideoLength) {
						//play pravachan or Mantra
						if(playMantra) {
							Intent intent = new Intent(context, MantraActivity.class);
							//calculations in milliseconds as seconds calculation of video clip could be in float
							long seekInt = ((timeDiffInSeconds*1000) - (bhajanVideoLength*1000));
							intent.putExtra("seekInterval", seekInt);
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
			}catch(Exception e) {
				Log.error("Exception in ProcessSchedule.background"+e);
				Intent intent = new Intent(context, End.class);
				intent.putExtra("status", getString(R.string.err108));
				context.startActivity(intent);
			}
			
			
			finish();
			return null;
		}

				
	}

}
