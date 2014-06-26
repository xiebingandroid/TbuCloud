package com.tallbigup.android.cloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
	
	public static final int BASE = 2;
	public static final String POXIAO_CLOUD = "px_cloud";
	public static final String POXIAO_CLOUD_PAY = "px_cloud_pay";
	public static final String POXIAO_CLOUD_LOGIN = "px_cloud_login";
	public static final String POXIAO_CLOUD_PUSH = "px_cloud_push";
	public static final String POXIAO_CLOUD_TOP_RANK_INFO = "px_cloud_top_rank_info";
	public static final String POXIAO_CLOUD_CURRENT_RANK_INFO = "px_cloud_current_rank_info";
	
	private static String province;
	private static boolean successInit = false;
	private static boolean hasGetRecommendFromNet = false;
	private static boolean hasGetUserRankInfoFromNet = false;
	private static boolean hasGetTop20RankInfoFromNet = false;
	
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
	    
	    sendReq();
	    
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
			final int payMoney,
			final int score) {
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
			if(playerInfo.getInt("score") < score){
				playerInfo.put("score",score);
			}
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
	
	public static void updatePlayerScore(final String objectId,final int score){
		if(objectId == null) {
			return ;
		}
		AVObject playerInfo = new AVObject("Player");
		AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

		try {
			playerInfo = query.get(objectId);
			playerInfo.put("score",score);
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
	 * 更新玩家昵称
	 * @param objectId  用户playerId
	 * @param nickName  玩家昵称
	 */
	public static void updatePlayerNickName(final String objectId,final String nickName){
		if(objectId == null) {
			return ;
		}
		AVObject playerInfo = new AVObject("Player");
		AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

		try {
			playerInfo = query.get(objectId);
			playerInfo.put("nickName",nickName);
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
			final int score,
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
		player.put("score", score);

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
					e.printStackTrace();
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
	public static void getSwitchState(final int type, final TbuCallback callback) {
		if(!TbuCloud.isSuccessInit()){
			if(callback != null) {
				callback.result(false);
			}
		}
		
		AVQuery<AVObject> query = new AVQuery<AVObject>("Switch");
		query.whereEqualTo("type", type);
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
		
	/**
	 * 更新游戏道具、技能使用信息
	 * @param className 对应的表名
	 * @param version   游戏版本号
	 * @param currentLevel 当前等级
	 * @param currentPropName 当前道具名称
	 * @param propCounts 道具或技能消耗的数量
	 * @param callback 更新结果回调
	 */
	public static void updateGamePropInfo(final String className,final String version,final int currentLevel,
						final String currentPropName, final int propCounts,final TbuCallback callback){
		if(!TbuCloud.isSuccessInit()){
			if(callback != null){
				callback.result(false);
			}
			return;
		}

		final AVObject gamePropInfo = new AVObject(className);
		
		AVQuery<AVObject> query = new AVQuery<AVObject>(className);
		query.whereEqualTo("version", version);
		query.whereEqualTo("level", currentLevel);
		query.whereEqualTo("propName", currentPropName);
		query.findInBackground(new FindCallback<AVObject>() {
		    public void done(List<AVObject> avObjects, AVException e) {
		        if (e == null) {
	        		if(avObjects!=null && !avObjects.isEmpty()) {
	        			AVObject object = avObjects.get(0);
	        			int lastCounts = object.getInt("counts");
	        			Log.i("POXIAOCLOUD","lastCounts=" + lastCounts);
	        			object.put("counts",lastCounts + propCounts);
	        			object.saveInBackground(new SaveCallback() {
	        				   @Override
	        				   public void done(AVException e) {
	        				        if (e == null) {
	        				            Log.i("POXIAOCLOUD", "Save successfully.");
	        				        } else {
	        				            Log.e("POXIAOCLOUD", "Save failed.");
	        				        }
	        				    }
	        				});
	        		}else {
	        			gamePropInfo.put("version", version);
	    				gamePropInfo.put("level", currentLevel);
	    				gamePropInfo.put("propName", currentPropName);
	    				gamePropInfo.put("counts",propCounts);

	        			gamePropInfo.saveInBackground(new SaveCallback() {
	        			    public void done(AVException e) {
	        			        if (e == null) {
	        			        		if(callback != null) {
	        			        			callback.result(true);
	        			        		}
	        			        } else {
	        			        		if(callback != null) {
	        			        			callback.result(false);
	        			        		}
	        			        }
	        			    }
	        			});
	        		}
		        } else {
		        	gamePropInfo.put("version", version);
    				gamePropInfo.put("level", currentLevel);
    				gamePropInfo.put("propName", currentPropName);
    				gamePropInfo.put("counts",propCounts);

        			gamePropInfo.saveInBackground(new SaveCallback() {
        			    public void done(AVException e) {
        			        if (e == null) {
        			        		if(callback != null) {
        			        			callback.result(true);
        			        		}
        			        } else {
        			        		if(callback != null) {
        			        			callback.result(false);
        			        		}
        			        }
        			    }
        			});
		        }
		    }
		});	
	
	}
	
		
	/**
	 * 统计用户打开push情况
	 * @param activity  初始化avoscloud时传入的activity的onStart()方法中调用
	 */
	public static void markOpenPushInfo(final Activity activity){
		Intent intent = activity.getIntent();
        AVAnalytics.trackAppOpened(intent);
	}
	
	public static final String ICON_URL = "icon_url";	
	public static final String GAME_NAME = "game_name";	
	public static final String PACKAGE_NAME = "package_name";	
	public static final String PARAM = "param";	
	public static final String APK_DOWNLOAD_RUL = "apk_download_url";	
	public static final String POXIAO = "poxiao";
	public static final String CHANNEL_ID = "channel_id";
 	
	/**
	 * 获取交叉推荐列表
	 * @param enterId   渠道号
	 * @param callback  
	 */
	public static void getRecommendList(final String enterId, final RecommendCallback callback){
		if(!isSuccessInit()){
			callback.result(false, null);
			return;
		}
		final List<Map<String,String>> paramList1 = new ArrayList<Map<String,String>>();
		final List<Map<String,String>> paramList2 = new ArrayList<Map<String,String>>();
		AVQuery<AVObject> query = new AVQuery<AVObject>("Recommend");
		String[] channels = {enterId, POXIAO};
		query.whereContainedIn("channel_id", Arrays.asList(channels));
		if(hasGetRecommendFromNet){
			query.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
		}
		query.findInBackground(new FindCallback<AVObject>() {
			@Override
			public void done(List<AVObject> avObjects, AVException e) {
				if(e == null){
					Map<String,String> params = null;	
					String channelId;
					for(int i=0;i<avObjects.size();i++){
						params = new HashMap<String,String>();
						channelId = avObjects.get(i).getString(CHANNEL_ID).trim();
						if(enterId.equals(channelId)){
							params.put(ICON_URL, avObjects.get(i).getString(ICON_URL).trim());
							params.put(GAME_NAME, avObjects.get(i).getString(GAME_NAME).trim());
							params.put(PACKAGE_NAME, avObjects.get(i).getString(PACKAGE_NAME).trim());
							params.put(PARAM, avObjects.get(i).getString(PARAM).trim());
							params.put(APK_DOWNLOAD_RUL, avObjects.get(i).getString(APK_DOWNLOAD_RUL).trim());
							paramList1.add(params);	
						}else if(POXIAO.equals(channelId)){
							params.put(ICON_URL, avObjects.get(i).getString(ICON_URL).trim());
							params.put(GAME_NAME, avObjects.get(i).getString(GAME_NAME).trim());
							params.put(PACKAGE_NAME, avObjects.get(i).getString(PACKAGE_NAME).trim());
							params.put(PARAM, avObjects.get(i).getString(PARAM).trim());
							params.put(APK_DOWNLOAD_RUL, avObjects.get(i).getString(APK_DOWNLOAD_RUL).trim());
							paramList2.add(params);	
						}
						if(avObjects.size()-1 == i){
							if(paramList1.size() > 0){
								callback.result(true, paramList1);
							}else{
								callback.result(true, paramList2);
							}
							hasGetRecommendFromNet = true;
						}
					}
				}else{
					Log.d("POXIAOCLOUD","查询错误：" + e.getMessage());
					callback.result(false, null);
				}
			}
		});	
	}
	
	/**
	 * 查询玩家得分及排名
	 * @param playerId  玩家playerId
	 * @param score 玩家当局得分
	 * @param callback
	 */
	public static void getUserRankInfo(final String playerId,final int score, final UserRankInfoCallback callback){
		if(!isSuccessInit() || playerId == null || playerId.equals("")){
			callback.result(false, null);
			return;
		}
		boolean hasGetCurrentRank = false;
		boolean hasGetTopRank = false;
		Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
		Map<String,String> rankInfo = null;
		List<AVObject> avObjects = null;//返回的排名表中的对象
		AVQuery<AVObject> query = new AVQuery<AVObject>("Player");
		int currentRoundrank = 1; 
		int topRank = 1;
		int topScore = score;
		int minScore = 0;

		AVObject user = null;
		try {
			user = query.get(playerId);
			topScore = user.getInt("score");
			if(topScore < score){
				topScore = score;
			}
			Log.i("POXIAOCLOUD","topScore=" + topScore);
		} catch (AVException e1) {
			Log.e("POXIAOCLOUD","fail select ...");
		}
		
		if(hasGetUserRankInfoFromNet){
			query.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
		}
		query.orderByDescending("score");	//按score字段的降序查找
		query.setLimit(100);

		try {
		    avObjects = query.find();
		    
		    if(avObjects != null){			  
		    	Log.i("MCH","avObjects.size()=" + avObjects.size());
	    		minScore = avObjects.get(avObjects.size() - 1).getInt("score");
	    		Log.i("MCH","minScore=" + minScore);
		    	if(avObjects.contains(user)){
				    topRank = avObjects.indexOf(user);
		    	}else{
		    		if(topScore == minScore){
		    			topRank = avObjects.size() + 1;
		    		}else{
		    			if(minScore < topScore){
			    			topRank = avObjects.size() - (topScore - minScore);
		    			}else{
			    			topRank = avObjects.size() + (minScore - topScore) * BASE;
		    			}
		    		}
		    	}
			    Log.i("POXIAOCLOUD","topRank=" + topRank);
				rankInfo =new HashMap<String,String>();
				rankInfo.put("rank", String.valueOf(topRank));
				rankInfo.put("score", String.valueOf(topScore));
				map.put(TbuCloud.POXIAO_CLOUD_TOP_RANK_INFO,rankInfo);
				hasGetTopRank = true;
			    Log.d("POXIAOCLOUD", "success select ...");
		    }else{
			    callback.result(false, null);
			    return;
		    }
		} catch (AVException e) {
		    Log.d("POXIAOCLOUD", "fail select ...");
		    callback.result(false, null);
		    return;
		}
		for(AVObject av : avObjects){ 	
			if(score >= av.getInt("score")){
				rankInfo =new HashMap<String,String>();
				rankInfo.put("rank", String.valueOf(currentRoundrank));
				rankInfo.put("score", String.valueOf(score));
				map.put(TbuCloud.POXIAO_CLOUD_CURRENT_RANK_INFO,rankInfo);
				hasGetCurrentRank = true;
			}else{
				currentRoundrank ++;
				if(currentRoundrank-1 == avObjects.size()){
					if(score == minScore){
		    			currentRoundrank = avObjects.size() + 1;
		    		}else{
		    			currentRoundrank = avObjects.size() + (minScore - score) * BASE;
		    		}
					rankInfo =new HashMap<String,String>();
					rankInfo.put("rank", String.valueOf(currentRoundrank));
					rankInfo.put("score", String.valueOf(score));
					map.put(TbuCloud.POXIAO_CLOUD_CURRENT_RANK_INFO,rankInfo);
					hasGetCurrentRank = true;
				}
			}
			if(hasGetCurrentRank && hasGetTopRank){
				Log.i("POXIAOCLOUD","currentRoundrank=" + currentRoundrank);
				hasGetUserRankInfoFromNet = true;
				callback.result(true, map);
				return;
			}
		}
		callback.result(false, null);
	}
	
	/**
	 * 查询排名前20的玩家昵称及得分
	 * @return
	 */
	public static void getRankInfo(final Top20RankInfoCallback callback){
		if(!isSuccessInit()){
			callback.result(false, null);
			return;
		}
		List<AVObject> avObjects = null;//返回的排名表中的对象
		AVQuery<AVObject> query = new AVQuery<AVObject>("Player");
		List<Map<String,String>> rankInfos = new ArrayList<Map<String,String>>();
		Map<String,String> map = null;
		if(hasGetTop20RankInfoFromNet){
			query.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
		}
		query.setLimit(20);	//限制20条数据
		query.orderByDescending("score");	//按score字段的降序查找

		try {
		    avObjects = query.find();
		    if(avObjects != null){
			    Log.d("POXIAOCLOUD", "success select ...");
		    }else{
				callback.result(false, null);
				return;
		    }
		} catch (AVException e) {
		    Log.d("POXIAOCLOUD", "fail select ...");
			callback.result(false, null);
			return;
		}
		
		for(AVObject av : avObjects){
			map = new HashMap<String, String>();		
			map.put("nickName", av.getString("nickName"));
			map.put("score", String.valueOf(av.getInt("score")));

			rankInfos.add(map);			
		}
		hasGetTop20RankInfoFromNet = true;
		callback.result(true, rankInfos);
	}
	
	private static SharedPreferences gameInfo; 
	
	/**
	 * 标记用户付费情况
	 * @param context
	 * @param type  0-未付费    1-已付费
	 */
	public static void markUserPay(Context context,int type){
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD, Context.MODE_PRIVATE);
		Editor editor = gameInfo.edit();
		editor.putInt(TbuCloud.POXIAO_CLOUD_PAY, type);
		editor.commit();
	}
	
	/**
	 * 标记用户登录时间
	 * @param context
	 * @param millionSeconds 当前时间的毫秒数
	 */
	public static void markUserLogin(Context context,long millionSeconds){
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD, Context.MODE_PRIVATE);
		Editor editor = gameInfo.edit();
		editor.putLong(TbuCloud.POXIAO_CLOUD_LOGIN, millionSeconds);
		editor.commit();
	}
	
	
	/**
	 * 获取用户上次登录时间
	 * @param context
	 * @param tag
	 * @return
	 */
	public static long getUserLastLogin(Context context){
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD, Context.MODE_PRIVATE);
		return gameInfo.getLong(TbuCloud.POXIAO_CLOUD_LOGIN, 0);
	}
	
	/**
	 * 获取用户付费情况
	 * @param context
	 * @param tag
	 * @return
	 */
	public static int getUserPay(Context context){
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD, Context.MODE_PRIVATE);
		return gameInfo.getInt(TbuCloud.POXIAO_CLOUD_PAY, 0);
	}
	
	/**
	 * 获取用户所在地理位置
	 */
	public static String getUserProvince(){
		return province;
	}
	
//	private static final String urlAddress = "http://115.236.18.198:8088/charge/getProv.htm";
	private static final String urlAddress = "http://172.23.1.233:8089/charge/getProv.htm";

	private static void sendReq(){
				 try {
				      URL url = new URL(urlAddress);
				      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				
				      urlConnection.setRequestMethod("POST");
				      urlConnection.setDoInput(true);
				      urlConnection.setDoOutput(true);
				      urlConnection.setUseCaches(false);
				      urlConnection.setConnectTimeout(10 * 1000);
				      urlConnection.setReadTimeout(10 * 1000);
				      urlConnection.connect();
				
				      InputStream inputStream = urlConnection.getInputStream();
				      byte[] byteBuffer = input2byte(inputStream);
				     
				      inputStream.close();
				      urlConnection.disconnect();
				      
				      province = new String(byteBuffer);
				  } catch (Exception e) {
				      e.printStackTrace();
				      province = "unknow";
				  }
				 Log.i("MCH","province=" + province);
	 }	
	
	 private static final byte[] input2byte(InputStream inStream)
	            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
	}
	
	/**
	 * 标记用户接收push时间
	 * @param context
	 * @param millionSeconds 当前时间的毫秒数
	 */
	public static void markUserReceiverPush(Context context,long millionSeconds){
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD, Context.MODE_PRIVATE);
		Editor editor = gameInfo.edit();
		editor.putLong(TbuCloud.POXIAO_CLOUD_PUSH, millionSeconds);
		editor.commit();
	}
	
	/**
	 * 获取用户上次接收push时间
	 * @param context
	 * @param tag
	 * @return
	 */
	public static long getUserLastReceiverPush(Context context){
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD, Context.MODE_PRIVATE);
		return gameInfo.getLong(TbuCloud.POXIAO_CLOUD_PUSH, 0);
	}
	
	/**
	 * 标记用户是否为新用户
	 * @param context
	 * @return  true -新用户      false-老用户
	 */
	public static int markUserType(Context context){
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD, Context.MODE_PRIVATE);
		if(gameInfo.getInt("firstLogin", 0) == 0){
			Editor editor = gameInfo.edit();
			editor.putInt("firstLogin", 1);
			editor.commit();
			return 0;
		}else{
			return 1;
		}
	}
}
