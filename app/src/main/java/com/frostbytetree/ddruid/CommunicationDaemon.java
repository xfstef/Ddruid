package com.frostbytetree.ddruid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

public class CommunicationDaemon extends Thread{
    private static CommunicationDaemon ourInstance = new CommunicationDaemon();
    long this_time, last_time;
    Data data = Data.getInstance();
    private static short my_id = 2;
    IACInterface commInterface = IACInterface.getInstance();
    ConfigFile cfgFile = ConfigFile.getInstance();
    AppLogic appLogic = AppLogic.getInstance();
    SclableURIS sclableURIS = SclableURIS.getInstance();
    SQLDaemon sqlDaemon;
    DataInterpreter dataInterpreter = DataInterpreter.getInstance();
    String User = null;
    String Pass = null;
    String sessionToken = null;
    short thread_throttling = 1000; // This option is used to determine how much the Thread sleeps.
                                    // 5000 - Idle mode: the app is minimized with no bkg operation.
                                    // 1000 - Passive mode: app is sending / getting data.
                                    // 100 - Active mode: app is actively working with the UI.

    ArrayList<Message> local_pile = new ArrayList<Message>();

    public static CommunicationDaemon getInstance() {
        return ourInstance;
    }

    private CommunicationDaemon() {
        this_time = System.currentTimeMillis();
        last_time = this_time;

    }

    @Override
    synchronized public void run(){

        do{
            //System.out.println("Service Thread Started ! " + persistency++);
            try {
                // Searching the inbox for new messages and deleting the old finished jobs.
                synchronized(commInterface.message_buffer_lock) {
                    for (int x = 0; x < commInterface.message_buffer.size(); x++)
                        if (commInterface.message_buffer.get(x).target_id == my_id &&
                                commInterface.message_buffer.get(x).requested_operation.status == 0) {
                            // This part adds all our jobs to the local jobs buffer.
                            commInterface.message_buffer.get(x).requested_operation.status = 1;
                            local_pile.add(commInterface.message_buffer.get(x));
                        }
                }

                // Processing found / still ongoing messages.
                sortByPriority();
                for(int x = 0; x < local_pile.size(); x++){
                    if(local_pile.get(x).requested_operation.status != 6)
                        callerMarshalling(local_pile.get(x));
                }
                this.wait();
                //Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(true);
    }

    private void callerMarshalling(Message message){    // Looks at who the message is directed to.
        if(message.caller_id == my_id) {
            postExecutionForwarder(message);
            return;
        }
        if(message.target_id == my_id){
            statusMarshalling(message);
            return;
        }
    }

    // This function looks at the responses that the CommDaemon got and interprets them.
    private void postExecutionForwarder(Message finished_operation){
        switch(finished_operation.requested_operation.status){
            case 2: // This means that the app is either in offline mode or the server is not reachable.
                break;
            case 3: // Operation Successful.
                finished_operation.requested_operation.status = 6;
                break;
            case 5: // Operation Error.
                // TODO: Decide what happens now.
                break;
        }
    }

    private void statusMarshalling(Message message) {   // Calls the appropriate message processing
        // procedure according to its status type.
        // TODO: Enable background processing of bigger requests so that the app communicates async.
        switch(message.requested_operation.status){
            case 1:
                message.requested_operation.status = 2;
                typeMarshalling(message);
                break;
            // TODO: Define behaviour for the other operation statuses.
        }
    }

    private void typeMarshalling(Message message) { // Calls the appropriate message processing
        // procedure according to its type.
        switch(message.requested_operation.type){
            case 0:
                firstContact(message);
                break;
            case 110:
                getConfigurations(message);
                break;
            case 111:
                getTableDataFromServer(message);
                break;
            case 212:
                postToServer(message);
                break;
            case 213:
                postToServer(message);
                break;
            // TODO: Define behaviour for the other operation types.
        }
    }

    private void firstContact(Message message){
        ClientResource online_resource = new ClientResource(message.requested_operation.REST_command);
        Representation representation = null;
        JSONObject response = null;

        try {
            representation = online_resource.get();
            try {
                response = new JSONObject(representation.getText());
                if(response.keys().next().toString().matches("v1")){
                    JSONObject child = response.getJSONObject("v1");
                    sclableURIS.login = child.getString("login");
                    sclableURIS.config = child.getString("config");
                    sclableURIS.data = child.getString("data");
                    sclableURIS.data_single = child.getString("data") + "/";
                }
            } catch (JSONException e) {
                message.requested_operation.status = 5;
                synchronized (appLogic){
                    appLogic.notify();
                }
                e.printStackTrace();
                return;
            }
        }catch (Exception e) {
            message.requested_operation.status = 5;
            synchronized (appLogic){
                appLogic.notify();
            }
            e.printStackTrace();
            return;
        }

        // TODO: Build switch case so that you can see what type of server this is.
        message.requested_operation.REST_command = sclableURIS.login;
        System.out.println("URIS: " + sclableURIS.login + ", " + sclableURIS.config);
        getLogin(message);
    }

    private void getLogin(Message message) {
        ClientResource online_resource = new ClientResource(message.requested_operation.REST_command);
        online_resource.setMethod(Method.POST);

        Representation representation = null;
        JSONObject response = null;
        message.requested_operation.status = 2;

        Series<Header> headers = new Series<Header>(Header.class);
        //online_resource.getRequestAttributes().put("org.restlet.https.headers", headers);
        online_resource.getRequestAttributes().put("org.restlet.http.headers", headers);
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");

        try{
            JSONObject login_res = new JSONObject();
            login_res.put("username", User);
            login_res.put("password", Pass);
            representation = online_resource.post(login_res.toString(), MediaType.APPLICATION_JSON);
            try{
                response = new JSONObject(representation.getText());
                sessionToken = response.getString("token");
                System.out.println("The login response: " + sessionToken);

                message.requested_operation.status = 3;
                synchronized (appLogic){
                    appLogic.notify();
                }
            } catch (JSONException e){
                e.printStackTrace();
                message.requested_operation.status = 5;
            }
        } catch (Exception e){
            e.printStackTrace();
            message.requested_operation.status = 4;
        }
    }

    private void postToServer(Message message) {
        // TODO: ----------------------- Check for user credentials / login ------------------------
        //
        // -----------------------------------------------------------------------------------------

        ClientResource online_resource = new ClientResource(message.requested_operation.REST_command);
        online_resource.setMethod(Method.POST);
        //System.out.println("fduifuidf: " + online_resource.toString());
        Representation representation = null;
        JSONObject response = null;
        message.requested_operation.status = 2;

        Series<Header> headers = new Series<Header>(Header.class);
        //online_resource.getRequestAttributes().put("org.restlet.https.headers", headers);
        online_resource.getRequestAttributes().put("org.restlet.http.headers", headers);
        headers.add("Authorization", sessionToken);
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");

        try {
            representation = online_resource.post(message.requested_operation.sclable_object.toString());
            try {

                response = new JSONObject(representation.getText());
                System.out.println("Response: " + response.toString());
                JSONArray answer = response.getJSONArray(message.requested_operation.table_action);
                JSONObject data = answer.getJSONObject(0);
                if(data.has("data")) {
                    JSONObject the_data = data.getJSONObject("data");
                    DataSet post_set = new DataSet();
                    for (int z = 0; z < message.requested_operation.the_table.attribute_count; z++) {
                        post_set.set.add(String.valueOf(the_data.get(message.requested_operation.the_table.attributes.get(z).name)));
                    }
                    message.requested_operation.new_post_set = post_set;
                }
                if(Integer.valueOf(data.getString("transaction")) == message.current_rowstamp) {
                    message.requested_operation.status = 3;
                    synchronized (appLogic) {
                        appLogic.notify();
                    }
                }else{
                    message.requested_operation.status = 5;
                    synchronized (appLogic){
                        appLogic.notify();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                message.requested_operation.status = 5;
                synchronized (appLogic){
                    appLogic.notify();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.requested_operation.status = 4;
            synchronized (appLogic){
                appLogic.notify();
            }
        }
    }

    private void getTableDataFromServer(Message message) {
        // TODO: ----------------------- Check for user credentials / login ------------------------
        //
        // -----------------------------------------------------------------------------------------

        ClientResource online_resource = new ClientResource(message.requested_operation.REST_command);
        Representation representation = null;
        JSONObject response = null;
        message.requested_operation.status = 2;

        Series<Header> headers = new Series<Header>(Header.class);
        //online_resource.getRequestAttributes().put("org.restlet.https.headers", headers);
        online_resource.getRequestAttributes().put("org.restlet.http.headers", headers);
        headers.add("Authorization", sessionToken);
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        System.out.println("Address: " + online_resource.toString());

        try {
            representation = online_resource.get();
            try {
                response = new JSONObject(representation.getText());
                synchronized (data.data_lock){
                    data.temp_object = response;
                }
                dataInterpreter.processTableData(message);
                message.requested_operation.status = 3;
                synchronized (appLogic){
                    appLogic.notify();
                }
            } catch (JSONException e) {
                message.requested_operation.status = 5;
                synchronized (appLogic){
                    appLogic.notify();
                }
                e.printStackTrace();
            }
        }catch (Exception e) {
            message.requested_operation.status = 5;
            synchronized (appLogic){
                appLogic.notify();
            }
            e.printStackTrace();
        }
    }

    private void getConfigurations(Message message) {
        // TODO: ----------------------- Check for user credentials / login ------------------------
        //
        // -----------------------------------------------------------------------------------------

        ClientResource online_resource = new ClientResource(message.requested_operation.REST_command);
        Representation representation = null;
        JSONObject response = null;
        message.requested_operation.status = 2;

        Series<Header> headers = new Series<Header>(Header.class);
        //online_resource.getRequestAttributes().put("org.restlet.https.headers", headers);
        online_resource.getRequestAttributes().put("org.restlet.http.headers", headers);
        headers.add("Authorization", sessionToken);
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");

        try {
            representation = online_resource.get();
            try {
                response = new JSONObject(representation.getText());
                synchronized (cfgFile.cfg_file_lock){
                    cfgFile.json_form = response;
                    if(cfgFile.json_form.has("app_color")) {
                        switch (cfgFile.json_form.getString("app_color")) {
                            case "teal":
                                cfgFile.custom_color = R.style.AppTheme;
                                break;
                            case "orange":
                                cfgFile.custom_color = R.style.AppTheme_Orange;
                                break;
                            case "purple":
                                cfgFile.custom_color = R.style.AppTheme_Indigo;
                                break;
                        }
                    }
                    else
                        cfgFile.custom_color = R.style.AppTheme;
                    if(cfgFile.json_form.has("user_name")){
                        cfgFile.username = cfgFile.json_form.getString("user_name");
                    }
                }

                message.requested_operation.status = 3;
                synchronized (appLogic){
                    appLogic.notify();
                }
            } catch (JSONException e) {
                message.requested_operation.status = 5;
                synchronized (appLogic){
                    appLogic.notify();
                }
                e.printStackTrace();
            }
        }catch (Exception e) {
            message.requested_operation.status = 5;
            synchronized (appLogic){
                appLogic.notify();
            }
            e.printStackTrace();
        }
    }

    private void sortByPriority() { // TODO: Sort current messages according to priority.

    }
}