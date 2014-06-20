package org.santsang.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class LiveSatsangService extends Service {
	static private final Logger Log = LoggerFactory.getLogger(LiveSatsangService.class);

	ScheduleReceiver alarm = new ScheduleReceiver();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.info("OnStartCommand called");
		Context context = getApplicationContext();
		alarm.setAlarm(context);
		Log.debug("OnStartCommand:ScheduleReceiver alarm set");
		return Service.START_STICKY;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Context context = getApplicationContext();
		Log.info("OnDestory called cancelling alarm");
		alarm.CancelAlarm(context);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
