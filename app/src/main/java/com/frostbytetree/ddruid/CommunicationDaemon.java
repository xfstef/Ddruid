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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        do {
            this_time = System.currentTimeMillis();
            if (this_time - last_time > 1000) {
                System.out.println("The service current time is: " + rawData.getTest());
                rawData.setTest("Tvoj Kurac !");
                System.out.println("The service current time is: " + rawData.getTest());
                last_time = this_time;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(true);
    }
}
