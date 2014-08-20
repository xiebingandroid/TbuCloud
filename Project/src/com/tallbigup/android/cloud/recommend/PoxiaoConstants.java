package com.tallbigup.android.cloud.recommend;

import android.os.Environment;

public class PoxiaoConstants {

    // 统计队列1的消息码
    public static final int UPLOAD_STATISTIUS_MSG_QUEUE_1 = Short.MAX_VALUE;
    // 统计队列2的消息码
    public static final int UPLOAD_STATISTIUS_MSG_QUEUE_2 = Short.MAX_VALUE - 1;

    public final static String FIRST_USE = "first_use";

    // 保存服务端返回的图片地址KEY
    public static final String IMAGE_SERVER_KEY = "com.hifreshday.doudizhudemo.IMAGE_SERVER_KEY";

    public static final String POXIAO_ROOT_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/.poxiaogame/";

    // 斗地主的根目录
    public static final String DDZ_PATH = POXIAO_ROOT_DIR + "ddz/";

    // 扎金花的根目录
    public static final String ZJH_PATH = POXIAO_ROOT_DIR + "zjh/";

    // 牛牛的根目录
    public static final String NN_PATH = POXIAO_ROOT_DIR + "nn/";

    // 千变双扣的根目录
    public static final String QBSQ_PATH = POXIAO_ROOT_DIR + "qbsq/";
    // 新千变双扣的根目录
    public static final String XBQBSQ_PATH = POXIAO_ROOT_DIR + "xbqbsk/";

    // 麻将根目录
    public static final String MJ_PATH = POXIAO_ROOT_DIR + "mj/";

    // 斗地主的图片目录
    public static final String IMAGE_PATH = DDZ_PATH + "image/";
    // 提交到服务端的超时时间
    public final static int HANDLER_TIME_OUT = 15;

    // 支付取消广播ACTION
    public static final String PAY_CANCEL_ACTION = "com.hifreshday.PAY_CANCEL_ACTION";

    // 初始化音乐
    public static final String MUSIC_INIT = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_INIT";
    // 背景音乐禁止
    public static final String MUSIC_DISABLED = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_DISABLED";
    // 背景音乐启动
    public static final String MUSIC_ENABLED = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_ENABLED";
    // 背景音乐播放
    public static final String MUSIC_PLAY = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_PLAY";
    // 更新用户信息界面
    public static final String REFREASH_PLAYER_INFO = "com.hifreshday.doudizhudemo.DoudizhuConstants.REFREASH_PLAYER_INFO";
    // 显示女宠的定价界面
    public static final String MAKE_PRICES_SHOW = "com.hifreshday.doudizhudemo.DoudizhuConstants.MAKE_PRICES_SHOW";

    // 省电在配置文件中保存的KEY
    public static final String POWER_SAVING_SETTING_KEY = "com.hifreshday.doudizhudemo.activity.SettingActivity2.POWER_SAVING_KEY";
    // 背景音乐在配置文件中保存的KEY
    public static final String BACKGROUND_MUSIC_SETTING_KEY = "com.hifreshday.singlegobang.activity.SettingActivity2.BACKGROUND_MUSIC_CHECK_KEY";
    // 音效在配置文件中保存的KEY
    public static final String GAME_MUSIC_SETTING_KEY = "com.hifreshday.doudizhudemo.activity.SettingActivity2.GAME_MUSIC_CHECK_KEY";

    // 显示女宠界面中的离开部分
    public static final String FEMALE_PET_LEAVE = "com.hifreshday.doudizhudemo.DoudizhuConstants.FEMALE_PET_LEAVE";

    public static final String UPDATE_COMPOUND = "com.hifreshday.doudizhudemo.DoudizhuConstants.UPDATE_COMPOUND";

    // 合成前的选择合成道具提交ACTION
    public static final String PRE_COMPOUND_ACTION = "com.hifreshday.doudizhudemo.DoudizhuConstants.PRE_COMPOUND_ACTION";

    // 合成前的选择合成道具提交ACTION
    public static final String UPDATE_COMPOUND_PROP_LIST_ACTION = "com.hifreshday.doudizhudemo.DoudizhuConstants.UPDATE_COMPOUND_PROP_LIST_ACTION";

    // 销毁自己的广播
    public static final String ACTIVITY_DESTORY_SELF_ACTION = "com.hifreshday.doudizhudemo.DoudizhuConstants.ACTIVITY_DESTORY_SELF_ACTION";

    // 释放女宠切换锁
    public static final String PLAYER_PET_CHANGE_FLAG_ACTION = "com.hifreshday.doudizhudemo.DoudizhuConstants.PLAYER_PET_CHANGE_FLAG_ACTION";

    // 关闭修改女宠价格的ACTION
    public static final String PLAYER_PET_MAKEPRICE_FLAG_ACTION = "com.hifreshday.doudizhudemo.DoudizhuConstants.PLAYER_PET_MAKEPRICE_FLAG_ACTION";

    // 缠绵界面布局重置
    public static final String CHANMIAN_RESET_LAYOUT_ACTION = "com.hifreshday.doudizhudemo.DoudizhuConstants.CHANMIAN_RESET_LAYOUT_ACTION";
}
