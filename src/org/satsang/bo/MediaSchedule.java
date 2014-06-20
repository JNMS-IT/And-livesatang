package org.satsang.bo;

import org.satsang.util.DateUtil;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class MediaSchedule implements Parcelable {

	private static final long serialVersionUID = 4233963216903684377L;
	private String scheduleDate = null;
	private String fileName = null;
	private String scheduleTime = null;
	private long videoLength = 0;
	private long fileSize = 0;
	private String checkSum = null;
	private String langCode = null;
	private String serverPath = null;
	private String lastModifiedDateTime=null;
	private String scheduleType=null;
	private String mediaType=null;
	private String downloadStatus = null;
	private String mediaPlayStatus = null;
	private String checkSumStatus=null;
	private long downloadedBytes = 0;
	private long checkSumRetryCount=0;
	private int version = 0;
	
	public MediaSchedule(){
		
	}
	
	public MediaSchedule(Cursor mtCursor)
	{
			this.setScheduleDate(DateUtil.formatServerDate(mtCursor.getString(mtCursor.getColumnIndex("scheduleDate"))));
			this.setFileName(mtCursor.getString(mtCursor.getColumnIndex("fileName")));
			this.setScheduleTime(mtCursor.getString(mtCursor.getColumnIndex("scheduleTime")));
			this.setVideoLength(mtCursor.getLong(mtCursor.getColumnIndex("videoLength")));
			this.setFileSize(mtCursor.getLong(mtCursor.getColumnIndex("fileSize")));
			this.setCheckSum(mtCursor.getString(mtCursor.getColumnIndex("checkSum")));
			this.setLangCode(mtCursor.getString(mtCursor.getColumnIndex("langCode")));
			this.setServerPath(mtCursor.getString(mtCursor.getColumnIndex("serverPath")));
			this.setLastModifiedDateTime(mtCursor.getString(mtCursor.getColumnIndex("lastModifiedDateTime")));
			this.setScheduleType(mtCursor.getString(mtCursor.getColumnIndex("scheduleType")));
			this.setMediaType(mtCursor.getString(mtCursor.getColumnIndex("mediaType")));
			this.setDownloadStatus(mtCursor.getString(mtCursor.getColumnIndex("downloadStatus")));
			this.setMediaPlayStatus(mtCursor.getString(mtCursor.getColumnIndex("mediaPlayStatus")));
			this.setCheckSumStatus(mtCursor.getString(mtCursor.getColumnIndex("checkSumStatus")));
			this.setDownloadedBytes(mtCursor.getLong(mtCursor.getColumnIndex("downloadedBytes")));
			this.setDownloadedBytes(mtCursor.getLong(mtCursor.getColumnIndex("checkSumRetryCount")));
			// By Rohit
			this.setVersion(mtCursor.getInt(mtCursor.getColumnIndex("version")));
	}
	private MediaSchedule(Parcel in){
//		in.createBinderArrayList();
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flag) {
		out.writeString(scheduleDate);
		out.writeString(fileName);
		out.writeString(scheduleTime);
		out.writeLong(videoLength);
		out.writeLong(fileSize);
		out.writeString(checkSum);
		out.writeString(langCode);
		out.writeString(serverPath);
		out.writeString(lastModifiedDateTime);
		out.writeString(scheduleType);
		out.writeString(mediaType);
		out.writeString(downloadStatus);
		out.writeString(mediaPlayStatus);
		out.writeString(checkSumStatus);
		out.writeLong(downloadedBytes);
		out.writeLong(checkSumRetryCount);
		out.writeInt(version);
		
	}

	public static final Parcelable.Creator<MediaSchedule> CREATOR = new Parcelable.Creator<MediaSchedule>() {
	    public MediaSchedule createFromParcel(Parcel in) {
	        return new MediaSchedule(in);
	    }

	    public MediaSchedule[] newArray(int size) {
	        return new MediaSchedule[size];
	    }
	};
	public String getScheduleDate() {
		return scheduleDate;
	}
	
	public void setScheduleDate(String scheduleDate) {
		this.scheduleDate = scheduleDate;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getScheduleTime() {
		return scheduleTime;
	}
	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
	public long getVideoLength() {
		return videoLength;
	}
	public void setVideoLength(long videoLength) {
		this.videoLength = videoLength;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	public String getServerPath() {
		return serverPath;
	}
	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}
	public String getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	public void setLastModifiedDateTime(String lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	public String getScheduleType() {
		return scheduleType;
	}
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}
	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getDownloadStatus() {
		return downloadStatus;
	}
	public void setDownloadStatus(String downloadStatus) {
		this.downloadStatus = downloadStatus;
	}
	
	public String getMediaPlayStatus() {
		return mediaPlayStatus;
	}
	public void setMediaPlayStatus(String mediaPlayStatus) {
		this.mediaPlayStatus = mediaPlayStatus;
	}
	public String getCheckSumStatus() {
		return checkSumStatus;
	}
	public void setCheckSumStatus(String checkSumStatus) {
		this.checkSumStatus = checkSumStatus;
	}
	public long getDownloadedBytes() {
		return downloadedBytes;
	}
	public void setDownloadedBytes(long downloadedBytes) {
		this.downloadedBytes = downloadedBytes;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public long getCheckSumRetryCount() {
		return checkSumRetryCount;
	}

	public void setCheckSumRetryCount(long checkSumRetryCount) {
		this.checkSumRetryCount = checkSumRetryCount;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	

	
}
