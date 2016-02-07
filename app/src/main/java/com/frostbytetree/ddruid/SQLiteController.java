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

    }
}
