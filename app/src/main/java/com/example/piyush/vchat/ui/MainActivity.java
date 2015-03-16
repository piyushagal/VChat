package com.example.piyush.vchat.ui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.piyush.vchat.R;
import com.example.piyush.vchat.interfaces.EndCallListener;
import com.example.piyush.vchat.utils.SessionHelper;



public class MainActivity extends ActionBarActivity implements EndCallListener {


    RelativeLayout subscriberView;
    LinearLayout publisherView;
    ProgressBar mLoadingSub;
    RelativeLayout mainLayout;
    LinearLayout menuLayout;

    SessionHelper mSessionHelper;
    private boolean isAudioMute = false;
    private boolean isVideoMute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        subscriberView = (RelativeLayout) findViewById(R.id.subscriberview);
        publisherView = (LinearLayout) findViewById(R.id.publisherview);
        mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);
        mainLayout = (RelativeLayout) findViewById(R.id.mainlayout);
        menuLayout = (LinearLayout) findViewById(R.id.menuLayout);
        mainLayout.setOnClickListener(menuHideShowListener);

        mSessionHelper = SessionHelper.getInstance(this);
        mSessionHelper.addPublisherView(publisherView);
        mSessionHelper.addSubscriberView(subscriberView);
        mSessionHelper.addProgressBar(mLoadingSub);

        mSessionHelper.setEndCallListener(this);

        mSessionHelper.publish();
        handle.sendEmptyMessageDelayed(0, 5000);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().hide();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        mSessionHelper.setIsInCall(false);
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSessionHelper.onResume();

    }

    public void handleButton(View view) {
        int id = view.getId();
        if(handle.hasMessages(0))
            handle.removeMessages(0);
        handle.sendEmptyMessageDelayed(0, 5000);
        switch (id) {
            case R.id.swapCamera:
                mSessionHelper.swapCamera();
                break;
            case R.id.endCall:
                mSessionHelper.sendSignal("endCall", "true");
                endCall();
                break;
            case R.id.toggleMute:
                mSessionHelper.toggleAudioDevice();
                isAudioMute = !isAudioMute;
                if(isAudioMute)
                    ((ImageButton) view).setImageResource(R.drawable.mute_pub);
                else
                    ((ImageButton) view).setImageResource(R.drawable.unmute_pub);
                break;
            case R.id.toggleVideo:
                mSessionHelper.toggleVideoDevice();
                isVideoMute = !isVideoMute;
                if(isVideoMute)
                    ((ImageButton) view).setImageResource(R.drawable.no_video);
                else
                    ((ImageButton) view).setImageResource(R.drawable.video);
                break;
        }
    }

    @Override
    public void endCall() {
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mSessionHelper.setIsInCall(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void hideMenu(){
        float value = mSessionHelper.dpToPx(R.dimen.in_call_image_button_height);
        menuLayout.animate().translationY(value).setDuration(1000);
        isMenuShown = false;
    }

    Handler handle = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message inputMessage){
            if(isMenuShown)
                hideMenu();
        }
    };

    private boolean isMenuShown = true;
    View.OnClickListener menuHideShowListener  = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
             if(isMenuShown){
                hideMenu();
             }else{
                 menuLayout.animate().translationY(0)
                                     .setDuration(1000);
                 if(handle.hasMessages(0))
                    handle.removeMessages(0);
                 handle.sendEmptyMessageDelayed(0, 5000);
                 isMenuShown = true;
             }

        }

    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

}
