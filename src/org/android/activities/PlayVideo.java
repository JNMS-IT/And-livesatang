package org.android.activities;

import java.io.File;

import org.santsang.schedule.ScheduleManager;
import org.satsang.audit.AuditConstants;
import org.satsang.audit.EventHandler;
import org.satsang.bo.MediaSchedule;
import org.satsang.bo.Singleton;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.Constants;
import org.satsang.util.CommonUtil;
import org.satsang.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

public class PlayVideo extends Activity {
	static private final Logger Log = LoggerFactory.getLogger(PlayVideo.class);

	MediaSchedule satsangSchedule = null;
	MediaSchedule pravachanSchedule = null;
	VideoView mVideoView;
	PlayVideo parentActivity = null;
	String marquee = null;
	TextView txt_marq;
	boolean playMantra = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_video);
		Singleton.getInstance().setSantsangInProgress(true);
		parentActivity = this;
		Log.trace("OnCreate");
		txt_marq = (TextView) findViewById(R.id.marquee);
		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");
		txt_marq.setTypeface(face);
		// txt_marq.setText(marquee);
		txt_marq.setVisibility(View.INVISIBLE);
		Log.info("Calling RetrieveSchedules()");
		parentActivity.runOnUiThread(new RetrieveSchedules());

	}

	public void playMedia(MediaSchedule satsangSchedule) throws Exception {
		Log.info("Playing satsang schedule");
		int timeDiffInSeconds = ConfigUtil.getScheduleTimeFromConfiguration() * 60;
		if (timeDiffInSeconds <= 0) {
			timeDiffInSeconds = -timeDiffInSeconds;
			Log.debug(timeDiffInSeconds + " " + satsangSchedule.getVideoLength());
			if (timeDiffInSeconds >= (satsangSchedule.getVideoLength())) {
				System.out.println("Satsang video already elapsed not playing satsang file");
				Intent intent = new Intent(getApplicationContext(), MantraActivity.class);
				intent.putExtra("seekInterval", 0l);
				parentActivity.startActivity(intent);
			} else {
				// play in seek mode
				Log.debug("Playing satsang in seek mode");
				EventHandler.appendAuditToFile(AuditConstants.BH_SEEK, satsangSchedule.getFileName(),
						AuditConstants.AUDIT_TYPE_AUDIT);
				play(satsangSchedule.getFileName(), timeDiffInSeconds, Constants.BHAJAN);
				Log.debug("Satsang play complete");
			}
		} else {
			// Display Progress time remaining
			Log.debug("Playing satsang in NORMAL MODE. Time difference:" + timeDiffInSeconds);
			Intent intent = new Intent(getApplicationContext(), SatsangStartActivity.class);
			intent.putExtra("timeDiffInSeconds", timeDiffInSeconds);
			parentActivity.startActivityForResult(intent, 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.trace("received result req code:" + requestCode);
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			EventHandler.appendAuditToFile(AuditConstants.BH_START, satsangSchedule.getFileName(),
					AuditConstants.AUDIT_TYPE_AUDIT);
			Log.debug("Playing satsang ONLY");
			play(satsangSchedule.getFileName(), 0, Constants.BHAJAN);
		} else {
			Log.debug("Playing satsang + pravachan");
			EventHandler.appendAuditToFile(AuditConstants.BH_START, satsangSchedule.getFileName() + " and "
					+ pravachanSchedule.getFileName(), AuditConstants.AUDIT_TYPE_AUDIT);
			playSatsangAndPravachan(satsangSchedule.getFileName(), 0);
		}
	}

	public void playMedia(MediaSchedule satsangSchedule, MediaSchedule pravachanSchedule) throws Exception {
		Log.info("Entering playMedia(PravachanSchedule, SatsangSchedule)");
		int timeDiffInSeconds = ConfigUtil.getScheduleTimeFromConfiguration() * 60;
		Log.debug("Time difference:" + timeDiffInSeconds);
		int satsangVideoLength = (int) satsangSchedule.getVideoLength();
		if (timeDiffInSeconds <= 0) {
			// convert negative value to positive
			timeDiffInSeconds = -timeDiffInSeconds;
			// play in seek mode
			Log.debug("Playing Satsang and Pravachan");
			if (timeDiffInSeconds < satsangSchedule.getVideoLength()) {
				Log.debug("Playing satsang first in seek mode and pravachan in normal mode");
				EventHandler.appendAuditToFile(AuditConstants.BH_SEEK, satsangSchedule.getFileName(),
						AuditConstants.AUDIT_TYPE_AUDIT);

				playSatsangAndPravachan(satsangSchedule.getFileName(), timeDiffInSeconds);
			} else if (timeDiffInSeconds < satsangVideoLength + pravachanSchedule.getVideoLength()) {
				Log.debug("Playing ONLY pravachan in seek mode");
				EventHandler.appendAuditToFile(AuditConstants.PV_SEEK, pravachanSchedule.getFileName(),
						AuditConstants.AUDIT_TYPE_AUDIT);
				play(pravachanSchedule.getFileName(), (timeDiffInSeconds - satsangVideoLength), Constants.PRAVACHAN);
				// 03-Aug-2013 Uncomment below
				Log.debug("setting marquee...");
				txt_marq.setVisibility(View.VISIBLE);
				txt_marq.setSelected(true);
				txt_marq.setEllipsize(TruncateAt.MARQUEE);
				txt_marq.setSingleLine(true);
			} else {
				Log.debug("Pravachan schedule alread elapsed not playing satsang and pravachan file");
				Intent intent = new Intent(getApplicationContext(), MantraActivity.class);
				intent.putExtra("seekInterval", 0l);
				parentActivity.startActivity(intent);

			}

		} else {
			Log.debug("Playing satsang in normal mode");
			Intent intent = new Intent(getApplicationContext(), SatsangStartActivity.class);
			intent.putExtra("timeDiffInSeconds", timeDiffInSeconds);
			parentActivity.startActivityForResult(intent, 2);

		}
	}

	
	 /* Use below method to play single file i.e. either satsang or pravachan only */
	private void play(String videoFile, long seekInterval, String scheduleType) {
		Log.debug("Playing video:" + videoFile);
		validateFile(videoFile, false);
		mVideoView = (VideoView) findViewById(R.id.videoPlayer);
		mVideoView.setKeepScreenOn(true);
		mVideoView.setFocusable(true);
		if (Constants.BHAJAN.equalsIgnoreCase(scheduleType)) {
			mVideoView.setOnErrorListener(bhajanVideoErrorListener);
		} else
			mVideoView.setOnErrorListener(pravachanVideoErrorListener);

		mVideoView.setVideoPath(videoFile);
		mVideoView.seekTo((int) seekInterval * 1000);
		mVideoView.start();
		if (Constants.PRAVACHAN.equalsIgnoreCase(scheduleType)) {
			mVideoView.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					EventHandler.appendAuditToFile(AuditConstants.PV_END, "", AuditConstants.AUDIT_TYPE_AUDIT);
					Log.debug("Pravachan finished");
					Intent intent = new Intent(getApplicationContext(), NewsVideoActivity.class);
					intent.putExtra("seekInterval", 0l);
					parentActivity.startActivity(intent);
					finish();
				}
			});
		} else {
			mVideoView.setOnCompletionListener(new BhajanCompletionListener());
		}

	}
	

	private class BhajanCompletionListener implements OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.debug("Bhajan finished");
			EventHandler.appendAuditToFile(AuditConstants.BH_END, "", AuditConstants.AUDIT_TYPE_AUDIT);
			Intent intent = new Intent(getApplicationContext(), MantraActivity.class);
			intent.putExtra("seekInterval", 0l);
			parentActivity.startActivity(intent);
			finish();
		}

	}

	/* This method plays bhajan and then pravachan */
	private void playSatsangAndPravachan(String videoFile, long seekInterval) {
		Log.debug("Playing video:" + videoFile);
		validateFile(videoFile, false);
		mVideoView = (VideoView) findViewById(R.id.videoPlayer);
		// MediaController mc = new MediaController(parentActivity);
		// mVideoView.setMediaController(mc);
		mVideoView.setKeepScreenOn(true);
		mVideoView.setFocusable(true);
		mVideoView.setOnErrorListener(bhajanVideoErrorListener);
		mVideoView.setVideoPath(videoFile);
		mVideoView.seekTo((int) seekInterval * 1000);
		mVideoView.start();
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				EventHandler.appendAuditToFile(AuditConstants.BH_END, satsangSchedule.getFileName(),
						AuditConstants.AUDIT_TYPE_AUDIT);
				Log.debug("Bhajan complete starting pravachan");
				EventHandler.appendAuditToFile(AuditConstants.PV_START, satsangSchedule.getFileName(),
						AuditConstants.AUDIT_TYPE_AUDIT);
				playPravachan();
			}
		});
	}

	/*
	 * This method will play pravachan file without seek assuming there is no seek required. This method should be called when satsang is finished
	 * playing and need to play pravachan without seek mode
	 */
	private void playPravachan() {
		Log.debug("Playing pravachan only");
		validateFile(pravachanSchedule.getFileName(), true);
		// 03-Aug-2013 Uncomment below
		Log.debug("setting marquee...");
		txt_marq.setVisibility(View.VISIBLE);
		txt_marq.setSelected(true);
		txt_marq.setEllipsize(TruncateAt.MARQUEE);
		txt_marq.setSingleLine(true);
		mVideoView = (VideoView) findViewById(R.id.videoPlayer);
		mVideoView.setKeepScreenOn(true);
		mVideoView.setFocusable(true);
		mVideoView.setOnErrorListener(pravachanVideoErrorListener);
		mVideoView.setVideoPath(pravachanSchedule.getFileName());
		mVideoView.start();
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				EventHandler.appendAuditToFile(AuditConstants.PV_END, pravachanSchedule.getFileName(),
						AuditConstants.AUDIT_TYPE_AUDIT);
				Log.debug("playPravachan complete");
				updateMediaPlayStatus(pravachanSchedule, "complete"); 
//				CommonUtil.deleteFile(pravachanSchedule.getFileName());
				Intent intent = new Intent(getApplicationContext(), NewsVideoActivity.class);
				intent.putExtra("seekInterval", 0l);
				parentActivity.startActivity(intent);
				finish();
			}
		});
	}

	private OnErrorListener bhajanVideoErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			EventHandler.appendAuditToFile("B100", "Error:" + what + " " + extra, AuditConstants.AUDIT_TYPE_ERROR);
			Intent intent = new Intent(getApplicationContext(), End.class);
			intent.putExtra("status", getString(R.string.err104));
			parentActivity.startActivity(intent);
			finish();
			return true;
		}
	};

	private OnErrorListener pravachanVideoErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.error("Error for pravachan + " + what + " " + extra);
			Intent intent = new Intent(getApplicationContext(), MantraActivity.class);
			intent.putExtra("seekInterval", 0l);
			parentActivity.startActivity(intent);
			finish();
			return true;
		}
	};

	// this method updates media status to local file system. It changes the
	// state to "complete"
	public void updateMediaPlayStatus(MediaSchedule pravachanSchedule, String status) {
		Log.info("Entering:MediaManager.updateMediaPlayStatus(PravachanSchedule, status)");
		ScheduleManager manager = new ScheduleManager();
		pravachanSchedule.setMediaPlayStatus(status);
		manager.updateMediaSchedule(parentActivity, pravachanSchedule);
	}

	
	private void validateFile(String fileName, boolean isPravachan) {
		File satsangFile = new File(fileName);
		if (!satsangFile.exists()) {

			if (isPravachan) {
				Intent mantra = new Intent(getApplicationContext(), MantraActivity.class);
				mantra.putExtra("seekInterval", 0l);
				parentActivity.startActivity(mantra);
			} else {
				Intent intent = new Intent(getApplicationContext(), End.class);
				intent.putExtra("status", getString(R.string.err404s));
				parentActivity.startActivity(intent);
			}
		}
	}

	
	class RetrieveSchedules implements Runnable {

		public void run() {
			Log.info("Inside RetrieveSchedules.run()");
			if (Constants.MODE_IS_LIVE) {
				Settings.System.putInt(parentActivity.getContentResolver(), Global.AUTO_TIME, 1);
				Settings.System.putInt(parentActivity.getContentResolver(), Global.AUTO_TIME_ZONE, 1);
			}
			try {
				DBAdapter dbAdp = new DBAdapter(parentActivity);
				marquee = dbAdp.getMarquee(Constants.MARQUEE_TYPE_PRAVACHAN);
				int marqueesize = marquee.length();
				Log.debug("Got marquee length: " + marqueesize);
				if (marqueesize <= 80) {
					while (marqueesize < 250) {
						marquee = " " + marquee + " " + marquee + " " + marquee + " ";
						marqueesize = marquee.length();
					}
				}

				txt_marq.setText(Html.fromHtml(marquee));

				Log.debug("retrieving schedules");

				pravachanSchedule = MyApplication.getPlayMap().get(Constants.PRAVACHAN);
				satsangSchedule = MyApplication.getPlayMap().get(Constants.BHAJAN);
				Log.debug("Got schedules now check null values");
				if (pravachanSchedule == null) {
					Log.debug("Pravachan schedule is null");
					playMantra = true;
					if (satsangSchedule != null) {
						Log.debug("SatsangSchedule not null");
						try {
							playMedia(satsangSchedule);
						} catch (Exception e) {
							Log.error("Exception calling playMedia(satsangSchedule) " + e);
						}
					} else {
						Log.debug("Both Pravachan and satsang are null");
						EventHandler.appendAuditToFile("BP100", "Bhajan and Pravachan are null",
								AuditConstants.AUDIT_TYPE_ERROR);
						Intent intent = new Intent(getApplicationContext(), End.class);
						intent.putExtra("status", getString(R.string.err104));
						parentActivity.startActivity(intent);
						// finish();
					}
				} else {
					System.out.println("satsangSchedule is null");
					if (satsangSchedule == null) {
						Log.debug("Satsang schedule is null. Seems mismatch between pravachan schedule and satsang schedule");
						EventHandler.appendAuditToFile("B100", "Bhajan and Pravachan are null",
								AuditConstants.AUDIT_TYPE_ERROR);
						Intent intent = new Intent(getApplicationContext(), End.class);
						intent.putExtra("status", getString(R.string.err104));
						parentActivity.startActivity(intent);
						// finish();
					} else {
						Log.debug("Satsang & Pravachan Schedule not null");
						try {
							playMedia(satsangSchedule, pravachanSchedule);
						} catch (Exception e) {
							Log.error("Exception calling playMedia(satsangSchedule, pravachanSchedule) " + e);
						}
					}
				}
			} catch (Exception e) {
				Log.error("Exception in run()" + e);
			}

		}

	}

	@Override
	public void onBackPressed() {
		return;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.trace("OnResume");
		Singleton.getInstance().setSantsangInProgress(true);
//		parentActivity.runOnUiThread(new RetrieveSchedules()); // Let user swipe to clean and relaunch application
		return;
	}

}
