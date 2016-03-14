package com.frostbytetree.ddruid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

/**
 * Created by XfStef on 3/14/2016.
 */

public class NetworkListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        System.out.println("~~~~~~~~~~~~ Network changed ~~~~~~~~~~~~");

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int networkType = intent.getExtras().getInt(ConnectivityManager.EXTRA_NETWORK_TYPE);
        boolean isWiFi = networkType == ConnectivityManager.TYPE_WIFI;
        boolean isMobile = networkType == ConnectivityManager.TYPE_MOBILE;
        NetworkInfo networkInfo = null;
        if (Build.VERSION.SDK_INT > 22)
            networkInfo = connectivityManager.getNetworkInfo(connectivityManager.getActiveNetwork());
        else
            networkInfo = connectivityManager.getNetworkInfo(networkType);
        boolean isConnected = networkInfo.isConnected();

        if (isWiFi) {
            if (isConnected) {
                Log.i("APP_TAG", "Wi-Fi - CONNECTED");
            } else {
                Log.i("APP_TAG", "Wi-Fi - DISCONNECTED");
            }
        } else if (isMobile) {
            if (isConnected) {
                Log.i("APP_TAG", "Mobile - CONNECTED");
            } else {
                Log.i("APP_TAG", "Mobile - DISCONNECTED");
            }
        } else {
            if (isConnected) {
                Log.i("APP_TAG", networkInfo.getTypeName() + " - CONNECTED");
            } else {
                Log.i("APP_TAG", networkInfo.getTypeName() + " - DISCONNECTED");
            }
        }
    }

}
