package com.frostbytetree.ddruid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
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
                    sortStatus(local_pile.get(0));
                    local_pile.remove(0);
                }
                Thread.sleep(thread_throttling);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(true);
    }

    private void sortStatus(Message message) {
        switch(message.requested_operation.status){
            case 1:
                message.requested_operation.status = 2;
                sortType(message);
                break;
            // TODO: Define behaviour for the other operation statuses.
        }
    }

    private void sortType(Message message) {
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

        /*Request comm_request = new Request();
        Response comm_response = new Response(comm_request);
        Client comm_client;
        // TODO: Add missing elements, such as Cookies, etc..

        if(establishProtocol(message.requested_operation.REST_command))
            comm_client = new Client(Protocol.HTTPS);
        else
            comm_client = new Client(Protocol.HTTP);

        comm_request.setMethod(Method.GET);
        comm_request.setResourceRef(message.requested_operation.REST_command);
        comm_response = comm_client.handle(comm_request);

        System.out.println("Server answer: " + comm_response.getEntityAsText());*/

        ClientResource online_resource = new ClientResource(message.requested_operation.REST_command);
        Representation representation = online_resource.get();
        JSONObject response = null;
        try {
            response = new JSONObject(representation.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Received: " + response.toString());

    }

    private boolean establishProtocol(String rest_command) {
        String command_protocol = rest_command.substring(0, 5);
        if(command_protocol.equals("https"))
            return true;
        else
            return false;
    }

    private void sortByPriority() { // TODO: Sort current messages according to priority.

    }
}
