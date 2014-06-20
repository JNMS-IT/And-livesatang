package org.satsang.ftp;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/* Utility class that reads configuration and return FTP connection */
public class FTPConnectionFactory implements ConnectionFactory {
	static private final Logger Log = LoggerFactory.getLogger(FTPConnectionFactory.class);

	public FTPClient connect() throws SocketTimeoutException, Exception {
		Log.info("Inside FTPConnectionFactory connect()");
		FTPClient ftp = null;
		String ftpUser = getFTPUserName();
		String ftpPass = getFTPUserPassword();
		try {
			/**
			 * If proxy is to be used then following code can be added (need to
			 * define property in config file if(proxyHost !=null) { ftp = new
			 * FTPHTTPClient(proxyHost, proxyPort, proxyUser, proxyPassword); }
			 * else { ftp = new FTPClient(); }
			 */
			ftp = new FTPClient();
			int ftpPort = getRemoteServerPort();
			if (ftpPort == -1) {
				// logger.debug("connect() No port configured");
				ftp.connect(getRemoteServerURL());
			} else {
				Log.debug("connecting to server");
				ftp.connect(getRemoteServerURL(), ftpPort);
			}
			if (ftpUser != null && ftpPass != null) {
				Log.debug("authenticating server");
				ftp.login(ftpUser, ftpPass);
			} else {
				// logger.debug("No authentication information available");
				// ftp.login("anonymous", "anonymous@anonymous"); No
				// authentication required
			}

			ftp.setDefaultTimeout(getFTPTimeout());
			ftp.enterLocalPassiveMode(); // new line add - 03-02-2013
			int reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				Log.error("FTP Server reply: "+reply);
				throw new FTPConnectionClosedException(reply+""); //TODO: use this code to display on UI
			}
			ftp.setControlKeepAliveTimeout(getKeepAlive());
			ftp.setDataTimeout(getDataTimeout());
			Log.debug("Returning FTP client object");
			return ftp;
		} catch(FTPConnectionClosedException fse){
			throw fse;
		} catch (SocketTimeoutException ste) {
			throw ste;
		} catch (SocketException soe) {
			throw soe;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	/* Connect using Apache secure FTP APIs */
	public FTPSClient secureConnect() throws Exception {
		Log.info("Entering:FTPConnectionFactory.secureConnect()");
		FTPSClient ftps = null;
		String ftpUser = getFTPUserName();
		String ftpPass = getFTPUserPassword();
		try {

			ftps = new FTPSClient(true);
			ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
			ftps.setRemoteVerificationEnabled(false);
			int ftpPort = getRemoteServerPort();
			if (ftpPort == -1) {
				// logger.debug("secureConnect() No port configured");
				ftps.connect(getRemoteServerURL());
			} else {
				Log.debug("connecting to server");
				ftps.connect(getRemoteServerURL(), ftpPort);
			}
			if (ftpUser != null && ftpPass != null) {
				Log.debug("authenticating server"+ ftpUser + " pass:" + ftpPass); //TODO: remove this
				ftps.login(ftpUser, ftpPass);
			} else {
				// logger.debug("No authentication information available");
				// ftp.login("anonymous", "anonymous@anonymous"); No
				// authentication required
			}

			ftps.setDefaultTimeout(getFTPTimeout());
			ftps.enterLocalPassiveMode(); // new line add - 03-02-2013
			int reply = ftps.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftps.disconnect();
				Log.error("FTPS Server reply: " + reply);
				throw new FTPConnectionClosedException(reply+"");
			}
			ftps.setControlKeepAliveTimeout(getKeepAlive());
			ftps.setDataTimeout(getDataTimeout());
			Log.debug("Returning SFTP client object");
			return ftps;
		} catch(FTPConnectionClosedException fse){
			throw fse;	
		} catch (SocketTimeoutException ste) {
			throw ste;
		} catch (SocketException soe) {
			throw soe;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}


	/* Returns data time out in milliseconds */
	private int getDataTimeout() {
		try {
			return Integer.valueOf(ConfigurationLive.getValue("media.satsang.ftp.download.dataTimeout")).intValue();
		} catch (Exception e) {
			Log.error("media.satsang.ftp.download.dataTimeout not defined", e);

		}
		return 3000;
	}

	private String getRemoteServerURL() throws Exception {
		 try { 
			 return ConfigurationLive.getValue("media.satsangFiles.hostname").toString(); }
		 catch (Exception e){
		 Log.error("media.satsangFiles.hostname not defined", e); 
		 throw new Exception("Undefined remote server"); 
		 }
		 
		
	}

	private int getRemoteServerPort() {
		 try {
		 	 return Integer.valueOf(ConfigurationLive.getValue("media.satsangFiles.port")).intValue(); 
		} catch (Exception e){
		 Log.error("media.satsangFiles.port not defined", e); 
		} 
		 return -1;
	}

	/*
	 * If timeout is not defined in the property file default timeout set in
	 * Constants will be used
	 */
	private int getFTPTimeout() {
		try {
			return Integer.valueOf(ConfigurationLive.getValue("media.satsang.ftp.download.timeout")).intValue();
		} catch (Exception e) {
			Log.error("media.satsang.ftp.download.timeout not defined", e);
		}
		return Constants.DEFAULT_FTP_TIMEOUT_INTERVAL;
	}

	private int getKeepAlive() {
		try {

			String keepAliveStr = ConfigurationLive.getValue("media.satsang.ftp.download.keepAlive").toString();
			int keepAlive = Integer.valueOf(keepAliveStr);
			return keepAlive;
		} catch (Exception e) {
			Log.error("media.satsang.ftp.download.keepAlive not defined", e);
		}
		return 60;
	}

	private String getFTPUserName() {
		
		 try { 
			 String localDirectory =ConfigurationLive.getValue("ftp.server.username").toString(); 
			 return localDirectory; 
		}catch (Exception e){
			Log.error("ftp.server.username not defined", e); } 
		 return null;
		
	}

	private String getFTPUserPassword() {
		  try { 
			  String localDirectory =ConfigurationLive.getValue("ftp.server.password").toString(); 
			  return localDirectory; 
		  }catch (Exception e){
		  Log.error("ftp.server.password not defined", e); } 
		  return null;
	}
}
