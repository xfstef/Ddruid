package com.frostbytetree.ddruid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppLogic appLogic;
    SQLiteController sqldaemon;
    RawData rawData;
    DataModels dataModels;
    ConfigFile configFile;
    UIBuilder uiBuilder;
    WidgetViews widgetViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Endlich gestartet!");

        RelativeLayout lin_test = (RelativeLayout)findViewById(R.id.test_layout);
        lin_test.setOnClickListener(this);

        startService(new Intent(this, DataTransferController.class));

        rawData = RawData.getInstance();
        dataModels = DataModels.getInstance();
        configFile = ConfigFile.getInstance();

        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this);

        widgetViews = WidgetViews.getInstance();

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

    @Override
    public void onClick(View view){
        View new_view = uiBuilder.inflate_model(null);
        setContentView(new_view);
    }
}
