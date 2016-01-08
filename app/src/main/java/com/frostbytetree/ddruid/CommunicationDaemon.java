package com.frostbytetree.ddruid;

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
                if(commInterface.message_buffer.size() != 0){
                    System.out.println("The latest message is:" +
                            commInterface.message_buffer.get(commInterface.message_buffer.size()-1).current_rowstamp);
                }
                Thread.sleep(thread_throttling);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(true);
    }
}
