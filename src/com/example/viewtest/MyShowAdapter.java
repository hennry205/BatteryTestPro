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

	//���һ��Ԫ��
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
        //�������convertViewΪ�գ�����Ҫ����View
        if(convertView == null)
        {
        	info = new TextView(context);
        	
        	//R.layout.item��Ӧitem.xml�����ļ�
            //����R.layout.item���ز���
        	convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
        	
        	//�ҵ�item.xml��textView1��Ӧ��TextView
            info = (TextView)convertView.findViewById(R.id.textView1);
            
            //�����úõĲ��ֱ��浽�����У�������������Tag��Ա���淽��ȡ��Tag
            convertView.setTag(info);
        }else{
        	info = (TextView)convertView.getTag();
        }

        info.setText(mData.get(position).getContent());
        
        return convertView;
	}

}

