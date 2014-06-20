package org.satsang.web;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.satsang.audit.AuditConstants;
import org.satsang.audit.EventHandler;
import org.satsang.database.DBAdapter;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.util.CommonUtil;
import org.satsang.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;

//TODO: change all params and mac Id parameters after testing
public class WebAdapter {
	static private final Logger Log = LoggerFactory.getLogger(WebAdapter.class);
	
	private Context context;
	
	public WebAdapter(Context context){
		this.context=context;
	}
	
	public void downloadAPKUpdateInfo() {
		Log.info("Entering downloadAPKUpdate()");
		try {
			SharedPreferences pref = context.getSharedPreferences("schedule.tasks", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			
			int appVersion = CommonUtil.getLocalVersion(context);
			String macId = CommonUtil.getMacId(context);
			
			//http://124.124.83.147:8080/live_santsang/update?mac=at00001&lver=2
			String url = ConfigurationLive.getValue("live.satsang.apkToken.url");
			String param = "mac="+macId+"&lver="+appVersion;
			JSONParser parser = new JSONParser();
			JSONObject Obj = parser.getJSONFromUrl(url, param, false);
			Obj = Obj.getJSONObject("data");
			
			Log.debug("Got apk information");
			
			editor.putString("schedule.tasks.apkUpdate.server.url", Obj.getString("url"));
			editor.putString("schedule.tasks.apkUpdate.download.token", Obj.getString("tkn"));
//			editor.putInt("schedule.tasks.apkUpdate.server.version", Obj.getInt("ver"));
//			editor.putLong("schedule.tasks.apkUpdate.server.fileSize", Obj.getLong("size"));
			editor.putBoolean("schedule.tasks.apkUpdate.isDownloaded", false);
			editor.commit();
			Log.debug("Stored token and URL");
		}catch(Exception e) {
			Log.error("Exception in downloadAPKUpdate"+e);
		}
	}
	public boolean downloadConfiguration(){
		boolean configUpdate=false;
		try {
			Log.info("downloadConfiugration called");
			DBAdapter dbAdapter = new DBAdapter(context);
			SharedPreferences pref = context.getSharedPreferences("schedule.tasks", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			String macId = CommonUtil.getMacId(context);
			
			//http://124.124.83.147:8080/live_santsang/data?id=at00001&type=conf
			String url = ConfigurationLive.getValue("live.satsang.configuration.url");
			String param="id="+macId+"&type=conf";
			JSONParser parser = new JSONParser();
			JSONObject response = parser.getJSONFromUrl(url, param, false);
			System.out.println("conf resp: " + response);
			if(response != null){
				JSONArray configList = response.getJSONArray("data");
				if(configList != null && configList.length() > 0){
					Log.info("config list not null " + configList.length());
					configUpdate=true;
					dbAdapter.insertConfigurationFromServer(configList);
					System.out.println("Storing configLocal: " + DateUtil.getServerFormatCurrentDateTimeString());
					editor.putString("schedule.tasks.configLocal", DateUtil.getServerFormatCurrentDateTimeString());
					editor.commit();
				}
			}
		}catch(Exception e){
			Log.error("Exception retrieving configuration from server"+e);
		}
		return configUpdate;
	}
	
	//TODO: Move marquee download code from async task to below method.
	public void downloadMarquee(){
		Log.info("Entering downloadMarquee");
		
		try {
			SharedPreferences pref = context.getSharedPreferences("schedule.tasks", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			String macId = CommonUtil.getMacId(context);
			//http://124.124.83.147:8080/live_santsang/getMarquee?mac=at00001
			String url=ConfigurationLive.getValue("live.satsang.marquee.url");
			String param="mac="+macId;
			JSONParser parser = new JSONParser();
			JSONObject response = parser.getJSONFromUrl(url, param, false);
			if(response != null) {
				JSONObject respObj = response.getJSONObject("response");
				String status = respObj.getString("sts");
				if("200".equalsIgnoreCase(status)) {
					Log.info("status 200");
					JSONArray marqueeList = response.getJSONArray("data");
					DBAdapter dbAdp = new DBAdapter(context);
					dbAdp.syncMarquee(marqueeList);
					editor.putString("schedule.tasks.marqLocal", DateUtil.getServerFormatCurrentDateTimeString());
					editor.commit();
				}
			}
		}catch (Exception e) {
			Log.error("Exception downloading Marquee " + e);
		}
		
	}
	//TODO: change Audit classes to Event and manage both errors and audit in it.
	public void uploadEvents(String auditType) {
		Log.info("uploading audit events");
		try {
			String macId = CommonUtil.getMacId(context);
			//http://124.124.83.147:8080/live_santsang/log?id=at00001&type=error&log=
			String url = ConfigurationLive.getValue("live.satsang.upload.url");
			StringBuffer param= new StringBuffer("id="+macId);
			
			EventHandler handler = new EventHandler();
			String auditRecords;
			//check explicitly, ideally no need to check caller to ensure proper audit type is sent
			if(AuditConstants.AUDIT_TYPE_ERROR.equalsIgnoreCase(auditType)) {
				auditRecords = handler.getAuditRecordsToUpload(auditType);
			}else {
				auditRecords = handler.getAuditRecordsToUpload(auditType);
			}
			Log.debug("Uploading audit event type " + auditType);
				if(auditRecords != null && (!"".equalsIgnoreCase(auditRecords))) {
					Log.debug("Audit records found");
					param.append("&type="+auditType+"&log="+URLEncoder.encode(auditRecords, "UTF-8"));
					JSONParser parser = new JSONParser();
					JSONObject response = parser.getJSONFromUrl(url, param.toString(), false);
					if(response != null){
						String resp = response.getString("sts");
						if(resp != null && (!"".equalsIgnoreCase(resp))){
							if(resp.equalsIgnoreCase("200")){
								Log.debug("Clearing audit events of type " + auditType);
								EventHandler.deleteAuditRecords(auditType);
								
							}
						}
					}
				}//end auditRecords not null
			
		}catch(Exception e){
			Log.error("Exception uploading error on server"+e);
		}
	}
	
	public void synchronize() {
		Log.trace("entring synchronize()");
		DBAdapter dbAdapter = new DBAdapter(context);
		try {
			String macId = CommonUtil.getMacId(context);
			int appVersion = CommonUtil.getLocalVersion(context);
			JSONParser parser = new JSONParser();
			//http://124.124.83.147:8080/live_santsang/auth?id=at00001&ver=1
			String url = ConfigurationLive.getValue("live.satsang.schedule.url");
			String param = "id="+ macId+"&ver="+appVersion;
			JSONObject response = parser.getJSONFromUrl(url, param, false);
//			System.out.println("response:" + response);
			if(response != null){
				String msg = response.getString("msg");
				String status = response.getString("sts");
				if (msg.equalsIgnoreCase("Mac address not present") || "500".equalsIgnoreCase(status)) {
					try {
						SharedPreferences pref = context.getSharedPreferences("org.sevakendra", Context.MODE_PRIVATE);
						pref.edit().putString("org.sevakendra.status", "inactive").commit();
						Log.debug("Committed sevakendra status");
					} catch (Exception e) {
						Log.error("E", "Error updating sevakendra status");
					}
			}else {
				//TODO: if configuration data is encrypted then this should also be encrypted
				String langCode = response.getString("LANG_CODE");
				if(langCode != null && (!"".equalsIgnoreCase(langCode))) {
					dbAdapter.updateConfiguration("local.default.locale", langCode.toString());
				}
				//retrieve media lists
				JSONArray mediaList = response.getJSONArray("MEDIA");
				if(!"".equalsIgnoreCase(mediaList.getString(0).toString())) {
					Log.debug("Got news items:" + mediaList.length());
					dbAdapter.synchronize(mediaList);	
				}
				
				/*JSONArray bhajanList = response.getJSONArray("BHGN");
				if(!"".equalsIgnoreCase(bhajanList.getString(0).toString())){
					Log.debug("Got bhajan items:" + bhajanList.length());
					dbAdapter.synchronize(bhajanList);
				}
				
				JSONArray pravachanList = response.getJSONArray("PVCN");
				if(!"".equalsIgnoreCase(pravachanList.get(0).toString())) {
					Log.debug("Got pravachan items:" + pravachanList.length());
					dbAdapter.synchronize(pravachanList);
				}*/
				SharedPreferences pref = context.getSharedPreferences("schedule.tasks", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				
				String confDate = response.getString("CONF_DTAE");
				if(confDate != null && !"".equalsIgnoreCase(confDate)) {
					Log.debug("storing confDate " + confDate + " in shared preferences");
					editor.putString("schedule.tasks.configServer", confDate);
					editor.commit();
				}
				
				String marqueeDate = response.getString("MRQE_DTAE");
				if(marqueeDate != null && !"".equalsIgnoreCase(marqueeDate)){
					Log.debug("storing marqueeDate " + marqueeDate + " in shared preferences");
					editor.putString("schedule.tasks.marqServer", confDate);
					editor.commit();	
				}
				
				int apkUpdateValue =(int) response.getLong("apkUpdate");
				Log.debug("storing apkUpdate value:" + apkUpdateValue);
				editor.putInt("schedule.tasks.auth.apkVersion", apkUpdateValue);
				editor.commit();
			}//End msg=success
			}
		}catch(Exception e){
			Log.error("Exception in getMessagesFromServerAndStore:" + e);
		}
	}
}
