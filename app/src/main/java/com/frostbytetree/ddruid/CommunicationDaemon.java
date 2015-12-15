package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */
public class CommunicationDaemon extends Thread{
    private static CommunicationDaemon ourInstance = new CommunicationDaemon();
    long this_time, last_time;
    RawData rawData = RawData.getInstance();

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
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(true);
    }
}
