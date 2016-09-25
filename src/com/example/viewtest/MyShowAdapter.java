package com.example.viewtest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyShowAdapter extends BaseAdapter {

	private Context context;
	private List<Data> mData;

	public MyShowAdapter(Context context) {
		this.context = context;
		this.mData = new ArrayList<Data>();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	//添加一个元素
    public void add(Data data) {
    	if (mData == null) {
            mData = new LinkedList();
        }
        mData.add(0, data);
        notifyDataSetChanged();
    }
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView info = null;
        //如果缓存convertView为空，则需要创建View
        if(convertView == null)
        {
        	info = new TextView(context);
        	
        	//R.layout.item对应item.xml布局文件
            //根据R.layout.item加载布局
        	convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
        	
        	//找到item.xml中textView1对应的TextView
            info = (TextView)convertView.findViewById(R.id.textView1);
            
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(info);
        }else{
        	info = (TextView)convertView.getTag();
        }

        info.setText(mData.get(position).getContent());
        
        return convertView;
	}

}

