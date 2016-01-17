package com.frostbytetree.ddruid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.IOException;
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

        System.out.println("Service Thread Started !");

        do{
            try {
                // Searching the inbox for new messages and deleting the old finished jobs.
                synchronized(commInterface.message_buffer_lock) {
                    for (int x = 0; x < commInterface.message_buffer.size(); x++)
                        if (commInterface.message_buffer.get(x).target_id == my_id &&
                                commInterface.message_buffer.get(x).requested_operation.status == 0) {
                            commInterface.message_buffer.get(x).requested_operation.status = 1;
                            local_pile.add(commInterface.message_buffer.get(x));
                        }else if(commInterface.message_buffer.get(x).caller_id == my_id &&
                                commInterface.message_buffer.get(x).requested_operation.status == 3)
                            commInterface.message_buffer.get(x).requested_operation.status = 6;
                            // TODO: Check if there are other functions that need to be called afterwards.
                }

                // Processing found / still ongoing messages.
                sortByPriority();
                for(int x = 0; x < local_pile.size(); x++){
                    statusMarshalling(local_pile.get(x));
                }

                Thread.sleep(thread_throttling);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(true);
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
            case 110:
                getConfigurations(message);
                break;
            // TODO: Define behaviour for the other operation types.
        }
    }

    private void getConfigurations(Message message) {
        System.out.println("Message Rowstamp: " + message.current_rowstamp);

        // TODO: ----------------------- Check for user credentials / login ------------------------
        //
        // -----------------------------------------------------------------------------------------

        ClientResource online_resource = new ClientResource(message.requested_operation.REST_command);
        Representation representation = online_resource.get();
        JSONObject response = null;

        try {
            response = new JSONObject(representation.getText());
        } catch (JSONException e) {
            message.requested_operation.status = 5;
            e.printStackTrace();
        } catch (IOException e) {
            message.requested_operation.status = 5;
            e.printStackTrace();
        }

        synchronized (cfgFile.cfg_file_lock){
            cfgFile.json_form = response;
        }

        message.requested_operation.status = 3;

    }

    private void sortByPriority() { // TODO: Sort current messages according to priority.

    }
}
