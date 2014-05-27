package com.tallbigup.android.cloud;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;

public class TbuCloud {
	
	private static boolean successInit = false;
	
	/**
	 * 游戏版本号，用来在自定义事件的时候在最前面标记
	 */
	private static String TAGID = "0";
	
	/**
	 * 判断插件是否初始化成功
	 * @return true，成功, false，失败
	 */
	public static boolean isSuccessInit() {
		return successInit;
	}

	/**
	 * 初始化插件。
	 * @param context
	 * @param callback
	 * @param appId
	 * @param appKey
	 * @param gameVersion
	 * @param cls
	 */
	public static void initCloud (
			Context context, 
			TbuCallback callback,
			final String appId, 
			final String appKey,
			final String gameVersion,
			Class <? extends Activity> cls) {
		if(gameVersion != null && gameVersion.length() > 0) {
			TAGID = gameVersion;
		}
		
		AVOSCloud.useAVCloudCN();
	    AVOSCloud.initialize(context, appId, appKey);
	    
	    PushService.setDefaultPushCallback(context, cls);
        PushService.subscribe(context, "public", cls);
	    AVInstallation.getCurrentInstallation().saveInBackground();
	    
	    AVAnalytics.enableCrashReport(context, true);
	    
	    successInit = true;
	    if(callback != null) {
	    		callback.result(successInit);
	    }
	}
	
	
	/**
	 * 在服务端记录1次应用登陆[非实时上传数据]
	 * @param activity
	 */
	public static void markAppOpened(Activity activity) {
		AVAnalytics.trackAppOpened(activity.getIntent());
	}
	
	
	/**
	 * 
	 * @param context
	 * @param title 自定义事件的名称，会自动在头上加上游戏的版本号
	 * @param tag 可以通过不同的tag来标记分类
	 */
	public static void markPersonInfo(final Context context, final String title, final String tag) {
		AVAnalytics.onEvent(context, TAGID + "_" + title, tag);
	}
	
	
	/**
	 * 更新玩家信息
	 * @param objectId
	 * @param level
	 * @param money
	 */
	public static void updatePlayerInfo(
			final String objectId, 
			final String level, 
			final int money,
			final int payMoney) {
		if(objectId == null) {
			return ;
		}
		AVObject playerInfo = new AVObject("Player");
		AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

		try {
			playerInfo = query.get(objectId);
			playerInfo.put("level",level);
			playerInfo.put("money",money);
			playerInfo.put("payMoney",payMoney);
			playerInfo.saveInBackground(new SaveCallback() {
			   @Override
			   public void done(AVException e) {
			        if (e == null) {
			            Log.i("POXIAOCLOUD", "Save successfully.");
			        } else {
			            Log.e("POXIAOCLOUD", "Save failed.");
			        }
			    }
			});
		} catch (AVException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 * @param nickName
	 * @param IMSI
	 * @param gameVersionCode
	 * @param level
	 * @param money
	 * @param enterId
	 * @param payMoney
	 * @param callback
	 */
	public static void createPlayer(
			final String nickName, 
			final String IMSI, 
			final String gameVersionCode,  
			final String level, 
			final int money, 
			final String enterId, 
			final int payMoney, 
			final CreatePlayerCallback callback) {
		if(TbuCloud.isSuccessInit()){
			if(callback != null) {
				callback.result(false, null);
			}
		}
		
		final AVObject player = new AVObject("Player");
		
		player.put("nickName", nickName);
		player.put("IMSI", IMSI);
		player.put("money", money);
		player.put("level",level);
		player.put("enterId",enterId);
		player.put("gameVersionCode", gameVersionCode);
		player.put("payMoney",payMoney);

		player.saveInBackground(new SaveCallback() {
		    public void done(AVException e) {
		        if (e == null) {
		        		if(callback != null) {
		        			callback.result(true, player.getObjectId());
		        		}
		        } else {
		        		if(callback != null) {
		        			callback.result(false, null);
		        		}
		        }
		    }
		});
	}
	
	
	/**
	 * 
	 * @param requestType
	 * @param money
	 * @param payType
	 * @param userId
	 * @param desc
	 * @param payCount
	 * @param enterId
	 * @param orderId
	 * @param errorCode
	 * @param errorMessgae
	 * @param gameVersion
	 * @param payVersionId
	 * @param levelId
	 * @param imsi
	 * @param carrier
	 * @param payPluginName
	 * @param userType
	 * @param callback
	 */
	public static void setPayInfo(
			final String requestType, 
			final int money, 
			final String payType, 
			final String userId, 
			final String desc, 
			final int payCount, 
			final String enterId, 
			final String orderId, 
			final String errorCode, 
			final String errorMessgae,
			final String gameVersion, 
			final String payVersionId, 
			final String levelId, 
			final String imsi,
			final String carrier,
			final String payPluginName,
			final String userType,
			final TbuCallback callback) { 

		if(TbuCloud.isSuccessInit()){
			callback.result(false);
		}
		AVObject payInfo = new AVObject("PayInfo");
		
		payInfo.put("requestType", requestType); //（"request" "clickOk" "cancel" "fail", "success"）
		payInfo.put("money", money); //单位：分
		payInfo.put("payType", payType);
		payInfo.put("userId", userId);
		payInfo.put("desc", desc);
		payInfo.put("payCount", payCount);
		payInfo.put("enterId", enterId);
		payInfo.put("orderId", orderId);
		payInfo.put("errorCode", errorCode);
		payInfo.put("errorMessgae", errorMessgae);
		payInfo.put("gameVersion", gameVersion);
		payInfo.put("payVersionId", payVersionId);
		payInfo.put("levelId", levelId);
		payInfo.put("carrier", carrier);
		payInfo.put("imsi", imsi);
		payInfo.put("payPluginName", payPluginName);
		payInfo.put("userType", userType); //"new" 首次登陆的用户， "old"非首次登陆的用户
		
		payInfo.saveInBackground(new SaveCallback() {
			public void done(AVException e) {
				if (e == null) {
					callback.result(true);
				} else {
					callback.result(false);
				}
			}
		});
	}
	
	/**
	 * 获取支付开关
	 * 支付开关的数据需要在后台配置
	 * type == 1 ： mm支付通道中允许打开新银河支付
	 * @param callback
	 */
	public static void getSwitchState(final String type, final TbuCallback callback) {
		if(!TbuCloud.isSuccessInit()){
			if(callback != null) {
				callback.result(false);
			}
		}
		
		AVQuery<AVObject> query = new AVQuery<AVObject>("Switch");
		query.whereEqualTo(type, 1);
		query.findInBackground(new FindCallback<AVObject>() {
		    public void done(List<AVObject> avObjects, AVException e) {
		        if (e == null) {
		        		if(avObjects!=null && !avObjects.isEmpty()) {
		        			if(avObjects.get(0).getInt("state") == 1) {
		        				callback.result(true);
		        			}else {
		        				callback.result(false);
		        			}
		        		}else {
		        			callback.result(false);
		        		}
		        } else {
		        		callback.result(false);
		        }
		    }
		});	
	}
}
