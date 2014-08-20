package com.tallbigup.android.cloud.extend.sign;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 基本签到模式实现。
 * @author molo
 *
 */
public class SignManager {
	
	private static final String STORE_NAME = "SignManager";
	private static final String STORE_KEY_NOTIFYPLAYER = "STORE_KEY_NOTIFYPLAYER";

	/**
	 * TODO : 获得签到第几天的信息
	 */
	public static int getCurrentSignDay(Context context) {
		
		return 0;
	}
	
	/**
	 * TODO : 今日是否签到过
	 */
	public static boolean isSignToday(Context context) {
		return false;
	}
	
	
	/**
	 * TODO : 签到
	 */
	public static void sign(Context context) {
		if(getNeedNotifyPlayer(context)) {
			
		}
	}
	
	
	/**
	 * 是否需要本地提醒。
	 * @param context
	 * @param need
	 * @return
	 */
	public static boolean setNeedNotifyPlayer(Context context, boolean need) {
		SharedPreferences sharePreference = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharePreference.edit();
		editor.putBoolean(STORE_KEY_NOTIFYPLAYER, need);
		editor.commit();
		return getNeedNotifyPlayer(context);
	}
	
	/**
	 * 是否需要本地提醒。
	 * @return
	 */
	private static boolean getNeedNotifyPlayer(Context context) {
		SharedPreferences sharePreference = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
		return sharePreference.getBoolean(STORE_KEY_NOTIFYPLAYER, false);
	}
}
