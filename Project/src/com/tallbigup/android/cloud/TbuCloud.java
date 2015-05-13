package com.tallbigup.android.cloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.tallbigup.android.cloud.feedback.FeedbackDialog;
import com.tallbigup.android.cloud.recommend.MoreGameDialog;
import com.tallbigup.android.cloud.recommend.RecommendCallback;

public class TbuCloud {

	public static final String POXIAO_CLOUD = "px_cloud";
	public static final String POXIAO_CLOUD_PAY = "px_cloud_pay";
	public static final String POXIAO_CLOUD_LOGIN = "px_cloud_login";
	public static final String POXIAO_CLOUD_PUSH = "px_cloud_push";

	public static final int DEFAULT_CACHE_LIFE = 24;
	public static int cacheLife = DEFAULT_CACHE_LIFE;

	private static String province;
	private static boolean successInit = false;
	private static String appId = "";

	private static String channelId = "";

	/**
	 * 游戏版本号，用来在自定义事件的时候在最前面标记
	 */
	private static String TAGID = "0";

	/**
	 * 判断插件是否初始化成功
	 * 
	 * @return true，成功, false，失败
	 */
	public static boolean isSuccessInit() {
		return successInit;
	}

	/**
	 * 初始化插件。
	 * 
	 * @param context
	 * @param callback
	 * @param appId
	 * @param appKey
	 * @param gameVersion
	 * @param cls
	 */
	public static void initCloud(final Context context,
			final TbuCallback callback, final String appId,
			final String appKey, final String gameVersion,
			final Class<? extends Activity> cls) {
		if (gameVersion != null && gameVersion.length() > 0) {
			TAGID = gameVersion;
		}

		TbuCloud.appId = appId;

		AVOSCloud.useAVCloudCN(); 
		AVOSCloud.initialize(context, appId, appKey);

		PushService.setDefaultPushCallback(context, cls);
		PushService.subscribe(context, "public", cls);
		AVInstallation.getCurrentInstallation().saveInBackground();
		
		AVAnalytics.enableCrashReport(context, true);

		sendReq();

		channelId = getChannelId(context);
		
		successInit = true;
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		if (callback != null) {
			callback.result(successInit);
		}
	}

	/**
	 * 在服务端记录1次应用登陆[非实时上传数据]
	 * 
	 * @param activity
	 */
	public static void markAppOpened(final Activity activity) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {
				AVAnalytics.trackAppOpened(activity.getIntent());
				return null;
			}
		}.execute("");
	}

	/**
	 * 
	 * @param context
	 * @param title
	 *            自定义事件的名称，会自动在头上加上游戏的版本号
	 * @param tag
	 *            可以通过不同的tag来标记分类
	 */
	public static void markPersonInfo(final Context context,
			final String title, final String tag) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {
				AVAnalytics.onEvent(context, TAGID + "_" + title, tag);
				return null;
			}
		}.execute("");
	}

	/**
	 * 更新玩家信息
	 * 
	 * @param objectId
	 * @param level
	 * @param money
	 */
	public static void updatePlayerInfo(final String objectId,
			final String level, final int money, final int payMoney,
			final int score) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {

				if (objectId == null) {
					return null;
				}
				AVObject playerInfo = new AVObject("Player");
				AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

				try {
					playerInfo = query.get(objectId);
					playerInfo.put("level", level);
					playerInfo.put("money", money);
					playerInfo.put("payMoney", payMoney);
					if (playerInfo.getInt("score") < score) {
						playerInfo.put("score", score);
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
				return null;
			}
		}.execute("");
	}

	public static void updatePlayerScore(final Context context,
			final String objectId, final int score,
			final UpdateCallback callback) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {

				gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
						Context.MODE_PRIVATE);
				Editor editor = gameInfo.edit();
				editor.putInt("topScore", score);
				editor.commit();
				if (objectId == null || !isSuccessInit()) {
					callback.result(false, "网络未连接");
					return null;
				}

				if (score <= 0) {
					return null;
				}

				AVObject playerInfo = new AVObject("Player");
				AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

				try {
					playerInfo = query.get(objectId);
					int topScore = playerInfo.getInt("score");
					if (score <= topScore) {
						return null;
					}
					playerInfo.put("score", score);
					playerInfo.saveInBackground(new SaveCallback() {
						@Override
						public void done(AVException e) {
							if (e == null) {
								callback.result(true, "更新得分成功");
								Log.i("POXIAOCLOUD", "Save successfully.");
							} else {
								callback.result(true, "更新得分失败");
								Log.e("POXIAOCLOUD", "Save failed.");
							}
						}
					});
				} catch (AVException e) {
					callback.result(false, "更新得分失败");
					e.printStackTrace();
				}
				return null;
			}
		}.execute("");
	}

	/**
	 * 更新玩家昵称
	 * 
	 * @param objectId
	 *            用户playerId
	 * @param nickName
	 *            玩家昵称
	 */
	public static void updatePlayerNickName(final Context context,
			final String objectId, final String nickName,
			final UpdateCallback callback) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {

				gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
						Context.MODE_PRIVATE);
				if (objectId == null || !isSuccessInit()) {
					callback.result(false, "网络未连接");
					return null;
				}
				AVObject playerInfo = new AVObject("Player");
				AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

				try {
					playerInfo = query.get(objectId);
					playerInfo.put("nickName", nickName);
					playerInfo.saveInBackground(new SaveCallback() {
						@Override
						public void done(AVException e) {
							if (e == null) {
								Editor editor = gameInfo.edit();
								editor.putString("nickName", nickName);
								editor.commit();
								callback.result(true, "更新昵称成功");
								Log.i("POXIAOCLOUD", "Save successfully.");
							} else {
								callback.result(true, "更新昵称失败");
								Log.e("POXIAOCLOUD", "Save failed.");
							}
						}
					});
				} catch (AVException e) {
					callback.result(true, "更新昵称失败");
					e.printStackTrace();
				}
				return null;
			}
		}.execute("");
	}

	/**
	 * 判断昵称是否唯一、可用
	 * 
	 * @param nickName
	 * @param callback
	 */
	public static void isNickNameUnique(final String nickName,
			final UpdateCallback callback) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {

				if (!isSuccessInit()) {
					callback.result(false, "网络未连接");
					return null;
				}

				AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

				query.whereEqualTo("nickName", nickName);
				try {
					if (query.getFirst() == null) {
						callback.result(true, "该昵称可用");
					} else {
						callback.result(false, "该昵称不可用");
					}
				} catch (AVException e) {
					callback.result(false, "发生错误");
					e.printStackTrace();
				}
				return null;
			}
		}.execute("");
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
	public static void createPlayer(final String nickName, final String IMSI,
			final String gameVersionCode, final String level, final int money,
			final String enterId, final int payMoney, final int score,
			final CreatePlayerCallback callback) {
			if (TbuCloud.isSuccessInit()) {
				if (callback != null) {
					callback.result(false, null);
				}
			}

			final Player player = new Player();
			
			player.setUsername(enterId+"_"+nickName+new Random().nextInt(100));
			player.setPassword("abc123");
			player.setNickName(nickName);
			player.setIMSI(IMSI);
			player.setGameVersionCode(gameVersionCode);
			player.setLevel(level);
			player.setMoney(money);
			player.setPayMoney(payMoney);
			player.setEnterId(enterId);
			player.setScore(score);

			player.saveInBackground(new SaveCallback() {
				public void done(AVException e) {
					if (e == null) {
						if (callback != null) {
							callback.result(true, player.getObjectId());
						}
					} else {
						e.printStackTrace();
						if (callback != null) {
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
	public static void setPayInfo(final String requestType, final int money,
			final String payType, final String userId, final String desc,
			final int payCount, final String enterId, final String orderId,
			final String errorCode, final String errorMessgae,
			final String gameVersion, final String payVersionId,
			final String levelId, final String imsi, final String carrier,
			final String payPluginName, final String userType,
			final TbuCallback callback) {
		if (TbuCloud.isSuccessInit()) {
			callback.result(false);
		}
		AVObject payInfo = new AVObject("PayInfo");

		payInfo.put("requestType", requestType); // （"request" "clickOk"
													// "cancel" "fail",
													// "success"）
		payInfo.put("money", money); // 单位：分
		payInfo.put("payType", payType);
		if(userId == null){
			payInfo.put("userId", "");
		}else{
			payInfo.put("userId", userId);
		}
		payInfo.put("desc", desc);
		payInfo.put("payCount", payCount);
		payInfo.put("enterId", enterId);
		payInfo.put("orderId", orderId);
		payInfo.put("errorCode", errorCode);
		payInfo.put("errorMessgae", errorMessgae);
		payInfo.put("gameVersion", gameVersion);
		if(payVersionId == null){
			payInfo.put("payVersionId", "1");
		}else{
			payInfo.put("payVersionId", payVersionId);
		}
		payInfo.put("levelId", levelId);
		payInfo.put("carrier", carrier);
		payInfo.put("imsi", imsi);
		if(payPluginName == null){
			payInfo.put("payPluginName", "");
		}else{
			payInfo.put("payPluginName", payPluginName);
		}
		payInfo.put("userType", userType); // "new" 首次登陆的用户， "old"非首次登陆的用户

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
	
	public static void setFeedback(final String playerId,final String feedback){
		AVObject payInfo = new AVObject("Feedback");

		payInfo.put("playerId", playerId); 
		payInfo.put("feedback", feedback);

		payInfo.saveInBackground(new SaveCallback() {
			public void done(AVException e) {
				if (e == null) {
					Log.e(POXIAO_CLOUD,"上传反馈成功...");
				} else {
					Log.e(POXIAO_CLOUD,"上传反馈失败...");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 获取支付开关 支付开关的数据需要在后台配置 type == 1 ： mm支付通道中允许打开新银河支付
	 * 
	 * @param callback
	 */
	public static void getSwitchState(final int type, final TbuCallback callback) {
		if (!TbuCloud.isSuccessInit()) {
			if (callback != null) {
				callback.result(false);
			}
			return;
		}

		AVQuery<AVObject> query = new AVQuery<AVObject>("Switch");
		query.whereEqualTo("type", type);
		query.findInBackground(new FindCallback<AVObject>() {
			public void done(List<AVObject> avObjects, AVException e) {
				if (e == null) {
					if (avObjects != null && !avObjects.isEmpty()) {
						AVObject obj = avObjects.get(0);
						if (obj.getString("black_list") != null
								&& obj.getString("black_list").contains(
										channelId)) {
							callback.result(false);
						} else if (obj.getInt("state") == 1) {
							callback.result(true);
						} else {
							callback.result(false);
						}
					} else {
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
	 * 
	 * @param className
	 *            对应的表名
	 * @param version
	 *            游戏版本号
	 * @param currentLevel
	 *            当前等级
	 * @param currentPropName
	 *            当前道具名称
	 * @param propCounts
	 *            道具或技能消耗的数量
	 * @param callback
	 *            更新结果回调
	 */
	public static void updateGamePropInfo(final String className,
			final String version, final int currentLevel,
			final String currentPropName, final int propCounts,
			final TbuCallback callback) {
		if (!TbuCloud.isSuccessInit()) {
			if (callback != null) {
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
					if (avObjects != null && !avObjects.isEmpty()) {
						AVObject object = avObjects.get(0);
						int lastCounts = object.getInt("counts");
						Log.i("POXIAOCLOUD", "lastCounts=" + lastCounts);
						object.put("counts", lastCounts + propCounts);
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
					} else {
						gamePropInfo.put("version", version);
						gamePropInfo.put("level", currentLevel);
						gamePropInfo.put("propName", currentPropName);
						gamePropInfo.put("counts", propCounts);

						gamePropInfo.saveInBackground(new SaveCallback() {
							public void done(AVException e) {
								if (e == null) {
									if (callback != null) {
										callback.result(true);
									}
								} else {
									if (callback != null) {
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
					gamePropInfo.put("counts", propCounts);

					gamePropInfo.saveInBackground(new SaveCallback() {
						public void done(AVException e) {
							if (e == null) {
								if (callback != null) {
									callback.result(true);
								}
							} else {
								if (callback != null) {
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
	 * 更新游戏道具、技能消耗信息
	 * 
	 * @param className
	 *            对应的表名
	 * @param version
	 *            游戏版本号
	 * @param currentPropName
	 *            当前道具名称
	 * @param propCounts
	 *            道具或技能消耗的数量
	 * @param propMoney
	 *            道具消耗或者获取的金币
	 * @param callback
	 *            更新结果回调
	 */
	public static void updateGamePropConsume(final String className,
			final String version, final String currentPropName,
			final int propCounts, final int propMoney) {
		final AVObject gamePropInfo = new AVObject(className);
		AVQuery<AVObject> query = new AVQuery<AVObject>(className);
		query.whereEqualTo("version", version);
		query.whereEqualTo("propName", currentPropName);
		query.findInBackground(new FindCallback<AVObject>() {
			public void done(List<AVObject> avObjects, AVException e) {
				if (e == null) {
					if (avObjects != null && !avObjects.isEmpty()) {
						AVObject object = avObjects.get(0);
						int lastCounts = object.getInt("counts");
						int lastMoney = object.getInt("money");
						Log.i("POXIAOCLOUD", "lastCounts=" + lastCounts
								+ ";lastMoney" + lastMoney);
						object.put("counts", lastCounts + propCounts);
						object.put("money", lastMoney + propMoney);
						object.saveInBackground();
					} else {
						gamePropInfo.put("version", version);
						gamePropInfo.put("propName", currentPropName);
						gamePropInfo.put("counts", propCounts);
						gamePropInfo.put("money", propMoney);
						gamePropInfo.saveInBackground();
					}
				} else {
					Log.e("POXIAOCLOUD", "AVException" + e);
				}
			}
		});
	}

	/**
	 * 统计用户打开push情况
	 * 
	 * @param activity
	 *            初始化avoscloud时传入的activity的onStart()方法中调用
	 */
	public static void markOpenPushInfo(final Activity activity) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {
				Intent intent = activity.getIntent();
				AVAnalytics.trackAppOpened(intent);
				return null;
			}
		}.execute("");
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
	 * 
	 * @param enterId
	 *            渠道号
	 * @param callback
	 */
	public static void getRecommendList(final String enterId,
			final RecommendCallback callback) {
		if (!isSuccessInit()) {
			callback.result(false, null);
			return;
		}
		final Editor editor = gameInfo.edit();
		Long lastcache = gameInfo.getLong("lastcache", 0);
		final Map<Integer, String[]> paramList1 = new HashMap<Integer, String[]>();
		final Map<Integer, String[]> paramList2 = new HashMap<Integer, String[]>();
		int size1 = gameInfo.getInt("recommend_1_count", 0);
		int size2 = gameInfo.getInt("recommend_2_count", 0);
		if ((size1 > 0 || size2 > 0)
				&& (System.currentTimeMillis() - lastcache) < cacheLife * 60 * 60 * 1000) {
			String[] params1 = null;
			String recommendStr = "";
			if (size1 > 0) {
				for (int i = 0; i < size1; i++) {
					recommendStr = gameInfo.getString("recommend_1_" + i, "");
					if (!recommendStr.equals("")) {
						params1 = recommendStr.split("&");
					}
					paramList1.put(i, params1);
				}
				callback.result(true, paramList1);
			} else if (size2 > 0) {
				for (int i = 0; i < size2; i++) {
					recommendStr = gameInfo.getString("recommend_2_" + i, "");
					if (!recommendStr.equals("")) {
						params1 = recommendStr.split("&");
					}
					paramList2.put(i, params1);
				}
				callback.result(true, paramList2);
			}
			return;
		}
		AVQuery<AVObject> query = new AVQuery<AVObject>("Recommend");
		String[] channels = { enterId, POXIAO };
		query.whereContainedIn("channel_id", Arrays.asList(channels));
		query.findInBackground(new FindCallback<AVObject>() {
			@Override
			public void done(List<AVObject> avObjects, AVException e) {
				if (e == null) {
					String[] params = null;
					String channelId;
					int order;
					Log.w("MCH", "avObjects.size()=" + avObjects.size());
					for (int i = 0; i < avObjects.size(); i++) {
						Log.w("MCH", "avObjects.order="
								+ avObjects.get(i).getInt("order"));
						params = new String[5];
						channelId = avObjects.get(i).getString(CHANNEL_ID)
								.trim();
						if (enterId.equals(channelId)) {
							params[0] = avObjects.get(i).getString(ICON_URL)
									.trim();
							params[1] = avObjects.get(i).getString(GAME_NAME)
									.trim();
							params[2] = avObjects.get(i)
									.getString(PACKAGE_NAME).trim();
							params[3] = avObjects.get(i).getString(PARAM)
									.trim();
							params[4] = avObjects.get(i)
									.getString(APK_DOWNLOAD_RUL).trim();
							order = avObjects.get(i).getInt("order") - 1;
							editor.putString("recommend_1_" + order, params[0]
									+ "&" + params[1] + "&" + params[2] + "&"
									+ params[3] + "&" + params[4]);
							paramList1.put(order, params);
						} else if (POXIAO.equals(channelId)) {
							params[0] = avObjects.get(i).getString(ICON_URL)
									.trim();
							params[1] = avObjects.get(i).getString(GAME_NAME)
									.trim();
							params[2] = avObjects.get(i)
									.getString(PACKAGE_NAME).trim();
							params[3] = avObjects.get(i).getString(PARAM)
									.trim();
							params[4] = avObjects.get(i)
									.getString(APK_DOWNLOAD_RUL).trim();
							order = avObjects.get(i).getInt("order") - 1;
							editor.putString("recommend_2_" + order, params[0]
									+ "&" + params[1] + "&" + params[2] + "&"
									+ params[3] + "&" + params[4]);
							paramList2.put(order, params);
						}
						if (avObjects.size() - 1 == i) {
							editor.putInt("recommend_1_count",
									paramList1.size());
							editor.putInt("recommend_2_count",
									paramList2.size());
							editor.putLong("lastcache",
									System.currentTimeMillis());
							editor.commit();
							if (paramList1.size() > 0) {
								callback.result(true, paramList1);
							} else {
								callback.result(true, paramList2);
							}
						}
					}
				} else {
					Log.d("POXIAOCLOUD", "查询错误：" + e.getMessage());
					callback.result(false, null);
				}
			}
		});
	}

	/**
	 * 查询玩家得分及排名、前20名得分及昵称
	 * 
	 * @param playerId
	 *            玩家playerId
	 * @param score
	 *            玩家当局得分
	 * @param callback
	 */
	public static void getRankInfo(final Context context,
			final String playerId, final int score,
			final RankInfoCallback callback) {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {

				gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
						Context.MODE_PRIVATE);
				Editor editor = gameInfo.edit();

				Map<Integer, Map<String, String>> map = new HashMap<Integer, Map<String, String>>();
				Map<String, String> rankInfo = null;
				int topRank = 1;
				int topScore = 0;
				int minScore = 0;
				String nickName = gameInfo.getString("nickName", "unknown");
				Long lastCacheTime = gameInfo.getLong("lastCacheTime", 0);
				String rankInfoStr20 = gameInfo.getString("rankInfoStr20", "");
				int[] scores = new int[20];
				String[] nickNames = new String[20];
				int[] ranks = new int[20];
				int top20Rank = 0;
				if (rankInfoStr20 != null
						&& !rankInfoStr20.equals("")
						&& (System.currentTimeMillis() - lastCacheTime) < cacheLife * 60 * 60 * 1000) {
					for (int i = 1; i < 21; i++) {
						String cacheRankInfo = gameInfo.getString("rankInfoStr"
								+ i, "");
						String[] caches = cacheRankInfo.split(",");
						rankInfo = new HashMap<String, String>();

						rankInfo.put("rank", caches[0]);
						rankInfo.put("nickName", caches[1]);
						rankInfo.put("score", caches[2]);

						top20Rank = Integer.valueOf(caches[0]);
						scores[top20Rank - 1] = Integer.valueOf(caches[2]);
						nickNames[top20Rank - 1] = caches[1];
						ranks[top20Rank - 1] = top20Rank;

						if (caches[0].equals("20")) {
							minScore = Integer.valueOf(caches[2]);
						}
						map.put(Integer.valueOf(caches[0]), rankInfo);
					}
					topRank = gameInfo.getInt("topRank", -1);
					topScore = gameInfo.getInt("topScore", 0);
					if (score > topScore) {
						topScore = score;
					}
					if (topScore > minScore) {
						map.clear();
						topRank = 1;
						for (int i = 0; i < scores.length; i++) {
							if (topScore < scores[i]) {
								topRank++;
							} else {
								break;
							}
						}
						for (int i = 1; i <= 20; i++) {
							rankInfo = new HashMap<String, String>();
							if (i < topRank) {
								rankInfo.put("rank", String.valueOf(i));
								rankInfo.put("nickName", nickNames[i - 1]);
								rankInfo.put("score",
										String.valueOf(scores[i - 1]));
							} else if (i == topRank) {
								rankInfo.put("rank", String.valueOf(topRank));
								rankInfo.put("nickName", nickName);
								rankInfo.put("score", String.valueOf(topScore));
							} else {
								rankInfo.put("rank", String.valueOf(i));
								rankInfo.put("nickName", nickNames[i - 2]);
								rankInfo.put("score",
										String.valueOf(scores[i - 2]));
							}
							map.put(i, rankInfo);
						}
					} else if (topScore == minScore) {
						topRank = 20;
						rankInfo = new HashMap<String, String>();
						rankInfo.put("rank", String.valueOf(topRank));
						rankInfo.put("nickName", nickName);
						rankInfo.put("score", String.valueOf(topScore));
						map.put(20, rankInfo);
					} else {
						topRank = 20 + (minScore - topScore) / 5;
					}
					editor.putInt("topRank", topRank);
					rankInfo = new HashMap<String, String>();
					rankInfo.put("rank", String.valueOf(topRank));
					rankInfo.put("nickName", nickName);
					rankInfo.put("score", String.valueOf(topScore));
					map.put(0, rankInfo);
					editor.commit();
					callback.result(true, map);
					return null;
				}

				if (!isSuccessInit() || playerId == null || playerId.equals("")) {
					callback.result(false, null);
					return null;
				}

				List<AVObject> avObjects = null;// 返回的排名表中的对象
				AVQuery<AVObject> query = new AVQuery<AVObject>("Player");

				AVObject user = null;
				try {
					user = query.get(playerId);
					topScore = gameInfo
							.getInt("topScore", user.getInt("score"));
					nickName = gameInfo.getString("nickName",
							user.getString("nickName"));
				} catch (AVException e1) {
					Log.e("POXIAOCLOUD", "fail select ...");
				}

				query.orderByDescending("score"); // 按score字段的降序查找
				query.setLimit(50);

				String rankInfoStr = "";

				try {
					avObjects = query.find();

					if (avObjects != null) {
						for (int i = 1; i < avObjects.size() + 1; i++) {
							rankInfo = new HashMap<String, String>();
							rankInfo.put("rank", String.valueOf(i));
							rankInfo.put("nickName", avObjects.get(i - 1)
									.getString("nickName"));
							rankInfo.put(
									"score",
									String.valueOf(avObjects.get(i - 1).getInt(
											"score")));
							rankInfoStr = i
									+ ","
									+ avObjects.get(i - 1)
											.getString("nickName") + ","
									+ avObjects.get(i - 1).getInt("score");
							editor.putString("rankInfoStr" + i, rankInfoStr);
							map.put(i, rankInfo);
							if (i == 20) {
								break;
							}
						}
						editor.commit();
						if (avObjects.size() >= 20) {
							minScore = Integer
									.valueOf(map.get(20).get("score"));
						} else {
							minScore = Integer.valueOf(map.get(
									avObjects.size() - 1).get("score"));
						}
						if (avObjects.contains(user)) {
							topRank = avObjects.indexOf(user) + 1;
						} else {
							topRank = avObjects.size() + (minScore - topScore)
									/ 5;
						}
						Log.i("POXIAOCLOUD", "topRank=" + topRank);
						rankInfo = new HashMap<String, String>();
						rankInfo.put("rank", String.valueOf(topRank));
						rankInfo.put("score", String.valueOf(topScore));
						rankInfo.put("nickName", nickName);
						map.put(0, rankInfo);
						Log.d("POXIAOCLOUD", "success select ...");
						editor.putInt("topRank", topRank);
						editor.putInt("topScore", topScore);
						editor.putString("nickName", nickName);
						editor.putLong("lastCacheTime",
								System.currentTimeMillis());
						editor.commit();
						callback.result(true, map);
					} else {
						callback.result(false, null);
						return null;
					}
				} catch (AVException e) {
					Log.d("POXIAOCLOUD", "fail select ...");
					callback.result(false, null);
					return null;
				}
				return null;
			}
		}.execute("");
	}

	private static SharedPreferences gameInfo;

	/**
	 * 标记用户付费情况
	 * 
	 * @param context
	 * @param type
	 *            0-未付费 1-已付费
	 */
	public static void markUserPay(Context context, int type) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		Editor editor = gameInfo.edit();
		editor.putInt(TbuCloud.POXIAO_CLOUD_PAY, type);
		editor.commit();
	}

	/**
	 * 标记用户登录时间
	 * 
	 * @param context
	 * @param millionSeconds
	 *            当前时间的毫秒数
	 */
	public static void markUserLogin(Context context, long millionSeconds) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		Editor editor = gameInfo.edit();
		editor.putLong(TbuCloud.POXIAO_CLOUD_LOGIN, millionSeconds);
		editor.commit();
	}

	/**
	 * 获取用户上次登录时间
	 * 
	 * @param context
	 * @param tag
	 * @return
	 */
	public static long getUserLastLogin(Context context) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		return gameInfo.getLong(TbuCloud.POXIAO_CLOUD_LOGIN, 0);
	}

	/**
	 * 获取用户付费情况
	 * 
	 * @param context
	 * @param tag
	 * @return
	 */
	public static int getUserPay(Context context) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		return gameInfo.getInt(TbuCloud.POXIAO_CLOUD_PAY, 0);
	}

	/**
	 * 获取用户所在地理位置
	 */
	public static String getUserProvince() {
		return province;
	}

	private static final String urlAddress = "http://115.236.18.198:8088/charge/getProv.htm";

	// private static final String urlAddress =
	// "http://poxiao888.vicp.cc:8089/charge/getProv.htm";

	private static void sendReq() {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {
				try {
					URL url = new URL(params[0]);
					HttpURLConnection urlConnection = (HttpURLConnection) url
							.openConnection();

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

					return new String(byteBuffer);
				} catch (Exception e) {
					e.printStackTrace();
					return "unknow";
				}
			}

			@Override
			protected void onPostExecute(String result) {
				Log.i("POXIAOCLOUD", "用户所在省份：" + result);
				province = result;
			}
		}.execute(urlAddress);
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
	 * 
	 * @param context
	 * @param millionSeconds
	 *            当前时间的毫秒数
	 */
	public static void markUserReceiverPush(Context context, long millionSeconds) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		Editor editor = gameInfo.edit();
		editor.putLong(TbuCloud.POXIAO_CLOUD_PUSH, millionSeconds);
		editor.commit();
	}

	/**
	 * 获取用户上次接收push时间
	 * 
	 * @param context
	 * @param tag
	 * @return
	 */
	public static long getUserLastReceiverPush(Context context) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		return gameInfo.getLong(TbuCloud.POXIAO_CLOUD_PUSH, 0);
	}

	/**
	 * 标记用户是否为新用户
	 * 
	 * @param context
	 * @return true -新用户 false-老用户
	 */
	public static int markUserType(Context context) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		if (gameInfo.getInt("firstLogin", 0) == 0) {
			Editor editor = gameInfo.edit();
			editor.putInt("firstLogin", 1);
			editor.commit();
			return 0;
		} else {
			return 1;
		}
	}

	private static String shareContent;

	/**
	 * 分享当前应用
	 * 
	 * @param activity
	 * @param content
	 *            自定义分享描述
	 */
	public static void share(Activity activity, String content) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享给好友");
		intent.putExtra(Intent.EXTRA_TEXT, content + shareContent);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(Intent.createChooser(intent, activity.getTitle()));
	}
	
	/**
	 * 获取分享内容
	 */
	public static void getShareContent(){
		if (!TbuCloud.isSuccessInit()) {
			return;
		}
		
		AVQuery<AVObject> query = new AVQuery<AVObject>("Share");
		query.whereExists("share_content");
		query.findInBackground(new FindCallback<AVObject>() {
			public void done(List<AVObject> avObjects, AVException e) {
				if (e == null) {
					if (avObjects != null && !avObjects.isEmpty()) {
						AVObject obj = avObjects.get(0);
						shareContent = obj.getString("share_content");
						Log.i("POXIAOCLOUD","shareContent=" + shareContent);
					} else {
						shareContent = "";
					}
				} else {
					shareContent = "";
				}
			}
		});
	}

	public static void showMoreGame(final Activity activity) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				MoreGameDialog moreGameDialog = new MoreGameDialog(activity,
						activity);
				moreGameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				moreGameDialog.getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				moreGameDialog.show();
				moreGameDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
			}
		});
	}
	
	public static void showFeedback(final Activity activity,final String playerId) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				FeedbackDialog feedbackDialog = new FeedbackDialog(activity,playerId);
				feedbackDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				feedbackDialog.getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				feedbackDialog.show();
				feedbackDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
			}
		});
	}

	public static String getAppId() {
		return TbuCloud.appId;
	}

	private static String getChannelId(Context context) {
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			return appInfo.metaData.getString("Channel ID");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "unknown";
		}
	}

	public static void saveNotifyId(Context context, int id) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		Editor editor = gameInfo.edit();
		editor.putInt("notify_id", id);
		editor.commit();
	}

	public static int getNotifyId(Context context) {
		gameInfo = context.getSharedPreferences(TbuCloud.POXIAO_CLOUD,
				Context.MODE_PRIVATE);
		return gameInfo.getInt("notify_id", 0);
	}
}
