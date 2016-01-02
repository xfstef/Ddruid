package com.frostbytetree.ddruid;

/**
 * Created by XfStef on 11/27/2015.
 */
public class AppLogic extends Thread{
    private static AppLogic ourInstance = new AppLogic();
    long this_time, last_time;
    RawData rawData = RawData.getInstance();
    UIBuilder uiBuilder = UIBuilder.getInstance();
    private static short my_id = 1;
    IACInterface commInterface = IACInterface.getInstance();

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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(true);
    }

}
