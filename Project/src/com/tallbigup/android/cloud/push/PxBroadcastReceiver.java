package com.tallbigup.android.cloud.push;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.widget.RemoteViews;

import com.tallbigup.android.cloud.R;
import com.tallbigup.android.cloud.TbuCloud;

public class PxBroadcastReceiver extends BroadcastReceiver{
	private static final String TAG = "MyCustomReceiver";
	private Context context;
	private boolean successDownload = false;
	private boolean needDownload = false;
	private Notification notification;
	private NotificationManager manager;
	private int id;

    @Override
    public void onReceive(Context context, Intent intent) {
    	this.context = context;
    	String packInfo = null;
    	String launchInfo = null;
    	String contentUrl = null;
    	String iconUrl = null;
    	String title = null;
    	String content = null;
    	String version = "1";
    	String channelId = null;
    	String province = null;
    	String pay = null;
    	String noLogin = null;
    	String userType = null;
    	String channelIdType = "0";
    	String provinceType = "0";
        Log.d(TAG, "Get Broadcat");
        String action = intent.getAction();
        Log.d(TAG,"action=" + action);
        SharedPreferences sp = context.getSharedPreferences("playerinfo", Context.MODE_PRIVATE);
        String playerId = sp.getString("playerId" + 50, "");
        Log.d(TAG,"playerId=" + playerId);
        
        if(action.equals("com.avos.UPDATE_STATUS")){
	        try {
	            String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");
	            JSONObject json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
	
	            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
	            @SuppressWarnings("unchecked")
				Iterator<String> itr = json.keys();
	            while (itr.hasNext()) {
	                String key = itr.next();
	                Log.d(TAG, "..." + key + " => " + json.getString(key));
	                if(key.equals("pack_info")){
	                	packInfo = json.getString(key);
	                }else if(key.equals("launch_info")){
	                	launchInfo = json.getString(key);
	                }else if(key.equals("content_url")){
	                	contentUrl = json.getString(key);	                	
	                }else if(key.equals("icon_url")){
	                	iconUrl = json.getString(key);
	                }else if(key.equals("title")){
	                	title = json.getString(key);
	                }else if(key.equals("content")){
	                	content = json.getString(key);
	                }else if(key.equals("verson")){
	                	version = json.getString(key);
	                }else if(key.equals("channel_id")){
	                	channelId = json.getString(key);
	                }else if(key.equals("province")){
	                	province = json.getString(key);
	                }else if(key.equals("pay")){
	                	pay = json.getString(key);
	                }else if(key.equals("no_login")){
	                	noLogin = json.getString(key);
	                }else if(key.equals("user_type")){
	                	userType = json.getString(key);
	                }else if(key.equals("channel_id_type")){
	                	channelIdType = json.getString(key);
	                }else if(key.equals("province_type")){
	                	provinceType = json.getString(key);
	                }
	            }
	            if(packInfo != null || launchInfo != null || contentUrl!= null){
	            	if(pay != null && !pay.equals("") && !String.valueOf(TbuCloud.getUserPay(context)).equals(pay)){
	            		return;
	            	}
	            	if(userType != null && !userType.equals("") && !String.valueOf(TbuCloud.markUserType(context)).equals(userType)){
	            		return;
	            	}
	            	if(version != null && !version.equals("")){
		            	String[] versions = version.split("&");
		            	for(int i=0;i<versions.length;i++){
		            		if(getGameVersion(context).equals(versions[i])){
			            		Log.i("MCH","匹配版本号成功");
		            			break;
		            		}
		            		if(i >= versions.length-1){
			            		Log.i("MCH","匹配版本号失败");
		            			return;
		            		}
		            	}
	            	}
	            	if(channelId != null && !channelId.equals("")){
		            	String[] channels = channelId.split("&");
		            	Log.i("MCH","channelIdType=" + channelIdType);
		            	Log.i("MCH","channelIdType.equals(0)" + channelIdType.equals("0"));
		            	if(channelIdType.equals("0")){
		            		Log.i("MCH","正向匹配渠道号");
			            	for(int i=0;i<channels.length;i++){
			            		if(getChannelId(context).equals(channels[i])){
				            		Log.i("MCH","channels[" + i + "]=" + channels[i]);
				            		Log.i("MCH","正向匹配渠道号成功");
			            			break;
			            		}
			            		if(i >= channels.length-1){
				            		Log.i("MCH","正向匹配渠道号失败");
			            			return;
			            		}
			            	}
		            	}else{
			            	for(int i=0;i<channels.length;i++){
			            		if(getChannelId(context).equals(channels[i])){
				            		Log.i("MCH","channels[" + i + "]=" + channels[i]);
				            		Log.i("MCH","反向匹配渠道号失败");
			            			return;
			            		}else{
				            		Log.i("MCH","反向匹配渠道号成功");
			            		}
			            	}
		            	}
	            	}
	            	if(province != null && !province.equals("")){
		            	String[] provinces = province.split("&");
		            	if(provinceType.equals("0")){
			            	for(int i=0;i<provinces.length;i++){
			            		if(TbuCloud.getUserProvince().equals(provinces[i])){
			            			break;
			            		}
			            		if(i >= provinces.length-1){
			            			return;
			            		}
			            	}
		            	}else{
			            	for(int i=0;i<provinces.length;i++){
			            		if(TbuCloud.getUserProvince().equals(provinces[i])){
			            			return;
			            		}
			            	}
		            	}
	            	}
	            	
	            	if(noLogin != null && !noLogin.equals("")){
		            	long lastLoginTime = TbuCloud.getUserLastLogin(context);
		            	if((System.currentTimeMillis()-lastLoginTime) < (Long.valueOf(noLogin)*24*60*60*1000)){
		            		return;
		            	}
	            	}
		            doPush(packInfo, launchInfo, contentUrl, iconUrl, title, content,version);
	            }
	        } catch (JSONException e) {
	            Log.d(TAG, "JSONException: " + e.getMessage());
	        }
        }
    }
    
    private void doPush(String packInfo,String launchInfo,String contentUrl,final String iconUrl,String title,String content,String version) {
    	long lastPushTime = TbuCloud.getUserLastReceiverPush(context);
    	long currentPushTime = System.currentTimeMillis();
    	if((currentPushTime - lastPushTime) < 5 * 60 * 1000){
    		Log.i("MCH","----doPush time is too short");
    		return;
    	}
    	TbuCloud.markUserReceiverPush(context, currentPushTime);
        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent();
		if(packInfo != null){
			if(isPackageInstall(context, packInfo)) {
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
	            ComponentName cn = new ComponentName(packInfo, launchInfo);
	            intent.setComponent(cn);
			}else {
				if(isWifiConn()){
					needDownload = true;
		            download(contentUrl,version);
		            if(isSDCardExist()){
		            	fileDir = getSdFilePath(version);
		            }else{
		            	fileDir = context.getFilesDir().getAbsolutePath() + File.separator +
	            				BUFFER_FILE_NAME_PRO + version + ".apk";
		            }
		    		Uri uri = Uri.fromFile( new File(fileDir) ); 
		    		intent = new Intent(Intent.ACTION_VIEW);
		        	intent.setDataAndType(uri,"application/vnd.android.package-archive");
				}else{
					intent.setAction("android.intent.action.VIEW");
		            Uri content_url = Uri.parse(contentUrl);
		            intent.setData(content_url);
				}
			}	
		}else{
			intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(contentUrl);
            intent.setData(content_url);
		}
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);	    	
    	final Notification notification = new Notification();
    	final RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.push_notification);
    	Bitmap bitmap = null;
    	try {
    		InputStream input = LoadPushImageUtil.getImageStream(iconUrl);
    		if(input == null){
    			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.push_logo);
    		}else{
    			bitmap = BitmapFactory.decodeStream(input);
    			if(bitmap == null){
    				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.push_logo);
    			}
    		}
		} catch (IOException e) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.push_logo);
		} catch(NetworkOnMainThreadException e){
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.push_logo);
		}
    	contentView.setImageViewBitmap(R.id.push_icon, bitmap);
    	contentView.setTextViewText(R.id.push_title, title);
    	contentView.setTextViewText(R.id.push_content, content);
    	notification.contentView = contentView;
    	notification.icon=R.drawable.push_logo;
    	notification.defaults = Notification.DEFAULT_ALL;
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	notification.contentIntent = pendingIntent;
        Random random = new Random(new Date().getTime()); 
        final int id = random.nextInt(1000000);
        this.manager = manager;
        this.notification = notification;
        this.id = id;
        Log.i("MCH","needDownload=" + needDownload);
        if(!needDownload){ 
            manager.notify(id, notification); 
        }
	 }	
    
    private boolean isPackageInstall(Context context, String packinfo) {
		PackageInfo packageInfo;

		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					packinfo, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if (packageInfo == null) {
			return false;
		} else {
			return true;
		}
	}
    
	private void download(String urlDownload,String version){
		DownloadFile downloadFile = new DownloadFile();
		downloadFile.execute(urlDownload,version);
	}
	
	private String fileDir = "";
	
	private class DownloadFile extends AsyncTask<String, Integer, String> {
	    @Override
	    protected String doInBackground(String... sInfos) {
	    		Log.i("POXIAOCLOUD", "sInfos[1]version = "  +sInfos[1]);
	        try {
	            URL url = new URL(sInfos[0]);
	            Log.i("POXIAOCLOUD", "download url is : " + sInfos[0]);
	            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	            
	            int downloaded = 0;
	            Log.i("POXIAOCLOUD", "init url");
	            if(isSDCardExist()) {
            			fileDir = getSdFilePath(sInfos[1]);
            			Log.i("POXIAOCLOUD", "sdcard fileDir = "  +fileDir);
            			// 仅在有SD卡的情况下开启断点续传
            			File file = new File(fileDir);
        	            if(file.exists()){
        	                downloaded = (int) file.length();
        	                connection.setRequestProperty("Range", "bytes="+downloaded+"-");
        	                Log.i("POXIAOCLOUD", "file exit bytes = " + downloaded );
        	            }else {
        	        	   	Log.i("POXIAOCLOUD", "file not exit");
        	            }
	            }else {
            			fileDir = context.getFilesDir().getAbsolutePath() + File.separator +
            				BUFFER_FILE_NAME_PRO + sInfos[1] + ".apk";
            			Log.i("POXIAOCLOUD", "not sdcard fileDir = "  +fileDir);
	            }

	            connection.setDoInput(true);
	            connection.setDoOutput(true);
	            connection.setConnectTimeout(10000);
	            
	            Log.i("POXIAOCLOUD", "connect ready");
	            connection.connect();
	            Log.i("POXIAOCLOUD", "connect do");
	            
	            int status = connection.getResponseCode();
	            Log.i("POXIAOCLOUD", "connect status=" + status);
	            if(
	            		!(status == 200 || status == 206 || status == 201 || status == 202) ) {
	            		successDownload = false;
	            		return null;
	            }
	            int fileLength = connection.getContentLength();
	            Log.i("POXIAOCLOUD", "fileLength = " + fileLength);
	            Log.i("POXIAOCLOUD", "downloaded = " + downloaded);
	            
	            if(fileLength == downloaded) {
	            		Log.i("POXIAOCLOUD", "connect arg same file length");
	            		successDownload = true;
	            		return null;
	            }
	            
	            byte data[] = new byte[1024];
	            long total = 0;
	            int count;

	            Log.i("POXIAOCLOUD", "start process = " + ((int) ( (total + downloaded) * 100 / (downloaded + fileLength) )));
	            publishProgress((int) ( (total + downloaded) * 100 / (downloaded + fileLength) ));

	            InputStream input = new BufferedInputStream(connection.getInputStream());
	            OutputStream output;
	            if(isSDCardExist()) {
            		if(downloaded > 0) {
            			Log.i("POXIAOCLOUD", "open file add model");
            			output = new FileOutputStream(fileDir, true);
            		}else {
            			Log.i("POXIAOCLOUD", "open file default model");
            			output = new FileOutputStream(fileDir);
            		}
	            }else {
	            	output = context.openFileOutput(BUFFER_FILE_NAME_PRO + sInfos[1] + ".apk", Context.MODE_WORLD_READABLE);
	            }
	            
	            while ((count = input.read(data)) != -1) {
	                total += count;
	                publishProgress((int) ( (total + downloaded) * 100 / (downloaded + fileLength) ));
	                output.write(data, 0, count);
	            }

	            output.flush();
	            output.close();
	            input.close();
	            successDownload = true;
	        } catch (Exception e) {
	        		Log.i("POXIAOCLOUD", "get error message on download." + e.getMessage());
	        		successDownload = false;
	        }
	        return null;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	    }

	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        Log.i("POXIAOCLOUD","已下载" + progress[0] + "%");
	    }
	    
	    @Override
	    protected void onPostExecute(String result) {
	    		super.onPostExecute(result);
	    		if(successDownload) {
	    			Log.i("POXIAOCLOUD", "下载完成...");
	    			showNotify(manager,notification,id);
	    		}else {
	    			Log.i("POXIAOCLOUD", "很遗憾,下载失败,请稍后再试...");
	    			showNotify(manager,notification,id);
	    		}
	    }
	    
	}
	
	private void showNotify(final NotificationManager manager,final Notification notification,final int id){
		needDownload = false;
		manager.notify(id, notification);
	}
	
		
	public static final String BUFFER_FILE_NAME_PRO= "poxiaogame";
	
	private static String getSdFilePath(String version){
		return (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
				BUFFER_FILE_NAME_PRO + version + ".apk");
	}
	
	private static boolean isSDCardExist() {
	    return Environment.getExternalStorageState().equals("mounted");
	}
	
	private boolean isWifiConn(){
		ConnectivityManager mgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mgr.getActiveNetworkInfo();
        if (null != networkInfo) {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    return false;
                case ConnectivityManager.TYPE_WIFI:
                    return true;
                default:
                    return false;
            }
        }else{
        	return false;
        }
	}
	

	private String getChannelId(Context context){
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager()
			        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		    return appInfo.metaData.getString("Channel ID");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "unknown";
		}
	}
	
	private String getGameVersion(Context context){
		try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return String.valueOf(pi.versionCode);
        } catch (Exception e) {
            return "1";
        }
	}
}
