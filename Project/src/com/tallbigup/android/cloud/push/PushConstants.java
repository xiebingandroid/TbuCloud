package com.tallbigup.android.cloud.push;

/**
 * push系统Android客户端的常量信息。
 * 
 * @author Molo.Xiao
 *
 */
public class PushConstants {
	
	public static final String NOTIFY_SERVER_URL = "http://111.1.17.140:8186/gm.do?";
	
	public static final String PUSH_SDK_VERSION = "2.0";
	
	public static final int NOTIFICATION_FLAG_TYPE_NULL = 0;
	public static final int NOTIFICATION_FLAG_TYPE_CURRENTSUPPORT = 1;
	
	/**
	 * 向服务端请求消息间隔。
	 */
	public static final long SERVER_REQ_INTERVAL = 1000*60*60*24; // 1天 1000*60*60*24
	
	
	// 本地存储key
	public static final String SHAREDPREFERENCES_DB_NAME_PUSH 
			= "com.hifreshday.push.android.db.name.push";
	
	// 本地存储key : 上次登录提醒时间
	public static final String SHAREDPREFERENCES_KEY_LAST_REQSERVERTIME 
			= "com.hifreshday.push.android.db.push.lastloginnotifytime";

	
	// 本地存储key : uuid缓存
	public static final String SHAREDPREFERENCES_KEY_UUID 
			= "com.hifreshday.push.android.db.push.uuid";
	
	// 本地存储key : 登陆状态，0，从未登陆过。1，登陆过。
	public static final String SHAREDPREFERENCES_KEY_ISFIRST_REQ 
			= "com.hifreshday.push.android.db.push.islogin";
		
	// 本地存储key : 接收到的最新消息号，初始为0。
	public static final String SHAREDPREFERENCES_KEY_MESSAGE_SEQ 
			= "com.hifreshday.push.android.db.push.messageseq";
	
	
	public static final long USER_NEVER_LOGIN_IN_FLAG = 0;
	
		
}
