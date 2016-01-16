package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */

public class AppLogic extends Thread{
    private static AppLogic ourInstance = new AppLogic();
    long this_time, last_time;
    RawData rawData = RawData.getInstance();
    UIBuilder uiBuilder = UIBuilder.getInstance();
    private static short my_id = 1;
    IACInterface commInterface = IACInterface.getInstance();
    WidgetActivity currentActivity = new WidgetActivity();
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

    void setCurrentWidget(WidgetActivity the_widget){
        currentActivity = the_widget;
    }

    @Override
    synchronized public void run(){

        System.out.println("Thread Started !");
        rawData.setPersistancy(true);

        do {
            try {
                Thread.sleep(thread_throttling);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(true);
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
        login_procedure.requested_operation.data_model = null;
        login_procedure.requested_operation.status = 0;
        // -----------------------------------------------------------------------------------------

        synchronized (commInterface.message_buffer_lock) {
            commInterface.message_buffer.add(login_procedure);
        }
        thread_throttling = 1000;



    }
}
