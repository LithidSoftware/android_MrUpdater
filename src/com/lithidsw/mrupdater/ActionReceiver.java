package com.lithidsw.mrupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ActionReceiver extends BroadcastReceiver {
	
	private SharedPreferences prefs;
	private Utils mUtils = new Utils();
	private UpdateChecker mChk;

	@Override
	public void onReceive(Context context, Intent intent) {

		if ((intent.getAction() != null)) {
			
			mChk = new UpdateChecker(context);
			prefs = context.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);

		    if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
		    	if (prefs.getBoolean(C.PREF_CHECK_INTERVAL, false)) {
		    		mUtils.setAlarms(context, "stop");
		    		mUtils.setAlarms(context, "start");
		    	}
		    	if (prefs.getBoolean(C.PREF_CHECK_BOOT, false)) {
		    		mChk.runUpdateCheck();
		    	}
		    }

		    if (intent.getAction().equals("com.lithidsw.mrupdater.CHECK_UPDATES")) {
		    	mChk.runUpdateCheck();
		    }

		}
	}
}
