package org.satsang.web;

import org.satsang.live.config.ConfigurationLive;
import org.satsang.live.config.Constants;

public class WebUtil {

	public static int getConnectionTimeout() {
		// logger.info("Entering getConnectionTimeout()");
		try {
			return Integer.valueOf(ConfigurationLive.getValue("live.satsang.url.connection.timeout"));
		} catch (Exception e) {
			// logger.error("Error reading URL connection timeout" +
			// e.getMessage(), e);
		}
		return Constants.DEFAULT_URL_CONNECTION_TIMEOUT;
	}

	public static int getReadTimeout() {
		// logger.info("Entering getReadTimeout()");
		try {
			return Integer.valueOf(ConfigurationLive.getValue("live.satsang.url.readtimeout"));
		} catch (Exception e) {
			// logger.error("Error reading read timeout" + e.getMessage(), e);
		}
		return Constants.DEFAULT_URL_READ_TIMEOUT;
	}

	public static boolean isHTTPSEnabled() {
		// logger.info("Entering isHTTPSEnabled()");
		try {
			return Boolean.valueOf(ConfigurationLive.getValue("live.satsang.web.https.enabled"));
		} catch (Exception e) {
			// logger.error("Error reading HTTPS property");
		}
		return false;
	}

}
