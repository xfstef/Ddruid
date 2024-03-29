package com.frostbytetree.ddruid;

import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by XfStef on 12/25/2015.
 */

// This is the Inner App Communication Interface which serves to relay messages between the main
// activities, the communication service and the SQLite Controller. Only Threads may create and
// server messages between them.

public class IACInterface {
    private static IACInterface ourInstance = new IACInterface();
    ArrayList<Message> message_buffer = new ArrayList<Message>();
    Object message_buffer_lock = new Object();
    int rowstamp;   // To be incremented whenever a new message is created.

    public static IACInterface getInstance() {

        return ourInstance;
    }

    private IACInterface() {
    }
}

class Message {
    int current_rowstamp;   // Is set when the message is created.
    short caller_id;
    short target_id;
    Operation requested_operation;
    short priority; // 0 - Critical
                    // 1 - Urgent
                    // 2 - Future To Do
    AppCompatActivity caller_widget;
    IDataInflateListener iDataInflateListener;
}

class Operation {
    short status;   // The Operation Status is used to display the current progress regarding the
                    // message. These can be:
                    // 0 - Message sent but not acknowledged;
                    // 1 - Message acknowledged and queued for processing;
                    // 2 - Operation is running;
                    // 3 - Operation finished successfully;
                    // 4 - No server connection, retrying soon;
                    // 5 - Operation generated ERROR;
                    // 6 - Operation finished and is now marked as archived.
    short type;     // Get or Post data from or to server, SQLite, RawData using a 3 digit number:
                    //   0 - GET Login TOKEN;
                    // 1xx - GET; 2xx - POST;
                    // x0x - SQLite DB; x1x - Server;
                    // xx0 - Config File; xx1 - DB Table; xx2 - Create DS; xx3 - Edit DS;
                    // xx4 - Operations Store; TODO: finish definitions.
    String REST_command;    // If a REST command is needed, else this string is NULL.
    Table the_table;   // If a table is needed then a data model will be given.
    JSONObject sclable_object;  // Used to send POST and DELETE commands to Sclable.
    DataSet new_post_set = null;
    String table_action;    // Used to save the table + the action names.
}