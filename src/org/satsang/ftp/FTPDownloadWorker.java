package org.satsang.ftp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.android.activities.DownloadActivity;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPSClient;
import org.satsang.audit.AuditConstants;
import org.satsang.audit.EventHandler;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This class is responsible for downloading files from server with offset and
 * without offset
 * 
 * @author pushparaj
 * 
 */
public class FTPDownloadWorker {
	static private final Logger Log = LoggerFactory.getLogger(FTPDownloadWorker.class);
	
	private DownloadActivity downloadActivity;

	public FTPDownloadWorker(DownloadActivity downloadActivity) {
		this.downloadActivity = downloadActivity;
	}

	/**
	 * Below method downloads fresh file without offset and stored locally. It
	 * assumes all the validations are done by caller
	 * 
	 * @param fileName
	 * @param serverPath
	 * @param localFile
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */

	public int download(String serverFileName, String serverPath, String localFile, long bytesDownloaded) {
		Log.info("Entering:FTPDownloadWorker.download()");
		FTPClient ftp = null;
		FTPSClient ftps = null;
		boolean downloadStatus = false;
		OutputStream output = null;
		try {
			ConnectionFactory factory = new FTPConnectionFactory();
			if (isFTPSecure()) {
				// Use below code in case of Apache secure FTP server
				Log.debug("Connecting in Apache secure mode");
				ftps = factory.secureConnect();
				ftp = ftps;
			} else {
				Log.debug("Connecting in non secure mode");
				ftp = factory.connect();
			}
			ftp = ftps;
			ftp.setBufferSize(4096);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.setCopyStreamListener(downloadActivity);
			if (bytesDownloaded == 0) {
				EventHandler.appendAuditToFile(AuditConstants.DOWNLOAD_START, serverFileName, AuditConstants.AUDIT_TYPE_AUDIT);
				Log.debug("Downloading normal mode:" + serverFileName);
				output = new FileOutputStream(localFile);
				downloadStatus = ftp.retrieveFile(serverPath + "/" + serverFileName, output);
			} else {
//				EventHandler.appendAuditToFile(AuditConstants.DOWNLOAD_START, "Seek Mode:" + serverFileName, AuditConstants.AUDIT_TYPE_AUDIT);
				Log.debug("Downloading seek mode:" + serverFileName + " offset:" + bytesDownloaded);
				ftp.setRestartOffset(bytesDownloaded);
				output = new FileOutputStream(localFile, true);
				downloadStatus = ftp.retrieveFile(serverPath + "/" + serverFileName, output);
			}
			ftp.logout();

			if (!downloadStatus) {
				return Constants.FILE_MIGHT_NOT_FOUND_ON_SERVER;
			}else {
				EventHandler.appendAuditToFile(AuditConstants.DOWNLOAD_END, serverFileName, AuditConstants.AUDIT_TYPE_AUDIT);
				return Constants.FTP_DOWNLOAD_COMPLETE;
			}
				
		} catch(FTPConnectionClosedException ftce){
			Log.error("FTPConnectionException:"+ftce.getMessage(),ftce);
			return Constants.FTP_CONNECTION_CLOSED;
		} catch (SocketTimeoutException soe) {
			Log.error("Exception downloading file:" + soe.getMessage(), soe);
			return Constants.SOCKET_TIMEOUT_ERROR;
		} catch (SocketException se){
			Log.error("Exception downloading file:" + se.getMessage(), se);
			return Constants.SOCKET_ERROR;
		} catch (IOException ioe) {
			Log.error("Exception downloading file:" + ioe.getMessage(), ioe);
			return Constants.IO_EXCEPTION;
		} catch (Exception e) {
			Log.error("Exception downloading file:" + e.getMessage(), e);
			return Constants.OTHER_FTP_EXCEPTION;
		} finally {
			try {
				if (ftp != null && ftp.isConnected())
					ftp.disconnect();
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (Exception f) {
				Log.error("FTPDownloadWorker: Exception closing resources");
			}
		}
		
	}

	/**
	 * Below method downloads file from server with offset. It assumes all
	 * validations are done by caller
	 * 
	 * @param serverFileName
	 * @param serverPath
	 * @param localFile
	 * @param offset
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */

	/*
	 * public void download(String serverFileName, String serverPath, String
	 * localFile, long offset) throws SocketTimeoutException, Exception{
	 * Log.i("I","FTPDownloadWorker.download - RESUME"); FTPClient ftp = null;
	 * FTPSClient ftps = null; // ChannelSftp sftp = null;
	 * 
	 * try { OutputStream output = new FileOutputStream(localFile, true);
	 * ConnectionFactory factory = new FTPConnectionFactory();
	 * if(isFTPSecure()){ Log.d("D","Connecting secure Apache FTP"); ftps =
	 * factory.secureConnect(); ftps.setFileType(FTP.BINARY_FILE_TYPE);
	 * ftps.setRestartOffset(offset);
	 * ftps.setCopyStreamListener(downloadActivity); boolean downloadComplete =
	 * ftps.retrieveFile(serverPath+"/"+serverFileName, output);
	 * if(!downloadComplete) throw new Exception("Failed to download"); //Below
	 * code for downloading using SSH // sftp = factory.connectSSH(); //
	 * sftp.get(serverPath+"/"+serverFileName, output, message,
	 * ChannelSftp.RESUME, offset); //PMD either use DownloadMonitor or
	 * ShowDownloadProgressMessage // sftp.get(serverPath+"/"+serverFileName,
	 * output, ChannelSftp.RESUME, offset); //
	 * logger.debug("Download complete:"+serverFileName + "EOF " +
	 * sftp.isEOF()); }else { ftp = factory.connect();
	 * ftp.setFileType(FTP.BINARY_FILE_TYPE); ftp.setRestartOffset(offset);
	 * ftp.setCopyStreamListener(downloadActivity); boolean downloadComplete =
	 * ftp.retrieveFile(serverPath+"/"+serverFileName, output);
	 * if(!downloadComplete) throw new Exception("Failed to download"); }
	 * 
	 * }catch (SocketTimeoutException soe){ Log.e("E","Exception in Resume:");
	 * throw soe; } catch (IOException ioe){ Log.e("E","Exception:"); throw ioe;
	 * } catch(Exception e){ Log.e("E","Exception:"); throw e; }
	 * 
	 * }
	 */

	private boolean isFTPSecure() {
		try {
			return Boolean.valueOf(ConfigurationLive.getValue("media.satsang.ftp.isSecure")).booleanValue();
		} catch (Exception e) {
			Log.error("Error reading FTP secure property");
		}
		return false;
	}
}
