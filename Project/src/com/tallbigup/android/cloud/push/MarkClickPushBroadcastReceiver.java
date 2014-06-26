package com.tallbigup.android.cloud.push;

import com.tallbigup.android.cloud.TbuCloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MarkClickPushBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("MCH","用户点击了通知");
		
//		TbuCloud.markPersonInfo(context, "click_push", "");
//		Intent dopush = intent.getParcelableExtra("dopush");
//		context.startActivity(dopush);
	}

}
