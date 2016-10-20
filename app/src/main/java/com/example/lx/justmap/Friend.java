package com.example.lx.justmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by lx on 2016/10/16.
 */
public class Friend implements Serializable
{

    private static final long serialVersionUID = -758459502806858414L;
    private static final String JSON_ID = "ID";
    private static final String JSON_PHONENUMNER = "PhoneNumber";
    private static final String JSON_NAME = "Name";
    private static final String JSON_LATITUDE = "Latitude";
    private static final String JSON_LONGTITUDE = "Longtitude";
//    private static final String JSON_ALTITUDE = "Altitude";
//    private static final String JSON_ACCURACY = "Accuracy";
//    private static final String JSON_NEARADDRESS = "NearestAddress";
//    private static final String JSON_SSINCEUPDATE = "SSinceUpdate";
//    private static final String JSON_SUTILUPDATE = "SUtilUpdate";
    private UUID mId;

    private String mPhoneNumber;
    private String mName;

    private String mLatitude;
    private String mLongtitude;
    private String mAltitude;
    private String mAccuracy;
    private String mNearestAddress;

    private String mSSinceUpdate;
    private String mSUtilUpdate;

    public Friend() {
        mId = UUID.randomUUID();
    }

    public UUID getId() {
        return mId;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLongtitude() {
        return mLongtitude;
    }

    public void setLongtitude(String longtitude) {
        mLongtitude = longtitude;
    }

    public String getAltitude() {
        return mAltitude;
    }

    public void setAltitude(String altitude) {
        mAltitude = altitude;
    }

    public String getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(String accuracy) {
        mAccuracy = accuracy;
    }

    public String getNearestAddress() {
        return mNearestAddress;
    }

    public void setNearestAddress(String nearestAddress) {
        mNearestAddress = nearestAddress;
    }

    public String getSSinceUpdate() {
        return mSSinceUpdate;
    }

    public void setSSinceUpdate(String SSinceUpdate) {
        mSSinceUpdate = SSinceUpdate;
    }

    public String getSUtilUpdate() {
        return mSUtilUpdate;
    }

    public void setSUtilUpdate(String SUtilUpdate) {
        mSUtilUpdate = SUtilUpdate;
    }

    @Override
    public String toString() {
        return mName.toString();
    }

    public Friend(JSONObject json) throws JSONException {
        mId = UUID.fromString(json.getString(JSON_ID));
        mName = json.getString(JSON_NAME);
        mPhoneNumber = json.getString(JSON_PHONENUMNER);

        if (json.has(JSON_LATITUDE))
            mLatitude = json.getString(JSON_LATITUDE);
        if (json.has(JSON_LONGTITUDE))
            mLongtitude = json.getString(JSON_LONGTITUDE);
//        if (json.has(JSON_ALTITUDE))
//            mAltitude = json.getDouble(JSON_ALTITUDE);
//        if (json.has(JSON_ACCURACY))
//            mAccuracy = json.getDouble(JSON_ACCURACY);
//        if (json.has(JSON_NEARADDRESS))
//            mNearestAddress = json.getString(JSON_NEARADDRESS);
//
//        if (json.has(JSON_SSINCEUPDATE))
//            mSSinceUpdate = json.getDouble(JSON_SSINCEUPDATE);
//        if (json.has(JSON_SUTILUPDATE))
//            mSUtilUpdate = json.getDouble(JSON_SUTILUPDATE);
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_ID, mId.toString());
        json.put(JSON_PHONENUMNER, mPhoneNumber);
        json.put(JSON_NAME, mName);

        json.put(JSON_LATITUDE, mLatitude);
        json.put(JSON_LONGTITUDE, mLongtitude);
//        json.put(JSON_ALTITUDE, mAltitude);
//        json.put(JSON_ACCURACY, mAccuracy);
//        json.put(JSON_NEARADDRESS, mNearestAddress.toString());
//
//        json.put(JSON_SSINCEUPDATE, mSSinceUpdate);
//        json.put(JSON_SUTILUPDATE, mSUtilUpdate);

        return json;
    }
}
