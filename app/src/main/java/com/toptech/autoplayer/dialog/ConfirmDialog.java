package com.toptech.autoplayer.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toptech.autoplayer.R;

/**
 * Created by zoipuus on 2017/11/30.
 */

public class ConfirmDialog extends Dialog {
    private View.OnClickListener mClickListener;
    private TextView content;

    public ConfirmDialog(Context context) {
        this(context, 0, null);
    }

    public ConfirmDialog(Context context, int theme, View.OnClickListener mClickListener) {
        super(context, theme);
        this.mClickListener = mClickListener;
        init(context);
    }

    protected ConfirmDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_confirm, null);
        this.getWindow().setContentView(layout);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int displayWidth = dm.widthPixels;
        int displayHeight = dm.heightPixels;
        android.view.WindowManager.LayoutParams p = this.getWindow().getAttributes();  //获取对话框当前的参数值
        int max = Math.max(displayWidth, displayHeight);
        p.width = (int) (max * 0.4);    //宽度设置为屏幕的0.4
        p.width = Math.min(displayHeight, p.width);
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //p.height = (int) (displayHeight * 0.28);    //高度设置为屏幕的0.28
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        this.getWindow().setAttributes(p);     //设置生效

        initViews(layout);
    }

    private void initViews(LinearLayout layout) {
        Button btn_ok = (Button) layout.findViewById(R.id.id_btn_ok);
        Button btn_cancel = (Button) layout.findViewById(R.id.id_btn_cancel);
        content = (TextView) layout.findViewById(R.id.id_confirm_dialog_content);

        btn_ok.requestFocus();

        if (this.mClickListener != null) {
            btn_ok.setOnClickListener(this.mClickListener);
            btn_cancel.setOnClickListener(this.mClickListener);
        }
    }

    public void setOnClickListener(View.OnClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public void setContent(int resId) {
        if (content != null)
            content.setText(resId);
    }
}
