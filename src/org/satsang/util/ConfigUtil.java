package org.satsang.util;

import java.util.Calendar;

import org.android.boot.BootReceiver;
import org.satsang.live.config.ConfigurationLive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

public class ConfigUtil {
	static private final Logger Log = LoggerFactory.getLogger(ConfigUtil.class);

	public static boolean checkScheduleFromConfiguration() throws Exception {
		boolean playMode = false;
		Log.trace("Entering:checkScheduleFromConfiguration()");
		try {
			String dayOfWeek = ConfigurationLive.readConfigFile().get("satsang.default.play.dayOfWeek");
			Log.debug("day of week >>>>" + dayOfWeek);
			if (dayOfWeek == null || ("".equalsIgnoreCase(dayOfWeek)))
				throw new Exception("play day in configuration not found");
			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK);
			int configDay = getDayOfWeek(dayOfWeek);
			if (day == configDay) {
				Log.debug("Play mode as per Configuration" + configDay + " Day_OF_WEEK: " + day);
				playMode = true;
			}
		} catch (Exception e) {
			Log.error("Error in checkScheduleFromConfiguration():" + e.getMessage());
			throw e;
		}
		return playMode;
	}

	public static int getScheduleTimeFromConfiguration() {
		Log.trace("Entering:getScheduleTimeFromConfiguration()");
		int diffInMinutes = -1;
		try {
			String playTime = ConfigurationLive.readConfigFile().get("satsang.default.play.time");
			Calendar cal = Calendar.getInstance();
			int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
			int minuteOfDay = cal.get(Calendar.MINUTE);
			Log.debug("hourOfDay: " + hourOfDay + " minuteOfDay: " + minuteOfDay + " playTime:" + playTime);
			diffInMinutes = getTimeDifferenceInMinutes(hourOfDay, minuteOfDay, playTime);
		} catch (Exception e) {
			Log.error("Error in getScheduleTimeFromConfiguration():" + e.getMessage(), e);
		}
		return diffInMinutes;
	}

	private static int getTimeDifferenceInMinutes(int hourOfDay, int minutesOfDay, String configTime) throws Exception {
		int diffInMinutes;
		int configHour = getHour(configTime);
		int configMins = getMinutes(configTime);
		int hourDiff = configHour - hourOfDay;
		diffInMinutes = (hourDiff * 60) + (configMins - minutesOfDay);
		Log.trace("minutesDiff: " + diffInMinutes + " hourDiff: " + hourDiff);
		return diffInMinutes;
	}

	public static int getHour(String time) throws Exception {
		int hour;
		int index = time.indexOf(":");
		if (index != -1) {
			hour = new Integer(time.substring(0, index)).intValue();
		} else {
			Log.warn("Could not find play hour in configuration");
			throw new Exception("Could not find play hour in configuration");
		}
		return hour;
	}

	public static int getMinutes(String time) throws Exception {
		int hour;
		int index = time.indexOf(":");
		if (index != -1) {
			hour = new Integer(time.substring(index + 1, time.length())).intValue();
		} else {
			Log.warn("Could not find play hour in configuration");
			throw new Exception("Could not find play hour in configuration");
		}
		return hour;
	}

	private static int getDayOfWeek(String dayOfWeek) {

		if (dayOfWeek != null && (!"".equalsIgnoreCase(dayOfWeek))) {
			if (dayOfWeek.equalsIgnoreCase("sunday")) {
				return 1;
			} else if (dayOfWeek.equalsIgnoreCase("monday")) {
				return 2;
			} else if (dayOfWeek.equalsIgnoreCase("tuesday")) {
				return 3;
			} else if (dayOfWeek.equalsIgnoreCase("wednesday")) {
				return 4;
			} else if (dayOfWeek.equalsIgnoreCase("thursday")) {
				return 5;
			} else if (dayOfWeek.equalsIgnoreCase("friday")) {
				return 6;
			} else if (dayOfWeek.equalsIgnoreCase("saturday")) {
				return 7;
			}
		}
		return 0;

	}

	/**
	 * PMD added for maintenance window. This method checks the configuration
	 * for maintenance window and returns number of seconds for which
	 * application need to be kept running
	 */
	public static int getMaintenanceWindow() {
		// logger.info("Entering:getMaintenanceWindow()");
		int diffInMinutes = -1;
		try {
			String startTime = ConfigurationLive.readConfigFile().get("live.satsang.maint.start");
			int duration = new Integer(ConfigurationLive.readConfigFile().get("live.satsang.maint.duration"))
					.intValue();
			String maintDayOfWeek = ConfigurationLive.readConfigFile().get("live.satsang.maint.day");

			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK);
			int maintDay = getDayOfWeek(maintDayOfWeek);
			if (day == maintDay) {
				// logger.debug("Maint mode as per Configuration" + maintDay +
				// " Day_OF_WEEK: " + day);
				int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
				int minuteOfDay = cal.get(Calendar.MINUTE);
				// System.out.println("hourOfDay: " + hourOfDay +
				// " minuteOfDay: " + minuteOfDay + " MaintStartTime:" +
				// startTime);
				diffInMinutes = getTimeDifferenceInMinutes(hourOfDay, minuteOfDay, startTime);
				// System.out.println("Diff" + diffInMinutes);
				// Check if difference is less than
				if (diffInMinutes <= 0 && (-diffInMinutes < duration)) {
					// logger.debug("Within maintenance window");
					return (diffInMinutes + duration) * 60;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} catch (Exception e) {
			// logger.error("Error in getMaintenanceWindow():"+e.getMessage(),
			// e);
		}
		// logger.info("Returning getMaintenanceWindow()");
		return diffInMinutes;
	}

	/* -1=outside play window. return value is in seconds to as to check for lag */
	public static int getPlayWindow() {
		int diffInMinutes = -1;
		try {
			String startTime = ConfigurationLive.readConfigFile().get("live.satsang.play.start");
//			int duration = new Integer(ConfigurationLive.readConfigFile().get("live.satsang.play.duration")).intValue();
			int duration = Integer.valueOf(ConfigurationLive.getValue("live.satsang.play.duration"));
			String playDayOfWeek = ConfigurationLive.getValue("satsang.default.play.dayOfWeek");

			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK);
			int playDay = getDayOfWeek(playDayOfWeek);
			if (day == playDay) {
				Log.debug("Play mode as per Configuration" + startTime +" duration: " + duration);
				int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
				int minuteOfDay = cal.get(Calendar.MINUTE);
				Log.debug("hourOfDay: " + hourOfDay + " minuteOfDay: " + minuteOfDay + " MaintStartTime:" + startTime);
				diffInMinutes = getTimeDifferenceInMinutes(hourOfDay, minuteOfDay, startTime);
				Log.debug("diff In Minutes: " + diffInMinutes);
				// Check if difference is less than
				if (diffInMinutes <= 0 && (-diffInMinutes < duration)) {
					Log.debug("Within play window");
					return (diffInMinutes + duration) * 60;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} catch (Exception e) {
			 Log.error("Error in getPlayWindow():"+e.getMessage(), e);
		}
		// logger.info("Returning getMaintenanceWindow()");
		return diffInMinutes;
	}
	
	/*
	 * Utility method which check if todays day is SATURDAY OR SUNDAY and
	 * returns true. TODO: Add check if current time is less than play start
	 * time. If play time has elapsed then dont download
	 */
	public static boolean downloadMarquee() {
		Log.trace("Entering downloadMarquee");
		try {
			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK);
			if (day == 1 || day == 2 || day == 3 || day == 4 || day == 5 || day == 6 || day == 7) {
				System.out.println("Marquee download day:" + day);
				return true;
			}
		} catch (Exception e) {
			Log.error("Error in marquee download day:" + e.getMessage());
		}
		return false;
	}

	/*
	 * Utility method to check week of the month. Used while updating software.
	 * During the week system will check if there was download failure. Post x
	 * number of attempts within same week download will be not start.
	 */
	public static int getWeekOfTheMonth() {
		int weekOfMonth = 0;
		try {
			Calendar cal = Calendar.getInstance();
			weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
		} catch (Exception e) {
			Log.error("Error getting current Week of the month");
		}
		Log.debug("Current week of month:" + weekOfMonth);
		return weekOfMonth;

	}

}
