package com.example.lx.justmap;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by lx on 2016/10/16.
 */
public class FriendLab {
    private static final String TAG = "FriendLab";
    private static final String FILENAME = "friendsTest3.json";

    private ArrayList<Friend> mFriends;
    private FriendJSONSerializer mSerializer;

    private static FriendLab sFriendLab;
    private Context mAppContext;

    public FriendLab(Context appContext) {
        mAppContext = appContext;

        mSerializer = new FriendJSONSerializer(mAppContext, FILENAME);

        try {
            mFriends = mSerializer.loadFriends();
        } catch (Exception e) {
            mFriends = new ArrayList<Friend>();
            Log.e(TAG, "Error saving friends: ", e);
        }

        if (mFriends.size() == 0) initFriends();
    }

    private void initFriends() {

        Friend f = new Friend();
        f.setName("张三");
        f.setPhoneNumber("13138103075");
        f.setLatitude("22.266688");
        f.setLongtitude("113.542494");

        mFriends.add(f);
//
//        Friend f2 = new Friend();
//        f2.setName("李四");
//        f2.setPhoneNumber("15820577916");
//        f2.setLatitude("22.266697");
//        f2.setLongtitude("113.532483");
//
//        mFriends.add(f2);
//
//        Friend f3 = new Friend();
//        f3.setName("王五");
//        f3.setPhoneNumber("15820575879");
//        f3.setLatitude("22.276676");
//        f3.setLongtitude("113.542484");
//
//        mFriends.add(f3);
//
//        Friend f4 = new Friend();
//        f4.setName("张三");
//        f4.setPhoneNumber("10086");
//        f4.setLatitude("22.226678");
//        f4.setLongtitude("113.522483");
//
//        mFriends.add(f4);
    }

    public ArrayList<Friend> getFriends() {
        return mFriends;
    }

    public static FriendLab getFriendLab(Context c) {
        if (sFriendLab == null)
            sFriendLab = new FriendLab(c.getApplicationContext());
        return sFriendLab;
    }

    public void deleteFriend (Friend f) {
        mFriends.remove(f);
    }


    public Context getAppContext() {
        return mAppContext;
    }

    public Friend getFriend(UUID id) {
        for (Friend f : mFriends) {
            if (f.getId().equals(id))
                return f;
        }
        return null;
    }

    public Friend getFriend(String phoneNumber) {
        for (Friend f : mFriends) {
            if (f.getPhoneNumber().equals(phoneNumber))
                return f;
        }
        return null;
    }

    public boolean savaFriends() {
        try {
            mSerializer.saveFriends(mFriends);
            Log.d(TAG, "diaries saved to file");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving friends: ", e);
            return false;
        }
    }
}
