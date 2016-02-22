package com.frostbytetree.ddruid;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

// This Background Thread is used to store and load the data from a local SQLite DB.

    //TODO Interface with the SQLite DB
    //TODO Load Raw Data from memory and update the DB if needed
    //TODO Load the Config File from memory and update the DB if needed
    //TODO Provide Raw Data to the app if mode is "starting" or "offline"
    //TODO Provide the Config File to the app if mode is "starting" or "offline"

public class SQLDaemon extends Thread{
    private static SQLDaemon ourInstance = new SQLDaemon();
    private static short my_id = 3;
    IACInterface commInterface;
    AppLogic appLogic;
    CommunicationDaemon communicationDaemon;
    Data data;

    ArrayList<Message> local_pile = new ArrayList<Message>();

    private int number_of_dbs;
    private SQLiteDatabase[] local_storage;
    private String[] local_storage_names;

    SharedPreferences preferences;

    public static SQLDaemon getInstance() {
        return ourInstance;
    }

    private SQLDaemon() {
        commInterface = IACInterface.getInstance();
        appLogic = AppLogic.getInstance();
        data = Data.getInstance();
    }

    @Override
    synchronized public void run(){

        System.out.println("Thread Started !");

        do {
            System.out.println("Test running!");
            try {
                // Checks the messages for news that address the SQLDaemon.
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
                //Thread.sleep(thread_throttling);
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
            statusMarshalling(message);
            return;
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
                // Got login procedure call.
                checkForLoginData(message);
                break;
            case 100:
                // Got get config file call;
                break;
            case 101:
                // Got get table call;
                break;
            case 104:
                // Got get Operations from store;
                break;
            case 200:
                // Got post config file;
                break;
            case 201:
                // Got post table call;
                break;
            case 202:
                // Got post create data set call;
                break;
            case 203:
                // Got post update data set call;
                break;
            case 204:
                // Got post Operations to store;
                break;
            // TODO: Define behaviour for the other operation types.
        }
    }

    private void checkForLoginData(Message message) {  // This method checks to see if the login data is already saved.
        if(number_of_dbs > 0) {
            for (int x = 0; x < local_storage_names.length; x++)
                if (message.requested_operation.REST_command.matches(local_storage_names[x])) {
                    // TODO: Implement authentication procedure check.
                    message.requested_operation.status = 3;
                    synchronized (appLogic){
                        appLogic.notify();
                    }
                    break;
                }
        }
        else {
            message.requested_operation.status = 5;
            synchronized (appLogic){
                appLogic.notify();
            }
        }

    }

    private void postExecutionForwarder(Message finished_operation) {  // This function determines what to do
        // after getting an answer for your message.
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

    public void prepare_dbs() {
        try {
            number_of_dbs = preferences.getInt("number_of_dbs", 0);
            local_storage_names = new String[number_of_dbs];
            local_storage = new SQLiteDatabase[number_of_dbs];
            JSONArray json_names = new JSONArray();

            json_names = new JSONArray(preferences.getString("saved_dbs", "[]"));
            for(int i = 0; i < json_names.length(); i++)
                local_storage_names[i] = json_names.getString(i);
        } catch (JSONException e) {
            local_storage_names = new String[0];
            local_storage = new SQLiteDatabase[0];
            e.printStackTrace();
        }
        System.out.println("The amount of dbs locally is: " + local_storage_names.length);
    }
}
