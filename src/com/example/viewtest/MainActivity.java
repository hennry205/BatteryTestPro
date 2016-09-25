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
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import java.util.List;

import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public  class MainActivity extends Activity  {

	private ListView show;
	private MyShowAdapter myadapter;
	private Timer myTimer = null;
	private TimerTask myTask = null;
	private String TAG="MyVT";
	private Data  sData;
	private OutputStream out = null;
	private OutputStream datafile = null;
	private long timer_cnt=0;
	private String filepath="/sdcard/battery_data.txt";
	private int wlock_flag = 0;
	private PowerManager.WakeLock wlock;
	
	private String batt_info=null;
	
	private String BATT_SOC_PATH = "sys/class/power_supply/battery/capacity";
	private String BATT_VOL_PATH = "sys/class/power_supply/battery/voltage_now";
	private String BATT_CURRENT_PATH = "sys/class/power_supply/battery/current_now";
	private String BATT_TEMP_PATH = "sys/class/power_supply/battery/temp";
	private int batt_soc,batt_vol,batt_ma,batt_temp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//���������沼��activity_main.xml
		setContentView(R.layout.activity_main);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wlock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "battery_monitor");
		
		//��ȡAdapter
		myadapter = new MyShowAdapter(this);
		
		//����ID�ҵ�activity_main.xml�е�ListView
		show = (ListView) findViewById(R.id.lv_show);
		//��ListView��Adapter����
		show.setAdapter(myadapter);
		
		writeFile2Sdcard("index batt_soc  batt_vol  batt_ma  batt_temp", false);
		//������ʱ��
		StartTimer();
				
		//�õ���ťʵ��
        Button startbtn = (Button)findViewById(R.id.btn_start);
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(TAG, "Start Timer.");
            	StartTimer();
            }
        });
        
      //�õ���ťʵ��
        Button stopbtn = (Button)findViewById(R.id.btn_stop);
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(TAG, "Stop Timer.");
            	StopTimer();
            }
        });
        
      //�õ���ťʵ��
        final Button wlockbtn = (Button)findViewById(R.id.btn_wlock);
        wlockbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(TAG, "Stop Timer.");
            	if(wlock_flag == 0){
            		wlock_flag = 1;
            		wlock.acquire();
            		wlockbtn.setText("��ֹ����");
            	}else{
            		wlock_flag = 0;
            		wlock.release();
            		wlockbtn.setText("��������");
            		
            	}
            }
        });
        
        
	}//onCreate
	
	//������ʱ��
	private void StartTimer() {
		
		if(myTimer != null && myTask != null)
			return;
			
		if(myTimer == null)
			myTimer = new Timer();
		
		if(myTask == null){
		    myTask = new TimerTask(){
		    	@Override
		    	public void run(){
				
		    		//��СUI�̱߳���ʹ�ø÷���
		    		runOnUiThread(new Runnable(){  
		    			@Override  
		    			public void run() {  
		    				timer_cnt++;
		    				
		    				//����ȡ���ַ�������ת��Ϊ��ֵ
		    				batt_soc = Integer.parseInt(getProperty(BATT_SOC_PATH));
		    				batt_vol = Integer.parseInt(getProperty(BATT_VOL_PATH))/1000;
		    				batt_ma = Integer.parseInt(getProperty(BATT_CURRENT_PATH))/1000;
		    				batt_temp = Integer.parseInt(getProperty(BATT_TEMP_PATH));
		    				
		    				//��ʽ�������ַ���
		    				batt_info=String.format("%12d %12d %12d %12d %12d",
		    						timer_cnt, batt_soc,batt_vol,batt_ma,batt_temp);
		    				
		    				Log.i(TAG, "batt_soc:" + batt_soc + " batt_vol:" + batt_vol + " batt_ma:" + batt_ma + " batt_temp:" + batt_temp);
		    				
		    				//������д�뵽sdcard
		    				writeFile2Sdcard(batt_info, true);
		    				
		    				//�����ݸ��µ��ֻ�listview������ʾ
		    				sData = new Data(batt_info);
		    				myadapter.add(sData);
		    				myadapter.notifyDataSetChanged();  
		    			}
		    		}); 
		    	}
		    };
		}
		
		if(myTimer != null && myTask != null)
		    myTimer.schedule(myTask,200,3*1000);
	}
	
	//ֹͣ��ʱ��
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
	
	//ִ��shell�����
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
    
	//���豸�ڵ��ȡ״̬����
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
    
    //������д�뵽sd���ļ���
    private void writeFile2Sdcard(String str, boolean append){
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
    
    //����������л��Ǻ���������������ͬ����
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
        	//��������
            setContentView(R.layout.activity_main);
            
        } else {
        	//��������
            setContentView(R.layout.activity_main);
        }
       
        super.onConfigurationChanged(newConfig);
    }
    
    //�˳���ǰActivityʱ������,����֮��Activity�ͽ�����  
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
