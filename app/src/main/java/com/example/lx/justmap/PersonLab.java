package com.example.lx.justmap;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by lx on 2016/10/16.
 */
public class PersonLab {
    private static final String TAG = "PersonLab";
    private static final String FILENAME1 = "friendsTest6.json";
    private static final String FILENAME2 = "enemiesTest6.json";

    private ArrayList<Person> mFriends;
    private ArrayList<Person> mEnemies;
    private PersonJSONSerializer mSerializer;

    private static PersonLab sPersonLab;
    private Context mAppContext;

    public PersonLab(Context appContext) {
        mAppContext = appContext;

        mSerializer = new PersonJSONSerializer(mAppContext, FILENAME1);
        mSerializer = new PersonJSONSerializer(mAppContext, FILENAME2);

        try {
            mFriends = mSerializer.loadFriends();
        } catch (Exception e) {
            mFriends = new ArrayList<Person>();
            Log.e(TAG, "Error loading friends: ", e);
        }
        try {
            mEnemies = mSerializer.loadFriends();
        } catch (Exception e) {
            mEnemies = new ArrayList<Person>();
            Log.e(TAG, "Error loading enemies: ", e);
        }

        if (mFriends.size() == 0) initFriends();
        if (mEnemies.size() == 0) initEnemies();
    }

    private void initFriends() {

        Person f = new Person();
        f.setName("张三");
        f.setPhoneNumber("13138103075");
        f.setLatitude("22.266688");
        f.setLongtitude("113.532494");

        mFriends.add(f);

        Person f2 = new Person();
        f2.setName("李四");
        f2.setPhoneNumber("15820577916");
        f2.setLatitude("22.266697");
        f2.setLongtitude("113.522483");

        mFriends.add(f2);

        Person f3 = new Person();
        f3.setName("王五");
        f3.setPhoneNumber("15820575879");
        f3.setLatitude("22.265676");
        f3.setLongtitude("113.536584");

        mFriends.add(f3);

        Person f4 = new Person();
        f4.setName("张三");
        f4.setPhoneNumber("10086");
        f4.setLatitude("22.226678");
        f4.setLongtitude("113.532483");

        mFriends.add(f4);
    }

    private void initEnemies() {

        Person f = new Person();
        f.setName("路三");
        f.setPhoneNumber("13138103075");
        f.setLatitude("22.266688");
        f.setLongtitude("113.542494");

        mEnemies.add(f);

        Person f2 = new Person();
        f2.setName("方四");
        f2.setPhoneNumber("13138103075");
        f2.setLatitude("22.276697");
        f2.setLongtitude("113.532483");

        mEnemies.add(f2);

        Person f3 = new Person();
        f3.setName("王五");
        f3.setPhoneNumber("15820575879");
        f3.setLatitude("22.276676");
        f3.setLongtitude("113.542484");

        mEnemies.add(f3);

    }

    public ArrayList<Person> getFriends() {
        return mFriends;
    }
    public ArrayList<Person> getEnemies() {
        return mEnemies;
    }

    public static PersonLab get(Context c) {
        if (sPersonLab == null)
            sPersonLab = new PersonLab(c.getApplicationContext());
        return sPersonLab;
    }

    public void deleteFriend (Person f) {
        mFriends.remove(f);
    }
    public void deleteEnemy (Person f) {
        mEnemies.remove(f);
    }



    public Person getFriend(UUID id) {
        for (Person f : mFriends) {
            if (f.getId().equals(id))
                return f;
        }
        return null;
    }
    public Person getEnemy(UUID id) {
        for (Person e : mEnemies) {
            if (e.getId().equals(id))
                return e;
        }
        return null;
    }

    public Person getFriend(String phoneNumber) {
        for (Person f : mFriends) {
            if (f.getPhoneNumber().equals(phoneNumber))
                return f;
        }
        return null;
    }
    public Person getEnemy(String phoneNumber) {
        for (Person e : mEnemies) {
            if (e.getPhoneNumber().equals(phoneNumber))
                return e;
        }
        return null;
    }

    public boolean savaLab() {
        try {
            mSerializer.saveFriends(mFriends);
            mSerializer.saveFriends(mEnemies);
            Log.d(TAG, "diaries saved to file");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving friends: ", e);
            return false;
        }
    }
}
