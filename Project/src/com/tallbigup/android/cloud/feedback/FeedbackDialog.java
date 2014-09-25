package com.tallbigup.android.cloud.feedback;

import com.tallbigup.android.cloud.LayoutUtil;
import com.tallbigup.android.cloud.TbuCloud;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class FeedbackDialog extends Dialog{
	
	private Activity activity;

	private EditText feedbackAdvice;
	private ImageButton feedbackCancelBtn;
	private ImageButton feedbackConfirmBtn;
	
	private String playerId;
	
	public FeedbackDialog(Activity activity,String playerId) {
		super(activity,LayoutUtil.getMoreGamedialogStyleResId());
		this.playerId = playerId;
		this.activity = activity;
	}

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(LayoutUtil.getFeedbackLayoutResId());
		initView();
	}
	
	private void initView(){
		feedbackAdvice = (EditText)findViewById(LayoutUtil.getFeedbackAdviceResId());
		
		feedbackConfirmBtn = (ImageButton)findViewById(LayoutUtil.getFeedbackConfirmStateResId());
		feedbackConfirmBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(feedbackAdvice.getText().toString().length() > 50){
					Toast.makeText(activity, "输入内容超过50字符", Toast.LENGTH_LONG).show();
					return;
				}else if(feedbackAdvice.getText().toString().length() == 0){
					Toast.makeText(activity, "输入内容不能为空", Toast.LENGTH_LONG).show();
					return;
				}
				dismiss();
				TbuCloud.setFeedback(playerId, feedbackAdvice.getText().toString());
			}
		});
		
		feedbackCancelBtn = (ImageButton)findViewById(LayoutUtil.getFeedbackCloseStateResId());
		feedbackCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		dismiss();
		super.onBackPressed();
	}
	
}
