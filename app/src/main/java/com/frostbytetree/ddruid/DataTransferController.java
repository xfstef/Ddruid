package com.frostbytetree.ddruid;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Service will be used to Communicate with the Server and the main App

    //TODO Establish connection to the Server and authenticate the current user
    //TODO Download / Open and Update the Config File
    //TODO Download / Open and Update the Raw Data
    //TODO Signal the DataModelInterpreter when there are updates
    //TODO Signal the TemporaryWidget when there are updates
    //TODO Signal the SQLDaemon when there are updates

public class DataTransferController extends Service{
    private static DataTransferController ourInstance = new DataTransferController();
    Data data;
    ConfigFile configFile;
    CommunicationDaemon commDaemon;
    Notification someNotification;
    NetworkListener networkListener;

    public static DataTransferController getInstance() {
        return ourInstance;
    }

    public DataTransferController() {
        data = Data.getInstance();
        configFile = ConfigFile.getInstance();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(0, someNotification);
        commDaemon = CommunicationDaemon.getInstance();
        commDaemon.start();
        networkListener = new NetworkListener();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
