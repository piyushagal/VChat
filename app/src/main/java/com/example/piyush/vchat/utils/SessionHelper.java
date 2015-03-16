package com.example.piyush.vchat.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.piyush.vchat.ui.CallActivity;
import com.example.piyush.vchat.interfaces.CallSignalListener;
import com.example.piyush.vchat.interfaces.EndCallListener;
import com.example.piyush.vchat.interfaces.UserListChangeListener;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by piyush on 3/14/15.
 */
public class SessionHelper implements Session.SessionListener, Publisher.PublisherListener, SubscriberKit.VideoListener, Session.SignalListener {
    Session mSession;
    Publisher mPublisher;
    Subscriber mSubscriber;

    Context mContext;
    private RelativeLayout mSubscriberView;
    private LinearLayout mPublisherView;
    private ProgressBar mLoadingSub;
    private static SessionHelper instance = null;

    ArrayList<String> userList = new ArrayList<String>();

    UserListChangeListener mListener;
    private boolean isConnected;
    private boolean isInCall = false;

    private String myUserId = "";
    private Handler mHandler = new Handler();
    private CallSignalListener mCallSignalListener;
    private EndCallListener mEndCallListener;

    private SessionHelper(Context context) {
        mContext = context;
    }

    public static SessionHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SessionHelper(context);
        }
        return instance;
    }

    public void addSubscriberView(RelativeLayout subscriberView) {
        mSubscriberView = subscriberView;
    }

    public void addPublisherView(LinearLayout publisherView) {
        mPublisherView = publisherView;
    }

    public void addProgressBar(ProgressBar loadingPopup) {
        mLoadingSub = loadingPopup;
    }

    public void setUserListChangeListener(UserListChangeListener listener) {
        mListener = listener;
    }

    public void addUser(String userName) {
        if (mSession != null)
            mSession.sendSignal("addedUser", userName);
    }

    public void sendSignal(String type, String data) {
        if (mSession != null) {
            if (type.equals("call")) {
                data = data + " " + myUserId;
            } else if (type.equals("acceptCall")) {
                data = data + " " + myUserId;
            } else if (type.equals("endCall")) {
                mSession.unpublish(mPublisher);
                data = myUserId;
            }
            mSession.sendSignal(type, data);
        }
    }

    public int dpToPx(int dp) {
        double screenDensity = mContext.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    public void sessionConnect() {
        if (mSession == null) {
            mSession = new Session(mContext, OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID);
            mSession.setSessionListener(this);
            mSession.setSignalListener(this);
            mSession.connect(OpenTokConfig.TOKEN);
        }
    }

    public void sessionDisconnect() {
        if (mSession != null) {
            mSession.sendSignal("removedUser", myUserId);
            mSession.disconnect();
            mSession = null;
        }
    }

    public void publish() {
        mPublisher = new Publisher(mContext, myUserId);
        mPublisher.setPublisherListener(this);
        if (mPublisherView != null)
            attachPublisherView(mPublisher);
        if (mSession == null)
            sessionConnect();
        mSession.publish(mPublisher);
    }

    public void subscribeToStream(Stream stream) {
        Subscriber subscriber = new Subscriber(mContext, stream);
        mSubscriber = subscriber;
        subscriber.setVideoListener(this);
        mSession.subscribe(subscriber);
        if (subscriber.getSubscribeToVideo() && mLoadingSub != null) {
            // start loading spinning
            mLoadingSub.setVisibility(View.VISIBLE);
        }
    }

    public void attachSubscriberView(Subscriber subscriber) {
        mSubscriber = subscriber;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                mContext.getResources().getDisplayMetrics().widthPixels, mContext.getResources()
                .getDisplayMetrics().heightPixels);
        mSubscriberView.addView(mSubscriber.getView(), layoutParams);
        mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);

    }

    public void attachPublisherView(Publisher publisher) {
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                160, 120);
        publisher.getView().setLayoutParams(new LinearLayout.LayoutParams(160, 120));
        layoutParams.weight = 1.0f;
        mPublisherView.addView(publisher.getView());
    }

    @Override
    public void onConnected(Session session) {
        isConnected = true;
        addUser(myUserId);
    }


    @Override
    public void onDisconnected(Session session) {
        Log.i("VChat", "Called function onDisconnected");
        for (int i = 0; i < userList.size(); i++)
            userList.remove(i);
        isConnected = false;

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i("VChat", "Called function onStreamReceived ");
        if (mSubscriber == null)
            subscribeToStream(stream);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i("VChat", "Called function onStreamDropped");
        unsubscribeToStream(stream);

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i("VChat", "Called function onError");

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i("VChat", "Called function onStreamCreated");
        if (!publisherKit.getName().equals(myUserId)) {
            subscribeToStream(stream);
        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i("VChat", "Called function onStreamDestroyed");
        unsubscribeToStream(stream);
    }

    private void unsubscribeToStream(Stream stream) {
        if (mSubscriber!=null && mSubscriber.getStream().equals(stream)) {
            mSubscriberView.removeView(mSubscriber.getView());
            mSubscriber = null;
        }
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.i("VChat", "Called function onError2");

    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        Log.i("VChat", "Called function onVideoDataReceived");
        if (mLoadingSub != null)
            mLoadingSub.setVisibility(View.GONE);
        if(mSubscriberView!=null){
            attachSubscriberView(mSubscriber);
        }
    }

    public void reloadInterface() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    mSubscriberView.removeView(mSubscriber.getView());
                    attachSubscriberView(mSubscriber);
                }
            }
        }, 500);
    }

    public void onResume() {
        if (mSession != null) {
            mSession.onResume();
        }
        reloadInterface();
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
        Log.i("VChat", "Called function onVideoDisabled");

    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
        Log.i("VChat", "Called function onVideoEnabled");

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {
        Log.i("VChat", "Called function onVideoDisableWarning");

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {
        Log.i("VChat", "Called function onVideoDisableWarningLifted");

    }


    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        Log.i("VChat", "Signal Received");
        if (type.equals("addedUser") && !myUserId.equals(data) && !userList.contains(data)) {
            if (mListener != null)
                mListener.onUserAdded(data);
            mSession.sendSignal("onlineUser", myUserId + " " + data); //Notify new user of it's existence
        } else if (type.equals("call") && !isInCall) {
            String dest = data.split(" ")[0];
            String src = data.split(" ")[1];
            // Show Call Activity, if either it's incoming or outgoing call for user
            if (myUserId.equals(dest) || myUserId.equals(src)) {
                Intent intent = new Intent(mContext, CallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userID", data);
                intent.putExtra("calltype", !src.equals(myUserId));
                mContext.startActivity(intent);
                isInCall = true;
            }
        } else if (type.equals("call-multi")) {
            // Conference Call implementation
        } else if (type.equals("acceptCall")) {
            String accepted = data.split(" ")[0];
            if (accepted.equals("true") && !data.split(" ")[1].equals(myUserId))
                mCallSignalListener.onAccept();
            else
                mCallSignalListener.onReject();
            isInCall = accepted.equals("true");
        } else if (type.equals("endCall") && !data.equals(myUserId)) {
            mSession.unpublish(mPublisher);
            isInCall = false;
            mEndCallListener.endCall();
        } else if (type.equals("onlineUser") && data.split(" ")[1].equals(myUserId)) {
            if (mListener != null)
                mListener.onUserAdded(data.split(" ")[0]);
        } else if (type.equals("removedUser") && !data.equals(myUserId)) {
            mListener.onUserRemoved(data);
        }
    }

    public void setIsInCall(boolean inCall) {
        if (isInCall && !inCall)
            sendSignal("endCall", "true");
        isInCall = inCall;
    }

    public ArrayList<String> getUserList() {
        return userList;
    }

    public void setUser(String s) {
        myUserId = s;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setCallSignalListener(CallSignalListener listener) {
        mCallSignalListener = listener;
    }

    public void toggleVideoDevice() {
        mPublisher.setPublishVideo(!mPublisher.getPublishVideo());
    }


    public void toggleAudioDevice() {
        mPublisher.setPublishAudio(!mPublisher.getPublishAudio());
    }

    public void swapCamera() {
        mPublisher.swapCamera();
    }

    public void setEndCallListener(EndCallListener listener) {
        mEndCallListener = listener;
    }
}
