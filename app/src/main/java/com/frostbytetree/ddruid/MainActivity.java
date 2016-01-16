package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    DrawerLayout Drawer;
    ActionBarDrawerToggle mDrawerToggle;

    Toolbar toolbar;

    EditText uri, username, password;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupWindowAnimations();
        initViewItems();


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
        if(appLogic.isInterrupted())
            appLogic.start();
        //sqldaemon = SQLiteController.getInstance();
        //sqldaemon.start();

        // TODO: This is a temporary login protocol. Please fix me.

        startService(new Intent(this, DataTransferController.class));

        // loading and instanciating toolbar

    }

    @TargetApi(21)
    private void setupWindowAnimations(){
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }

    private void initViewItems()
    {
        username = (EditText)findViewById(R.id.etUsername);
        password = (EditText)findViewById(R.id.etPassword);
        uri = (EditText)findViewById(R.id.etUri);
        login = (Button)findViewById(R.id.bLogin);
        login.setOnClickListener(this);
        toolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onClick(View view){
        System.out.println("OnClick Funktion called!");
        switch(view.getId()){
            case R.id.bLogin:
                System.out.println("Login geklickt!");
                Intent i = new Intent(getApplicationContext(), WidgetActivity.class);


                startActivity(i);

                // appLogic.initLoginProc();
                break;

        }
        // uiBuilder.inflate_model(null);
        // System.out.println("The new view is: " + view);
    }

}
