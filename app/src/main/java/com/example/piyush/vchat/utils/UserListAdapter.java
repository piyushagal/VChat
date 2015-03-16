package com.example.piyush.vchat.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.piyush.vchat.R;

/**
 * Created by piyush on 3/15/15.
 */
public class UserListAdapter extends BaseAdapter {

    private Context mContext;
    SessionHelper mSessionHelper;

    public UserListAdapter(Context context){
        mContext = context;
        mSessionHelper = SessionHelper.getInstance(context);
    }
    @Override
    public int getCount() {
        return mSessionHelper.getUserList().size();
    }

    @Override
    public String getItem(int i) {
        return mSessionHelper.getUserList().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
       View row = view;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.user_list_row, null, false);
        }
        TextView tv = (TextView)row.findViewById(R.id.textView1);
        tv.setText(getItem(i));
        return row;
    }

    public void addUser(String user){
        mSessionHelper.getUserList().add(user);
        notifyDataSetChanged();
    }

    public void removeUser(String userName) {
        mSessionHelper.getUserList().remove(userName);
        notifyDataSetChanged();
    }
}
