package org.satsang.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MD5Checksum {
	static private final Logger Log = LoggerFactory.getLogger(MD5Checksum.class);
	/*
     * Calculate checksum of a File using MD5 algorithm
     */
    public static String checkSum(String path){
    	Log.info("verifying checksum for " + path);
        StringBuffer sb = new StringBuffer("");
        try {
            FileInputStream fis = new FileInputStream(path);
            MessageDigest md = MessageDigest.getInstance("MD5");
          
            //Using MessageDigest update() method to provide input
            byte[] buffer = new byte[8192];
            int numOfBytesRead;
            while( (numOfBytesRead = fis.read(buffer)) != -1){
                md.update(buffer, 0, numOfBytesRead);
            }
            byte[] mdbytes = md.digest();
          //convert the byte to hex format
            
            for (int i = 0; i < mdbytes.length; i++) {
            	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
         
            //System.out.println("Digest(in hex format):: " + sb.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        Log.info("checksum for " + path + " is" + sb.toString());   
       return sb.toString();
    }

}