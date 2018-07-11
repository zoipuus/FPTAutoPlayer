package com.toptech.autoplayer.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.util.Log;

/**
 * Created by zoipuus on 2017/11/28.
 */

public class NetworkChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Log.d(TAG, "action ->> " + intent.getAction());
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            Log.d(TAG, "NetWork state change!");
            NetworkInfo info =intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info == null) {
                return;
            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                Log.d(TAG, "info.getType() : " + info.getType());
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    boolean bool = wifiManager.enableNetwork(wifiInfo.getNetworkId(), true);
                    Log.d(TAG, "NetWork SSID : " + wifiInfo.getSSID());
                    Log.d(TAG, "enableNetwork : " + bool);
                }

                startAutoPlayActivity(context);

            } else if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                Log.d(TAG, "NetWork disconnected!");
            }
        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.i(TAG, "isNetworkAvailable ->> " + NetworkManager.isNetworkAvailable(context));
            if (NetworkManager.isNetworkAvailable(context)) {
                startAutoPlayActivity(context);
            }
        }
    }

    private void startAutoPlayActivity(Context context) {
        Intent localIntent = new Intent();
        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.intent.action.START_AutoPlayImageActivity");
        context.startActivity(localIntent);
    }
}
