package com.tallbigup.android.cloud.extend.activitynotify;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityNotifyManager {
	
	public static final String SP_NAME = "activity_notity_manager";
	public static final String PRIZE_TYPE = "prize_type";
	public static final String PRIZE_NUM = "prize_num";
	public static final String NOTITY_ID = "notify_id";
	public static final String START_TYPE = "start_type";
	public static final String ACTIVITY_CONTENT = "activity_content";
	public static final int START_BY_ACTIVITY_NOTIFY = 1;
	public static final int START_BY_CLICK_ICON = 0;
	
	private static Bundle activityInfos;
	
	public static boolean isActivityNotify(Context context,Intent intent){
		if(intent == null){
			return false;
		}
		if(intent.getExtras() == null){
			return false;
		}
		activityInfos = intent.getExtras();
		return activityInfos.getInt(ActivityNotifyManager.START_TYPE)==1;
	} 
	
	public static int getActivityPrizeType(Context context){
		return Integer.valueOf(activityInfos.getString(ActivityNotifyManager.PRIZE_TYPE).trim());
	}
	
	public static int getActivityPrizeNum(Context context){
		return Integer.valueOf(activityInfos.getString(ActivityNotifyManager.PRIZE_NUM).trim());
	}
	
	public static String getActivityContent(Context context){
		return activityInfos.getString(ActivityNotifyManager.PRIZE_TYPE).trim();
	}
	
	public static void clearLastActivityNotify(Context context){
		final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(activityInfos.getInt(ActivityNotifyManager.NOTITY_ID));
	}
	
}
