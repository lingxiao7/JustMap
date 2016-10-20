package com.example.lx.justmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

/**
 * Created by lx on 2016/10/16.
 */
public class FriendsActivity  extends Activity implements FriendAdapter.ListItemClickHelp {
    private static final String TAG = "FriendsActivity";

    //定义一个startActivityForResult（）方法用到的整型值
    private final int requestCode = 1500;

    private ArrayList<Friend> mFriends;
    private ListView mListView;

    private FriendAdapter mAdapter;
    private int mPosition = -1;

    private ToggleButton mEditToggleButton;
    private Button mAddButton;
    private Button mRadarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.friends_list);

        mFriends = FriendLab.getFriendLab(this).getFriends();

        mListView  =  (ListView)findViewById(R.id.lvw_friends_list);
        mAdapter = new FriendAdapter(this , mFriends, this);
        mListView.setAdapter(mAdapter); //为ListView设置适配器

        mEditToggleButton = (ToggleButton)findViewById(R.id.btn_friends_list_edit);
        mEditToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditToggleButton.isChecked()) {
                    if (mPosition == -1) return;
                    Intent intent = new Intent(FriendsActivity.this, FriendsDetailActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(FriendsDetailActivity.EXTRA_FRIEND_ID, mFriends.get(mPosition).getId());
                    intent.putExtras(mBundle);
                    startActivityForResult(intent, requestCode);
                }
                else {
                    FriendLab.getFriendLab(FriendsActivity.this).savaFriends();
                }
            }
        });


        mAddButton = (Button)findViewById(R.id.btn_friends_list_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder customizeDialog =
                        new AlertDialog.Builder(FriendsActivity.this);
                final View dialogView = LayoutInflater.from(FriendsActivity.this)
                        .inflate(R.layout.dialog_add_friend,null);
                //customizeDialog.setTitle("添加Friend");
                customizeDialog.setView(dialogView);

                final AlertDialog dialog = customizeDialog.show();

                Button okButton = (Button)dialogView.findViewById(R.id.btn_dialog_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText nameEditText = (EditText)dialogView.findViewById(R.id.txt_friend_name);
                        EditText numberEditText = (EditText)dialogView.findViewById(R.id.txt_friend_number);

                        if (nameEditText.getText().toString().equals("") || numberEditText.getText().toString().equals("")) {
                            dialog.dismiss();
                            return;
                        }

                        Friend f = new Friend();
                        f.setName(nameEditText.getText().toString());
                        f.setPhoneNumber(numberEditText.getText().toString());
                        mFriends.add(f);

                        mAdapter = new FriendAdapter(FriendsActivity.this, mFriends, FriendsActivity.this);
                        mListView.setAdapter(mAdapter); //为ListView设置适配器
                        dialog.dismiss();

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

        mRadarButton = (Button)findViewById(R.id.btn_friends_list_radar);
        mRadarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsActivity.this.finish();
            }
        });
    }

    /**
     * 接收当前Activity跳转后，目标Activity关闭后的回传值
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case RESULT_OK:{//接收并显示Activity传过来的值
                FriendsActivity.this.finish();
                break;
            }
            default:
                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        FriendLab.getFriendLab(this).savaFriends();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter = new FriendAdapter(this , mFriends, this);
        mListView.setAdapter(mAdapter); //为ListView设置适配器
    }


    @Override
    public void onClick(int position, int index) {
        switch (index) {
            case 0:
                final Friend f = mFriends.get(position);

                AlertDialog.Builder customizeDialog =
                        new AlertDialog.Builder(FriendsActivity.this);
                final View dialogView = LayoutInflater.from(FriendsActivity.this)
                        .inflate(R.layout.dialog_delete,null);
                //customizeDialog.setTitle("添加Friend");
                customizeDialog.setView(dialogView);
                TextView numberTextView = (TextView)dialogView.findViewById(R.id.txt_friend_number);
                numberTextView.setText(f.getPhoneNumber().toString());


                final AlertDialog dialog = customizeDialog.show();

                Button okButton = (Button)dialogView.findViewById(R.id.btn_dialog_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FriendLab.getFriendLab(getApplication()).deleteFriend(f);
                        mPosition = -1;
                        dialog.dismiss();

                        mAdapter = new FriendAdapter(FriendsActivity.this , mFriends, FriendsActivity.this);
                        mListView.setAdapter(mAdapter); //为ListView设置适配器
                    }
                });

                Button closeButton = (Button)dialogView.findViewById(R.id.btn_dialog_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case 1:
                mPosition = position;
                break;
            default:
                break;
        }
    }
}
