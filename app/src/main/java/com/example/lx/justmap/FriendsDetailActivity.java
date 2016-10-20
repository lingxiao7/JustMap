package com.example.lx.justmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by lx on 2016/10/16.
 */
public class FriendsDetailActivity extends Activity{
    public static final String EXTRA_FRIEND_ID = "com.example.lx.justmap.friend_id";

    private Friend mFriend;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.friend_detail);

        //接收Activity传过来的值
        final Intent data = getIntent();

        UUID friendId = (UUID)this.getIntent().getSerializableExtra(EXTRA_FRIEND_ID);

        mFriend = FriendLab.getFriendLab(this).getFriend(friendId);

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
                numberTextView.setText(mFriend.getPhoneNumber().toString());


                final AlertDialog dialog = customizeDialog.show();

                Button okButton = (Button)dialogView.findViewById(R.id.btn_dialog_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FriendLab.getFriendLab(getApplication()).deleteFriend(mFriend);
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
    }

    private void initView() {
        mNameTextView = (TextView)findViewById(R.id.txt_friend_name);
        mNameTextView.setText(mFriend.getName());

        mNumberTextView = (TextView)findViewById(R.id.txt_friend_number);
        mNumberTextView.setText(mFriend.getPhoneNumber());

        mLongLaTextView = (TextView)findViewById(R.id.txt_friend_long_lang);
        mLongLaTextView.setText(mFriend.getLatitude() + "/" +  mFriend.getLongtitude());

        mAltitudeTextView = (TextView)findViewById(R.id.txt_friend_altitude);
        mAltitudeTextView.setText(mFriend.getAltitude());

        mAccuracyTextView = (TextView)findViewById(R.id.txt_friend_accuracy);
        mAccuracyTextView.setText(mFriend.getAccuracy());

        mNearAdTextView = (TextView)findViewById(R.id.txt_friend_nearest_city);
        mNearAdTextView.setText(mFriend.getNearestAddress());

        mLastUpdateTextview = (TextView)findViewById(R.id.txt_friend_secs_last_update);
        mLastUpdateTextview.setText(mFriend.getSSinceUpdate());

        mNextUpdatetextview = (TextView)findViewById(R.id.txt_friend_secs_next_update);
        mNextUpdatetextview.setText((mFriend.getSUtilUpdate()));
    }
}
