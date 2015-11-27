package com.frostbytetree.ddruid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    AppLogic appLogic;
    SQLiteController sqldaemon;
    RawData rawData;
    DataModels dataModels;
    ConfigFile configFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Endlich gestartet!");

        startService(new Intent(this, DataTransferController.class));

        rawData = RawData.getInstance();
        dataModels = DataModels.getInstance();
        configFile = ConfigFile.getInstance();

        rawData.setTest("Moj Kurac !");

        appLogic = AppLogic.getInstance();
        appLogic.start();
        sqldaemon = SQLiteController.getInstance();
        sqldaemon.start();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
