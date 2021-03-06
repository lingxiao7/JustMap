package com.example.lx.justmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

/**
 * Created by lx on 2016/10/16.
 */
public class FriendsDetailActivity extends Activity{
    public static final String EXTRA_FRIEND_ID = "com.example.lx.justmap.friend_id";

    //定义一个startActivityForResult（）方法用到的整型值
    private final int requestCode = 1;

    private Person mPerson;

    private Button mListButton;
    private TextView mNameTextView;
    private Button mDeleteButton;
    private TextView mNumberTextView;
    private TextView mLongLaTextView;
    private TextView mAltitudeTextView;
    private TextView mAccuracyTextView;
    private TextView mNearAdTextView;
    private TextView mLastUpdateTextview;
    private TextView mNextUpdatetextview;

    private Button mRadarButton;
    private Button mEnemiesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.friend_detail);

        //接收Activity传过来的值
        final Intent data = getIntent();

        UUID friendId = (UUID)this.getIntent().getSerializableExtra(EXTRA_FRIEND_ID);

        mPerson = PersonLab.get(this).getFriend(friendId);

        initView();

        mListButton = (Button)findViewById(R.id.btn_friends_list);
        mListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsDetailActivity.this.finish();
            }
        });

        mDeleteButton = (Button)findViewById(R.id.btn_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder customizeDialog =
                        new AlertDialog.Builder(FriendsDetailActivity.this);
                final View dialogView = LayoutInflater.from(FriendsDetailActivity.this)
                        .inflate(R.layout.dialog_delete,null);
                //customizeDialog.setTitle("添加Friend");
                customizeDialog.setView(dialogView);
                TextView numberTextView = (TextView)dialogView.findViewById(R.id.txt_friend_number);
                numberTextView.setText(mPerson.getPhoneNumber().toString());


                final AlertDialog dialog = customizeDialog.show();

                Button okButton = (Button)dialogView.findViewById(R.id.btn_dialog_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PersonLab.get(getApplication()).deleteFriend(mPerson);
                        dialog.dismiss();
                        FriendsDetailActivity.this.finish();
                    }
                });

                Button closeButton = (Button)dialogView.findViewById(R.id.btn_dialog_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

            }
        });

        mRadarButton = (Button)findViewById(R.id.btn_radar);
        mRadarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转回MainActivity
                //注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
                FriendsDetailActivity.this.setResult(RESULT_OK);
                FriendsDetailActivity.this.finish();
            }
        });

        mEnemiesButton = (Button)findViewById(R.id.btn_enemies);
        mEnemiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(FriendsDetailActivity.this, EnemiesActivity.class);
                startActivityForResult(intent, requestCode);
            }
        });
    }

    private void initView() {
        mNameTextView = (TextView)findViewById(R.id.txt_friend_name);
        mNameTextView.setText(mPerson.getName());

        mNumberTextView = (TextView)findViewById(R.id.txt_friend_number);
        mNumberTextView.setText(mPerson.getPhoneNumber());

        mLongLaTextView = (TextView)findViewById(R.id.txt_friend_long_lang);
        mLongLaTextView.setText(mPerson.getLatitude() + "/" +  mPerson.getLongtitude());

        mAltitudeTextView = (TextView)findViewById(R.id.txt_friend_altitude);
        mAltitudeTextView.setText(mPerson.getAltitude());

        mAccuracyTextView = (TextView)findViewById(R.id.txt_friend_accuracy);
        mAccuracyTextView.setText(mPerson.getAccuracy());

        mNearAdTextView = (TextView)findViewById(R.id.txt_friend_nearest_city);
        mNearAdTextView.setText(mPerson.getNearestAddress());

        mLastUpdateTextview = (TextView)findViewById(R.id.txt_friend_secs_last_update);
        mLastUpdateTextview.setText(mPerson.getSSinceUpdate());

        mNextUpdatetextview = (TextView)findViewById(R.id.txt_friend_secs_next_update);
        mNextUpdatetextview.setText((mPerson.getSUtilUpdate()));
    }


    /**
     * 接收当前Activity跳转后，目标Activity关闭后的回传值
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case RESULT_OK:{//接收并显示Activity传过来的值
                FriendsDetailActivity.this.setResult(RESULT_OK);
                FriendsDetailActivity.this.finish();
                break;
            }
            default:
                break;
        }
    }
}
