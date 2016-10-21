package com.example.lx.justmap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
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
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

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
    private boolean isRefresh = false;
    private double mLatitude;
    private double mLongtitude;

    private ArrayList<Person> mFriends;
    private ArrayList<Person> mEnemies;
    private Person mMy;

    private Button mFriendsButton;
    private Button mEnemiesButton;
    private Button mLocationButton;
    private ToggleButton mRefreshToggleButton;



    private IntentFilter receiveFilter;
    private MessageReceiver messageReceiver;
    private SmsStatusReceiver mSmsStatusReceiver;
    private SmsDeliveryStatusReceiver mSmsDeliveryStatusReceiver;
    private String mAddressString;



    private RadarView radarView;
    private Thread radarSweepThread;

    private boolean startRadar = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);


        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely);
        animation.setInterpolator(new LinearInterpolator());
        initPerson();
        initView();

        mMy = new Person();
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
        mEnemiesButton = (Button)findViewById(R.id.btn_enemies);
        mEnemiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Activity
                Intent i = new Intent(MapActivity.this, EnemiesActivity.class);
                startActivity(i);
            }
        });


        radarView = (RadarView) findViewById(R.id.radar_view);

        radarView.setVisibility(View.VISIBLE);// 设置可见
        Animation radarAnimEnter = AnimationUtils.loadAnimation(
                MapActivity.this, R.anim.radar_anim_enter);// 初始化radarView进入动画
        radarView.startAnimation(radarAnimEnter);// 开始进入动画
        radarSweepThread = new Thread(new RadarSweep());// 雷达扫描线程
        radarSweepThread.start();
        startRadar = false;

        mRefreshToggleButton = (ToggleButton)findViewById(R.id.btn_refresh);
        mRefreshToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 当按钮第一次被点击时候响应的事件
                if (mRefreshToggleButton.isChecked()) {
                    mBaiduMap.clear();
                    refreshOverlays();
                    isRefresh = true;
                    Animation radarAnimEnter = AnimationUtils.loadAnimation(
                            MapActivity.this, R.anim.radar_anim_exit);// 初始化radarView退出动画
                    radarView.startAnimation(radarAnimEnter);// 开始进入动画
                    radarView.setVisibility(View.INVISIBLE);// 设置不可见
                    radarSweepThread.interrupt();// 停止扫描更新
                    startRadar = true;
                    radarView.setVisibility(View.VISIBLE);// 设置可见
                    radarAnimEnter = AnimationUtils.loadAnimation(
                            MapActivity.this, R.anim.radar_anim_enter);// 初始化radarView进入动画
                    radarView.startAnimation(radarAnimEnter);// 开始进入动画
                    radarSweepThread = new Thread(new RadarSweep());// 雷达扫描线程
                    radarSweepThread.start();
                    startRadar = false;
                }
                // 当按钮再次被点击时候响应的事件
                else {
                    //
                    isRefresh = false;

                }
            }
        });

        mLocationButton = (Button)findViewById(R.id.btn_locate);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LatLng latLng = new LatLng(mLatitude, mLongtitude);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

                String str =  "Latitude: " + mLatitude + "\nLongtitude: " + mLongtitude;
                Toast.makeText(getApplicationContext(), str.toString(), Toast.LENGTH_SHORT).show();
                mBaiduMap.animateMapStatus(msu);
            }
        });

    }

    private void initOverlays() {

        for (Person f : mFriends) {
            if (f.getLatitude() != null) {

                double latitude = Double.valueOf(f.getLatitude());
                double longtitude = Double.valueOf(f.getLongtitude());
                addOverlays(f, latitude, longtitude, false);
            }
        }

        for (Person e : mEnemies) {

            if (e.getLatitude() != null) {

                double latitude = Double.valueOf(e.getLatitude());
                double longtitude = Double.valueOf(e.getLongtitude());
                addOverlays(e, latitude, longtitude, true);
            }
        }
    }


    private void addOverlays(Person f, double latitude, double longtitude, boolean isEnemy) {

        //构建Marker图标
        BitmapDescriptor bitmap;
        int color;
        if (isEnemy) {
            color = Color.RED;
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.enemy_marker);
        }
        else {
            color = Color.GREEN;
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.friend_marker);
        }

        Marker marker;
        String str = f.getName() + "\n" + f.getPhoneNumber();



        //定义Maker坐标点
        LatLng point = new LatLng(latitude, longtitude);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .zIndex(9); // 设置marker所在层级;

        OverlayOptions textOption = new TextOptions()
                .fontSize(24)
                .fontColor(0xFF000000)
                .text(str)
                .position(point);
        //在地图上添加Marker，并显示
        marker = (Marker) mBaiduMap.addOverlay(option);
        mBaiduMap.addOverlay(textOption);

        LatLng point2 = new LatLng(mLatitude, mLongtitude);
        List<LatLng> pts = new ArrayList<LatLng>();
        pts.add(point);
        pts.add(point2);
        //构建用户绘制多边形的Option对象
        OverlayOptions polylineOption = new PolylineOptions()
                .width(3)
                .points(pts)
                .color(color);
        //在地图上添加多边形Option，用于显示
        mBaiduMap.addOverlay(polylineOption);

        //计算距离
        LatLng point3 = new LatLng((mLatitude + latitude) / 2, (mLongtitude + longtitude) / 2);
        double dist = DistanceUtil.getDistance(point, point2);

        OverlayOptions textOption2 = new TextOptions()
                .fontSize(24)
                .fontColor(0xFF000000)
                .text(Double.toString(Math.ceil(dist)) + 'm')
                .position(point3);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(textOption2);

        //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
        Bundle bundle = new Bundle();
        //info必须实现序列化接口
        bundle.putSerializable("info", f);
        marker.setExtraInfo(bundle);
    }

    private void initPerson() {
        mFriends = PersonLab.get(this).getFriends();
        mEnemies = PersonLab.get(this).getEnemies();
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
                Person f =  (Person) bundle.getSerializable("info");

                String str = f.getName() + " " + f.getPhoneNumber() + "\n" + f.getLatitude() + " " + f.getLongtitude();


                Toast.makeText(getApplicationContext(), str.toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

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
                initOverlays();
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
        for (Person f : mFriends) {
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
//                if ("10086".equals(mAddressString)) {//测试截断短信
//                    abortBroadcast();
//                }
            }

            Person f = PersonLab.get(MapActivity.this).getFriend(mAddressString);
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

                if (PersonLab.get(getApplication()).getFriend(mAddressString) != null)
                    addOverlays(f, latitude, longtitude, false);
                else
                    addOverlays(f, latitude, longtitude, true);
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


    /**
     * @ClassName RadarSweep
     * @Description 雷达扫描动画刷新线程类
     */
    private class RadarSweep implements Runnable {
        int i = 1;

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted() && i == 1) {
                try {
                    radarView.postInvalidate();// 刷新radarView, 执行onDraw();
                    Thread.sleep(10);// 暂停当前线程，更新UI线程
                } catch (InterruptedException e) {
                    i = 0;// 结束当前扫描线程标志符
                    break;
                }
            }
        }

    }

}
