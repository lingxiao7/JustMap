package com.example.lx.justmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
public class EnemiesActivity extends Activity implements PersonAdapter.ListItemClickHelp {
    private static final String TAG = "FriendsActivity";

    //定义一个startActivityForResult（）方法用到的整型值
    private final int requestCode = 2;

    private ArrayList<Person> mEnemies;
    private ListView mListView;

    private PersonAdapter mAdapter;
    private int mPosition = -1;

    private ToggleButton mEditToggleButton;
    private Button mAddButton;
    private Button mRadarButton;
    private Button mFriendsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.enemies_list);

        mEnemies = PersonLab.get(this).getEnemies();

        mListView  =  (ListView)findViewById(R.id.lvw_enemies_list);
        mAdapter = new PersonAdapter(this , mEnemies, this, false);
        mListView.setAdapter(mAdapter); //为ListView设置适配器

        mEditToggleButton = (ToggleButton)findViewById(R.id.btn_enemies_list_edit);
        mEditToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditToggleButton.isChecked()) {
                    if (mPosition == -1) return;
                    Intent intent = new Intent(EnemiesActivity.this, EnemiesDetailActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(EnemiesDetailActivity.EXTRA_ENEMY_ID, mEnemies.get(mPosition).getId());
                    intent.putExtras(mBundle);
                    startActivityForResult(intent, requestCode);
                }
                else {
                    PersonLab.get(EnemiesActivity.this).savaLab();
                }
            }
        });


        mAddButton = (Button)findViewById(R.id.btn_enemies_list_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder customizeDialog =
                        new AlertDialog.Builder(EnemiesActivity.this);
                final View dialogView = LayoutInflater.from(EnemiesActivity.this)
                        .inflate(R.layout.dialog_add_enemy,null);
                //customizeDialog.setTitle("添加Friend");
                customizeDialog.setView(dialogView);

                final AlertDialog dialog = customizeDialog.show();

                Button okButton = (Button)dialogView.findViewById(R.id.btn_dialog_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText nameEditText = (EditText)dialogView.findViewById(R.id.txt_enemy_name);
                        EditText numberEditText = (EditText)dialogView.findViewById(R.id.txt_enemy_number);

                        if (nameEditText.getText().toString().equals("") || numberEditText.getText().toString().equals("")) {
                            dialog.dismiss();
                            return;
                        }

                        Person f = new Person();
                        f.setName(nameEditText.getText().toString());
                        f.setPhoneNumber(numberEditText.getText().toString());
                        mEnemies.add(f);

                        mAdapter = new PersonAdapter(EnemiesActivity.this, mEnemies, EnemiesActivity.this, false);
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

        mRadarButton = (Button)findViewById(R.id.btn_enemies_list_radar);
        mRadarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EnemiesActivity.this.setResult(RESULT_OK);
                EnemiesActivity.this.finish();
            }
        });

        mFriendsButton = (Button)findViewById(R.id.btn_enemies_list_friends);
        mFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(EnemiesActivity.this, FriendsActivity.class);
                startActivityForResult(intent, requestCode);
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
                EnemiesActivity.this.setResult(RESULT_OK);
                EnemiesActivity.this.finish();
                break;
            }
            default:
                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        PersonLab.get(this).savaLab();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter = new PersonAdapter(this , mEnemies, this, false);
        mListView.setAdapter(mAdapter); //为ListView设置适配器
    }


    @Override
    public void onClick(int position, int index) {
        switch (index) {
            case 0:
                final Person f = mEnemies.get(position);

                AlertDialog.Builder customizeDialog =
                        new AlertDialog.Builder(EnemiesActivity.this);
                final View dialogView = LayoutInflater.from(EnemiesActivity.this)
                        .inflate(R.layout.dialog_delete,null);
                //customizeDialog.setTitle("添加Friend");
                customizeDialog.setView(dialogView);
                TextView numberTextView = (TextView)dialogView.findViewById(R.id.txt_friend_number);
                String number = f.getPhoneNumber().toString();
                numberTextView.setText(number);


                final AlertDialog dialog = customizeDialog.show();

                Button okButton = (Button)dialogView.findViewById(R.id.btn_dialog_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PersonLab.get(getApplication()).deleteFriend(f);
                        mPosition = -1;
                        dialog.dismiss();

                        mAdapter = new PersonAdapter(EnemiesActivity.this , mEnemies, EnemiesActivity.this, false);
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
