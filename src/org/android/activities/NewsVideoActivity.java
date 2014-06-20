package org.android.activities;

import org.satsang.audit.AuditConstants;
import org.satsang.audit.EventHandler;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

public class NewsVideoActivity extends Activity {
	static private final Logger Log = LoggerFactory.getLogger(NewsVideoActivity.class);
	
	private NewsVideoActivity context=this;
	private VideoView mVideoView;
	private String fileName=null;
	private long seekInterval=0;
	private long activityStartTime=0;
	TextView txt_marquee;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_video);
		Log.trace("OnCreate called");
		txt_marquee = (TextView) findViewById(R.id.marquee);
		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");
		txt_marquee.setTypeface(face);
		txt_marquee.setVisibility(View.INVISIBLE);
		
		activityStartTime = System.currentTimeMillis();
		Bundle extras = getIntent().getExtras();

		try {
			seekInterval = extras.getLong("seekInterval");
			Log.debug("seekInterval " + seekInterval);
			fileName = MyApplication.getPlayMap().get(Constants.NEWS).getFileName();
		}catch(Exception e) {
			Log.error("Error retrieving information " + e);
		}
		if(fileName == null || "".equalsIgnoreCase(fileName)) {
			Intent intent = new Intent(getApplicationContext(), End.class);
			intent.putExtra("status", getString(R.string.santasangfinish));
			context.startActivity(intent);
		}else {
			if(seekInterval > 0) {
				EventHandler.appendAuditToFile(AuditConstants.NW_SEEK, fileName, AuditConstants.AUDIT_TYPE_AUDIT);
			}else {
				EventHandler.appendAuditToFile(AuditConstants.NW_START, fileName, AuditConstants.AUDIT_TYPE_AUDIT);
			}
			this.runOnUiThread(new MyPlayer());	
		}
		
		
	}
	
	class MyPlayer implements Runnable {

		@Override
		public void run() {
			Log.trace("Inside MyPlayer.run()");
			DBAdapter dbAdp = new DBAdapter(context);
			try {
				String newsMarquee = dbAdp.getMarquee(Constants.MARQUEE_TYPE_NEWS);
				if(newsMarquee != null && !"".equalsIgnoreCase(newsMarquee)) {
					//set marquee length
					int marqSize = newsMarquee.length();
					if (marqSize <= 80) {
						while (marqSize < 250) {
							newsMarquee = " " + newsMarquee + " " + newsMarquee + " " + newsMarquee + " ";
							marqSize = newsMarquee.length();
						}
					}
					txt_marquee.setText(Html.fromHtml(newsMarquee));
					txt_marquee.setVisibility(View.VISIBLE);
					txt_marquee.setSelected(true);
					txt_marquee.setEllipsize(TruncateAt.MARQUEE);
					txt_marquee.setSingleLine(true);	
				}	
			}catch(Exception e) {
				Log.error("Error retrieving news marquee " + e);
			}
			
			
			
			seekInterval = seekInterval + (System.currentTimeMillis() - activityStartTime); //if paused then push ahead
			mVideoView = (VideoView) findViewById(R.id.videoPlayer);
			mVideoView.setKeepScreenOn(true);
			mVideoView.setFocusable(true);
			mVideoView.setVideoPath(fileName);
			Log.debug("Set video file:"+fileName);
			mVideoView.seekTo((int) seekInterval * 1000);
			Log.info("Starting news video");
			mVideoView.start();

			mVideoView.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.error("Error in playing news video");
					txt_marquee.setVisibility(View.INVISIBLE);
					//TODO: uncomment below after testing
//					CommonUtil.deleteFile(fileName);
					Intent intent = new Intent(getApplicationContext(), End.class);
					intent.putExtra("status", getString(R.string.santasangfinish));
					context.startActivity(intent);
					finish();
					return false;
				}
			});
			mVideoView.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Log.trace("Video play complete");
					EventHandler.appendAuditToFile(AuditConstants.NW_END, fileName, AuditConstants.AUDIT_TYPE_AUDIT);
					txt_marquee.setVisibility(View.INVISIBLE);
					//TODO: uncomment below after testing
//					CommonUtil.deleteFile(fileName);
					Intent intent = new Intent(getApplicationContext(), End.class);
					intent.putExtra("status", getString(R.string.santasangfinish));
					context.startActivity(intent);
					finish();
				}
			});
			
		}
		
	}
	@Override
	public void onBackPressed() {
		return;
	}
	@Override
	protected void onRestart() {
		super.onRestart();
		return;
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
//		finish();
		return;
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.trace("OnResume");
//		context.runOnUiThread(new MyPlayer()); //Let user swipe clean and relaunch the application
		return;
	}

	

}
