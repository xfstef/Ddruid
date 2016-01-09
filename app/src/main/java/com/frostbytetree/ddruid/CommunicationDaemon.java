package com.frostbytetree.ddruid;

import java.util.ArrayList;

/**
 * Created by XfStef on 11/27/2015.
 */

public class CommunicationDaemon extends Thread{
    private static CommunicationDaemon ourInstance = new CommunicationDaemon();
    long this_time, last_time;
    RawData rawData = RawData.getInstance();
    private static short my_id = 2;
    IACInterface commInterface = IACInterface.getInstance();
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
                synchronized(commInterface.message_buffer_lock) {
                    for(int x = 0; x < commInterface.message_buffer.size(); x++){
                        if(commInterface.message_buffer.get(x).target_id == my_id &&
                                commInterface.message_buffer.get(x).requested_operation.status == 0) {
                            commInterface.message_buffer.get(x).requested_operation.status = 1;
                            local_pile.add(commInterface.message_buffer.get(x));
                        }
                    }
                }
                sortByPriority();
                while(local_pile.size() > 0){
                    handleMessage(local_pile.get(0));
                    local_pile.remove(0);
                }
                Thread.sleep(thread_throttling);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(true);
    }

    private void handleMessage(Message message) {   // TODO: Handle each message accordingly.
        System.out.println("Message Rowstamp: " + message.current_rowstamp);



    }

    private void sortByPriority() { // TODO: Sort current messages according to priority.

    }
}
