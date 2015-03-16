package com.example.piyush.vchat.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.example.piyush.vchat.R;
import com.example.piyush.vchat.interfaces.CallSignalListener;
import com.example.piyush.vchat.utils.SessionHelper;

import java.io.IOException;


public class CallActivity extends Activity implements CallSignalListener {

    private Button mAcceptButton;
    private Button mRejectButton;
    private TextView mUserID;
    private boolean isInComing = true;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call);
        getActionBar().hide();

        mAcceptButton = (Button) findViewById(R.id.accept);
        mRejectButton = (Button) findViewById(R.id.reject);
        SessionHelper.getInstance(this).setCallSignalListener(this);
        mPlayer = new MediaPlayer();
        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SessionHelper.getInstance(getApplicationContext()).sendSignal("acceptCall", "false");
                onReject();
            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionHelper.getInstance(getApplicationContext()).sendSignal("acceptCall", "true");
                onAccept();
            }
        });


        mUserID = (TextView) findViewById(R.id.userID);

        Intent intent = getIntent();
        isInComing = intent.getBooleanExtra("calltype", true);
        String data = intent.getStringExtra("userID");
        AssetFileDescriptor afd = null;

        try {
            if (!isInComing) {
                mAcceptButton.setVisibility(View.GONE);
                mRejectButton.setText("Cancel");
                mUserID.setText("Calling " + data.split(" ")[0] + " ...");
                afd = getAssets().openFd("outgoing.mp3");
            } else {
                mUserID.setText(data.split(" ")[1] + " Calling ...");
                afd = getAssets().openFd("ringtone.mp3");
            }

            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mPlayer.setLooping(true);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onAccept() {
        mPlayer.stop();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onReject() {
        mPlayer.stop();
        finish();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        SessionHelper.getInstance(getApplicationContext()).sendSignal("acceptCall", "false");
        onReject();
    }
}
