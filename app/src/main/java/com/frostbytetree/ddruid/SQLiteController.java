package com.frostbytetree.ddruid;

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

public class SQLiteController extends Thread{
    private static SQLiteController ourInstance = new SQLiteController();
    private static short my_id = 3;
    IACInterface commInterface;
    AppLogic appLogic;
    CommunicationDaemon communicationDaemon;
    Data data;

    ArrayList<Message> local_pile = new ArrayList<Message>();

    public static SQLiteController getInstance() {
        return ourInstance;
    }

    private SQLiteController() {
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
                // Checks the messages for news that address the SQLiteController.
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
}
