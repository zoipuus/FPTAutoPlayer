package com.toptech.autoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.toptech.autoplayer.ftp.FTPManager;
import com.toptech.autoplayer.utils.ConstantUtil;
import com.toptech.autoplayer.utils.FileUtil;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by zoipuus on 2017/11/28.
 */

public class MainActivity extends Activity {

    private FTPManager mFTPManager;
    private boolean isConnected;
    private ConnectStateHandler mConnectStateHandler;
    private Button btn_connect;
    private ProgressBar progressBar;

    private static class ConnectStateHandler extends Handler {
        private final WeakReference<MainActivity> mWeakReference;

        public ConnectStateHandler(MainActivity activity) {
            mWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case ConstantUtil.FTP_SERVER_CONNECTED:
                        // TODO:delete all files
                        FileUtil.onDeleteFiles(new File(FileUtil.getCachePath(activity, FileUtil.IMAGE_PATH)));
                        activity.onDisconnectFTP();
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.START_AutoPlayImageActivity");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                        break;
                    case ConstantUtil.FTP_SERVER_DISCONNECTED:
                        activity.btn_connect.setVisibility(View.VISIBLE);
                        activity.progressBar.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mConnectStateHandler = new ConnectStateHandler(this);
    }

    private void initViews() {
        final EditText edit_ip = (EditText) this.findViewById(R.id.id_ip_address);
        final EditText edit_port = (EditText) this.findViewById(R.id.id_port);
        final EditText edit_user_name = (EditText) this.findViewById(R.id.id_user_name);
        final EditText edit_password = (EditText) this.findViewById(R.id.id_password);
        final EditText edit_download_path = (EditText) this.findViewById(R.id.id_download_path);
        btn_connect = (Button) this.findViewById(R.id.id_btn_connect);
        progressBar = (ProgressBar) this.findViewById(R.id.id_pro_connecting);

        edit_ip.setText(getSharedPreferencesString(ConstantUtil.KEY_IP_ADDRESS));
        edit_port.setText(getSharedPreferencesInt(ConstantUtil.KEY_PORT) + "");
        edit_user_name.setText(getSharedPreferencesString(ConstantUtil.KEY_USER_NAME));
        edit_password.setText(getSharedPreferencesString(ConstantUtil.KEY_PASSWORD));
        edit_download_path.setText(getSharedPreferencesString(ConstantUtil.KEY_DOWNLOAD_PATH));

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_ip.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.tip_ip_address_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_ip.requestFocus();
                } else if (edit_port.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.tip_port_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_port.requestFocus();
                } else if (edit_user_name.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.tip_user_name_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_user_name.requestFocus();
                } else if (edit_password.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.tip_password_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_password.requestFocus();
                } else if (edit_download_path.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.tip_download_path_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_download_path.requestFocus();
                } else {
                    btn_connect.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    saveSharedPreferencesString(ConstantUtil.KEY_IP_ADDRESS, edit_ip.getText().toString().trim());
                    int port = 0;
                    try{
                        port = Integer.parseInt(edit_port.getText().toString().trim());
                    } catch (NumberFormatException e) {

                    }
                    saveSharedPreferencesInt(ConstantUtil.KEY_PORT, port);
                    saveSharedPreferencesString(ConstantUtil.KEY_USER_NAME, edit_user_name.getText().toString().trim());
                    saveSharedPreferencesString(ConstantUtil.KEY_PASSWORD, edit_password.getText().toString().trim());
                    saveSharedPreferencesString(ConstantUtil.KEY_DOWNLOAD_PATH, edit_download_path.getText().toString().trim());
                    /*
                    ConstantUtil.FTP_SERVER_HOST = getSharedPreferencesString(ConstantUtil.KEY_IP_ADDRESS);
                    ConstantUtil.FTP_SERVER_PORT = getSharedPreferencesInt(ConstantUtil.KEY_PORT);
                    ConstantUtil.FTP_SERVER_USERNAME = getSharedPreferencesString(ConstantUtil.KEY_USER_NAME);
                    ConstantUtil.FTP_SERVER_PASSWORD = getSharedPreferencesString(ConstantUtil.KEY_PASSWORD);
                    ConstantUtil.FTP_SERVER_DOWNLOAD_PATH = getSharedPreferencesString(ConstantUtil.KEY_DOWNLOAD_PATH);
                    */
                    new Thread() {
                        @Override
                        public void run() {
                            mFTPManager = new FTPManager();
                            isConnected = mFTPManager.connect(MainActivity.this.getApplicationContext());
                            while (!isConnected) {
                                try {
                                    sleep(3 * 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Message msg = mConnectStateHandler.obtainMessage();
                            if (isConnected) {
                                msg.what = ConstantUtil.FTP_SERVER_CONNECTED;
                            } else {
                                msg.what = ConstantUtil.FTP_SERVER_DISCONNECTED;
                            }
                            mConnectStateHandler.sendMessage(msg);
                        }
                    }.start();
                }
            }
        });
    }

    private void saveSharedPreferencesString(String key, String value) {
        SharedPreferences sp = getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    private void saveSharedPreferencesInt(String key, int value) {
        SharedPreferences sp = getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    private String getSharedPreferencesString(String key) {
        SharedPreferences sp = getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    private int getSharedPreferencesInt(String key) {
        SharedPreferences sp = getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        onDisconnectFTP();
        isConnected = true;
    }

    private void onDisconnectFTP() {
        if (mFTPManager != null) {
            mFTPManager.onDisconnectFTP();
        }
    }
}
