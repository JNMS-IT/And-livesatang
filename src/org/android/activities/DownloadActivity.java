package org.android.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.santsang.schedule.ScheduleManager;
import org.satsang.bo.MediaSchedule;
import org.satsang.bo.Singleton;
import org.satsang.ftp.DownloadManager;
import org.satsang.live.config.Constants;
import org.satsang.util.MD5Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadActivity extends Activity implements CopyStreamListener {
	static private final Logger Log = LoggerFactory.getLogger(DownloadActivity.class);
	
	public static ProgressBar dwProgressBar;
	Typeface face;
	public long downloadedBytes;
	public long bytesToDownload;
	private long totalBytesDownloaded = 0;
	private long totalBytesToDownload = 0;
	private final DownloadActivity context=this;
	private TextView TxtWelcome;
	String percentFormat = "%.3f";
	ArrayList<MediaSchedule> scheduleList = new ArrayList<MediaSchedule>();
	ArrayList<MediaSchedule> displayList = new ArrayList<MediaSchedule>();
	String recentDownloadedFile="";
	
	MyApplication app;

	final private Handler mHandler = new Handler();
	Runnable mUpdateTime = new Runnable() {
		public void run() {
			updateTimeView();
		}

		private void updateTimeView() {
			long currentValue = dwProgressBar.getProgress();
			long maxValue = dwProgressBar.getMax();
			double percent = ((double) currentValue / maxValue) * 100;
			String percentStr = String.format(percentFormat, percent);
			if (dwProgressBar.getProgress() == dwProgressBar.getMax()) {
				//TODO: test if this is required here
				/*Intent intent = new Intent(getApplicationContext(), End.class);
				intent.putExtra("status", getString(R.string.finishdownloading));
				context.startActivity(intent);*/
			} else {
				String message = getString(R.string.startdownloading) + percentStr + "%";
				((TextView) TxtWelcome).setText(message);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		Log.info("DownloadActivity onCreate called");
		((MyApplication) getApplication()).setLocaleConfiguration();
		TxtWelcome = (TextView) findViewById(R.id.txt_welcome);
		((TextView) TxtWelcome).setText(getString(R.string.startdownloading));
		face = Typeface.createFromAsset(getAssets(), "fonts/DroidHindi.ttf");
		TextView dStatusText = (TextView) findViewById(R.id.txt_downloadStatus);

		app = (MyApplication) getApplication();
//		app.setDownloadStatusText(dStatusText);
		Singleton.getInstance().setDownloadInProgress(true);
		dwProgressBar = (ProgressBar) findViewById(R.id.dw_progress);
		dwProgressBar.setIndeterminate(false);
		dwProgressBar.setVisibility(View.INVISIBLE);
		Log.info("calling DownloadPravachanFiles async task");
		if(Constants.MODE_IS_LIVE) {
			Settings.System.putInt(this.getContentResolver(),Global.AUTO_TIME, 1);
			Settings.System.putInt(this.getContentResolver(),Global.AUTO_TIME_ZONE, 1);	
		}
		new DownloadPravachanFiles().execute();
	}

	public void showProgress(String string) {
		dwProgressBar.setVisibility(View.VISIBLE);
	}

	public void stopProgress() {
		dwProgressBar.setVisibility(View.INVISIBLE);
	}

	/*
	 * Below methods used to show how many bytes are downloaded megsTotal will
	 * provide bytes downloaded
	 */
	private long megsTotal = 0;

	public void bytesTransferred(CopyStreamEvent event) {
		bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
	}

	public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
		long megs = totalBytesTransferred / 1000000;
		for (long l = megsTotal; l < megs; l++) {
			System.err.print("#");
		}
		megsTotal = megs;
		dwProgressBar.setProgress(dwProgressBar.getProgress() + (int) bytesTransferred);
		mHandler.postDelayed(mUpdateTime, 1000);
	}

	/**
	 * Background Async Task to download file
	 * */

	class DownloadPravachanFiles extends AsyncTask<Integer, Integer, Integer> {
		
		//TODO: return results from background task based on it in below onPostExecute call end activity
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			Singleton.getInstance().setDownloadInProgress(false);
			dwProgressBar.setVisibility(View.INVISIBLE);
			
			if(result.intValue() == Constants.FTP_DOWNLOAD_COMPLETE) {
				TxtWelcome.setText(getString(R.string.finishdownloading));
			}else {
				TxtWelcome.setText("(100."+ result + ")" + getString(R.string.err100));
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}
		
		
		//TODO: when one file is downloaded verify checksum and update UI using onProgressUpdate
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values[0].intValue() == -1) {
				app.setDownloadStatus();
				TextView dStatusText = (TextView) context.findViewById(R.id.txt_downloadStatus);
				app.setDownloadStatusText(dStatusText);
			}
			if(values[0].intValue() == 0) {
				dwProgressBar.setVisibility(View.VISIBLE);	
			}else if(values[0].intValue() == 1) {
				if(!"".equalsIgnoreCase(recentDownloadedFile)) {
					app.updatePravachanSchedule(displayList);
					app.setDownloadStatus();
					TextView dStatusText = (TextView) context.findViewById(R.id.txt_downloadStatus);
					app.setDownloadStatusText(dStatusText);
				}
				
			}
			
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected Integer doInBackground(Integer... f_url) {
			Log.info("Entering DownloadPravachanFiles background task");
			//TODO: read schedule list from MyApplication?? decide based on invocation type (scheduled or user driven)
			ScheduleManager schMgr = new ScheduleManager();
			try {
				Singleton.getInstance().setDownloadInProgress(true);
				scheduleList = schMgr.getAllPravachanRecords(context);
				
				((MyApplication) context.getApplication()).updatePravachanSchedule(scheduleList);
				publishProgress(-1);
				
				if(scheduleList != null) {
					Log.info("schedule list not null");
					//Get downloaded bytes vs pending bytes
					getFileSizeStatus(scheduleList);
					//check if all files are downloaded and if schedule is incomplete
					int status = filterFilesToDownload(scheduleList);
					Log.debug("status code:" + status);
					if(status == Constants.FTP_DOWNLOAD_COMPLETE || status == Constants.INCOMPLETE_SCHEDULE){
						Log.debug("file download complete OR Incomplete schedule");
						return status;
						
					} else {
						//download pending files..
						Log.info("processing files to download");
						dwProgressBar.setMax((int) totalBytesToDownload);
						dwProgressBar.setProgress((int) totalBytesDownloaded);
						publishProgress(0);
						mHandler.postDelayed(mUpdateTime, 1000);
						//TODO: remove after testing
						try{
							Thread.sleep(20000);
						}catch(Exception e){
							
						}
						
						//TODO: for testing
						for(int i=0; i< scheduleList.size(); i++) {
							System.out.println("Schedule: " + scheduleList.get(i).getFileName());
						}
						//create copy of schedule list
						displayList.addAll(scheduleList);
						
						DownloadManager manager = new DownloadManager(context);
						for (int i = 0; i < scheduleList.size(); i++) {
							if("complete".equalsIgnoreCase(scheduleList.get(i).getDownloadStatus()) || "skipped".equalsIgnoreCase(scheduleList.get(i).getDownloadStatus())){
								//skip file	from downloading
							}else{
								Log.debug("initiating download for:"+ scheduleList.get(i).getFileName());
								int downloadstatus = manager.downloadFiles(scheduleList.get(i));
								Log.debug("download status return value" + downloadstatus);
								if (downloadstatus == Constants.FTP_DOWNLOAD_COMPLETE) {
									Log.debug("download complete for " + scheduleList.get(i).getFileName());
									displayList.get(i).setDownloadStatus("complete");
									publishProgress(1);
									continue;
								}else{
									Log.debug("download failed for " + scheduleList.get(i).getFileName());
									return downloadstatus;
								}
							}
							
						}
						Log.info("after for loop forwarding to end");
						return Constants.FTP_DOWNLOAD_COMPLETE;
					}
					
				}else{ //No records found for download
					Log.info("No records found for download");
					return Constants.NO_SCHEDULE_FOUND;
				}
				
			} catch (Exception e) {
				Log.error("Exception in pravachan schedule list:" + e.getMessage(), e);
				return Constants.DOWNLOAD_TASK_EXCEPTION;
			}
			
		}

		/* This method iterates through pravachan schedule list and returns if all 4 files are downloaded or not */
		/*private boolean isFileDownloadComplete(List<MediaSchedule> scheduleList) {
			Iterator<MediaSchedule> itr = scheduleList.iterator();
			int fileCount=0;
			while(itr.hasNext()){
				MediaSchedule schedule = itr.next();
				
				if(schedule.getDownloadStatus().equalsIgnoreCase("complete") || schedule.getDownloadStatus().equalsIgnoreCase("skipped")) {
					fileCount++;
				}
			}
			if(fileCount==Constants.MAX_PRAVACHAN_SCHEDULE_FILES){
				return true;
			}else return false;
		}*/
		
		/* This method iterates through list and identifies if file download is complete, if download schedule is incomplete and return number of files to download*/
		private int filterFilesToDownload(List<MediaSchedule> scheduleList) {
			Log.info("Entering filterFilesToDownload");
			int filesToDownload=0;
			int filesDownloaded=0;
			try {
				Iterator<MediaSchedule> itr = scheduleList.iterator();
				int i=0;	
				while(itr.hasNext()) {
					MediaSchedule schedule = itr.next();
					if(Constants.PRAVACHAN.equalsIgnoreCase(schedule.getScheduleType())){
						//add pravachan count
						i++;	
					}
					Log.debug("download status for " + schedule.getFileName() + ":" + schedule.getDownloadStatus());
					if((schedule.getDownloadStatus().equalsIgnoreCase("complete")) || (schedule.getDownloadStatus().equalsIgnoreCase("skipped"))) {
						//dont add to updated list
						filesDownloaded++;
					}else{
						Log.debug("adding filesToDownload count for " + schedule.getFileName());
						filesToDownload++;
					}
				}
				if (filesToDownload > 0) return Constants.FTP_DOWNLOAD_PENDING;
				if(filesToDownload ==0 && i < Constants.MAX_PRAVACHAN_SCHEDULE_FILES) return Constants.INCOMPLETE_SCHEDULE;
				if(filesDownloaded == Constants.MAX_PRAVACHAN_SCHEDULE_FILES) return Constants.FTP_DOWNLOAD_COMPLETE;
			}catch(Exception e){
				Log.error("Exception in filterFilesToDownload"+e.getMessage(), e);
			}
			return filesToDownload;
		}

		private void getFileSizeStatus(List<MediaSchedule> scheduleList) {
			Log.info("Entering getFileSizeStatus");
			long bytesDownloaded = 0;
			long bytesToDownload = 0;
			try {
				Iterator<MediaSchedule> itr = scheduleList.iterator();
				while (itr.hasNext()) {
					MediaSchedule schedule = itr.next();
					bytesDownloaded = bytesDownloaded + schedule.getDownloadedBytes();
					bytesToDownload = bytesToDownload + schedule.getFileSize();
					
				}
			} catch (Exception e) {
				Log.error("exception in filesizeStatus:" + e.getMessage(),e);
			}
			Log.debug("DownloadedBytes:" + bytesDownloaded + " & TotalBytesToDownload:" + bytesToDownload);
			totalBytesDownloaded = bytesDownloaded;
			totalBytesToDownload = bytesToDownload;
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
		((MyApplication) getApplication()).setLocaleConfiguration();
		
	}
}
