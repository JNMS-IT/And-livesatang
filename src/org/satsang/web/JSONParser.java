package org.satsang.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.security.BlowfishEasy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONParser {
	static private final Logger Log = LoggerFactory.getLogger(JSONParser.class);
	
	public JSONObject getJSONFromUrl(String url, String param, boolean isEncrypted) {
		InputStream is = null;
		JSONObject jObj = null;
		BufferedReader reader = null;
		String json = "";
		BlowfishEasy manager = new BlowfishEasy("");
		Log.trace("param:" + param);
		// Making HTTP request
		try {
			if (isEncrypted) {
				param = "a=" + manager.encryptString(param);
			}
			url = url + "?" + param;
			Log.debug("url:" + url);
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, WebUtil.getConnectionTimeout());
			HttpConnectionParams.setSoTimeout(httpParameters, WebUtil.getReadTimeout());
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

			reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			json = sb.toString();
		

		// try parse the string to a JSON object
			jObj = new JSONObject(json);
			// If encryption is enabled extract values
			if (isEncrypted) {
				String encrypted = jObj.getString("a");
				String decrypted = manager.decryptString(encrypted);
				decrypted = new String(decrypted.getBytes(), "UTF-8");
				jObj = new JSONObject(decrypted);
			} else {
				json = new String(json.getBytes("ISO-8859-1"), "UTF-8");
				jObj = new JSONObject(json);
			}
		} catch (JSONException e) {
			Log.error("Error parsing data " + e.toString());
		} catch (Exception e) {
			Log.error("Exception is response handling:" + e);
		}finally {
			try {
				if(is != null) is.close();	
				if(reader != null) reader.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}

		// return JSON String
		Log.trace("response:" + jObj);
		return jObj;

	}

	private boolean isURLEncryptionEnabled() {
		try {
			return Boolean.valueOf(ConfigurationLive.getValue("live.satsang.encrypt.url.params")).booleanValue();
		} catch (Exception e) {
			Log.error("Error reading url encryption property");
		}
		return false;
	}

}
