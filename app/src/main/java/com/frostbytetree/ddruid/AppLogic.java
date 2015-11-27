package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */
public class AppLogic extends Thread{
    private static AppLogic ourInstance = new AppLogic();
    long this_time, last_time;
    RawData rawData = RawData.getInstance();

    public static AppLogic getInstance() {
        return ourInstance;
    }

    private AppLogic() {
        this_time = System.currentTimeMillis();
        last_time = this_time;
    }

    @Override
    synchronized public void run(){

        System.out.println("Thread Started !");
        rawData.setPersistancy(true);

        do {
            this_time = System.currentTimeMillis();
            if (this_time - last_time > 1000) {
                System.out.println("The thread current time is: " + rawData.getTest());
                rawData.setTest("Moj Kurac !");
                System.out.println("The thread current time is: " + rawData.getTest());
                last_time = this_time;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(true);
    }
}
