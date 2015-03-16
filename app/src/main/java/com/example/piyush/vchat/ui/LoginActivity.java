package com.example.piyush.vchat.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.piyush.vchat.R;
import com.example.piyush.vchat.utils.SessionHelper;


public class LoginActivity extends ActionBarActivity {

    Button mLogin;
    EditText mUser;

    SessionHelper mSessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUser = (EditText)findViewById(R.id.editText);
        mLogin = (Button)findViewById(R.id.button);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
                intent.putExtra("userName", mUser.getText().toString());
                startActivity(intent);
                mSessionHelper = SessionHelper.getInstance(getApplicationContext());
                mSessionHelper.setUser(mUser.getText().toString());
                mSessionHelper.sessionConnect();

            }
        });
    }
}
