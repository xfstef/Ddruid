package com.frostbytetree.ddruid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppLogic appLogic;
    SQLiteController sqldaemon;
    RawData rawData;
    DataModels dataModels;
    ConfigFile configFile;
    UIBuilder uiBuilder;
    WidgetViews widgetViews;
    IACInterface commInterface;

    View login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Endlich gestartet!");

        LinearLayout lin_test = (LinearLayout)findViewById(R.id.test_layout);
        lin_test.setOnClickListener(this);

        commInterface = IACInterface.getInstance();

        rawData = RawData.getInstance();
        dataModels = DataModels.getInstance();
        configFile = ConfigFile.getInstance();

        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this);

        widgetViews = WidgetViews.getInstance();

        rawData.setTest("Moj Kurac !");

        appLogic = AppLogic.getInstance();
        appLogic.start();
        //sqldaemon = SQLiteController.getInstance();
        //sqldaemon.start();

        login = (View)findViewById(R.id.bLogin);
        login.setOnClickListener(this); // TODO: This is a temporary login protocol. Please fix me.

        startService(new Intent(this, DataTransferController.class));


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onClick(View view){

        switch(view.getId()){
            case R.id.bLogin:
                appLogic.initLoginProc();
        }
        // uiBuilder.inflate_model(null);
        // System.out.println("The new view is: " + view);
    }
}
