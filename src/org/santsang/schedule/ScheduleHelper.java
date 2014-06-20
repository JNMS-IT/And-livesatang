package org.santsang.schedule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.satsang.bo.MediaSchedule;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.satsang.util.DateUtil;
import org.satsang.util.MD5Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleHelper {
	static private final Logger Log = LoggerFactory.getLogger(ScheduleHelper.class);
	
	/**This method is used to synchronize Bhajan, Pravachan and/or News with local file size. */
	public ArrayList<MediaSchedule> syncScheduleFiles(ArrayList<MediaSchedule> scheduleList) {
		Log.info("Entering syncScheduleFiles");
		try {
			// By Rohit - As Delete a File code is already there below,what I saw from my projects
			// is sometimes file is not deleted from system, may be because there is some unused handle associated with it
			// So its always better to call System.gc() before it
			System.gc();

			Iterator<MediaSchedule> itr = scheduleList.iterator();
			ArrayList<MediaSchedule> updatedList = new ArrayList<MediaSchedule>();
			while (itr.hasNext()) {
				try {
					MediaSchedule schedule = itr.next();
					String fileName = schedule.getFileName();
					String downloadDir = getDownloadDirectory(schedule);
					//TODO: removed this to check checksum every time && (!"complete".equalsIgnoreCase(schedule.getDownloadStatus()) && (!"skipped".equalsIgnoreCase(schedule.getDownloadStatus())))
					//Once checksum is verified no need to check it again.. fix code below
					if (fileName != null && (!"".equalsIgnoreCase(fileName))) {
						Log.debug("Processing Schedule: " + schedule.getScheduleDate() + " File:"+ schedule.getFileName());
						File mediaFile = new File(downloadDir + fileName);
						if (mediaFile.exists() && mediaFile.isFile()) {
							long mediaFileLength = mediaFile.length();
							long checkRetry = schedule.getCheckSumRetryCount();
							Log.debug("checkSum retry count is: " + checkRetry + " for " + schedule.getFileName());
							// If checksum verification is success then only mark it as download complete
							if(mediaFileLength == schedule.getFileSize()) {
								String localChecksum = MD5Checksum.checkSum(mediaFile.getAbsolutePath());
								if(localChecksum.equalsIgnoreCase(schedule.getCheckSum())) {
									Log.debug("Updating status to complete for:" + schedule.getFileName());
									schedule.setCheckSumStatus("complete");
									schedule.setDownloadStatus("complete");
									schedule.setDownloadedBytes(mediaFileLength);
								}else {
									if(checkRetry > Constants.CHECKSUM_RETRY_COUNT) {
										Log.debug("Updating status to skipped for:" + schedule.getFileName());
										schedule.setDownloadStatus("skipped");
										schedule.setCheckSumStatus("fail");
										schedule.setCheckSumRetryCount(checkRetry+1);
										schedule.setDownloadedBytes(0);
										mediaFile.delete();
									}else {
										Log.debug("Updating status to fail and deleting file :" + schedule.getFileName());
										schedule.setCheckSumRetryCount(checkRetry+1);
										schedule.setCheckSumStatus("fail");
										schedule.setDownloadStatus("processing");
										schedule.setDownloadedBytes(0);
										mediaFile.delete();
									}
									
								}
							}else if (mediaFileLength < schedule.getFileSize()) {
								Log.debug("Setting download status to processing for:"+ schedule.getFileName());
								schedule.setDownloadedBytes(mediaFileLength);
								schedule.setDownloadStatus("processing");
							} else if(mediaFileLength > schedule.getFileSize()){
								//File is corrupt
								if(checkRetry > Constants.CHECKSUM_RETRY_COUNT) { //This is download retry count
									Log.debug("Checksum retry exceeded status set to skipped for:"+ schedule.getFileName());
									schedule.setDownloadStatus("skipped");
									schedule.setCheckSumStatus("fail");
									schedule.setCheckSumRetryCount(checkRetry+1);
								}else {
									Log.debug("Setting download status to null for:"+ schedule.getFileName());
									schedule.setDownloadStatus("null");
									schedule.setCheckSumRetryCount(checkRetry+1);
								}
								schedule.setDownloadedBytes(0);
								mediaFile.delete();
							}//end of corrupt file handling
						} else {
							//Media file does not exist. Check if file was deleted as a part of checksum verification retry failure
							if(!"skipped".equalsIgnoreCase(schedule.getDownloadStatus())){
								Log.debug("download status null for:"+ schedule.getFileName());
								schedule.setDownloadStatus("null");
								schedule.setDownloadedBytes(0);	
							}
						}
					} else {
						// ignore file is null or name is blank

					}
					//Additional check for deleting skipped files
					if (schedule.getDownloadStatus().equalsIgnoreCase("skipped")) { //will skip bhajan as well
						Log.debug("deleting skipped file:"+schedule.getFileName());
						File discardFile = new File(downloadDir + schedule.getFileName());
						if (discardFile.exists())
							discardFile.delete();
					}
					int compare = DateUtil.compareDate(schedule.getScheduleDate());
					if (compare == 1 && !Constants.BHAJAN.equalsIgnoreCase(schedule.getScheduleType())) { //do not delete bhajan
						File oldFile = new File(downloadDir + schedule.getFileName());
						Log.debug("Deleting old file: "+oldFile);
						if (oldFile.exists()) {
							oldFile.delete();
						}
					} else {
						updatedList.add(schedule);
					}
						
				} catch (Exception e) {
					Log.error("Error syncScheduleFiles:" + e.getMessage(), e);
				}

			}
			Log.info("Returning updated List");
			return updatedList;
				

		} catch (Exception e) {
			Log.error("Error synchronizing downloaded files and schedule: " + e.getMessage(), e);
		}
		Log.debug("Returning null value");
		return null;
	}

	private String getDownloadDirectory(MediaSchedule schedule) {
		String schType = schedule.getScheduleType();
		if(schType != null) {
			if(Constants.BHAJAN.equalsIgnoreCase(schType)) {
				return ConfigurationLive.getValue("bhajan.download.directory");
			}else {
				return ConfigurationLive.getValue("local.media.files.directory");
			}
		}
		return null;
	}

	//TODO: apply this logic carefully as bhajan file should not be deleted. Assuming 
	public void cleanUpFiles(ArrayList<MediaSchedule> scheduleList, String downloadDir) {
		try {
			// By Rohit - As Delete a File code is already there below,what I saw from my projects
	     	// is sometimes file is not deleted from system, may be because there is some unused handle associated with it
	        // So its always better to call System.gc() before it
			System.gc();
			HashMap<String,String> validFiles = new HashMap<String,String>();
			if(scheduleList != null && scheduleList.size() > 0)
			{
		     Iterator<MediaSchedule> itr = scheduleList.iterator();
			 while(itr.hasNext()){
				validFiles.put(itr.next().getFileName(), itr.next().getFileName());
			 }
		    }
			File dir = new File(downloadDir);
			if(dir.exists() && dir.isDirectory()){
				File[] files = dir.listFiles();
				for(int i=0; i< files.length;i++){
					String fileName = files[i].getName();
					System.out.println("File name:" + fileName);
					String vFName = validFiles.get(fileName);
					if(vFName == null || ("".equalsIgnoreCase(vFName))) {
						File invalidFile = new File(fileName);
						invalidFile.delete();
					}
				}	
			}
			
		}catch(Exception e){
			System.out.println("Error in cleanUpFiles"+e);
		}
	}
	
	
}
