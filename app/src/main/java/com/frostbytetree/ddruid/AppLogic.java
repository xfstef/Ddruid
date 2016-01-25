package com.frostbytetree.ddruid;

import android.os.Looper;

/**
 * Created by XfStef on 11/27/2015.
 */

public class AppLogic extends Thread{
    private static AppLogic ourInstance = new AppLogic();
    long this_time, last_time;
    Data data = Data.getInstance();
    UIBuilder uiBuilder = UIBuilder.getInstance();
    private static short my_id = 1;
    IACInterface commInterface = IACInterface.getInstance();
    Widget currentWidget;
    ConfigFile configFile = ConfigFile.getInstance();
    ConfigFileInterpreter configFileInterpreter = ConfigFileInterpreter.getInstance();
    MainActivity mainActivity;
    WidgetViews widgetViews = WidgetViews.getInstance();
    short thread_throttling = 5000; // This option is used to determine how much the Thread sleeps.
                                    // 5000 - Idle mode: the app is minimized with no bkg operation.
                                    // 1000 - Passive mode: app is sending / getting data.
                                    // 100 - Active mode: app is actively working with the UI.

    public static AppLogic getInstance() {
        return ourInstance;
    }

    private AppLogic() {
        this_time = System.currentTimeMillis();
        last_time = this_time;
    }

    void setCurrentWidget(Widget the_widget){
        currentWidget = the_widget;
    }

    @Override
    synchronized public void run(){

        System.out.println("Thread Started !");
        data.setPersistancy(true);

        do {
            System.out.println("Test running!");
            try {
                // Checks the messages for news that address the AppLogic.
                synchronized(commInterface.message_buffer_lock) {
                    for (int x = 0; x < commInterface.message_buffer.size(); x++)
                        if (commInterface.message_buffer.get(x).target_id == my_id &&
                                commInterface.message_buffer.get(x).requested_operation.status == 0) {
                            // TODO: run requested operations
                        }else if(commInterface.message_buffer.get(x).caller_id == my_id &&
                                commInterface.message_buffer.get(x).requested_operation.status != 6)
                            postExecutionForwarder(commInterface.message_buffer.get(x));
                }
                Thread.sleep(thread_throttling);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(true);
    }

    // This function looks at the messages that the AppLogic got and interprets them.
    private void postExecutionForwarder(Message finished_operation){
        switch(finished_operation.requested_operation.status){
            case 3:
                switch(finished_operation.requested_operation.type){
                    case 110:   // Got Config File successfully. Trying to interpret it now.
                        configFileInterpreter.buildWidgets();
                        configFileInterpreter.buildDataModels();
                        setCurrentWidget(widgetViews.the_widgets.get(widgetViews.the_widgets.size()-1));
                        mainActivity.startWidgetActivity();
                        thread_throttling = 5000;
                        break;
                    // TODO: Implement the rest of possible post successful operation calls
                }
                break;
            case 5:
                switch (finished_operation.requested_operation.type){
                    case 110:   // Did not get the Config File successfully. Retrying soon.
                        // TODO: Decide what to do in case the comm Daemon couldn't get the cfg file.
                        break;
                    // TODO: Implement the rest of possible post failed operation calls
                }
                break;
        }
        finished_operation.requested_operation.status = 6;
    }

    public void initLoginProc() {
        String user, pass;  // TODO: Get the real user name and pass and send them to the server.
        // TODO: ------------------------- Automate this whole procedure !!! -----------------------
        Message login_procedure = new Message();
        login_procedure.caller_id = my_id;
        login_procedure.target_id = 2;  // TODO: set the target ID with a variable.
        login_procedure.current_rowstamp = commInterface.rowstamp;
        commInterface.rowstamp++;
        login_procedure.priority = 0;   // TODO: set priority with a variable.
        login_procedure.requested_operation = new Operation();
        login_procedure.requested_operation.type = 110;   // TODO: use a variable.
        login_procedure.requested_operation.REST_command =
                "http://82.223.15.251/config.json";
                //"https://demo23.sclable.me/mobile/sclable-mobile-service/config";
        login_procedure.requested_operation.the_table = null;
        login_procedure.requested_operation.status = 0;
        // -----------------------------------------------------------------------------------------


        /*
        If no config file download it and interpret it
        if it is so then just switch to widget
         */
        if(configFile.json_form == null) {
            synchronized (commInterface.message_buffer_lock) {
                commInterface.message_buffer.add(login_procedure);
            }
            thread_throttling = 100;
        }else {
            // The last widget is always the widget menu
            // For now when the user goes back then widget menu is displayed fist when rejoining
            setCurrentWidget(widgetViews.the_widgets.get(widgetViews.the_widgets.size()-1));
            mainActivity.startWidgetActivity();
        }

    }
}
