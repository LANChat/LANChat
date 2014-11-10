package com.lanchat.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.view.Window;
import android.widget.TextView;

public class LoadingActivity extends BasicActivity {
	private TimeCount time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		time = new TimeCount(3000, 1000);
		time.start();
		// time.onFinish();
	}

	@Override
	public void onDestroy() {
		time.cancel();
		super.onDestroy();
	}

	/* 定义一个倒计时的内部类 */
	class TimeCount extends CountDownTimer {

		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			startActivity(new Intent(getApplication(), InitActivity.class));
			LoadingActivity.this.finish();
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
		}
	}

	@Override
	public void processMessage(Message msg) {
		// TODO Auto-generated method stub

	}

}
