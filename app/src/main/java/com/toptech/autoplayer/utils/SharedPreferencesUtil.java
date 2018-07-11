package com.toptech.autoplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by zoipuus on 2017/11/30.
 */

public class SharedPreferencesUtil {

    public static final String TAG = "SharedPreferencesUtil";

    public static void setSharedPreferencesString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static void setSharedPreferencesInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    public static String getSharedPreferencesString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static int getSharedPreferencesInt(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("FTP_SERVER_INFO", Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }

    public static boolean isUpdateFile(Context context) {
        boolean bool = !getSharedPreferencesString(context, ConstantUtil.KEY_FTP_LOCAL_VERSION).equals(getSharedPreferencesString(context, ConstantUtil.KEY_FTP_REMOTE_VERSION));
        Log.i(TAG, "local version : " + getSharedPreferencesString(context, ConstantUtil.KEY_FTP_LOCAL_VERSION)
                + "\nremote version : " + getSharedPreferencesString(context, ConstantUtil.KEY_FTP_REMOTE_VERSION)
                + "\nisUpdateFile -> " + bool);
        return bool;
    }

    public static boolean checkFTPServerInfo(Context context) {
        Log.i(TAG, "IP ADDRESS : " + getSharedPreferencesString(context, ConstantUtil.KEY_IP_ADDRESS)
                + "\nPORT : " + getSharedPreferencesInt(context, ConstantUtil.KEY_PORT)
                + "\nUSER NAME : " + getSharedPreferencesString(context, ConstantUtil.KEY_USER_NAME)
                + "\nPASSWORD : " + getSharedPreferencesString(context, ConstantUtil.KEY_PASSWORD)
                + "\nDOWNLOAD PATH : " + getSharedPreferencesString(context, ConstantUtil.KEY_DOWNLOAD_PATH));
        return (!TextUtils.isEmpty(getSharedPreferencesString(context, ConstantUtil.KEY_IP_ADDRESS))
                && getSharedPreferencesInt(context, ConstantUtil.KEY_PORT) != 0
                && !TextUtils.isEmpty(getSharedPreferencesString(context, ConstantUtil.KEY_USER_NAME))
                && !TextUtils.isEmpty(getSharedPreferencesString(context, ConstantUtil.KEY_PASSWORD))
                && !TextUtils.isEmpty(getSharedPreferencesString(context, ConstantUtil.KEY_DOWNLOAD_PATH)));
    }
}
