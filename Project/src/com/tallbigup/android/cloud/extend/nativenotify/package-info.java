/**
 * 本地提醒功能模块
 */
/**
 * @author molo
 * 
 * AndroidManifest.xml的设置
 * 
 *  <receiver android:name="com.tallbigup.android.cloud.extend.nativenotify.NativeNotifyReceiver" >
            <meta-data android:name="push_gameid" android:value="your game id here" />
        	<meta-data android:name="push_appid" android:value="your appid here" />
        	<meta-data android:name="push_enterid" android:value="e=your enterId here" />
        	<meta-data android:name="push_url" android:value="your url here" />
        	<meta-data android:name="push_icon" android:resource="@drawable/push_logo" />
 *  </receiver>
 *
 */
package com.tallbigup.android.cloud.extend.nativenotify;