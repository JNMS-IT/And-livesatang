package org.android.activities;

import java.util.Date;

import org.satsang.audit.AuditConstants;
import org.satsang.audit.EventHandler;
import org.satsang.live.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.widget.TextView;

public class MantraActivity extends Activity {
	static private final Logger Log = LoggerFactory.getLogger(MantraActivity.class);
	
	private MantraActivity context=this;
	private long seekInterval=0; // received in milliseconds
	private long activityStartTime=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mantra);
		Log.trace("Inside MantraActivity OnCreate");
		activityStartTime = System.currentTimeMillis();
		seekInterval = getIntent().getExtras().getLong("seekInterval"); 
		((MyApplication) getApplication()).setLocaleConfiguration();
		if(seekInterval > 0) {
			EventHandler.appendAuditToFile(AuditConstants.MN_SEEK, "", AuditConstants.AUDIT_TYPE_AUDIT);
		}else {
			EventHandler.appendAuditToFile(AuditConstants.MN_START, "", AuditConstants.AUDIT_TYPE_AUDIT);
		}
		this.runOnUiThread(new MyAudioPlayer());
		
	}
	
	class MyAudioPlayer implements Runnable {
		MediaPlayer player = new MediaPlayer();
		private long count=1;
		private long loopCount=0;
		
		@Override
		public void run() {
			Log.trace("Inside MyAudioPlayer.run()");
			Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");
			TextView txtMantra = (TextView) findViewById(R.id.txt_mantra);
			txtMantra.setTypeface(face);
			txtMantra.setText(getString(R.string.mantraLabel));
			TextView mantraError = (TextView) findViewById(R.id.txt_mantra_error);
			mantraError.setTypeface(face);
			mantraError.setText(getString(R.string.mantraError));
			
			
			
			Log.debug("Starting mediaPlayer seekInterval: " + seekInterval);
			try {
				/*long audioLength = 14420l; // length of Recent file sent is 14 sec
			    long totalDuration = 1800000l;
			    if(seekInterval > 0) {
			      totalDuration = totalDuration - seekInterval;  
			    }*/
				seekInterval = Constants.MANTRA_AUDIO_LENGTH - seekInterval;
				loopCount = seekInterval / Constants.MANTRA_CLIP_LENGTH;
				long reminder = seekInterval % Constants.MANTRA_CLIP_LENGTH;
				if(reminder != 0){
				 loopCount++; 
				}
				Log.info("current time:" + new Date() + " loop count: " + loopCount);
				AssetFileDescriptor descriptor = getAssets().openFd("mantra/mantra.mp3");
				long start = descriptor.getStartOffset();
				long end = descriptor.getLength();
				player.setDataSource(descriptor.getFileDescriptor(), start, end);
				player.prepare();
				player.start();	
			}catch(Exception e){
				Log.error("Error playing mantra file"+e.toString());
			}
			
			player.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					EventHandler.appendAuditToFile("M100", "Error:"+ what + " " + extra, AuditConstants.AUDIT_TYPE_ERROR);
					Log.error("M100 Error:"+ what + "" + extra);
					Intent intent = new Intent(context, NewsVideoActivity.class);
					intent.putExtra("seekInterval", 0l);
					context.startActivity(intent);
					finish();
					return false;
				}
			});
			
			player.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer arg0) {
					// By Rohit
				 if(count < loopCount) {
                    count++;
                    player.seekTo(0);
                    player.start();
                 }else {
				   // DEALLOCATE ALL MEMORY By Rohit
				   if (player != null) {
				    if(player.isPlaying()) player.stop();
					player.release();
					player = null;
				   }	
					Log.trace("Mantra Play complete starting news");
					EventHandler.appendAuditToFile(AuditConstants.MN_END, "", AuditConstants.AUDIT_TYPE_AUDIT);
					Intent intent = new Intent(context, NewsVideoActivity.class);
					intent.putExtra("seekInterval", 0l);
					context.startActivity(intent);
					finish();
				}
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
	protected void onResume() {
		super.onResume();
		Log.trace("OnResume calling MyAudioPlayer");
		((MyApplication) getApplication()).setLocaleConfiguration();
//		context.runOnUiThread(new MyAudioPlayer());
		return;
	}

	

}
