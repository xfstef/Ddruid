package com.frostbytetree.ddruid;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppLogic appLogic;
    SQLDaemon sqldaemon;
    Data data;
    DataInterpreter dataInterpreter;
    ConfigFile configFile;
    ConfigFileInterpreter configFileInterpreter;
    UIBuilder uiBuilder;
    WidgetViews widgetViews;
    IACInterface commInterface;
    DrawerLayout drawer;
    ActionBarDrawerToggle mDrawerToggle;
    SclableInterpreter sclableInterpreter;
    CommunicationDaemon communicationDaemon;
    SharedPreferences sharedPreferences;
    SclableURIS sclableURIS;
    private static final String CLASS_NAME = "MainActivity";

    Toolbar toolbar;

    EditText uri, username, password;
    TextView statusText;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //int resourceID = getResources().getIdentifier("green", "color", getPackageName());


        sharedPreferences = getSharedPreferences("com.frostbytetree.ddruid", Context.MODE_PRIVATE);

        if (Build.VERSION.SDK_INT > 21) {
            setupWindowAnimations();
        }
        initViewItems();


        LinearLayout lin_test = (LinearLayout) findViewById(R.id.test_layout);
        lin_test.setOnClickListener(this);
        commInterface = IACInterface.getInstance();

        data = Data.getInstance();
        configFile = ConfigFile.getInstance();
        sclableURIS = SclableURIS.getInstance();

        uiBuilder = UIBuilder.getInstance();
        uiBuilder.setContext(this);

        widgetViews = WidgetViews.getInstance();
        widgetViews.default_widget = new Widget(this);

        dataInterpreter = DataInterpreter.getInstance();

        configFileInterpreter = ConfigFileInterpreter.getInstance();
        configFileInterpreter.context = this;

        sclableInterpreter = SclableInterpreter.getInstance();
        sclableInterpreter.context = this;

        appLogic = AppLogic.getInstance();
        appLogic.mainActivity = this;
        if (!appLogic.isAlive())
            appLogic.start();

        data.appLogic = appLogic;

        sqldaemon = SQLDaemon.getInstance();
        if (!sqldaemon.isAlive())
            sqldaemon.start();

        sqldaemon.preferences = sharedPreferences;
        sqldaemon.prepare_dbs();

        appLogic.sqlDaemon = sqldaemon;

        startService(new Intent(this, DataTransferController.class));
        communicationDaemon = CommunicationDaemon.getInstance();
        communicationDaemon.sqlDaemon = sqldaemon;
        appLogic.communicationDaemon = communicationDaemon;
        sqldaemon.communicationDaemon = communicationDaemon;

        // loading and instanciating toolbar

    }

    @TargetApi(21)
    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }

    private void initViewItems() {
        setContentView(R.layout.activity_main);
        username = (EditText) findViewById(R.id.etUsername);
        password = (EditText) findViewById(R.id.etPassword);
        uri = (EditText) findViewById(R.id.etUri);
        login = (Button) findViewById(R.id.bLogin);
        login.setOnClickListener(this);
        statusText = (TextView) findViewById(R.id.txtStatusError);
        toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

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
        }; // drawer Toggle Object Made
        drawer.setDrawerListener(mDrawerToggle); // drawer Listener set to the drawer toggle
        mDrawerToggle.syncState();

        try {
            uri.setText(sharedPreferences.getString("last_uri", ""));

            //TODO: remove this when done
            username.setText("frostbyte");
            password.setText("fr0st");
            uri.setText("https://demo23.sclable.me/mobile3/mobile-api");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startWidgetActivity() {

        Intent i = new Intent(getApplicationContext(), WidgetActivity.class);
        //i.putExtra("widget", new_widget.id);
        i.putExtra("username", username.getText().toString());
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (appLogic.currentWidget != null)
            startWidgetActivity();

        initViewItems();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        // TODO: This is a temporary login protocol. Please fix me.
        //System.out.println("OnClick Funktion called!");
        switch (view.getId()) {
            case R.id.bLogin:
                if (!uri.getText().toString().isEmpty() && !username.getText().toString().isEmpty()
                        && !password.getText().toString().isEmpty()) {
                    hideKeyboard();
                    statusText.setVisibility(View.GONE);
                    setContentView(R.layout.loading);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("last_uri", String.valueOf(uri.getText()));
                    editor.commit();
                    configFile.server_uri = String.valueOf(uri.getText());
                    //System.out.println("The uri: " + configFile.server_uri);
                    communicationDaemon.User = username.getText().toString();
                    communicationDaemon.Pass = password.getText().toString();
                    appLogic.login();
                } else {
                    // TODO: Tell the user that the URI field is empty.
                }
                break;

        }
    }

    public void onBackPressed()
    {
        if (this.drawer.isDrawerOpen(GravityCompat.START)) {
            this.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

    }

    public void loginFailed(short i) {

        Log.i(CLASS_NAME,"Login failed because: " + i);
        final short code = i;
        // failurecode 0 : URI fail
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_main);
                initViewItems();
                showErrorMessage(code);
            }
        });

    }
    private void showErrorMessage(short i)
    {
        switch(i)
        {
            case 0 :
                statusText.setText("Could not connect to Server, please verify your URI or Internet Connection!");
                statusText.setVisibility(View.VISIBLE);
                break;
        }
    }
}