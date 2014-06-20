package org.satsang.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPSClient;
import org.satsang.audit.AuditConstants;
import org.satsang.audit.EventHandler;
import org.satsang.live.config.Constants;

import android.util.Log;

public class FTPUtil {

	/* Standalone utility for downloading file NOT IN USE. RETAINED FOR FUTURE USE. NOT TESTED */
	public static int download(String serverFileName, String localFile, boolean isSecure, int bufferSize) {
		FTPClient ftp = null;
		FTPSClient ftps = null;
		boolean downloadStatus = false;
		OutputStream output = null;
		
		try {
			ConnectionFactory factory = new FTPConnectionFactory();
			if (isSecure) {
				Log.d("livesatsang", "Connecting in Apache secure mode");
				ftps = factory.secureConnect();
				ftp = ftps;
			} else {
				Log.d("livesatsang", "Connecting in non secure mode");
				ftp = factory.connect();
			}
			ftp = ftps;
			ftp.setBufferSize(bufferSize);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			File downloadedFile = new File(localFile);
			long bytesDownloaded = 0;
			if (downloadedFile.exists() && downloadedFile.isFile()) {
				bytesDownloaded = downloadedFile.length();
			}
			if (bytesDownloaded == 0) {
				EventHandler.appendAuditToFile(AuditConstants.DOWNLOAD_START, serverFileName, AuditConstants.AUDIT_TYPE_AUDIT);
				System.out.println("Downloading normal mode:" + serverFileName);
				output = new FileOutputStream(localFile);
				downloadStatus = ftp.retrieveFile(serverFileName, output);
			} else {
				EventHandler.appendAuditToFile(AuditConstants.DOWNLOAD_START, "Seek Mode:" + serverFileName, AuditConstants.AUDIT_TYPE_AUDIT);
				System.out.println("Downloading seek mode:" + serverFileName + " offset:" + bytesDownloaded);
				ftp.setRestartOffset(bytesDownloaded);
				output = new FileOutputStream(localFile, true);
				downloadStatus = ftp.retrieveFile(serverFileName, output);
			}
			ftp.logout();
			if (!downloadStatus) {
				return Constants.FILE_MIGHT_NOT_FOUND_ON_SERVER;
			}else {
				return Constants.FTP_DOWNLOAD_COMPLETE;
			}
				
		} catch (SocketTimeoutException soe) {
			Log.e("livesatsang", "Exception downloading file:" + soe.getMessage());
			return Constants.SOCKET_ERROR;
		}catch (FTPConnectionClosedException fce) {
				return Constants.FTP_CONNECTION_CLOSED;
		} catch (IOException ioe) {
			Log.e("livesatsang", "Exception:" + ioe.getMessage());
			return Constants.IO_EXCEPTION;
		}  catch (Exception e) {
			Log.e("livesatsang", "Exception:" + e.getMessage());
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
				Log.e("livesatsang", "FTPDownloadWorker: Exception closing resources");
			}
		
	}
	}

}
