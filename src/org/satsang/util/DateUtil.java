package org.satsang.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.satsang.live.config.ConfigurationLive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;

@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
public class DateUtil {
	static private final Logger Log = LoggerFactory.getLogger(DateUtil.class);


	public static String shuffleDate(String date) throws Exception {
		try {
			Date original = parseDate(date);
			Date shuffledDate = DateUtils.addDays(original, 7);
			// logger.debug("Shuffled Date:"+shuffledDate);
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			return formatter.format(shuffledDate);
		} catch (Exception e) {
			// logger.error("Error shuffling date"+e.getMessage(), e);
			throw e;
		}
	}

	@SuppressLint("DefaultLocale")
	public static Date parseDate(String date) throws Exception {
		// logger.info("Entering:DateUtil.parseDate(date)");
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", new Locale("en", "EN"));
		try {

			System.out.println("date before parsing" + date);
			Date scheduleDate = (Date) formatter.parse(date.toUpperCase());
			// logger.debug("parseDate() setDatetime:"+ scheduleDate);
			System.out.println("date after parsing" + scheduleDate);
			return scheduleDate;
		} catch (Exception e) {
			Log.error("Exception:parseDate(date):" + e.getMessage(), e);
			throw new Exception("Invalid Date");
		}
	}

	public static Date getFormattedCurrentDate() throws Exception {
		Calendar current = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String currentDateStr = dateFormat.format(current.getTime());
		Date currentDate = (Date) dateFormat.parse(currentDateStr);
		return currentDate;
	}
	
	public static String getFormattedCurrentDateString() {
		Calendar current = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String currentDateStr = dateFormat.format(current.getTime());
		return currentDateStr;
	}
	
	public static String getServerFormatCurrentDateTimeString() {
		Calendar current = Calendar.getInstance();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", new Locale("en", "EN"));
		String currentDateStr = formatter.format(current.getTime());
		return currentDateStr;
	}

	/* -1=less 1=greater and 0=equals */
	public static int compareDate(String date) {
		Calendar current = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String currentDateStr = dateFormat.format(current.getTime());
		try {
			Date currentDate = (Date) dateFormat.parse(currentDateStr);
			Date schDate = parseDate(date);
			return currentDate.compareTo(schDate);
		} catch (Exception e) {
			Log.error("Exception comparing date:"+e.getMessage(),e);
		}
		return 0;
	}

	public static String shuffleDate(String date, int count) throws Exception {
		try {
			Date original = parseDate(date);
			Date shuffledDate = DateUtils.addDays(original, 7 * count);
			// TODO: Need to add current date to original date. If shuffled
			// after few months it does not shuffle
			// to latest date.
			// Date shuffledDate = DateUtils.addDays(getFormattedCurrentDate(),
			// 7*count);
			// logger.debug("Shuffled Date:"+shuffledDate);
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			return formatter.format(shuffledDate);
		} catch (Exception e) {
			// logger.error("Error shuffling date"+e.getMessage(), e);
			throw e;
		}
	}

	// TODO: Check what is the command used to set system date on TC. Retrieve
	// it from property??? hardcode it...
	public static void setSystemDate(String date) {
		// logger.info("Entering setSystemDate(date)");
		String command = "sudo date -s \"" + date + "\""; // new line add -
															// 03-02-2013
		Runtime rt = null;
		Process proc = null;
		try {
			rt = Runtime.getRuntime();
			proc = rt.exec(command);
			// logger.debug("System date set:"+ date);
			// If system response is slow add JVM hook
			// rt.addShutdownHook(new Thread());
		} catch (Exception e) {
			// logger.error("Exception setting system date:"+e.getMessage(),e);
		} finally {
			if (proc != null)
				proc.destroy();
			if (rt != null)
				rt.exit(0);
		}
	}

	// new method add - 03-02-2013
	public static Date parseServerDate(String date) {
		// String date = "Sat Nov 24 15:32:48 IST 2012";
		Date schDate = new Date();
		DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		try {
			schDate = (Date) formatter.parse(date.toUpperCase());
		} catch (Exception e) {
			Log.error("Exception parseServerDate "+e);
		}
		return schDate;
	}
	
	//new method added to format configured time in livesatsang.conf
	public static String getFormattedTime(String time){
		try {
			int index = time.indexOf(":");
			long hour = Long.valueOf(time.substring(0, index));
			long min = Long.valueOf(time.substring(index+1, time.length()));
			String timeFormat = "%02d:%02d";
			String formattedTime = String.format(timeFormat, hour, min);
			Log.debug("getFormatted Time:" + formattedTime);
			return formattedTime;
		}catch(Exception e) {
			Log.error("Error in getFormattedTime:" + e);
		}
		return time;
	}
	
	//TODO: test below code
	public static Date getNextSunday(Date today) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);

        int dow = cal.get(Calendar.DAY_OF_WEEK);
        while (dow != Calendar.SUNDAY) {
            int date = cal.get(Calendar.DATE);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            
            if (date == getMonthLastDate(month, year)) {
                if (month == Calendar.DECEMBER) {
                    month = Calendar.JANUARY;
                    cal.set(Calendar.YEAR, year + 1);
                } else {
                    month++;
                }
                cal.set(Calendar.MONTH, month);
                date = 1;
            } else {
                date++;
            }
            
            cal.set(Calendar.DATE, date);
            String satsangTime = ConfigurationLive.getValue("satsang.default.play.time");
            int hour=0;
            int mins=0;
            try {
            	hour = ConfigUtil.getHour(satsangTime);
            	mins = ConfigUtil.getMinutes(satsangTime);		
            }catch(Exception e){
            	System.out.println("error in retrieving santsang hour");
            }
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, mins);
            dow = cal.get(Calendar.DAY_OF_WEEK);
        }
        System.out.println("next sunday is:" + cal.getTime());
        return cal.getTime();
    }

    private static int getMonthLastDate(int month, int year) {
        switch (month) {
            case Calendar.JANUARY:  
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;

            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
                
            default:    //  Calendar.FEBRUARY
                return year % 4 == 0 ? 29 : 28;
        }
    }

    //New date from server: 2014-05-08 00:19:05.0
    public static String formatServerDate(String date) {

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", new Locale("en", "EN"));
		formatter.setTimeZone(TimeZone.getDefault());
		try {
			Date inputDate = (Date) formatter.parse(date);
			DateFormat truncateTime = new SimpleDateFormat("dd-MMM-yyyy", new Locale("en", "EN"));
			truncateTime.setTimeZone(TimeZone.getDefault()); //09Feb2014
			String dateOnly = truncateTime.format(inputDate);
			return dateOnly.toString();
		}catch (Exception e) {
			Log.error("error formatServerDate:" + e);
		}
		return "";
	}
    
    /* if first < second => -1; if first > second => 1; if first == second => 0  datetimestamp=2014-05-04 00:22:20.0*/
    public static int compare(String first, String second) {
    	try {
    		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", new Locale("en", "EN"));
    		formatter.setTimeZone(TimeZone.getDefault());
			Date firstDate = (Date) formatter.parse(first);
			Date secondDate = (Date) formatter.parse(second);
			return firstDate.compareTo(secondDate);
    	}catch(Exception e){
    		Log.error("Error in compare(String,String) dates");
    	}
    	return 0; //TODO: comparision value to be changed to -2 in case of failure??
    }
    
    /* Used for comparing scheduled tasks having datetimestamp format as 2014-05-04 00:22:20.0 and returns diff in seconds */
    public static long compareDateTime(String first, String second) {
    	try {
    		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", new Locale("en", "EN"));
    		formatter.setTimeZone(TimeZone.getDefault());
			Date firstDate = (Date) formatter.parse(first);
			Date secondDate = (Date) formatter.parse(second);
			long diff = (firstDate.getTime() - secondDate.getTime())/1000;
			Log.trace("compareDateTime diff:" + diff);
			return diff;
    	}catch(Exception e){
    		Log.error("Error in compare(String,String) dates");
    	}
    	
    	return 0;
    }
    /*public static String getFormattedCurrentDateTimeString() {
		Calendar current = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", new Locale("en", "EN"));
		String currentDateStr = dateFormat.format(current.getTime());
		return currentDateStr;
	}*/
    
    //This method checks current date time and calculates difference in minutes between current time and schedule time
    public static long getScheduleDifferenceInMinutes(String storedDate) {
    	try {
    		Calendar current = Calendar.getInstance();
			long currentTime = current.getTimeInMillis();
	    	DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", new Locale("en", "EN"));
			formatter.setTimeZone(TimeZone.getDefault());
			Date inputDate = (Date) formatter.parse(storedDate);
			long inputTime = inputDate.getTime();
			Log.info("currentTime:" + currentTime + " inputDateTime: " + inputTime );
			long difference = (inputTime-currentTime)/(1000*60) % 60;
			Log.info("Time difference >>" + difference);
			return difference;
		}catch(Exception e){
			Log.error("error in getDifferenceInHours");
		}
    	return -1;
    }
    
    /*This method take current time stamp (long) and returns date in android shell date format i.e. date -s YYYYMMDD.HHmmss i.e 20120423.130000 */
    public static String getShellFormattedDate(long timeStamp){
    	try {
    		//TODO: test on device
    		DateFormat format = new SimpleDateFormat("yyyyMMdd.HHmmss", new Locale("en", "EN"));
        	format.setTimeZone(TimeZone.getDefault());
        	Calendar calendar = Calendar.getInstance();
        	calendar.setTimeInMillis(timeStamp);
    		String shellFmt = format.format(calendar.getTime());
    		Log.info("shellFmt Date:" + shellFmt);
    		return shellFmt;
    	}catch(Exception e) {
    		Log.error("Exception getShellFormattedDate:" + e);
    	}
    	return null;
    	
    }
    
}
