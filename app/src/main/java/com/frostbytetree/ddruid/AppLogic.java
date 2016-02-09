package com.frostbytetree.ddruid;

import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

public class AppLogic extends Thread{
    private static AppLogic ourInstance = new AppLogic();
    long this_time, last_time;
    Data data = Data.getInstance();
    UIBuilder uiBuilder;
    private static short my_id = 1;
    IACInterface commInterface;
    Widget currentWidget;
    ConfigFile configFile;
    ConfigFileInterpreter configFileInterpreter;
    DataInterpreter dataInterpreter;
    MainActivity mainActivity;
    WidgetViews widgetViews;
    CommunicationDaemon communicationDaemon;

    // This approach is used when selecting a object within a list, because passing objects via
    // activity less efficient
    DataSet temporary_dataSet;
    short thread_throttling = 5000; // This option is used to determine how much the Thread sleeps.
                                    // 5000 - Idle mode: the app is minimized with no bkg operation.
                                    // 1000 - Passive mode: app is sending / getting data.
                                    // 100 - Active mode: app is actively working with the UI.
    short backend = 0;  // This option is used to define which type of server the connection uses.
                        // 0 - Custom Server (Default);
                        // 1 - Sclable.
    String uri  =   null;
                    //"http://82.223.15.251";
                    //"https://demo23.sclable.me/mobile/sclable-mobile-service";
    ArrayList<Message> local_pile = new ArrayList<Message>();

    public static AppLogic getInstance() {
        return ourInstance;
    }

    private AppLogic() {
        this_time = System.currentTimeMillis();
        last_time = this_time;
        uiBuilder = UIBuilder.getInstance();
        commInterface = IACInterface.getInstance();
        configFile = ConfigFile.getInstance();
        configFileInterpreter = ConfigFileInterpreter.getInstance();
        widgetViews = WidgetViews.getInstance();
        dataInterpreter = DataInterpreter.getInstance();
        configFileInterpreter.appLogic = this;
    }

    void setCurrentWidget(Widget the_widget){
        currentWidget = the_widget;
    }

    @Override
    synchronized public void run(){

        System.out.println("Thread Started !");

        do {
            System.out.println("Test running!");
            try {
                // Checks the messages for news that address the AppLogic.
                synchronized(commInterface.message_buffer_lock) {
                    for (int x = 0; x < commInterface.message_buffer.size(); x++)
                        if (commInterface.message_buffer.get(x).target_id == my_id &&
                                commInterface.message_buffer.get(x).requested_operation.status == 0) {
                            commInterface.message_buffer.get(x).requested_operation.status = 1;
                            local_pile.add(commInterface.message_buffer.get(x));
                        }
                }
                //Thread.sleep(thread_throttling);
                for(int x = 0; x < local_pile.size(); x++){
                    if(local_pile.get(x).requested_operation.status != 6)
                        callerMarshalling(local_pile.get(x));
                }
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(true);
    }

    private void callerMarshalling(Message message) {
        if(message.caller_id == my_id) {
            postExecutionForwarder(message);
            return;
        }
        if(message.target_id == my_id){
            // TODO: Add new message behaviour.
            return;
        }
    }

    // This function looks at the messages that the AppLogic got and interprets them.
    private void postExecutionForwarder(Message finished_operation){
        switch(finished_operation.requested_operation.status){
            case 2: // This means that the app is either in offline mode or the server is not reachable.
                break;
            case 3: // Operation Successful.
                switch(finished_operation.requested_operation.type){
                    case 0: // Got session token.
                        finished_operation.requested_operation.status = 6;
                        getConfig();
                        break;
                    case 110:   // Got Config File successfully. Trying to interpret it now.
                        configFileInterpreter.startStartupProcess();
                        setCurrentWidget(widgetViews.the_widgets.get(widgetViews.the_widgets.size() - 1));
                        finished_operation.requested_operation.status = 6;
                        mainActivity.startWidgetActivity();
                        break;
                    case 111:   // Got a table from the server succesfully. Trying to read it now.
                        dataInterpreter.processTableData(finished_operation);
                        finished_operation.requested_operation.status = 6;
                        break;
                    case 212:   // Got the POST Operations finished successfully message.
                        finished_operation.requested_operation.status = 6;
                        break;
                    // TODO: Implement the rest of possible post successful operation calls
                }
                break;
            case 4: // No server connection.
                switch(finished_operation.requested_operation.type) {
                    case 0:
                        /*
                        If no config file download it and interpret it
                        if it is so then just switch to widget
                        */
                        if(configFile.json_form != null) {
                            setCurrentWidget(widgetViews.the_widgets.get(widgetViews.the_widgets.size() - 1));
                            mainActivity.startWidgetActivity();
                            finished_operation.requested_operation.status = 6;
                        }
                        break;
                    case 110:
                        break;
                    case 111:
                        break;
                    case 212:
                        break;
                }
                break;
            case 5: // Operation Error.
                switch (finished_operation.requested_operation.type){
                    case 0: // Could not get session token.
                        mainActivity.loginFailed((short) 0); // TODO: Add failure code.
                        break;
                    case 110:   // Did not get the Config File successfully.
                        mainActivity.loginFailed((short) 0); // TODO: Add failure code.
                        break;
                    case 111:   // Did not get a server table succesfully.
                        System.out.println("Got table data: " + data.temp_object.toString());
                        break;
                    case 212:   // Could not post to server error.
                        System.out.println("Got error from server.");
                        break;
                    // TODO: Implement the rest of possible post failed operation calls
                }
                break;
        }
    }

    public void getTableData(Table the_table, AppCompatActivity caller){
        // TODO: Check if the data exists locally. If not then download it.

        String table_address = the_table.table_name.replace(".","/");
        // TODO: ------------------------- Automate this whole procedure !!! -----------------------
        Message download_table_data_procedure = new Message();
        download_table_data_procedure.caller_id = my_id;
        download_table_data_procedure.target_id = 2;  // TODO: set the target ID with a variable.
        download_table_data_procedure.current_rowstamp = commInterface.rowstamp;
        commInterface.rowstamp++;
        download_table_data_procedure.priority = 0;   // TODO: set priority with a variable.
        download_table_data_procedure.requested_operation = new Operation();
        download_table_data_procedure.requested_operation.type = 111;   // TODO: use a variable.
        download_table_data_procedure.requested_operation.REST_command = configFile.server_uri + "/data/" + table_address;
        download_table_data_procedure.requested_operation.the_table = the_table;
        download_table_data_procedure.requested_operation.status = 0;
        // -----------------------------------------------------------------------------------------

        download_table_data_procedure.iDataInflateListener = (IDataInflateListener) caller;
        download_table_data_procedure.caller_widget = caller;

        synchronized (commInterface.message_buffer_lock) {  // Adding message.
            commInterface.message_buffer.add(download_table_data_procedure);
            local_pile.add(commInterface.message_buffer.get(commInterface.message_buffer.size()-1));
        }
        synchronized (communicationDaemon) {    // Waking up communicationDaemon
            communicationDaemon.notify();
        }
    }

    public void login(String user, String pass) {
        // TODO: ------------------------- Automate this whole procedure !!! -----------------------
        Message login_procedure = new Message();
        login_procedure.caller_id = my_id;
        login_procedure.target_id = 2;  // TODO: set the target ID with a variable.
        login_procedure.current_rowstamp = commInterface.rowstamp;
        commInterface.rowstamp++;
        login_procedure.priority = 0;   // TODO: set priority with a variable.
        login_procedure.requested_operation = new Operation();
        login_procedure.requested_operation.type = 0;   // TODO: use a variable.
        login_procedure.requested_operation.REST_command = configFile.server_uri;
        login_procedure.requested_operation.the_table = null;
        login_procedure.requested_operation.status = 0;
        // -----------------------------------------------------------------------------------------

        JSONObject login_object = new JSONObject();
        try {
            login_object.put("username", user);
            login_object.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        login_procedure.requested_operation.sclable_object = login_object;

        synchronized (commInterface.message_buffer_lock) {
            commInterface.message_buffer.add(login_procedure);
            local_pile.add(commInterface.message_buffer.get(commInterface.message_buffer.size()-1));
        }
        synchronized (communicationDaemon){
            communicationDaemon.notify();
        }

    }

    public void getConfig() {
        // TODO: ------------------------- Automate this whole procedure !!! -----------------------
        Message get_config = new Message();
        get_config.caller_id = my_id;
        get_config.target_id = 2;  // TODO: set the target ID with a variable.
        get_config.current_rowstamp = commInterface.rowstamp;
        commInterface.rowstamp++;
        get_config.priority = 0;   // TODO: set priority with a variable.
        get_config.requested_operation = new Operation();
        get_config.requested_operation.type = 110;   // TODO: use a variable.
        get_config.requested_operation.REST_command = configFile.server_uri + "/config";
        get_config.requested_operation.the_table = null;
        get_config.requested_operation.status = 0;
        // -----------------------------------------------------------------------------------------

        synchronized (commInterface.message_buffer_lock) {
            commInterface.message_buffer.add(get_config);
            local_pile.add(commInterface.message_buffer.get(commInterface.message_buffer.size()-1));
        }
        synchronized (communicationDaemon) {    // Waking up communicationDaemon
            communicationDaemon.notify();
        }
    }

    // This Function is called when the App wants to send a POST / DELETE message to the server.
    // It requires the new / to be modified DataSet, the action and the table that will be modified.
    public void sendPost(DataSet dataSet, Action action, Table table){

        try{
            sendPostHelper(dataSet, action, table);
        }catch (Exception e){
            e.printStackTrace();
        }

        switch(action.type){
            case 0: // Create.
                // TODO: Implement logic for creating a new tuple successfully.
                break;
            case 1: // Simple Edit.
                // TODO: Implement logic for handling this change in the app.
                break;
            case 2: // Edit with form.
                // TODO: Implement logic for handling this change in the app.
                break;
            case 3: // Delete.
                // TODO: Implement logic for handling this change in the app.
                break;
        }
    }

    private void sendPostHelper(DataSet dataSet, Action action, Table table){
        JSONObject new_object = new JSONObject();
        JSONArray table_action = new JSONArray();
        String t_a_concat = table.table_name + "." + action.name;
        JSONObject action_element = new JSONObject();
        Message post_procedure;

        // TODO: ------------------------- Automate this whole procedure !!! -----------------------
        post_procedure = new Message();
        post_procedure.caller_id = my_id;
        post_procedure.target_id = 2;  // TODO: set the target ID with a variable.
        post_procedure.current_rowstamp = commInterface.rowstamp;
        commInterface.rowstamp++;
        post_procedure.priority = 0;   // TODO: set priority with a variable.
        post_procedure.requested_operation = new Operation();
        post_procedure.requested_operation.type = 212;   // TODO: use a variable.
        post_procedure.requested_operation.REST_command = configFile.server_uri + "/data";
        post_procedure.requested_operation.the_table = table;
        post_procedure.requested_operation.status = 0;
        // -----------------------------------------------------------------------------------------

        try {
            JSONObject data = new JSONObject();
            for(int x = 0; x < action.attributes.size(); x++){
                data.put(action.attributes.get(x).name, dataSet.set.get(x));
            }
            System.out.println("Compressed data: " + data.toString());
            action_element.put("transaction", String.valueOf(post_procedure.current_rowstamp));
            action_element.put("data", data);
            table_action.put(action_element);
            new_object.put(t_a_concat, table_action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        post_procedure.requested_operation.sclable_object = new_object;
        System.out.println("The POST JSON: " + new_object.toString());

        synchronized (commInterface.message_buffer_lock) {  // Adding message.
            commInterface.message_buffer.add(post_procedure);
            local_pile.add(commInterface.message_buffer.get(commInterface.message_buffer.size()-1));
        }
        synchronized (communicationDaemon) {    // Waking up communicationDaemon
            communicationDaemon.notify();
        }
    }
}
