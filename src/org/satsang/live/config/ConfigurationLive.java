package org.satsang.live.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.PropertyResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Environment;

/* This class reads configuration properties from liveSatsang.conf file and loads 
 * configuration in the memory. */
public class ConfigurationLive {
	static private final Logger Log = LoggerFactory.getLogger(ConfigurationLive.class);
	
	private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();

	protected ConfigurationLive() {

	}

	static {
		try {
			Log.info("Loading configuration data");
			// Below code is used if liveSatsang.conf and client.conf is
			// encrypted.
			/*
			 * BlowfishEasy manager = new BlowfishEasy(""); PersistenceManager
			 * pMgr = new PersistenceManager(); String sdcard =
			 * Environment.getExternalStorageDirectory().getAbsolutePath();
			 * List<String> lsList =
			 * pMgr.read(sdcard+"/livesatsang/conf/"+Constants.configFile);
			 * ListIterator<String> itr = lsList.listIterator();
			 * while(itr.hasNext()){ String line = (String) itr.next(); String
			 * decLine = manager.decryptString(line);
			 * if(!decLine.startsWith("#")){ int index = decLine.indexOf("=");
			 * String key = decLine.substring(0, index); String value =
			 * decLine.substring(index+1, decLine.length()); map.put(key,
			 * value); // Log.d("D","Adding: " + key + ":" + value); } }
			 * System.out.println("loaded config1");
			 * 
			 * List<String> client =
			 * pMgr.read(sdcard+"/livesatsang/conf/client.conf"); itr =
			 * client.listIterator(); while(itr.hasNext()){ String line =
			 * (String) itr.next(); String decLine =
			 * manager.decryptString(line); if(!decLine.startsWith("#")){ int
			 * index = decLine.indexOf("="); String key = decLine.substring(0,
			 * index); String value = decLine.substring(index+1,
			 * decLine.length()); map.put(key, value); //
			 * Log.d("D","Adding client: " + key + ":" + value); } }
			 * System.out.println("loaded config2");
			 */

			// Below code for reading from non encrypted liveSatsang.conf and
			// client.conf files

			String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
			InputStream inst = new FileInputStream(sdcard + "/livesatsang/conf/" + Constants.configFile);
			PropertyResourceBundle bundle = new PropertyResourceBundle(inst);
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = bundle.getString(key);
				// Log.d("D","Adding: " + key + ":" + value);
				map.put(key, value);

			}
			
			System.out.println("loaded config1");
			// Override existing properties by loading client.conf (client.conf
			// to contain client specific configuration)
			inst = new FileInputStream(sdcard + "/livesatsang/conf/client.conf");
			PropertyResourceBundle client = new PropertyResourceBundle(inst);
			keys = client.getKeys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = client.getString(key);
				// Log.d("D","Adding: " + key + ":" + value);
				map.put(key, value);

			}
			System.out.println("loaded config2");
		} catch (Exception e) {
			Log.error("Exception:readConfigFile():" + e.getMessage(), e);
		}

	}

	/*
	 * Get the absolute path of this program installation and search for config
	 * file
	 */
	/*
	 * public static HashMap<String,String> readConfigFile(){ //
	 * logger.info("Entering:Configuration.readConfigFile()"); HashMap<String,
	 * String> map = new HashMap<String, String>(); try { InputStream inst =
	 * Thread
	 * .currentThread().getContextClassLoader().getResourceAsStream(Constants
	 * .configFile); PropertyResourceBundle bundle = new
	 * PropertyResourceBundle(inst); Enumeration<String> keys =
	 * bundle.getKeys(); while (keys.hasMoreElements()){ String key = (String)
	 * keys.nextElement(); String value = bundle.getString(key); map.put(key,
	 * value); // logger.debug("Adding: " + key + ":" + value); } }catch
	 * (Exception e){ //TODO: Log error in log file or in database. Add file
	 * handling exception also. logger.error("Exception:readConfigFile():" +
	 * e.getMessage(), e); }
	 * logger.info("Exiting:Configuration.readConfigFile()"); return map; }
	 */
	public static ConcurrentHashMap<String, String> readConfigFile() {
		if(map.isEmpty()) {
			reloadMap();
		}
		return map;
	}

	

	public static String getValue(String key) {
		if(map.isEmpty()) {
			reloadMap();
		}
		return map.get(key);
	}
	
	public static void discardMap(){
		map.clear();
	}

	public static void reloadMap(){
		try {
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		InputStream inst = new FileInputStream(sdcard + "/livesatsang/conf/" + Constants.configFile);
		PropertyResourceBundle bundle = new PropertyResourceBundle(inst);
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = bundle.getString(key);
			// Log.d("D","Adding: " + key + ":" + value);
			map.put(key, value);

		}
		Log.debug("loaded config1");
		// Override existing properties by loading client.conf (client.conf
		// to contain client specific configuration)
		inst = new FileInputStream(sdcard + "/livesatsang/conf/client.conf");
		PropertyResourceBundle client = new PropertyResourceBundle(inst);
		keys = client.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = client.getString(key);
			// Log.d("D","Adding: " + key + ":" + value);
			map.put(key, value);

		}
		Log.debug("loaded config2");
	} catch (Exception e) {
		Log.error("Exception:readConfigFile():" + e.getMessage(), e);
	}

	}
	
	/** update values from database and or from string in hashmap **/
	public static void addValues(HashMap<String,String> keyValues){
		if(keyValues !=null && !keyValues.isEmpty()) {
			//TODO: synchronize read write to map
				map.putAll(keyValues);
			
		}
	}
}
