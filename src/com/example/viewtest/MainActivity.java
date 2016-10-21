package com.example.viewtest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import java.util.List;

import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public  class MainActivity extends Activity  {

	private ListView show;
	private MyShowAdapter myadapter;
	private Timer myTimer = null;
	private TimerTask myTask = null;
	private String TAG="BattTest";
	private Data  sData;
	private OutputStream out = null;
	private OutputStream datafile = null;
	private double timer_cnt=0;
	private int wlock_flag = 0;
	private PowerManager.WakeLock wlock;
	private PowerManager.WakeLock wlock_lcd;
	
	private  Button startbtn;
	private  Button stopbtn;
	
	private String batt_info=null;
	private String file_batt_info=null;
	
	private final String BATT_SOC_PATH = "sys/class/power_supply/battery/capacity";
	private final String BATT_VOL_PATH = "sys/class/power_supply/battery/voltage_now";
	private final String BATT_CURRENT_PATH = "sys/class/power_supply/battery/current_now";
	private final String BATT_TEMP_PATH = "sys/class/power_supply/battery/temp";
	private final String USB_VOL_PATH = "sys/class/power_supply/usb/voltage_now";
	
	private final String CAT_BATT_SOC_PATH = "cat /sys/class/power_supply/battery/capacity";
	private final String CAT_BATT_VOL_PATH = "cat /sys/class/power_supply/battery/voltage_now";
	private final String CAT_BATT_CURRENT_PATH = "cat /sys/class/power_supply/battery/current_now";
	private final String CAT_BATT_TEMP_PATH = "cat /sys/class/power_supply/battery/temp";
	private final String CAT_USB_VOL_PATH = "cat /sys/class/power_supply/usb/voltage_now";
	
	private String cmdErrResult;
	private int batt_soc,batt_vol,batt_ma,batt_temp;
	private int usb_vol;
	private final boolean use_shell_cmd = true;
	
	final private int MENU_ABOUT = 1;
	final private int MENU_HELP = 2;
	
	private AlertDialog dialog_verinfo;
	
	private final String LOG_FILE_PATH="/sdcard/battery_data.txt";
	private final String LOG_DIR_PATH="/sdcard/batterytest/";
	private String LOG_DFILE_PATH;
	private String save_log_file;
	
	final private String VerInfo1 = "版本V1.1-2016/10/21\n" + "LogDir: /sdcard/batterytest/";
	final private String VerInfo2 = "版本V1.1-2016/10/21\n" + "LogDir: /sdcard/";
	private String VerInfo;
	
	//--------------------------------------------------------------------
	//Config  log保存目录
	//true: log保存到/sdcard/battery_data.txt
	//false: log保存到/sdcard/batterytest/batterydata-$(data).txt
	private boolean RootLogFile = false;
	
	//--------------------------------------------------------------------
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "BatteryTest start... ");
		
		//加载主界面布局activity_main.xml
		setContentView(R.layout.activity_main);
		
		//----------------------------------------------------------------
		//获取休眠锁
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wlock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "battery_monitor1");
		//获取禁止息屏锁
		PowerManager pm_lcd = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wlock_lcd = pm_lcd.newWakeLock( PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "battery_monitor2");
		
		//----------------------------------------------------------------
		//获取Adapter
		myadapter = new MyShowAdapter(this);
		//根据ID找到activity_main.xml中的ListView
		show = (ListView) findViewById(R.id.lv_show);
		//将ListView与Adapter关联
		show.setAdapter(myadapter);
		
		//----------------------------------------------------------------
		//判断log文件保存路径
		if(RootLogFile){
			save_log_file = LOG_FILE_PATH;
			VerInfo = VerInfo2;
		}else{
			createDir(LOG_DIR_PATH);
			LOG_DFILE_PATH = LOG_DIR_PATH + "battery_data-" + getFileDateTime() + ".txt";
			save_log_file = LOG_DFILE_PATH;
			VerInfo = VerInfo1;
		}
		Log.i(TAG, "LOG_DFILE_PATH: " + LOG_DFILE_PATH);
		writeFile2Sdcard(save_log_file, "time  batt_soc  batt_vol  batt_ma  batt_temp  vbus", false);
		
		//----------------------------------------------------------------
		//点击  关于 菜单对话框
		// 创建退出对话框  
		dialog_verinfo = new AlertDialog.Builder(this).create();  
        // 设置对话框标题  
		dialog_verinfo.setTitle("关于");  
        // 设置对话框消息  
		dialog_verinfo.setMessage(VerInfo);  
        // 添加选择按钮并注册监听  
		dialog_verinfo.setButton("确定", menu_listener);  
				
		//----------------------------------------------------------------
		//得到开始按钮实例
        startbtn = (Button)findViewById(R.id.btn_start);
        startbtn.setBackgroundColor(Color.parseColor("#669933"));
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(TAG, "Start Timer.");
            	startbtn.setBackgroundColor(Color.parseColor("#669933"));
            	stopbtn.setBackgroundColor(Color.parseColor("#666666"));
            	StartTimer();
            }
        });
        
        //得到停止按钮实例
        stopbtn = (Button)findViewById(R.id.btn_stop);
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(TAG, "Stop Timer.");
            	stopbtn.setBackgroundColor(Color.parseColor("#669933"));
            	startbtn.setBackgroundColor(Color.parseColor("#666666"));
            	StopTimer();
            }
        });
        
        //得到休眠按钮实例
        final Button wlockbtn = (Button)findViewById(R.id.btn_wlock);
        wlockbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(TAG, "wlock_flag: " + wlock_flag);
            	if(wlock_flag == 0){
            		wlock_flag = 1;
            		wlock.acquire();
            		wlock_lcd.acquire();
            		wlockbtn.setText("禁止休眠");
            	}else{
            		wlock_flag = 0;
            		wlock.release();
            		wlock_lcd.release();
            		wlockbtn.setText("允许休眠");
            		
            	}
            }
        });
        
		//----------------------------------------------------------------
		//启动定时器
		StartTimer();
        
	}//onCreate end
	
	//启动定时器
	private void StartTimer() {
		
		if(myTimer != null && myTask != null)
			return;
			
		if(myTimer == null)
			myTimer = new Timer();
		
		if(myTask == null){
		    myTask = new TimerTask(){
		    	@Override
		    	public void run(){
				
		    		//更小UI线程必须使用该方法
		    		runOnUiThread(new Runnable(){  
		    			@Override  
		    			public void run() {  
		    				timer_cnt += 10;
		    				
		    				//将读取的字符串数据转换为数值
		    				if(use_shell_cmd){
		    					
		    					cmdErrResult = ShellUtils.execCommand(CAT_BATT_SOC_PATH, false).errorMsg;
		    					if(cmdErrResult.length() == 0)
		    						batt_soc = Integer.parseInt(ShellUtils.execCommand(CAT_BATT_SOC_PATH, false).successMsg);
		    					
		    					cmdErrResult = ShellUtils.execCommand(CAT_BATT_VOL_PATH, false).errorMsg;
		    					if(cmdErrResult.length() == 0)
		    						batt_vol = Integer.parseInt(ShellUtils.execCommand(CAT_BATT_VOL_PATH, false).successMsg)/1000;
		    					
		    					cmdErrResult = ShellUtils.execCommand(CAT_BATT_CURRENT_PATH, false).errorMsg;
		    					if(cmdErrResult.length() == 0)
		    						batt_ma = Integer.parseInt(ShellUtils.execCommand(CAT_BATT_CURRENT_PATH, false).successMsg)/1000;
		    					
		    					cmdErrResult = ShellUtils.execCommand(CAT_BATT_TEMP_PATH, false).errorMsg;
		    					if(cmdErrResult.length() == 0)
		    						batt_temp = Integer.parseInt(ShellUtils.execCommand(CAT_BATT_TEMP_PATH, false).successMsg);
		    					
		    					cmdErrResult = ShellUtils.execCommand(CAT_USB_VOL_PATH, false).errorMsg;
		    					if(cmdErrResult.length() == 0)
		    					    usb_vol = Integer.parseInt(ShellUtils.execCommand(CAT_USB_VOL_PATH, false).successMsg)/1000;
		    				}else{
		    					batt_soc = Integer.parseInt(getProperty(BATT_SOC_PATH));
		    					batt_vol = Integer.parseInt(getProperty(BATT_VOL_PATH))/1000;
		    					batt_ma = Integer.parseInt(getProperty(BATT_CURRENT_PATH))/1000;
		    					batt_temp = Integer.parseInt(getProperty(BATT_TEMP_PATH));
		    					usb_vol = Integer.parseInt(getProperty(USB_VOL_PATH))/1000;
		    				}
		    				
		    				//格式化生成字符串
		    				//batt_info=String.format("%12d %s %12d %12d %12d %12d",timer_cnt, getFontDateTime(), batt_soc,batt_vol,batt_ma,batt_temp);
		    				batt_info = String.format("%s %8d %8d %8d %8d %8d",
		    						getFontDateTime(), batt_soc,batt_vol,batt_ma,batt_temp,usb_vol);
		    				
		    				Log.i(TAG, "batt_soc:" + batt_soc + " batt_vol:" + batt_vol + 
		    						" batt_ma:" + batt_ma + " batt_temp:" + batt_temp + " usb_vol:" + usb_vol);
		    				
		    				file_batt_info = String.format("%6.1f %8d %8d %8d %8d %8d",
		    						timer_cnt/60, batt_soc,batt_vol,batt_ma,batt_temp,usb_vol);
		    				//将数据写入到sdcard
		    				writeFile2Sdcard(save_log_file, file_batt_info, true);
		    				
		    				//将数据更新到手机listview界面显示
		    				sData = new Data(batt_info);
		    				myadapter.add(sData);
		    				myadapter.notifyDataSetChanged();  
		    			}
		    		}); 
		    	}
		    };
		}
		
		if(myTimer != null && myTask != null)
		    myTimer.schedule(myTask, 500,10*1000);
	}
	
	//停止定时器
	private void StopTimer() {

		timer_cnt = 0;
		
		if(myTimer != null){
    	    myTimer.cancel();
    	    myTimer = null;
		}
		
		if(myTask != null){
			myTask.cancel();
			myTask = null;
		}

	}
	
	 public boolean createDir(String destDirName) {
	        File dir = new File(destDirName);
	        if (dir.exists()) {
	        	Log.i(TAG,"Creat dir:" + destDirName + " it is already exist");
	            return false;
	        }
	        if (!destDirName.endsWith(File.separator)) {
	            destDirName = destDirName + File.separator;
	        }
	        //创建目录
	        if (dir.mkdirs()) {
	        	Log.i(TAG,"Creat dir:" + destDirName + " successful!");
	            return true;
	        } else {
	        	Log.i(TAG,"Creat dir:" + destDirName + " failed!");
	            return false;
	        }
	}
	 
	//执行shell命令函数
	public void exeCmd(String cmd){         
        try{  
        	 Log.i(TAG,"Do execmd: " + cmd);
             Process p = Runtime.getRuntime().exec(cmd);  
             BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream()));  
             //BufferedReader in = new BufferedReader( new InputStreamReader( p.getErrorStream())); 
             
             String line = null;    
             while ((line = in.readLine()) != null) {    
                Log.i(TAG,"Get: " + line);   
                myadapter.add(new Data("The line:" + line));
             }    
               
        }  catch(Throwable t)  {  
              t.printStackTrace();  
       }  
    }   
    
	//从设备节点获取状态参数
    private static String getProperty(String path) {
        String prop = "0";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            prop = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return prop;
    }
    
    //将数据写入到sd卡文件中
    private void writeFile2Sdcard(String filepath, String str, boolean append){
    	try{
    		FileWriter writer = null;
            
            writer = new FileWriter(filepath, append);     
            writer.write(str);
            writer.append("\n");
            writer.flush();
            writer.close();
            
    	} catch (IOException e){
    		e.printStackTrace();
    	} catch (Exception e){
    		e.printStackTrace();
    	} finally {
    		try{
    			if(out != null){
    				out.close();
    			}
    		}catch(IOException e){
    			e.printStackTrace();
    		}
    	}

    }
    
	public String getFontDateTime(){
		Calendar c = Calendar.getInstance();
		
		String year  = c.get(Calendar.YEAR) + "";
		String month = (c.get(Calendar.MONTH)+1) + "-";
		String date  = c.get(Calendar.DATE)+"/";
		String hour  = c.get(Calendar.HOUR_OF_DAY) + ":";
		String minute = c.get(Calendar.MINUTE) + ":";
		String second = c.get(Calendar.SECOND) + "";
		
		String dateTime = (month.length()==1 ? "0"+month : month)
				+ (date.length()==1 ? "0"+date : date)
				+ (hour.length()==1 ? "0"+hour : hour)
				+ (minute.length()==1 ? "0"+minute : minute)
				+ (second.length()==1 ? "0"+second : second);
		
		return dateTime;
	}
	
	public String getFileDateTime(){
		Calendar c = Calendar.getInstance();
		
		String year  = c.get(Calendar.YEAR) + "";
		String month = (c.get(Calendar.MONTH)+1) + "-";
		String date  = c.get(Calendar.DATE)+"_";
		String hour   = c.get(Calendar.HOUR_OF_DAY) + "-";
		String minute = c.get(Calendar.MINUTE) + "-";
		String second = c.get(Calendar.SECOND) + "";
		
		String dateTime = (month.length()==2 ? "0"+month : month)
				+ (date.length()==2 ? "0"+date : date)
				+ (hour.length()==2 ? "0"+hour : hour)
				+ (minute.length()==2 ? "0"+minute : minute)
				+ (second.length()==1 ? "0"+second : second);
		
		return dateTime;
	}
	
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // 创建退出对话框  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // 设置对话框标题  
            isExit.setTitle("系统提示");  
            // 设置对话框消息  
            isExit.setMessage("确定要退出吗");  
            // 添加选择按钮并注册监听  
            isExit.setButton("确定", listener);  
            isExit.setButton2("取消", listener);  
            // 显示对话框  
            isExit.show();  
        }  
        return false;  
    }  
	
    /**监听对话框里面的button点击事件*/  
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
    {  
        public void onClick(DialogInterface dialog, int which)  
        {  
            switch (which)  
            {  
            case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序  
                finish();  
                break;  
            case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框  
                break;  
            default:  
                break;  
            }  
        }  
    }; 
	
    /**监听对话框里面的button点击事件*/  
    DialogInterface.OnClickListener menu_listener = new DialogInterface.OnClickListener()  
    {  
        public void onClick(DialogInterface dialog, int which)  
        {  
            switch (which)  
            {  
            case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序  
            	dialog.dismiss();  
                break;    
            default:  
                break;  
            }  
        }  
    }; 
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	menu.add(1, MENU_ABOUT, 1, "关于");
    	menu.add(1, MENU_HELP, 2, "帮助");
    	
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case MENU_ABOUT:
            	Log.i(TAG, "click about");
            	//Toast.makeText(this, "haha,Insert Employee Successfully!", Toast.LENGTH_LONG).show();
            	dialog_verinfo.show();
                break;
            case MENU_HELP:
            	Log.i(TAG, "click help");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    //这里横竖屏切换是横屏和竖屏采用相同布局
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
        	//横屏布局
            setContentView(R.layout.activity_main);
            
        } else {
        	//竖屏布局
            setContentView(R.layout.activity_main);
        }
       
        super.onConfigurationChanged(newConfig);
    }
    
    //退出当前Activity时被调用,调用之后Activity就结束了  
    @Override  
    protected void onDestroy() {  
    	
		if(myTimer != null){
    	    myTimer.cancel();
    	    myTimer = null;
		}
		
		if(myTask != null){
			myTask.cancel();
			myTask = null;
		}
			
        Log.i(TAG, "onDestory called.");  
        
        super.onDestroy();  
    } 
    
}
