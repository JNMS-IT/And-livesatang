package org.satsang.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.satsang.file.PersistenceManager;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHandler {
	static private final Logger Log = LoggerFactory.getLogger(EventHandler.class);

	public synchronized static void appendAuditToFile(String eventName, String eventDetail, String eventType) {
		Log.trace("Inside AuditEventHandler appendAuditToFile()");
		PersistenceManager manager = new PersistenceManager();
		try {
			String eventStr = eventName + "|" + new Date() + "|" + eventDetail;
			List<String> auditList = new ArrayList<String>();
			auditList.add(eventStr);
			if(AuditConstants.AUDIT_TYPE_AUDIT.equalsIgnoreCase(eventType)) {
				manager.append(auditList, getAuditFile());	
			}else {
				manager.append(auditList, getErrorLogFile());
			}
			
		} catch (Exception e) {
			Log.error("Error appending Audit record"+e.getMessage(),e);
		}
	}
	
	

	public static void deleteAuditRecords(String eventType) {
		Log.trace("Entering deleteAuditRecords()");
		PersistenceManager manager = new PersistenceManager();
		try {
			if(AuditConstants.AUDIT_TYPE_AUDIT.equalsIgnoreCase(eventType)) {
				manager.store(new ArrayList<String>(), getAuditFile());	
			}else {
				manager.store(new ArrayList<String>(), getErrorLogFile());
			}
			
		} catch (Exception e) {
			 Log.error("Error deleting audit records"+e.getMessage(), e);
		}
	}

	/**
	 * This method is used to read records that are not sent to server. It
	 * return the list for ex LSStart|Sun Oct 21 08:59:11 IST 2012|Event Details
	 * LSEnd|Sun Oct 21 09:59:11 IST 2012|Event Details
	 */
	public String getAuditRecordsToUpload(String eventType) throws Exception {
		 Log.trace("Entering getAuditRecordsFromFile()");
		try {
			PersistenceManager manager = new PersistenceManager();
			List<String> auditList;
			if(AuditConstants.AUDIT_TYPE_AUDIT.equalsIgnoreCase(eventType)) {
				auditList = manager.read(getAuditFile());	
			}else {
				auditList = manager.read(getErrorLogFile());
			}
			
			Iterator<String> itr = auditList.iterator();
			StringBuffer events = new StringBuffer();
			while (itr.hasNext()) {
				events.append(itr.next() + "#"); 
			}
			return events.toString();
		} catch (Exception e) {
			 Log.error("Error retrieving audit records from file"+e.getMessage(),e);
			throw new Exception("Error retrieving audit records from file");
		}
	}

	/** Utility method to read audit.dat file location */
	private static String getAuditFile() {
		try {
			String auditFile = ConfigurationLive.getValue("satsang.audit.fileName");
			return auditFile;
		} catch (Exception e) {
			 Log.error("Exception:" + e.getMessage(), e);
		}
		// logger.info("getAuditFile()");
		return "";
	}
	
	private static String getErrorLogFile() {
		try {
			String auditFile = ConfigurationLive.getValue("satsang.error.log.fileName");
			return auditFile;
		} catch (Exception e) {
			 Log.error("Exception:" + e.getMessage(), e);
		}
		// logger.info("getAuditFile()");
		return "";
	}

}
