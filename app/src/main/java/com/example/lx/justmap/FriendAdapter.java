package com.example.lx.justmap;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lx on 2016/10/18.
 */
public class FriendAdapter extends BaseAdapter {
    private List<Friend> mData;       //创建Diary类型的List表
    private LayoutInflater mInflater;               //定义线性布局过滤器

    private ListItemClickHelp callback;

    public interface ListItemClickHelp {
        void onClick(int position, int index);
    }

    public FriendAdapter(Context context , List<Friend> data, ListItemClickHelp callback){
        this.mData = data ;
        mInflater = LayoutInflater.from(context);       //获取布局
        this.callback = callback;
    }

    /**
     * 得到列表长度
     * @return
     */
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;    //得到子项位置id
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int p = position;
        if (convertView == null) {
            //通过LayoutInflater实例化布局
            convertView = mInflater.inflate(R.layout.friends_list_item, null);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onClick(p, 1);
            }
        });

        TextView nameTextView = (TextView)convertView.findViewById(R.id.name_cell);
        nameTextView.setText(mData.get(position).getName());

        Button deleteButton;
        deleteButton = (Button)convertView.findViewById(R.id.delete_button_cell);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onClick(p, 0);
            }
        });
        return convertView;
    }
}

