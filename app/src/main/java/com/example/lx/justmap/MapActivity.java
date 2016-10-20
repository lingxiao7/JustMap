package com.example.lx.justmap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE;
import static android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE;
import static android.telephony.SmsManager.RESULT_ERROR_NULL_PDU;
import static android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF;

public class MapActivity extends Activity {
    private  String SMS_SEND_ACTION = "SMS_SEND";
    private  String SMS_DELIVERED_ACTION ="SMS_DELIVERED";
    MapView mMapView = null;
    private BaiduMap mBaiduMap;

    // 定位相关
    public LocationClient mLocationClient;
    public MyLocationListener mLocationListener;

    private boolean isFirstIn = true;
    private boolean isShowMaker = false;
    private boolean isRefresh = false;
    private double mLatitude;
    private double mLongtitude;

    private ArrayList<Friend> mFriends;
    private Friend mMy;

    private Button mFriendsButton;
    private Button mEnemiesButton;
    private ToggleButton mRefreshToggleButton;


    private IntentFilter receiveFilter;
    private MessageReceiver messageReceiver;
    private SmsStatusReceiver mSmsStatusReceiver;
    private SmsDeliveryStatusReceiver mSmsDeliveryStatusReceiver;
    private String mAddressString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        initFriends();
        initView();

        mMy = new Friend();
        // 定位
        initLocation();

        mFriendsButton = (Button)findViewById(R.id.btn_friends);
        mFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Activity
                Intent i = new Intent(MapActivity.this, FriendsActivity.class);
                startActivity(i);
            }
        });

        mRefreshToggleButton = (ToggleButton)findViewById(R.id.btn_refresh);
        mRefreshToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 当按钮第一次被点击时候响应的事件
                if (mRefreshToggleButton.isChecked()) {
                    mBaiduMap.clear();
                    refreshOverlays();
                    isRefresh = true;
                }
                // 当按钮再次被点击时候响应的事件
                else {
                    //
                    isRefresh = false;
                }
            }
        });


    }

    private void initFriends() {
        mFriends = FriendLab.getFriendLab(this).getFriends();
    }

    private void initLocation(){
        mLocationClient = new LocationClient(this);     //声明LocationClient类
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener( mLocationListener );    //注册监听函数

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mLocationClient.setLocOption(option);
    }

    private void initView() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);

        //添加marker点击事件的监听
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //从marker中获取info信息
                Bundle bundle = marker.getExtraInfo();
                Friend f =  (Friend) bundle.getSerializable("info");

                String str = f.getName() + " " + f.getPhoneNumber() + "\n" + f.getLatitude() + " " + f.getLongtitude();


                Toast.makeText(getApplicationContext(), str.toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_friends);
        Marker marker;
        isShowMaker = true;

        for (Friend f : mFriends) {
            if (f.getLatitude() != null) {


                double latitude = Double.valueOf(f.getLatitude());
                double longtitude = Double.valueOf(f.getLongtitude());

                //定义Maker坐标点
                LatLng point = new LatLng(latitude, longtitude);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .zIndex(9); // 设置marker所在层级;
                //在地图上添加Marker，并显示
                marker = (Marker) mBaiduMap.addOverlay(option);

                //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
                Bundle bundle = new Bundle();
                //info必须实现序列化接口
                bundle.putSerializable("info", f);
                marker.setExtraInfo(bundle);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted())
            mLocationClient.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        mSmsStatusReceiver = new SmsStatusReceiver();
        registerReceiver(mSmsStatusReceiver,new IntentFilter(SMS_SEND_ACTION));

        mSmsDeliveryStatusReceiver = new SmsDeliveryStatusReceiver();
        registerReceiver(mSmsDeliveryStatusReceiver,new IntentFilter(SMS_DELIVERED_ACTION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_map_common :
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.id_map_site :
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.id_map_traffic :
                if (mBaiduMap.isTrafficEnabled()) {
                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle(R.string.menu_map_traffic_off);
                } else {
                    mBaiduMap.setTrafficEnabled(true);
                    item.setTitle(R.string.menu_map_traffic_on);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();

            mBaiduMap.setMyLocationData(data);

            mLatitude = location.getLatitude();
            mLongtitude = location.getLongitude();
            if (isFirstIn) {
                LatLng latLng = new LatLng(mLatitude, mLongtitude);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

                String str =  "Latitude: " + mLatitude + "\nLongtitude: " + mLongtitude;
                Toast.makeText(getApplicationContext(), str.toString(), Toast.LENGTH_SHORT).show();

//                //定义Maker坐标点
//                LatLng point = new LatLng(mLatitude + 0.00001, mLongtitude + 0.00001);
//                //构建Marker图标
//                BitmapDescriptor bitmap = BitmapDescriptorFactory
//                        .fromResource(R.drawable.icon_friends);
//                //构建MarkerOption，用于在地图上添加Marker
//                OverlayOptions option = new MarkerOptions()
//                        .position(point)
//                        .icon(bitmap);
//                //在地图上添加Marker，并显示
//                mBaiduMap.addOverlay(option);
                mBaiduMap.animateMapStatus(msu);

                isFirstIn = false;
            }

            if (isRefresh) {

                mMy.setLatitude(Double.toString(mLatitude));
                mMy.setLongtitude(Double.toString(mLongtitude));
                mMy.setLongtitude(Double.toString(mLongtitude));
                mMy.setAltitude(Double.toString(location.getAltitude()));
                mMy.setAccuracy(Double.toString(location.getRadius()));
                mMy.setNearestAddress(location.getAddrStr());
                mMy.setSSinceUpdate(location.getTime());

                isRefresh = false;
            }
        }
    }


    private void refreshOverlays() {
        for (Friend f : mFriends) {
            String phone = f.getPhoneNumber().toString();
            String context = "Where are you? " + f.getName().toString();
            smsSendMsg(phone, context);
        }

        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver,receiveFilter);
    }

    //短信发送后的发送状态广播接收器
    public class SmsStatusReceiver extends BroadcastReceiver {

        private static final String TAG = "SmsStatusReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"SmsStatusReceiver onReceive.");
            switch(getResultCode()) {
                case Activity.RESULT_OK:
                    Log.d(TAG, "Activity.RESULT_OK");
                    break;
                case RESULT_ERROR_GENERIC_FAILURE:
                    Log.d(TAG, "RESULT_ERROR_GENERIC_FAILURE");
                    break;
                case RESULT_ERROR_NO_SERVICE:
                    Log.d(TAG, "RESULT_ERROR_NO_SERVICE");
                    break;
                case RESULT_ERROR_NULL_PDU:
                    Log.d(TAG, "RESULT_ERROR_NULL_PDU");
                    break;
                case RESULT_ERROR_RADIO_OFF:
                    Log.d(TAG, "RESULT_ERROR_RADIO_OFF");
                    break;
            }
        }
    }

    //短信发送到对方后，对对方返回的接受状态的处理逻辑
    public class SmsDeliveryStatusReceiver extends BroadcastReceiver {

        private static final String TAG = "SmsDeliveryStatus";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"SmsDeliveryStatusReceiver onReceive.");
            switch(getResultCode()) {
                case Activity.RESULT_OK:
                    //Toast.makeText(context,"Send Succeeded",Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "RESULT_OK");
                    break;
                case Activity.RESULT_CANCELED:
                    //Toast.makeText(context,"Send Failed",Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "RESULT_CANCELED");
                    break;
            }
        }
    }

    class MessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String sms = "";
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                mAddressString = smsMessage.getDisplayOriginatingAddress();
                String fullMessage = smsMessage.getMessageBody();


                //content.setText(fullMessage);
                sms += fullMessage;
               /* if ("10086".equals(address)) {//测试截断短信
                    abortBroadcast();
                }*/
            }

            Friend f = FriendLab.getFriendLab(MapActivity.this).getFriend(mAddressString);
            if (sms.charAt(0) == 'W') {
                StringBuffer str1 = new StringBuffer(256);
                str1.append("latitude : ");
                str1.append(mMy.getLatitude());
                str1.append("\nlontitude : ");
                str1.append(mMy.getLongtitude());
                str1.append("\naccuracy : ");
                str1.append(mMy.getAccuracy());

                Log.i("BaiduLocationApiDem", str1.toString());

                smsSendMsg(mAddressString, str1.toString());


                StringBuffer str2 = new StringBuffer(256);
                str2.append("altitude : ");
                str2.append(mMy.getAltitude());// 单位：米
                str2.append("\naddress : ");
                str2.append(mMy.getNearestAddress());
                str2.append("\ntime : ");
                str2.append(mMy.getSSinceUpdate());

                Log.i("BaiduLocationApiDem", str2.toString());

                smsSendMsg(mAddressString, str2.toString());
            }
            else if (sms.charAt(0) == 'l'){
                sms = sms.replace(" : ", ":");
                sms = sms.replace("\n", ":");
                String []str = sms.split(":");


                f.setLatitude(str[1]);
                f.setLongtitude(str[3]);
                f.setAccuracy(str[5]);

                double latitude = Double.valueOf(str[1]);
                double longtitude = Double.valueOf(str[3]);

                //定义Maker坐标点
                LatLng point = new LatLng(latitude, longtitude);
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_friends);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap);
                //在地图上添加Marker，并显示
                Marker marker = (Marker) mBaiduMap.addOverlay(option);

                //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
                Bundle bundle = new Bundle();
                //info必须实现序列化接口
                bundle.putSerializable("info", f);
                marker.setExtraInfo(bundle);
             }
            else if (sms.charAt(0) == 'a') {
                sms = sms.replace(" : ", ":");
                sms = sms.replace("\n", ":");
                String []str = sms.split(":");

                f.setAltitude(str[1]);
                f.setNearestAddress(str[3]);
                f.setSSinceUpdate(str[5]);
            }
        }
    }

    private void smsSendMsg(String toWho, String msg) {

        String message = msg.toString();
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> list = manager.divideMessage(message);  //因为一条短信有字数限制，因此要将长短信拆分
        //这个意图包装了对短信发送状态回调的处理逻辑
        PendingIntent sentIntent = PendingIntent.getBroadcast(MapActivity.this, 1, new Intent(SMS_SEND_ACTION), 0);
        //这个意图包装了对短信接受状态回调的处理逻辑
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(MapActivity.this, 2, new Intent(SMS_DELIVERED_ACTION), 0);
        for(String text:list){
            manager.sendTextMessage(toWho, null, text, sentIntent, deliveryIntent);
        }
        Toast.makeText(getApplicationContext(), "发送完毕", Toast.LENGTH_SHORT).show();
    }

}
