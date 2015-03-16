package com.example.piyush.vchat.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.piyush.vchat.R;
import com.example.piyush.vchat.interfaces.UserListChangeListener;
import com.example.piyush.vchat.utils.SessionHelper;
import com.example.piyush.vchat.utils.UserListAdapter;

import java.util.ArrayList;


public class UserListActivity extends ActionBarActivity implements UserListChangeListener {
    ArrayList<String> users;
    ListView mList;
    UserListAdapter mAdapter;
    SessionHelper mSessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        SessionHelper.getInstance(this).setUserListChangeListener(this);
        mSessionHelper = SessionHelper.getInstance(this);
        users = mSessionHelper.getUserList();

        mList = (ListView)findViewById(R.id.userListView);
        mAdapter = new UserListAdapter(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                callAndPublish(users.get(i));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar ab =getSupportActionBar();
        ab.setTitle(getIntent().getStringExtra("userName").toUpperCase());
        return super.onCreateOptionsMenu(menu);
    }


    private void callAndPublish(String s) {
        mSessionHelper.sendSignal("call", s);
    }


    @Override
    public void onUserAdded(String userName) {
        mAdapter.addUser(userName);
    }

    @Override
    public void onUserRemoved(String userName) {
         mAdapter.removeUser(userName);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!mSessionHelper.isConnected())
            mSessionHelper.sessionConnect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mSessionHelper!=null){
            mSessionHelper.sessionDisconnect();
        }
    }
}
