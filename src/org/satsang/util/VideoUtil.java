package org.satsang.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

/*NOT IN USE*/
public class VideoUtil {
	static private final Logger Log = LoggerFactory.getLogger(VideoUtil.class);
	private boolean isVideoClean=false;
	
	/** Either use check sum or this utility to check if video runs properly or not. */
	public boolean verifyVideoInBackground(String videoFile){
			Log.info("Inside verifyVideoInBackground()");
			MediaPlayer player = new MediaPlayer();
			try {
				player.setDataSource(videoFile);
				player.setVolume(0.0f, 0.0f);
				player.prepare();
				player.start();
			}catch(Exception e){
				System.out.println("error playBhajanInBackground");
				return false;
			}
			
			player.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
					//Do nothing isVideoClean is already set to false
					return false;
				}
			});
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer arg0) {
					isVideoClean=true;	
				}
			});
			return isVideoClean;
		}
}
