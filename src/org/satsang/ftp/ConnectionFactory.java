package org.satsang.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

//import com.jcraft.jsch.ChannelSftp;

public interface ConnectionFactory {

	public FTPClient connect() throws Exception;

	public FTPSClient secureConnect() throws Exception;
	// public ChannelSftp connectSSH() throws Exception;

}
