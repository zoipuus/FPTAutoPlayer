package com.toptech.autoplayer.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.toptech.autoplayer.R;
import com.toptech.autoplayer.ftp.FTPConnectListener;
import com.toptech.autoplayer.utils.ConstantUtil;
import com.toptech.autoplayer.utils.SharedPreferencesUtil;

/**
 * Created by zoipuus on 2017/11/30.
 */

public class FTPServerInfoDialog extends Dialog {
    //private Button btn_connect, btn_cancel;
    private ProgressBar progressBar;
    private LinearLayout layout_dialog_btn;
    private FTPConnectListener mFTPConnectListener;

    public FTPServerInfoDialog(Context context) {
        this(context, 0);
    }

    public FTPServerInfoDialog(Context context, int theme) {
        super(context, theme);
        init(context, theme);
    }

    protected FTPServerInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setFTPConnectListener(FTPConnectListener mFTPConnectListener) {
        this.mFTPConnectListener = mFTPConnectListener;
    }

    private void init(Context context, int theme) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ScrollView dialog_info = (ScrollView) inflater.inflate(R.layout.dialog_ftp_server_info, null);
        this.getWindow().setContentView(dialog_info);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int displayWidth = dm.widthPixels;
        int displayHeight = dm.heightPixels;
        android.view.WindowManager.LayoutParams p = this.getWindow().getAttributes();  //获取对话框当前的参数值
        int max = Math.max(displayWidth, displayHeight);
        p.width = (int) (max * 0.5);    //宽度设置为屏幕的0.5
        p.width = Math.min(displayHeight, p.width);
//        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.height = (int) (p.width * 0.8);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        this.getWindow().setAttributes(p);     //设置生效

        initView(dialog_info, context);
    }

    private void initView(ScrollView dialog_info, final Context context) {
        final EditText edit_ip = (EditText) dialog_info.findViewById(R.id.id_ip_address);
        final EditText edit_port = (EditText) dialog_info.findViewById(R.id.id_port);
        final EditText edit_user_name = (EditText) dialog_info.findViewById(R.id.id_user_name);
        final EditText edit_password = (EditText) dialog_info.findViewById(R.id.id_password);
        final EditText edit_download_path = (EditText) dialog_info.findViewById(R.id.id_download_path);
        Button btn_connect = (Button) dialog_info.findViewById(R.id.id_btn_connect);
        Button btn_cancel = (Button) dialog_info.findViewById(R.id.id_btn_cancel);
        layout_dialog_btn = (LinearLayout) dialog_info.findViewById(R.id.id_layout_dialog_btn);
        progressBar = (ProgressBar) dialog_info.findViewById(R.id.id_pro_connecting);

        edit_ip.setText(SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_IP_ADDRESS));
        edit_port.setText(SharedPreferencesUtil.getSharedPreferencesInt(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_PORT) == 0 ?
                "" : SharedPreferencesUtil.getSharedPreferencesInt(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_PORT) + "");
        edit_user_name.setText(SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_USER_NAME));
        edit_password.setText(SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_PASSWORD));
        edit_download_path.setText(SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_DOWNLOAD_PATH));

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_ip.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.tip_ip_address_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_ip.requestFocus();
                } else if (edit_port.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.tip_port_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_port.requestFocus();
                } else if (edit_user_name.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.tip_user_name_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_user_name.requestFocus();
                } else if (edit_password.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.tip_password_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_password.requestFocus();
                } else if (edit_download_path.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.tip_download_path_can_not_empty, Toast.LENGTH_SHORT).show();
                    edit_download_path.requestFocus();
                } else {
                    setConnectState(true);

                    SharedPreferencesUtil.setSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_IP_ADDRESS, edit_ip.getText().toString().trim());
                    int port = 0;
                    try{
                        port = Integer.parseInt(edit_port.getText().toString().trim());
                    } catch (NumberFormatException e) {

                    }
                    SharedPreferencesUtil.setSharedPreferencesInt(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_PORT, port);
                    SharedPreferencesUtil.setSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_USER_NAME, edit_user_name.getText().toString().trim());
                    SharedPreferencesUtil.setSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_PASSWORD, edit_password.getText().toString().trim());
                    SharedPreferencesUtil.setSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_DOWNLOAD_PATH, edit_download_path.getText().toString().trim());
                    /*
                    ConstantUtil.FTP_SERVER_HOST = SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_IP_ADDRESS);
                    ConstantUtil.FTP_SERVER_PORT = SharedPreferencesUtil.getSharedPreferencesInt(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_PORT);
                    ConstantUtil.FTP_SERVER_USERNAME = SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_USER_NAME);
                    ConstantUtil.FTP_SERVER_PASSWORD = SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_PASSWORD);
                    ConstantUtil.FTP_SERVER_DOWNLOAD_PATH = SharedPreferencesUtil.getSharedPreferencesString(FTPServerInfoDialog.this.getContext(), ConstantUtil.KEY_DOWNLOAD_PATH);
                    */
                    if (mFTPConnectListener != null) {
                        mFTPConnectListener.connect();
                    }
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void setConnectState(boolean isConnecting) {
        if (isConnecting) {
            layout_dialog_btn.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            layout_dialog_btn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
