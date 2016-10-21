package com.example.viewtest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BatteryTestService extends Service {

	public static final String TAG = "BattTest";

	private MyBinder mBinder = new MyBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		
		Notification notification = new Notification(R.drawable.ic_launcher, "有通知到来", System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, "BatteryTest", "正在运行",	pendingIntent);
		startForeground(1, notification);
		
		Log.i(TAG, "onCreate() executed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand() executed");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy() executed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (IBinder) mBinder;
	}

	class MyBinder extends Binder {

		public void startDownload() {
			Log.i("TAG", "startDownload() executed");

		}

	}

}