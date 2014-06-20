package org.santsang.schedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.android.activities.DownloadActivity;
import org.satsang.bo.MediaSchedule;
import org.satsang.live.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaScheduleComparator implements Comparator<MediaSchedule> {
	static private final Logger Log = LoggerFactory.getLogger(MediaScheduleComparator.class);

	@Override
	public int compare(MediaSchedule arg0, MediaSchedule arg1) {
		Log.info("Entering compare");
		try {
			Date first = parseDate(arg0.getScheduleDate());
			Date second = parseDate(arg1.getScheduleDate());
			String schType0 = arg0.getMediaType();
			//Check if bhajan is present in the schedule object. If yes put it to last in the list.
			if(schType0 != null && Constants.BHAJAN.equalsIgnoreCase(schType0)) return 1;
			if (first.after(second)) {
				return 1;
			} else if (first.before(second)) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			 Log.error("Error comparing pravachan schedule objects"+e.getMessage(),e);
		}

		return 0;
	}

	public static Date parseDate(String dateTime) throws Exception {
		Log.info("Entering:PravachanScheduleComparator.parseDate(dateTime)");
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			Date scheduleDate = (Date) formatter.parse(dateTime.toUpperCase());
			return scheduleDate;
		} catch (Exception e) {
			 Log.error("Exception:parseDate(dateTime):" + e.getMessage(), e);
			throw new Exception("Invalid Date");
		}
	}

}
