package com.example.piyush.vchat.interfaces;

/**
 * Created by piyush on 3/14/15.
 */
public interface UserListChangeListener {
    public void onUserAdded(String userName);
    public void onUserRemoved(String userName);
}
